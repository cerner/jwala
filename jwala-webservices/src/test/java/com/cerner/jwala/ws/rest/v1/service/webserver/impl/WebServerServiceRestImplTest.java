package com.cerner.jwala.ws.rest.v1.service.webserver.impl;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerControlOperation;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.exception.FaultCodeException;
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
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.service.resource.impl.ResourceGeneratorType;
import com.cerner.jwala.service.webserver.WebServerCommandService;
import com.cerner.jwala.service.webserver.WebServerControlService;
import com.cerner.jwala.service.webserver.exception.WebServerServiceException;
import com.cerner.jwala.service.webserver.impl.WebServerServiceImpl;
import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;
import com.cerner.jwala.ws.rest.v1.response.ApplicationResponse;
import com.cerner.jwala.ws.rest.v1.response.ResponseBuilder;
import org.apache.tika.mime.MediaType;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.ServletInputStream;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author horspe00
 */
@RunWith(MockitoJUnitRunner.class)
public class WebServerServiceRestImplTest {

    private static final String name = "webserverName";
    private static final String name2 = "webserverName2";
    private static final String host = "localhost";
    private static final Path statusPath = new Path("/statusPath");
    private static final Path httpConfigFile = new Path("d:/some-dir/httpd.conf");
    private static final Path SVR_ROOT = new Path("./");
    private static final Path DOC_ROOT = new Path("htdocs");
    private static final List<WebServer> webServerList = createWebServerList();
    private static final WebServer webServer = webServerList.get(0);
    private static final WebServer webServer2 = webServerList.get(1);

    @Mock
    private WebServerServiceImpl impl;

    @Mock
    private WebServerControlService webServerControlService;

    @Mock
    private WebServerCommandService commandImpl;

    @Mock
    private ResourceService resourceService;

    @Mock
    private GroupService groupService;

    @Mock
    private AuthenticatedUser authenticatedUser;

    @Mock
    private HistoryService mockHistoryService;

    @Mock
    private HistoryFacadeService mockHistoryFacadeService;

    @Mock
    private BinaryDistributionService binaryDistributionService;

    @Mock
    private BinaryDistributionLockManager mockBinaryDistributionLockManager;

    private WebServerServiceRestImpl webServerServiceRest;
    private Map<String, ReentrantReadWriteLock> writeLockMap = new HashMap<>();
    private Response statusNotOk;

    private static List<WebServer> createWebServerList() {
        final Group groupOne = new Group(Identifier.id(1L, Group.class), "ws-groupOne");
        final Group groupTwo = new Group(Identifier.id(2L, Group.class), "ws-groupTwo");

        final List<Group> groupsList = new ArrayList<>();
        groupsList.add(groupOne);
        groupsList.add(groupTwo);
        final List<Group> singleGroupList = new ArrayList<>();
        singleGroupList.add(groupOne);

        final WebServer ws = new WebServer(Identifier.id(1L, WebServer.class), groupsList, name, host, 8080, 8009, statusPath,
                httpConfigFile, SVR_ROOT, DOC_ROOT, WebServerReachableState.WS_UNREACHABLE, null);
        final WebServer ws2 = new WebServer(Identifier.id(2L, WebServer.class), singleGroupList, name2, host, 8080, 8009, statusPath,
                httpConfigFile, SVR_ROOT, DOC_ROOT, WebServerReachableState.WS_UNREACHABLE, null);
        final List<WebServer> result = new ArrayList<>();
        result.add(ws);
        result.add(ws2);
        return result;
    }

    @Before
    public void setUp() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");


        webServerServiceRest = new WebServerServiceRestImpl(impl, webServerControlService, commandImpl, resourceService, groupService, binaryDistributionService, mockHistoryFacadeService);
        webServerServiceRest.setLockManager(mockBinaryDistributionLockManager);

        when(authenticatedUser.getUser()).thenReturn(new User("Unused"));

        InternalErrorException iee = new InternalErrorException(null, "User does not have permission to create the directory ~/.jwala");
        statusNotOk = ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                FaultType.DUPLICATE_GROUP_NAME, iee.getMessage(), iee));
    }

    @After
    public void tearDown() throws IOException {
        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testGetWebServerList() {
        when(impl.getWebServers()).thenReturn(webServerList);

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
        when(impl.getWebServer(any(Identifier.class))).thenReturn(webServer);

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
        when(impl.createWebServer(any(CreateWebServerRequest.class), any(User.class))).thenReturn(webServer2);

        final Response response = webServerServiceRest.createWebServer(jsonCreateWebServer, authenticatedUser);
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
        when(impl.createWebServer(any(CreateWebServerRequest.class), any(User.class))).thenReturn(webServer);

        List<String> templateNames = new ArrayList<>();
        templateNames.add("httpd.conf");

        when(groupService.getGroupWebServersResourceTemplateNames(anyString())).thenReturn(templateNames);
        when(groupService.getGroupWebServerResourceTemplate(anyString(), anyString(), eq(false), any(ResourceGroup.class))).thenReturn("httpd.conf template");
        when(groupService.getGroupWebServerResourceTemplateMetaData(anyString(), anyString())).thenReturn("{}");
        ResourceTemplateMetaData mockMetaData = mock(ResourceTemplateMetaData.class);
        when(mockMetaData.getDeployFileName()).thenReturn("httpd.conf");
        when(resourceService.getTokenizedMetaData(anyString(), Matchers.anyObject(), anyString())).thenReturn(mockMetaData);
        final Response response = webServerServiceRest.createWebServer(jsonCreateWebServer, authenticatedUser);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    @Test
    public void testUpdateWebServer() {
        final JsonUpdateWebServer jsonUpdateWebServer = mock(JsonUpdateWebServer.class);
        when(impl.updateWebServer(any(UpdateWebServerRequest.class), any(User.class))).thenReturn(webServer);

        final Response response = webServerServiceRest.updateWebServer(jsonUpdateWebServer, authenticatedUser);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();

        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof WebServer);

        final WebServer received = (WebServer) content;
        assertEquals(webServer, received);
    }

    @Test
    public void testRemoveWebServer() {
        when(webServerControlService.controlWebServer(any(ControlWebServerRequest.class), any(User.class))).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(impl.getWebServer(any(Identifier.class))).thenReturn(webServer);
        when(impl.getWebServer(anyString())).thenReturn(webServer);
        final Response response = webServerServiceRest.removeWebServer(Identifier.id(1l, WebServer.class), authenticatedUser, true);
        verify(impl, atLeastOnce()).removeWebServer(any(Identifier.class));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        assertEquals("successful", applicationResponse.getApplicationResponseContent());
    }

    @Test
    public void testRemoveWebServerWhenWebServerNotStopped() {
        when(impl.getWebServer(any(Identifier.class))).thenReturn(webServer);
        when(impl.isStarted(any(WebServer.class))).thenReturn(true);
        final Response response = webServerServiceRest.removeWebServer(Identifier.id(1l, WebServer.class), authenticatedUser, true);
        assertEquals("Web server webserverName must be stopped before it can be deleted!",
                ((ApplicationResponse) response.getEntity()).getApplicationResponseContent());
    }

    @Test
    public void testRemoveWebServerForceDelete() {
        when(webServerControlService.controlWebServer(any(ControlWebServerRequest.class), any(User.class))).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(impl.getWebServer(any(Identifier.class))).thenReturn(webServer);
        when(impl.getWebServer(anyString())).thenReturn(webServer);
        final Response response = webServerServiceRest.removeWebServer(Identifier.id(1l, WebServer.class), authenticatedUser, false);
        verify(impl, atLeastOnce()).removeWebServer(any(Identifier.class));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        assertEquals("successful", applicationResponse.getApplicationResponseContent());
    }

    @Test
    public void testRemoveWebServerException() {
        when(webServerControlService.controlWebServer(any(ControlWebServerRequest.class), any(User.class))).thenThrow(new RuntimeException("java.net.UnknownHostException"));
        when(impl.getWebServer(any(Identifier.class))).thenReturn(webServer);
        when(impl.getWebServer(anyString())).thenReturn(webServer);
        final Response response = webServerServiceRest.removeWebServer(Identifier.id(1l, WebServer.class), authenticatedUser, false);
        System.out.println(response.getStatus());
        System.out.println(response.getStatusInfo().toString());
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        assertEquals("java.net.UnknownHostException", applicationResponse.getApplicationResponseContent());
    }

    @Test
    public void testControlWebServer() {

        final CommandOutput execData = mock(CommandOutput.class);
        final ExecReturnCode execDataReturnCode = mock(ExecReturnCode.class);
        when(execDataReturnCode.wasSuccessful()).thenReturn(true);
        when(execData.getReturnCode()).thenReturn(execDataReturnCode);

        final JsonControlWebServer jsonControlWebServer = new JsonControlWebServer("start");
        when(webServerControlService.controlWebServer(any(ControlWebServerRequest.class), any(User.class))).thenReturn(execData);
        final Response response = webServerServiceRest.controlWebServer(Identifier.id(1l, WebServer.class), jsonControlWebServer, authenticatedUser, false, null);
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
        when(webServerControlService.controlWebServer(any(ControlWebServerRequest.class), any(User.class))).thenReturn(execData);
        webServerServiceRest.controlWebServer(Identifier.id(1l, WebServer.class), jsonControlWebServer, authenticatedUser, false, 120L);
    }

    @Test
    public void testGenerateHttpdConfig() {
        when(impl.getResourceTemplate(anyString(), anyString(), eq(true), any(ResourceGroup.class)))
                .thenReturn("httpd configuration");
        Response response = webServerServiceRest.generateConfig("any-server-name");
        assertEquals("httpd configuration", response.getEntity());
    }

    @Test
    public void testGetWebServersByGroup() {
        final List<WebServer> webServers = new ArrayList<>();
        webServers.add(new WebServer(null, new ArrayList<Group>(), "test", null, null, null, new Path("/statusPath"),
                new Path("d:/some-dir/httpd.conf"), SVR_ROOT, DOC_ROOT, WebServerReachableState.WS_UNREACHABLE,
                null));

        final Identifier<Group> groupId = new Identifier<>("1");

        when(impl.findWebServers(Matchers.eq(groupId))).thenReturn(webServers);
        final Response response = webServerServiceRest.getWebServers(groupId);

        final List<WebServer> result =
                (List<WebServer>) ((ApplicationResponse) response.getEntity()).getApplicationResponseContent();
        assertEquals("test", result.get(0).getName());
    }

    @Test
    public void testGenerateAndDeployConfig() throws CommandFailureException, IOException {
        CommandOutput retSuccessExecData = new CommandOutput(new ExecReturnCode(0), "", "");
//        when(webServerControlService.secureCopyFile(anyString(), anyString(), anyString(), anyString())).thenReturn(retSuccessExecData);
        when(impl.getResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"contentType\":\"text/plain\",\"deployPath\":\"./anyPath\",\"deployFileName\":\"httpd.conf\"}");
        CommandOutput commandOutput = mock(CommandOutput.class);
//        when(webServerControlService.createDirectory(isNull(WebServer.class), anyString())).thenReturn(commandOutput);
        ExecReturnCode execReturnCode = mock(ExecReturnCode.class);
        when(commandOutput.getReturnCode()).thenReturn(execReturnCode);
        when(commandOutput.getReturnCode().wasSuccessful()).thenReturn(true);
        when(resourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        when(resourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), any(), any(ResourceGeneratorType.class))).thenReturn("{\"contentType\":\"text/plain\",\"deployPath\":\"./anyPath\",\"deployFileName\":\"httpd.conf\"}");
        ResourceTemplateMetaData mockMetaData = mock(ResourceTemplateMetaData.class);
        when(mockMetaData.getDeployFileName()).thenReturn("httpd.conf");
        when(mockMetaData.getDeployPath()).thenReturn("./anyPath");
        when(mockMetaData.getContentType()).thenReturn(MediaType.TEXT_PLAIN);
        when(resourceService.getTokenizedMetaData(anyString(), Matchers.anyObject(), anyString())).thenReturn(mockMetaData);
        Response response = webServerServiceRest.generateAndDeployConfig(webServer.getName(), "httpd.conf", authenticatedUser);
        assertTrue(response.hasEntity());
    }

    @Test
    public void testGenerateAndDeployConfigBinaryFile() throws CommandFailureException, IOException {
        CommandOutput retSuccessExecData = new CommandOutput(new ExecReturnCode(0), "", "");
//        when(webServerControlService.secureCopyFile(anyString(), anyString(), anyString(), anyString())).thenReturn(retSuccessExecData);
        when(impl.getResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"contentType\":\"application/binary\",\"deployPath\":\"./anyPath\"}");
        when(resourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        CommandOutput commandOutput = mock(CommandOutput.class);
//        when(webServerControlService.createDirectory(isNull(WebServer.class), anyString())).thenReturn(commandOutput);
        ExecReturnCode execReturnCode = mock(ExecReturnCode.class);
        when(commandOutput.getReturnCode()).thenReturn(execReturnCode);
        when(commandOutput.getReturnCode().wasSuccessful()).thenReturn(true);
        when(resourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), any(), any(ResourceGeneratorType.class))).thenReturn("{\"contentType\":\"application/binary\",\"deployPath\":\"./anyPath\"}");
        ResourceTemplateMetaData mockMetaData = mock(ResourceTemplateMetaData.class);
        when(mockMetaData.getDeployFileName()).thenReturn("httpd.conf");
        when(mockMetaData.getDeployPath()).thenReturn("./anyPath");
        when(mockMetaData.getContentType()).thenReturn(MediaType.TEXT_PLAIN);
        when(resourceService.getTokenizedMetaData(anyString(), Matchers.anyObject(), anyString())).thenReturn(mockMetaData);
        Response response = webServerServiceRest.generateAndDeployConfig(webServer.getName(), "httpd.exe", authenticatedUser);
        assertTrue(response.hasEntity());
    }

    @Test
    public void testGenerateAndDeployConfigThrowsExceptionForWebServerNotStopped() throws CommandFailureException, IOException {
        when(impl.isStarted(any(WebServer.class))).thenReturn(true);
        when(impl.getResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"contentType\":\"text/plain\",\"deployPath\":\"./anyPath\"}");
        when(resourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        when(resourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), any(), any(ResourceGeneratorType.class))).thenReturn("{\"contentType\":\"text/plain\",\"deployPath\":\"./anyPath\"}");
        boolean exceptionThrown = false;
        try {
            webServerServiceRest.generateAndDeployConfig(webServer.getName(), "httpd.conf", authenticatedUser);
        } catch (InternalErrorException ie) {
            exceptionThrown = true;
            assertEquals(ie.getMessage(), "The target Web Server must be stopped before attempting to update the resource file");
        } //finally {
            //assertTrue(exceptionThrown);
        //}
    }

    /*@Test
    public void testGenerateAndDeployConfigFailsSecureCopy() throws CommandFailureException, IOException {
        when(impl.getResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"contentType\":\"text/plain\",\"deployPath\":\"./anyPath\",\"deployFileName\":\"httpd.conf\"}");
        when(resourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        CommandOutput commandOutput = mock(CommandOutput.class);
        when(webServerControlService.createDirectory(isNull(WebServer.class), anyString())).thenReturn(commandOutput);
        ExecReturnCode execReturnCode = mock(ExecReturnCode.class);
        when(commandOutput.getReturnCode()).thenReturn(execReturnCode);
        when(commandOutput.getReturnCode().wasSuccessful()).thenReturn(true);
        when(webServerControlService.secureCopyFile(anyString(), anyString(), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "", "FAILED SECURE COPY TEST"));
        when(resourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), any(), any(ResourceGeneratorType.class))).thenReturn("{\"contentType\":\"text/plain\",\"deployPath\":\"./anyPath\",\"deployFileName\":\"httpd.conf\"}");
        ResourceTemplateMetaData mockMetaData = mock(ResourceTemplateMetaData.class);
        when(mockMetaData.getDeployFileName()).thenReturn("httpd.conf");
        when(mockMetaData.getDeployPath()).thenReturn("./anyPath");
        when(mockMetaData.getContentType()).thenReturn(MediaType.TEXT_PLAIN);
        when(resourceService.getTokenizedMetaData(anyString(), Matchers.anyObject(), anyString())).thenReturn(mockMetaData);
        boolean failedSecureCopy = false;
        Response response = null;
        try {
            response = webServerServiceRest.generateAndDeployConfig(webServer.getName(), "httpd.conf", authenticatedUser);
        } catch (InternalErrorException e) {
            failedSecureCopy = true;
        }
        assertFalse(failedSecureCopy);
        assertNotNull(response);
        assertEquals(webServer.getName(), ((Map) ((ApplicationResponse) response.getEntity()).getApplicationResponseContent()).get("webServerName"));
    }*/

    /*@Test
    public void testGenerateAndDeployConfigThrowsException() throws IOException, CommandFailureException {
        when(impl.getResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"contentType\":\"text/plain\",\"deployPath\":\"./anyPath\",\"deployFileName\":\"httpd.conf\"}");
        when(resourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        when(webServerControlService.secureCopyFile(anyString(), anyString(), anyString(), anyString())).thenThrow(new CommandFailureException(new ExecCommand("Fail secure copy"), new Exception()));
        CommandOutput commandOutput = mock(CommandOutput.class);
        when(webServerControlService.createDirectory(isNull(WebServer.class), anyString())).thenReturn(commandOutput);
        ExecReturnCode execReturnCode = mock(ExecReturnCode.class);
        when(commandOutput.getReturnCode()).thenReturn(execReturnCode);
        when(commandOutput.getReturnCode().wasSuccessful()).thenReturn(true);
        Response response = null;
        when(resourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), any(), any(ResourceGeneratorType.class))).thenReturn("{\"contentType\":\"text/plain\",\"deployPath\":\"./anyPath\",\"deployFileName\":\"httpd.conf\"}");
        ResourceTemplateMetaData mockMetaData = mock(ResourceTemplateMetaData.class);
        when(mockMetaData.getDeployFileName()).thenReturn("httpd.conf");
        when(mockMetaData.getDeployPath()).thenReturn("./anyPath");
        when(mockMetaData.getContentType()).thenReturn(MediaType.TEXT_PLAIN);
        when(resourceService.getTokenizedMetaData(anyString(), Matchers.anyObject(), anyString())).thenReturn(mockMetaData);
        response = webServerServiceRest.generateAndDeployConfig(webServer.getName(), "httpd.conf", authenticatedUser);
        assertNotNull(response);
        assertEquals(webServer.getName(), ((Map) ((ApplicationResponse) response.getEntity()).getApplicationResponseContent()).get("webServerName"));
    }*/

    @Test (expected = WebServerServiceException.class)
    public void testGenerateAndDeployWebServerWithNoHttpdConfTemplate() {
        when(impl.getWebServer(anyString())).thenReturn(webServer);
        when(impl.isStarted(any(WebServer.class))).thenReturn(false);
        webServerServiceRest.generateAndDeployWebServer(webServer.getName(), authenticatedUser);
    }

    @Test
    public void testGenerateAndDeployWebServer() throws CommandFailureException, IOException {
        List<String> webServerResourceNames = new ArrayList<>();
        webServerResourceNames.add("httpd.conf");

        CommandOutput retSuccessExecData = new CommandOutput(new ExecReturnCode(0), "", "");
        when(webServerControlService.controlWebServer(any(ControlWebServerRequest.class), any(User.class))).thenReturn(retSuccessExecData);
//        when(webServerControlService.secureCopyFile(anyString(), anyString(), anyString(), anyString())).thenReturn(retSuccessExecData);
//        when(webServerControlService.createDirectory(any(WebServer.class), anyString())).thenReturn(retSuccessExecData);
//        when(webServerControlService.changeFileMode(any(WebServer.class), anyString(), anyString(), anyString())).thenReturn(retSuccessExecData);
        when(impl.getWebServer(anyString())).thenReturn(webServer);
        when(impl.generateInstallServiceScript(any(WebServer.class))).thenReturn("invoke me");
        when(impl.getResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"contentType\":\"text/plain\",\"deployPath\":\"./anyPath\",\"deployFileName\":\"httpd.conf\"}");
        when(impl.getResourceTemplateNames(anyString())).thenReturn(webServerResourceNames);
        when(resourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        when(resourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), any(), any(ResourceGeneratorType.class))).thenReturn("{\"contentType\":\"text/plain\",\"deployPath\":\"./anyPath\",\"deployFileName\":\"httpd.conf\"}");
        ResourceTemplateMetaData mockMetaData = mock(ResourceTemplateMetaData.class);
        when(mockMetaData.getDeployFileName()).thenReturn("httpd.conf");
        when(mockMetaData.getDeployPath()).thenReturn("./anyPath");
        when(mockMetaData.getContentType()).thenReturn(MediaType.TEXT_PLAIN);
        when(resourceService.getTokenizedMetaData(anyString(), Matchers.anyObject(), anyString())).thenReturn(mockMetaData);

        Response response = null;
        response = webServerServiceRest.generateAndDeployWebServer(webServer.getName(), authenticatedUser);
        assertNotNull(response);

//        when(webServerControlService.secureCopyFile(anyString(), anyString(), anyString(), anyString())).thenThrow(new CommandFailureException(new ExecCommand("failed command"), new Throwable()));
        response = null;
        response = webServerServiceRest.generateAndDeployWebServer(webServer.getName(), authenticatedUser);
        assertNotNull(response);
    }

    @Test (expected = InternalErrorException.class)
    @Ignore
    public void testGenerateAndDeployWebServerFailsMakeDirectory() throws CommandFailureException, IOException {
        List<String> resourceTemplateNames = new ArrayList<>();
        resourceTemplateNames.add("httpd.conf");

        CommandOutput retFailExecData = new CommandOutput(new ExecReturnCode(1), "", "Failed to create the directory");
//        when(webServerControlService.createDirectory(any(WebServer.class), anyString())).thenReturn(retFailExecData);
        when(impl.getWebServer(anyString())).thenReturn(webServer);
        when(impl.isStarted(any(WebServer.class))).thenReturn(false);
        when(impl.getResourceTemplateNames(anyString())).thenReturn(resourceTemplateNames);

        webServerServiceRest.generateAndDeployWebServer(webServer.getName(), authenticatedUser);

    }

    @Test (expected = InternalErrorException.class)
    @Ignore
    public void testGenerateAndDeployWebServerFailsSecureCopyScriptsStartScript() throws CommandFailureException, IOException {
        List<String> resourceTemplateNames = new ArrayList<>();
        resourceTemplateNames.add("httpd.conf");

        CommandOutput retFailExecData = new CommandOutput(new ExecReturnCode(1), "", "Failed secure copy");
        CommandOutput retSuccessExecData = new CommandOutput(new ExecReturnCode(0), "", "");
//        when(webServerControlService.createDirectory(any(WebServer.class), anyString())).thenReturn(retSuccessExecData);
//        when(webServerControlService.secureCopyFile(eq(webServer.getName()), eq(ApplicationProperties.get("commands.scripts-path") + "/" + AemControl.Properties.START_SCRIPT_NAME.getValue()), anyString(), anyString())).thenReturn(retFailExecData);
        when(impl.getWebServer(anyString())).thenReturn(webServer);
        when(impl.isStarted(any(WebServer.class))).thenReturn(false);
        when(impl.getResourceTemplateNames(anyString())).thenReturn(resourceTemplateNames);

        webServerServiceRest.generateAndDeployWebServer(webServer.getName(), authenticatedUser);

    }

    @Test (expected = InternalErrorException.class)
    @Ignore
    public void testGenerateAndDeployWebServerFailsSecureCopyScriptsStopScript() throws CommandFailureException, IOException {
        List<String> resourceTemplateNames = new ArrayList<>();
        resourceTemplateNames.add("httpd.conf");

        CommandOutput retFailExecData = new CommandOutput(new ExecReturnCode(1), "", "Failed secure copy");
        CommandOutput retSuccessExecData = new CommandOutput(new ExecReturnCode(0), "", "");
//        when(webServerControlService.createDirectory(any(WebServer.class), anyString())).thenReturn(retSuccessExecData);
//        when(webServerControlService.secureCopyFile(eq(webServer.getName()), eq(ApplicationProperties.get("commands.scripts-path") + "/" + AemControl.Properties.START_SCRIPT_NAME.getValue()), anyString(), anyString())).thenReturn(retSuccessExecData);
//        when(webServerControlService.secureCopyFile(eq(webServer.getName()), eq(ApplicationProperties.get("commands.scripts-path") + "/" + AemControl.Properties.STOP_SCRIPT_NAME.getValue()), anyString(), anyString())).thenReturn(retFailExecData);
        when(impl.getWebServer(anyString())).thenReturn(webServer);
        when(impl.isStarted(any(WebServer.class))).thenReturn(false);
        when(impl.getResourceTemplateNames(anyString())).thenReturn(resourceTemplateNames);

        webServerServiceRest.generateAndDeployWebServer(webServer.getName(), authenticatedUser);
    }

    @Test (expected = InternalErrorException.class)
    @Ignore
    public void testGenerateAndDeployWebServerFailsSecureCopyScriptsInvokeScript() throws CommandFailureException, IOException {
        List<String> resourceTemplateNames = new ArrayList<>();
        resourceTemplateNames.add("httpd.conf");

        CommandOutput retFailExecData = new CommandOutput(new ExecReturnCode(1), "", "Failed secure copy");
        CommandOutput retSuccessExecData = new CommandOutput(new ExecReturnCode(0), "", "");
 /*       when(webServerControlService.createDirectory(any(WebServer.class), anyString())).thenReturn(retSuccessExecData);
        when(webServerControlService.secureCopyFile(eq(webServer.getName()), eq(ApplicationProperties.get("commands.scripts-path") + "/" + AemControl.Properties.START_SCRIPT_NAME.getValue()), anyString(), anyString())).thenReturn(retSuccessExecData);
        when(webServerControlService.secureCopyFile(eq(webServer.getName()), eq(ApplicationProperties.get("commands.scripts-path") + "/" + AemControl.Properties.STOP_SCRIPT_NAME.getValue()), anyString(), anyString())).thenReturn(retSuccessExecData);
        when(webServerControlService.secureCopyFile(eq(webServer.getName()), eq(ApplicationProperties.get("commands.scripts-path") + "/" + AemControl.Properties.INSTALL_SERVICE_WS_SERVICE_SCRIPT_NAME.getValue()), anyString(), anyString())).thenReturn(retFailExecData);
*/        when(impl.getWebServer(anyString())).thenReturn(webServer);
        when(impl.isStarted(any(WebServer.class))).thenReturn(false);
        when(impl.getResourceTemplateNames(anyString())).thenReturn(resourceTemplateNames);

        webServerServiceRest.generateAndDeployWebServer(webServer.getName(), authenticatedUser);
    }

    @Test (expected = InternalErrorException.class)
    @Ignore
    public void testGenerateAndDeployWebServerFailsChangeFileMode() throws CommandFailureException, IOException {
        List<String> resourceTemplateNames = new ArrayList<>();
        resourceTemplateNames.add("httpd.conf");

        CommandOutput retFailExecData = new CommandOutput(new ExecReturnCode(1), "", "Failed secure copy");
        CommandOutput retSuccessExecData = new CommandOutput(new ExecReturnCode(0), "", "");
/*
        when(webServerControlService.createDirectory(any(WebServer.class), anyString())).thenReturn(retSuccessExecData);
        when(webServerControlService.secureCopyFile(eq(webServer.getName()), eq(ApplicationProperties.get("commands.scripts-path") + "/" + AemControl.Properties.START_SCRIPT_NAME.getValue()), anyString(), anyString())).thenReturn(retSuccessExecData);
        when(webServerControlService.secureCopyFile(eq(webServer.getName()), eq(ApplicationProperties.get("commands.scripts-path") + "/" + AemControl.Properties.STOP_SCRIPT_NAME.getValue()), anyString(), anyString())).thenReturn(retSuccessExecData);
        when(webServerControlService.secureCopyFile(eq(webServer.getName()), eq(ApplicationProperties.get("commands.scripts-path") + "/" + AemControl.Properties.INSTALL_SERVICE_WS_SERVICE_SCRIPT_NAME.getValue()), anyString(), anyString())).thenReturn(retSuccessExecData);
        when(webServerControlService.changeFileMode(any(WebServer.class), anyString(), anyString(), anyString())).thenReturn(retFailExecData);
*/
        when(impl.getWebServer(anyString())).thenReturn(webServer);
        when(impl.isStarted(any(WebServer.class))).thenReturn(false);
        when(impl.getResourceTemplateNames(anyString())).thenReturn(resourceTemplateNames);

        webServerServiceRest.generateAndDeployWebServer(webServer.getName(), authenticatedUser);
    }

    @Test (expected = InternalErrorException.class)
    @Ignore
    public void testGenerateAndDeployWebServerSecureCopyInstallServiceWSServiceFails() throws CommandFailureException, IOException {
        List<String> webServerResourceNames = new ArrayList<>();
        webServerResourceNames.add("httpd.conf");

        CommandOutput retSuccessExecData = new CommandOutput(new ExecReturnCode(0), "", "");
        CommandOutput retFailExecData = new CommandOutput(new ExecReturnCode(1), "", "Failed secure copy");
        when(webServerControlService.controlWebServer(any(ControlWebServerRequest.class), any(User.class))).thenReturn(retSuccessExecData);

     /*   when(webServerControlService.secureCopyFile(anyString(), anyString(), anyString(), anyString())).thenReturn(retSuccessExecData);
        when(webServerControlService.secureCopyFile(anyString(), contains("install-ws-service"), anyString(), anyString())).thenReturn(retFailExecData);
        when(webServerControlService.createDirectory(any(WebServer.class), anyString())).thenReturn(retSuccessExecData);
        when(webServerControlService.changeFileMode(any(WebServer.class), anyString(), anyString(), anyString())).thenReturn(retSuccessExecData);
     */   when(impl.getWebServer(anyString())).thenReturn(webServer);
        when(impl.generateInstallServiceScript(any(WebServer.class))).thenReturn("invoke me");
        when(impl.getResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"contentType\":\"text/plain\",\"deployPath\":\"./anyPath\",\"deployFileName\":\"httpd.conf\"}");
        when(impl.getResourceTemplateNames(anyString())).thenReturn(webServerResourceNames);
        when(resourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        when(resourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), any(), any(ResourceGeneratorType.class))).thenReturn("{\"contentType\":\"text/plain\",\"deployPath\":\"./anyPath\",\"deployFileName\":\"httpd.conf\"}");
        ResourceTemplateMetaData mockMetaData = mock(ResourceTemplateMetaData.class);
        when(mockMetaData.getDeployFileName()).thenReturn("httpd.conf");
        when(mockMetaData.getDeployPath()).thenReturn("./anyPath");
        when(mockMetaData.getContentType()).thenReturn(MediaType.TEXT_PLAIN);
        when(resourceService.getTokenizedMetaData(anyString(), Matchers.anyObject(), anyString())).thenReturn(mockMetaData);

        webServerServiceRest.generateAndDeployWebServer(webServer.getName(), authenticatedUser);

    }

    @Test (expected = InternalErrorException.class)
    public void testGenerateAndDeployWebServerCallInstallServiceWSFails() throws CommandFailureException, IOException {
        List<String> webServerResourceNames = new ArrayList<>();
        webServerResourceNames.add("httpd.conf");

        CommandOutput retSuccessExecData = new CommandOutput(new ExecReturnCode(0), "", "");
        CommandOutput retFailExecData = new CommandOutput(new ExecReturnCode(1), "", "Failed secure copy");
        when(webServerControlService.controlWebServer(any(ControlWebServerRequest.class), any(User.class))).thenReturn(retSuccessExecData);
        //when(webServerControlService.secureCopyFile(anyString(), anyString(), anyString(), anyString())).thenReturn(retSuccessExecData);
        when(webServerControlService.controlWebServer(eq(new ControlWebServerRequest(webServer.getId(), WebServerControlOperation.INSTALL_SERVICE)), any(User.class))).thenReturn(retFailExecData);
        //when(webServerControlService.createDirectory(any(WebServer.class), anyString())).thenReturn(retSuccessExecData);
//        when(webServerControlService.changeFileMode(any(WebServer.class), anyString(), anyString(), anyString())).thenReturn(retSuccessExecData);
        when(impl.getWebServer(anyString())).thenReturn(webServer);
        when(impl.generateInstallServiceScript(any(WebServer.class))).thenReturn("invoke me");
        when(impl.getResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"contentType\":\"text/plain\",\"deployPath\":\"./anyPath\",\"deployFileName\":\"httpd.conf\"}");
        when(impl.getResourceTemplateNames(anyString())).thenReturn(webServerResourceNames);
        when(resourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        when(resourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), any(), any(ResourceGeneratorType.class))).thenReturn("{\"contentType\":\"text/plain\",\"deployPath\":\"./anyPath\",\"deployFileName\":\"httpd.conf\"}");
        ResourceTemplateMetaData mockMetaData = mock(ResourceTemplateMetaData.class);
        when(mockMetaData.getDeployFileName()).thenReturn("httpd.conf");
        when(mockMetaData.getDeployPath()).thenReturn("./anyPath");
        when(mockMetaData.getContentType()).thenReturn(MediaType.TEXT_PLAIN);
        when(resourceService.getTokenizedMetaData(anyString(), Matchers.anyObject(), anyString())).thenReturn(mockMetaData);

        webServerServiceRest.generateAndDeployWebServer(webServer.getName(), authenticatedUser);
    }

    @Test
    public void testGenerateAndDeployWebServerDeleteServiceForNonexistentService() throws CommandFailureException, IOException {
        List<String> webServerResourceNames = new ArrayList<>();
        webServerResourceNames.add("httpd.conf");

        CommandOutput retSuccessExecData = new CommandOutput(new ExecReturnCode(0), "", "");
        CommandOutput retServiceDoesNotExist = new CommandOutput(new ExecReturnCode(36), "", "Service does not exist");
        when(webServerControlService.controlWebServer(any(ControlWebServerRequest.class), any(User.class))).thenReturn(retSuccessExecData);
/*
        when(webServerControlService.secureCopyFile(anyString(), anyString(), anyString(), anyString())).thenReturn(retSuccessExecData);
        when(webServerControlService.controlWebServer(eq(new ControlWebServerRequest(webServer.getId(), WebServerControlOperation.DELETE_SERVICE)), any(User.class))).thenReturn(retServiceDoesNotExist);
        when(webServerControlService.createDirectory(any(WebServer.class), anyString())).thenReturn(retSuccessExecData);
        when(webServerControlService.changeFileMode(any(WebServer.class), anyString(), anyString(), anyString())).thenReturn(retSuccessExecData);
*/
        when(impl.getWebServer(anyString())).thenReturn(webServer);
        when(impl.generateInstallServiceScript(any(WebServer.class))).thenReturn("invoke me");
        when(impl.getResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"contentType\":\"text/plain\",\"deployPath\":\"./anyPath\",\"deployFileName\":\"httpd.conf\"}");
        when(impl.getResourceTemplateNames(anyString())).thenReturn(webServerResourceNames);
        when(resourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        when(resourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), any(), any(ResourceGeneratorType.class))).thenReturn("{\"contentType\":\"text/plain\",\"deployPath\":\"./anyPath\",\"deployFileName\":\"httpd.conf\"}");
        ResourceTemplateMetaData mockMetaData = mock(ResourceTemplateMetaData.class);
        when(mockMetaData.getDeployFileName()).thenReturn("httpd.conf");
        when(mockMetaData.getDeployPath()).thenReturn("./anyPath");
        when(mockMetaData.getContentType()).thenReturn(MediaType.TEXT_PLAIN);
        when(resourceService.getTokenizedMetaData(anyString(), Matchers.anyObject(), anyString())).thenReturn(mockMetaData);

        Response response = webServerServiceRest.generateAndDeployWebServer(webServer.getName(), authenticatedUser);
        assertNotNull(response);

    }

    @Test (expected = InternalErrorException.class)
    @Ignore
    public void testGenerateAndDeployWebServerDeleteServiceFails() throws CommandFailureException, IOException {
        List<String> webServerResourceNames = new ArrayList<>();
        webServerResourceNames.add("httpd.conf");

        CommandOutput retSuccessExecData = new CommandOutput(new ExecReturnCode(0), "", "");
        CommandOutput retServiceDoesNotExist = new CommandOutput(new ExecReturnCode(1), "", "Fail to delete service");
        when(webServerControlService.controlWebServer(any(ControlWebServerRequest.class), any(User.class))).thenReturn(retSuccessExecData);
/*        when(webServerControlService.secureCopyFile(anyString(), anyString(), anyString(), anyString())).thenReturn(retSuccessExecData);
        when(webServerControlService.controlWebServer(eq(new ControlWebServerRequest(webServer.getId(), WebServerControlOperation.DELETE_SERVICE)), any(User.class))).thenReturn(retServiceDoesNotExist);
        when(webServerControlService.createDirectory(any(WebServer.class), anyString())).thenReturn(retSuccessExecData);
        when(webServerControlService.changeFileMode(any(WebServer.class), anyString(), anyString(), anyString())).thenReturn(retSuccessExecData);
 */       when(impl.getWebServer(anyString())).thenReturn(webServer);
        when(impl.generateInstallServiceScript(any(WebServer.class))).thenReturn("invoke me");
        when(impl.getResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"contentType\":\"text/plain\",\"deployPath\":\"./anyPath\",\"deployFileName\":\"httpd.conf\"}");
        when(impl.getResourceTemplateNames(anyString())).thenReturn(webServerResourceNames);
        when(resourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        when(resourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), any(), any(ResourceGeneratorType.class))).thenReturn("{\"contentType\":\"text/plain\",\"deployPath\":\"./anyPath\",\"deployFileName\":\"httpd.conf\"}");

        webServerServiceRest.generateAndDeployWebServer(webServer.getName(), authenticatedUser);
    }

    @Test
    public void testGenerateAndDeployWebServerTemplate() throws CommandFailureException, IOException {
        CommandOutput retSuccessExecData = new CommandOutput(new ExecReturnCode(0), "", "");
//        when(webServerControlService.secureCopyFile(anyString(), anyString(), anyString(), anyString())).thenReturn(retSuccessExecData);
        when(impl.getResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"contentType\":\"text/plain\",\"deployPath\":\"./anyPath\",\"deployFileName\":\"${webServer.name}.txt\"}");
        CommandOutput commandOutput = mock(CommandOutput.class);
 //       when(webServerControlService.createDirectory(isNull(WebServer.class), anyString())).thenReturn(commandOutput);
        ExecReturnCode execReturnCode = mock(ExecReturnCode.class);
        when(commandOutput.getReturnCode()).thenReturn(execReturnCode);
        when(commandOutput.getReturnCode().wasSuccessful()).thenReturn(true);
        when(resourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        when(impl.getResourceTemplate(anyString(), anyString(), anyBoolean(), any(ResourceGroup.class))).thenReturn("");
        when(impl.getWebServer(anyString())).thenReturn(webServer);
        when(resourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), any(), any(ResourceGeneratorType.class))).thenReturn("{\"contentType\":\"text/plain\",\"deployPath\":\"./anyPath\",\"deployFileName\":\"${webServer.name}.txt\"}");
        ResourceTemplateMetaData mockMetaData = mock(ResourceTemplateMetaData.class);
        when(mockMetaData.getDeployFileName()).thenReturn("httpd.conf");
        when(mockMetaData.getDeployPath()).thenReturn("./anyPath");
        when(mockMetaData.getContentType()).thenReturn(MediaType.TEXT_PLAIN);
        when(resourceService.getTokenizedMetaData(anyString(), Matchers.anyObject(), anyString())).thenReturn(mockMetaData);

        Response response = webServerServiceRest.generateAndDeployConfig(webServer.getName(), "${webServer.name}.txt", authenticatedUser);
        assertTrue(response.hasEntity());
    }

    @Test (expected = InternalErrorException.class)
    public void testGenerateAndDeployWebServerWhenWebServerNotStopped() {
        when(impl.getWebServer(anyString())).thenReturn(webServer);
        when(impl.isStarted(any(WebServer.class))).thenReturn(true);
        webServerServiceRest.generateAndDeployWebServer(webServer.getName(), authenticatedUser);
    }

    @Test
    public void testGetHttpdConfig() throws CommandFailureException {
        when(commandImpl.getHttpdConf(webServer.getId())).thenReturn(new CommandOutput(new ExecReturnCode(0), "", ""));
        Response response = webServerServiceRest.getHttpdConfig(webServer.getId());
        assertTrue(response.hasEntity());
    }

    @Test
    public void testGetHttpdConfigThrowsException() throws CommandFailureException {
        when(commandImpl.getHttpdConf(webServer.getId())).thenThrow(CommandFailureException.class);
        Response response = webServerServiceRest.getHttpdConfig(webServer.getId());
        assertTrue(response.hasEntity());
    }

    @Test
    public void testGetResourceName() {
        List<String> resourceNames = new ArrayList<>();
        resourceNames.add("httpd-test.tpl");
        when(impl.getResourceTemplateNames(webServer.getName())).thenReturn(resourceNames);
        Response response = webServerServiceRest.getResourceNames(webServer.getName());
        assertTrue(response.hasEntity());
    }

    @Test
    public void testGetResourceTemplates() {
        String resourceTemplateName = "httpd-conf.tpl";
        when(impl.getResourceTemplate(webServer.getName(), resourceTemplateName, false, new ResourceGroup())).thenReturn("ServerRoot=./test");
        Response response = webServerServiceRest.getResourceTemplate(webServer.getName(), resourceTemplateName, false);
        assertTrue(response.hasEntity());
    }

    @Test
    public void testUpdateResourceTemplate() {
        String resourceTemplateName = "httpd-conf.tpl";
        String content = "ServerRoot=./test-update";
        when(impl.updateResourceTemplate(webServer.getName(), resourceTemplateName, content)).thenReturn(content);
        Response response = webServerServiceRest.updateResourceTemplate(webServer.getName(), resourceTemplateName, content);
        assertTrue(response.hasEntity());
    }

    @Test
    public void testUpdateResourceTemplateException() {
        String resourceTemplateName = "httpd-conf.tpl";
        String content = "ServerRoot=./test-update";
        when(impl.updateResourceTemplate(webServer.getName(), resourceTemplateName, content)).thenReturn(null);
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

        when(impl.previewResourceTemplate(anyString(), anyString(), anyString(), anyString())).thenThrow(new RuntimeException("test runtime exception"));
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
        when(webServerControlService.controlWebServer(any(ControlWebServerRequest.class), any(User.class))).thenReturn(execData);
        when(webServerControlService.waitForState(any(ControlWebServerRequest.class), anyLong())).thenReturn(true);
        final Response response = webServerServiceRest.controlWebServer(Identifier.id(1l, WebServer.class), jsonControlWebServer, authenticatedUser, true, 120L);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test (expected = InternalErrorException.class)
    public void testControlWebServerWaitForException() {
        final CommandOutput execData = mock(CommandOutput.class);
        final ExecReturnCode execDataReturnCode = mock(ExecReturnCode.class);
        when(execDataReturnCode.wasSuccessful()).thenReturn(true);
        when(execData.getReturnCode()).thenReturn(execDataReturnCode);

        final JsonControlWebServer jsonControlWebServer = new JsonControlWebServer("start");
        when(webServerControlService.controlWebServer(any(ControlWebServerRequest.class), any(User.class))).thenReturn(execData);
        when(webServerControlService.waitForState(any(ControlWebServerRequest.class), anyLong())).thenReturn(false);
        webServerServiceRest.controlWebServer(Identifier.id(1l, WebServer.class), jsonControlWebServer, authenticatedUser, true, 120L);
    }

    /**
     * Instead of mocking the ServletInputStream, let's extend it instead.
     *
     * @see "http://stackoverflow.com/questions/20995874/how-to-mock-a-javax-servlet-servletinputstream"
     */
    static class DelegatingServletInputStream extends ServletInputStream {

        private InputStream inputStream;

        public DelegatingServletInputStream() {
            inputStream = new ByteArrayInputStream("------WebKitFormBoundaryXRxegBGqTe4gApI2\r\nContent-Disposition: form-data; name=\"hct.properties\"; filename=\"hotel-booking.txt\"\r\nContent-Type: text/plain\r\n\r\n\r\n------WebKitFormBoundaryXRxegBGqTe4gApI2--".getBytes(Charset.defaultCharset()));
        }

        /**
         * Return the underlying source stream (never <code>null</code>).
         */
        public final InputStream getSourceStream() {
            return inputStream;
        }


        public int read() throws IOException {
            return inputStream.read();
        }

        public void close() throws IOException {
            super.close();
            inputStream.close();
        }

    }

    private class MyIS extends ServletInputStream {

        private InputStream backingStream;

        public MyIS(InputStream backingStream) {
            this.backingStream = backingStream;
        }

        @Override
        public int read() throws IOException {
            return backingStream.read();
        }

    }

}
