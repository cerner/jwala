package com.cerner.jwala.persistence.jpa.service.app.impl;

import com.cerner.jwala.common.configuration.TestExecutionProfile;
import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.exception.NotFoundException;
import com.cerner.jwala.common.request.app.CreateApplicationRequest;
import com.cerner.jwala.common.request.app.UploadAppTemplateRequest;
import com.cerner.jwala.common.request.group.CreateGroupRequest;
import com.cerner.jwala.common.request.jvm.CreateJvmRequest;
import com.cerner.jwala.dao.MediaDao;
import com.cerner.jwala.dao.impl.MediaDaoImpl;
import com.cerner.jwala.persistence.configuration.TestJpaConfiguration;
import com.cerner.jwala.persistence.jpa.domain.JpaApplication;
import com.cerner.jwala.persistence.jpa.domain.JpaGroup;
import com.cerner.jwala.persistence.jpa.domain.JpaJvm;
import com.cerner.jwala.persistence.jpa.domain.JpaMedia;
import com.cerner.jwala.persistence.jpa.service.ApplicationCrudService;
import com.cerner.jwala.persistence.jpa.service.GroupCrudService;
import com.cerner.jwala.persistence.jpa.service.GroupJvmRelationshipService;
import com.cerner.jwala.persistence.jpa.service.JvmCrudService;
import com.cerner.jwala.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.cerner.jwala.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.cerner.jwala.persistence.jpa.service.impl.ApplicationCrudServiceImpl;
import com.cerner.jwala.persistence.jpa.service.impl.GroupCrudServiceImpl;
import com.cerner.jwala.persistence.jpa.service.impl.GroupJvmRelationshipServiceImpl;
import com.cerner.jwala.persistence.jpa.service.impl.JvmCrudServiceImpl;
import com.cerner.jwala.persistence.jpa.type.MediaType;
import com.cerner.jwala.persistence.service.GroupPersistenceService;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.persistence.service.impl.JpaGroupPersistenceServiceImpl;
import com.cerner.jwala.persistence.service.impl.JpaJvmPersistenceServiceImpl;
import org.apache.commons.lang.RandomStringUtils;
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

import javax.persistence.EntityExistsException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@EnableTransactionManagement
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
        classes = {ApplicationCrudServiceImplTest.Config.class
        })
public class ApplicationCrudServiceImplTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(ApplicationCrudServiceImplTest.class);

    @Configuration
    @Import(TestJpaConfiguration.class)
    static class Config {

        @Bean
        public MediaDao getMediaDao() {
            return new MediaDaoImpl();
        }

        @Bean
        public GroupPersistenceService getGroupPersistenceService() {
            return new JpaGroupPersistenceServiceImpl(getGroupCrudService(), getGroupJvmRelationshipService(),
                    getApplicationCrudService());
        }

        @Bean
        public JvmPersistenceService getJvmPersistenceService() {
            return new JpaJvmPersistenceServiceImpl(getJvmCrudService(), getApplicationCrudService(), getGroupJvmRelationshipService());
        }

        @Bean
        public GroupCrudService getGroupCrudService() {
            return new GroupCrudServiceImpl();
        }

        @Bean
        public ApplicationCrudService getApplicationCrudService() {
            return new ApplicationCrudServiceImpl();
        }

        @Bean
        public GroupJvmRelationshipService getGroupJvmRelationshipService() {
            return new GroupJvmRelationshipServiceImpl(getGroupCrudService(),
                    getJvmCrudService());
        }

        @Bean
        public JvmCrudService getJvmCrudService() {
            return new JvmCrudServiceImpl();
        }
    }

    @Autowired
    ApplicationCrudService applicationCrudService;

    @Autowired
    GroupCrudService groupCrudService;

    @Autowired
    JvmCrudService jvmCrudService;

    @Autowired
    MediaDao mediaDao;

    private String aUser;

    private String alphaLower = "abcdefghijklmnopqrstuvwxyz";
    private String alpha = alphaLower + alphaLower.toUpperCase(Locale.US);
    private String alphaNum = alpha + "0123456789,.-/_$ ";
    private String alphaUnsafe = alphaNum + "\\\t\r\n";

    private String textContext = "/" + RandomStringUtils.random(25, alphaUnsafe.toCharArray());
    private String textName = RandomStringUtils.random(25, alphaUnsafe.toCharArray());
    private String textGroup = RandomStringUtils.random(25, alphaUnsafe.toCharArray());


    private Identifier<Group> expGroupId;
    private JpaGroup jpaGroup;
    private JpaMedia jpaMedia;

    private User userObj;


    @Before
    public void setup() {
        User user = new User("testUser");

        aUser = "TestUserId";
        userObj = new User(aUser);
        jpaGroup = groupCrudService.createGroup(new CreateGroupRequest(textGroup));
        final JpaMedia media = new JpaMedia();
        media.setName("test-media");
        media.setType(MediaType.JDK);
        media.setLocalPath(new File("d:/not/a/real/path.zip").toPath());
        media.setRemoteDir(new File("d:/fake/remote/path").toPath());
        media.setMediaDir(new File("test-media").toPath());
        jpaMedia = mediaDao.create(media);
        expGroupId = Identifier.id(jpaGroup.getId());
    }

    @After
    public void tearDown() {
        try {
            groupCrudService.removeGroup(expGroupId);
        } catch (Exception x) {
            LOGGER.trace("Test tearDown", x);
        }
    }

    @Test(expected = EntityExistsException.class)
    public void testApplicationCrudServiceEEE() {
        CreateApplicationRequest request = new CreateApplicationRequest(expGroupId, textName, textContext, true, true, false);

        JpaApplication created = applicationCrudService.createApplication(request, jpaGroup);

        assertNotNull(created);

        try {
            JpaApplication duplicate = applicationCrudService.createApplication(request, jpaGroup);
            fail(duplicate.toString());
        } catch (BadRequestException e) {
            assertEquals(FaultType.DUPLICATE_APPLICATION, e.getMessageResponseStatus());
            throw e;
        } finally {
            try {
                applicationCrudService.removeApplication(Identifier.<Application>id(created.getId())
                );
            } catch (Exception x) {
                LOGGER.trace("Test tearDown", x);
            }
        }

    }

    @Test
    public void testDuplicateContextsOk() {
        CreateApplicationRequest request = new CreateApplicationRequest(expGroupId, textName, textContext, true, true, false);

        JpaApplication created2 = null;
        JpaApplication created = applicationCrudService.createApplication(request, jpaGroup);

        assertNotNull(created);

        try {
            CreateApplicationRequest request2 = new CreateApplicationRequest(expGroupId, textName + "-another", textContext, true, true, false);

            created2 = applicationCrudService.createApplication(request2, jpaGroup);

            assertNotNull(created2);
        } finally {
            try {
                applicationCrudService.removeApplication(Identifier.<Application>id(created.getId()));
            } catch (Exception x) {
                LOGGER.trace("Test tearDown", x);
            }
            try {
                if (created2 != null) {
                    applicationCrudService.removeApplication(Identifier.<Application>id(created2.getId()));
                }
            } catch (Exception x) {
                LOGGER.trace("Test tearDown", x);
            }
        }
    }

    @Test
    public void testGetResourceTemplateNames() {
        List<String> templateNames = applicationCrudService.getResourceTemplateNames("testNoAppExists", "nada");
        assertEquals(0, templateNames.size());
    }

    @Test(expected = NonRetrievableResourceTemplateContentException.class)
    public void testGetResourceTemplateNonExistent() {
        CreateJvmRequest createJvmRequest = new CreateJvmRequest("testGetResourceTemplateJvm", "testHost", 9100, 9101,
                9102, -1, 9103, new Path("./"), "", null, null, null, null);
        JpaJvm jvm = jvmCrudService.createJvm(createJvmRequest, jpaMedia, jpaMedia);
        applicationCrudService.getResourceTemplate("testNoAppExists", "hct.xml", jvm);
    }

    @Test
    public void testGetResourceTemplate() throws FileNotFoundException {
        InputStream data = new FileInputStream(new File("./src/test/resources/ServerXMLTemplate.tpl"));
        Scanner scanner = new Scanner(data).useDelimiter("\\A");
        String templateContent = scanner.hasNext() ? scanner.next() : "";

        CreateJvmRequest createJvmRequest = new CreateJvmRequest("testJvmName", "testHost", 9100, 9101, 9102, -1, 9103,
                new Path("./"), "", null, null, null, null);
        CreateApplicationRequest createApplicationRequest = new CreateApplicationRequest(new Identifier<Group>(jpaGroup.getId()), "testAppResourceTemplateName", "/hctTest", true, true, false);
        Group group = new Group(new Identifier<Group>(jpaGroup.getId()), jpaGroup.getName());
        JpaJvm jpaJvm = jvmCrudService.createJvm(createJvmRequest, jpaMedia, jpaMedia);
        JpaApplication jpaApp = applicationCrudService.createApplication(createApplicationRequest, jpaGroup);

        List<Application> appsForJpaGroup = applicationCrudService.findApplicationsBelongingTo(new Identifier<Group>(jpaGroup.getId()));
        assertEquals(1, appsForJpaGroup.size());

        Application app = new Application(new Identifier<Application>(jpaApp.getId()), jpaApp.getName(), jpaApp.getWarPath(), jpaApp.getWebAppContext(), group, true, true, false, "testApp.war");
        UploadAppTemplateRequest uploadTemplateRequest = new UploadAppTemplateRequest(app, "ServerXMLTemplate.tpl", "hct.xml",
                "testJvmName", StringUtils.EMPTY, templateContent);

        applicationCrudService.uploadAppTemplate(uploadTemplateRequest, jpaJvm);
        String templateResult = applicationCrudService.getResourceTemplate("testAppResourceTemplateName", "hct.xml", jpaJvm);

        assertTrue(!templateResult.isEmpty());

        data = new FileInputStream(new File("./src/test/resources/ServerXMLTemplate.tpl"));
        scanner = new Scanner(data).useDelimiter("\\A");
        templateContent = scanner.hasNext() ? scanner.next() : "";
        uploadTemplateRequest = new UploadAppTemplateRequest(app, "ServerXMLTemplate.tpl", "hct.xml", "testJvmName", StringUtils.EMPTY, templateContent);
        applicationCrudService.uploadAppTemplate(uploadTemplateRequest, jpaJvm);
        String templateContentUpdateWithTheSame = applicationCrudService.getResourceTemplate("testAppResourceTemplateName", "hct.xml", jpaJvm);

        assertEquals(templateContent, templateContentUpdateWithTheSame);

        applicationCrudService.updateResourceTemplate(app.getName(), "hct.xml", "new template content", jpaJvm);
        String updatedContent = applicationCrudService.getResourceTemplate(app.getName(), "hct.xml", jpaJvm);
        assertEquals("new template content", updatedContent);
    }

    @Test(expected = ResourceTemplateUpdateException.class)
    public void testUpdateResourceTemplate() {
        CreateJvmRequest createJvmRequest = new CreateJvmRequest("testJvmName", "testHost", 9100, 9101, 9102, -1, 9103,
                new Path("./"), "", null, null, null, null);
        JpaJvm jpaJvm = jvmCrudService.createJvm(createJvmRequest, jpaMedia, jpaMedia);
        applicationCrudService.updateResourceTemplate("noApp", "noTemplate", "doesn't matter", jpaJvm);
    }

    @Test(expected = NotFoundException.class)
    public void testGetApplicationThrowsException() {
        applicationCrudService.getApplication(new Identifier<Application>(888888L));
    }

    @Test
    public void testGetApplication() {
        CreateApplicationRequest createTestApp = new CreateApplicationRequest(new Identifier<Group>(jpaGroup.getId()), "testAppName", "/testApp", true, true, false);
        JpaApplication jpaApp = applicationCrudService.createApplication(createTestApp, jpaGroup);
        Application application = applicationCrudService.getApplication(new Identifier<Application>(jpaApp.getId()));
        assertEquals(jpaApp.getName(), application.getName());
    }

    @Test(expected = NotFoundException.class)
    public void testGetApplicationThrowsNotFoundException() {
        applicationCrudService.getApplication(new Identifier<Application>(808L));
    }

    @Test
    public void testGetApplications() {
        List<Application> apps = applicationCrudService.getApplications();
        assertEquals(0, apps.size());
    }

    @Test
    public void testFindApplicationBelongingToJvm() {
        CreateJvmRequest createJvmRequest = new CreateJvmRequest("testAppJvm", "theHost", 9100, 9101, 9102, -1, 9103,
                new Path("."), "", null, null, null, null);
        JpaJvm jpaJvm = jvmCrudService.createJvm(createJvmRequest, jpaMedia, jpaMedia);

        List<Application> apps = applicationCrudService.findApplicationsBelongingToJvm(new Identifier<Jvm>(jpaJvm.getId()));
        assertEquals(0, apps.size());
    }

    @Test
    public void testFindApplication() {
        CreateApplicationRequest createTestApp = new CreateApplicationRequest(new Identifier<Group>(jpaGroup.getId()), "testAppName", "/testApp", true, true, false);
        JpaApplication jpaApp = applicationCrudService.createApplication(createTestApp, jpaGroup);

        CreateJvmRequest createJvmRequest = new CreateJvmRequest("testJvmName", "hostName", 9100, 9101, 9102, -1, 9103,
                new Path("./"), "", null, null, null, null);
        JpaJvm jpaJvm = jvmCrudService.createJvm(createJvmRequest, jpaMedia, jpaMedia);

        List<JpaJvm> jvmList = new ArrayList<>();
        jvmList.add(jpaJvm);
        jpaGroup.setJvms(jvmList);

        applicationCrudService.findApplication(jpaApp.getName(), jpaGroup.getName(), jpaJvm.getName());
    }

}
