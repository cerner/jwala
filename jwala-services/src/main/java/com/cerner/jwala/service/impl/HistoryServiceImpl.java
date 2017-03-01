package com.cerner.jwala.service.impl;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.persistence.jpa.domain.JpaHistory;
import com.cerner.jwala.persistence.jpa.service.HistoryCrudService;
import com.cerner.jwala.persistence.jpa.type.EventType;
import com.cerner.jwala.service.HistoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link HistoryService} implementation.
 *
 * Created by Jedd Cuison on 12/2/2015.
 */
public class HistoryServiceImpl implements HistoryService {

    private final HistoryCrudService historyCrudService;

    @Autowired
    public HistoryServiceImpl(final HistoryCrudService historyCrudService) {
        this.historyCrudService = historyCrudService;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW) // Write history independent of any transaction.
    public List<JpaHistory> createHistory(final String serverName, final List<Group> groups, final String event,
                                          final EventType eventType, final String user) {
        final List<JpaHistory> jpaHistoryList = new ArrayList<>();
        if (groups != null) {
            for (Group group : groups) {
                jpaHistoryList.add(historyCrudService.createHistory(serverName, group, event, eventType, user));
            }
        }
        return jpaHistoryList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<JpaHistory> findHistory(final String groupName, final String serverName, final Integer numOfRec) {
        return historyCrudService.findHistory(groupName, serverName, numOfRec);
    }

}
