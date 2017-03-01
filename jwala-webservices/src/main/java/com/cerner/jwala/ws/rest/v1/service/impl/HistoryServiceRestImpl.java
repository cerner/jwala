package com.cerner.jwala.ws.rest.v1.service.impl;

import com.cerner.jwala.persistence.jpa.domain.JpaHistory;
import com.cerner.jwala.service.HistoryService;
import com.cerner.jwala.ws.rest.v1.service.HistoryServiceRest;

import javax.ws.rs.core.Response;
import java.util.List;

/**
 * {@link HistoryServiceRest} implementation.
 *
 * Created by Jedd Cuison on 12/7/2015.
 */
public class HistoryServiceRestImpl implements HistoryServiceRest {

    private HistoryService historyService;

    public HistoryServiceRestImpl(final HistoryService historyService) {
        this.historyService = historyService;
    }

    @Override
    public Response findHistory(final String groupName, final Integer numOfRec) {
        final List<JpaHistory> historyList = historyService.findHistory(groupName, null, numOfRec);
        return Response.ok(historyList).build();
    }

    @Override
    public Response findHistory(final String groupName, final String serverName, final Integer numOfRec) {
        final List<JpaHistory> historyList = historyService.findHistory(groupName, serverName, numOfRec);
        return Response.ok(historyList).build();
    }

}
