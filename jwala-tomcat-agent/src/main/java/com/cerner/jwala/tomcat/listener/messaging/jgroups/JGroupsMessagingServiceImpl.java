package com.cerner.jwala.tomcat.listener.messaging.jgroups;

import com.cerner.jwala.tomcat.listener.messaging.MessagingService;
import com.cerner.jwala.tomcat.listener.messaging.MessagingServiceException;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the {@link MessagingService} via JGroups
 *
 * Users of this class are required to synchronize the access to init(), send() and destroy() in one block, so that
 * these methods can not be called from different threads at the same time.
 * 
 * Created by Jedd Cuison on 8/15/2016
 */
public class JGroupsMessagingServiceImpl implements MessagingService<Message> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JGroupsMessagingServiceImpl.class);

    private final JChannel channel;
    private final String clusterName;

    public JGroupsMessagingServiceImpl(final JChannel channel, final String clusterName) {
        this.channel = channel;
        this.clusterName = clusterName;
    }

    @Override
    public void init() {
        try {
            LOGGER.info("JGroups channel connecting...");
            connect(clusterName);
        } catch (final Exception e) {
            throw new MessagingServiceException("Failed to initialize the service!", e);
        }
    }

    @Override
    public void send(final Message msg) {
        try {
            connect(clusterName);
            LOGGER.info("Sending msg {}", msg);
            LOGGER.info("Msg content = {}", msg.getObject());
            channel.send(msg);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new MessagingServiceException("Failed to deliver message!", e);
        }
    }

    @Override
    public void destroy() {
        if (channel.isConnected()) {
            LOGGER.info("Closing channel connection...");
            channel.close();
            LOGGER.info("Channel closed");
        }
    }

    /**
     * Connect channel if it's not already connected
     * @param clusterName the cluster to connect to
     * @throws Exception the exception
     */
    private void connect(final String clusterName) throws Exception {
        if (!channel.isConnected())  {
            LOGGER.info("Connecting to JGroups cluster {} using configuredProperties {}", clusterName, this.toString());
            channel.connect(clusterName);
            LOGGER.info("Channel connected");
        }
    }

    public JChannel getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return "JGroupsMessagingServiceImpl{" +
                "clusterName='" + clusterName + '\'' +
                ", channel=" + channel +
                '}';
    }
}
