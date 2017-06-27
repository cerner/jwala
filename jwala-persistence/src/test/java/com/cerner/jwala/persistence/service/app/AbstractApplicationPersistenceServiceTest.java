package com.cerner.jwala.persistence.service.app;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.media.MediaType;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.exception.NotFoundException;
import com.cerner.jwala.common.request.app.*;
import com.cerner.jwala.common.request.group.AddJvmToGroupRequest;
import com.cerner.jwala.common.request.group.CreateGroupRequest;
import com.cerner.jwala.common.request.jvm.CreateJvmRequest;
import com.cerner.jwala.dao.MediaDao;
import com.cerner.jwala.persistence.jpa.domain.JpaJvm;
import com.cerner.jwala.persistence.jpa.domain.JpaMedia;
import com.cerner.jwala.persistence.service.ApplicationPersistenceService;
import com.cerner.jwala.persistence.service.GroupPersistenceService;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import static org.junit.Assert.*;

@Transactional
public abstract class AbstractApplicationPersistenceServiceTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractApplicationPersistenceServiceTest.class); 

    @Autowired
    private ApplicationPersistenceService applicationPersistenceService;

    @Autowired
    private GroupPersistenceService groupPersistenceService;

    @Autowired
    private JvmPersistenceService jvmPersistenceService;

    @Autowired
    private MediaDao mediaDao;

    private String aUser;
    
    private String alphaLower = "abcdefghijklmnopqrstuvwxyz";
    private String alpha = alphaLower + alphaLower.toUpperCase(Locale.US);
    private String alphaNum = alpha + "0123456789,.-/_$ ";
    private String alphaUnsafe = alphaNum + "\\\t\r\n";
    
    private String textContext = "/" + RandomStringUtils.random(25,alphaUnsafe.toCharArray());
    private String textWarPath = RandomStringUtils.random(25,alphaUnsafe.toCharArray()) + ".war";
    private String textWarDeployPath = "c:/war/deploy/path-" + RandomStringUtils.random(25, alphaUnsafe.toCharArray());
    private String textName    = RandomStringUtils.random(25,alphaUnsafe.toCharArray());
    private String textGroup   = RandomStringUtils.random(25,alphaUnsafe.toCharArray());

    private String textUpdatedContext = "/updated" + RandomStringUtils.random(25,alphaUnsafe.toCharArray());
    private String textUpdatedWarPath = RandomStringUtils.random(25,alphaUnsafe.toCharArray()) + "-updated.war";
    private String textUpdatedName    = textName + "-updated";
    private String textUpdatedGroup   = textGroup+ "-updated";

    private Identifier<Group> expGroupId;
    private Identifier<Group> expUpdatedGroupId;
    private Identifier<Application> updateAppId;
    private Identifier<Application> deleteAppId;

    @Before
    public void setup() {
        User user = new User("testUser");

        Group group = groupPersistenceService.createGroup(new CreateGroupRequest(textGroup));
        Group updGroup = groupPersistenceService.createGroup(new CreateGroupRequest(textUpdatedGroup));
        expGroupId = group.getId();
        expUpdatedGroupId = updGroup.getId();

        final JpaMedia media = new JpaMedia();
        media.setName("test-media");
        media.setType(MediaType.JDK);
        mediaDao.create(media);

        deleteAppId = null;
        updateAppId = null;
    }
    
    @After
    public void tearDown() {
        if(updateAppId != null) { 
            try { applicationPersistenceService.removeApplication(updateAppId); } catch (Exception x) { LOGGER.trace("Test tearDown", x); }
        }
        try { groupPersistenceService.removeGroup(expUpdatedGroupId); } catch (Exception x) { LOGGER.trace("Test tearDown", x); }
        try { groupPersistenceService.removeGroup(expGroupId); } catch (Exception x) { LOGGER.trace("Test tearDown", x); }
    }
    
    @Test
    public void testCreateApp() {
        CreateApplicationRequest request = new CreateApplicationRequest(expGroupId,  textName, textContext, true, true, false);
        Application created = applicationPersistenceService.createApplication(request);
        assertNotNull(created.getGroup());
        assertEquals(expGroupId, created.getGroup().getId());
        assertEquals(textName, created.getName());
        assertEquals(textContext, created.getWebAppContext());
        assertTrue(created.isSecure());
        updateAppId = created.getId(); 
        deleteAppId = created.getId();
    }

    @Test
    public void testCreateNonSecureApp() {
        CreateApplicationRequest request = new CreateApplicationRequest(expGroupId,  textName, textContext, false, true, false);
        Application created = applicationPersistenceService.createApplication(request);
        assertNotNull(created.getGroup());
        assertEquals(expGroupId, created.getGroup().getId());
        assertEquals(textName, created.getName());
        assertEquals(textContext, created.getWebAppContext());
        assertTrue(!created.isSecure());
        updateAppId = created.getId();
        deleteAppId = created.getId();
    }
    
    @Test
    public void testUpdateApp() {
        if(updateAppId == null) {
            testCreateApp();
        }
        
        UpdateApplicationRequest updateApplicationRequest = new UpdateApplicationRequest(updateAppId, expUpdatedGroupId,  textUpdatedContext, textUpdatedName, true, true, false, textWarDeployPath);
        Application created = applicationPersistenceService.updateApplication(updateApplicationRequest);
        assertEquals(updateAppId, created.getId());
        assertNotNull(created.getGroup());
        assertEquals(expUpdatedGroupId, created.getGroup().getId());
        assertEquals(textUpdatedName, created.getName());
        assertEquals(textUpdatedContext, created.getWebAppContext());

    }
    
    @Test
    public void testRemoveApp() {
        testCreateApp();
        applicationPersistenceService.removeApplication(deleteAppId);
    }

    @Test(expected = NotFoundException.class)
    public void testRemoveAppAndFailUpdate() {
        testRemoveApp();
        testUpdateApp();
    }    

    @Test
    public void testUpdateSecureFlag() {
        CreateApplicationRequest request = new CreateApplicationRequest(expGroupId,  textName, textContext, true, true, false);
        Application created = applicationPersistenceService.createApplication(request);
        assertTrue(created.isSecure());

        final UpdateApplicationRequest updateApplicationRequest =
                new UpdateApplicationRequest(created.getId(),
                                             created.getGroup().getId(),
                                             created.getWebAppContext(),
                                             created.getName(), false, true, false, textWarDeployPath);
        Application updatedApplication = applicationPersistenceService.updateApplication(updateApplicationRequest);
        assertTrue(!updatedApplication.isSecure());
    }

    @Test
    public void testCreateAppConfForJvm() {
        String jvmName = "testJvmName";

        CreateGroupRequest createGroupReq = new CreateGroupRequest("testGroupName");
        Group group = groupPersistenceService.createGroup(createGroupReq);

        CreateApplicationRequest request = new CreateApplicationRequest(group.getId(), "testAppName", "/hctTest", true, true, false);
        Application app = applicationPersistenceService.createApplication(request);

        CreateJvmRequest createJvmRequest = new CreateJvmRequest(jvmName, "testHost", 9101, 9102, 9103, -1, 9104, new Path("./"), "", null, null, null, null);
        Jvm jvm = jvmPersistenceService.createJvm(createJvmRequest);

        AddJvmToGroupRequest addJvmToGroup = new AddJvmToGroupRequest(group.getId(), jvm.getId());
        group = groupPersistenceService.addJvmToGroup(addJvmToGroup);

        applicationPersistenceService.createApplicationConfigTemplateForJvm(jvmName, app, group.getId(), "app context meta data", "app context template");
        String resourceContent = applicationPersistenceService.getResourceTemplate(app.getName(), "hctTest.xml", jvmName, group.getName());
        assertEquals("app context template", resourceContent);

        applicationPersistenceService.removeApplication(app.getId());
        jvmPersistenceService.removeJvm(jvm.getId());
        groupPersistenceService.removeGroup(group.getId());
    }

    @Test
    public void testUpdateResourceTemplate() {
        String jvmName = "testJvmName";

        CreateGroupRequest createGroupReq = new CreateGroupRequest("testGroupName");
        Group group = groupPersistenceService.createGroup(createGroupReq);

        CreateJvmRequest createJvmRequest = new CreateJvmRequest(jvmName, "testHost", 9101, 9102, 9103, -1, 9104, new Path("./"), "", null, null, null, null);
        Jvm jvm = jvmPersistenceService.createJvm(createJvmRequest);

        AddJvmToGroupRequest addJvmToGroup = new AddJvmToGroupRequest(group.getId(), jvm.getId());
        group = groupPersistenceService.addJvmToGroup(addJvmToGroup);

        CreateApplicationRequest request = new CreateApplicationRequest(group.getId(), "testAppName", "/hctTest", true, true, false);
        Application app = applicationPersistenceService.createApplication(request);

        applicationPersistenceService.removeApplication(app.getId());
        jvmPersistenceService.removeJvm(jvm.getId());
        groupPersistenceService.removeGroup(group.getId());
    }

    @Test
    public void testUploadAppTemplate() throws FileNotFoundException {
        CreateJvmRequest createJvmRequest = new CreateJvmRequest("testJvmName", "testHostName", 9101, 9102, 9103, -1,
                9104, new Path("./"), "", null, null, null, null);

        CreateGroupRequest createGroupReq = new CreateGroupRequest("testGroupName");
        Group group = groupPersistenceService.createGroup(createGroupReq);

        Jvm jvm = jvmPersistenceService.createJvm(createJvmRequest);
        JpaJvm jpaJvm = jvmPersistenceService.getJpaJvm(jvm.getId(), false);

        AddJvmToGroupRequest addJvmGrpRequest = new AddJvmToGroupRequest(group.getId(), jvm.getId());
        group = groupPersistenceService.addJvmToGroup(addJvmGrpRequest);

        CreateApplicationRequest request = new CreateApplicationRequest(group.getId(), "testAppName", "/hctTest", true, true, false);
        Application app = applicationPersistenceService.createApplication(request);

        Application sameApp = applicationPersistenceService.getApplication(app.getId());
        assertEquals(app.getName(), sameApp.getName());

        sameApp = applicationPersistenceService.findApplication(app.getName(), group.getName(), jvm.getJvmName());
        assertEquals(app.getName(), sameApp.getName());

        List<Application> appList = applicationPersistenceService.findApplicationsBelongingToJvm(jvm.getId());
        assertEquals(1, appList.size());
        assertEquals(app.getName(), appList.get(0).getName());

        InputStream dataStream = new FileInputStream(new File("./src/test/resources/ServerXMLTemplate.tpl"));
        Scanner scanner = new Scanner(dataStream).useDelimiter("\\A");
        String templateContent = scanner.hasNext() ? scanner.next() : "";

        UploadAppTemplateRequest uploadAppTemplateRequest = new UploadAppTemplateRequest(app, "ServerXMLTemplate.tpl", "hctTest.xml", jvm.getJvmName(), "meta data", templateContent);

        applicationPersistenceService.uploadAppTemplate(uploadAppTemplateRequest, jpaJvm);

        applicationPersistenceService.removeApplication(app.getId());
        jvmPersistenceService.removeJvm(jvm.getId());
        groupPersistenceService.removeGroup(group.getId());
    }
}