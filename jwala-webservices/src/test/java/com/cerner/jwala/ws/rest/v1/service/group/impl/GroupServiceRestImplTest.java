package com.cerner.jwala.ws.rest.v1.service.group.impl;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.group.GroupControlOperation;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.resource.ResourceContent;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerControlOperation;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.request.group.AddJvmsToGroupRequest;
import com.cerner.jwala.common.request.group.CreateGroupRequest;
import com.cerner.jwala.common.request.group.RemoveJvmFromGroupRequest;
import com.cerner.jwala.common.request.group.UpdateGroupRequest;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.cerner.jwala.persistence.service.GroupPersistenceService;
import com.cerner.jwala.service.HistoryFacadeService;
import com.cerner.jwala.service.HistoryService;
import com.cerner.jwala.service.app.ApplicationService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionService;
import com.cerner.jwala.service.exception.GroupServiceException;
import com.cerner.jwala.service.group.GroupControlService;
import com.cerner.jwala.service.group.GroupJvmControlService;
import com.cerner.jwala.service.group.GroupService;
import com.cerner.jwala.service.group.GroupWebServerControlService;
import com.cerner.jwala.service.group.impl.GroupControlServiceImpl;
import com.cerner.jwala.service.group.impl.GroupJvmControlServiceImpl;
import com.cerner.jwala.service.jvm.JvmService;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.service.resource.impl.ResourceGeneratorType;
import com.cerner.jwala.service.webserver.WebServerCommandService;
import com.cerner.jwala.service.webserver.WebServerControlService;
import com.cerner.jwala.service.webserver.WebServerService;
import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;
import com.cerner.jwala.ws.rest.v1.provider.NameSearchParameterProvider;
import com.cerner.jwala.ws.rest.v1.response.ApplicationResponse;
import com.cerner.jwala.ws.rest.v1.service.app.ApplicationServiceRest;
import com.cerner.jwala.ws.rest.v1.service.group.GroupChildType;
import com.cerner.jwala.ws.rest.v1.service.group.MembershipDetails;
import com.cerner.jwala.ws.rest.v1.service.jvm.impl.JsonControlJvm;
import com.cerner.jwala.ws.rest.v1.service.webserver.WebServerServiceRest;
import com.cerner.jwala.ws.rest.v1.service.webserver.impl.JsonControlWebServer;
import com.cerner.jwala.ws.rest.v1.service.webserver.impl.WebServerServiceRestImpl;
import org.apache.openjpa.persistence.EntityExistsException;
import org.apache.tika.mime.MediaType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import javax.servlet.ServletInputStream;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link GroupServiceRestImpl}.
 *
 * @author meleje00
 */

public class GroupServiceRestImplTest {

    private static final String GROUP_CONTROL_TEST_USERNAME = "groupControlTest";
    private static final String name = "groupName";
    private static final List<Group> groupList = createGroupList();
    private static final Group group = groupList.get(0);

    @Mock
    private GroupService mockGroupService;

    @Mock
    private ResourceService mockResourceService;

    @Mock
    private JvmService mockJvmService;

    @Mock
    private WebServerService mockWebServerService;

    @Mock
    private GroupControlServiceImpl mockControlImpl;

    @Mock
    private GroupJvmControlServiceImpl mockControlJvmImpl;

    @Mock
    private GroupWebServerControlService mockGroupWSControlService;

    @Mock
    private AuthenticatedUser mockAuthenticatedUser;

    @Mock
    private GroupPersistenceService mockGroupPersistenceService;

    @Mock
    private WebServerServiceRest mockWebServerServiceRest;

    @Mock
    private HistoryService mockHistoryService;

    @Mock
    private HistoryFacadeService mockHistoryFacadeService;

    @Mock
    private Jvm mockJvm;

    private List<Jvm> jvms;

    @Mock
    private WebServer mockWebServer;

    private List<WebServer> webServers;

    private Set<Group> groups;

    @Mock
    private Group mockLiteGroup;

    private Set<Group> liteGroups;

    @Mock
    private Group mockGroup;

    @InjectMocks
    private GroupServiceRestImpl groupServiceRest;

    @Mock
    private GroupControlService mockGroupControlService;

    @Mock
    private GroupJvmControlService mockGroupJvmControlService;

    @Mock
    private ApplicationService mockApplicationService;

    @Mock
    private ExecutorService mockExecutorService;

    @Mock
    private WebServerControlService mockWebServerControlService;

    @Mock
    private WebServerCommandService mockWebServerCommandService;

    @Mock
    private BinaryDistributionService mockBinaryDistributionService;

    @Mock
    private ApplicationServiceRest applicationServiceRest;


    private static List<Group> createGroupList() {
        final Group ws = new Group(Identifier.id(1L, Group.class), name);
        final List<Group> result = new ArrayList<>();
        result.add(ws);
        return result;
    }

    public GroupServiceRestImplTest() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH,
                this.getClass().getClassLoader().getResource("vars.properties").getPath().replace("vars.properties", ""));
    }

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(mockAuthenticatedUser.getUser()).thenReturn(new User(GROUP_CONTROL_TEST_USERNAME));

        when(mockJvm.getJvmName()).thenReturn("jvm1");
        when(mockLiteGroup.getName()).thenReturn("group1");
        liteGroups = new HashSet<>();
        liteGroups.add(mockLiteGroup);
        when(mockJvm.getGroups()).thenReturn(liteGroups);
        jvms = new ArrayList<>();
        jvms.add(mockJvm);

        when(mockWebServer.getName()).thenReturn("webServer1");
        when(mockGroup.getName()).thenReturn("group2");
        groups = new HashSet<>();
        groups.add(mockGroup);
        when(mockWebServer.getGroups()).thenReturn(groups);
        webServers = new ArrayList<>();
        webServers.add(mockWebServer);

        mockWebServerService = mock(WebServerService.class);
        mockWebServerControlService = mock(WebServerControlService.class);
        mockWebServerCommandService = mock(WebServerCommandService.class);
        mockResourceService = mock(ResourceService.class);
        mockGroupService = mock(GroupService.class);
        mockBinaryDistributionService = mock(BinaryDistributionService.class);

        groupServiceRest = new GroupServiceRestImpl(mockGroupService, mockResourceService, mockGroupControlService,
                mockGroupJvmControlService, mockGroupWSControlService, mockJvmService, mockWebServerService,
                mockApplicationService, applicationServiceRest, mockWebServerServiceRest);


        final WebServerServiceRest webServerServiceRest = new WebServerServiceRestImpl(mockWebServerService, mockWebServerControlService, mockWebServerCommandService,  mockResourceService, mockGroupService, mockBinaryDistributionService, mockHistoryFacadeService);

        groupServiceRest = new GroupServiceRestImpl(mockGroupService, mockResourceService, mockGroupControlService,
                mockGroupJvmControlService, mockGroupWSControlService, mockJvmService, mockWebServerService,
                mockApplicationService, applicationServiceRest, webServerServiceRest);
    }

    @After
    public void cleanUp() {
        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testGetGroupList() {
        when(mockGroupService.getGroups(eq(false))).thenReturn(groupList);

        final NameSearchParameterProvider nameSearchParameterProvider = new NameSearchParameterProvider();
        final Response response = groupServiceRest.getGroups(nameSearchParameterProvider, false);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof List);

        final List<Group> receivedList = (List<Group>) content;
        final Group received = receivedList.get(0);
        assertEquals(group, received);
    }

    @Test
    public void testGetGroupListName() {
        when(mockGroupService.findGroups(any(String.class))).thenReturn(groupList);

        final NameSearchParameterProvider nameSearchParameterProvider = new NameSearchParameterProvider(name);

        final Response response = groupServiceRest.getGroups(nameSearchParameterProvider, false);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof List);

        final List<Group> receivedList = (List<Group>) content;
        final Group received = receivedList.get(0);
        assertEquals(group, received);
    }

    @Test
    public void testGetGroup() {
        when(mockGroupService.getGroup(any(Identifier.class))).thenReturn(group);

        Response response = groupServiceRest.getGroup("1", false);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof Group);

        final Group received = (Group) content;
        assertEquals(group, received);

        when(mockGroupService.getGroup(anyString())).thenReturn(group);
        response = groupServiceRest.getGroup("1", true);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testCreateGroup() {
        when(mockGroupService.createGroup(any(CreateGroupRequest.class), any(User.class))).thenReturn(group);

        final Response response = groupServiceRest.createGroup(name, mockAuthenticatedUser);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof Group);

        final Group received = (Group) content;
        assertEquals(group, received);
    }

    @Test
    public void testUpdateGroup() {
        final JsonUpdateGroup jsonUpdateGroup = new JsonUpdateGroup("currentName", name);
        when(mockGroupService.updateGroup(any(UpdateGroupRequest.class), any(User.class))).thenReturn(group);
        when(mockGroupService.getGroup(eq("currentName"))).thenReturn(group);

        final Response response = groupServiceRest.updateGroup(jsonUpdateGroup, mockAuthenticatedUser);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();

        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof Group);

        final Group received = (Group) content;
        assertEquals(group, received);
    }

    @Test
    public void testRemoveGroup() {
        Response response = groupServiceRest.removeGroup("groupName", true);
        verify(mockGroupService, atLeastOnce()).removeGroup(eq("groupName"));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        assertNull(applicationResponse);

        response = groupServiceRest.removeGroup("1", false);
        Identifier<Group> groupIdentifier = new Identifier<Group>("1");
        verify(mockGroupService, atLeastOnce()).removeGroup(eq(groupIdentifier));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testAddJvmsToGroup() {
        when(mockGroupService.addJvmsToGroup(any(AddJvmsToGroupRequest.class), any(User.class))).thenReturn(group);

        final Set<String> jvmIds = new HashSet<String>();
        jvmIds.add("1");
        final JsonJvms jsonJvms = new JsonJvms(jvmIds);
        final Response response = groupServiceRest.addJvmsToGroup(Identifier.id(1l, Group.class), jsonJvms, mockAuthenticatedUser);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof Group);

        final Group received = (Group) content;
        assertEquals(group, received);
    }

    @Test
    public void testRemoveJvmsFromGroup() {
        when(mockGroupService.removeJvmFromGroup(any(RemoveJvmFromGroupRequest.class), any(User.class))).thenReturn(group);

        final Response response =
                groupServiceRest.removeJvmFromGroup(Identifier.id(1l, Group.class),
                        Identifier.id(1l, Jvm.class), mockAuthenticatedUser);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof Group);

        final Group received = (Group) content;
        assertEquals(group, received);
    }

    @Test
    public void getOtherGroupMembershipDetailsOfTheChildrenChildTypeNull() {
        when(mockGroupService.getOtherGroupingDetailsOfJvms(any(Identifier.class))).thenReturn(jvms);

        when(mockGroupService.getOtherGroupingDetailsOfWebServers(any(Identifier.class))).thenReturn(webServers);
        final Response response = groupServiceRest.getOtherGroupMembershipDetailsOfTheChildren(new Identifier<Group>("1"), null);
        assertEquals(response.getStatus(), 200);
        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();

        final List<MembershipDetails> membershipDetailsList = (List) applicationResponse.getApplicationResponseContent();
        assertEquals("jvm1", membershipDetailsList.get(0).getName());
        assertEquals("group1", membershipDetailsList.get(0).getGroupNames().get(0));
        assertEquals("webServer1", membershipDetailsList.get(1).getName());
        assertEquals("group2", membershipDetailsList.get(1).getGroupNames().get(0));
    }

    @Test
    public void getOtherGroupMembershipDetailsOfTheChildrenChildTypeJvm() {
        when(mockGroupService.getOtherGroupingDetailsOfJvms(any(Identifier.class))).thenReturn(jvms);
        final Response response =
                groupServiceRest.getOtherGroupMembershipDetailsOfTheChildren(new Identifier<Group>("1"), GroupChildType.JVM);
        assertEquals(response.getStatus(), 200);
        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();

        final List<MembershipDetails> membershipDetailsList = (List) applicationResponse.getApplicationResponseContent();
        assertEquals("jvm1", membershipDetailsList.get(0).getName());
        assertEquals("group1", membershipDetailsList.get(0).getGroupNames().get(0));
    }

    @Test
    public void getOtherGroupMembershipDetailsOfTheChildrenChildTypeWebServer() {
        when(mockGroupService.getOtherGroupingDetailsOfWebServers(any(Identifier.class))).thenReturn(webServers);
        final Response response
                = groupServiceRest.getOtherGroupMembershipDetailsOfTheChildren(new Identifier<Group>("1"), GroupChildType.WEB_SERVER);
        assertEquals(response.getStatus(), 200);
        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final List<MembershipDetails> membershipDetailsList = (List) applicationResponse.getApplicationResponseContent();
        assertEquals("webServer1", membershipDetailsList.get(0).getName());
        assertEquals("group2", membershipDetailsList.get(0).getGroupNames().get(0));
    }

    @Test
    public void testControlGroupWebServers() {
        JsonControlWebServer mockControlWebServer = mock(JsonControlWebServer.class);
        when(mockControlWebServer.toControlOperation()).thenReturn(WebServerControlOperation.START);
        Response response = groupServiceRest.controlGroupWebservers(group.getId(), mockControlWebServer, mockAuthenticatedUser);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void testControlGroup() {
        JsonControlGroup mockControlGroup = mock(JsonControlGroup.class);
        when(mockControlGroup.toControlOperation()).thenReturn(GroupControlOperation.START);
        Response response = groupServiceRest.controlGroup(group.getId(), mockControlGroup, mockAuthenticatedUser);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void testControlGroups() {
        JsonControlGroup mockControlGroup = mock(JsonControlGroup.class);
        when(mockControlGroup.toControlOperation()).thenReturn(GroupControlOperation.START);
        Response response = groupServiceRest.controlGroups(mockControlGroup, mockAuthenticatedUser);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void testControlGroupJvms() {
        Response response = groupServiceRest.controlGroupJvms(group.getId(), new JsonControlJvm(JvmControlOperation.START.getExternalValue()), mockAuthenticatedUser);
        assertNotNull(response);
    }

    @Test
    public void testUpdateWebServerTemplate() {

        Group mockGroupWithWebServers = mock(Group.class);
        when(mockGroupService.updateGroupWebServerResourceTemplate(anyString(), anyString(), anyString())).thenReturn("httpd.conf content for testing");
        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroup);
        when(mockGroupService.getGroupWithWebServers(any(Identifier.class))).thenReturn(mockGroupWithWebServers);
        when(mockGroupWithWebServers.getWebServers()).thenReturn(new HashSet<>(webServers));
        when(mockWebServerService.updateResourceTemplate(anyString(), anyString(), anyString())).thenReturn("httpd.conf content for testing");

        Response response = groupServiceRest.updateGroupWebServerResourceTemplate(group.getName(), "httpd.conf", "httpd.conf content for testing");
        assertNotNull(response);

        when(mockGroupService.updateGroupWebServerResourceTemplate(anyString(), anyString(), anyString())).thenThrow(new ResourceTemplateUpdateException("test webServer", "httpd.conf"));
        response = groupServiceRest.updateGroupWebServerResourceTemplate(group.getName(), "httpd.conf", "httpd.conf content for testing");
        assertNotNull(response);
    }

    @Test
    public void testPreviewWebServerTemplate() {
        Response response = groupServiceRest.previewGroupWebServerResourceTemplate(group.getName(), "myFile", "httpd.conf");
        assertNotNull(response);
    }

    @Test
    public void testPreviewWebServerTemplateThrowsRuntimeException() {
        when(mockGroupService.previewGroupWebServerResourceTemplate(anyString(), anyString(), anyString(), any(ResourceGroup.class))).thenThrow(new RuntimeException());
        Response response = groupServiceRest.previewGroupWebServerResourceTemplate(group.getName(), "myFile", "httpd.conf");
        assertNotNull(response);
        assertTrue(response.getStatus() > 499);
    }

    @Test
    public void testGetWebServerTemplate() {
        Response response = groupServiceRest.getGroupWebServerResourceTemplate(group.getName(), "httpd.conf", false);
        assertNotNull(response);
    }

    @Test
    public void testGenerateAndDeployWebServerFilesNoWebServers() {
        Set<WebServer> emptyWsSet = new HashSet<>();
        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroup);
        when(mockGroupService.getGroupWithWebServers(any(Identifier.class))).thenReturn(mockGroup);
        when(mockGroup.getWebServers()).thenReturn(emptyWsSet);
        Response response = groupServiceRest.generateAndDeployGroupWebServersFile(group.getName(), "httpd.conf", mockAuthenticatedUser);
        assertNotNull(response);
    }

    @Test
    public void testGenerateAndDeployWebServerFiles() throws IOException, CommandFailureException {
        final String rawMetaData = "{\"deployPath\":\"/some/test/path\",\"contentType\":\"text/plain\",\"deployFileName\":\"httpd.conf\"}";
        final String httpdConfTemplateContent = "fake httpd.conf content for testing";
        ResourceTemplateMetaData mockMetaData = mock(ResourceTemplateMetaData.class);
        when(mockMetaData.getContentType()).thenReturn(MediaType.TEXT_PLAIN);
        when(mockMetaData.getDeployFileName()).thenReturn("httpd.conf");
        when(mockMetaData.getDeployPath()).thenReturn("/some/test/path");
        when(mockMetaData.isHotDeploy()).thenReturn(false);

        Set<WebServer> wsSet = new HashSet<>();
        wsSet.add(mockWebServer);

        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroup);
        when(mockGroupService.getGroupWithWebServers(any(Identifier.class))).thenReturn(mockGroup);
        when(mockGroup.getWebServers()).thenReturn(wsSet);
        when(mockWebServerService.getResourceTemplateMetaData(anyString(), anyString())).thenReturn(rawMetaData);
        when(mockWebServerService.getResourceTemplate(anyString(), anyString(), anyBoolean(), any(ResourceGroup.class))).thenReturn(httpdConfTemplateContent);
        when(mockResourceService.updateResourceMetaData(any(ResourceIdentifier.class), anyString(), anyString())).thenReturn(rawMetaData);
        when(mockResourceService.getTokenizedMetaData(anyString(), Matchers.anyObject(), anyString())).thenReturn(mockMetaData);
        when(mockResourceService.getResourceContent(any(ResourceIdentifier.class))).thenReturn(new ResourceContent("{\"test\":\"meta data\"}", "test resource content"));

        Response response = groupServiceRest.generateAndDeployGroupWebServersFile(group.getName(), "httpd.conf", mockAuthenticatedUser);
        assertEquals(200, response.getStatus());
        assertEquals("SUCCESS", ((ApplicationResponse)response.getEntity()).getMessage());
        verify(mockWebServerService).updateResourceTemplate(anyString(), anyString(), anyString());
        verify(mockResourceService).updateResourceMetaData(any(ResourceIdentifier.class), anyString(), anyString());
    }

    @Test
    public void testGetWebserverResourceNames() {
        Response response = groupServiceRest.getGroupWebServersResourceNames(group.getName());
        assertNotNull(response);
    }

    @Test
    public void testGetJvmResourceNames() {
        Response response = groupServiceRest.getGroupJvmsResourceNames(group.getName());
        assertNotNull(response);
    }

    @Test
    public void testUpdateJvmTemplate() {
        Group mockGroupWithJvms = mock(Group.class);
        Set<Jvm> mockJvms = new HashSet<>();
        mockJvms.add(mockJvm);

        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroupWithJvms);
        when(mockGroupWithJvms.getJvms()).thenReturn(mockJvms);

        Response response = groupServiceRest.updateGroupJvmResourceTemplate(group.getName(), "server.xml", "test server.xml content");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        when(mockGroupService.updateGroupJvmResourceTemplate(anyString(), anyString(), anyString())).thenThrow(new ResourceTemplateUpdateException("test jvm", "server.xml"));

        response = groupServiceRest.updateGroupJvmResourceTemplate(group.getName(), "server.xml", "server.xml content for testing");
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());

    }

    @Test
    public void testPreviewGroupJvmTemplate() {
        Set<Jvm> jvmSet = new HashSet<>();
        Jvm mockPreviewJvm = mock(Jvm.class);
        when(mockPreviewJvm.getJvmName()).thenReturn("test-jvm");
        jvmSet.add(mockPreviewJvm);

        Group mockPreviewJvmGroup = mock(Group.class);
        when(mockPreviewJvmGroup.getJvms()).thenReturn(jvmSet);

        final ResourceGroup resourceGroup = new ResourceGroup();
        final String templateContent = "preview template ${jvm.jvmName}";
        final String fileName = "myFile";
        when(mockResourceService.generateResourceGroup()).thenReturn(resourceGroup);
        when(mockGroupService.getGroup(anyString())).thenReturn(mockPreviewJvmGroup);
        groupServiceRest.previewGroupJvmResourceTemplate("test-group-name", fileName, templateContent);

        verify(mockResourceService).generateResourceFile(eq(fileName), eq(templateContent), eq(resourceGroup), eq(mockPreviewJvm), eq(ResourceGeneratorType.PREVIEW));
    }

    @Test
    public void testPreviewGroupJvmTemplateThrowsRuntimeException() {
        Set<Jvm> jvmSet = new HashSet<>();
        Jvm mockPreviewJvm = mock(Jvm.class);
        when(mockPreviewJvm.getJvmName()).thenReturn("test-jvm");
        jvmSet.add(mockPreviewJvm);

        Group mockPreviewJvmGroup = mock(Group.class);
        when(mockPreviewJvmGroup.getJvms()).thenReturn(jvmSet);

        final ResourceGroup resourceGroup = new ResourceGroup();
        final String templateContent = "preview template ${jvm.jvmName}";
        final String fileName = "myFile";
        when(mockResourceService.generateResourceGroup()).thenReturn(resourceGroup);
        when(mockGroupService.getGroup(anyString())).thenReturn(mockPreviewJvmGroup);
        when(mockResourceService.generateResourceFile(fileName, templateContent, resourceGroup, mockPreviewJvm, ResourceGeneratorType.PREVIEW)).thenThrow(new RuntimeException("Fail"));

        Response response = groupServiceRest.previewGroupJvmResourceTemplate("test-group-name", fileName, templateContent);
        assertEquals(500, response.getStatus());
    }

    @Test
    public void testGetJvmTemplate() {
        Response response = groupServiceRest.getGroupJvmResourceTemplate(group.getName(), "server.xml", false);
        assertNotNull(response);
    }

    @Test
    public void testGenerateAndDeployJvmTemplate() {
        Set<WebServer> emptyWsSet = new HashSet<>();
        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroup);
        when(mockGroupService.getGroupWithWebServers(any(Identifier.class))).thenReturn(mockGroup);
        when(mockGroup.getWebServers()).thenReturn(emptyWsSet);
        Response response = groupServiceRest.generateAndDeployGroupJvmFile(group.getName(), "server.xml", mockAuthenticatedUser);
        assertNotNull(response);
    }

    @Test
    public void testUpdateGroupWebServerTemplateNoWebServers() {
        Group mockGroupNoWebServers = mock(Group.class);
        when(mockGroupNoWebServers.getWebServers()).thenReturn(null);
        when(mockGroupService.updateGroupWebServerResourceTemplate(anyString(), anyString(), anyString())).thenReturn("no content updated");
        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroupNoWebServers);
        when(mockGroupService.getGroupWithWebServers(any(Identifier.class))).thenReturn(mockGroupNoWebServers);
        Response response = groupServiceRest.updateGroupWebServerResourceTemplate("groupName", "resourceTemplateName.txt", "no content");
        assertTrue(response.getStatus() > 199 && response.getStatus() < 300);
    }

    @Test
    public void testTestUpdateGroupJvmTemplateNoJvms() {
        when(mockGroupService.updateGroupJvmResourceTemplate(anyString(), anyString(), anyString())).thenReturn("no content updated");
        Group mockGroupNoJvms = mock(Group.class);
        when(mockGroupNoJvms.getJvms()).thenReturn(null);
        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroupNoJvms);
        Response response = groupServiceRest.updateGroupJvmResourceTemplate("groupName", "resourceTemplateName.xml", "no content");
        assertTrue(response.getStatus() > 199 && response.getStatus() < 300);
    }

    @Test
    public void testPreviewGroupAppResourceTemplate() {
        final ResourceGroup resourceGroup = new ResourceGroup();
        when(mockResourceService.generateResourceGroup()).thenReturn(resourceGroup);
        groupServiceRest.previewGroupAppResourceTemplate("testGroup", "hct.xml", "preview me!");
        verify(mockGroupService).previewGroupAppResourceTemplate("testGroup", "hct.xml", "preview me!", resourceGroup);

        when(mockGroupService.previewGroupAppResourceTemplate(anyString(), anyString(), anyString(), any(ResourceGroup.class))).thenThrow(new RuntimeException());
        Response response = groupServiceRest.previewGroupAppResourceTemplate("testGroup", "hct.xml", "preview me!");
        assertTrue(response.getStatus() > 499);
    }

    @Test
    public void testGetGroupAppResourceTemplate() {
        groupServiceRest.getGroupAppResourceTemplate("testGroup", "some-app-name", "hct.xml", false);
        verify(mockGroupService).getGroupAppResourceTemplate(eq("testGroup"), eq("some-app-name"), eq("hct.xml"), eq(false), any(ResourceGroup.class));
    }

    @Test
    public void testGetGroupAppResourceNames() {
        groupServiceRest.getGroupAppResourceNames("testGroup");
        verify(mockGroupService).getGroupAppsResourceTemplateNames("testGroup");
    }

    @Test
    public void testGetStartedWebServersAndJvmCounts() {
        when(mockGroupService.getGroups()).thenReturn(new ArrayList<>(groups));
        when(mockJvmService.getJvmStartedCount(anyString())).thenReturn(0L);
        when(mockJvmService.getJvmCount(anyString())).thenReturn(1L);
        when(mockWebServerService.getWebServerStartedCount(anyString())).thenReturn(0L);
        when(mockWebServerService.getWebServerCount(anyString())).thenReturn(1L);

        Response response = groupServiceRest.getStartedWebServersAndJvmsCount();
        assertNotNull(response.getEntity());

        response = groupServiceRest.getStartedWebServersAndJvmsCount("testGroup");
        assertNotNull(response.getEntity());
    }

    @Test
    public void testGetStoppedWebServersAndJvmsCount() {
        when(mockGroupService.getGroups()).thenReturn(new ArrayList<>(groups));
        when(mockJvmService.getJvmStoppedCount(anyString())).thenReturn(0L);
        when(mockJvmService.getJvmForciblyStoppedCount(anyString())).thenReturn(0L);
        when(mockJvmService.getJvmCount(anyString())).thenReturn(1L);
        when(mockWebServerService.getWebServerStoppedCount(anyString())).thenReturn(0L);
        when(mockWebServerService.getWebServerCount(anyString())).thenReturn(1L);

        Response response = groupServiceRest.getStoppedWebServersAndJvmsCount();
        assertNotNull(response);

        response = groupServiceRest.getStoppedWebServersAndJvmsCount("testGroup");
        assertNotNull(response);

    }

    @Test
    public void testAreAllJvmsStopped() {
        when(mockGroupService.getGroup(anyString())).thenReturn(group);
        Response response = groupServiceRest.areAllJvmsStopped("testGroup");
        assertNotNull(response);

        Group mockGroupWithJvms = mock(Group.class);
        Set<Jvm> mockJvmList = new HashSet<>();
        Jvm mockJvmStarted = mock(Jvm.class);
        mockJvmList.add(mockJvmStarted);

        when(mockJvmStarted.getState()).thenReturn(JvmState.JVM_STARTED);
        when(mockGroupWithJvms.getJvms()).thenReturn(mockJvmList);
        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroupWithJvms);
        response = groupServiceRest.areAllJvmsStopped("testGroup");
        assertNotNull(response);

        when(mockJvmStarted.getState()).thenReturn(JvmState.JVM_STOPPED);
        response = groupServiceRest.areAllJvmsStopped("testGroup");
        assertNotNull(response);
    }

    @Test
    public void testAreAllWebServerStopped() {
        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroup);
        when(mockGroupService.getGroupWithWebServers(any(Identifier.class))).thenReturn(mockGroup);
        Response response = groupServiceRest.areAllWebServersStopped("testGroup");
        assertNotNull(response);

        Group mockGroupWithWebServers = mock(Group.class);
        Set<WebServer> mockWSList = new HashSet<>();
        WebServer mockWSStarted = mock(WebServer.class);
        mockWSList.add(mockWSStarted);

        when(mockWSStarted.getState()).thenReturn(WebServerReachableState.WS_REACHABLE);
        when(mockGroupWithWebServers.getWebServers()).thenReturn(mockWSList);
        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroupWithWebServers);
        when(mockGroupService.getGroupWithWebServers(any(Identifier.class))).thenReturn(mockGroupWithWebServers);
        when(mockWebServerService.isStarted(any(WebServer.class))).thenReturn(true);
        response = groupServiceRest.areAllWebServersStopped("testGroup");
        assertNotNull(response);

        when(mockWSStarted.getState()).thenReturn(WebServerReachableState.WS_UNREACHABLE);
        when(mockWebServerService.isStarted(any(WebServer.class))).thenReturn(false);
        response = groupServiceRest.areAllWebServersStopped("testGroup");
        assertNotNull(response);
    }

    @Test
    public void testGetHosts() {
        when(mockGroupService.getHosts(anyString())).thenReturn(new ArrayList<String>());
        Response result = groupServiceRest.getHosts("test-group");
        assertEquals(200, result.getStatus());
    }

    @Test
    public void testGetAllHosts() {
        when(mockGroupService.getAllHosts()).thenReturn(new ArrayList<String>());
        Response result = groupServiceRest.getAllHosts();
        assertEquals(200, result.getStatus());
    }

    @Test
    public void testCreateGroupFail() {
        when(mockGroupService.createGroup(any(CreateGroupRequest.class), any(User.class))).thenThrow(EntityExistsException.class);
        Response response = groupServiceRest.createGroup(mockGroup.getName(), mockAuthenticatedUser);
        assertEquals(500, response.getStatus());
    }

    @Test
    public void testUpdateGroupFail() {
        JsonUpdateGroup mockJsonUpdateGroup = mock(JsonUpdateGroup.class);
        Identifier mockIdentifier = mock(Identifier.class);
        when(mockJsonUpdateGroup.getId()).thenReturn("1");
        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroup);
        when(mockGroup.getId()).thenReturn(mockIdentifier);
        when(mockIdentifier.getId()).thenReturn(1l);
        when(mockJsonUpdateGroup.getName()).thenReturn("test-group");
        when(mockGroupService.updateGroup(any(UpdateGroupRequest.class), any(User.class))).thenThrow(EntityExistsException.class);
        Response response = groupServiceRest.updateGroup(mockJsonUpdateGroup, mockAuthenticatedUser);
        assertEquals(500, response.getStatus());
    }

    @Test
    public void testRemoveGroupByNameFail() {
        Mockito.doThrow(GroupServiceException.class).when(mockGroupService).removeGroup(anyString());
        Response response = groupServiceRest.removeGroup("test-group", true);
        assertEquals(500, response.getStatus());
    }

    @Test
    public void testRemoveGroupByIdFail() {
        Mockito.doThrow(GroupServiceException.class).when(mockGroupService).removeGroup(any(Identifier.class));
        Response response = groupServiceRest.removeGroup("1", false);
        assertEquals(500, response.getStatus());
    }

    @Test(expected = InternalErrorException.class)
    public void testGenerateAndDeployGroupAppFileFail() throws IOException {
        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroup);
        when(mockGroupService.getGroupAppResourceTemplateMetaData(anyString(), anyString())).thenReturn("anyString");
        Application mockApp = mock(Application.class);
        when(mockApplicationService.getApplication(anyString())).thenReturn(mockApp);
        when(mockResourceService.getTokenizedMetaData(anyString(), any(Application.class), anyString())).thenThrow(new IOException("Cannot parse meta data: \"anyString\""));
        groupServiceRest.generateAndDeployGroupAppFile("test-group", "anyFile.txt", "testApp", mockAuthenticatedUser, "anyHost");
    }

    @Test(expected = InternalErrorException.class)
    public void testPerformGroupAppDeployToJvmsFail() {
        final Set<Jvm> jvms = new HashSet<>();
        final String hostname = "testHost";
        final ApplicationServiceRest mockApplicationServiceRest = mock(ApplicationServiceRest.class);
        JvmState jvmState = JvmState.JVM_STARTED;
        jvms.add(mockJvm);
        when(mockJvm.getHostName()).thenReturn(hostname);
        when(mockGroup.getJvms()).thenReturn(jvms);
        when(mockJvm.getState()).thenReturn(jvmState);

        groupServiceRest.performGroupAppDeployToJvms("test-group", "test-file", mockAuthenticatedUser, mockGroup, "test-app", mockApplicationServiceRest, hostname, false);
    }

    /**
     * Instead of mocking the ServletInputStream, let's extend it instead.
     *
     * @see "http://stackoverflow.com/questions/20995874/how-to-mock-a-javax-servlet-servletinputstream"
     */
    static class DelegatingServletInputStream extends ServletInputStream {

        private InputStream inputStream;

        public DelegatingServletInputStream() {
            inputStream = new ByteArrayInputStream("------WebKitFormBoundaryXRxegBGqTe4gApI2\r\nContent-Disposition: form-data; name=\"hct.properties\"; filename=\"hotel-booking.txt\"\r\nContent-Type: text/plain\r\n\r\n\r\n------WebKitFormBoundaryXRxegBGqTe4gApI2--".getBytes(Charset.defaultCharset()));
        }

        /**
         * Return the underlying source stream (never <code>null</code>).
         */
        public final InputStream getSourceStream() {
            return inputStream;
        }


        public int read() throws IOException {
            return inputStream.read();
        }

        public void close() throws IOException {
            super.close();
            inputStream.close();
        }

    }

    private class MyIS extends ServletInputStream {

        private InputStream backingStream;

        public MyIS(InputStream backingStream) {
            this.backingStream = backingStream;
        }

        @Override
        public int read() throws IOException {
            return backingStream.read();
        }

    }
}