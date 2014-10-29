package com.siemens.cto.aem.service.webserver.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.path.FileSystemPath;
import com.siemens.cto.aem.domain.model.path.Path;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.CreateWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.UpdateWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.persistence.dao.webserver.WebServerDao;
import com.siemens.cto.toc.files.RepositoryAction;
import com.siemens.cto.toc.files.TemplateManager;
import com.siemens.cto.toc.files.TocFile;

/**
 * Created by z0031wps on 4/2/2014.
 */
@RunWith(MockitoJUnitRunner.class)
public class WebServerServiceImplTest {

    @Mock
    private WebServerDao wsDao;

    private WebServerServiceImpl wsService;

    @Mock
    private WebServer mockWebServer;
    @Mock
    private WebServer mockWebServer2;

    @Mock
    private TemplateManager templateManager;

    @Mock
    private RepositoryAction repositoryAction;

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

    @Before
    public void setUp() throws IOException{

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

        when(mockWebServer.getId()).thenReturn(new Identifier<WebServer>(1L));
        when(mockWebServer.getName()).thenReturn("the-ws-name");
        when(mockWebServer.getHost()).thenReturn("the-ws-hostname");
        when(mockWebServer.getGroups()).thenReturn(groups);
        when(mockWebServer.getPort()).thenReturn(51000);
        when(mockWebServer.getHttpsPort()).thenReturn(52000);
        when(mockWebServer.getStatusPath()).thenReturn(new Path("/statusPath"));
        when(mockWebServer.getHttpConfigFile()).thenReturn(new FileSystemPath("d:/some-dir/httpd.conf"));


        when(mockWebServer2.getId()).thenReturn(new Identifier<WebServer>(2L));
        when(mockWebServer2.getName()).thenReturn("the-ws-name-2");
        when(mockWebServer2.getHost()).thenReturn("the-ws-hostname");
        when(mockWebServer2.getGroups()).thenReturn(groups2);
        when(mockWebServer2.getPort()).thenReturn(51000);
        when(mockWebServer2.getHttpsPort()).thenReturn(52000);
        when(mockWebServer2.getStatusPath()).thenReturn(new Path("/statusPath"));
        when(mockWebServer2.getHttpConfigFile()).thenReturn(new FileSystemPath("d:/some-dir/httpd.conf"));

        mockWebServersAll.add(mockWebServer);
        mockWebServersAll.add(mockWebServer2);

        mockWebServers11.add(mockWebServer);
        mockWebServers12.add(mockWebServer2);

        wsService = new WebServerServiceImpl(wsDao, templateManager);

        when(repositoryAction.getType()).thenReturn(RepositoryAction.Type.NONE);
        when(templateManager.getAbsoluteLocation(any(TocFile.class))).thenAnswer(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                TocFile file = (TocFile)invocation.getArguments()[0];
                return "/" + file.getFileName();
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test() {
        when(wsDao.getWebServer(any(Identifier.class))).thenReturn(mockWebServer);
        final WebServer webServer= wsService.getWebServer(new Identifier<WebServer>(1L));
        assertEquals(new Identifier<WebServer>(1L), webServer.getId());
        assertEquals(group.getId(), webServer.getGroups().iterator().next().getId());
        assertEquals("the-ws-name", webServer.getName());
        assertEquals("the-ws-group-name", webServer.getGroups().iterator().next().getName());
        assertEquals("the-ws-hostname", webServer.getHost());
    }

    @Test
    public void testGetWebServers() {
        when(wsDao.getWebServers(any(PaginationParameter.class))).thenReturn(mockWebServersAll);
        final List<WebServer> webServers= wsService.getWebServers(PaginationParameter.all());
        assertEquals(2, webServers.size());
    }

    @Test
    public void testFindWebServersBelongingTo() {
        when(wsDao.findWebServersBelongingTo(eq(new Identifier<Group>(1L)), any(PaginationParameter.class))).thenReturn(mockWebServers11);
        when(wsDao.findWebServersBelongingTo(eq(new Identifier<Group>(2L)), any(PaginationParameter.class))).thenReturn(mockWebServers12);

        final List<WebServer> webServers= wsService.findWebServers(group.getId(), PaginationParameter.all());
        final List<WebServer> webServers2 = wsService.findWebServers(group2.getId(), PaginationParameter.all());

        assertEquals(1, webServers.size());
        assertEquals(1, webServers2.size());

        verify(wsDao, times(1)).findWebServersBelongingTo(eq(group.getId()), any(PaginationParameter.class));
        verify(wsDao, times(1)).findWebServersBelongingTo(eq(group2.getId()), any(PaginationParameter.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateWebServers() {
        when(wsDao.createWebServer(any(Event.class))).thenReturn(mockWebServer);
        CreateWebServerCommand cmd = new CreateWebServerCommand(mockWebServer.getGroupIds(),
                                                                mockWebServer.getName(),
                                                                mockWebServer.getHost(),
                                                                mockWebServer.getPort(),
                                                                mockWebServer.getHttpsPort(),
                                                                mockWebServer.getStatusPath(),
                                                                mockWebServer.getHttpConfigFile());
        final WebServer webServer = wsService.createWebServer(cmd, testUser);

        assertEquals(new Identifier<WebServer>(1L), webServer.getId());
        assertEquals(group.getId(), webServer.getGroups().iterator().next().getId());
        assertEquals("the-ws-name", webServer.getName());
        assertEquals("the-ws-group-name", webServer.getGroups().iterator().next().getName());
        assertEquals("the-ws-hostname", webServer.getHost());
    }

    @Test
    public void testDeleteWebServers() {
        when(wsDao.getWebServers(any(PaginationParameter.class))).thenReturn(mockWebServersAll);
        wsService.removeWebServer(mockWebServer.getId());
        verify(wsDao, atLeastOnce()).removeWebServer(mockWebServer.getId());
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateWebServers() {
        when(wsDao.updateWebServer(any(Event.class))).thenReturn(mockWebServer2);

        UpdateWebServerCommand cmd = new UpdateWebServerCommand(mockWebServer2.getId(), groupIds2,
                                                                mockWebServer2.getName(),
                                                                mockWebServer2.getHost(),
                                                                mockWebServer2.getPort(),
                                                                mockWebServer2.getHttpsPort(),
                                                                mockWebServer2.getStatusPath(),
                                                                mockWebServer2.getHttpConfigFile());
        final WebServer webServer = wsService.updateWebServer(cmd, testUser);

        assertEquals(new Identifier<WebServer>(2L), webServer.getId());
        assertEquals(group2.getId(), webServer.getGroups().iterator().next().getId());
        assertEquals("the-ws-name-2", webServer.getName());
        assertEquals(group2.getName(), webServer.getGroups().iterator().next().getName());
        assertEquals("the-ws-hostname", webServer.getHost());
        assertEquals("d:/some-dir/httpd.conf", webServer.getHttpConfigFile().getUriPath());
    }

    @Test
    public void testFindWebServersNameFragment() {
        when(wsDao.findWebServers(eq("the-ws-name-2"), any(PaginationParameter.class))).thenReturn(mockWebServers12);
        when(wsDao.findWebServers(eq("the-ws-name"), any(PaginationParameter.class))).thenReturn(mockWebServersAll);

        final List<WebServer> webServers= wsService.findWebServers("the-ws-name-2", PaginationParameter.all());
        final List<WebServer> webServers2 = wsService.findWebServers("the-ws-name", PaginationParameter.all());

        assertEquals(1, webServers.size());
        assertEquals(2, webServers2.size());

        verify(wsDao, times(1)).findWebServers(eq("the-ws-name-2"), any(PaginationParameter.class));
        verify(wsDao, times(1)).findWebServers(eq("the-ws-name"), any(PaginationParameter.class));
    }

    @Test
    public void testRemoveWebServersBelongingTo() {
        wsService.removeWebServersBelongingTo(mockWebServer.getGroups().iterator().next().getId());
        verify(wsDao, atLeastOnce()).removeWebServersBelongingTo(group.getId());
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
    public void testGenerateHttpdConfig() throws IOException {
        Application app1 = new Application(null, "hello-world-1", null, "/hello-world-1", null);
        Application app2 = new Application(null, "hello-world-2", null, "/hello-world-2", null);

        Application [] appArray = {app1, app2};

        when(wsDao.findApplications(anyString(), any(PaginationParameter.class))).thenReturn(Arrays.asList(appArray));

        String generatedHttpdConf = wsService.generateHttpdConfig("Apache2.4", null);

        assertEquals(removeCarriageReturnsAndNewLines(readReferenceFile("/httpd.conf")),
                     removeCarriageReturnsAndNewLines(generatedHttpdConf));
    }

    @Test
    public void testGenerateHttpdConfigWithSsl() throws IOException {
        Application app1 = new Application(null, "hello-world-1", null, "/hello-world-1", null);
        Application app2 = new Application(null, "hello-world-2", null, "/hello-world-2", null);

        Application [] appArray = {app1, app2};

        when(wsDao.findApplications(anyString(), any(PaginationParameter.class))).thenReturn(Arrays.asList(appArray));

        String generatedHttpdConf = wsService.generateHttpdConfig("Apache2.4", true);

        assertEquals(removeCarriageReturnsAndNewLines(readReferenceFile("/httpd-ssl.conf")),
                     removeCarriageReturnsAndNewLines(generatedHttpdConf));
    }

    @Test
    public void testGenerateWorkerProperties() throws IOException {
        final Jvm jvm1 = mock(Jvm.class);
        when(jvm1.getJvmName()).thenReturn("tc1");
        when(jvm1.getHostName()).thenReturn("host1");
        when(jvm1.getAjpPort()).thenReturn(8009);

        final Jvm jvm2 = mock(Jvm.class);
        when(jvm2.getJvmName()).thenReturn("tc2");
        when(jvm2.getHostName()).thenReturn("host2");
        when(jvm2.getAjpPort()).thenReturn(8109);

        final List<Jvm> jvms = new ArrayList<>();
        jvms.add(jvm1);
        jvms.add(jvm2);

        when(wsDao.findJvms(anyString(), any(PaginationParameter.class))).thenReturn(jvms);

        final Application app1 = mock(Application.class);
            when(app1.getName()).thenReturn("hello-world-1");

        final Application app2 = mock(Application.class);
        when(app2.getName()).thenReturn("hello-world-2");

        final Application app3 = mock(Application.class);
        when(app3.getName()).thenReturn("hello-world-3");

        final List<Application> apps = new ArrayList<>();
        apps.add(app1);
        apps.add(app2);
        apps.add(app3);

        when(wsDao.findApplications(anyString(), any(PaginationParameter.class))).thenReturn(apps);

        String workerPropertiesStr = wsService.generateWorkerProperties("Apache2.4");

        assertEquals(removeCarriageReturnsAndNewLines(readReferenceFile("/workers.properties")),
                     removeCarriageReturnsAndNewLines(workerPropertiesStr));
    }

    private String removeCarriageReturnsAndNewLines(String s) {
        return s.replaceAll("\\r", "").replaceAll("\\n", "");
    }
}