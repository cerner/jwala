package com.cerner.jwala.tomcat.listener.messaging.jgroups;

import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.jgroups.JChannel;
import org.jgroups.stack.IpAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * A life cycle listener that sends state via JGroups
 *
 * Created by Jedd Cuison on 8/15/2016
 */
public class JGroupsReportingLifeCycleListener implements LifecycleListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(JGroupsReportingLifeCycleListener.class);
    private static final long SCHEDULER_DELAY_INITIAL_DEFAULT = 60;
    private static final long SCHEDULER_DELAY_SUBSEQUENT_DEFAULT = 60;
    private static final int SCHEDULER_THREAD_COUNT_DEFAULT = 1;
    private static final String JGROUPS_COORDINATOR_HOSTNAME = "jgroupsCoordinatorHostname";
    private static final String JGROUPS_COORDINATOR_IP_ADDRESS = "jgroupsCoordinatorIp";
    private static final Object lockObject = new Object();

    private JGroupsMessagingServiceImpl messagingService;
    private JGroupsStateReporter jgroupsStateReporter;

    private String serverId;
    private String serverName;
    private String jgroupsPreferIpv4Stack;
    private String jgroupsConfigXml;
    private String jgroupsCoordinatorIp;
    private String jgroupsCoordinatorHostname;
    private String jgroupsCoordinatorPort;
    private String jgroupsClusterName;
    private long schedulerDelayInitial = SCHEDULER_DELAY_INITIAL_DEFAULT;
    private long schedulerDelaySubsequent = SCHEDULER_DELAY_SUBSEQUENT_DEFAULT;
    private TimeUnit schedulerDelayUnit = TimeUnit.SECONDS;
    private int schedulerThreadCount = SCHEDULER_THREAD_COUNT_DEFAULT;

    private JChannel channel;
    private IpAddress jgroupsDestIpAddr;

    @Override
    public void lifecycleEvent(final LifecycleEvent event) {
        LOGGER.info("LifeCycleEvent received: {} on {}", event.getType(), event.getLifecycle().getStateName());
        synchronized (lockObject) {
            if (messagingService == null) {
                // init messaging service...
                LOGGER.info("Set systems property java.net.preferIPv4Stack to '{}'", jgroupsPreferIpv4Stack);
                System.setProperty("java.net.preferIPv4Stack", jgroupsPreferIpv4Stack);

                try {
                    channel = new JChannel(jgroupsConfigXml);
                    jgroupsDestIpAddr = getDestAddr();
                } catch (final Exception e) {
                    LOGGER.error("Failed to create JGroups channel!", e);
                    return;
                }

                channel.setDiscardOwnMessages(true);
                messagingService = new JGroupsMessagingServiceImpl(channel, jgroupsClusterName);
                try {
                    jgroupsStateReporter = new JGroupsStateReporter(messagingService, serverId, serverName,
                            jgroupsDestIpAddr, schedulerThreadCount,
                            schedulerDelayInitial, schedulerDelaySubsequent, schedulerDelayUnit);
                } catch (final Exception e) {
                    LOGGER.error("Failed to report state!", e);
                }
            }
        }
        final LifecycleState state = event.getLifecycle().getState();
        try {
            jgroupsStateReporter.sendAndRepeat(state);
        } catch (final Exception e) {
            LOGGER.error("Failed to report state!",e);
        }
    }

    private IpAddress getDestAddr() throws Exception {
        if (null != jgroupsCoordinatorIp) {
            return new IpAddress(jgroupsCoordinatorIp + ":" + jgroupsCoordinatorPort);
        } else {
            if (null == this.jgroupsCoordinatorHostname) {
                final String errorMessage = MessageFormat.format("Expecting hostname specified as JGroupsReportingLifecycleListener attribute {0} when no IP address specified for the JGroups coordinator", JGROUPS_COORDINATOR_HOSTNAME);
                LOGGER.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }
            return getIPAddressFromHostname(jgroupsCoordinatorHostname);
        }
    }

    private IpAddress getIPAddressFromHostname(String jgroupsCoordinatorHostname) throws UnknownHostException {
        LOGGER.info("Get IP address for JGroups from hostname {}", jgroupsCoordinatorHostname);
        final InetAddress[] allByName = InetAddress.getAllByName(jgroupsCoordinatorHostname);
        if (null == allByName || 0 == allByName.length) {
            final String errorMessage = MessageFormat.format("Expecting at least 1 IP address from hostname {0}, but were returned none", jgroupsCoordinatorHostname);
            LOGGER.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }

        LOGGER.info("Getting JGroups IP address by hostname {} returned a total of {} addresses", jgroupsCoordinatorHostname, allByName.length);
        ArrayList<InetAddress> filteredAddresses = new ArrayList<>();
        for (InetAddress address : allByName) {
            LOGGER.debug("Filtering JGroups IP address {}", address);
            if (!address.isLoopbackAddress()) {
                if (Boolean.valueOf(jgroupsPreferIpv4Stack) && address instanceof Inet4Address) {
                    LOGGER.debug("-- adding JGroups IPv4 address to list: {}", address);
                    filteredAddresses.add(address);
                } else if (!Boolean.valueOf(jgroupsPreferIpv4Stack) && address instanceof Inet6Address) {
                    LOGGER.debug("-- adding JGroups IPv6 address to list: {}", address);
                    filteredAddresses.add(address);
                }
            }
        }

        if (filteredAddresses.size() != 1) {
            final String errorMessage = MessageFormat.format("Expecting only 1 filtered JGroups IP address, but instead found {0}. Please use the IP address attribute {1} to specify the JGroups coordinator node location.", filteredAddresses.size(), JGROUPS_COORDINATOR_IP_ADDRESS);
            LOGGER.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }

        return new IpAddress(filteredAddresses.get(0), Integer.parseInt(jgroupsCoordinatorPort));
    }

    public void setServerId(final String serverId) {
        this.serverId = serverId;
    }

    public void setServerName(final String serverName) {
        this.serverName = serverName;
    }

    public void setJgroupsPreferIpv4Stack(String jgroupsPreferIpv4Stack) {
        this.jgroupsPreferIpv4Stack = jgroupsPreferIpv4Stack;
    }

    public void setJgroupsConfigXml(final String jgroupsConfigXml) {
        this.jgroupsConfigXml = jgroupsConfigXml;
    }

    public void setJgroupsCoordinatorIp(final String jgroupsCoordinatorIp) {
        this.jgroupsCoordinatorIp = jgroupsCoordinatorIp;
    }

    public void setJgroupsCoordinatorHostname(final String jgroupsCoordinatorHostname) {
        this.jgroupsCoordinatorHostname = jgroupsCoordinatorHostname;
    }

    public void setJgroupsCoordinatorPort(final String jgroupsCoordinatorPort) {
        this.jgroupsCoordinatorPort = jgroupsCoordinatorPort;
    }

    public void setJgroupsClusterName(final String jgroupsClusterName) {
        this.jgroupsClusterName = jgroupsClusterName;
    }

    public void setSchedulerDelayInitial(final String schedulerDelayInitial) {
        try {
            this.schedulerDelayInitial = Long.parseLong(schedulerDelayInitial);
        } catch (final NumberFormatException e) {
            LOGGER.warn(
                    "Failed to convert schedulerDelayInitial value of \"{}\" to long! "
                            + "The default value {} will be used instead.",
                    schedulerDelayInitial, SCHEDULER_DELAY_INITIAL_DEFAULT, e);
        }
    }

    public void setSchedulerDelaySubsequent(final String schedulerDelaySubsequent) {
        try {
            this.schedulerDelaySubsequent = Long.parseLong(schedulerDelaySubsequent);
        } catch (final NumberFormatException e) {
            LOGGER.warn(
                    "Failed to convert schedulerDelaySubsequent value of \"{}\" to long! "
                            + "The default value {} will be used instead.",
                    schedulerDelaySubsequent, SCHEDULER_DELAY_SUBSEQUENT_DEFAULT, e);
        }
    }

    public void setSchedulerDelayUnit(final String schedulerDelayUnit) {
        try {
            this.schedulerDelayUnit = TimeUnit.valueOf(schedulerDelayUnit);
        } catch (final IllegalArgumentException e) {
            LOGGER.warn("Invalid schedulerDelayUnit value \"{}\"! The default value of {} will be used instead.",
                    TimeUnit.SECONDS, e);
        }
    }

    public void setSchedulerThreadCount(final String schedulerThreadCount) {
        try {
            this.schedulerThreadCount = Integer.parseInt(schedulerThreadCount);
        } catch (final NumberFormatException e) {
            LOGGER.warn(
                    "Failed to convert schedulerThreadCount value of \"{}\" to integer! "
                            + "The default value {} will be used instead.",
                    schedulerDelaySubsequent, SCHEDULER_THREAD_COUNT_DEFAULT, e);
        }
    }
}
