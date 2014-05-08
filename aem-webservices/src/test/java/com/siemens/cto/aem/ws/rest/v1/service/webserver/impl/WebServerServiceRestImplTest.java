package com.siemens.cto.aem.ws.rest.v1.service.webserver.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.CreateWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.UpdateWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.service.webserver.impl.WebServerServiceImpl;
import com.siemens.cto.aem.ws.rest.v1.provider.PaginationParamProvider;
import com.siemens.cto.aem.ws.rest.v1.response.ApplicationResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * @author horspe00
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class WebServerServiceRestImplTest {

    private static final String name = "webserverName";
    private static final String host = "localhost";
    private static final String portNumber = "8080";
    private static final String groupIdOne = "1";
    private static final String groupIdTwo = "2";
    private static final List<WebServer> webServerList = createWebServerList();
    private static final WebServer webServer = webServerList.get(0);

    @Mock
    private WebServerServiceImpl impl;

    private WebServerServiceRestImpl cut;

    private static List<WebServer> createWebServerList() {
        final Group groupOne = new Group(Identifier.id(1L, Group.class), "ws-groupOne");
        final Group groupTwo = new Group(Identifier.id(2L, Group.class), "ws-groupTwo");

        final List<Group> groupsList = new ArrayList<Group>();
        groupsList.add(groupOne);
        groupsList.add(groupTwo);

        final WebServer ws = new WebServer(Identifier.id(1L, WebServer.class), groupsList, name, host, 8080);
        final List<WebServer> result = new ArrayList<WebServer>();
        result.add(ws);
        return result;
    }

    @Before
    public void setUp() {
        cut = new WebServerServiceRestImpl(impl);
    }

    @Test
    public void testGetWebServerList() {
        when(impl.getWebServers(any(PaginationParameter.class))).thenReturn(webServerList);

        final PaginationParamProvider paginationProvider = new PaginationParamProvider();
        final Response response = cut.getWebServers(paginationProvider);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof List);

        final List<WebServer> receivedList = (List<WebServer>) content;
        final WebServer received = receivedList.get(0);
        assertEquals(webServer, received);
    }

    @Test
    public void testGetWebServer() {
        when(impl.getWebServer(any(Identifier.class))).thenReturn(webServer);

        final Response response = cut.getWebServer(Identifier.id(Long.valueOf(1l), WebServer.class));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof WebServer);

        final WebServer received = (WebServer) content;
        assertEquals(webServer, received);
    }

    @Test
    public void testCreateWebServer() {
        final JsonCreateWebServer jsonCreateWebServer = mock(JsonCreateWebServer.class);
        jsonCreateWebServer.addGroupId(groupIdOne);
        jsonCreateWebServer.addGroupId(groupIdTwo);
        when(impl.createWebServer(any(CreateWebServerCommand.class), any(User.class))).thenReturn(webServer);

        final Response response = cut.createWebServer(jsonCreateWebServer);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof WebServer);

        final WebServer received = (WebServer) content;
        assertEquals(webServer, received);
    }

    @Test
    public void testUpdateWebServer() {
        final JsonUpdateWebServer jsonUpdateWebServer = mock(JsonUpdateWebServer.class);
        when(impl.updateWebServer(any(UpdateWebServerCommand.class), any(User.class))).thenReturn(webServer);

        final Response response = cut.updateWebServer(jsonUpdateWebServer);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();

        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof WebServer);

        final WebServer received = (WebServer) content;
        assertEquals(webServer, received);
    }

    @Test
    public void testRemoveWebServer() {
        final Response response = cut.removeWebServer(Identifier.id(Long.valueOf(1l), WebServer.class));
        verify(impl, atLeastOnce()).removeWebServer(any(Identifier.class));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        assertNull(applicationResponse);
    }
}
