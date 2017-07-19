package com.cerner.jwala.persistence.jpa.service.group.impl;

import com.cerner.jwala.common.configuration.TestExecutionProfile;
import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.exception.NotFoundException;
import com.cerner.jwala.common.request.app.CreateApplicationRequest;
import com.cerner.jwala.common.request.group.CreateGroupRequest;
import com.cerner.jwala.common.request.jvm.UploadJvmTemplateRequest;
import com.cerner.jwala.common.request.webserver.UploadWebServerTemplateRequest;
import com.cerner.jwala.persistence.configuration.TestJpaConfiguration;
import com.cerner.jwala.persistence.jpa.domain.JpaApplication;
import com.cerner.jwala.persistence.jpa.domain.JpaGroup;
import com.cerner.jwala.persistence.jpa.domain.JpaWebServer;
import com.cerner.jwala.persistence.jpa.service.ApplicationCrudService;
import com.cerner.jwala.persistence.jpa.service.GroupCrudService;
import com.cerner.jwala.persistence.jpa.service.WebServerCrudService;
import com.cerner.jwala.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.cerner.jwala.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.cerner.jwala.persistence.jpa.service.impl.GroupCrudServiceImpl;
import com.cerner.jwala.persistence.jpa.service.impl.WebServerCrudServiceImpl;
import junit.framework.TestCase;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@EnableTransactionManagement
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
        classes = {GroupCrudServiceImplTest.Config.class
        })
public class GroupCrudServiceImplTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(GroupCrudServiceImplTest.class);
    private String groupName = "groupName";
    private Identifier<Group> groupId;

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
    }

    @Autowired
    GroupCrudService groupCrudService;

    @Autowired
    private ApplicationCrudService applicationCrudService;

    private JpaApplication jpaApplication;

    private JpaApplication jpaOtherApplication;

    @Autowired
    WebServerCrudService webServerCrudService;

    @Before
    public void setup() {
        User user = new User("testUser");
        CreateGroupRequest createGroupRequest = new CreateGroupRequest(groupName);
        JpaGroup testGroup = groupCrudService.createGroup(createGroupRequest);
        groupId = new Identifier<>(testGroup.getId());

        jpaApplication = applicationCrudService.createApplication(new CreateApplicationRequest(groupId, "some-app-name", "", false, false, false),
                testGroup);
        jpaOtherApplication = applicationCrudService.createApplication(new CreateApplicationRequest(groupId, "some-other-app-name", "", false, false, false),
                testGroup);
    }

    @After
    public void tearDown() {
        applicationCrudService.removeApplication(new Identifier<Application>(jpaApplication.getId()));
        applicationCrudService.removeApplication(new Identifier<Application>(jpaOtherApplication.getId()));
        groupCrudService.removeGroup(groupId);
    }

    @Test
    public void testGetGroup() {
        JpaGroup group = groupCrudService.getGroup(groupName);
        assertNotNull(group);

        try {
            groupCrudService.getGroup("group does not exist");
        } catch (NotFoundException e) {
            assertTrue(e.getMessageResponseStatus().equals(FaultType.GROUP_NOT_FOUND));
        }
    }

    @Test
    public void testGetGroupId() {
        TestCase.assertEquals(groupId.getId(), groupCrudService.getGroupId(groupName));
    }

    @Test
    public void testLinkWebServer() {
        WebServer webServer = new WebServer(new Identifier<WebServer>(1111L), new HashSet<Group>(), "testWebServer", "testHost",
                101, 102, new Path("./statusPath"), WebServerReachableState.WS_UNREACHABLE, null);
        webServer = webServerCrudService.createWebServer(webServer, "testGroupCrud");
        groupCrudService.linkWebServer(webServer);
        JpaGroup group = groupCrudService.getGroup(groupName);
        List<JpaWebServer> wsList = new ArrayList<>();
        final JpaWebServer jpaWebServer = webServerCrudService.findById(webServer.getId().getId());
        wsList.add(jpaWebServer);
        group.setWebServers(wsList);
        List<JpaGroup> groupsList = new ArrayList<>();
        groupsList.add(group);
        jpaWebServer.setGroups(groupsList);
        groupCrudService.linkWebServer(webServerCrudService.getWebServer(new Identifier<WebServer>(jpaWebServer.getId())));
    }

    @Test
    public void testUploadGroupJvmTemplate() throws FileNotFoundException {
        FileInputStream dataInputStream = new FileInputStream(new File("./src/test/resources/ServerXMLTemplate.tpl"));
        Scanner scanner = new Scanner(dataInputStream).useDelimiter("\\A");
        String templateContent = scanner.hasNext() ? scanner.next() : "";

        Jvm jvm = new Jvm(new Identifier<Jvm>(1212L), "testJvm", new HashSet<Group>());
        UploadJvmTemplateRequest uploadJvmTemplateRequest = new UploadJvmTemplateRequest(jvm, "ServerXMLTemplate.tpl",
                templateContent, StringUtils.EMPTY) {
            @Override
            public String getConfFileName() {
                return "server.xml";
            }
        };
        groupCrudService.uploadGroupJvmTemplate(uploadJvmTemplateRequest, groupCrudService.getGroup(groupName));
        // twice is nice :)
        groupCrudService.uploadGroupJvmTemplate(uploadJvmTemplateRequest, groupCrudService.getGroup(groupName));

        // TODO: Assert the code below!
        groupCrudService.checkGroupJvmResourceFileName(groupName, "ServerXMLTemplate.tpl");
    }

    @Test
    public void testUploadGroupWebServerTemplate() throws FileNotFoundException {
        InputStream dataInputStream = new FileInputStream(new File("./src/test/resources/HttpdSslConfTemplate.tpl"));
        Scanner scanner = new Scanner(dataInputStream).useDelimiter("\\A");
        String templateContent = scanner.hasNext() ? scanner.next() : "";

        WebServer webServer = new WebServer(new Identifier<WebServer>(1313L), new HashSet<Group>(), "testWebServer");
        UploadWebServerTemplateRequest uploadWsTemplateRequest = new UploadWebServerTemplateRequest(webServer,
                "HttpdSslConfTemplate.tpl", StringUtils.EMPTY, templateContent) {
            @Override
            public String getConfFileName() {
                return "httpd.conf";
            }
        };
        groupCrudService.uploadGroupWebServerTemplate(uploadWsTemplateRequest, groupCrudService.getGroup(groupName));

        // twice is ... ok I guess
        groupCrudService.uploadGroupWebServerTemplate(uploadWsTemplateRequest, groupCrudService.getGroup(groupName));

        // TODO: Assert the code below!
        groupCrudService.checkGroupWebServerResourceFileName(groupName, uploadWsTemplateRequest.getConfFileName());
    }

    @Test
    public void testGetGroupJvmTemplateNames() {
        groupCrudService.getGroupJvmsResourceTemplateNames(groupName);
    }

    @Test
    public void testGetGroupWebServerTemplateNames() {
        groupCrudService.getGroupWebServersResourceTemplateNames(groupName);
    }

    @Test(expected = ResourceTemplateUpdateException.class)
    public void testUpdateJvmResourceTemplateThrowsException() {
        // no template exists yet so throw the exception
        groupCrudService.updateGroupJvmResourceTemplate(groupName, "server.xml", "new content");
    }

    @Test
    public void testUpdateJvmResourceTemplate() throws FileNotFoundException {
        testUploadGroupJvmTemplate();
        groupCrudService.updateGroupJvmResourceTemplate(groupName, "server.xml", "new content");
        final String groupJvmResourceTemplate = groupCrudService.getGroupJvmResourceTemplate(groupName, "server.xml");
        assertEquals("new content", groupJvmResourceTemplate);
    }

    @Test(expected = NonRetrievableResourceTemplateContentException.class)
    public void testGetGroupJvmResourceTemplateThrowsException() {
        groupCrudService.getGroupJvmResourceTemplate(groupName, "NOTME");
    }

    @Test(expected = ResourceTemplateUpdateException.class)
    public void testUpdateWebServerResourceTemplateThrowsException() {
        // no template exists so throw exception
        groupCrudService.updateGroupWebServerResourceTemplate(groupName, "httpd.conf", "new httpd content");
    }

    @Test
    public void testUpdateWebServerResourceTemplate() throws FileNotFoundException {
        testUploadGroupWebServerTemplate();
        groupCrudService.updateGroupWebServerResourceTemplate(groupName, "httpd.conf", "new httpd content");
        final String groupWebServerResourceTemplate = groupCrudService.getGroupWebServerResourceTemplate(groupName, "httpd.conf");
        assertEquals("new httpd content", groupWebServerResourceTemplate);
    }

    @Test(expected = NonRetrievableResourceTemplateContentException.class)
    public void testGetGroupWebServerTemplateThrowsException() {
        groupCrudService.getGroupWebServerResourceTemplate(groupName, "UHUHUH-youdidntsaythemagicword");
    }

    @Test(expected = ResourceTemplateUpdateException.class)
    public void testUpdateGroupAppTemplateThrowsException() {
        groupCrudService.updateGroupAppResourceTemplate(groupName, "some-app-name", "hct.xml", "new hct.xml template");
    }

    @Test
    public void testUpdateGroupAppTemplate() {
        groupCrudService.populateGroupAppTemplate(groupName, "some-app-name", "hct.xml", "hct meta data", "old hct.xml template");
        String appTemplateContent = groupCrudService.getGroupAppResourceTemplate(groupName, "some-app-name", "hct.xml");
        assertEquals("old hct.xml template", appTemplateContent);

        groupCrudService.updateGroupAppResourceTemplate(groupName, "some-app-name", "hct.xml", "new hct.xml template");
        appTemplateContent = groupCrudService.getGroupAppResourceTemplate(groupName, "some-app-name", "hct.xml");
        assertEquals("new hct.xml template", appTemplateContent);
    }

    @Test(expected = NonRetrievableResourceTemplateContentException.class)
    public void testGetGroupAppResourceTemplateThrowsException() {
        groupCrudService.getGroupAppResourceTemplate("noSuchGroup", "noSuchAppName", "noSuchTemplate");
    }

    @Test
    public void testGetGroupAppResourceTemplateNames() {
        List<String> templateNames = groupCrudService.getGroupAppsResourceTemplateNames(groupName);
        assertEquals(0, templateNames.size());
    }

    @Test
    public void testGetGroupAppResourceTemplateNamesWithAppName() {
        groupCrudService.populateGroupAppTemplate(groupName, "some-app-name", "hct.xml", "hct meta data", "old hct.xml template");
        groupCrudService.populateGroupAppTemplate(groupName, "some-other-app-name", "other-hct.xml", "other hct meta data", "old other hct.xml template");

        List<String> templateNames = groupCrudService.getGroupAppsResourceTemplateNames(groupName, "some-app-name");
        assertEquals(1, templateNames.size());
    }

    @Test(expected = NonRetrievableResourceTemplateContentException.class)
    public void testGetGroupAppResourceTemplateMetaDataWithAppname() {
        groupCrudService.getGroupAppResourceTemplateMetaData(groupName, "hct.xml", "some-app-name");
    }
}