package com.cerner.jwala.service.impl.spring.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.cerner.jwala.service.MessagingService;

/**
 * {@link MessagingService} implementation using Spring's SimpMessagingTemplate.
 *
 * Created by Jedd Cuison on 3/23/2016.
 */
@Service
public class SimpMessagingServiceImpl implements MessagingService {

    private SimpMessagingTemplate messagingTemplate;
    private final String topic;

    @Autowired
    public SimpMessagingServiceImpl(final SimpMessagingTemplate messagingTemplate,
                                    @Value("${spring.messaging.topic.serverStates:/topic/server-states}")
                                    final String topic) {
        this.messagingTemplate = messagingTemplate;
        this.topic = topic;
    }

    @Override
    public synchronized void send(final Object payLoad) {
        messagingTemplate.convertAndSend(topic, payLoad);
    }

}
