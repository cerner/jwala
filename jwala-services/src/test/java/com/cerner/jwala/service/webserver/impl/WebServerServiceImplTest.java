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
import com.cerner.jwala.service.state.impl.InMemoryStateManagerServiceImpl;
import com.cerner.jwala.service.webserver.exception.WebServerServiceException;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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

/**
 * Created by Arvindo Kinny on 4/2/2014.
 */
@RunWith(MockitoJUnitRunner.class)
public class WebServerServiceImplTest {

    @Mock
    private WebServerPersistenceService webServerPersistenceService;

    @Mock
    private JvmPersistenceService jvmPersistenceService;

    private WebServerServiceImpl wsService;

    @Mock
    private WebServer mockWebServer;
    @Mock
    private WebServer mockWebServer2;

    private BinaryDistributionLockManager binaryDistributionLockManager;

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
    private ResourceService resourceService;
    private ResourceGroup resourceGroup;
    private InMemoryStateManagerService<Identifier<WebServer>, WebServerReachableState> inMemService = new InMemoryStateManagerServiceImpl<>();


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
        resourceService = mock(ResourceService.class);

        when(resourceService.generateResourceGroup()).thenReturn(new ResourceGroup(new ArrayList<Group>()));

        when(mockWebServer.getId()).thenReturn(new Identifier<WebServer>(1L));
        when(mockWebServer.getName()).thenReturn("the-ws-name");
        when(mockWebServer.getHost()).thenReturn("the-ws-hostname");
        when(mockWebServer.getGroups()).thenReturn(groups);
        when(mockWebServer.getGroupIds()).thenReturn(groupIds);
        when(mockWebServer.getPort()).thenReturn(51000);
        when(mockWebServer.getHttpsPort()).thenReturn(52000);
        when(mockWebServer.getStatusPath()).thenReturn(new Path("/statusPath"));
        when(mockWebServer.getHttpConfigFile()).thenReturn(new Path("d:/some-dir/httpd.conf"));
        when(mockWebServer.getSvrRoot()).thenReturn(new Path("./"));
        when(mockWebServer.getDocRoot()).thenReturn(new Path("htdocs"));


        when(mockWebServer2.getId()).thenReturn(new Identifier<WebServer>(2L));
        when(mockWebServer2.getName()).thenReturn("the-ws-name-2");
        when(mockWebServer2.getHost()).thenReturn("the-ws-hostname");
        when(mockWebServer2.getGroups()).thenReturn(groups2);
        when(mockWebServer2.getPort()).thenReturn(51000);
        when(mockWebServer2.getHttpsPort()).thenReturn(52000);
        when(mockWebServer2.getStatusPath()).thenReturn(new Path("/statusPath"));
        when(mockWebServer2.getHttpConfigFile()).thenReturn(new Path("d:/some-dir/httpd.conf"));
        when(mockWebServer2.getSvrRoot()).thenReturn(new Path("./"));
        when(mockWebServer2.getDocRoot()).thenReturn(new Path("htdocs"));

        mockWebServersAll.add(mockWebServer);
        mockWebServersAll.add(mockWebServer2);

        mockWebServers11.add(mockWebServer);
        mockWebServers12.add(mockWebServer2);

        wsService = new WebServerServiceImpl(webServerPersistenceService, resourceService, inMemService, StringUtils.EMPTY, binaryDistributionLockManager, jvmPersistenceService);

        resourceGroup = new ResourceGroup(new ArrayList<>(groups));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test() {
        when(webServerPersistenceService.getWebServer(any(Identifier.class))).thenReturn(mockWebServer);
        final WebServer webServer = wsService.getWebServer(new Identifier<WebServer>(1L));
        assertEquals(new Identifier<WebServer>(1L), webServer.getId());
        assertEquals(group.getId(), webServer.getGroups().iterator().next().getId());
        assertEquals("the-ws-name", webServer.getName());
        assertEquals("the-ws-group-name", webServer.getGroups().iterator().next().getName());
        assertEquals("the-ws-hostname", webServer.getHost());
    }

    @Test
    public void testGetWebServers() {
        when(webServerPersistenceService.getWebServers()).thenReturn(mockWebServersAll);
        final List<WebServer> webServers = wsService.getWebServers();
        assertEquals(2, webServers.size());
    }

    @Test
    public void testFindWebServersBelongingTo() {
        when(webServerPersistenceService.findWebServersBelongingTo(eq(new Identifier<Group>(1L)))).thenReturn(mockWebServers11);
        when(webServerPersistenceService.findWebServersBelongingTo(eq(new Identifier<Group>(2L)))).thenReturn(mockWebServers12);

        final List<WebServer> webServers = wsService.findWebServers(group.getId());
        final List<WebServer> webServers2 = wsService.findWebServers(group2.getId());

        assertEquals(1, webServers.size());
        assertEquals(1, webServers2.size());

        verify(webServerPersistenceService, times(1)).findWebServersBelongingTo(eq(group.getId()));
        verify(webServerPersistenceService, times(1)).findWebServersBelongingTo(eq(group2.getId()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateWebServers() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");

        when(mockWebServer.getState()).thenReturn(WebServerReachableState.WS_NEW);
        when(webServerPersistenceService.createWebServer(any(WebServer.class), anyString())).thenReturn(mockWebServer);
        CreateWebServerRequest cmd = new CreateWebServerRequest(mockWebServer.getGroupIds(),
                                                                mockWebServer.getName(),
                                                                mockWebServer.getHost(),
                                                                mockWebServer.getPort(),
                                                                mockWebServer.getHttpsPort(),
                                                                mockWebServer.getStatusPath(),
                                                                mockWebServer.getSvrRoot(),
                                                                mockWebServer.getDocRoot(),
                                                                mockWebServer.getState(),
                                                                mockWebServer.getErrorStatus());
        final WebServer webServer = wsService.createWebServer(cmd, testUser);

        assertEquals(new Identifier<WebServer>(1L), webServer.getId());
        assertEquals(group.getId(), webServer.getGroups().iterator().next().getId());
        assertEquals("the-ws-name", webServer.getName());
        assertEquals("the-ws-group-name", webServer.getGroups().iterator().next().getName());
        assertEquals("the-ws-hostname", webServer.getHost());
        assertEquals(WebServerReachableState.WS_NEW, inMemService.get(webServer.getId()));

        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testDeleteWebServers() {
        when(webServerPersistenceService.getWebServers()).thenReturn(mockWebServersAll);
        wsService.removeWebServer(mockWebServer.getId());
        verify(webServerPersistenceService, atLeastOnce()).removeWebServer(mockWebServer.getId());
        assertNull(inMemService.get(mockWebServer.getId()));
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateWebServers() {
        when(webServerPersistenceService.getWebServer(any(Identifier.class))).thenReturn(mockWebServer2);
        when(webServerPersistenceService.updateWebServer(any(WebServer.class), anyString())).thenReturn(mockWebServer2);

        UpdateWebServerRequest cmd = new UpdateWebServerRequest(mockWebServer2.getId(),
                                                                groupIds2,
                                                                mockWebServer2.getName(),
                                                                mockWebServer2.getHost(),
                                                                mockWebServer2.getPort(),
                                                                mockWebServer2.getHttpsPort(),
                                                                mockWebServer2.getStatusPath(),
                                                                mockWebServer2.getHttpConfigFile(),
                                                                mockWebServer2.getSvrRoot(),
                                                                mockWebServer2.getDocRoot(),
                                                                mockWebServer2.getState(),
                                                                mockWebServer2.getErrorStatus());
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
        when(mockWebServer.getName()).thenReturn("mockWebServer");
        when(webServerPersistenceService.findWebServerByName(eq("aWebServer"))).thenReturn(mockWebServer);
        assertEquals("mockWebServer", wsService.getWebServer("aWebServer").getName());
    }

    @Test
    public void testGetResourceTemplateNames() {
        final String[] nameArray = {"httpd.conf"};
        when(webServerPersistenceService.getResourceTemplateNames(eq("Apache2.4"))).thenReturn(Arrays.asList(nameArray));
        final List names = wsService.getResourceTemplateNames("Apache2.4");
        assertEquals("httpd.conf", names.get(0));
    }

    @Test
    public void testGetResourceTemplate() {
        when(webServerPersistenceService.getResourceTemplate(anyString(), anyString())).thenReturn("<template/>");
        assertEquals("<template/>", wsService.getResourceTemplate("any", "any", false, new ResourceGroup()));
    }

    @Test
    public void testGetResourceTemplateTokensReplaced() {
        final WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServer.getName()).thenReturn("mockWebServer");
        when(webServerPersistenceService.getResourceTemplate(anyString(), anyString())).thenReturn("<template>${webServer.name}</template>");
        when(webServerPersistenceService.findWebServerByName(anyString())).thenReturn(mockWebServer);
        wsService.getResourceTemplate("any", "httpd.conf", true, new ResourceGroup());
        verify(resourceService).generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), any(WebServer.class), any(ResourceGeneratorType.class));
    }

    @Test
    public void testNonHttpdConfGetResourceTemplateTokensReplaced() {
        final WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServer.getName()).thenReturn("mockWebServer");
        when(webServerPersistenceService.getResourceTemplate(anyString(), anyString())).thenReturn("<template>${webServer.name}</template>");
        when(webServerPersistenceService.findWebServerByName(anyString())).thenReturn(mockWebServer);
        wsService.getResourceTemplate("any", "any-except-httpd.conf", true, new ResourceGroup());
        verify(resourceService).generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), any(WebServer.class), any(ResourceGeneratorType.class));
    }

    @Test
    public void testUpdateResourceTemplate() {
        when(webServerPersistenceService.getResourceTemplate("wsName", "resourceTemplateName")).thenReturn("template");
        assertEquals("template", wsService.updateResourceTemplate("wsName", "resourceTemplateName", "template"));
        verify(webServerPersistenceService).updateResourceTemplate(eq("wsName"), eq("resourceTemplateName"), eq("template"));
    }

    @Test
    public void testUploadWebServerConfig() throws IOException {
        UploadWebServerTemplateRequest request = mock(UploadWebServerTemplateRequest.class);
        final String metaData = "{\"deployPath\":\"d:/httpd-data\",\"deployFileName\":\"httpd.conf\"}";
        when(request.getMetaData()).thenReturn(metaData);
        ResourceTemplateMetaData mockMetaData = mock(ResourceTemplateMetaData.class);
        when(mockMetaData.getDeployPath()).thenReturn("d:/httpd-data");
        when(mockMetaData.getDeployFileName()).thenReturn("httpd.conf");
        when(resourceService.getTokenizedMetaData(anyString(), Matchers.anyObject(), anyString())).thenReturn(mockMetaData);
        wsService.uploadWebServerConfig(mockWebServer, "httpd.conf", "test content", metaData, group.getName(), testUser);
        verify(resourceService).createResource(any(ResourceIdentifier.class), any(ResourceTemplateMetaData.class), any(InputStream.class));
    }

    @Test
    public void testPreviewResourceTemplate() {
        List<Application> appList = new ArrayList<>();
        List<Jvm> jvmList = new ArrayList<>();
        when(webServerPersistenceService.findWebServerByName(anyString())).thenReturn(mockWebServer);
        when(webServerPersistenceService.findJvms(anyString())).thenReturn(jvmList);
        when(webServerPersistenceService.findApplications(anyString())).thenReturn(appList);
        wsService.previewResourceTemplate("myFile","wsName", "groupName", "my template");
        verify(resourceService).generateResourceFile(eq("myFile"), eq("my template"), any(ResourceGroup.class), any(WebServer.class), any(ResourceGeneratorType.class));
    }

    @Test
    public void testIsStarted() {
        when(mockWebServer.getState()).thenReturn(WebServerReachableState.WS_REACHABLE);
        assertTrue(wsService.isStarted(mockWebServer));
    }

    @Test
    public void testUpdateErrorStatus() {
        wsService.updateErrorStatus(mockWebServer.getId(), "test update error status");
        verify(webServerPersistenceService).updateErrorStatus(new Identifier<WebServer>(1L), "test update error status");
    }

    @Test
    public void testUpdateState() {
        wsService.updateState(mockWebServer.getId(), WebServerReachableState.WS_REACHABLE, "");
        verify(webServerPersistenceService).updateState(new Identifier<WebServer>(1L), WebServerReachableState.WS_REACHABLE, "");
        assertEquals(WebServerReachableState.WS_REACHABLE, inMemService.get(mockWebServer.getId()));
    }

    @Test
    public void testGetWebServerStartedCount() {
        final String groupName = "testGroup";
        final Long returnCount = 1L;
        when(webServerPersistenceService.getWebServerStartedCount(eq(groupName))).thenReturn(returnCount);
        assertEquals(returnCount, wsService.getWebServerStartedCount(groupName));
    }

    @Test
    public void testGetWebServerCount() {
        final String groupName = "testGroup";
        final Long returnCount = 1L;
        when(webServerPersistenceService.getWebServerCount(eq(groupName))).thenReturn(returnCount);
        assertEquals(returnCount, wsService.getWebServerCount(groupName));
    }

    @Test
    public void testGetWebServerStoppedCount() {
        final String groupName = "testGroup";
        final Long returnCount = 1L;
        when(webServerPersistenceService.getWebServerStoppedCount(eq(groupName))).thenReturn(returnCount);
        assertEquals(returnCount, wsService.getWebServerStoppedCount(groupName));
    }

    @Test
    public void testGetResourceTemplateMetaData() {
        final String wsName = "testWS";
        final String resourceTemplate = "resourceTemplateName";
        when(webServerPersistenceService.getResourceTemplateMetaData(eq(wsName), eq(resourceTemplate))).thenReturn("");
        assertEquals("", wsService.getResourceTemplateMetaData(wsName, resourceTemplate));
    }

    @Test (expected = WebServerServiceException.class)
    public void testGenerateInstallServiceWSBat() {
        when(resourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), eq(mockWebServer), any(ResourceGeneratorType.class))).thenThrow(IOException.class);
        wsService.generateInstallServiceScript(mockWebServer);
    }

    @Test
    public void testGetWebServerPropogationNew() {
        List<WebServer> webServers = new ArrayList<>();
        when(webServerPersistenceService.getWebServers()).thenReturn(webServers);
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
        when(resourceService.getTokenizedMetaData(anyString(), Matchers.anyObject(), anyString())).thenThrow(new IOException("FAIL upload config because of meta data mapping"));
        wsService.uploadWebServerConfig(mockWebServer, "httpd.conf", "test content", metaData, group.getName(), testUser);
    }

    private String removeCarriageReturnsAndNewLines(String s) {
        return s.replaceAll("\\r", "").replaceAll("\\n", "");
    }
}