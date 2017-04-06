package com.cerner.jwala.service.webserver.impl;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.request.webserver.CreateWebServerRequest;
import com.cerner.jwala.common.request.webserver.UpdateWebServerRequest;
import com.cerner.jwala.common.request.webserver.UploadWebServerTemplateRequest;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.persistence.service.WebServerPersistenceService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionLockManager;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.service.resource.impl.ResourceGeneratorType;
import com.cerner.jwala.service.state.InMemoryStateManagerService;
import com.cerner.jwala.service.webserver.WebServerCommandService;
import com.cerner.jwala.service.webserver.WebServerControlService;
import com.cerner.jwala.service.webserver.WebServerService;
import com.cerner.jwala.service.webserver.component.ClientFactoryHelper;
import com.cerner.jwala.service.webserver.component.WebServerStateSetterWorker;
import com.cerner.jwala.service.webserver.exception.WebServerServiceException;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

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

    @BeforeClass
    public static void init() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH,
                WebServerServiceImplTest.class.getClassLoader().getResource("vars.properties").getPath()
                        .replace("/vars.properties", ""));
    }

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
    private ResourceGroup resourceGroup;


    @Before
    public void setUp() throws IOException {

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

        when(Config.resourceService.generateResourceGroup()).thenReturn(new ResourceGroup(new ArrayList<Group>()));

        when(Config.mockWebServer.getId()).thenReturn(new Identifier<WebServer>(1L));
        when(Config.mockWebServer.getName()).thenReturn("the-ws-name");
        when(Config.mockWebServer.getHost()).thenReturn("the-ws-hostname");
        when(Config.mockWebServer.getGroups()).thenReturn(groups);
        when(Config.mockWebServer.getGroupIds()).thenReturn(groupIds);
        when(Config.mockWebServer.getPort()).thenReturn(51000);
        when(Config.mockWebServer.getHttpsPort()).thenReturn(52000);
        when(Config.mockWebServer.getStatusPath()).thenReturn(new Path("/statusPath"));
        when(Config.mockWebServer.getHttpConfigFile()).thenReturn(new Path("d:/some-dir/httpd.conf"));
        when(Config.mockWebServer.getSvrRoot()).thenReturn(new Path("./"));
        when(Config.mockWebServer.getDocRoot()).thenReturn(new Path("htdocs"));


        when(Config.mockWebServer2.getId()).thenReturn(new Identifier<WebServer>(2L));
        when(Config.mockWebServer2.getName()).thenReturn("the-ws-name-2");
        when(Config.mockWebServer2.getHost()).thenReturn("the-ws-hostname");
        when(Config.mockWebServer2.getGroups()).thenReturn(groups2);
        when(Config.mockWebServer2.getPort()).thenReturn(51000);
        when(Config.mockWebServer2.getHttpsPort()).thenReturn(52000);
        when(Config.mockWebServer2.getStatusPath()).thenReturn(new Path("/statusPath"));
        when(Config.mockWebServer2.getHttpConfigFile()).thenReturn(new Path("d:/some-dir/httpd.conf"));
        when(Config.mockWebServer2.getSvrRoot()).thenReturn(new Path("./"));
        when(Config.mockWebServer2.getDocRoot()).thenReturn(new Path("htdocs"));

        mockWebServersAll.add(mockWebServer);
        mockWebServersAll.add(mockWebServer2);

        mockWebServers11.add(mockWebServer);
        mockWebServers12.add(mockWebServer2);

        resourceGroup = new ResourceGroup(new ArrayList<>(groups));

        reset(Config.mockJvmPersistenceService, Config.mockBinaryDistributionLockManager, Config.mockInMemService, Config.resourceService, Config.mockWebServerPersistenceService);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test() {
        when(Config.mockWebServerPersistenceService.getWebServer(any(Identifier.class))).thenReturn(Config.mockWebServer);
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

        when(Config.mockWebServer.getState()).thenReturn(WebServerReachableState.WS_NEW);
        when(Config.mockWebServerPersistenceService.createWebServer(any(WebServer.class), anyString())).thenReturn(Config.mockWebServer);
        when(Config.mockInMemService.get(any(Identifier.class))).thenReturn(WebServerReachableState.WS_NEW);
        CreateWebServerRequest cmd = new CreateWebServerRequest(Config.mockWebServer.getGroupIds(),
                                                                Config.mockWebServer.getName(),
                                                                Config.mockWebServer.getHost(),
                                                                Config.mockWebServer.getPort(),
                                                                Config.mockWebServer.getHttpsPort(),
                                                                Config.mockWebServer.getStatusPath(),
                                                                Config.mockWebServer.getSvrRoot(),
                                                                Config.mockWebServer.getDocRoot(),
                                                                Config.mockWebServer.getState(),
                                                                Config.mockWebServer.getErrorStatus());
        final WebServer webServer = wsService.createWebServer(cmd, testUser);

        assertEquals(new Identifier<WebServer>(1L), webServer.getId());
        assertEquals(group.getId(), webServer.getGroups().iterator().next().getId());
        assertEquals("the-ws-name", webServer.getName());
        assertEquals("the-ws-group-name", webServer.getGroups().iterator().next().getName());
        assertEquals("the-ws-hostname", webServer.getHost());
        assertEquals(WebServerReachableState.WS_NEW, Config.mockInMemService.get(webServer.getId()));

        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test(expected = WebServerServiceException.class)
    public void testCreateWebServerValidateJvmName() {
        final Jvm jvm = new Jvm(new Identifier<Jvm>(99L), "testJvm", new HashSet<Group>());

        when(Config.mockWebServer.getState()).thenReturn(WebServerReachableState.WS_NEW);
        when(Config.mockWebServerPersistenceService.createWebServer(any(WebServer.class), anyString())).thenReturn(mockWebServer);
        when(Config.mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(jvm);
        CreateWebServerRequest cmd = new CreateWebServerRequest(Config.mockWebServer.getGroupIds(),
                                                                Config.mockWebServer.getName(),
                                                                Config.mockWebServer.getHost(),
                                                                Config.mockWebServer.getPort(),
                                                                Config.mockWebServer.getHttpsPort(),
                                                                Config.mockWebServer.getStatusPath(),
                                                                Config.mockWebServer.getSvrRoot(),
                                                                Config.mockWebServer.getDocRoot(),
                                                                Config.mockWebServer.getState(),
                                                                Config.mockWebServer.getErrorStatus());
        final WebServer webServer = wsService.createWebServer(cmd, testUser);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateWebServers() {
        when(Config.mockWebServerPersistenceService.getWebServer(any(Identifier.class))).thenReturn(Config.mockWebServer2);
        when(Config.mockWebServerPersistenceService.updateWebServer(any(WebServer.class), anyString())).thenReturn(Config.mockWebServer2);
        when(Config.mockWebServer2.getHttpConfigFile()).thenReturn(Config.mockPath);
        when(Config.mockPath.getUriPath()).thenReturn("d:/some-dir/httpd.conf");

        UpdateWebServerRequest cmd = new UpdateWebServerRequest(Config.mockWebServer2.getId(),
                                                                groupIds2,
                                                                Config.mockWebServer2.getName(),
                                                                Config.mockWebServer2.getHost(),
                                                                Config.mockWebServer2.getPort(),
                                                                Config.mockWebServer2.getHttpsPort(),
                                                                Config.mockWebServer2.getStatusPath(),
                                                                Config.mockWebServer2.getHttpConfigFile(),
                                                                Config.mockWebServer2.getSvrRoot(),
                                                                Config.mockWebServer2.getDocRoot(),
                                                                Config.mockWebServer2.getState(),
                                                                Config.mockWebServer2.getErrorStatus());
        final WebServer webServer = wsService.updateWebServer(cmd, testUser);

        assertEquals(new Identifier<WebServer>(2L), webServer.getId());
        assertEquals(group2.getId(), webServer.getGroups().iterator().next().getId());
        assertEquals("the-ws-name-2", webServer.getName());
        assertEquals(group2.getName(), webServer.getGroups().iterator().next().getName());
        assertEquals("the-ws-hostname", webServer.getHost());
        assertEquals("d:/some-dir/httpd.conf", webServer.getHttpConfigFile().getUriPath());
    }

    @Test(expected = WebServerServiceException.class)
    public void testUpdateWebServerShouldValidateJvmName() {
        final Jvm jvm = new Jvm(new Identifier<Jvm>(99L), "testJvm", new HashSet<Group>());
        when(Config.mockWebServerPersistenceService.getWebServer(any(Identifier.class))).thenReturn(mockWebServer2);
        when(Config.mockWebServerPersistenceService.updateWebServer(any(WebServer.class), anyString())).thenReturn(mockWebServer2);
        when(Config.mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(jvm);

        UpdateWebServerRequest cmd = new UpdateWebServerRequest(Config.mockWebServer2.getId(),
                                                                groupIds2,
                                                                Config.mockWebServer2.getName(),
                                                                Config.mockWebServer2.getHost(),
                                                                Config.mockWebServer2.getPort(),
                                                                Config.mockWebServer2.getHttpsPort(),
                                                                Config.mockWebServer2.getStatusPath(),
                                                                Config.mockWebServer2.getHttpConfigFile(),
                                                                Config.mockWebServer2.getSvrRoot(),
                                                                Config.mockWebServer2.getDocRoot(),
                                                                Config.mockWebServer2.getState(),
                                                                Config.mockWebServer2.getErrorStatus());
        final WebServer webServer = wsService.updateWebServer(cmd, testUser);

        assertEquals(new Identifier<WebServer>(2L), webServer.getId());
        assertEquals(group2.getId(), webServer.getGroups().iterator().next().getId());
        assertEquals("the-ws-name-2", webServer.getName());
        assertEquals(group2.getName(), webServer.getGroups().iterator().next().getName());
        assertEquals("the-ws-hostname", webServer.getHost());
        assertEquals("d:/some-dir/httpd.conf", webServer.getHttpConfigFile().getUriPath());
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
        when(Config.mockWebServer.getName()).thenReturn("mockWebServer");
        when(Config.mockWebServerPersistenceService.findWebServerByName(eq("aWebServer"))).thenReturn(Config.mockWebServer);
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
        when(Config.mockWebServer.getName()).thenReturn("mockWebServer");
        when(Config.mockWebServerPersistenceService.getResourceTemplate(anyString(), anyString())).thenReturn("<template>${webServer.name}</template>");
        when(Config.mockWebServerPersistenceService.findWebServerByName(anyString())).thenReturn(mockWebServer);
        wsService.getResourceTemplate("any", "httpd.conf", true, new ResourceGroup());
        verify(Config.resourceService).generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), any(WebServer.class), any(ResourceGeneratorType.class));
    }

    @Test
    public void testNonHttpdConfGetResourceTemplateTokensReplaced() {
        final WebServer mockWebServer = mock(WebServer.class);
        when(Config.mockWebServer.getName()).thenReturn("mockWebServer");
        when(Config.mockWebServerPersistenceService.getResourceTemplate(anyString(), anyString())).thenReturn("<template>${webServer.name}</template>");
        when(Config.mockWebServerPersistenceService.findWebServerByName(anyString())).thenReturn(mockWebServer);
        wsService.getResourceTemplate("any", "any-except-httpd.conf", true, new ResourceGroup());
        verify(Config.resourceService).generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), any(WebServer.class), any(ResourceGeneratorType.class));
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
        when(Config.resourceService.getTokenizedMetaData(anyString(), Matchers.anyObject(), anyString())).thenReturn(mockMetaData);
        wsService.uploadWebServerConfig(Config.mockWebServer, "httpd.conf", "test content", metaData, group.getName(), testUser);
        verify(Config.resourceService).createResource(any(ResourceIdentifier.class), any(ResourceTemplateMetaData.class), any(InputStream.class));
    }

    @Test
    public void testPreviewResourceTemplate() {
        List<Application> appList = new ArrayList<>();
        List<Jvm> jvmList = new ArrayList<>();
        when(Config.mockWebServerPersistenceService.findWebServerByName(anyString())).thenReturn(mockWebServer);
        when(Config.mockWebServerPersistenceService.findJvms(anyString())).thenReturn(jvmList);
        when(Config.mockWebServerPersistenceService.findApplications(anyString())).thenReturn(appList);
        wsService.previewResourceTemplate("myFile", "wsName", "groupName", "my template");
        verify(Config.resourceService).generateResourceFile(eq("myFile"), eq("my template"), any(ResourceGroup.class), any(WebServer.class), any(ResourceGeneratorType.class));
    }

    @Test
    public void testIsStarted() {
        when(Config.mockWebServer.getState()).thenReturn(WebServerReachableState.WS_REACHABLE);
        assertTrue(wsService.isStarted(Config.mockWebServer));
    }

    @Test
    public void testUpdateErrorStatus() {
        wsService.updateErrorStatus(Config.mockWebServer.getId(), "test update error status");
        verify(Config.mockWebServerPersistenceService).updateErrorStatus(new Identifier<WebServer>(1L), "test update error status");
    }

    @Test
    public void testUpdateState() {
        when(Config.mockInMemService.get(any(Identifier.class))).thenReturn(WebServerReachableState.WS_REACHABLE);
        wsService.updateState(Config.mockWebServer.getId(), WebServerReachableState.WS_REACHABLE, "");
        verify(Config.mockWebServerPersistenceService).updateState(new Identifier<WebServer>(1L), WebServerReachableState.WS_REACHABLE, "");
        assertEquals(WebServerReachableState.WS_REACHABLE, Config.mockInMemService.get(Config.mockWebServer.getId()));
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

    @Test(expected = WebServerServiceException.class)
    public void testGenerateInstallServiceWSBat() {
        when(Config.resourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), eq(mockWebServer), any(ResourceGeneratorType.class))).thenThrow(IOException.class);
        wsService.generateInstallServiceScript(mockWebServer);
    }

    @Test
    public void testGetWebServerPropogationNew() {
        List<WebServer> webServers = new ArrayList<>();
        when(Config.mockWebServerPersistenceService.getWebServers()).thenReturn(webServers);
        assertEquals(webServers, wsService.getWebServersPropagationNew());
    }

    @Test(expected = InternalErrorException.class)
    public void testUploadWebServerConfigFail() throws IOException {
        UploadWebServerTemplateRequest request = mock(UploadWebServerTemplateRequest.class);
        final String metaData = "\"deployPath\":\"d:/httpd-data\",\"deployFileName\":\"httpd.conf\"}";
        when(request.getMetaData()).thenReturn(metaData);
        when(request.getWebServer()).thenReturn(mockWebServer);
        when(Config.mockWebServer.getName()).thenReturn("testWebServer");
        when(request.getConfFileName()).thenReturn("httpd.conf");
        when(Config.resourceService.getTokenizedMetaData(anyString(), Matchers.anyObject(), anyString())).thenThrow(new IOException("FAIL upload config because of meta data mapping"));
        wsService.uploadWebServerConfig(mockWebServer, "httpd.conf", "test content", metaData, group.getName(), testUser);
    }

    private String removeCarriageReturnsAndNewLines(String s) {
        return s.replaceAll("\\r", "").replaceAll("\\n", "");
    }

    @Configuration
    static class Config {

        // Was initially using @Mock but an intermittent problem with testPerformDiagnosis happens with the said mocking approach.
        // Sometimes pingAndUpdateState of JvmServiceImpl throws a NPE at
        // jvmStateService.updateState(jvm.getId(), JvmState.JVM_STOPPED, StringUtils.EMPTY);
        // even though the mocks were initialized.
        static JvmPersistenceService mockJvmPersistenceService = mock(JvmPersistenceService.class);

        static WebServerPersistenceService mockWebServerPersistenceService = mock(WebServerPersistenceService.class);

        static ResourceService resourceService = mock(ResourceService.class);

        static InMemoryStateManagerService mockInMemService = mock(InMemoryStateManagerService.class);

        static BinaryDistributionLockManager mockBinaryDistributionLockManager = mock(BinaryDistributionLockManager.class);

        static HttpComponentsClientHttpRequestFactory mockHttpClientFactory = mock(HttpComponentsClientHttpRequestFactory.class);

        static ClientFactoryHelper mockClientFactoryHelper = mock(ClientFactoryHelper.class);

        static WebServerStateSetterWorker mockWebServerStateSetterWorker = mock(WebServerStateSetterWorker.class);

        static WebServerControlService mockWebServerControlService = mock(WebServerControlService.class);

        static WebServerCommandService mockWebServerCommandService = mock(WebServerCommandService.class);

        static WebServer mockWebServer = mock(WebServer.class);

        static WebServer mockWebServer2 = mock(WebServer.class);

        static Path mockPath = mock(Path.class);

        @Bean
        public WebServer getWebServer() {
            return mockWebServer;
        }

        @Bean
        public ClientFactoryHelper getClientFactoryHelper() {
            return mockClientFactoryHelper;
        }

        @Bean
        public WebServerStateSetterWorker getWebServerStateSetterWorker() {
            return mockWebServerStateSetterWorker;
        }

        @Bean
        public WebServerControlService getWebServerControlService() {
            return mockWebServerControlService;
        }

        @Bean
        public WebServerCommandService getWebServerCommandService() {
            return mockWebServerCommandService;
        }

        @Bean
        public JvmPersistenceService getMockJvmPersistenceService() {
            return mockJvmPersistenceService;
        }

        @Bean
        public WebServerPersistenceService getWebServerPersistenceService() {
            return mockWebServerPersistenceService;
        }


        @Bean(name = "httpRequestFactory")
        public static HttpComponentsClientHttpRequestFactory getMockHttpClientFactory() {
            return mockHttpClientFactory;
        }

        @Bean
        public WebServerService getWebServerService() {
            return new WebServerServiceImpl(mockWebServerPersistenceService, resourceService, mockInMemService, StringUtils.EMPTY, mockBinaryDistributionLockManager);
        }


    }

}
