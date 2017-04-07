package com.cerner.jwala.service.webserver.component;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.domain.model.webserver.WebServerState;
import com.cerner.jwala.service.MessagingService;
import com.cerner.jwala.service.group.GroupStateNotificationService;
import com.cerner.jwala.service.state.InMemoryStateManagerService;
import com.cerner.jwala.service.webserver.WebServerService;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit test(s) for WebServerStateSetterWorker.
 *
 * Created by Jedd Cuison on 2/19/2016.
 */
@SuppressWarnings("unchecked")
public class WebServerStateSetterWorkerTest {

    private WebServerStateSetterWorker webServerStateSetterWorker;

    @Mock
    private Map mockWebServerReachableStateMap;

    @Mock
    private WebServer mockWebServer;

    @Mock
    private ClientHttpResponse mockClientHttpResponse;

    @Mock
    private ClientHttpRequest mockClientHttpRequest;

    @Mock
    private HttpComponentsClientHttpRequestFactory mockHttpClientRequestFactory;

    @Mock
    private WebServerService mockWebServerService;

    @Mock
    private MessagingService mockMessagingService;

    @Mock
    private GroupStateNotificationService mockGroupNotificationService;

    @Mock
    private InMemoryStateManagerService mockInMemoryStateManagerService;

    @Mock
    private HttpComponentsClientHttpRequestFactory mockHttpRequestFactory;

    private int executeCount;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        webServerStateSetterWorker = new WebServerStateSetterWorker(mockInMemoryStateManagerService, mockWebServerService,
                mockMessagingService, mockGroupNotificationService, mockHttpRequestFactory);
    }

    @Test
    public void testPingWebServer() throws Exception {
        when(mockClientHttpResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockClientHttpRequest.execute()).thenReturn(mockClientHttpResponse);
        when(mockHttpRequestFactory.createRequest(any(URI.class), eq(HttpMethod.GET))).thenReturn(mockClientHttpRequest);
        final Identifier<WebServer> id = new Identifier<>(1L);
        when(mockWebServer.getId()).thenReturn(id);
        webServerStateSetterWorker.pingWebServer(mockWebServer);
        verify(mockWebServerService).updateState(any(Identifier.class), eq(WebServerReachableState.WS_REACHABLE),
                eq(StringUtils.EMPTY));
        verify(mockMessagingService).send(any(WebServerState.class));
    }

    @Test
    public void testPingWebServerNotFound() throws Exception {
        when(mockClientHttpResponse.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        when(mockClientHttpRequest.execute()).thenReturn(mockClientHttpResponse);
        when(mockHttpRequestFactory.createRequest(any(URI.class), eq(HttpMethod.GET))).thenReturn(mockClientHttpRequest);
        final Identifier<WebServer> id = new Identifier<>(1L);
        when(mockWebServer.getId()).thenReturn(id);
        webServerStateSetterWorker.pingWebServer(mockWebServer);
        verify(mockWebServerService).updateState(any(Identifier.class), eq(WebServerReachableState.WS_UNREACHABLE),
                contains("failed with a response code"));
        verify(mockMessagingService).send(any(WebServerState.class));
    }

    @Test
    public void testPingWebServerWithIOException() throws Exception {
        when(mockClientHttpRequest.execute()).thenThrow(IOException.class);
        when(mockHttpRequestFactory.createRequest(any(URI.class), eq(HttpMethod.GET))).thenReturn(mockClientHttpRequest);
        final Identifier<WebServer> id = new Identifier<>(1L);
        when(mockWebServer.getId()).thenReturn(id);
        webServerStateSetterWorker.pingWebServer(mockWebServer);
        verify(mockWebServerService).updateState(any(Identifier.class), eq(WebServerReachableState.WS_UNREACHABLE),
                eq(StringUtils.EMPTY));
        verify(mockMessagingService).send(any(WebServerState.class));
    }

    @Test
    public void testPingWebServerTwice() throws Exception {
        when(mockClientHttpResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockClientHttpRequest.execute()).thenReturn(mockClientHttpResponse);
        when(mockHttpRequestFactory.createRequest(any(URI.class), eq(HttpMethod.GET))).thenReturn(mockClientHttpRequest);
        final Identifier<WebServer> id = new Identifier<>(1L);
        when(mockWebServer.getId()).thenReturn(id);
        webServerStateSetterWorker.pingWebServer(mockWebServer);
        verify(mockWebServerService).updateState(any(Identifier.class), eq(WebServerReachableState.WS_REACHABLE),
                eq(StringUtils.EMPTY));
        verify(mockMessagingService).send(any(WebServerState.class));


        reset(mockWebServerService);
        reset(mockMessagingService);
        webServerStateSetterWorker.pingWebServer(mockWebServer);
        verify(mockWebServerService, never()).updateState(any(Identifier.class), eq(WebServerReachableState.WS_REACHABLE),
                eq(StringUtils.EMPTY));
        verify(mockMessagingService, never()).send(any(WebServerState.class));
    }

    @Test
    public void testPingWebServerNotFoundTwice() throws Exception {
        when(mockClientHttpResponse.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        when(mockClientHttpRequest.execute()).thenReturn(mockClientHttpResponse);
        when(mockHttpRequestFactory.createRequest(any(URI.class), eq(HttpMethod.GET))).thenReturn(mockClientHttpRequest);
        final Identifier<WebServer> id = new Identifier<>(1L);
        when(mockWebServer.getId()).thenReturn(id);
        webServerStateSetterWorker.pingWebServer(mockWebServer);
        verify(mockWebServerService).updateState(any(Identifier.class), eq(WebServerReachableState.WS_UNREACHABLE),
                contains("failed with a response code"));
        verify(mockMessagingService).send(any(WebServerState.class));

        reset(mockWebServerService);
        reset(mockMessagingService);
        webServerStateSetterWorker.pingWebServer(mockWebServer);
        verify(mockWebServerService, never()).updateState(any(Identifier.class), eq(WebServerReachableState.WS_REACHABLE),
                eq(StringUtils.EMPTY));
        verify(mockMessagingService, never()).send(any(WebServerState.class));
    }

    @Test
    public void testPingNewWebServer() throws Exception {
        final Identifier<WebServer> id = new Identifier<>(1L);
        when(mockWebServer.getId()).thenReturn(id);
        when(mockWebServer.getState()).thenReturn(WebServerReachableState.WS_NEW);
        webServerStateSetterWorker.pingWebServer(mockWebServer);
        verify(mockWebServerService, never()).updateState(any(Identifier.class), eq(WebServerReachableState.WS_REACHABLE),
                eq(StringUtils.EMPTY));
        verify(mockMessagingService, never()).send(any(WebServerState.class));
    }

    @Test
    public void testPingWebServerThatIsCurrentlyBeingPinged() throws Exception {
        executeCount = 0;
        when(mockClientHttpResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockHttpRequestFactory.createRequest(any(URI.class), eq(HttpMethod.GET))).thenReturn(new ClientHttpRequest() {
            @Override
            public HttpHeaders getHeaders() {
                return null;
            }

            @Override
            public HttpMethod getMethod() {
                return null;
            }

            @Override
            public URI getURI() {
                return null;
            }

            @Override
            public OutputStream getBody() throws IOException {
                return null;
            }

            @Override
            public ClientHttpResponse execute() throws IOException {
                // Test the ff scenario: Executing ping with a web server that is currently being pinged should return
                // without doing the ping or updating the state
                executeCount++;

                // Be careful when modifying this! This not only tests web server ping logic it also prevents a
                // never ending loop!
                if (executeCount > 1) {
                    fail("ClientHttpRequest.execute is only expected to run once!");
                }

                webServerStateSetterWorker.pingWebServer(mockWebServer);
                verify(mockWebServerService, never()).updateState(any(Identifier.class), eq(WebServerReachableState.WS_REACHABLE),
                        eq(StringUtils.EMPTY));
                verify(mockMessagingService, never()).send(any(WebServerState.class));
                reset(mockWebServerService, mockMessagingService);
                return mockClientHttpResponse;
            }
        });
        final Identifier<WebServer> id = new Identifier<>(1L);
        when(mockWebServer.getId()).thenReturn(id);
        webServerStateSetterWorker.pingWebServer(mockWebServer);
        verify(mockWebServerService).updateState(any(Identifier.class), eq(WebServerReachableState.WS_REACHABLE),
                eq(StringUtils.EMPTY));
        verify(mockMessagingService).send(any(WebServerState.class));
    }

}