package com.cerner.jwala.service.webserver.impl;

import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.service.webserver.WebServerService;
import com.cerner.jwala.service.webserver.WebServerStateRetrievalScheduledTaskHandler;
import com.cerner.jwala.service.webserver.component.WebServerStateSetterWorker;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Unit test for {@link WebServerStateRetrievalScheduledTaskHandler}
 *
 * Created by Jedd Cuison on 1/27/2017
 */
public class WebServerStateRetrievalScheduledTaskHandlerTest {

    @Mock
    private WebServerService mockWebServerService;

    @Mock
    private WebServerStateSetterWorker mockWebServerStateSetterWorker;

    private WebServerStateRetrievalScheduledTaskHandler webServerStateRetrievalScheduledTaskHandler;

    @Before
    public void setup() {
        initMocks(this);
        webServerStateRetrievalScheduledTaskHandler =
                new WebServerStateRetrievalScheduledTaskHandler(mockWebServerService, mockWebServerStateSetterWorker);
    }

    @Test
    public void testExecuteEnableTaskHandler() {
        final List<WebServer> webServerList = new ArrayList<>();
        webServerList.add(mock(WebServer.class));
        webServerList.add(mock(WebServer.class));
        when(mockWebServerService.getWebServersPropagationNew()).thenReturn(webServerList);
        webServerStateRetrievalScheduledTaskHandler.execute();
        verify(mockWebServerService).getWebServersPropagationNew();
        verify(mockWebServerStateSetterWorker, times(2)).pingWebServer(any(WebServer.class));
    }

}
