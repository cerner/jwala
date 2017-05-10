package com.cerner.jwala.ws.rest.v1.service.webserver.impl;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerControlOperation;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.domain.model.webserver.WebServerState;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.request.webserver.ControlWebServerRequest;
import com.cerner.jwala.common.request.webserver.CreateWebServerRequest;
import com.cerner.jwala.common.request.webserver.UpdateWebServerRequest;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.service.HistoryFacadeService;
import com.cerner.jwala.service.HistoryService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionLockManager;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionService;
import com.cerner.jwala.service.group.GroupService;
import com.cerner.jwala.service.impl.spring.component.SimpMessagingServiceImpl;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.service.resource.impl.ResourceGeneratorType;
import com.cerner.jwala.service.webserver.WebServerCommandService;
import com.cerner.jwala.service.webserver.WebServerControlService;
import com.cerner.jwala.service.webserver.WebServerService;
import com.cerner.jwala.service.webserver.exception.WebServerServiceException;
import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;
import com.cerner.jwala.ws.rest.v1.response.ApplicationResponse;
import com.cerner.jwala.ws.rest.v1.service.webserver.WebServerServiceRest;
import org.apache.tika.mime.MediaType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author horspe00
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
        classes = {WebServerServiceRestImplTest.Config.class
        })
public class WebServerServiceRestImplTest {

    private static final String name = "webserverName";
    private static final String name2 = "webserverName2";
    private static final String host = "localhost";
    private static final Path statusPath = new Path("/statusPath");

    private static List<WebServer> webServerList;
    private static WebServer webServer;
    private static WebServer webServer2;

    @Autowired
    private WebServerServiceRestImpl webServerServiceRest;

    private static List<WebServer> createWebServerList() {
        final Group groupOne = new Group(Identifier.id(1L, Group.class), "ws-groupOne");
        final Group groupTwo = new Group(Identifier.id(2L, Group.class), "ws-groupTwo");

        final List<Group> groupsList = new ArrayList<>();
        groupsList.add(groupOne);
        groupsList.add(groupTwo);
        final List<Group> singleGroupList = new ArrayList<>();
        singleGroupList.add(groupOne);

        final WebServer ws = new WebServer(Identifier.id(1L, WebServer.class), groupsList, name, host, 8080, 8009, statusPath,
                WebServerReachableState.WS_UNREACHABLE, null);
        final WebServer ws2 = new WebServer(Identifier.id(2L, WebServer.class), singleGroupList, name2, host, 8080, 8009, statusPath,
                WebServerReachableState.WS_UNREACHABLE, null);
        final List<WebServer> result = new ArrayList<>();
        result.add(ws);
        result.add(ws2);
        return result;
    }

    @Before
    public void setUp() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");

        when(Config.mockAuthenticatedUser.getUser()).thenReturn(new User("Unused"));

        webServerList = createWebServerList();
        webServer = webServerList.get(0);
        webServer2 = webServerList.get(1);
    }

    @After
    public void tearDown() throws IOException {
        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testGetWebServerList() {
        when(Config.mockWebServerService.getWebServers()).thenReturn(webServerList);

        final Response response = webServerServiceRest.getWebServers(null);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof List);

        final List<WebServer> receivedList = (List<WebServer>) content;
        final WebServer received = receivedList.get(0);
        assertEquals(webServer, received);
        final WebServer received2 = receivedList.get(1);
        assertEquals(webServer2, received2);
    }

    @Test
    public void testGetWebServer() {
        when(Config.mockWebServerService.getWebServer(any(Identifier.class))).thenReturn(webServer);

        final Response response = webServerServiceRest.getWebServer(Identifier.id(1l, WebServer.class));
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
        when(Config.mockWebServerService.createWebServer(any(CreateWebServerRequest.class), any(User.class))).thenReturn(webServer2);

        final Response response = webServerServiceRest.createWebServer(jsonCreateWebServer, Config.mockAuthenticatedUser);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof WebServer);

        final WebServer received = (WebServer) content;
        assertEquals(webServer2, received);
    }

    @Test
    public void testCreateWebServerPopulatesTemplatesFromGroup() throws IOException {
        final JsonCreateWebServer jsonCreateWebServer = mock(JsonCreateWebServer.class);
        when(Config.mockWebServerService.createWebServer(any(CreateWebServerRequest.class), any(User.class))).thenReturn(webServer);

        List<String> templateNames = new ArrayList<>();
        templateNames.add("httpd.conf");

        when(Config.mockGroupService.getGroupWebServersResourceTemplateNames(anyString())).thenReturn(templateNames);
        when(Config.mockGroupService.getGroupWebServerResourceTemplate(anyString(), anyString(), eq(false), any(ResourceGroup.class))).thenReturn("httpd.conf template");
        when(Config.mockGroupService.getGroupWebServerResourceTemplateMetaData(anyString(), anyString())).thenReturn("{}");
        ResourceTemplateMetaData mockMetaData = mock(ResourceTemplateMetaData.class);
        when(mockMetaData.getDeployFileName()).thenReturn("httpd.conf");
        when(Config.mockResourceService.getTokenizedMetaData(anyString(), Matchers.anyObject(), anyString())).thenReturn(mockMetaData);
        final Response response = webServerServiceRest.createWebServer(jsonCreateWebServer, Config.mockAuthenticatedUser);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    @Test
    public void testUpdateWebServer() {
        final JsonUpdateWebServer jsonUpdateWebServer = mock(JsonUpdateWebServer.class);
        when(Config.mockWebServerService.updateWebServer(any(UpdateWebServerRequest.class), any(User.class))).thenReturn(webServer);

        final Response response = webServerServiceRest.updateWebServer(jsonUpdateWebServer, Config.mockAuthenticatedUser);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();

        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof WebServer);

        final WebServer received = (WebServer) content;
        assertEquals(webServer, received);
    }

    @Test
    public void testRemoveWebServer() {
        final Identifier<WebServer> id = new Identifier<>(1L);
        final User user = new User("user");
        when(Config.mockAuthenticatedUser.getUser()).thenReturn(user);
        webServerServiceRest.deleteWebServer(id, false, Config.mockAuthenticatedUser);
        verify(Config.mockWebServerService).deleteWebServer(id, false, user);
    }

    @Test
    public void testRemoveWebServerWhenWebServerNotStopped() {
        when(Config.mockWebServerService.getWebServer(any(Identifier.class))).thenReturn(webServer);
        when(Config.mockWebServerService.isStarted(any(WebServer.class))).thenReturn(true);
        final Response response = webServerServiceRest.deleteWebServer(Identifier.id(1l, WebServer.class), false, Config.mockAuthenticatedUser);
        assertEquals(204, response.getStatus());
    }

    @Test(expected = WebServerServiceException.class)
    public void testRemoveWebServerException() {
        final Identifier<WebServer> id = new Identifier<>(1L);
        final User user = new User("user");
        final AuthenticatedUser mockAuthUser = mock(AuthenticatedUser.class);
        when(mockAuthUser.getUser()).thenReturn(user);
        doThrow(WebServerServiceException.class).when(Config.mockWebServerService).deleteWebServer(id, false, user);
        webServerServiceRest.deleteWebServer(id, false, mockAuthUser);
    }

    @Test
    public void testControlWebServer() {
        final CommandOutput execData = mock(CommandOutput.class);
        final ExecReturnCode execDataReturnCode = mock(ExecReturnCode.class);
        when(execDataReturnCode.wasSuccessful()).thenReturn(true);
        when(execData.getReturnCode()).thenReturn(execDataReturnCode);

        final JsonControlWebServer jsonControlWebServer = new JsonControlWebServer("start");
        when(Config.mockWebServerControlService.controlWebServer(any(ControlWebServerRequest.class), any(User.class))).thenReturn(execData);
        final Response response = webServerServiceRest.controlWebServer(Identifier.id(1l, WebServer.class), jsonControlWebServer, Config.mockAuthenticatedUser, false, null);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test(expected = InternalErrorException.class)
    public void testControlWebServerThrowsExpcetionForFailedCommandOutput() {
        final CommandOutput execData = mock(CommandOutput.class);
        final ExecReturnCode execDataReturnCode = mock(ExecReturnCode.class);
        final JsonControlWebServer jsonControlWebServer = new JsonControlWebServer("start");
        when(execDataReturnCode.wasSuccessful()).thenReturn(false);
        when(execData.getReturnCode()).thenReturn(new ExecReturnCode(1));
        when(execData.getStandardError()).thenReturn("TEST ERROR");
        when(execData.getStandardOutput()).thenReturn("");
        when(Config.mockWebServerControlService.controlWebServer(any(ControlWebServerRequest.class), any(User.class))).thenReturn(execData);
        webServerServiceRest.controlWebServer(Identifier.id(1l, WebServer.class), jsonControlWebServer, Config.mockAuthenticatedUser, false, 120L);
    }

    @Test
    public void testGenerateHttpdConfig() {
        when(Config.mockWebServerService.getResourceTemplate(anyString(), anyString(), eq(true), any(ResourceGroup.class)))
                .thenReturn("httpd configuration");
        Response response = webServerServiceRest.generateConfig("any-server-name");
        assertEquals("httpd configuration", response.getEntity());
    }

    @Test
    public void testGetWebServersByGroup() {
        final List<WebServer> webServers = new ArrayList<>();
        webServers.add(new WebServer(null, new ArrayList<Group>(), "test", null, null, null, new Path("/statusPath"),
                WebServerReachableState.WS_UNREACHABLE, null));

        final Identifier<Group> groupId = new Identifier<>("1");

        when(Config.mockWebServerService.findWebServers(Matchers.eq(groupId))).thenReturn(webServers);
        final Response response = webServerServiceRest.getWebServers(groupId);

        final List<WebServer> result =
                (List<WebServer>) ((ApplicationResponse) response.getEntity()).getApplicationResponseContent();
        assertEquals("test", result.get(0).getName());
    }

    @Test
    public void testGenerateAndDeployConfig() throws CommandFailureException, IOException {
        when(Config.mockWebServerService.getResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"contentType\":\"text/plain\",\"deployPath\":\"./anyPath\",\"deployFileName\":\"httpd.conf\"}");
        CommandOutput commandOutput = mock(CommandOutput.class);
        ExecReturnCode execReturnCode = mock(ExecReturnCode.class);
        when(commandOutput.getReturnCode()).thenReturn(execReturnCode);
        when(commandOutput.getReturnCode().wasSuccessful()).thenReturn(true);
        when(Config.mockResourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        when(Config.mockResourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), any(), any(ResourceGeneratorType.class))).thenReturn("{\"contentType\":\"text/plain\",\"deployPath\":\"./anyPath\",\"deployFileName\":\"httpd.conf\"}");
        ResourceTemplateMetaData mockMetaData = mock(ResourceTemplateMetaData.class);
        when(mockMetaData.getDeployFileName()).thenReturn("httpd.conf");
        when(mockMetaData.getDeployPath()).thenReturn("./anyPath");
        when(mockMetaData.getContentType()).thenReturn(MediaType.TEXT_PLAIN);
        when(Config.mockResourceService.getTokenizedMetaData(anyString(), Matchers.anyObject(), anyString())).thenReturn(mockMetaData);
        Response response = webServerServiceRest.generateAndDeployConfig(webServer.getName(), "httpd.conf", Config.mockAuthenticatedUser);
        assertTrue(response.hasEntity());
    }

    @Test
    public void testGenerateAndDeployConfigBinaryFile() throws CommandFailureException, IOException {
        when(Config.mockWebServerService.getResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"contentType\":\"application/binary\",\"deployPath\":\"./anyPath\"}");
        when(Config.mockResourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        CommandOutput commandOutput = mock(CommandOutput.class);
        ExecReturnCode execReturnCode = mock(ExecReturnCode.class);
        when(commandOutput.getReturnCode()).thenReturn(execReturnCode);
        when(commandOutput.getReturnCode().wasSuccessful()).thenReturn(true);
        when(Config.mockResourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), any(), any(ResourceGeneratorType.class))).thenReturn("{\"contentType\":\"application/binary\",\"deployPath\":\"./anyPath\"}");
        ResourceTemplateMetaData mockMetaData = mock(ResourceTemplateMetaData.class);
        when(mockMetaData.getDeployFileName()).thenReturn("httpd.conf");
        when(mockMetaData.getDeployPath()).thenReturn("./anyPath");
        when(mockMetaData.getContentType()).thenReturn(MediaType.TEXT_PLAIN);
        when(Config.mockResourceService.getTokenizedMetaData(anyString(), Matchers.anyObject(), anyString())).thenReturn(mockMetaData);
        Response response = webServerServiceRest.generateAndDeployConfig(webServer.getName(), "httpd.exe", Config.mockAuthenticatedUser);
        assertTrue(response.hasEntity());
    }

    @Test
    public void testGenerateAndDeployConfigThrowsExceptionForWebServerNotStopped() throws CommandFailureException, IOException {
        when(Config.mockWebServerService.isStarted(any(WebServer.class))).thenReturn(true);
        when(Config.mockWebServerService.getResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"contentType\":\"text/plain\",\"deployPath\":\"./anyPath\"}");
        when(Config.mockResourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        when(Config.mockResourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), any(), any(ResourceGeneratorType.class))).thenReturn("{\"contentType\":\"text/plain\",\"deployPath\":\"./anyPath\"}");
        try {
            webServerServiceRest.generateAndDeployConfig(webServer.getName(), "httpd.conf", Config.mockAuthenticatedUser);
        } catch (InternalErrorException ie) {
            assertEquals(ie.getMessage(), "The target Web Server must be stopped before attempting to update the resource file");
        }
    }

    @Test(expected = WebServerServiceException.class)
    public void testGenerateAndDeployWebServerWithNoHttpdConfTemplate() {
        when(Config.mockWebServerService.getWebServer(anyString())).thenReturn(webServer);
        when(Config.mockWebServerService.isStarted(any(WebServer.class))).thenReturn(false);
        when(Config.mockWebServerService.getResourceTemplateNames(eq(webServer.getName()))).thenReturn(Collections.singletonList("not-httpd.conf"));

        webServerServiceRest.generateAndDeployWebServer(webServer.getName(), Config.mockAuthenticatedUser);
    }

    @Test
    public void testGenerateAndDeployWebServer() throws CommandFailureException, IOException {
        List<String> webServerResourceNames = new ArrayList<>();
        webServerResourceNames.add("httpd.conf");

        CommandOutput retSuccessExecData = new CommandOutput(new ExecReturnCode(0), "", "");
        when(Config.mockWebServerControlService.controlWebServer(any(ControlWebServerRequest.class), any(User.class))).thenReturn(retSuccessExecData);
        when(Config.mockWebServerService.getWebServer(anyString())).thenReturn(webServer);
        when(Config.mockWebServerService.generateInstallServiceScript(any(WebServer.class))).thenReturn("invoke me");
        when(Config.mockWebServerService.getResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"contentType\":\"text/plain\",\"deployPath\":\"./anyPath\",\"deployFileName\":\"httpd.conf\"}");
        when(Config.mockWebServerService.getResourceTemplateNames(anyString())).thenReturn(webServerResourceNames);
        when(Config.mockResourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        when(Config.mockResourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), any(), any(ResourceGeneratorType.class))).thenReturn("{\"contentType\":\"text/plain\",\"deployPath\":\"./anyPath\",\"deployFileName\":\"httpd.conf\"}");
        ResourceTemplateMetaData mockMetaData = mock(ResourceTemplateMetaData.class);
        when(mockMetaData.getDeployFileName()).thenReturn("httpd.conf");
        when(mockMetaData.getDeployPath()).thenReturn("./anyPath");
        when(mockMetaData.getContentType()).thenReturn(MediaType.TEXT_PLAIN);
        when(Config.mockResourceService.getTokenizedMetaData(anyString(), Matchers.anyObject(), anyString())).thenReturn(mockMetaData);

        Response response = null;
        response = webServerServiceRest.generateAndDeployWebServer(webServer.getName(), Config.mockAuthenticatedUser);
        assertNotNull(response);

        response = null;
        response = webServerServiceRest.generateAndDeployWebServer(webServer.getName(), Config.mockAuthenticatedUser);
        assertNotNull(response);
    }

    @Test(expected = InternalErrorException.class)
    public void testGenerateAndDeployWebServerCallInstallServiceWSFails() throws CommandFailureException, IOException {
        List<String> webServerResourceNames = new ArrayList<>();
        webServerResourceNames.add("httpd.conf");

        CommandOutput retSuccessExecData = new CommandOutput(new ExecReturnCode(0), "", "");
        CommandOutput retFailExecData = new CommandOutput(new ExecReturnCode(1), "", "Failed secure copy");
        when(Config.mockWebServerControlService.controlWebServer(any(ControlWebServerRequest.class), any(User.class))).thenReturn(retSuccessExecData);
        when(Config.mockWebServerControlService.controlWebServer(eq(new ControlWebServerRequest(webServer.getId(), WebServerControlOperation.INSTALL_SERVICE)), any(User.class))).thenReturn(retFailExecData);
        when(Config.mockWebServerService.getWebServer(anyString())).thenReturn(webServer);
        when(Config.mockWebServerService.generateInstallServiceScript(any(WebServer.class))).thenReturn("invoke me");
        when(Config.mockWebServerService.getResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"contentType\":\"text/plain\",\"deployPath\":\"./anyPath\",\"deployFileName\":\"httpd.conf\"}");
        when(Config.mockWebServerService.getResourceTemplateNames(anyString())).thenReturn(webServerResourceNames);
        when(Config.mockResourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        when(Config.mockResourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), any(), any(ResourceGeneratorType.class))).thenReturn("{\"contentType\":\"text/plain\",\"deployPath\":\"./anyPath\",\"deployFileName\":\"httpd.conf\"}");
        ResourceTemplateMetaData mockMetaData = mock(ResourceTemplateMetaData.class);
        when(mockMetaData.getDeployFileName()).thenReturn("httpd.conf");
        when(mockMetaData.getDeployPath()).thenReturn("./anyPath");
        when(mockMetaData.getContentType()).thenReturn(MediaType.TEXT_PLAIN);
        when(Config.mockResourceService.getTokenizedMetaData(anyString(), Matchers.anyObject(), anyString())).thenReturn(mockMetaData);

        webServerServiceRest.generateAndDeployWebServer(webServer.getName(), Config.mockAuthenticatedUser);
    }

    @Test
    public void testGenerateAndDeployWebServerDeleteServiceForNonexistentService() throws CommandFailureException, IOException {
        List<String> webServerResourceNames = new ArrayList<>();
        webServerResourceNames.add("httpd.conf");

        CommandOutput retSuccessExecData = new CommandOutput(new ExecReturnCode(0), "", "");
        when(Config.mockWebServerControlService.controlWebServer(any(ControlWebServerRequest.class), any(User.class))).thenReturn(retSuccessExecData);

        when(Config.mockWebServerService.getWebServer(anyString())).thenReturn(webServer);
        when(Config.mockWebServerService.generateInstallServiceScript(any(WebServer.class))).thenReturn("invoke me");
        when(Config.mockWebServerService.getResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"contentType\":\"text/plain\",\"deployPath\":\"./anyPath\",\"deployFileName\":\"httpd.conf\"}");
        when(Config.mockWebServerService.getResourceTemplateNames(anyString())).thenReturn(webServerResourceNames);
        when(Config.mockWebServerService.isStarted(eq(webServer))).thenReturn(false);

        ResourceTemplateMetaData mockMetaData = mock(ResourceTemplateMetaData.class);
        when(mockMetaData.getDeployFileName()).thenReturn("httpd.conf");
        when(mockMetaData.getDeployPath()).thenReturn("./anyPath");
        when(mockMetaData.getContentType()).thenReturn(MediaType.TEXT_PLAIN);

        when(Config.mockResourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        when(Config.mockResourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), any(), any(ResourceGeneratorType.class))).thenReturn("{\"contentType\":\"text/plain\",\"deployPath\":\"./anyPath\",\"deployFileName\":\"httpd.conf\"}");
        when(Config.mockResourceService.getTokenizedMetaData(anyString(), Matchers.anyObject(), anyString())).thenReturn(mockMetaData);

        Response response = webServerServiceRest.generateAndDeployWebServer(webServer.getName(), Config.mockAuthenticatedUser);
        assertNotNull(response);

        verify(Config.mockSimpleMessageService).send(any(WebServerState.class));

    }

    @Test
    public void testGenerateAndDeployWebServerTemplate() throws CommandFailureException, IOException {
        CommandOutput retSuccessExecData = new CommandOutput(new ExecReturnCode(0), "", "");
        when(Config.mockWebServerService.getResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"contentType\":\"text/plain\",\"deployPath\":\"./anyPath\",\"deployFileName\":\"${webServer.name}.txt\"}");
        CommandOutput commandOutput = mock(CommandOutput.class);
        ExecReturnCode execReturnCode = mock(ExecReturnCode.class);
        when(commandOutput.getReturnCode()).thenReturn(execReturnCode);
        when(commandOutput.getReturnCode().wasSuccessful()).thenReturn(true);
        when(Config.mockResourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        when(Config.mockWebServerService.getResourceTemplate(anyString(), anyString(), anyBoolean(), any(ResourceGroup.class))).thenReturn("");
        when(Config.mockWebServerService.getWebServer(anyString())).thenReturn(webServer);
        when(Config.mockResourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), any(), any(ResourceGeneratorType.class))).thenReturn("{\"contentType\":\"text/plain\",\"deployPath\":\"./anyPath\",\"deployFileName\":\"${webServer.name}.txt\"}");
        ResourceTemplateMetaData mockMetaData = mock(ResourceTemplateMetaData.class);
        when(mockMetaData.getDeployFileName()).thenReturn("httpd.conf");
        when(mockMetaData.getDeployPath()).thenReturn("./anyPath");
        when(mockMetaData.getContentType()).thenReturn(MediaType.TEXT_PLAIN);
        when(Config.mockResourceService.getTokenizedMetaData(anyString(), Matchers.anyObject(), anyString())).thenReturn(mockMetaData);

        Response response = webServerServiceRest.generateAndDeployConfig(webServer.getName(), "${webServer.name}.txt", Config.mockAuthenticatedUser);
        assertTrue(response.hasEntity());
    }

    @Test(expected = InternalErrorException.class)
    public void testGenerateAndDeployWebServerWhenWebServerNotStopped() {
        when(Config.mockWebServerService.getWebServer(anyString())).thenReturn(webServer);
        when(Config.mockWebServerService.isStarted(any(WebServer.class))).thenReturn(true);
        webServerServiceRest.generateAndDeployWebServer(webServer.getName(), Config.mockAuthenticatedUser);
    }

    @Test
    public void testGetHttpdConfig() throws CommandFailureException {
        when(Config.mockCommandImpl.getHttpdConf(webServer.getId())).thenReturn(new CommandOutput(new ExecReturnCode(0), "", ""));
        Response response = webServerServiceRest.getHttpdConfig(webServer.getId());
        assertTrue(response.hasEntity());
    }

    @Test
    public void testGetHttpdConfigThrowsException() throws CommandFailureException {
        when(Config.mockCommandImpl.getHttpdConf(webServer.getId())).thenThrow(CommandFailureException.class);
        Response response = webServerServiceRest.getHttpdConfig(webServer.getId());
        assertTrue(response.hasEntity());
    }

    @Test
    public void testGetResourceName() {
        List<String> resourceNames = new ArrayList<>();
        resourceNames.add("httpd-test.tpl");
        when(Config.mockWebServerService.getResourceTemplateNames(webServer.getName())).thenReturn(resourceNames);
        Response response = webServerServiceRest.getResourceNames(webServer.getName());
        assertTrue(response.hasEntity());
    }

    @Test
    public void testGetResourceTemplates() {
        String resourceTemplateName = "httpd-conf.tpl";
        when(Config.mockWebServerService.getResourceTemplate(webServer.getName(), resourceTemplateName, false, new ResourceGroup())).thenReturn("ServerRoot=./test");
        Response response = webServerServiceRest.getResourceTemplate(webServer.getName(), resourceTemplateName, false);
        assertTrue(response.hasEntity());
    }

    @Test
    public void testUpdateResourceTemplate() {
        String resourceTemplateName = "httpd-conf.tpl";
        String content = "ServerRoot=./test-update";
        when(Config.mockWebServerService.updateResourceTemplate(webServer.getName(), resourceTemplateName, content)).thenReturn(content);
        Response response = webServerServiceRest.updateResourceTemplate(webServer.getName(), resourceTemplateName, content);
        assertTrue(response.hasEntity());
    }

    @Test
    public void testUpdateResourceTemplateException() {
        String resourceTemplateName = "httpd-conf.tpl";
        String content = "ServerRoot=./test-update";
        when(Config.mockWebServerService.updateResourceTemplate(webServer.getName(), resourceTemplateName, content)).thenReturn(null);
        Response response = webServerServiceRest.updateResourceTemplate(webServer.getName(), resourceTemplateName, content);
        assertTrue(response.hasEntity());
        assertEquals(500, response.getStatus());
        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        assertEquals("Failed to update the template httpd-conf.tpl for webserverName. See the log for more details.", applicationResponse.getApplicationResponseContent());
    }

    @Test
    public void testPreviewResourceTemplate() {
        Response response = webServerServiceRest.previewResourceTemplate(webServer.getName(), "httpd.conf", "groupName", "httpd.conf");
        assertNotNull(response);

        when(Config.mockWebServerService.previewResourceTemplate(anyString(), anyString(), anyString(), anyString())).thenThrow(new RuntimeException("test runtime exception"));
        response = webServerServiceRest.previewResourceTemplate(webServer.getName(), "httpd.conf", "groupName", "httpd.conf");
        assertNotNull(response);
    }

    @Test
    public void testControlWebServerWait() {
        final CommandOutput execData = mock(CommandOutput.class);
        final ExecReturnCode execDataReturnCode = mock(ExecReturnCode.class);
        when(execDataReturnCode.wasSuccessful()).thenReturn(true);
        when(execData.getReturnCode()).thenReturn(execDataReturnCode);

        final JsonControlWebServer jsonControlWebServer = new JsonControlWebServer("start");
        when(Config.mockWebServerControlService.controlWebServer(any(ControlWebServerRequest.class), any(User.class))).thenReturn(execData);
        when(Config.mockWebServerControlService.waitForState(any(ControlWebServerRequest.class), anyLong())).thenReturn(true);
        final Response response = webServerServiceRest.controlWebServer(Identifier.id(1l, WebServer.class), jsonControlWebServer, Config.mockAuthenticatedUser, true, 120L);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test(expected = InternalErrorException.class)
    public void testControlWebServerWaitForException() {
        final CommandOutput execData = mock(CommandOutput.class);
        final ExecReturnCode execDataReturnCode = mock(ExecReturnCode.class);
        when(execDataReturnCode.wasSuccessful()).thenReturn(true);
        when(execData.getReturnCode()).thenReturn(execDataReturnCode);

        final JsonControlWebServer jsonControlWebServer = new JsonControlWebServer("start");
        when(Config.mockWebServerControlService.controlWebServer(any(ControlWebServerRequest.class), any(User.class))).thenReturn(execData);
        when(Config.mockWebServerControlService.waitForState(any(ControlWebServerRequest.class), anyLong())).thenReturn(false);
        webServerServiceRest.controlWebServer(Identifier.id(1l, WebServer.class), jsonControlWebServer, Config.mockAuthenticatedUser, true, 120L);
    }

    @Configuration
    static class Config {

        @Mock
        static WebServerService mockWebServerService = mock(WebServerService.class);

        @Mock
        static WebServerControlService mockWebServerControlService = mock(WebServerControlService.class);

        @Mock
        static WebServerCommandService mockCommandImpl = mock(WebServerCommandService.class);

        @Mock
        static ResourceService mockResourceService = mock(ResourceService.class);

        @Mock
        static GroupService mockGroupService = mock(GroupService.class);

        @Mock
        static AuthenticatedUser mockAuthenticatedUser = mock(AuthenticatedUser.class);

        @Mock
        static HistoryService mockHistoryService = mock(HistoryService.class);

        @Mock
        static HistoryFacadeService mockHistoryFacadeService = mock(HistoryFacadeService.class);

        @Mock
        static BinaryDistributionService mockBinaryDistributionService = mock(BinaryDistributionService.class);

        @Mock
        static BinaryDistributionLockManager mockBinaryDistributionLockManager = mock(BinaryDistributionLockManager.class);

        static SimpMessagingServiceImpl mockSimpleMessageService = mock(SimpMessagingServiceImpl.class);

        @Bean
        WebServerService getMockWebServerService() {
            return mockWebServerService;
        }

        @Bean
        WebServerControlService getWebServerControlService() {
            return mockWebServerControlService;
        }

        @Bean
        WebServerCommandService getWebServerCommandService() {
            return mockCommandImpl;
        }

        @Bean
        ResourceService getResourceService() {
            return mockResourceService;
        }

        @Bean
        GroupService getGroupService() {
            return mockGroupService;
        }

        @Bean
        AuthenticatedUser getAuthenticatedUser() {
            return mockAuthenticatedUser;
        }

        @Bean
        HistoryService getMockHistoryService() {
            return mockHistoryService;
        }

        @Bean
        HistoryFacadeService getMockHistoryFacadeService() {
            return mockHistoryFacadeService;
        }

        @Bean
        BinaryDistributionService getBinaryDistributionService() {
            return mockBinaryDistributionService;
        }

        @Bean
        BinaryDistributionLockManager getMockBinaryDistributionLockManager() {
            return mockBinaryDistributionLockManager;
        }

        @Bean
        SimpMessagingServiceImpl getMessagingService() {
            return mockSimpleMessageService;
        }

        @Bean
        WebServerServiceRest getWebServerServiceRest() {
            return new WebServerServiceRestImpl(mockWebServerService, mockWebServerControlService, mockCommandImpl, mockResourceService, mockGroupService, mockBinaryDistributionService, mockHistoryFacadeService);
        }

    }

}
