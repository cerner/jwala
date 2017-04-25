package com.cerner.jwala.persistence.jpa.service;

import com.cerner.jwala.common.configuration.TestExecutionProfile;
import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.media.MediaType;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.request.app.CreateApplicationRequest;
import com.cerner.jwala.common.request.group.CreateGroupRequest;
import com.cerner.jwala.common.request.jvm.CreateJvmRequest;
import com.cerner.jwala.common.request.webserver.UploadWebServerTemplateRequest;
import com.cerner.jwala.dao.MediaDao;
import com.cerner.jwala.dao.impl.MediaDaoImpl;
import com.cerner.jwala.persistence.configuration.TestJpaConfiguration;
import com.cerner.jwala.persistence.jpa.domain.*;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.JpaWebServerConfigTemplate;
import com.cerner.jwala.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.cerner.jwala.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.cerner.jwala.persistence.jpa.service.impl.ApplicationCrudServiceImpl;
import com.cerner.jwala.persistence.jpa.service.impl.GroupCrudServiceImpl;
import com.cerner.jwala.persistence.jpa.service.impl.JvmCrudServiceImpl;
import com.cerner.jwala.persistence.jpa.service.impl.WebServerCrudServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import static org.junit.Assert.*;

/**
 * Integration test for {@link WebServerCrudServiceImpl}
 * <p/>
 * Created by Jedd Cuison on 12/16/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@EnableTransactionManagement
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
        classes = {WebServerCrudServiceImplTest.Config.class})
public class WebServerCrudServiceImplTest {

    public static final String HTTPD_CONF_META_DATA = "{\"deployFileName\":\"httpd.conf\",\"deployPath\":\"./deploy/here\"}";
    @Autowired
    private WebServerCrudService webServerCrudService;

    @Autowired
    private GroupCrudService groupCrudService;

    @Autowired
    private ApplicationCrudService applicationCrudService;

    @Autowired
    private JvmCrudService jvmCrudService;

    @Autowired
    private MediaDao mediaDao;


    @Before
    public void setup() {
        User user = new User("testUser");
    }

    @Test
    public void testCrud() {
        final WebServer newWebServer = new WebServer(null, new ArrayList<Group>(), "zWebServer", "zHost", 8080, 443,
                new Path("any"), WebServerReachableState.WS_UNREACHABLE, null);
        final WebServer createdWebServer = webServerCrudService.createWebServer(newWebServer, "me");
        assertTrue(createdWebServer.getId() != null);
        assertTrue(createdWebServer.getId().getId() != null);
        assertEquals(newWebServer.getName(), createdWebServer.getName());

        final WebServer editedWebServer = new WebServer(createdWebServer.getId(), new ArrayList<Group>(), "zWebServerx",
                "zHostx", 808, 44, new Path("anyx"), WebServerReachableState.WS_UNREACHABLE, null);
        final WebServer updatedWebServer = webServerCrudService.updateWebServer(editedWebServer, "me");
        assertEquals(editedWebServer.getId().getId(), updatedWebServer.getId().getId());

        // Test getWebServer
        WebServer gottenWebServer = webServerCrudService.getWebServer(editedWebServer.getId());
        assertEquals(editedWebServer.getName(), gottenWebServer.getName());

        // Test findWebServerByName
        gottenWebServer = webServerCrudService.findWebServerByName(editedWebServer.getName());
        assertEquals(editedWebServer.getName(), gottenWebServer.getName());

        // Test getWebServers
        assertEquals(1, webServerCrudService.getWebServers().size());

        // Test removeWebServer
        webServerCrudService.removeWebServer(editedWebServer.getId());
        assertEquals(0, webServerCrudService.getWebServers().size());
    }

    @Test
    public void removeWebServersBelongingToTest() {
        JpaGroup group = new JpaGroup();
        group.setName("zGroup");
        group = groupCrudService.create(group);
        final JpaWebServer webServer = new JpaWebServer();
        webServer.setName("zWebServer");
        webServer.setStatusPath("zStatusPath");
        webServer.getGroups().add(group);
        group.getWebServers().add(webServerCrudService.create(webServer));
        groupCrudService.update(group);
        assertEquals(1, webServerCrudService.getWebServer(new Identifier<WebServer>(webServer.getId())).getGroups().size());
        webServerCrudService.removeWebServersBelongingTo(new Identifier<Group>(group.getId()));
        assertEquals(0, webServerCrudService.getWebServers().size());
    }

    /*@Test
    public void testFindWebServers() {
        final List<WebServer> foundWebServers = webServerCrudService.findWebServers("toast");
        assertTrue(foundWebServers.size() == 0);
    }*/

    @Test
    public void testFindWebServersBelongingTo() {
        CreateGroupRequest createGroupReq = new CreateGroupRequest("testGroupName");
        JpaGroup group = groupCrudService.createGroup(createGroupReq);
        List<WebServer> webServersBelongingTo = webServerCrudService.findWebServersBelongingTo(new Identifier<Group>(group.getId()));
        assertTrue(webServersBelongingTo.size() == 0);

        WebServer webServer = new WebServer(new Identifier<WebServer>(1111L), new HashSet<Group>(), "testWebServer",
                "testHost", 101, 102, new Path("./statusPath"), WebServerReachableState.WS_UNREACHABLE, null);
        webServer = webServerCrudService.createWebServer(webServer, "testUser");
        List<JpaWebServer> wsList = new ArrayList<>();
        wsList.add(webServerCrudService.findById(webServer.getId().getId()));
        group.setWebServers(wsList);
        webServersBelongingTo = webServerCrudService.findWebServersBelongingTo(new Identifier<Group>(group.getId()));
        assertTrue(webServersBelongingTo.size() == 1);
    }

    @Test(expected = EntityExistsException.class)
    public void testCreateWebServerThrowsException() {
        WebServer webServer = new WebServer(new Identifier<WebServer>(1111L), new HashSet<Group>(), "testWebServer", "testHost",
                101, 102, new Path("./statusPath"), WebServerReachableState.WS_UNREACHABLE, null);
        webServerCrudService.createWebServer(webServer, "testUser");
        // causes problems
        webServerCrudService.createWebServer(webServer, "testUser");
    }

    @Test
    public void testFindApplications() {
        WebServer webServer = new WebServer(new Identifier<WebServer>(1111L), new HashSet<Group>(), "testWebServer", "testHost",
                101, 102, new Path("./statusPath"), WebServerReachableState.WS_UNREACHABLE, null);
        webServer = webServerCrudService.createWebServer(webServer, "testUser");
        List<Application> applications = webServerCrudService.findApplications("testWebServer");
        assertTrue(applications.size() == 0);

        CreateGroupRequest creatGroupReq = new CreateGroupRequest("testGroupName");
        JpaGroup group = groupCrudService.createGroup(creatGroupReq);
        CreateApplicationRequest createAppReq = new CreateApplicationRequest(new Identifier<Group>(group.getId()), "testAppName", "/context", true, true, false);
        JpaApplication application = applicationCrudService.createApplication(createAppReq, group);
        List<JpaWebServer> wsList = new ArrayList<>();
        wsList.add(webServerCrudService.findById(webServer.getId().getId()));
        group.setWebServers(wsList);
        CreateJvmRequest createJvmReq = new CreateJvmRequest("testJvmName", "testHostName", 1212, 1213, 1214, -1, 1215,
                new Path("./statusPath"), "", null, null, null, null);
        final JpaMedia media = new JpaMedia();
        media.setName("test-media");
        media.setType(MediaType.JDK);
        media.setLocalPath(new File("d:/not/a/real/path.zip").toPath());
        media.setRemoteDir(new File("d:/fake/remote/path").toPath());
        media.setMediaDir(new File("test-media").toPath());

        final JpaMedia someJpaMedia = mediaDao.create(media);
        final JpaJvm jvm = jvmCrudService.createJvm(createJvmReq, mediaDao.create(media), someJpaMedia);

        List<JpaJvm> jvmsList = new ArrayList<>();
        jvmsList.add(jvm);
        group.setJvms(jvmsList);
        List<JpaGroup> groupList = new ArrayList<>();
        groupList.add(group);
        webServerCrudService.findById(webServer.getId().getId()).setGroups(groupList);
        applications = webServerCrudService.findApplications(webServer.getName());
        assertTrue(applications.size() == 1);

        final List<Jvm> jvms = webServerCrudService.findJvms(webServer.getName());
        assertEquals(1, jvms.size());
    }

    @Test
    public void testFindJvms() {
        WebServer webServer = new WebServer(new Identifier<WebServer>(1111L), new HashSet<Group>(), "testWebServer",
                "testHost", 101, 102, new Path("./statusPath"), WebServerReachableState.WS_UNREACHABLE, null);
        webServerCrudService.createWebServer(webServer, "testUser");
        List<Jvm> jvms = webServerCrudService.findJvms("testWebServer");
        assertTrue(jvms.size() == 0);
    }

    @Test
    public void testGetResourceTemplateNames() {
        final List<String> templates = webServerCrudService.getResourceTemplateNames("toost");
        assertTrue(templates.size() == 0);
    }

    @Test(expected = NonRetrievableResourceTemplateContentException.class)
    public void testGetResourceTemplate() {
        webServerCrudService.getResourceTemplate("teest", "tust");
    }

    @Test
    public void testUploadWebServerTemplate() throws FileNotFoundException {
        InputStream dataInputStream = new FileInputStream(new File("./src/test/resources/HttpdSslConfTemplate.tpl"));
        Scanner scanner = new Scanner(dataInputStream).useDelimiter("\\A");
        String templateContent = scanner.hasNext() ? scanner.next() : "";

        WebServer webServer = new WebServer(new Identifier<WebServer>(1111L), new HashSet<Group>(), "testWebServer",
                "testHost", 101, 102, new Path("./statusPath"), WebServerReachableState.WS_UNREACHABLE, null);
        webServer = webServerCrudService.createWebServer(webServer, "testUser");
        UploadWebServerTemplateRequest uploadWsTemplateRequest = new UploadWebServerTemplateRequest(webServer,
                "HttpdSslConfTemplate.tpl", HTTPD_CONF_META_DATA, templateContent) {
            @Override
            public String getConfFileName() {
                return "httpd.conf";
            }
        };
        JpaWebServerConfigTemplate template = webServerCrudService.uploadWebserverConfigTemplate(uploadWsTemplateRequest);
        assertNotNull(template);

        // again!
        dataInputStream = new FileInputStream(new File("./src/test/resources/HttpdSslConfTemplate.tpl"));
        uploadWsTemplateRequest = new UploadWebServerTemplateRequest(webServer, "HttpdSslConfTemplate.tpl",
                HTTPD_CONF_META_DATA, templateContent) {
            @Override
            public String getConfFileName() {
                return "httpd.conf";
            }
        };
        template = webServerCrudService.uploadWebserverConfigTemplate(uploadWsTemplateRequest);
        assertNotNull(template);

        // test getting the template while we're here
        final String resourceTemplate = webServerCrudService.getResourceTemplate(webServer.getName(), "httpd.conf");
        assertTrue(!resourceTemplate.isEmpty());
    }

    @Ignore
    @Test
    public void testPopulateWebServerConfigTemplate() throws FileNotFoundException {
        InputStream dataInputStream = new FileInputStream(new File("./src/test/resources/HttpdSslConfTemplate.tpl"));
        Scanner scanner = new Scanner(dataInputStream).useDelimiter("\\A");
        String templateContent = scanner.hasNext() ? scanner.next() : "";

        WebServer webServer = new WebServer(new Identifier<WebServer>(1111L), new HashSet<Group>(), "testWebServer",
                "testHost", 101, 102, new Path("./statusPath"), WebServerReachableState.WS_UNREACHABLE, null);
        webServer = webServerCrudService.createWebServer(webServer, "testUser");
        UploadWebServerTemplateRequest uploadWsTemplateRequest = new UploadWebServerTemplateRequest(webServer,
                "HttpdSslConfTemplate.tpl", StringUtils.EMPTY, templateContent) {
            @Override
            public String getConfFileName() {
                return "httpd.conf";
            }
        };
        List<UploadWebServerTemplateRequest> templateCommands = new ArrayList<>();
        templateCommands.add(uploadWsTemplateRequest);
//        webServerCrudService.populateWebServerConfig(templateCommands, new User("userId"), false);
        final String resourceTemplate = webServerCrudService.getResourceTemplate(webServer.getName(), "httpd.conf");
        assertTrue(!resourceTemplate.isEmpty());
    }

    @Test
    public void testUpdateResourceTemplate() throws FileNotFoundException {
        testUploadWebServerTemplate();
        webServerCrudService.updateResourceTemplate("testWebServer", "httpd.conf", "this is my template");
        final String updatedContent = webServerCrudService.getResourceTemplate("testWebServer", "httpd.conf");
        assertEquals("this is my template", updatedContent);
    }

    @Test
    public void testGetResourceTemplateMetaData() throws FileNotFoundException {
        testUploadWebServerTemplate();
        String metaData = webServerCrudService.getResourceTemplateMetaData("testWebServer", "httpd.conf");
        assertEquals(HTTPD_CONF_META_DATA, metaData);
    }

    @Test(expected = NonRetrievableResourceTemplateContentException.class)
    public void testGetResourceTemplateMetaDataReturnsNoResult() throws FileNotFoundException {
        String metaData = webServerCrudService.getResourceTemplateMetaData("testWebServer", "httpd.conf");
        assertEquals(HTTPD_CONF_META_DATA, metaData);
    }

    @Test(expected = ResourceTemplateUpdateException.class)
    public void testUpdateResourceTemplateThrowsException() {
        webServerCrudService.updateResourceTemplate("tyest", "templateNOOOO", "0_o");
    }

    @Test
    public void findApplicationsTest() {
        JpaGroup group = new JpaGroup();
        group.setName("aGroup");
        group = groupCrudService.create(group);

        final JpaWebServer webServer = new JpaWebServer();
        webServer.setName("aWebServer");
        webServer.setStatusPath("aStatusPath");
        webServer.getGroups().add(group);
        group.getWebServers().add(webServerCrudService.create(webServer));
        groupCrudService.update(group);

        final JpaApplication application = new JpaApplication();
        application.setName("anApplication");
        application.setWebAppContext("aWebAppContext");
        application.setGroup(group);
        applicationCrudService.create(application);

        assertEquals(1, webServerCrudService.getWebServer(new Identifier<WebServer>(webServer.getId())).getGroups().size());
        assertEquals(1, webServerCrudService.findApplications(webServer.getName()).size());
    }

    @Test
    public void testUpdateState() {
        final JpaWebServer jpaWebServerParam = new JpaWebServer();
        jpaWebServerParam.setName("WebServer");
        jpaWebServerParam.setStatusPath("/jwala.png");
        final JpaWebServer jpaWebServer = webServerCrudService.create(jpaWebServerParam);
        assertEquals(1, webServerCrudService.updateState(new Identifier<WebServer>(jpaWebServer.getId()),
                WebServerReachableState.WS_UNREACHABLE));
    }

    @Test
    public void testUpdateStateAndErrSts() {
        final JpaWebServer jpaWebServerParam = new JpaWebServer();
        jpaWebServerParam.setName("WebServer");
        jpaWebServerParam.setStatusPath("/jwala.png");
        final JpaWebServer jpaWebServer = webServerCrudService.create(jpaWebServerParam);
        assertEquals(1, webServerCrudService.updateState(new Identifier<WebServer>(jpaWebServer.getId()),
                WebServerReachableState.WS_UNREACHABLE, "error!"));
    }

    @Test
    public void testGetWebServerStartedCount() {
        Long webServerStartedCount = webServerCrudService.getWebServerStartedCount("test-group");
        assertEquals(new Long(0), webServerStartedCount);
    }

    @Test
    public void testGetWebServerCount() {
        Long count = webServerCrudService.getWebServerCount("test-group");
        assertEquals(new Long(0), count);
    }

    @Test
    public void testGetWebServerAndItsGroups() {
        JpaGroup group = new JpaGroup();
        group.setName("aGroup");
        group = groupCrudService.create(group);

        final JpaWebServer webServer = new JpaWebServer();
        webServer.setName("aWebServer");
        webServer.setStatusPath("aStatusPath");
        webServer.getGroups().add(group);

        final JpaWebServer jpaWebServer = webServerCrudService.create(webServer);

        group.getWebServers().add(webServer);
        groupCrudService.update(group);

        JpaWebServer result = webServerCrudService.getWebServerAndItsGroups(jpaWebServer.getId());
        assertEquals(1, result.getGroups().size());
    }

    @Test
    public void testGetWebServerAndItsGroupsForAWebServerAssignedToSeveralGroups() {
        JpaGroup group1 = new JpaGroup();
        group1.setName("group1");
        group1 = groupCrudService.create(group1);

        JpaGroup group2 = new JpaGroup();
        group2.setName("group2");
        group2 = groupCrudService.create(group2);

        final JpaWebServer webServer = new JpaWebServer();
        webServer.setName("aWebServer");
        webServer.setStatusPath("aStatusPath");
        webServer.getGroups().add(group1);
        webServer.getGroups().add(group2);

        final JpaWebServer jpaWebServer = webServerCrudService.create(webServer);

        group1.getWebServers().add(webServer);
        groupCrudService.update(group1);

        group2.getWebServers().add(webServer);
        groupCrudService.update(group2);

        JpaWebServer result = webServerCrudService.getWebServerAndItsGroups(jpaWebServer.getId());
        assertEquals("aWebServer", result.getName());
        assertEquals(2, result.getGroups().size());
    }

    @Test
    public void testGetWebServerStoppedCount() {
        Long count = webServerCrudService.getWebServerStoppedCount("test-group");
        assertEquals(new Long(0), count);
    }

    @Test
    public void testRemoveTemplate() {
        int result = webServerCrudService.removeTemplate("httpd.conf");
        assertEquals(0, result);
    }

    @Test
    public void testRemoveTemplateWithWebServerName() {
        int result = webServerCrudService.removeTemplate("test-ws", "httpd.conf");
        assertEquals(0, result);
    }

    @Test
    public void testGetWebServersByGroupName() {
        JpaGroup group = new JpaGroup();
        group.setName("aGroup");
        group = groupCrudService.create(group);

        final JpaWebServer webServer = new JpaWebServer();
        webServer.setName("aWebServer");
        webServer.setStatusPath("aStatusPath");
        webServer.getGroups().add(group);

        webServerCrudService.create(webServer);

        group.getWebServers().add(webServer);
        groupCrudService.update(group);

        List<WebServer> result = webServerCrudService.getWebServersByGroupName(group.getName());
        assertEquals(1, result.size());
    }

    @Test
    public void testFindWebServer() {
        JpaGroup group = new JpaGroup();
        group.setName("aGroup");
        group = groupCrudService.create(group);

        final JpaWebServer webServer = new JpaWebServer();
        webServer.setName("aWebServer");
        webServer.setStatusPath("aStatusPath");
        webServer.getGroups().add(group);

        final JpaWebServer jpaWebServer = webServerCrudService.create(webServer);

        group.getWebServers().add(webServer);
        groupCrudService.update(group);

        JpaWebServer result = webServerCrudService.findWebServer(group.getName(), webServer.getName());
        assertEquals(jpaWebServer, result);
    }

    @Test
    public void testCheckWebServerResourceFileName() {

        boolean result = webServerCrudService.checkWebServerResourceFileName("test-group", "test-ws", "httpd.conf");
        assertFalse(result);
    }

    @Test
    public void testCheckWebServerResourceFileNameReturnsTrue() {
        JpaGroup group = new JpaGroup();
        group.setName("aGroup");
        group = groupCrudService.create(group);

        final JpaWebServer webServer = new JpaWebServer();
        webServer.setName("aWebServer");
        webServer.setStatusPath("aStatusPath");
        webServer.getGroups().add(group);

        webServerCrudService.create(webServer);

        group.getWebServers().add(webServer);
        groupCrudService.update(group);

        JpaWebServerConfigTemplate template = webServerCrudService.uploadWebserverConfigTemplate(
                new UploadWebServerTemplateRequest(
                        webServerCrudService.getWebServer(new Identifier<WebServer>(webServer.getId())),
                        "httpd.conf.tpl",
                        HTTPD_CONF_META_DATA,
                        "httpd template content") {
                    @Override
                    public String getConfFileName() {
                        return "httpd.conf";
                    }
                });
        assertNotNull(template);

        boolean result = webServerCrudService.checkWebServerResourceFileName(group.getName(), webServer.getName(), "httpd.conf");
        assertTrue(result);
    }

    @Configuration
    @Import(TestJpaConfiguration.class)
    static class Config {
        @Bean
        public GroupCrudService getGroupCrudService() {
            return new GroupCrudServiceImpl();
        }

        @Bean
        public WebServerCrudService getWebServerCrudService() {
            return new WebServerCrudServiceImpl();
        }

        @Bean
        public ApplicationCrudService getApplicationCrudService() {
            return new ApplicationCrudServiceImpl();
        }

        @Bean
        public JvmCrudService getJvmCrudService() {
            return new JvmCrudServiceImpl();
        }

        @Bean
        public MediaDao getMediaDao() {
            return new MediaDaoImpl();
        }
    }

}
