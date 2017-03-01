package com.cerner.jwala.service;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.persistence.jpa.type.EventType;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * Brings history and messaging functionalities together
 *
 * Created by Jedd Cuison on 11/2/2016
 */
@Service
public interface HistoryFacadeService {

    /**
     * Write history and send it to the web socket
     *
     * @param serverName the server name
     * @param groups the groups where the event happened
     * @param event the event + the details of the event e.g. DEPLOY deploy context.xml resource
     * @param eventType {@link EventType}
     * @param user the user
     *
     * TODO: Split event into event name and description/details in the future
     */
    void write(String serverName, Collection<Group> groups, String event, EventType eventType, String user);

    /**
     * Write history and send it to the web socket
     *
     * @param serverName the server name
     * @param group the group where the event happened
     * @param event the event + the details of the event e.g. DEPLOY deploy context.xml resource
     * @param eventType {@link EventType}
     * @param user the user
     */
    void write(String serverName, Group group, String event, EventType eventType, String user);
}
