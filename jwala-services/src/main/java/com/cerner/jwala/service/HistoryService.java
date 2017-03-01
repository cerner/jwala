package com.cerner.jwala.service;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.persistence.jpa.domain.JpaGroup;
import com.cerner.jwala.persistence.jpa.domain.JpaHistory;
import com.cerner.jwala.persistence.jpa.type.EventType;

import java.util.List;

/**
 * History service.
 *
 * Created by Jedd Cuison on 12/2/2015.
 */
public interface HistoryService {

    /**
     * Create history data.
     * @param serverName the server name
     * @param groups list of {@link JpaGroup}
     * @param event the event
     * @param eventType @{link EventType}
     * @param user the user name/id
     */
    List<JpaHistory> createHistory(String serverName, List<Group> groups, String event, EventType eventType, String user);

    /**
     * Retrieve history data.
     * @param groupName the group name
     * @param serverName The server name, if null the history of all the servers belonging to the group will be queried.
     * @param numOfRec The Number of records to fetch. If null, all records are retrieved.
     * @return a list of {@link JpaHistory}
     */
    List<JpaHistory> findHistory(String groupName, String serverName, Integer numOfRec);

}
