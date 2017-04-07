package com.cerner.jwala.service.webserver.impl;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.domain.model.resource.ResourceContent;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.request.webserver.ControlWebServerRequest;
import com.cerner.jwala.common.request.webserver.CreateWebServerRequest;
import com.cerner.jwala.common.request.webserver.UpdateWebServerRequest;
import com.cerner.jwala.common.request.webserver.UploadWebServerTemplateRequest;
import com.cerner.jwala.persistence.service.WebServerPersistenceService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionLockManager;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.service.resource.impl.ResourceGeneratorType;
import com.cerner.jwala.service.state.InMemoryStateManagerService;
import com.cerner.jwala.service.state.impl.InMemoryStateManagerServiceImpl;
import com.cerner.jwala.service.webserver.WebServerControlService;
import com.cerner.jwala.service.webserver.WebServerService;
import com.cerner.jwala.service.webserver.exception.WebServerServiceException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by Arvindo Kinny on 4/2/2014.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {WebServerServiceImplTest.Config.class})
public class WebServerServiceImplTest {

    @Autowired
    private WebServerService wsService;

    @Mock
    private WebServer mockWebServer;

    @Mock
    private WebServer mockWebServer2;


    private ArrayList<WebServer> mockWebServersAll = new ArrayList<>();
    private ArrayList<WebServer> mockWebServers11 = new ArrayList<>();
    private ArrayList<WebServer> mockWebServers12 = new ArrayList<>();

    private Group group;
    private Group group2;
    private Identifier<Group> groupId;
    private Identifier<Group> groupId2;
    private Collection<Identifier<Group>> groupIds;
    private Collection<Identifier<Group>> groupIds2;
    private Collection<Group> groups;
    private Collection<Group> groups2;

    private User testUser = new User("testUser");

    final Identifier<WebServer> id = new Identifier<>(1L);
    final User user = new User("user");

    @Before
    public void setUp() throws IOException {
        initMocks(this);

        groupId = new Identifier<>(1L);
        groupId2 = new Identifier<>(2L);
        groupIds = new ArrayList<>(1);
        groupIds2 = new ArrayList<>(1);
        groupIds.add(groupId);
        groupIds2.add(groupId2);
        group = new Group(groupId, "the-ws-group-name");
        group2 = new Group(new Identifier<Group>(2L), "the-ws-group-name-2");
        groups = new ArrayList<>(1);
        groups2 = new ArrayList<>(1);
        groups.add(group);
        groups2.add(group2);

        when(Config.mockResourceService.generateResourceGroup()).thenReturn(new ResourceGroup(new ArrayList<Group>()));

        when(mockWebServer.getId()).thenReturn(new Identifier<WebServer>(1L));
        when(mockWebServer.getName()).thenReturn("the-ws-name");
        when(mockWebServer.getHost()).thenReturn("the-ws-hostname");
        when(mockWebServer.getGroups()).thenReturn(groups);
        when(mockWebServer.getGroupIds()).thenReturn(groupIds);
        when(mockWebServer.getPort()).thenReturn(51000);
        when(mockWebServer.getHttpsPort()).thenReturn(52000);
        when(mockWebServer.getStatusPath()).thenReturn(new Path("/statusPath"));


        when(mockWebServer2.getId()).thenReturn(new Identifier<WebServer>(2L));
        when(mockWebServer2.getName()).thenReturn("the-ws-name-2");
        when(mockWebServer2.getHost()).thenReturn("the-ws-hostname");
        when(mockWebServer2.getGroups()).thenReturn(groups2);
        when(mockWebServer2.getPort()).thenReturn(51000);
        when(mockWebServer2.getHttpsPort()).thenReturn(52000);
        when(mockWebServer2.getStatusPath()).thenReturn(new Path("/statusPath"));

        mockWebServersAll.add(mockWebServer);
        mockWebServersAll.add(mockWebServer2);

        mockWebServers11.add(mockWebServer);
        mockWebServers12.add(mockWebServer2);

        reset(Config.mockBinaryDistributionLockManager, Config.mockResourceService, Config.mockWebServerControlService,
              Config.mockWebServerPersistenceService);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test() {
        when(Config.mockWebServerPersistenceService.getWebServer(any(Identifier.class))).thenReturn(mockWebServer);
        final WebServer webServer = wsService.getWebServer(new Identifier<WebServer>(1L));
        assertEquals(new Identifier<WebServer>(1L), webServer.getId());
        assertEquals(group.getId(), webServer.getGroups().iterator().next().getId());
        assertEquals("the-ws-name", webServer.getName());
        assertEquals("the-ws-group-name", webServer.getGroups().iterator().next().getName());
        assertEquals("the-ws-hostname", webServer.getHost());
    }

    @Test
    public void testGetWebServers() {
        when(Config.mockWebServerPersistenceService.getWebServers()).thenReturn(mockWebServersAll);
        final List<WebServer> webServers = wsService.getWebServers();
        assertEquals(2, webServers.size());
    }

    @Test
    public void testFindWebServersBelongingTo() {
        when(Config.mockWebServerPersistenceService.findWebServersBelongingTo(eq(new Identifier<Group>(1L)))).thenReturn(mockWebServers11);
        when(Config.mockWebServerPersistenceService.findWebServersBelongingTo(eq(new Identifier<Group>(2L)))).thenReturn(mockWebServers12);

        final List<WebServer> webServers = wsService.findWebServers(group.getId());
        final List<WebServer> webServers2 = wsService.findWebServers(group2.getId());

        assertEquals(1, webServers.size());
        assertEquals(1, webServers2.size());

        verify(Config.mockWebServerPersistenceService, times(1)).findWebServersBelongingTo(eq(group.getId()));
        verify(Config.mockWebServerPersistenceService, times(1)).findWebServersBelongingTo(eq(group2.getId()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateWebServers() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");

        when(mockWebServer.getState()).thenReturn(WebServerReachableState.WS_NEW);
        when(Config.mockWebServerPersistenceService.createWebServer(any(WebServer.class), anyString())).thenReturn(mockWebServer);
        CreateWebServerRequest cmd = new CreateWebServerRequest(mockWebServer.getGroupIds(),
                                                                mockWebServer.getName(),
                                                                mockWebServer.getHost(),
                                                                mockWebServer.getPort(),
                                                                mockWebServer.getHttpsPort(),
                                                                mockWebServer.getStatusPath(),
                                                                mockWebServer.getState());
        final WebServer webServer = wsService.createWebServer(cmd, testUser);

        assertEquals(new Identifier<WebServer>(1L), webServer.getId());
        assertEquals(group.getId(), webServer.getGroups().iterator().next().getId());
        assertEquals("the-ws-name", webServer.getName());
        assertEquals("the-ws-group-name", webServer.getGroups().iterator().next().getName());
        assertEquals("the-ws-hostname", webServer.getHost());
        assertEquals(WebServerReachableState.WS_NEW, Config.inMemService.get(webServer.getId()));

        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateWebServers() throws IOException {
        when(Config.mockWebServerPersistenceService.getWebServer(any(Identifier.class))).thenReturn(mockWebServer2);
        when(Config.mockWebServerPersistenceService.updateWebServer(any(WebServer.class), anyString())).thenReturn(mockWebServer2);

        ResourceContent mockResourceContent = mock(ResourceContent.class);
        when(mockResourceContent.getMetaData()).thenReturn("{deployPath:\"/fake/deploy/path\"}");
        ResourceTemplateMetaData mockResourceTemplateMetaData = mock(ResourceTemplateMetaData.class);
        when(mockResourceTemplateMetaData.getDeployPath()).thenReturn("/fake/deploy/path");
        when(Config.mockResourceService.getResourceContent(any(ResourceIdentifier.class))).thenReturn(mockResourceContent);
        when(Config.mockResourceService.getMetaData(anyString())).thenReturn(mockResourceTemplateMetaData);


        UpdateWebServerRequest cmd = new UpdateWebServerRequest(mockWebServer2.getId(),
                                                                groupIds2,
                                                                mockWebServer2.getName(),
                                                                mockWebServer2.getHost(),
                                                                mockWebServer2.getPort(),
                                                                mockWebServer2.getHttpsPort(),
                                                                mockWebServer2.getStatusPath(),
                                                                mockWebServer2.getState());
        final WebServer webServer = wsService.updateWebServer(cmd, testUser);

        assertEquals(new Identifier<WebServer>(2L), webServer.getId());
        assertEquals(group2.getId(), webServer.getGroups().iterator().next().getId());
        assertEquals("the-ws-name-2", webServer.getName());
        assertEquals(group2.getName(), webServer.getGroups().iterator().next().getName());
        assertEquals("the-ws-hostname", webServer.getHost());
    }

    private final String readReferenceFile(String file) throws IOException {
        BufferedReader bufferedReader =
                new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(file)));
        StringBuilder referenceHttpdConfBuilder = new StringBuilder();
        String line;
        do {
            line = bufferedReader.readLine();
            if (line != null) {
                referenceHttpdConfBuilder.append(line);
            }
        } while (line != null);

        return referenceHttpdConfBuilder.toString();
    }

    @Test
    public void testGetWebServer() {
        final WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServer.getName()).thenReturn("mockWebServer");
        when(Config.mockWebServerPersistenceService.findWebServerByName(eq("aWebServer"))).thenReturn(mockWebServer);
        assertEquals("mockWebServer", wsService.getWebServer("aWebServer").getName());
    }

    @Test
    public void testGetResourceTemplateNames() {
        final String[] nameArray = {"httpd.conf"};
        when(Config.mockWebServerPersistenceService.getResourceTemplateNames(eq("Apache2.4"))).thenReturn(Arrays.asList(nameArray));
        final List names = wsService.getResourceTemplateNames("Apache2.4");
        assertEquals("httpd.conf", names.get(0));
    }

    @Test
    public void testGetResourceTemplate() {
        when(Config.mockWebServerPersistenceService.getResourceTemplate(anyString(), anyString())).thenReturn("<template/>");
        assertEquals("<template/>", wsService.getResourceTemplate("any", "any", false, new ResourceGroup()));
    }

    @Test
    public void testGetResourceTemplateTokensReplaced() {
        final WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServer.getName()).thenReturn("mockWebServer");
        when(Config.mockWebServerPersistenceService.getResourceTemplate(anyString(), anyString())).thenReturn("<template>${webServer.name}</template>");
        when(Config.mockWebServerPersistenceService.findWebServerByName(anyString())).thenReturn(mockWebServer);
        wsService.getResourceTemplate("any", "httpd.conf", true, new ResourceGroup());
        verify(Config.mockResourceService).generateResourceFile(anyString(), anyString(), any(ResourceGroup.class),
                any(WebServer.class), any(ResourceGeneratorType.class));
    }

    @Test
    public void testNonHttpdConfGetResourceTemplateTokensReplaced() {
        final WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServer.getName()).thenReturn("mockWebServer");
        when(Config.mockWebServerPersistenceService.getResourceTemplate(anyString(), anyString())).thenReturn("<template>${webServer.name}</template>");
        when(Config.mockWebServerPersistenceService.findWebServerByName(anyString())).thenReturn(mockWebServer);
        wsService.getResourceTemplate("any", "any-except-httpd.conf", true, new ResourceGroup());
        verify(Config.mockResourceService).generateResourceFile(anyString(), anyString(), any(ResourceGroup.class),
                any(WebServer.class), any(ResourceGeneratorType.class));
    }

    @Test
    public void testUpdateResourceTemplate() {
        when(Config.mockWebServerPersistenceService.getResourceTemplate("wsName", "resourceTemplateName")).thenReturn("template");
        assertEquals("template", wsService.updateResourceTemplate("wsName", "resourceTemplateName", "template"));
        verify(Config.mockWebServerPersistenceService).updateResourceTemplate(eq("wsName"), eq("resourceTemplateName"), eq("template"));
    }

    @Test
    public void testUploadWebServerConfig() throws IOException {
        UploadWebServerTemplateRequest request = mock(UploadWebServerTemplateRequest.class);
        final String metaData = "{\"deployPath\":\"d:/httpd-data\",\"deployFileName\":\"httpd.conf\"}";
        when(request.getMetaData()).thenReturn(metaData);
        ResourceTemplateMetaData mockMetaData = mock(ResourceTemplateMetaData.class);
        when(mockMetaData.getDeployPath()).thenReturn("d:/httpd-data");
        when(mockMetaData.getDeployFileName()).thenReturn("httpd.conf");
        when(Config.mockResourceService.getTokenizedMetaData(anyString(), Matchers.anyObject(), anyString()))
                .thenReturn(mockMetaData);
        wsService.uploadWebServerConfig(mockWebServer, "httpd.conf", "test content", metaData, group.getName(), testUser);
        verify(Config.mockResourceService).createResource(any(ResourceIdentifier.class), any(ResourceTemplateMetaData.class),
                any(InputStream.class));
    }

    @Test
    public void testPreviewResourceTemplate() {
        List<Application> appList = new ArrayList<>();
        List<Jvm> jvmList = new ArrayList<>();
        when(Config.mockWebServerPersistenceService.findWebServerByName(anyString())).thenReturn(mockWebServer);
        when(Config.mockWebServerPersistenceService.findJvms(anyString())).thenReturn(jvmList);
        when(Config.mockWebServerPersistenceService.findApplications(anyString())).thenReturn(appList);
        wsService.previewResourceTemplate("myFile","wsName", "groupName", "my template");
        verify(Config.mockResourceService).generateResourceFile(eq("myFile"), eq("my template"), any(ResourceGroup.class),
                any(WebServer.class), any(ResourceGeneratorType.class));
    }

    @Test
    public void testIsStarted() {
        when(mockWebServer.getState()).thenReturn(WebServerReachableState.WS_REACHABLE);
        assertTrue(wsService.isStarted(mockWebServer));
    }

    @Test
    public void testUpdateState() {
        wsService.updateState(mockWebServer.getId(), WebServerReachableState.WS_REACHABLE, "");
        verify(Config.mockWebServerPersistenceService).updateState(new Identifier<WebServer>(1L), WebServerReachableState.WS_REACHABLE, "");
        assertEquals(WebServerReachableState.WS_REACHABLE, Config.inMemService.get(mockWebServer.getId()));
    }

    @Test
    public void testGetWebServerStartedCount() {
        final String groupName = "testGroup";
        final Long returnCount = 1L;
        when(Config.mockWebServerPersistenceService.getWebServerStartedCount(eq(groupName))).thenReturn(returnCount);
        assertEquals(returnCount, wsService.getWebServerStartedCount(groupName));
    }

    @Test
    public void testGetWebServerCount() {
        final String groupName = "testGroup";
        final Long returnCount = 1L;
        when(Config.mockWebServerPersistenceService.getWebServerCount(eq(groupName))).thenReturn(returnCount);
        assertEquals(returnCount, wsService.getWebServerCount(groupName));
    }

    @Test
    public void testGetWebServerStoppedCount() {
        final String groupName = "testGroup";
        final Long returnCount = 1L;
        when(Config.mockWebServerPersistenceService.getWebServerStoppedCount(eq(groupName))).thenReturn(returnCount);
        assertEquals(returnCount, wsService.getWebServerStoppedCount(groupName));
    }

    @Test
    public void testGetResourceTemplateMetaData() {
        final String wsName = "testWS";
        final String resourceTemplate = "resourceTemplateName";
        when(Config.mockWebServerPersistenceService.getResourceTemplateMetaData(eq(wsName), eq(resourceTemplate))).thenReturn("");
        assertEquals("", wsService.getResourceTemplateMetaData(wsName, resourceTemplate));
    }

    @Test (expected = WebServerServiceException.class)
    public void testGenerateInstallServiceWSBat() {
        when(Config.mockResourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class),
                eq(mockWebServer), any(ResourceGeneratorType.class))).thenThrow(IOException.class);
        wsService.generateInstallServiceScript(mockWebServer);
    }

    @Test
    public void testGetWebServerPropogationNew() {
        List<WebServer> webServers = new ArrayList<>();
        when(Config.mockWebServerPersistenceService.getWebServers()).thenReturn(webServers);
        assertEquals(webServers, wsService.getWebServersPropagationNew());
    }

    @Test (expected = InternalErrorException.class)
    public void testUploadWebServerConfigFail() throws IOException {
        UploadWebServerTemplateRequest request = mock(UploadWebServerTemplateRequest.class);
        final String metaData = "\"deployPath\":\"d:/httpd-data\",\"deployFileName\":\"httpd.conf\"}";
        when(request.getMetaData()).thenReturn(metaData);
        when(request.getWebServer()).thenReturn(mockWebServer);
        when(mockWebServer.getName()).thenReturn("testWebServer");
        when(request.getConfFileName()).thenReturn("httpd.conf");
        when(Config.mockResourceService.getTokenizedMetaData(anyString(), Matchers.anyObject(), anyString()))
                .thenThrow(new IOException("FAIL upload config because of meta data mapping"));
        wsService.uploadWebServerConfig(mockWebServer, "httpd.conf", "test content", metaData, group.getName(), testUser);
    }

    private String removeCarriageReturnsAndNewLines(String s) {
        return s.replaceAll("\\r", "").replaceAll("\\n", "");
    }

    @Test
    public void testDeleteNewWebServer() {
        when(mockWebServer.getState()).thenReturn(WebServerReachableState.WS_NEW);
        when(Config.mockWebServerPersistenceService.getWebServer(id)).thenReturn(mockWebServer);
        wsService.deleteWebServer(id, false, user);
        verify(Config.mockWebServerControlService, never()).controlWebServer(any(ControlWebServerRequest.class), eq(user));
        verify(Config.mockWebServerPersistenceService).removeWebServer(id);
    }

    @Test
    public void testDeleteNewWebServerWithHardOption() {
        when(mockWebServer.getState()).thenReturn(WebServerReachableState.WS_NEW);
        when(Config.mockWebServerPersistenceService.getWebServer(id)).thenReturn(mockWebServer);
        wsService.deleteWebServer(id, true, user);
        verify(Config.mockWebServerControlService, never()).controlWebServer(any(ControlWebServerRequest.class), eq(user));
        verify(Config.mockWebServerPersistenceService).removeWebServer(id);
    }

    @Test
    public void testDeleteStoppedWebServer() {
        final CommandOutput mockCommandOutput = mock(CommandOutput.class);
        when(mockWebServer.getState()).thenReturn(WebServerReachableState.WS_UNREACHABLE);
        when(Config.mockWebServerPersistenceService.getWebServer(id)).thenReturn(mockWebServer);
        when(mockCommandOutput.getReturnCode()).thenReturn(new ExecReturnCode(0));
        when(Config.mockWebServerControlService.controlWebServer(any(ControlWebServerRequest.class), eq(user))).thenReturn(mockCommandOutput);
        wsService.deleteWebServer(id, true, user);
        verify(Config.mockWebServerControlService).controlWebServer(any(ControlWebServerRequest.class), eq(user));
        verify(Config.mockWebServerPersistenceService).removeWebServer(id);
    }

    @Test
    public void testFailedDeleteServiceOfStoppedWebServer() {
        final CommandOutput mockCommandOutput = mock(CommandOutput.class);
        when(mockWebServer.getName()).thenReturn("testWebServer");
        when(mockWebServer.getState()).thenReturn(WebServerReachableState.WS_UNREACHABLE);
        when(Config.mockWebServerPersistenceService.getWebServer(id)).thenReturn(mockWebServer);
        when(mockCommandOutput.getReturnCode()).thenReturn(new ExecReturnCode(ExecReturnCode.JWALA_EXIT_NO_SUCH_SERVICE));
        when(Config.mockWebServerControlService.controlWebServer(any(ControlWebServerRequest.class), eq(user))).thenReturn(mockCommandOutput);
        try {
            wsService.deleteWebServer(id, true, user);
        } catch (final WebServerServiceException e) {
            assertTrue(e.getMessage().indexOf("Failed to delete the web server service testWebServer!") == 0);
        }
        verify(Config.mockWebServerControlService).controlWebServer(any(ControlWebServerRequest.class), eq(user));
        verify(Config.mockWebServerPersistenceService, never()).removeWebServer(id);
    }

    @Test
    public void testDeleteNonStoppedAndNotNewWebServer() {
        when(mockWebServer.getState()).thenReturn(WebServerReachableState.WS_REACHABLE);
        when(Config.mockWebServerPersistenceService.getWebServer(id)).thenReturn(mockWebServer);
        try {
            wsService.deleteWebServer(id, true, user);
            fail("Expecting to get a wsServiceException!");
        } catch (final WebServerServiceException e) {
            assertEquals("Please stop web server the-ws-name first before attempting to delete it", e.getMessage());
        }
        verify(Config.mockWebServerPersistenceService, never()).removeWebServer(id);
    }

    static class Config {

        private static WebServerPersistenceService mockWebServerPersistenceService = mock(WebServerPersistenceService.class);

        private static WebServerControlService mockWebServerControlService = mock(WebServerControlService.class);

        private static ResourceService mockResourceService = mock(ResourceService.class);

        private static InMemoryStateManagerService<Identifier<WebServer>, WebServerReachableState> inMemService =
                new InMemoryStateManagerServiceImpl<>();

        private static BinaryDistributionLockManager mockBinaryDistributionLockManager = mock(BinaryDistributionLockManager.class);

        @Bean
        public static WebServerPersistenceService getMockWebServerPersistenceService() {
            return mockWebServerPersistenceService;
        }

        @Bean
        public static WebServerControlService getMockWebServerControlService() {
            return mockWebServerControlService;
        }

        @Bean
        public ResourceService getMockResourceService() {
            return mockResourceService;
        }

        @Bean
        public InMemoryStateManagerService<Identifier<WebServer>, WebServerReachableState> getInMemService() {
            return inMemService;
        }

        @Bean
        public WebServerService getWebSereWebServerService() {
            return new WebServerServiceImpl(mockWebServerPersistenceService, mockResourceService, inMemService, "/any",
                    mockBinaryDistributionLockManager);
        }

    }
}