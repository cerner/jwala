package com.cerner.jwala.service.jvm.impl;

import com.cerner.jwala.common.FileUtility;
import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.media.Media;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.ExecCommand;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.request.group.AddJvmToGroupRequest;
import com.cerner.jwala.common.request.jvm.ControlJvmRequest;
import com.cerner.jwala.common.request.jvm.CreateJvmAndAddToGroupsRequest;
import com.cerner.jwala.common.request.jvm.CreateJvmRequest;
import com.cerner.jwala.common.request.jvm.UpdateJvmRequest;
import com.cerner.jwala.control.AemControl;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.cerner.jwala.persistence.jpa.type.EventType;
import com.cerner.jwala.persistence.service.GroupPersistenceService;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.service.HistoryFacadeService;
import com.cerner.jwala.service.RemoteCommandExecutorService;
import com.cerner.jwala.service.VerificationBehaviorSupport;
import com.cerner.jwala.service.app.ApplicationService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionLockManager;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionService;
import com.cerner.jwala.service.group.GroupStateNotificationService;
import com.cerner.jwala.service.jvm.JvmControlService;
import com.cerner.jwala.service.jvm.JvmService;
import com.cerner.jwala.service.jvm.exception.JvmServiceException;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.service.resource.impl.ResourceGeneratorType;
import com.cerner.jwala.service.webserver.component.ClientFactoryHelper;
import com.jcraft.jsch.JSchException;
import org.apache.commons.io.FileUtils;
import org.apache.tika.mime.MediaType;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class JvmServiceImplTest extends VerificationBehaviorSupport {

    private static final String JUNIT_JVM = "junit-jvm";
    @Mock
    private JvmPersistenceService mockJvmPersistenceService;

    @Mock
    private GroupPersistenceService mockGroupPersistenceService;

    @Mock
    private User mockUser;

    @Mock
    private ApplicationService mockApplicationService;

    @Mock
    private SimpMessagingTemplate mockMessagingTemplate;

    @Mock
    private GroupStateNotificationService mockGroupStateNotificationService;

    @Mock
    private ClientFactoryHelper mockClientFactoryHelper;

    @Mock
    private ResourceService mockResourceService;

    @Mock
    private JvmControlService mockJvmControlService;

    @Mock
    private BinaryDistributionService mockBinaryDistributionService;

    @Mock
    private BinaryDistributionLockManager mockBinaryDistributionLockManager;

    @Mock
    private HistoryFacadeService mockHistoryFacadeService;

    @Mock
    private FileUtility mockFileUtility;

    @Mock
    SshConfiguration sshConfiguration;

    @Mock
    RemoteCommandExecutorService remoteCommandExecutorService;

    private JvmService jvmService;

    private JvmServiceImpl jvmServiceImpl;

    private final Map<String, ReentrantReadWriteLock> lockMap = new HashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmServiceImplTest.class);

    @Before
    public void setup() throws IOException {

        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");
        initMocks(this);
        jvmServiceImpl = new JvmServiceImpl(mockJvmPersistenceService, mockGroupPersistenceService, mockApplicationService,
                mockMessagingTemplate, mockGroupStateNotificationService, mockResourceService, mockClientFactoryHelper,
                "/topic/server-states", mockJvmControlService, mockBinaryDistributionService, mockBinaryDistributionLockManager,
                mockHistoryFacadeService, new FileUtility());
        jvmService = jvmServiceImpl;

        FileUtils.forceMkdir(new File(ApplicationProperties.get("paths.generated.resource.dir") + "/" + JUNIT_JVM));
    }

    @Test
    public void testCreateValidate() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");

        final CreateJvmRequest createJvmRequest = mock(CreateJvmRequest.class);
        final CreateJvmAndAddToGroupsRequest createJvmAndAddToGroupsRequest = mock(CreateJvmAndAddToGroupsRequest.class);
        final Jvm jvm = new Jvm(new Identifier<Jvm>(99L), "testJvm", new HashSet<Group>());

        when(mockJvmPersistenceService.createJvm(any(CreateJvmRequest.class))).thenReturn(jvm);
        when(mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(jvm);
        when(createJvmAndAddToGroupsRequest.getCreateCommand()).thenReturn(createJvmRequest);

        jvmService.createJvm(createJvmAndAddToGroupsRequest, mockUser);

        verify(createJvmAndAddToGroupsRequest, times(1)).validate();
        verify(mockJvmPersistenceService, times(1)).createJvm(createJvmRequest);

        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testCreateValidateAdd() {

        final CreateJvmRequest createJvmRequest = mock(CreateJvmRequest.class);
        final CreateJvmAndAddToGroupsRequest command = mock(CreateJvmAndAddToGroupsRequest.class);
        final Jvm jvm = mockJvmWithId(new Identifier<Jvm>(-123456L));
        final Set<AddJvmToGroupRequest> addCommands = createMockedAddRequests(3);
        final Set<Identifier<Group>> groupsSet = new HashSet<>();
        groupsSet.add(new Identifier<Group>(111L));

        when(command.toAddRequestsFor(eq(jvm.getId()))).thenReturn(addCommands);
        when(command.getCreateCommand()).thenReturn(createJvmRequest);
        when(command.getGroups()).thenReturn(groupsSet);
        when(mockJvmPersistenceService.createJvm(createJvmRequest)).thenReturn(jvm);
        when(mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(jvm);

        jvmService.createJvm(command, mockUser);

        verify(command, times(1)).validate();
        verify(mockJvmPersistenceService, times(1)).createJvm(createJvmRequest);
        for (final AddJvmToGroupRequest addCommand : addCommands) {
            verify(mockGroupPersistenceService, times(1)).addJvmToGroup(matchCommand(addCommand));
        }
    }

    @Test
    public void testCreateValidateInheritsDefaultTemplates() throws IOException {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");

        final CreateJvmRequest createJvmRequest = mock(CreateJvmRequest.class);
        final CreateJvmAndAddToGroupsRequest createJvmAndAddToGroupsRequest = mock(CreateJvmAndAddToGroupsRequest.class);
        final Group mockGroup = mock(Group.class);

        Set<Group> groupSet = new HashSet<>();
        groupSet.add(mockGroup);
        List<String> templateNames = new ArrayList<>();
        templateNames.add("template-name");
        List<String> appTemplateNames = new ArrayList<>();
        appTemplateNames.add("app-template-name");
        final Jvm jvm = new Jvm(new Identifier<Jvm>(99L), "testJvm", groupSet);

        ResourceTemplateMetaData mockMetaData = mock(ResourceTemplateMetaData.class);
        when(mockMetaData.getDeployFileName()).thenReturn("app-context.xml");

        when(createJvmAndAddToGroupsRequest.getCreateCommand()).thenReturn(createJvmRequest);
        when(mockJvmPersistenceService.createJvm(any(CreateJvmRequest.class))).thenReturn(jvm);
        when(mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(jvm);
        when(mockResourceService.generateResourceGroup()).thenReturn(mock(ResourceGroup.class));
        when(mockResourceService.getAppTemplate(anyString(), anyString(), anyString())).thenReturn("<context>xml</context>");
        when(mockGroup.getName()).thenReturn("mock-group-name");
        when(mockGroupPersistenceService.getGroupJvmsResourceTemplateNames(anyString())).thenReturn(templateNames);
        when(mockResourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), anyBoolean(), eq(ResourceGeneratorType.TEMPLATE))).thenReturn("<server>xml</server>");
        when(mockGroupPersistenceService.getGroupJvmResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"deployPath\":\"c:/fake/path\", \"deployFileName\":\"server-deploy.xml\"}");
        when(mockGroupPersistenceService.getGroupAppsResourceTemplateNames(anyString())).thenReturn(appTemplateNames);
        final String jsonMetaData = "{\"deployPath\":\"c:/fake/app/path\", \"deployFileName\":\"app-context.xml\", \"entity\":{\"deployToJvms\":\"true\", \"target\":\"app-target\"}}";
        when(mockGroupPersistenceService.getGroupAppResourceTemplateMetaData(anyString(), anyString())).thenReturn(jsonMetaData);
        when(mockResourceService.getTokenizedMetaData(anyString(), Matchers.anyObject(), anyString())).thenReturn(mockMetaData);
        when(mockResourceService.getMetaData(anyString())).thenReturn(new ObjectMapper().readValue(jsonMetaData, ResourceTemplateMetaData.class));
        when(mockGroupPersistenceService.getGroupJvmResourceTemplate(eq("mock-group-name"), eq("template-name"))).thenReturn("<server>xml</server>");

        jvmService.createJvm(createJvmAndAddToGroupsRequest, mockUser);

        verify(createJvmAndAddToGroupsRequest, times(1)).validate();
        verify(mockJvmPersistenceService, times(1)).createJvm(createJvmRequest);

        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testCreateValidateInheritsDefaultTemplatesFromMultipleGroups() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");

        final CreateJvmRequest createJvmRequest = mock(CreateJvmRequest.class);
        final CreateJvmAndAddToGroupsRequest createJvmAndAddToGroupsRequest = mock(CreateJvmAndAddToGroupsRequest.class);
        final Group mockGroup = mock(Group.class);
        final Group mockGroup2 = mock(Group.class);

        Set<Group> groupSet = new HashSet<>();
        groupSet.add(mockGroup);
        groupSet.add(mockGroup2);
        List<String> templateNames = new ArrayList<>();
        List<String> appTemplateNames = new ArrayList<>();
        final Jvm jvm = new Jvm(new Identifier<Jvm>(99L), "testJvm", groupSet);

        when(createJvmAndAddToGroupsRequest.getCreateCommand()).thenReturn(createJvmRequest);
        when(mockJvmPersistenceService.createJvm(any(CreateJvmRequest.class))).thenReturn(jvm);
        when(mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(jvm);
        when(mockResourceService.generateResourceGroup()).thenReturn(mock(ResourceGroup.class));
        when(mockGroup.getName()).thenReturn("mock-group-name");
        when(mockGroupPersistenceService.getGroupJvmsResourceTemplateNames(anyString())).thenReturn(templateNames);
        when(mockGroupPersistenceService.getGroupAppsResourceTemplateNames(anyString())).thenReturn(appTemplateNames);

        jvmService.createJvm(createJvmAndAddToGroupsRequest, mockUser);

        verify(createJvmAndAddToGroupsRequest, times(1)).validate();
        verify(mockJvmPersistenceService, times(1)).createJvm(createJvmRequest);

        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test(expected = InternalErrorException.class)
    public void testCreateValidateInheritsDefaultTemplatesJvmTemplateThrowsIOException() throws IOException {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");

        final CreateJvmRequest createJvmRequest = mock(CreateJvmRequest.class);
        final CreateJvmAndAddToGroupsRequest createJvmAndAddToGroupsRequest = mock(CreateJvmAndAddToGroupsRequest.class);
        final Group mockGroup = mock(Group.class);

        Set<Group> groupSet = new HashSet<>();
        groupSet.add(mockGroup);
        List<String> templateNames = new ArrayList<>();
        templateNames.add("template-name");
        final Jvm jvm = new Jvm(new Identifier<Jvm>(99L), "testJvm", groupSet);

        when(createJvmAndAddToGroupsRequest.getCreateCommand()).thenReturn(createJvmRequest);
        when(mockJvmPersistenceService.createJvm(any(CreateJvmRequest.class))).thenReturn(jvm);
        when(mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(jvm);
        when(mockResourceService.generateResourceGroup()).thenReturn(mock(ResourceGroup.class));
        when(mockResourceService.getTokenizedMetaData(anyString(), Matchers.anyObject(), anyString())).thenThrow(new IOException("FAIL converting meta data"));
        when(mockGroup.getName()).thenReturn("mock-group-name");
        when(mockGroupPersistenceService.getGroupJvmsResourceTemplateNames(anyString())).thenReturn(templateNames);
        when(mockResourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), anyBoolean(), eq(ResourceGeneratorType.TEMPLATE))).thenReturn("<server>xml</server>");
        when(mockGroupPersistenceService.getGroupJvmResourceTemplateMetaData(anyString(), anyString())).thenReturn("{deployPath:c:/fake/path}");

        jvmService.createJvm(createJvmAndAddToGroupsRequest, mockUser);

        verify(createJvmRequest, times(1)).validate();
        verify(mockJvmPersistenceService, times(1)).createJvm(createJvmRequest);

        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test(expected = InternalErrorException.class)
    public void testCreateValidateInheritsDefaultTemplatesAppTemplateThrowsIOException() throws IOException {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");

        final CreateJvmRequest createJvmRequest = mock(CreateJvmRequest.class);
        final CreateJvmAndAddToGroupsRequest createJvmAndAddToGroupsRequest = mock(CreateJvmAndAddToGroupsRequest.class);
        final Group mockGroup = mock(Group.class);

        Set<Group> groupSet = new HashSet<>();
        groupSet.add(mockGroup);
        List<String> templateNames = new ArrayList<>();
        List<String> appTemplateNames = new ArrayList<>();
        appTemplateNames.add("app-template-name");
        final Jvm jvm = new Jvm(new Identifier<Jvm>(99L), "testJvm", groupSet);

        when(createJvmAndAddToGroupsRequest.getCreateCommand()).thenReturn(createJvmRequest);
        when(mockJvmPersistenceService.createJvm(any(CreateJvmRequest.class))).thenReturn(jvm);
        when(mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(jvm);
        when(mockResourceService.generateResourceGroup()).thenReturn(mock(ResourceGroup.class));
        when(mockResourceService.getAppTemplate(anyString(), anyString(), anyString())).thenReturn("<context>xml</context>");
        when(mockGroup.getName()).thenReturn("mock-group-name");
        when(mockGroupPersistenceService.getGroupJvmsResourceTemplateNames(anyString())).thenReturn(templateNames);
        when(mockGroupPersistenceService.getGroupAppsResourceTemplateNames(anyString())).thenReturn(appTemplateNames);
        when(mockResourceService.getMetaData(anyString())).thenThrow(new IOException());
        jvmService.createJvm(createJvmAndAddToGroupsRequest, mockUser);

        verify(createJvmRequest, times(1)).validate();
        verify(mockJvmPersistenceService, times(1)).createJvm(createJvmRequest);

        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testUpdateJvmShouldValidateCommand() {

        final UpdateJvmRequest updateJvmRequest = mock(UpdateJvmRequest.class);
        final Set<AddJvmToGroupRequest> addCommands = createMockedAddRequests(5);

        when(updateJvmRequest.getAssignmentCommands()).thenReturn(addCommands);

        jvmService.updateJvm(updateJvmRequest, true);

        verify(updateJvmRequest, times(1)).validate();
        verify(mockJvmPersistenceService, times(1)).updateJvm(updateJvmRequest, true);
        verify(mockJvmPersistenceService, times(1)).removeJvmFromGroups(Matchers.<Identifier<Jvm>>anyObject());
        for (final AddJvmToGroupRequest addCommand : addCommands) {
            verify(mockGroupPersistenceService, times(1)).addJvmToGroup(matchCommand(addCommand));
        }
    }

    @Test
    public void testRemoveJvm() {

        final Identifier<Jvm> id = new Identifier<>(-123456L);
        Jvm mockJvm = mockJvmWithId(id);
        when(mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(mockJvm);
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);

        when(mockJvmControlService.controlJvm(any(ControlJvmRequest.class), any(User.class))).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));

        jvmService.removeJvm(id, mockUser);

        verify(mockJvmPersistenceService, times(1)).removeJvm(eq(id));
    }

    @Test(expected = InternalErrorException.class)
    public void testRemoveJvmInStartedState() {
        final Identifier<Jvm> id = new Identifier<>(-123456L);
        Jvm mockJvm = mockJvmWithId(id);
        when(mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(mockJvm);
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STARTED);

        jvmService.removeJvm(id, mockUser);
    }

    @Test
    public void testGetAll() {

        jvmService.getJvms();

        verify(mockJvmPersistenceService, times(1)).getJvms();
    }

    @Test
    public void testGetSpecific() {

        final Identifier<Jvm> id = new Identifier<>(-123456L);

        jvmService.getJvm(id);

        verify(mockJvmPersistenceService, times(1)).getJvm(eq(id));
    }

    protected Jvm mockJvmWithId(final Identifier<Jvm> anId) {
        final Jvm jvm = mock(Jvm.class);
        when(jvm.getId()).thenReturn(anId);
        return jvm;
    }

    @Test
    public void testPerformDiagnosis() throws IOException, URISyntaxException {
        Identifier<Jvm> aJvmId = new Identifier<>(11L);
        Jvm jvm = mock(Jvm.class);
        when(jvm.getId()).thenReturn(aJvmId);
        when(jvm.getStatusUri()).thenReturn(new URI("http://test.com"));
        when(mockJvmPersistenceService.getJvm(aJvmId)).thenReturn(jvm);

        ClientHttpResponse mockResponse = mock(ClientHttpResponse.class);
        when(mockResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockClientFactoryHelper.requestGet(any(URI.class))).thenReturn(mockResponse);

        when(jvm.getState()).thenReturn(JvmState.JVM_START);
        jvmService.performDiagnosis(aJvmId, mockUser);
        verify(mockHistoryFacadeService).write(anyString(), anyCollection(), eq("Diagnose and resolve state"),
                eq(EventType.USER_ACTION_INFO), anyString());
    }

    @Test
    public void testPerformDiagnosisThrowsIOException() throws URISyntaxException, IOException {
        Identifier<Jvm> aJvmId = new Identifier<>(11L);
        Jvm jvm = mock(Jvm.class);
        when(jvm.getId()).thenReturn(aJvmId);
        when(jvm.getStatusUri()).thenReturn(new URI("http://test.com"));
        when(mockJvmPersistenceService.getJvm(aJvmId)).thenReturn(jvm);

        when(mockClientFactoryHelper.requestGet(any(URI.class))).thenThrow(new IOException("TEST IO EXCEPTION"));
        when(jvm.getState()).thenReturn(JvmState.JVM_STARTED);
        jvmService.performDiagnosis(aJvmId, mockUser);
        verify(mockHistoryFacadeService).write(anyString(), anyCollection(), eq("Diagnose and resolve state"),
                eq(EventType.USER_ACTION_INFO), anyString());
    }

    @Test
    public void testPerformDiagnosisThrowsRuntimeException() throws IOException, URISyntaxException {
        Identifier<Jvm> aJvmId = new Identifier<>(11L);
        Jvm jvm = mock(Jvm.class);
        when(jvm.getId()).thenReturn(aJvmId);
        when(jvm.getStatusUri()).thenReturn(new URI("http://test.com"));
        when(mockJvmPersistenceService.getJvm(aJvmId)).thenReturn(jvm);

        when(mockClientFactoryHelper.requestGet(any(URI.class))).thenThrow(new RuntimeException("RUN!!"));
        when(jvm.getState()).thenReturn(JvmState.JVM_STARTED);
        jvmService.performDiagnosis(aJvmId, mockUser);
        verify(mockHistoryFacadeService).write(anyString(), anyCollection(), eq("Diagnose and resolve state"),
                eq(EventType.USER_ACTION_INFO), anyString());
    }

    @Test
    public void testGetResourceTemplateNames() {
        String testJvmName = "testJvmName";
        ArrayList<String> value = new ArrayList<>();
        when(mockJvmPersistenceService.getResourceTemplateNames(testJvmName)).thenReturn(value);
        value.add("testJvm.tpl");
        List<String> result = jvmService.getResourceTemplateNames(testJvmName);
        assertTrue(result.size() == 1);
    }

    @Test
    public void testGetResourceTemplate() {
        String testJvmName = "testJvmName";
        String resourceTemplateName = "test-resource.tpl";
        Jvm jvm = mock(Jvm.class);
        String expectedValue = "<template>resource</template>";
        when(mockJvmPersistenceService.getResourceTemplate(testJvmName, resourceTemplateName)).thenReturn(expectedValue);
        List<Jvm> jvmList = new ArrayList<>();
        jvmList.add(jvm);
        when(mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(jvm);
        when(mockResourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        when(mockResourceService.generateResourceFile(eq(resourceTemplateName), eq(expectedValue), any(ResourceGroup.class), eq(jvm), any(ResourceGeneratorType.class))).thenReturn(expectedValue);
        String result = jvmService.getResourceTemplate(testJvmName, resourceTemplateName, true);
        assertEquals(expectedValue, result);

        result = jvmService.getResourceTemplate(testJvmName, resourceTemplateName, false);
        assertEquals(expectedValue, result);
    }

    @Test
    public void testUpdateResourceTemplate() {
        String testJvmName = "testJvmName";
        String resourceTemplateName = "test-resource.tpl";
        String template = "<template>update</template>";
        when(mockJvmPersistenceService.updateResourceTemplate(testJvmName, resourceTemplateName, template)).thenReturn(template);
        String result = jvmService.updateResourceTemplate(testJvmName, resourceTemplateName, template);
        assertEquals(template, result);
    }

    @Test
    public void testGetJvmByName() {
        jvmService.getJvm("testJvm");
        verify(mockJvmPersistenceService).findJvmByExactName("testJvm");
    }

    @Test
    public void testPreviewTemplate() {
        final String jvmName = "jvm-1Test";
        Jvm testJvm = new Jvm(new Identifier<Jvm>(111L), jvmName, "testHost", new HashSet<Group>(), mock(Group.class), 9101, 9102, 9103, -1, 9104, new Path("./"), "", JvmState.JVM_STOPPED, "", null, null, null, null, null, null, null);
        List<Jvm> jvmList = new ArrayList<>();
        jvmList.add(testJvm);
        when(mockJvmPersistenceService.findJvm(anyString(), anyString())).thenReturn(testJvm);
        when(mockResourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        when(mockResourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), eq(testJvm), any(ResourceGeneratorType.class))).thenReturn("TEST jvm-1Test TEST");

        String preview = jvmService.previewResourceTemplate("myFile", jvmName, "groupTest", "TEST ${jvm.jvmName} TEST");
        assertEquals("TEST jvm-1Test TEST", preview);
    }

    @Test
    public void testUpdateState() {
        Identifier<Jvm> jvmId = new Identifier<Jvm>(999L);
        jvmService.updateState(jvmId, JvmState.JVM_STOPPED);
        verify(mockJvmPersistenceService).updateState(jvmId, JvmState.JVM_STOPPED, "");
    }

    @Test
    public void testDeployApplicationContextXMLs() {
        final Identifier<Jvm> jvmId = new Identifier<>(2323L);
        final Identifier<Group> groupId = new Identifier<>(222L);
        final Jvm jvm = mockJvmWithId(jvmId);
        when(jvm.getJvmName()).thenReturn("testJvmName");

        List<Group> groupsList = new ArrayList<Group>();
        Group mockGroup = mock(Group.class);
        groupsList.add(mockGroup);
        when(mockGroup.getId()).thenReturn(groupId);
        when(mockGroup.getName()).thenReturn("testGroupName");

        List<Application> appList = new ArrayList<>();
        Application mockApp = mock(Application.class);
        appList.add(mockApp);
        when(mockApp.getName()).thenReturn("testAppName");

        List<String> templateNamesList = new ArrayList<>();
        templateNamesList.add("testAppResource.xml");

        when(mockApplicationService.getResourceTemplateNames(anyString(), anyString())).thenReturn(templateNamesList);
        final User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("user-id");

        when(mockApplicationService.findApplications(any(Identifier.class))).thenReturn(appList);
        when(mockJvmPersistenceService.findGroupsByJvm(any(Identifier.class))).thenReturn(groupsList);
        jvmService.deployApplicationContextXMLs(jvm, mockUser);
        verify(mockApplicationService).deployConf(anyString(), anyString(), anyString(), anyString(), any(ResourceGroup.class), any(User.class));
    }

    @Test(expected = InternalErrorException.class)
    public void testCheckSetenvBat() {
        final String jvmName = "test-jvm-check-for-setenvbat";
        when(mockJvmPersistenceService.getResourceTemplate(jvmName, "setenv.bat")).thenReturn("ignore template content, just need to check no exception is thrown");
        jvmService.checkForSetenvScript(jvmName);

        verify(mockJvmPersistenceService).getResourceTemplate(anyString(), anyString());

        when(mockJvmPersistenceService.getResourceTemplate(jvmName, "setenv.bat")).thenThrow(new NonRetrievableResourceTemplateContentException("JVM", "setenv.bat", new Throwable()));
        jvmService.checkForSetenvScript(jvmName);
    }

    @Test
    public void testGenerateAndDeployConfig() throws CommandFailureException, IOException {

        CommandOutput commandOutput = mock(CommandOutput.class);
        Jvm mockJvm = mock(Jvm.class);
        ResourceGroup mockResourceGroup = mock(ResourceGroup.class);

        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(mockJvm.getJvmName()).thenReturn("test-jvm-deploy-config");
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(111L));
        when(mockJvm.getJdkMedia()).thenReturn(new Media(1, "test media", "x:/test/archive/path.zip", "JDK", "x:/test-destination", "root-dir-destination"));
        when(commandOutput.getReturnCode()).thenReturn(new ExecReturnCode(0));
        when(mockJvmControlService.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString(), anyString(), anyBoolean())).thenReturn(commandOutput);
        when(mockJvmControlService.executeCreateDirectoryCommand(any(Jvm.class), anyString())).thenReturn(commandOutput);
        when(mockJvmControlService.executeChangeFileModeCommand(any(Jvm.class), anyString(), anyString(), anyString())).thenReturn(commandOutput);

        when(mockJvmControlService.controlJvm(eq(new ControlJvmRequest(mockJvm.getId(), JvmControlOperation.DELETE_SERVICE)), any(User.class))).thenReturn(commandOutput);
        when(mockJvmControlService.controlJvm(eq(new ControlJvmRequest(mockJvm.getId(), JvmControlOperation.DEPLOY_CONFIG_ARCHIVE)), any(User.class))).thenReturn(commandOutput);
        when(mockJvmControlService.controlJvm(eq(new ControlJvmRequest(mockJvm.getId(), JvmControlOperation.INSTALL_SERVICE)), any(User.class))).thenReturn(commandOutput);

        when(mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        when(mockJvmPersistenceService.getJvmTemplate(anyString(), any(Identifier.class))).thenReturn("<server>some xml</server>");

        when(mockResourceService.generateResourceGroup()).thenReturn(mockResourceGroup);
        when(mockResourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), anyObject(), any(ResourceGeneratorType.class))).thenReturn("<server>some xml</server>");
        when(mockJvmControlService.executeCheckFileExistsCommand(any(Jvm.class), anyString())).thenReturn(commandOutput);

        final List<String> templateNames = new ArrayList<>();
        templateNames.add("setenv.bat");
        when(mockJvmPersistenceService.getResourceTemplateNames(anyString())).thenReturn(templateNames);

        Jvm response = jvmService.generateAndDeployJvm(mockJvm.getJvmName(), mockUser);
        assertEquals(response.getJvmName(), mockJvm.getJvmName());

        // test failing the invoke service
        CommandOutput mockExecDataFail = mock(CommandOutput.class);
        when(mockExecDataFail.getReturnCode()).thenReturn(new ExecReturnCode(1));
        when(mockExecDataFail.getStandardError()).thenReturn("ERROR");

        when(mockJvmControlService.controlJvm(eq(new ControlJvmRequest(mockJvm.getId(), JvmControlOperation.INSTALL_SERVICE)), any(User.class))).thenReturn(mockExecDataFail);

        boolean exceptionThrown = false;
        try {
            jvmService.generateAndDeployJvm(mockJvm.getJvmName(), mockUser);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try {
            jvmService.generateAndDeployJvm(mockJvm.getJvmName(), mockUser);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        // test secure copy fails
        when(mockJvmControlService.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString(), anyString(), anyBoolean())).thenReturn(mockExecDataFail);
        exceptionThrown = false;
        try {
            jvmService.generateAndDeployJvm(mockJvm.getJvmName(), mockUser);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        // test secure copy throws a command failure exception
        ExecCommand execCommand = new ExecCommand("fail command");
        Throwable throwable = new JSchException("Failed scp");
        final CommandFailureException commandFailureException = new CommandFailureException(execCommand, throwable);
        when(mockJvmControlService.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString(), anyString(), anyBoolean())).thenThrow(commandFailureException);
        exceptionThrown = false;
        try {
            jvmService.generateAndDeployJvm(mockJvm.getJvmName(), mockUser);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try {
            jvmService.generateAndDeployJvm(mockJvm.getJvmName(), mockUser);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
//        FileUtils.deleteDirectory(new File("./" + jvm.getJvmName()));
//        FileUtils.deleteDirectory(new File("./" + jvm.getJvmName() + "null"));
    }


    @Test(expected = InternalErrorException.class)
    public void testGenerateAndDeployJVMFailsJVMStarted() {
        Jvm mockJvm = mock(Jvm.class);

        when(mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STARTED);
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(111L));
        when(mockJvm.getHostName()).thenReturn("testHostName");
        jvmService.generateAndDeployJvm("test-jvm-fails-started", mockUser);
    }

    @Test
    public void testGenerateAndDeplyJVMFailsForResourceGeneration() {
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(111L));
        when(mockJvm.getHostName()).thenReturn("test-host-name");

        Group mockGroup = mock(Group.class);
        when(mockGroup.getId()).thenReturn(new Identifier<Group>(111L));
        when(mockGroup.getName()).thenReturn("test-group-for-failed-resource-generation");
        List<Group> groupList = Collections.singletonList(mockGroup);

        Application mockApp = mock(Application.class);
        when(mockApp.getName()).thenReturn("test-application-for-failed-resource-generation");
        List<Application> appList = Collections.singletonList(mockApp);

        Map<String, List<String>> jvmErrorMap = new HashMap<>();
        List<String> errorList = Arrays.asList("failed generation server.xml", "failed generation context.xml", "failed generation setenv.bat");
        jvmErrorMap.put("test-jvm-fails-resource-generation", errorList);

        Map<String, List<String>> appErrorMap = new HashMap<>();
        List<String> appErrorList = Arrays.asList("failed generation app-context.xml", "failed generation app-properties.properties");
        appErrorMap.put("test-jvm-app-fails-resource-generation", appErrorList);

        List<String> appResourceNamesList = Collections.singletonList("test-app-context.xml");

        when(mockApplicationService.getResourceTemplateNames(anyString(), anyString())).thenReturn(appResourceNamesList);
        when(mockApplicationService.findApplications(any(Identifier.class))).thenReturn(appList);
        when(mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        when(mockJvmPersistenceService.findGroupsByJvm(any(Identifier.class))).thenReturn(groupList);
        doThrow(new InternalErrorException(FaultType.RESOURCE_GENERATION_FAILED, "Test for failing JVM resource generation", null, jvmErrorMap)).when(mockResourceService).validateAllResourcesForGeneration(any(ResourceIdentifier.class));
        doThrow(new InternalErrorException(FaultType.RESOURCE_GENERATION_FAILED, "Test failing for Web App resource generation", null, appErrorMap)).when(mockResourceService).validateSingleResourceForGeneration(any(ResourceIdentifier.class));

        InternalErrorException caughtException = null;
        try {
            jvmService.generateAndDeployJvm("test-jvm-fails-resource-generation", mockUser);
        } catch (InternalErrorException iee) {
            caughtException = iee;
        }
        verify(mockResourceService).validateSingleResourceForGeneration(any(ResourceIdentifier.class));
        verify(mockResourceService).validateAllResourcesForGeneration(any(ResourceIdentifier.class));

        assertNotNull(caughtException);
        assertEquals(2, caughtException.getErrorDetails().size());
        assertEquals(3, caughtException.getErrorDetails().get("test-jvm-fails-resource-generation").size());
        assertEquals(2, caughtException.getErrorDetails().get("test-jvm-app-fails-resource-generation").size());
    }

    @Test(expected = InternalErrorException.class)
    public void testGenerateAndDeployJVMFailsCreateDirectory() throws CommandFailureException {
        Jvm mockJvm = mock(Jvm.class);
        CommandOutput commandOutputFails = new CommandOutput(new ExecReturnCode(1), "", "Fail creating the directory");

        when(mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        when(mockJvmControlService.executeCreateDirectoryCommand(any(Jvm.class), anyString())).thenReturn(commandOutputFails);
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(111L));
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);

        jvmService.generateAndDeployJvm("test-jvm-fails-started", mockUser);
    }

    @Test(expected = InternalErrorException.class)
    public void testGenerateAndDeployJVMFailsDeployingInvokeService() throws CommandFailureException {
        Jvm mockJvm = mock(Jvm.class);
        CommandOutput commandOutputFails = new CommandOutput(new ExecReturnCode(1), "", "Fail creating the directory");
        CommandOutput commandOutputSucceeds = new CommandOutput(new ExecReturnCode(0), "SUCCESS", "");

        when(mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        when(mockJvmControlService.executeCreateDirectoryCommand(any(Jvm.class), anyString())).thenReturn(commandOutputSucceeds);
        when(mockJvmControlService.secureCopyFile(any(ControlJvmRequest.class), contains(AemControl.Properties.DEPLOY_CONFIG_ARCHIVE_SCRIPT_NAME.getValue()), anyString(), anyString(), anyBoolean())).thenReturn(commandOutputSucceeds);
        when(mockJvmControlService.secureCopyFile(any(ControlJvmRequest.class), contains(AemControl.Properties.INSTALL_SERVICE_SCRIPT_NAME.getValue()), anyString(), anyString(), anyBoolean())).thenReturn(commandOutputFails);
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(111L));
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);

        jvmService.generateAndDeployJvm("test-jvm-fails-started", mockUser);
    }

    @Test(expected = InternalErrorException.class)
    public void testGenerateAndDeployJVMFailsChangeFileMode() throws CommandFailureException {
        Jvm mockJvm = mock(Jvm.class);
        CommandOutput commandOutputFails = new CommandOutput(new ExecReturnCode(1), "", "Fail creating the directory");
        CommandOutput commandOutputSucceeds = new CommandOutput(new ExecReturnCode(0), "SUCCESS", "");

        when(mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        when(mockJvmControlService.executeCreateDirectoryCommand(any(Jvm.class), anyString())).thenReturn(commandOutputSucceeds);
        when(mockJvmControlService.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString(), anyString(), anyBoolean())).thenReturn(commandOutputSucceeds);
        when(mockJvmControlService.executeChangeFileModeCommand(any(Jvm.class), anyString(), anyString(), anyString())).thenReturn(commandOutputFails);
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(111L));
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);

        jvmService.generateAndDeployJvm("test-jvm-fails-started", mockUser);
    }

    @Test
    public void testJvmCounts() {
        when(mockJvmPersistenceService.getJvmStartedCount(anyString())).thenReturn(1L);
        when(mockJvmPersistenceService.getJvmCount(anyString())).thenReturn(6L);
        when(mockJvmPersistenceService.getJvmStoppedCount(anyString())).thenReturn(2L);
        when(mockJvmPersistenceService.getJvmForciblyStoppedCount(anyString())).thenReturn(3L);

        Long startedCount = jvmService.getJvmStartedCount("anyGroupName");
        Long jvmCount = jvmService.getJvmCount("anyGroupName");
        Long jvmStoppedCount = jvmService.getJvmStoppedCount("anyGroupName");
        Long jvmForciblyStoppedCount = jvmService.getJvmForciblyStoppedCount("anyGroupName");

        assertEquals(new Long(1L), startedCount);
        assertEquals(new Long(2L), jvmStoppedCount);
        assertEquals(new Long(3L), jvmForciblyStoppedCount);
        assertEquals(new Long(6L), jvmCount);
    }

    @Test
    public void testDeleteJvm() {
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STARTED);
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(111L));
        when(mockJvm.getJvmName()).thenReturn("test-delete-jvm");

        when(mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);

        try {
            jvmService.deleteJvm("test-delete-jvm", "test-user-deletes-jvm");
            fail("Expecting a JvmServiceException");
        } catch (JvmServiceException jse){
            assertEquals("The target JVM must be stopped before attempting to delete it", jse.getMessage());
        }

        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(mockJvmControlService.controlJvm(any(ControlJvmRequest.class), any(User.class))).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        jvmService.deleteJvm("test-delete-jvm", "test-user-deletes-jvm");
        verify(mockJvmPersistenceService).removeJvm(any(Identifier.class));
    }

    @Test
    public void testGenerateAndDeployFile() throws CommandFailureException, IOException {
        CommandOutput mockExecData = mock(CommandOutput.class);
        final Jvm mockJvm = mockJvmWithId(new Identifier<Jvm>(111L));
        ResourceTemplateMetaData mockMetaData = mock(ResourceTemplateMetaData.class);

        when(mockJvm.getJvmName()).thenReturn("test-jvm-deploy-file");
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(0));
        when(mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        when(mockResourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), anyString(), any(ResourceGeneratorType.class))).thenReturn("<server>xml</server>");
        when(mockJvmPersistenceService.getJvmTemplate(anyString(), any(Identifier.class))).thenReturn("<server>xml</server>");
        when(mockJvmControlService.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString(), anyString(), anyBoolean())).thenReturn(mockExecData);
        when(mockJvmControlService.executeCreateDirectoryCommand(any(Jvm.class), anyString())).thenReturn(mockExecData);
        when(mockResourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        when(mockResourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), any(), any(ResourceGeneratorType.class))).thenReturn("{\"deployFileName\":\"server.xml\", \"deployPath\":\"/\",\"contentType\":\"text/plain\"}");
        when(mockMetaData.getDeployFileName()).thenReturn("server.xml");
        when(mockMetaData.getDeployPath()).thenReturn("/");
        when(mockMetaData.getContentType()).thenReturn(MediaType.APPLICATION_XML);
        when(mockResourceService.getTokenizedMetaData(anyString(), Matchers.anyObject(), anyString())).thenReturn(mockMetaData);
        Jvm jvm = jvmService.generateAndDeployFile("test-jvm-deploy-file", "server.xml", mockUser);
        assertEquals(mockJvm, jvm);

        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(1));
        when(mockExecData.getStandardError()).thenReturn("ERROR");
        when(mockResourceService.generateAndDeployFile(any(ResourceIdentifier.class), anyString(), anyString(), anyString())).thenThrow(new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, "xxx"));

        boolean exceptionThrown = false;
        try {
            jvmService.generateAndDeployFile(jvm.getJvmName(), "server.xml", mockUser);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try {
            jvmService.generateAndDeployFile(jvm.getJvmName(), "server.xml", mockUser);
        } catch (InternalErrorException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        when(mockJvmControlService.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString(), anyString(), anyBoolean())).thenThrow(new CommandFailureException(new ExecCommand("fail for secure copy"), new Throwable("test fail")));
        exceptionThrown = false;
        try {
            jvmService.generateAndDeployFile(jvm.getJvmName(), "server.xml", mockUser);
        } catch (InternalErrorException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try {
            jvmService.generateAndDeployFile(jvm.getJvmName(), "server.xml", mockUser);
        } catch (InternalErrorException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

//        FileUtils.deleteDirectory(new File("./" + jvm.getJvmName()));
    }

    @Test(expected = InternalErrorException.class)
    public void testGenerateAndDeployFileJvmStarted() {
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STARTED);
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(11111L));
        when(mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        jvmService.generateAndDeployFile("jvmName", "fileName", mockUser);
    }

    @Test
    public void testGenerateJvmConfigJar() {
        Set<Group> groups = new HashSet<Group>() {{
            add(new Group(Identifier.id(2L, Group.class), "junit-group"));
        }};

        Jvm jvm = new Jvm(Identifier.id(1L, Jvm.class), JUNIT_JVM, groups);

        jvmServiceImpl = new JvmServiceImpl(mockJvmPersistenceService, mockGroupPersistenceService, mockApplicationService,
                mockMessagingTemplate, mockGroupStateNotificationService, mockResourceService, mockClientFactoryHelper,
                "/topic/server-states", mockJvmControlService, mockBinaryDistributionService, mockBinaryDistributionLockManager,
                mockHistoryFacadeService, new FileUtility());

        when(mockJvmPersistenceService.findJvmByExactName(jvm.getJvmName())).thenReturn(jvm);
        when(mockResourceService.generateResourceFile(anyString(),
                anyString(),
                any(ResourceGroup.class),
                any(Jvm.class),
                eq(ResourceGeneratorType.TEMPLATE))).thenReturn("some file content");
        try {
            jvmServiceImpl.generateJvmConfigJar(jvm);
        } catch (CommandFailureException e) {
            LOGGER.error("Failed to generate remote jar.", e);
        }
    }
}
