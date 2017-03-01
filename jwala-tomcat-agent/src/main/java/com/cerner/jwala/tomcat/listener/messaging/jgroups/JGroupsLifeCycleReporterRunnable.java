package com.cerner.jwala.tomcat.listener.messaging.jgroups;

import com.cerner.jwala.tomcat.listener.messaging.MessagingService;
import org.jgroups.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A thread that sends a message via JGroups
 *
 * Created by Jedd Cuison on 8/15/2016
 */
public class JGroupsLifeCycleReporterRunnable implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(JGroupsLifeCycleReporterRunnable.class);

    private MessagingService<Message> messagingService;
    private JGroupsServerInfoMessageBuilder msgBuilder;

    public JGroupsLifeCycleReporterRunnable(final MessagingService<Message> messagingService,
                                            final JGroupsServerInfoMessageBuilder msgBuilder) {
        this.messagingService = messagingService;
        this.msgBuilder = msgBuilder;
    }

    @Override
    public void run() {
        LOGGER.info("+++ JGroups life cycle reporting thread {} running...", Thread.currentThread().getId());
        messagingService.send(msgBuilder.build());
        LOGGER.info("--- JGroups life cycle reporting thread {} is done...", Thread.currentThread().getId());
    }
}
