package com.cerner.jwala.service.group.impl;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.exception.ApplicationException;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.exception.NotFoundException;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.request.group.*;
import com.cerner.jwala.common.request.jvm.UploadJvmTemplateRequest;
import com.cerner.jwala.persistence.service.ApplicationPersistenceService;
import com.cerner.jwala.persistence.service.GroupPersistenceService;
import com.cerner.jwala.service.VerificationBehaviorSupport;
import com.cerner.jwala.service.group.GroupService;
import com.cerner.jwala.service.jvm.JvmService;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.service.resource.impl.ResourceGeneratorType;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.persistence.EntityExistsException;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ExecutorService;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {GroupServiceImplVerifyTest.Config.class})
public class GroupServiceImplVerifyTest extends VerificationBehaviorSupport {

    @Autowired
    private GroupServiceImpl groupService;

    private User user;

    public GroupServiceImplVerifyTest() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH,
                this.getClass().getClassLoader().getResource("vars.properties").getPath()
                        .replace("vars.properties", ""));
    }

    @Before
    public void setUp() {
        user = new User("unused");
        reset(Config.mockGroupPersistenceService);
    }

    @Test
    public void testCreateGroup() {

        final CreateGroupRequest command = new CreateGroupRequest("Group");
        when(Config.mockGroupPersistenceService.getGroup(anyString())).thenThrow(NotFoundException.class);
        groupService.createGroup(command,
                user);

        verify(Config.mockGroupPersistenceService, times(1)).createGroup(command);
    }

    @Test(expected = EntityExistsException.class)
    public void testCreateGroupException() {
        Group group = mock(Group.class);
        final CreateGroupRequest command = new CreateGroupRequest("Group");
        when(Config.mockGroupPersistenceService.getGroup(anyString())).thenReturn(group);
        groupService.createGroup(command,
                user);

        verify(Config.mockGroupPersistenceService, times(1)).createGroup(command);
    }

    @Test
    public void testGetGroup() {

        final Identifier<Group> id = new Identifier<>(-123456L);

        groupService.getGroup(id);

        verify(Config.mockGroupPersistenceService, times(1)).getGroup(eq(id));
    }

    @Test
    public void testGetGroups() {

        groupService.getGroups();

        verify(Config.mockGroupPersistenceService, times(1)).getGroups();
    }

    @Test
    public void testFindGroups() {

        final String fragment = "unused";

        groupService.findGroups(fragment);

        verify(Config.mockGroupPersistenceService, times(1)).findGroups(eq(fragment));
    }

    @Test(expected = BadRequestException.class)
    public void testFindGroupsWithBadName() {

        final String badFragment = "";

        groupService.findGroups(badFragment);
    }

    @Test
    public void testUpdateGroup() throws InterruptedException {
        Group mockGroup = mock(Group.class);
        final Identifier<Group> id = new Identifier<>(-123456L);
        final UpdateGroupRequest updateGroupRequest = new UpdateGroupRequest(id, "testGroup");
        when(Config.mockGroupPersistenceService.getGroup(any(Identifier.class))).thenReturn(mockGroup);
        when(mockGroup.getName()).thenReturn("testGroup");
        when(Config.mockGroupPersistenceService.getGroup(anyString())).thenThrow(NotFoundException.class);
        groupService.updateGroup(updateGroupRequest, user);
        
        verify(Config.mockGroupPersistenceService).updateGroup(updateGroupRequest);
    }

    @Test(expected = EntityExistsException.class)
    public void testUpdateGroupException() throws InterruptedException {
        Group mockGroup = mock(Group.class);
        final Identifier<Group> id = new Identifier<>(-123456L);
        final UpdateGroupRequest updateGroupRequest = new UpdateGroupRequest(id, "testGroup");
        when(Config.mockGroupPersistenceService.getGroup(anyString())).thenReturn(mockGroup);
        when(Config.mockGroupPersistenceService.getGroup(any(Identifier.class))).thenReturn(mockGroup);
        when(mockGroup.getName()).thenReturn("testGroup1");
        groupService.updateGroup(updateGroupRequest, user);

        verify(Config.mockGroupPersistenceService).updateGroup(updateGroupRequest);
    }

    @Test
    public void testRemoveGroup() {
        final Identifier<Group> id = new Identifier<>(-123456L);
        groupService.removeGroup(id);
        verify(Config.mockGroupPersistenceService, times(1)).removeGroup(eq(id));
    }

    @Test
    public void testAddJvmToGroup() {
        final AddJvmToGroupRequest addJvmToGroupRequest = mock(AddJvmToGroupRequest.class);
        groupService.addJvmToGroup(addJvmToGroupRequest, user);
        verify(addJvmToGroupRequest, times(1)).validate();
        verify(Config.mockGroupPersistenceService, times(1)).addJvmToGroup(addJvmToGroupRequest);
    }

    @Test
    public void testAddJvmsToGroup() {

        final AddJvmsToGroupRequest addJvmsToGroupRequest = mock(AddJvmsToGroupRequest.class);

        final Set<AddJvmToGroupRequest> addJvmToGroupRequests = createMockedAddRequests(5);
        when(addJvmsToGroupRequest.toRequests()).thenReturn(addJvmToGroupRequests);

        groupService.addJvmsToGroup(addJvmsToGroupRequest, user);

        verify(addJvmsToGroupRequest, times(1)).validate();
        for (final AddJvmToGroupRequest addJvmToGroupRequest : addJvmToGroupRequests) {
            verify(addJvmToGroupRequest, times(1)).validate();
            verify(Config.mockGroupPersistenceService, times(1)).addJvmToGroup(addJvmToGroupRequest);
        }
    }

    @Test
    public void testRemoveJvmFromGroup() {

        final RemoveJvmFromGroupRequest removeJvmFromGroupRequest = mock(RemoveJvmFromGroupRequest.class);

        groupService.removeJvmFromGroup(removeJvmFromGroupRequest, user);

        verify(removeJvmFromGroupRequest, times(1)).validate();
        verify(Config.mockGroupPersistenceService, times(1)).removeJvmFromGroup(removeJvmFromGroupRequest);

        // TODO: Remove if this is no londer needed.
        // verify(stateNotificationWorker).refreshState(eq(groupStateService), any(Group.class));
    }

    @Test
    public void testGetOtherGroupingDetailsOfJvms() {
        final Set<Group> groupSet = new HashSet<>();
        groupSet.add(new Group(new Identifier<Group>("1"), "Group1"));
        groupSet.add(new Group(new Identifier<Group>("2"), "Group2"));
        groupSet.add(new Group(new Identifier<Group>("3"), "Group3"));

        final Set<Jvm> jvmSet = new HashSet<>();
        jvmSet.add(new Jvm(new Identifier<Jvm>("1"), "Jvm1", groupSet));

        final Group group = new Group(new Identifier<Group>("1"), "Group1", jvmSet);

        when(Config.mockGroupPersistenceService.getGroup(any(Identifier.class), eq(false))).thenReturn(group);

        final List<Jvm> otherGroupingDetailsOfJvm = groupService.getOtherGroupingDetailsOfJvms(new Identifier<Group>("1"));

        assertTrue(otherGroupingDetailsOfJvm.size() == 1);
        assertEquals(otherGroupingDetailsOfJvm.get(0).getGroups().size(), 2);

        String groupNames = "";
        for (Group grp : otherGroupingDetailsOfJvm.get(0).getGroups()) {
            groupNames += grp.getName();
        }

        assertTrue("Group3Group2".equalsIgnoreCase(groupNames) || "Group2Group3".equalsIgnoreCase(groupNames));
    }

    @Test
    public void testGetOtherGroupingDetailsOfWebServers() {
        final List<Group> groupSet = new ArrayList<>();
        groupSet.add(new Group(new Identifier<Group>("2"), "Group2"));
        groupSet.add(new Group(new Identifier<Group>("3"), "Group3"));

        final Set<WebServer> webServerSet = new HashSet<>();
        webServerSet.add(new WebServer(new Identifier<WebServer>("1"), groupSet, "WebServer1", null, null, null, null,
                WebServerReachableState.WS_UNREACHABLE, null));

        groupSet.add(new Group(new Identifier<Group>("1"), "Group1", new HashSet<Jvm>(), webServerSet, null, null));

        when(Config.mockGroupPersistenceService.getGroup(any(Identifier.class), eq(true))).thenReturn(groupSet.get(2));

        final List<WebServer> otherGroupingDetailsOfWebServer =
                groupService.getOtherGroupingDetailsOfWebServers(new Identifier<Group>("1"));
        assertTrue(otherGroupingDetailsOfWebServer.size() == 1);

        String groupNames = "";
        for (Group grp : otherGroupingDetailsOfWebServer.get(0).getGroups()) {
            groupNames += grp.getName();
        }

        assertTrue("Group3Group2".equalsIgnoreCase(groupNames) || "Group2Group3".equalsIgnoreCase(groupNames));
    }

    @Test
    public void testPopulateJvmConfig() throws FileNotFoundException {
        List<UploadJvmTemplateRequest> uploadRequests = new ArrayList<>();
        String templateContent = "<server>content</server>";

        UploadJvmTemplateRequest uploadJvmRequest = new UploadJvmTemplateRequest(new Jvm(new Identifier<Jvm>(11L), "testJvm",
                new HashSet<Group>()), "ServerXMLTemplate.tpl", templateContent, StringUtils.EMPTY) {
            @Override
            public String getConfFileName() {
                return "server.xml";
            }
        };
        uploadRequests.add(uploadJvmRequest);
        final Identifier<Group> aGroupId = new Identifier<>(11L);
        groupService.populateJvmConfig(aGroupId, uploadRequests, user, false);
        verify(Config.mockGroupPersistenceService, times(1)).populateJvmConfig(aGroupId, uploadRequests, user, false);
    }

    @Test
    public void testGroupJvmsResourceTemplateNames() {
        groupService.getGroupJvmsResourceTemplateNames("testGroupName");
        verify(Config.mockGroupPersistenceService, times(1)).getGroupJvmsResourceTemplateNames("testGroupName");


        List<String> jvmTemplates = new ArrayList<>();
        jvmTemplates.add("server.xml");
        when(Config.mockGroupPersistenceService.getGroupJvmsResourceTemplateNames(anyString())).thenReturn(jvmTemplates);
        List<String> templateNames = groupService.getGroupJvmsResourceTemplateNames("testGroupName");
        assertEquals(1, templateNames.size());
    }

    @Test
    public void testGroupWebServersResourceTemplateNames() {
        groupService.getGroupWebServersResourceTemplateNames("testGroupName");
        verify(Config.mockGroupPersistenceService, times(1)).getGroupWebServersResourceTemplateNames("testGroupName");
    }

    @Test
    public void testGetGroupJvmResourceTemplate() {
        reset(Config.mockGroupPersistenceService);

        groupService.getGroupJvmResourceTemplate("testGroupName", "server.xml", new ResourceGroup(), false);
        verify(Config.mockGroupPersistenceService, times(1)).getGroupJvmResourceTemplate("testGroupName", "server.xml");
        verify(Config.mockGroupPersistenceService, times(0)).getGroup("testGroupName");

        reset(Config.mockGroupPersistenceService, Config.mockResourceService);

        Group mockGroup = mock(Group.class);
        Set<Jvm> jvms = new HashSet<>();
        Set<Group> groups = new HashSet<>();
        groups.add(mockGroup);
        jvms.add(new Jvm(new Identifier<Jvm>(111L), "testJvmName", groups));
        when(mockGroup.getJvms()).thenReturn(jvms);
        when(Config.mockGroupPersistenceService.getGroup(anyString())).thenReturn(mockGroup);
        when(Config.mockGroupPersistenceService.getGroupJvmResourceTemplate(anyString(), anyString())).thenReturn("${jvm.jvmName}");

        groupService.getGroupJvmResourceTemplate("testGroupName", "server.xml", new ResourceGroup(), true);

        verify(Config.mockGroupPersistenceService).getGroupJvmResourceTemplate(eq("testGroupName"), eq("server.xml"));
        verify(Config.mockGroupPersistenceService).getGroup(eq("testGroupName"));
        verify(Config.mockResourceService).generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), any(Jvm.class), any(ResourceGeneratorType.class));
    }

    @Test
    public void testGetGroupWebServerResourceTemplate() {
        groupService.getGroupWebServerResourceTemplate("testGroupName", "httpd.conf", false, new ResourceGroup());
        verify(Config.mockGroupPersistenceService, times(1)).getGroupWebServerResourceTemplate("testGroupName", "httpd.conf");
        verify(Config.mockGroupPersistenceService, times(0)).getGroup("testGroupName");

        reset(Config.mockGroupPersistenceService, Config.mockResourceService);

        Group mockGroup = mock(Group.class);
        Set<WebServer> webServers = new HashSet<>();
        Set<Group> groups = new HashSet<>();
        final WebServer testWebServer = new WebServer(new Identifier<WebServer>(111L), groups, "testWebServerName");
        webServers.add(testWebServer);
        when(mockGroup.getWebServers()).thenReturn(webServers);
        when(mockGroup.getId()).thenReturn(new Identifier<Group>(111L));
        when(Config.mockGroupPersistenceService.getGroup(anyString())).thenReturn(mockGroup);
        when(Config.mockGroupPersistenceService.getGroupWithWebServers(any(Identifier.class))).thenReturn(mockGroup);
        when(Config.mockGroupPersistenceService.getGroupWebServerResourceTemplate(anyString(), anyString())).thenReturn("${webServer.name}");

        groupService.getGroupWebServerResourceTemplate("testGroupName", "httpd.conf", true, new ResourceGroup());

        verify(Config.mockGroupPersistenceService).getGroupWebServerResourceTemplate(eq("testGroupName"), eq("httpd.conf"));
        verify(Config.mockGroupPersistenceService).getGroup(eq("testGroupName"));
        verify(Config.mockGroupPersistenceService).getGroupWithWebServers(any(Identifier.class));
        verify(Config.mockResourceService).generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), any(WebServer.class), any(ResourceGeneratorType.class));
    }

    @Test
    public void testUpdateGroupJvmTemplate() {
        groupService.updateGroupJvmResourceTemplate("testGroupName", "server.xml", "${jvm.jvmName}");
        verify(Config.mockGroupPersistenceService).updateGroupJvmResourceTemplate("testGroupName", "server.xml", "${jvm.jvmName}");
    }

    @Test
    public void testUpdateGroupWebServerTemplate() {
        groupService.updateGroupWebServerResourceTemplate("testGroupName", "httpd.conf", "${webServer.name}");
        verify(Config.mockGroupPersistenceService).updateGroupWebServerResourceTemplate("testGroupName", "httpd.conf", "${webServer.name}");
    }

    @Test
    public void testPreviewWebServerTemplate() {
        Group mockGroup = mock(Group.class);
        Set<WebServer> wsList = new HashSet<>();
        WebServer webServer = new WebServer(new Identifier<WebServer>(111L), new HashSet<Group>(), "testWebServerName");
        when(Config.mockGroupPersistenceService.getGroup(anyString())).thenReturn(mockGroup);
        when(Config.mockGroupPersistenceService.getGroupWithWebServers(any(Identifier.class))).thenReturn(mockGroup);
        when(mockGroup.getWebServers()).thenReturn(wsList);
        String template = groupService.previewGroupWebServerResourceTemplate("myFile", "testGroupName", "${webServer.name}", new ResourceGroup());
        assertEquals("${webServer.name}", template);

        reset(Config.mockGroupPersistenceService, Config.mockResourceService);
        when(Config.mockGroupPersistenceService.getGroup(anyString())).thenReturn(mockGroup);
        when(Config.mockGroupPersistenceService.getGroupWithWebServers(any(Identifier.class))).thenReturn(mockGroup);

        wsList.add(webServer);
        groupService.previewGroupWebServerResourceTemplate("myFile", "testGroupName", "${webServer.name}", new ResourceGroup());

        verify(Config.mockGroupPersistenceService).getGroup(eq("testGroupName"));
        verify(Config.mockGroupPersistenceService).getGroupWithWebServers(any(Identifier.class));
        verify(Config.mockResourceService).generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), any(WebServer.class), any(ResourceGeneratorType.class));
    }

    @Test
    public void testGetGroupWithWebServer() {
        final Identifier<Group> aGroupId = new Identifier<>(1212L);
        groupService.getGroupWithWebServers(aGroupId);
        verify(Config.mockGroupPersistenceService).getGroupWithWebServers(aGroupId);
    }

    @Test
    public void testGetGroupName() {
        final String testGroupName = "testGroupName";
        groupService.getGroup(testGroupName);
        verify(Config.mockGroupPersistenceService).getGroup(testGroupName);
    }

    @Test
    public void testGetGroupsFetchWebServers() {
        groupService.getGroups(true);
        verify(Config.mockGroupPersistenceService).getGroups(true);
    }

    @Test
    public void testRemoveGroupName() {
        Group mockGroup = mock(Group.class);
        when(mockGroup.getJvms()).thenReturn(new HashSet<Jvm>());
        when(mockGroup.getApplications()).thenReturn(new HashSet<Application>());
        Group mockGroupWithWS = mock(Group.class);
        when(mockGroupWithWS.getWebServers()).thenReturn(new HashSet<WebServer>());

        when(Config.mockGroupPersistenceService.getGroup(anyString())).thenReturn(mockGroup);
        when(Config.mockGroupPersistenceService.getGroupWithWebServers(any(Identifier.class))).thenReturn(mockGroupWithWS);

        final String testGroupName = "testGroupName";
        groupService.removeGroup(testGroupName);
        verify(Config.mockGroupPersistenceService).removeGroup(testGroupName);
    }

    @Test
    public void testGetGroupAppTemplateNames() {
        when(Config.mockGroupPersistenceService.getGroupAppsResourceTemplateNames(anyString())).thenReturn(new ArrayList<String>());
        groupService.getGroupAppsResourceTemplateNames("testGroup");
        verify(Config.mockGroupPersistenceService).getGroupAppsResourceTemplateNames("testGroup");
    }

    @Test
    public void testUpdateGroupAppResourceTemplate() {
        when(Config.mockGroupPersistenceService.updateGroupAppResourceTemplate(anyString(), eq("some-app-name"), anyString(),
                anyString())).thenReturn("template content");
        groupService.updateGroupAppResourceTemplate("testGroup", "some-app-name", "hct.xml", "template content");
        verify(Config.mockGroupPersistenceService).updateGroupAppResourceTemplate("testGroup", "some-app-name", "hct.xml", "template content");
    }

    @Test(expected = ApplicationException.class)
    public void testPreviewGroupAppTemplate() {
        Group mockGroup = mock(Group.class);
        Set<Jvm> jvmsList = new HashSet<>();
        List<Application> appList = new ArrayList<>();
        Jvm mockJvm = mock(Jvm.class);
        jvmsList.add(mockJvm);
        Application mockApp = mock(Application.class);
        appList.add(mockApp);
        when(mockJvm.getJvmName()).thenReturn("testJvm");
        when(mockApp.getWebAppContext()).thenReturn("/hct");
        when(mockApp.getId()).thenReturn(new Identifier<Application>(99L));
        when(mockApp.getName()).thenReturn("testApp");
        when(mockApp.getWarPath()).thenReturn("./");
        when(mockApp.getGroup()).thenReturn(mockGroup);
        when(mockApp.isSecure()).thenReturn(true);
        when(mockApp.getWarName()).thenReturn("testApp.war");
        when(mockApp.isLoadBalanceAcrossServers()).thenReturn(true);
        when(mockGroup.getJvms()).thenReturn(jvmsList);
        when(Config.mockGroupPersistenceService.getGroup(anyString())).thenReturn(mockGroup);
        when(Config.mockApplicationPersistenceService.getApplications()).thenReturn(appList);
        when(Config.mockApplicationPersistenceService.findApplication(anyString(), anyString(), anyString())).thenReturn(mockApp);
        when(Config.mockApplicationPersistenceService.getApplication(anyString())).thenReturn(mockApp);
        when(Config.mockGroupPersistenceService.getGroupAppResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"entity\":{\"target\": \"testApp\"}}");

        String content = groupService.previewGroupAppResourceTemplate("testGroup", "hct.xml", "hct content", new ResourceGroup());
        assertEquals("hct content", content);

        groupService.previewGroupAppResourceTemplate("testGroup", "hct.xml", "hct content ${webApp.fail.token}", new ResourceGroup());
    }

    @Test
    public void testGetGroupAppResourceTemplate() {
        Group mockGroup = mock(Group.class);
        Set<Jvm> jvmsList = new HashSet<>();
        List<Application> appList = new ArrayList<>();
        Jvm mockJvm = mock(Jvm.class);
        jvmsList.add(mockJvm);
        Application mockApp = mock(Application.class);
        appList.add(mockApp);
        when(mockJvm.getJvmName()).thenReturn("testJvm");
        when(mockApp.getWebAppContext()).thenReturn("/hct");
        when(mockApp.getId()).thenReturn(new Identifier<Application>(99L));
        when(mockApp.getName()).thenReturn("testApp");
        when(mockApp.getWarPath()).thenReturn("./");
        when(mockApp.getGroup()).thenReturn(mockGroup);
        when(mockApp.isSecure()).thenReturn(true);
        when(mockApp.getWarName()).thenReturn("testApp.war");
        when(mockApp.isLoadBalanceAcrossServers()).thenReturn(true);
        when(mockGroup.getJvms()).thenReturn(jvmsList);
        when(Config.mockGroupPersistenceService.getGroup(anyString())).thenReturn(mockGroup);
        when(Config.mockGroupPersistenceService.getGroupAppResourceTemplate(anyString(), anyString(), anyString())).thenReturn("hct content ${webApp.name}");
        when(Config.mockApplicationPersistenceService.getApplications()).thenReturn(appList);
        when(Config.mockApplicationPersistenceService.findApplication(anyString(), anyString(), anyString())).thenReturn(mockApp);
        when(Config.mockApplicationPersistenceService.getApplication(anyString())).thenReturn(mockApp);

        String content = groupService.getGroupAppResourceTemplate("testGroup", "testAppName", "hct.xml", false, new ResourceGroup());
        assertEquals("hct content ${webApp.name}", content);

        reset(Config.mockGroupPersistenceService, Config.mockApplicationPersistenceService, Config.mockResourceService);

        when(Config.mockGroupPersistenceService.getGroupAppResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"entity\":{\"target\": \"testApp\"}}");
        when(Config.mockGroupPersistenceService.getGroups()).thenReturn(Collections.singletonList(mockGroup));
        groupService.getGroupAppResourceTemplate("testGroup", "testAppName", "hct.xml", true, new ResourceGroup());

        verify(Config.mockGroupPersistenceService).getGroupAppResourceTemplate(eq("testGroup"), eq("testAppName"), eq("hct.xml"));
        verify(Config.mockApplicationPersistenceService).getApplication(eq("testAppName"));
        verify(Config.mockResourceService).generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), any(Application.class), any(ResourceGeneratorType.class));

        when(Config.mockGroupPersistenceService.getGroupAppResourceTemplate(anyString(), anyString(), anyString())).thenReturn("hct content ${webApp.fail.name}");
        try {
            groupService.getGroupAppResourceTemplate("testGroup", "testAppName", "hct.xml", true, new ResourceGroup());
        } catch (ApplicationException ae) {
            assertEquals("Template token replacement failed.", ae.getMessage());
        } catch (Exception e) {
            assertFalse("Expecting ApplicationException but got " + e, true);
        }
    }

    @Test
    public void testGetAllHosts() {
        List<Group> mockGroupsList = new ArrayList<>();
        Group mockGroup1 = mock(Group.class);
        Group mockGroup2 = mock(Group.class);
        mockGroupsList.add(mockGroup1);
        mockGroupsList.add(mockGroup2);

        List<String> group1Hosts = new ArrayList<>();
        group1Hosts.add("host1");
        group1Hosts.add("host2");
        List<String> group2Hosts = new ArrayList<>();
        group2Hosts.add("host2");
        group2Hosts.add("host3");

        when(Config.mockGroupPersistenceService.getGroups()).thenReturn(mockGroupsList);
        when(mockGroup1.getName()).thenReturn("mockGroup1");
        when(mockGroup2.getName()).thenReturn("mockGroup2");
        when(Config.mockGroupPersistenceService.getHosts(eq("mockGroup1"))).thenReturn(group1Hosts);
        when(Config.mockGroupPersistenceService.getHosts(eq("mockGroup2"))).thenReturn(group2Hosts);

        List<String> result = groupService.getAllHosts();
        assertEquals(3, result.size());
    }

    static class Config {

        private static JvmService mockJvmService = mock(JvmService.class);
        private static ExecutorService mockExecutorService = mock(ExecutorService.class);
        private static ResourceService mockResourceService = mock(ResourceService.class);
        private static ApplicationPersistenceService mockApplicationPersistenceService = mock(ApplicationPersistenceService.class);
        private static GroupPersistenceService mockGroupPersistenceService = mock(GroupPersistenceService.class);

        @Bean
        public JvmService getJvmService() {
            return mockJvmService;
        }

        @Bean
        public ExecutorService getExecutorService() {
            return mockExecutorService;
        }

        @Bean
        public ResourceService getResourceService() {
            return mockResourceService;
        }

        @Bean
        public ApplicationPersistenceService getApplicationPersistenceService() {
            return mockApplicationPersistenceService;
        }

        @Bean
        public GroupPersistenceService getGroupPersistenceService() {
            return mockGroupPersistenceService;
        }

        @Bean
        public GroupService getGroupService() {
            return new GroupServiceImpl(Config.mockGroupPersistenceService, mockApplicationPersistenceService, mockResourceService);
        }
    }
}
