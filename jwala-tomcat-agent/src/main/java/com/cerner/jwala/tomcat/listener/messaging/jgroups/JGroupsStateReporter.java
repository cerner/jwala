package com.cerner.jwala.tomcat.listener.messaging.jgroups;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.catalina.LifecycleState;
import org.jgroups.Address;
import org.jgroups.stack.IpAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The reporting mechanism
 *
 * Usage:
 * jgroupsStateReporter.sendAndRepeat(state)
 * 
 * Created by Jedd Cuison on 8/18/2016.
 */
public class JGroupsStateReporter {

    public static final Logger LOGGER = LoggerFactory.getLogger(JGroupsStateReporter.class);

    private ScheduledExecutorService scheduler;
    private final JGroupsMessagingServiceImpl messagingService;
    private LifecycleState state;
    private JGroupsServerInfoMessageBuilder msgBuilder;
    private String serverId;
    private String serverName;
    private IpAddress coordinator;
    private int schedulerThreadCount;
    private long schedulerDelayInitial;
    private long schedulerDelaySubsequent;
    private TimeUnit schedulerDelayUnit;
 
    public JGroupsStateReporter(final JGroupsMessagingServiceImpl messagingService, final String serverId,
            final String serverName, final IpAddress coordinator, final int schedulerThreadCount,
            final long schedulerDelayInitial, final long schedulerDelaySubsequent, final TimeUnit schedulerDelayUnit) {
        this.messagingService = messagingService;
        this.serverId = serverId;
        this.serverName = serverName;
        this.coordinator = coordinator;
        this.schedulerThreadCount = schedulerThreadCount;
        this.schedulerDelayInitial = schedulerDelayInitial;
        this.schedulerDelaySubsequent = schedulerDelaySubsequent;
        this.schedulerDelayUnit = schedulerDelayUnit;
    }

    /**
     * 
     * Send the initial message and then schedule an executor to repeat this message on an interval.
     * 
     * @param newState
     *            the latest state
     */
    public synchronized void sendAndRepeat(final LifecycleState newState) {        
        init(newState);
        sendMsg(serverId, serverName, coordinator);
        schedulePeriodicMsgDelivery(schedulerThreadCount, schedulerDelayInitial, schedulerDelaySubsequent, schedulerDelayUnit);
    }
    
    /**
     * Destroy the scheduler on new state
     * @param newState the latest state
     * @return {@link JGroupsStateReporter} for chaining purposes
     */
    private void init(final LifecycleState newState) {
        if (scheduler != null && !newState.equals(state)) {
            LOGGER.info("Shutting down the scheduler NOW...");
            scheduler.shutdownNow();
            scheduler = null;
        }
        state = newState;
    }

    /**
     * Create a message and send it
     * 
     * @param serverId
     *            the server instance id
     * @param serverName
     *            the server name
     * @param destAddr
     *            the JGroups destination address @return
     *            {@link JGroupsStateReporter} for chaining purposes
     */
    private void sendMsg(final String serverId, final String serverName, final Address destAddr) {
        messagingService.init();
        try {
            final Address channelAddress =
                    messagingService.getChannel().getAddress();
            msgBuilder = new JGroupsServerInfoMessageBuilder().setServerId(serverId).setServerName(serverName)
                    .setState(state).setSrcAddress(channelAddress).setDestAddress(destAddr);
        } catch (final Exception e) {
            throw new JGroupsStateReporterException("Failed to create message!", e);
        }
        messagingService.send(msgBuilder.build()); // send the
                                                   // state
                                                   // details
                                                   // immediately
        if (LifecycleState.STOPPED.equals(state) || LifecycleState.DESTROYED.equals(state)) {
            LOGGER.info("State {} received, destroying messaging service...", state);
            messagingService.destroy();
        }
    }

    /**
     * Schedule periodic message delivery
     * 
     * @param schedulerThreadCount
     *            the thread count
     * @param schedulerDelayInitial
     *            the scheduler's initial delay
     * @param schedulerDelaySubsequent
     *            the scheduler's subsequent delay
     * @param schedulerDelayUnit
     *            {@link TimeUnit} the delay unit
     * @return {@link JGroupsStateReporter} for chaining purposes
     */
    private void schedulePeriodicMsgDelivery(final int schedulerThreadCount,
            final long schedulerDelayInitial, final long schedulerDelaySubsequent, final TimeUnit schedulerDelayUnit) {
        if (messagingService.getChannel().isConnected() && scheduler == null) {
            LOGGER.info(
                    "Creating scheduler with treadCount: {}, initialDelay: {}, subsequentDelay: {} and timeUnit: {}",
                    schedulerThreadCount, schedulerDelayInitial, schedulerDelaySubsequent, schedulerDelayUnit);
            scheduler = Executors.newScheduledThreadPool(schedulerThreadCount);
            scheduler.scheduleAtFixedRate(new JGroupsLifeCycleReporterRunnable(messagingService, msgBuilder),
                    schedulerDelayInitial, schedulerDelaySubsequent, schedulerDelayUnit);
        }
    }
}
