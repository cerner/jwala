package com.cerner.jwala.persistence.service.group;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.exception.GroupException;
import com.cerner.jwala.common.exception.NotFoundException;
import com.cerner.jwala.common.request.app.CreateApplicationRequest;
import com.cerner.jwala.common.request.group.CreateGroupRequest;
import com.cerner.jwala.common.request.jvm.UploadJvmTemplateRequest;
import com.cerner.jwala.common.request.webserver.UploadWebServerTemplateRequest;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.ConfigTemplate;
import com.cerner.jwala.persistence.service.*;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

import static org.junit.Assert.*;

@Transactional
public abstract class AbstractGroupPersistenceServiceIntegrationTest {

    @Autowired
    private GroupPersistenceService groupPersistenceService;

    @Autowired
    private JvmPersistenceService jvmPersistenceService;

    @Autowired
    private ApplicationPersistenceService applicationPersistenceService;

    private CommonGroupPersistenceServiceBehavior groupHelper;
    private CommonJvmPersistenceServiceBehavior jvmHelper;
    private Group preCreatedGroup;
    private Jvm preCreatedJvm;
    private String userId;
    private Application application;

    @Before
    public void setUp() throws Exception {
        User user = new User("testUser");

        groupHelper = new CommonGroupPersistenceServiceBehavior(groupPersistenceService);
        jvmHelper = new CommonJvmPersistenceServiceBehavior(jvmPersistenceService);

        userId = "Test User Id";

        preCreatedGroup = groupHelper.createGroup("Pre-Created GroupName",
                                                  userId);

        preCreatedJvm = jvmHelper.createJvm("Pre-Created JVM Name", "Pre-Created JVM Host Name", 5, 4, 3, 2, 1, userId,
                new Path("/abc"), "EXAMPLE_OPTS=%someEnv%/someVal", null, null, null, null);

        application = applicationPersistenceService.createApplication(new CreateApplicationRequest(preCreatedGroup.getId(),
                        "testApp", "", false, false, false));

    }

    @Test
    public void testCreateGroup() {

        final String groupName = "newGroupName";

        final Group actualGroup = groupHelper.createGroup(groupName,
                                                     userId);

        assertEquals(groupName,
                     actualGroup.getName());
        assertNotNull(actualGroup.getId());
    }

    @Test(expected = EntityExistsException.class)
    public void testCreateDuplicateGroup() {

        groupHelper.createGroup(preCreatedGroup.getName(),
                           userId);
    }

    @Test
    public void testUpdateGroup() {

        final String newGroupName = "My New Name";

        final Group actualGroup = groupHelper.updateGroup(preCreatedGroup.getId(),
                                                     newGroupName,
                                                     userId);



        assertEquals(newGroupName,
                     actualGroup.getName());
    }

    @Test(expected = NotFoundException.class)
    public void testUpdateNonExistent() {

        final Identifier<Group> nonExistentGroupId = new Identifier<>(-123456L);

        groupHelper.updateGroup(nonExistentGroupId,
                           "Unused",
                           userId);
    }

    @Test(expected = EntityExistsException.class)
    public void testUpdateDuplicateGroup() {

        final Group newGroup = groupHelper.createGroup("Group Name to turn into a duplicate",
                                                  userId);

        groupHelper.updateGroup(newGroup.getId(),
                           preCreatedGroup.getName(),
                           userId);
    }

    @Test
    public void testGetGroup() {

        final Identifier<Group> expectedGroupIdentifier = preCreatedGroup.getId();

        final Group group = groupPersistenceService.getGroup(expectedGroupIdentifier);

        assertEquals(preCreatedGroup.getName(),
                     group.getName());
        assertEquals(expectedGroupIdentifier,
                     group.getId());
    }

    @Test(expected = NotFoundException.class)
    public void testGetNonExistentGroup() {

        groupPersistenceService.getGroup(new Identifier<Group>(-123456L));
    }

    @Test
    public void testGetGroups() {

        groupHelper.createGroup("Auto-constructed Group " + (1),
                           "Auto-constructed User " + (1));

        final List<Group> actualGroups = groupPersistenceService.getGroups();

        assertTrue(actualGroups.size() > 0);
    }

    @Test
    public void testFindGroups() {

        final String expectedContains = preCreatedGroup.getName().substring(3, 5);

        final List<Group> actualGroups = groupPersistenceService.findGroups(expectedContains);

        for(final Group group : actualGroups) {
            assertTrue(group.getName().contains(expectedContains));
        }
    }

    @Test
    public void testRemoveGroup() {

        final Identifier<Group> groupId = preCreatedGroup.getId();

        applicationPersistenceService.removeApplication(application.getId());
        groupPersistenceService.removeGroup(groupId);

        try {
            groupPersistenceService.getGroup(groupId);
        } catch (final NotFoundException nfe) {
            //Success (This could be declared as expected in the @Test annotation, but I want to verify
            //that removeGroup() actually succeeded and didn't throw a NotFoundException itself
            assertTrue(true);  //To avoid the empty catch block warning
        }
    }

    @Test(expected = NotFoundException.class)
    public void testRemoveNonExistent() {

        final Identifier<Group> nonExistentGroupId = new Identifier<>(-123456L);

        groupPersistenceService.removeGroup(nonExistentGroupId);
    }

    @Test
    public void testAddJvmToGroup() {

        final Identifier<Group> groupId = preCreatedGroup.getId();

        groupHelper.addJvmToGroup(groupId,
                             preCreatedJvm.getId(),
                             userId);

        final Group group = groupPersistenceService.getGroup(groupId);

        assertNotNull(group);
        assertNotNull(group.getJvms());
        assertFalse(group.getJvms().isEmpty());

        boolean foundJvm = false;
        for (final Jvm jvm : group.getJvms()) {
            if (jvm.getId().equals(preCreatedJvm.getId())) {
                foundJvm = true;
                break;
            }
        }
        assertTrue(foundJvm);
    }

    @Test(expected = NotFoundException.class)
    public void testAddNonExistentJvmToGroup() {

        final Identifier<Jvm> nonExistentJvm = new Identifier<>(-123456L);

        groupHelper.addJvmToGroup(preCreatedGroup.getId(),
                             nonExistentJvm,
                             userId);
    }

    @Test(expected = NotFoundException.class)
    public void testAddJvmToNonExistentGroup() {

        final Identifier<Group> nonExistentGroup = new Identifier<>(-123456L);

        groupHelper.addJvmToGroup(nonExistentGroup,
                             preCreatedJvm.getId(),
                             userId);
    }

    @Test(expected = NotFoundException.class)
    public void testRemoveJvmFromNonExistentGroup() {

        final Identifier<Group> nonExistentGroup = new Identifier<>(-123456L);

        groupHelper.removeJvmFromGroup(nonExistentGroup,
                                  preCreatedJvm.getId(),
                                  userId);
    }

    @Test
    public void testRemoveJvmFromGroup() {

        final Identifier<Group> groupId = preCreatedGroup.getId();
        final Identifier<Jvm> jvmId = preCreatedJvm.getId();

        groupHelper.addJvmToGroup(groupId,
                             jvmId,
                             userId);

        groupHelper.removeJvmFromGroup(groupId,
                                  jvmId,
                                  userId);
    }

    @Test(expected = NotFoundException.class)
    public void testRemoveNonExistentJvmFromGroup() {

        final Identifier<Jvm> nonExistentJvm = new Identifier<>(-123456L);

        groupHelper.removeJvmFromGroup(preCreatedGroup.getId(),
                                  nonExistentJvm,
                                  userId);
    }

    @Test (expected = GroupException.class)
    public void testRemoveGroupWithRelationships() {

        final Jvm aSecondJvm = jvmHelper.createJvm("anotherJvmName", "anotherJvmHostName", 5, 4, 3, 2, 1, userId,
                new Path("/abc"), "EXAMPLE_OPTS=%someEnv%/someVal", null, null,  null, null);

        final Identifier<Group> groupId = preCreatedGroup.getId();

        groupHelper.addJvmToGroup(groupId,
                             preCreatedJvm.getId(),
                             userId);

        groupHelper.addJvmToGroup(groupId,
                             aSecondJvm.getId(),
                             userId);

        applicationPersistenceService.removeApplication(application.getId());
        groupPersistenceService.removeGroup(groupId);
    }

    @Test
    public void testGetGroupWithWebServers() {
        Group group = groupPersistenceService.getGroupWithWebServers(preCreatedGroup.getId());
        assertTrue(group.getWebServers().size() == 0);
    }

    @Test
    public void testGetGroupByName() {
        Group group = groupPersistenceService.getGroup(preCreatedGroup.getName());
        assertEquals(preCreatedGroup, group);
    }

    @Test
    public void testGetGroupById() {
        Group group = groupPersistenceService.getGroup(preCreatedGroup.getId(), true);
        assertEquals(preCreatedGroup.getName(), group.getName());
    }

    @Test
    public void testGetGroupsFetchWebServers() {
        List<Group> group = groupPersistenceService.getGroups(true);
        assertTrue(group.size() > 0);
    }

    @Test
    public void testRemoveGroupId() {
        List<Group> groups = groupPersistenceService.getGroups(true);
        assertEquals(1, groups.size());
        CreateGroupRequest createReq = new CreateGroupRequest("removeME");
        Group removeME = groupPersistenceService.createGroup(createReq);
        groups = groupPersistenceService.getGroups(true);
        assertEquals(2, groups.size());
        groupPersistenceService.removeGroup(removeME.getId());
        groups = groupPersistenceService.getGroups(true);
        assertEquals(1, groups.size());
    }

    @Test
    public void testRemoveGroupByName() {
        List<Group> groups = groupPersistenceService.getGroups(true);
        assertEquals(1, groups.size());
        CreateGroupRequest createReq = new CreateGroupRequest("removeME");
        Group removeME = groupPersistenceService.createGroup(createReq);
        groups = groupPersistenceService.getGroups(true);
        assertEquals(2, groups.size());
        groupPersistenceService.removeGroup(removeME.getName());
        groups = groupPersistenceService.getGroups(true);
        assertEquals(1, groups.size());
    }

    @Test
    public void testPopuplateJvmConfig() {
        List<UploadJvmTemplateRequest> uploadCommands = new ArrayList<>();
        groupPersistenceService.populateJvmConfig(preCreatedGroup.getId(), uploadCommands, new User("user"), true);
    }

    @Test
    public void testPopulateGroupJvmTemplates() throws FileNotFoundException {
        List<UploadJvmTemplateRequest> uploadCommands = new ArrayList<UploadJvmTemplateRequest>();
        InputStream data = new FileInputStream(new File("./src/test/resources/ServerXMLTemplate.tpl"));
        Scanner scanner = new Scanner(data).useDelimiter("\\A");
        String templateContent = scanner.hasNext() ? scanner.next() : "";

        UploadJvmTemplateRequest request = new UploadJvmTemplateRequest(preCreatedJvm, "ServerXMLTemplate.tpl", templateContent, StringUtils.EMPTY) {
            @Override
            public String getConfFileName() {
                return "server.xml";
            }
        };
        uploadCommands.add(request);
        groupPersistenceService.populateGroupJvmTemplates(preCreatedGroup.getName(), uploadCommands);
    }

    @Test
    public void testPopulateGroupWebServerTemplates() throws FileNotFoundException {
        Map<String, UploadWebServerTemplateRequest> uploadCommands = new HashMap<>();
        InputStream data = new FileInputStream(new File("./src/test/resources/HttpdSslConfTemplate.tpl"));
        Scanner scanner = new Scanner(data).useDelimiter("\\A");
        String templateContent = scanner.hasNext() ? scanner.next() : "";

        WebServer webServer = new WebServer(new Identifier<WebServer>(1L), new HashSet<Group>(),"testWebServer");
        UploadWebServerTemplateRequest request = new UploadWebServerTemplateRequest(webServer, "HttpdSslConfTemplate.tpl", StringUtils.EMPTY, templateContent) {
            @Override
            public String getConfFileName() {
                return "httpd.conf";
            }
        };
        uploadCommands.put("httpd.conf", request);
        groupPersistenceService.populateGroupWebServerTemplates(preCreatedGroup.getName(), uploadCommands);
    }

    @Test
    public void testGetGroupJvmTemplateResourceNames() {
        List<String> list = groupPersistenceService.getGroupJvmsResourceTemplateNames(preCreatedGroup.getName());
        assertEquals(0, list.size());
    }

    @Test
    public void testGetGroupWebServerTemplateResourceNames() {
        List<String> list = groupPersistenceService.getGroupWebServersResourceTemplateNames(preCreatedGroup.getName());
        assertEquals(0, list.size());
    }

    @Test
    public void testUpdateGroupJvmResourceTemplate() throws FileNotFoundException {
        testPopulateGroupJvmTemplates();
        String content = groupPersistenceService.updateGroupJvmResourceTemplate(preCreatedGroup.getName(), "server.xml",
                "new server.xml content");
        assertEquals("new server.xml content", content);
        content = groupPersistenceService.getGroupJvmResourceTemplate(preCreatedGroup.getName(), "server.xml");
        assertEquals("new server.xml content", content);

    }

    @Test
    public void testUpdateGroupWebServerResourceTemplate() throws FileNotFoundException {
        testPopulateGroupWebServerTemplates();
        String content = groupPersistenceService.updateGroupWebServerResourceTemplate(preCreatedGroup.getName(), "httpd.conf", "now this is the httpd.conf");
        assertEquals("now this is the httpd.conf", content);
        content = groupPersistenceService.getGroupWebServerResourceTemplate(preCreatedGroup.getName(), "httpd.conf");
        assertEquals("now this is the httpd.conf", content);
    }

    @Test
    public void testPopulateGroupAppTemplate() {
        ConfigTemplate template = groupPersistenceService.populateGroupAppTemplate(preCreatedGroup.getName(), "testApp",
                "app.xml", "some meta data", "app content");
        assertNotNull(template);
    }
}
