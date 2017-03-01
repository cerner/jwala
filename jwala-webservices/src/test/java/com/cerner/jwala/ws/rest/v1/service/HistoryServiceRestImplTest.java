package com.cerner.jwala.ws.rest.v1.service;

import com.cerner.jwala.service.HistoryService;
import com.cerner.jwala.ws.rest.v1.service.impl.HistoryServiceRestImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.ws.rs.core.Response;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by RS045609 on 2/14/2017.
 */
public class HistoryServiceRestImplTest {
    @Mock
    private HistoryService mockHistoryService;

    private HistoryServiceRestImpl historyServiceRest;

    @Before
    public void setUp() {
        initMocks(this);
        historyServiceRest = new HistoryServiceRestImpl(mockHistoryService);
    }

    @Test
    public void testFindHistory() {
        final Response response = historyServiceRest.findHistory("any", 1);
        assertEquals(response.getStatus(), 200);
    }

    @Test
    public void testFindHistoryWithServer() {
        final Response response = historyServiceRest.findHistory("any", "any", 1);
        assertEquals(response.getStatus(), 200);
    }
}
