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
import com.cerner.jwala.common.domain.model.media.MediaType;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.domain.model.resource.ResourceContent;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.ExecCommand;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.properties.PropertyKeys;
import com.cerner.jwala.common.request.group.AddJvmToGroupRequest;
import com.cerner.jwala.common.request.jvm.*;
import com.cerner.jwala.control.AemControl;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.persistence.jpa.type.EventType;
import com.cerner.jwala.persistence.service.GroupPersistenceService;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.persistence.service.WebServerPersistenceService;
import com.cerner.jwala.service.HistoryFacadeService;
import com.cerner.jwala.service.HistoryService;
import com.cerner.jwala.service.MessagingService;
import com.cerner.jwala.service.VerificationBehaviorSupport;
import com.cerner.jwala.service.app.ApplicationService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionLockManager;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionService;
import com.cerner.jwala.service.group.GroupService;
import com.cerner.jwala.service.group.GroupStateNotificationService;
import com.cerner.jwala.service.jvm.JvmControlService;
import com.cerner.jwala.service.jvm.JvmService;
import com.cerner.jwala.service.jvm.JvmStateService;
import com.cerner.jwala.service.jvm.exception.JvmServiceException;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.service.resource.impl.ResourceGeneratorType;
import com.cerner.jwala.service.webserver.component.ClientFactoryHelper;
import com.jcraft.jsch.JSchException;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.persistence.NoResultException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyCollection;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {JvmServiceImplTest.Config.class})
public class JvmServiceImplTest extends VerificationBehaviorSupport {

    private static final String JUNIT_JVM = "junit-jvm";

    @Autowired
    private JvmService jvmService;

    private final Map<String, ReentrantReadWriteLock> lockMap = new HashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmServiceImplTest.class);

    final Identifier<Jvm> id = new Identifier<>(1L);
    final User user = new User("user");

    @Mock
    private Jvm mockJvm;

    @Before
    public void setup() throws IOException {
        initMocks(this);
        FileUtils.forceMkdir(new File(ApplicationProperties.get(PropertyKeys.PATHS_GENERATED_RESOURCE_DIR) + "/" + JUNIT_JVM));

        reset(Config.mockJvmPersistenceService, Config.mockGroupService, Config.mockApplicationService,
                Config.mockMessagingTemplate, Config.mockGroupStateNotificationService, Config.mockResourceService,
                Config.mockClientFactoryHelper, Config.mockJvmControlService, Config.mockBinaryDistributionService,
                Config.mockBinaryDistributionLockManager, Config.mockJvmStateService, Config.mockWebServerPersistenceService, Config.mockGroupPersistenceService);
    }

    @Test
    public void testCreateValidate() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");

        final CreateJvmRequest createJvmRequest = mock(CreateJvmRequest.class);
        final CreateJvmAndAddToGroupsRequest createJvmAndAddToGroupsRequest = mock(CreateJvmAndAddToGroupsRequest.class);
        final Jvm jvm = new Jvm(new Identifier<Jvm>(99L), "testJvm", new HashSet<Group>());

        when(Config.mockJvmPersistenceService.createJvm(any(CreateJvmRequest.class))).thenReturn(jvm);
        when(Config.mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(jvm);
        when(createJvmAndAddToGroupsRequest.getCreateCommand()).thenReturn(createJvmRequest);
        when(createJvmRequest.getJvmName()).thenReturn("TestJvm");
        when(Config.mockJvmPersistenceService.findJvmByExactName(anyString())).thenThrow(NoResultException.class);
        when(Config.mockWebServerPersistenceService.findWebServerByName(anyString())).thenThrow(NoResultException.class);

        jvmService.createJvm(createJvmAndAddToGroupsRequest, Config.mockUser);

        verify(createJvmAndAddToGroupsRequest, times(1)).validate();
        verify(Config.mockJvmPersistenceService, times(1)).createJvm(createJvmRequest);

        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test(expected = JvmServiceException.class)
    public void testvalidateWebserverNameConflict() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");

        final CreateJvmRequest createJvmRequest = mock(CreateJvmRequest.class);
        final CreateJvmAndAddToGroupsRequest createJvmAndAddToGroupsRequest = mock(CreateJvmAndAddToGroupsRequest.class);
        final Jvm jvm = new Jvm(new Identifier<Jvm>(99L), "testJvm", new HashSet<Group>());

        when(Config.mockJvmPersistenceService.createJvm(any(CreateJvmRequest.class))).thenReturn(jvm);
        when(Config.mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(jvm);
        when(createJvmAndAddToGroupsRequest.getCreateCommand()).thenReturn(createJvmRequest);
        when(Config.mockWebServerPersistenceService.findWebServerByName(anyString())).thenReturn(Config.mockWebServer);
        when(createJvmRequest.getJvmName()).thenReturn("TestJvm");
        when(Config.mockJvmPersistenceService.findJvmByExactName(anyString())).thenThrow(NoResultException.class);

        jvmService.createJvm(createJvmAndAddToGroupsRequest, Config.mockUser);

        verify(createJvmAndAddToGroupsRequest, times(1)).validate();
        verify(Config.mockJvmPersistenceService, times(1)).createJvm(createJvmRequest);

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
        when(Config.mockJvmPersistenceService.createJvm(createJvmRequest)).thenReturn(jvm);
        when(Config.mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(jvm);
        when(createJvmRequest.getJvmName()).thenReturn("TestJvm");
        when(Config.mockWebServerPersistenceService.findWebServerByName(anyString())).thenThrow(NoResultException.class);
        when(Config.mockJvmPersistenceService.findJvmByExactName(anyString())).thenThrow(NoResultException.class);

        jvmService.createJvm(command, Config.mockUser);

        verify(command, times(1)).validate();
        verify(Config.mockJvmPersistenceService, times(1)).createJvm(createJvmRequest);
        for (final AddJvmToGroupRequest addCommand : addCommands) {
            verify(Config.mockGroupPersistenceService, times(1)).addJvmToGroup(matchCommand(addCommand));
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
        when(Config.mockJvmPersistenceService.createJvm(any(CreateJvmRequest.class))).thenReturn(jvm);
        when(Config.mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(jvm);
        when(Config.mockResourceService.generateResourceGroup()).thenReturn(mock(ResourceGroup.class));
        when(Config.mockResourceService.getAppTemplate(anyString(), anyString(), anyString())).thenReturn("<context>xml</context>");
        when(mockGroup.getName()).thenReturn("mock-group-name");
        when(Config.mockGroupService.getGroupJvmsResourceTemplateNames(anyString())).thenReturn(templateNames);
        when(Config.mockGroupService.getGroupJvmResourceTemplate(anyString(), anyString(), any(ResourceGroup.class), anyBoolean())).thenReturn("<server>xml</server>");
        when(Config.mockGroupService.getGroupJvmResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"deployPath\":\"c:/fake/path\", \"deployFileName\":\"server-deploy.xml\"}");
        when(Config.mockGroupService.getGroupAppsResourceTemplateNames(anyString())).thenReturn(appTemplateNames);
        when(Config.mockGroupService.getGroupAppResourceTemplateMetaData(anyString(), anyString())).thenReturn("{\"deployPath\":\"c:/fake/app/path\", \"deployFileName\":\"app-context.xml\", \"entity\":{\"deployToJvms\":\"true\", \"target\":\"app-target\"}}");
        when(Config.mockResourceService.getTokenizedMetaData(anyString(), Matchers.anyObject(), anyString())).thenReturn(mockMetaData);
        when(createJvmRequest.getJvmName()).thenReturn("TestJvm");
        when(Config.mockJvmPersistenceService.findJvmByExactName(anyString())).thenThrow(NoResultException.class);
        when(Config.mockWebServerPersistenceService.findWebServerByName(anyString())).thenThrow(NoResultException.class);

        jvmService.createJvm(createJvmAndAddToGroupsRequest, Config.mockUser);

        verify(createJvmAndAddToGroupsRequest, times(1)).validate();
        verify(Config.mockJvmPersistenceService, times(1)).createJvm(createJvmRequest);

        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testCreateValidateInheritsDefaultTemplatesFromMultipleGroups() throws IOException {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");

        final CreateJvmRequest createJvmRequest = mock(CreateJvmRequest.class);
        final CreateJvmAndAddToGroupsRequest createJvmAndAddToGroupsRequest = mock(CreateJvmAndAddToGroupsRequest.class);
        final Group mockGroup = mock(Group.class);
        final Group mockGroup2 = mock(Group.class);

        Set<Group> groupSet = new HashSet<>();
        groupSet.add(mockGroup);
        groupSet.add(mockGroup2);
        List<String> templateNames = new ArrayList<>();
        templateNames.add("server.xml");
        List<String> appTemplateNames = new ArrayList<>();
        final Jvm jvm = new Jvm(new Identifier<Jvm>(99L), "testJvm", groupSet);

        when(createJvmAndAddToGroupsRequest.getCreateCommand()).thenReturn(createJvmRequest);
        when(Config.mockJvmPersistenceService.createJvm(any(CreateJvmRequest.class))).thenReturn(jvm);
        when(Config.mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(jvm);
        when(Config.mockResourceService.generateResourceGroup()).thenReturn(mock(ResourceGroup.class));
        when(mockGroup.getName()).thenReturn("mock-group-name");
        when(Config.mockGroupPersistenceService.getGroupJvmsResourceTemplateNames(anyString())).thenReturn(templateNames);
        when(Config.mockGroupPersistenceService.getGroupAppsResourceTemplateNames(anyString())).thenReturn(appTemplateNames);
        when(createJvmRequest.getJvmName()).thenReturn("TestJvm");
        when(Config.mockWebServerPersistenceService.findWebServerByName(anyString())).thenThrow(NoResultException.class);
        when(Config.mockJvmPersistenceService.findJvmByExactName(anyString())).thenThrow(NoResultException.class);
        when(Config.mockResourceService.getMetaData(anyString())).thenReturn(mock(ResourceTemplateMetaData.class));
        when(Config.mockGroupPersistenceService.getGroupJvmResourceTemplate(anyString(), anyString())).thenReturn("<Server></Server>");

        jvmService.createJvm(createJvmAndAddToGroupsRequest, Config.mockUser);

        verify(createJvmAndAddToGroupsRequest, times(1)).validate();
        verify(Config.mockJvmPersistenceService, times(1)).createJvm(createJvmRequest);

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
        when(Config.mockJvmPersistenceService.createJvm(any(CreateJvmRequest.class))).thenReturn(jvm);
        when(Config.mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(jvm);
        when(Config.mockResourceService.generateResourceGroup()).thenReturn(mock(ResourceGroup.class));
        when(Config.mockResourceService.getMetaData(anyString())).thenThrow(new IOException());
        when(mockGroup.getName()).thenReturn("mock-group-name");
        when(Config.mockGroupPersistenceService.getGroupJvmsResourceTemplateNames(anyString())).thenReturn(templateNames);
        when(Config.mockResourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), anyBoolean(), eq(ResourceGeneratorType.TEMPLATE))).thenReturn("<server>xml</server>");
        when(Config.mockGroupPersistenceService.getGroupJvmResourceTemplateMetaData(anyString(), anyString())).thenReturn("{deployPath:c:/fake/path}");
        when(createJvmRequest.getJvmName()).thenReturn("TestJvm");
        when(Config.mockWebServerPersistenceService.findWebServerByName(anyString())).thenThrow(NoResultException.class);
        when(Config.mockJvmPersistenceService.findJvmByExactName(anyString())).thenThrow(NoResultException.class);

        jvmService.createJvm(createJvmAndAddToGroupsRequest, Config.mockUser);

        verify(createJvmRequest, times(1)).validate();
        verify(Config.mockJvmPersistenceService, times(1)).createJvm(createJvmRequest);

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
        when(Config.mockJvmPersistenceService.createJvm(any(CreateJvmRequest.class))).thenReturn(jvm);
        when(Config.mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(jvm);
        when(Config.mockResourceService.generateResourceGroup()).thenReturn(mock(ResourceGroup.class));
        when(Config.mockResourceService.getAppTemplate(anyString(), anyString(), anyString())).thenReturn("<context>xml</context>");
        when(mockGroup.getName()).thenReturn("mock-group-name");
        when(Config.mockGroupPersistenceService.getGroupJvmsResourceTemplateNames(anyString())).thenReturn(templateNames);
        when(Config.mockGroupPersistenceService.getGroupAppsResourceTemplateNames(anyString())).thenReturn(appTemplateNames);
        when(Config.mockResourceService.getMetaData(anyString())).thenThrow(new IOException());

        when(createJvmRequest.getJvmName()).thenReturn("TestJvm");
        when(Config.mockJvmPersistenceService.findJvmByExactName(anyString())).thenThrow(NoResultException.class);
        when(Config.mockWebServerPersistenceService.findWebServerByName(anyString())).thenThrow(NoResultException.class);
        jvmService.createJvm(createJvmAndAddToGroupsRequest, Config.mockUser);

        verify(createJvmRequest, times(1)).validate();
        verify(Config.mockJvmPersistenceService, times(1)).createJvm(createJvmRequest);

        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testUpdateJvmShouldValidateCommand() {

        final UpdateJvmRequest updateJvmRequest = mock(UpdateJvmRequest.class);
        final Set<AddJvmToGroupRequest> addCommands = createMockedAddRequests(5);

        when(updateJvmRequest.getAssignmentCommands()).thenReturn(addCommands);
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getJvmName()).thenReturn("jvmName");
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(111L));
        when(mockJvm.getState()).thenReturn(JvmState.JVM_NEW);
        when(Config.mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(mockJvm);
        when(Config.mockWebServerPersistenceService.findWebServerByName(anyString())).thenReturn(null);
        when(updateJvmRequest.getNewJvmName()).thenReturn("TestJvm");
        when(Config.mockJvmPersistenceService.findJvmByExactName(anyString())).thenThrow(NoResultException.class);
        when(Config.mockWebServerPersistenceService.findWebServerByName(anyString())).thenThrow(NoResultException.class);

        jvmService.updateJvm(updateJvmRequest, true);

        verify(updateJvmRequest, times(1)).validate();
        verify(Config.mockJvmPersistenceService, times(1)).updateJvm(updateJvmRequest, true);
        verify(Config.mockJvmPersistenceService, times(1)).removeJvmFromGroups(Matchers.<Identifier<Jvm>>anyObject());
        for (final AddJvmToGroupRequest addCommand : addCommands) {
            verify(Config.mockGroupPersistenceService, times(1)).addJvmToGroup(matchCommand(addCommand));
        }
    }

    @Test(expected = JvmServiceException.class)
    public void testUpdateJvmNameShouldFail(){
        final UpdateJvmRequest updateJvmRequest = mock(UpdateJvmRequest.class);
        final String oldjvmName = "old-jvm-name";
        final String newjvmName = "new-jvm-name";
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getJvmName()).thenReturn(oldjvmName);
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(111L));
        when(Config.mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(mockJvm);
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(updateJvmRequest.getNewJvmName()).thenReturn(newjvmName);
        jvmService.updateJvm(updateJvmRequest, true);
    }

    @Test(expected = JvmServiceException.class)
    public void testUpdateJvmNamethrowJvmServiceException(){
        final UpdateJvmRequest updateJvmRequest = mock(UpdateJvmRequest.class);
        final Set<AddJvmToGroupRequest> addCommands = createMockedAddRequests(5);

        when(updateJvmRequest.getAssignmentCommands()).thenReturn(addCommands);
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getJvmName()).thenReturn("jvmName");
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(111L));
        when(mockJvm.getState()).thenReturn(JvmState.JVM_NEW);
        when(Config.mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(mockJvm);
        when(Config.mockWebServerPersistenceService.findWebServerByName(anyString())).thenReturn(Config.mockWebServer);
        when(updateJvmRequest.getNewJvmName()).thenReturn("TestJvm");
        jvmService.updateJvm(updateJvmRequest, true);

        verify(updateJvmRequest, times(1)).validate();
        verify(Config.mockJvmPersistenceService, times(1)).updateJvm(updateJvmRequest, true);
        verify(Config.mockJvmPersistenceService, times(1)).removeJvmFromGroups(Matchers.<Identifier<Jvm>>anyObject());
        for (final AddJvmToGroupRequest addCommand : addCommands) {
            verify(Config.mockGroupPersistenceService, times(1)).addJvmToGroup(matchCommand(addCommand));
        }
    }

    @Test
    public void testUpdateJvmNameShouldWork(){
        final UpdateJvmRequest updateJvmRequest = mock(UpdateJvmRequest.class);
        final Set<AddJvmToGroupRequest> addCommands = createMockedAddRequests(5);
        when(updateJvmRequest.getAssignmentCommands()).thenReturn(addCommands);
        final String oldjvmName = "old-jvm-name";
        final String newjvmName = "new-jvm-name";
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getJvmName()).thenReturn(oldjvmName);
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(111L));
        when(Config.mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(mockJvm);
        when(mockJvm.getState()).thenReturn(JvmState.JVM_NEW);
        when(updateJvmRequest.getNewJvmName()).thenReturn(newjvmName);
        when(Config.mockWebServerPersistenceService.findWebServerByName(anyString())).thenThrow(NoResultException.class);
        when(Config.mockJvmPersistenceService.findJvmByExactName(anyString())).thenThrow(NoResultException.class);
        jvmService.updateJvm(updateJvmRequest, true);
        verify(updateJvmRequest, times(1)).validate();
        verify(Config.mockJvmPersistenceService, times(1)).updateJvm(updateJvmRequest, true);
        verify(Config.mockJvmPersistenceService, times(1)).removeJvmFromGroups(Matchers.<Identifier<Jvm>>anyObject());
        for (final AddJvmToGroupRequest addCommand : addCommands) {
            verify(Config.mockGroupPersistenceService, times(1)).addJvmToGroup(matchCommand(addCommand));
        }
    }

    @Test
    public void testGetAll() {

        jvmService.getJvms();

        verify(Config.mockJvmPersistenceService, times(1)).getJvms();
    }

    @Test
    public void testGetSpecific() {

        final Identifier<Jvm> id = new Identifier<>(-123456L);

        jvmService.getJvm(id);

        verify(Config.mockJvmPersistenceService, times(1)).getJvm(eq(id));
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
        final URI uri = new URI("http://test.com");
        when(jvm.getStatusUri()).thenReturn(new URI("http://test.com"));
        when(Config.mockJvmPersistenceService.getJvm(aJvmId)).thenReturn(jvm);

        ClientHttpResponse mockResponse = mock(ClientHttpResponse.class);
        when(mockResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        when(Config.mockClientFactoryHelper.requestGet(any(URI.class))).thenReturn(mockResponse);

        jvmService.performDiagnosis(aJvmId, new User("user"));
        verify(Config.mockClientFactoryHelper).requestGet(eq(uri));

        reset(Config.mockClientFactoryHelper);
        when(mockResponse.getStatusCode()).thenReturn(HttpStatus.REQUEST_TIMEOUT);
        when(Config.mockClientFactoryHelper.requestGet(any(URI.class))).thenReturn(mockResponse);
        jvmService.performDiagnosis(aJvmId, new User("user"));
        verify(Config.mockClientFactoryHelper).requestGet(eq(uri));
    }

    @Test
    public void testPerformDiagnosisThrowsIOException() throws URISyntaxException, IOException {
        Identifier<Jvm> aJvmId = new Identifier<>(11L);
        Jvm jvm = mock(Jvm.class);
        when(jvm.getId()).thenReturn(aJvmId);
        final URI uri = new URI("http://test.com");
        when(jvm.getStatusUri()).thenReturn(uri);
        when(Config.mockJvmPersistenceService.getJvm(aJvmId)).thenReturn(jvm);

        when(Config.mockClientFactoryHelper.requestGet(any(URI.class))).thenThrow(new IOException("TEST IO EXCEPTION"));
        jvmService.performDiagnosis(aJvmId, new User("user"));
        verify(Config.mockClientFactoryHelper).requestGet(eq(uri));
    }

    @Test
    public void testPerformDiagnosisThrowsRuntimeException() throws IOException, URISyntaxException {
        Identifier<Jvm> aJvmId = new Identifier<>(11L);
        Jvm jvm = mock(Jvm.class);
        when(jvm.getId()).thenReturn(aJvmId);
        when(jvm.getStatusUri()).thenReturn(new URI("http://test.com"));
        when(Config.mockJvmPersistenceService.getJvm(aJvmId)).thenReturn(jvm);

        when(Config.mockClientFactoryHelper.requestGet(any(URI.class))).thenThrow(new RuntimeException("RUN!!"));
        when(jvm.getState()).thenReturn(JvmState.JVM_STARTED);
        jvmService.performDiagnosis(aJvmId, Config.mockUser);
        verify(Config.mockHistoryFacadeService).write(anyString(), anyCollection(), eq("Diagnose and resolve state"),
                eq(EventType.USER_ACTION_INFO), anyString());
    }

    @Test
    public void testGetResourceTemplateNames() {
        String testJvmName = "testJvmName";
        ArrayList<String> value = new ArrayList<>();
        when(Config.mockJvmPersistenceService.getResourceTemplateNames(testJvmName)).thenReturn(value);
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
        when(Config.mockJvmPersistenceService.getResourceTemplate(testJvmName, resourceTemplateName)).thenReturn(expectedValue);
        List<Jvm> jvmList = new ArrayList<>();
        jvmList.add(jvm);
        when(Config.mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(jvm);
        when(Config.mockResourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        when(Config.mockResourceService.generateResourceFile(eq(resourceTemplateName), eq(expectedValue), any(ResourceGroup.class), eq(jvm), any(ResourceGeneratorType.class))).thenReturn(expectedValue);
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
        when(Config.mockJvmPersistenceService.updateResourceTemplate(testJvmName, resourceTemplateName, template)).thenReturn(template);
        String result = jvmService.updateResourceTemplate(testJvmName, resourceTemplateName, template);
        assertEquals(template, result);
    }

    @Test
    public void testGetJvmByName() {
        jvmService.getJvm("testJvm");
        verify(Config.mockJvmPersistenceService).findJvmByExactName("testJvm");
    }

    @Test
    public void testPreviewTemplate() {
        final String jvmName = "jvm-1Test";
        Jvm testJvm = new Jvm(new Identifier<Jvm>(111L), jvmName, "testHost", new HashSet<Group>(), 9101, 9102, 9103, -1, 9104, new Path("./"), "", JvmState.JVM_STOPPED, "", null, null, null, null, null, null, null);
        List<Jvm> jvmList = new ArrayList<>();
        jvmList.add(testJvm);
        when(Config.mockJvmPersistenceService.findJvm(anyString(), anyString())).thenReturn(testJvm);
        when(Config.mockResourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        when(Config.mockResourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), eq(testJvm), any(ResourceGeneratorType.class))).thenReturn("TEST jvm-1Test TEST");

        String preview = jvmService.previewResourceTemplate("myFile", jvmName, "groupTest", "TEST ${jvm.jvmName} TEST");
        assertEquals("TEST jvm-1Test TEST", preview);
    }

    @Test
    public void testUpdateState() {
        Identifier<Jvm> jvmId = new Identifier<Jvm>(999L);
        jvmService.updateState(jvmId, JvmState.JVM_STOPPED);
        verify(Config.mockJvmPersistenceService).updateState(jvmId, JvmState.JVM_STOPPED, "");
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

        when(Config.mockApplicationService.getResourceTemplateNames(anyString(), anyString())).thenReturn(templateNamesList);
        final User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("user-id");

        when(Config.mockApplicationService.findApplications(any(Identifier.class))).thenReturn(appList);
        when(Config.mockJvmPersistenceService.findGroupsByJvm(any(Identifier.class))).thenReturn(groupsList);
        jvmService.deployApplicationContextXMLs(jvm, mockUser);
        verify(Config.mockApplicationService).deployConf(anyString(), anyString(), anyString(), anyString(), any(ResourceGroup.class), any(User.class));
    }

    @Test
    public void testGenerateAndDeployConfig() throws CommandFailureException, IOException {

        CommandOutput commandOutput = mock(CommandOutput.class);
        Jvm mockJvm = mock(Jvm.class);
        ResourceGroup mockResourceGroup = mock(ResourceGroup.class);

        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(mockJvm.getJvmName()).thenReturn("test-jvm-deploy-config");
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(111L));
        when(mockJvm.getJdkMedia()).thenReturn(new Media(1L, "test JDK media", MediaType.JDK,
                Paths.get("x:/test/archive/path.zip"), Paths.get("x:/test-destination"), Paths.get("root-dir-destination")));
        when(mockJvm.getTomcatMedia()).thenReturn(new Media(2L, "test Tomcat media", MediaType.TOMCAT,
                Paths.get("./src/test/resources/binaries/apache-tomcat-test.zip"), Paths.get("x:/test-destination-tomcat"), Paths.get("tomcat-root-dir-destination")));
        when(commandOutput.getReturnCode()).thenReturn(new ExecReturnCode(0));
        when(Config.mockJvmControlService.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString(), anyString(), anyBoolean())).thenReturn(commandOutput);
        when(Config.mockJvmControlService.executeCreateDirectoryCommand(any(Jvm.class), anyString())).thenReturn(commandOutput);
        when(Config.mockJvmControlService.executeChangeFileModeCommand(any(Jvm.class), anyString(), anyString(), anyString())).thenReturn(commandOutput);
        when(Config.mockJvmControlService.controlJvm(eq(ControlJvmRequestFactory.create(JvmControlOperation.DELETE_SERVICE, mockJvm)), any(User.class))).thenReturn(commandOutput);
        when(Config.mockJvmControlService.controlJvm(eq(ControlJvmRequestFactory.create(JvmControlOperation.DEPLOY_JVM_ARCHIVE, mockJvm)), any(User.class))).thenReturn(commandOutput);
        when(Config.mockJvmControlService.controlJvm(eq(ControlJvmRequestFactory.create(JvmControlOperation.INSTALL_SERVICE, mockJvm)), any(User.class))).thenReturn(commandOutput);
        when(Config.mockJvmControlService.executeCheckFileExistsCommand(any(Jvm.class), anyString())).thenReturn(commandOutput);

        when(Config.mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        when(Config.mockJvmPersistenceService.getJvmTemplate(anyString(), any(Identifier.class))).thenReturn("<server>some xml</server>");

        when(Config.mockResourceService.generateResourceGroup()).thenReturn(mockResourceGroup);
        when(Config.mockResourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), anyObject(), any(ResourceGeneratorType.class))).thenReturn("<server>some xml</server>");

        final List<String> templateNames = new ArrayList<>();
        templateNames.add("setenv.bat");
        when(Config.mockJvmPersistenceService.getResourceTemplateNames(anyString())).thenReturn(templateNames);

        Jvm response = jvmService.generateAndDeployJvm(mockJvm.getJvmName(), Config.mockUser);
        assertEquals(response.getJvmName(), mockJvm.getJvmName());

        // test failing the invoke service
        CommandOutput mockExecDataFail = mock(CommandOutput.class);
        when(mockExecDataFail.getReturnCode()).thenReturn(new ExecReturnCode(1));
        when(mockExecDataFail.getStandardError()).thenReturn("ERROR");

        when(Config.mockJvmControlService.controlJvm(eq(new InstallServiceControlJvmRequest(mockJvm)), any(User.class))).thenReturn(mockExecDataFail);

        boolean exceptionThrown = false;
        try {
            jvmService.generateAndDeployJvm(mockJvm.getJvmName(), Config.mockUser);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try {
            jvmService.generateAndDeployJvm(mockJvm.getJvmName(), Config.mockUser);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        // test secure copy fails
        when(Config.mockJvmControlService.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString(), anyString(), anyBoolean())).thenReturn(mockExecDataFail);
        exceptionThrown = false;
        try {
            jvmService.generateAndDeployJvm(mockJvm.getJvmName(), Config.mockUser);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        // test secure copy throws a command failure exception
        ExecCommand execCommand = new ExecCommand("fail command");
        Throwable throwable = new JSchException("Failed scp");
        final CommandFailureException commandFailureException = new CommandFailureException(execCommand, throwable);
        when(Config.mockJvmControlService.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString(), anyString(), anyBoolean())).thenThrow(commandFailureException);
        exceptionThrown = false;
        try {
            jvmService.generateAndDeployJvm(mockJvm.getJvmName(), Config.mockUser);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try {
            jvmService.generateAndDeployJvm(mockJvm.getJvmName(), Config.mockUser);
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

        when(Config.mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STARTED);
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(111L));
        when(mockJvm.getHostName()).thenReturn("testHostName");
        jvmService.generateAndDeployJvm("test-jvm-fails-started", Config.mockUser);
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

        when(Config.mockApplicationService.getResourceTemplateNames(anyString(), anyString())).thenReturn(appResourceNamesList);
        when(Config.mockApplicationService.findApplications(any(Identifier.class))).thenReturn(appList);
        when(Config.mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        when(Config.mockJvmPersistenceService.findGroupsByJvm(any(Identifier.class))).thenReturn(groupList);
        doThrow(new InternalErrorException(FaultType.RESOURCE_GENERATION_FAILED, "Test for failing JVM resource generation", null, jvmErrorMap)).when(Config.mockResourceService).validateAllResourcesForGeneration(any(ResourceIdentifier.class));
        doThrow(new InternalErrorException(FaultType.RESOURCE_GENERATION_FAILED, "Test failing for Web App resource generation", null, appErrorMap)).when(Config.mockResourceService).validateSingleResourceForGeneration(any(ResourceIdentifier.class));

        InternalErrorException caughtException = null;
        try {
            jvmService.generateAndDeployJvm("test-jvm-fails-resource-generation", Config.mockUser);
        } catch (InternalErrorException iee) {
            caughtException = iee;
        }
        verify(Config.mockResourceService).validateSingleResourceForGeneration(any(ResourceIdentifier.class));
        verify(Config.mockResourceService).validateAllResourcesForGeneration(any(ResourceIdentifier.class));

        assertNotNull(caughtException);
        assertEquals(2, caughtException.getErrorDetails().size());
        assertEquals(3, caughtException.getErrorDetails().get("test-jvm-fails-resource-generation").size());
        assertEquals(2, caughtException.getErrorDetails().get("test-jvm-app-fails-resource-generation").size());
    }

    @Test(expected = InternalErrorException.class)
    public void testGenerateAndDeployJVMFailsCreateDirectory() throws CommandFailureException {
        Jvm mockJvm = mock(Jvm.class);
        CommandOutput commandOutputFails = new CommandOutput(new ExecReturnCode(1), "", "Fail creating the directory");

        when(Config.mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        when(Config.mockJvmControlService.executeCreateDirectoryCommand(any(Jvm.class), anyString())).thenReturn(commandOutputFails);
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(111L));
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);

        jvmService.generateAndDeployJvm("test-jvm-fails-started", Config.mockUser);
    }

    @Test(expected = InternalErrorException.class)
    public void testGenerateAndDeployJVMFailsDeployingInvokeService() throws CommandFailureException {
        Jvm mockJvm = mock(Jvm.class);
        CommandOutput commandOutputFails = new CommandOutput(new ExecReturnCode(1), "", "Fail creating the directory");
        CommandOutput commandOutputSucceeds = new CommandOutput(new ExecReturnCode(0), "SUCCESS", "");

        when(Config.mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        when(Config.mockJvmControlService.executeCreateDirectoryCommand(any(Jvm.class), anyString())).thenReturn(commandOutputSucceeds);
        when(Config.mockJvmControlService.secureCopyFile(any(ControlJvmRequest.class), contains(AemControl.Properties.DEPLOY_CONFIG_ARCHIVE_SCRIPT_NAME.getValue()), anyString(), anyString(), anyBoolean())).thenReturn(commandOutputSucceeds);
        when(Config.mockJvmControlService.secureCopyFile(any(ControlJvmRequest.class), contains(AemControl.Properties.INSTALL_SERVICE_SCRIPT_NAME.getValue()), anyString(), anyString(), anyBoolean())).thenReturn(commandOutputFails);
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(111L));
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);

        jvmService.generateAndDeployJvm("test-jvm-fails-started", Config.mockUser);
    }

    @Test(expected = InternalErrorException.class)
    public void testGenerateAndDeployJVMFailsChangeFileMode() throws CommandFailureException {
        Jvm mockJvm = mock(Jvm.class);
        CommandOutput commandOutputFails = new CommandOutput(new ExecReturnCode(1), "", "Fail creating the directory");
        CommandOutput commandOutputSucceeds = new CommandOutput(new ExecReturnCode(0), "SUCCESS", "");

        when(Config.mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        when(Config.mockJvmControlService.executeCreateDirectoryCommand(any(Jvm.class), anyString())).thenReturn(commandOutputSucceeds);
        when(Config.mockJvmControlService.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString(), anyString(), anyBoolean())).thenReturn(commandOutputSucceeds);
        when(Config.mockJvmControlService.executeChangeFileModeCommand(any(Jvm.class), anyString(), anyString(), anyString())).thenReturn(commandOutputFails);
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(111L));
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);

        jvmService.generateAndDeployJvm("test-jvm-fails-started", Config.mockUser);
    }

    @Test
    public void testJvmCounts() {
        when(Config.mockJvmPersistenceService.getJvmStartedCount(anyString())).thenReturn(1L);
        when(Config.mockJvmPersistenceService.getJvmCount(anyString())).thenReturn(6L);
        when(Config.mockJvmPersistenceService.getJvmStoppedCount(anyString())).thenReturn(2L);
        when(Config.mockJvmPersistenceService.getJvmForciblyStoppedCount(anyString())).thenReturn(3L);

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
    public void testGenerateAndDeployFile() throws CommandFailureException, IOException {
        CommandOutput mockExecData = mock(CommandOutput.class);
        final Jvm mockJvm = mockJvmWithId(new Identifier<Jvm>(111L));
        ResourceTemplateMetaData mockMetaData = mock(ResourceTemplateMetaData.class);

        when(mockJvm.getJvmName()).thenReturn("test-jvm-deploy-file");
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(0));
        when(Config.mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        when(Config.mockResourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), anyString(), any(ResourceGeneratorType.class))).thenReturn("<server>xml</server>");
        when(Config.mockResourceService.getResourceContent(any(ResourceIdentifier.class))).thenReturn(new ResourceContent("{\"fake\":\"meta-data\"}","some template content"));
        when(Config.mockResourceService.getTokenizedMetaData(anyString(), any(Jvm.class), anyString())).thenReturn(new ResourceTemplateMetaData("template-name", org.apache.tika.mime.MediaType.APPLICATION_XML, "deploy-file-name", "deploy-path", null, false, true, false));
        when(Config.mockJvmPersistenceService.getJvmTemplate(anyString(), any(Identifier.class))).thenReturn("<server>xml</server>");
        when(Config.mockJvmControlService.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString(), anyString(), anyBoolean())).thenReturn(mockExecData);
        when(Config.mockJvmControlService.executeCreateDirectoryCommand(any(Jvm.class), anyString())).thenReturn(mockExecData);
        when(Config.mockResourceService.generateResourceGroup()).thenReturn(new ResourceGroup());
        when(Config.mockResourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), any(), any(ResourceGeneratorType.class))).thenReturn("{\"deployFileName\":\"server.xml\", \"deployPath\":\"/\",\"contentType\":\"text/plain\"}");
        when(mockMetaData.getDeployFileName()).thenReturn("server.xml");
        when(mockMetaData.getDeployPath()).thenReturn("/");
        when(mockMetaData.getContentType()).thenReturn(org.apache.tika.mime.MediaType.APPLICATION_XML);
        when(Config.mockResourceService.getTokenizedMetaData(anyString(), Matchers.anyObject(), anyString())).thenReturn(mockMetaData);

        Jvm jvm = jvmService.generateAndDeployFile("test-jvm-deploy-file", "server.xml", Config.mockUser);
        assertEquals(mockJvm, jvm);

        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(1));
        when(mockExecData.getStandardError()).thenReturn("ERROR");
        when(Config.mockResourceService.generateAndDeployFile(any(ResourceIdentifier.class), anyString(), anyString(), anyString())).thenThrow(new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, "xxx"));

        boolean exceptionThrown = false;
        try {
            jvmService.generateAndDeployFile(jvm.getJvmName(), "server.xml", Config.mockUser);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try {
            jvmService.generateAndDeployFile(jvm.getJvmName(), "server.xml", Config.mockUser);
        } catch (InternalErrorException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        when(Config.mockJvmControlService.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString(), anyString(), anyBoolean())).thenThrow(new CommandFailureException(new ExecCommand("fail for secure copy"), new Throwable("test fail")));
        exceptionThrown = false;
        try {
            jvmService.generateAndDeployFile(jvm.getJvmName(), "server.xml", Config.mockUser);
        } catch (InternalErrorException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try {
            jvmService.generateAndDeployFile(jvm.getJvmName(), "server.xml", Config.mockUser);
        } catch (InternalErrorException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    @Test(expected = InternalErrorException.class)
    public void testGenerateAndDeployFileJvmStarted() throws IOException {
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STARTED);
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(11111L));
        when(Config.mockResourceService.getResourceContent(any(ResourceIdentifier.class))).thenReturn(new ResourceContent("{\"fake\":\"meta-data\"}","some template content"));
        when(Config.mockResourceService.getTokenizedMetaData(anyString(), any(Jvm.class), anyString())).thenReturn(new ResourceTemplateMetaData("template-name", org.apache.tika.mime.MediaType.APPLICATION_XML, "deploy-file-name", "deploy-path", null, false, true, false));
        when(Config.mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        jvmService.generateAndDeployFile("jvmName", "fileName", Config.mockUser);
    }

    @Test
    public void testGenerateAndDeployFileJvmStartedHotDeployTrue() throws IOException {
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STARTED);
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(11111L));
        when(Config.mockResourceService.getResourceContent(any(ResourceIdentifier.class))).thenReturn(new ResourceContent("{\"fake\":\"meta-data\"}","some template content"));
        when(Config.mockResourceService.getTokenizedMetaData(anyString(), any(Jvm.class), anyString())).thenReturn(new ResourceTemplateMetaData("template-name", org.apache.tika.mime.MediaType.APPLICATION_XML, "deploy-file-name", "deploy-path", null, false, true, true));
        when(Config.mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);

        jvmService.generateAndDeployFile("jvmName", "fileName", Config.mockUser);

        verify(Config.mockResourceService).validateSingleResourceForGeneration(any(ResourceIdentifier.class));
        verify(Config.mockResourceService).generateAndDeployFile(any(ResourceIdentifier.class), anyString(), anyString(), anyString());
    }

    @Test (expected = JvmServiceException.class)
    public void testGenerateAndDeployFileJvmStartedHotDeployTrueThrowsIOException() throws IOException {
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STARTED);
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(11111L));
        when(Config.mockResourceService.getResourceContent(any(ResourceIdentifier.class))).thenReturn(new ResourceContent("{\"fake\":\"meta-data\"}","some template content"));
        when(Config.mockResourceService.getTokenizedMetaData(anyString(), any(Jvm.class), anyString())).thenThrow(new IOException("Test throwing the IOException during tokenization"));
        when(Config.mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);

        jvmService.generateAndDeployFile("jvmName", "fileName", Config.mockUser);

        verify(Config.mockResourceService, never()).validateSingleResourceForGeneration(any(ResourceIdentifier.class));
        verify(Config.mockResourceService, never()).generateAndDeployFile(any(ResourceIdentifier.class), anyString(), anyString(), anyString());
    }

    @Test
    public void testGenerateJvmConfigJar() {
        Set<Group> groups = new HashSet<Group>() {{
            add(new Group(Identifier.id(2L, Group.class), "junit-group"));
        }};

        Media mockTomcatMedia = mock(Media.class);
        java.nio.file.Path mockPath = mock(java.nio.file.Path.class);
        when(mockPath.toFile()).thenReturn(new File("./src/test/resources/binaries/apache-tomcat-test.zip"));
        when(mockTomcatMedia.getLocalPath()).thenReturn(mockPath);

        Jvm jvm = mock(Jvm.class);
        when(jvm.getJvmName()).thenReturn(JUNIT_JVM);
        when(jvm.getGroups()).thenReturn(groups);
        when(jvm.getTomcatMedia()).thenReturn(mockTomcatMedia);

        when(Config.mockJvmPersistenceService.findJvmByExactName(jvm.getJvmName())).thenReturn(jvm);
        when(Config.mockResourceService.generateResourceFile(anyString(),
                anyString(),
                any(ResourceGroup.class),
                any(Jvm.class),
                eq(ResourceGeneratorType.TEMPLATE))).thenReturn("some file content");
        try {
            ((JvmServiceImpl) jvmService).generateJvmConfigJar(jvm);
        } catch (CommandFailureException e) {
            LOGGER.error("Failed to generate remote jar.", e);
        }
    }

    @Test
    public void testGenerateJvmConfigJarCopiesManagerXML() {
        Set<Group> groups = new HashSet<Group>() {{
            add(new Group(Identifier.id(2L, Group.class), "junit-group"));
        }};

        Media mockTomcatMedia = mock(Media.class);
        java.nio.file.Path mockPath = mock(java.nio.file.Path.class);
        when(mockPath.toFile()).thenReturn(new File("./src/test/resources/binaries/apache-tomcat-test.zip"));
        when(mockTomcatMedia.getLocalPath()).thenReturn(mockPath);

        Jvm jvm = mock(Jvm.class);
        when(jvm.getJvmName()).thenReturn(JUNIT_JVM);
        when(jvm.getGroups()).thenReturn(groups);
        when(jvm.getTomcatMedia()).thenReturn(mockTomcatMedia);

        when(Config.mockJvmPersistenceService.findJvmByExactName(jvm.getJvmName())).thenReturn(jvm);
        when(Config.mockResourceService.generateResourceFile(anyString(),
                anyString(),
                any(ResourceGroup.class),
                any(Jvm.class),
                eq(ResourceGeneratorType.TEMPLATE))).thenReturn("some file content");

        ((JvmServiceImpl) jvmService).generateJvmConfigJar(jvm);
        verify(Config.mockJvmPersistenceService).findJvmByExactName(anyString());
    }

    @Test
    public void testDeleteNewJvm() {
        when(mockJvm.getState()).thenReturn(JvmState.JVM_NEW);
        when(Config.mockJvmPersistenceService.getJvm(id)).thenReturn(mockJvm);
        jvmService.deleteJvm(id, false, user);
        verify(Config.mockJvmControlService, never()).controlJvm(any(ControlJvmRequest.class), eq(user));
        verify(Config.mockJvmPersistenceService).removeJvm(id);
    }

    @Test
    public void testDeleteStoppedJvm() {
        final CommandOutput mockCommandOutput = mock(CommandOutput.class);
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(Config.mockJvmPersistenceService.getJvm(id)).thenReturn(mockJvm);
        when(mockCommandOutput.getReturnCode()).thenReturn(new ExecReturnCode(0));
        when(Config.mockJvmControlService.controlJvm(any(ControlJvmRequest.class), eq(user))).thenReturn(mockCommandOutput);
        jvmService.deleteJvm(id, true, user);
        verify(Config.mockJvmControlService).controlJvm(any(ControlJvmRequest.class), eq(user));
        verify(Config.mockJvmPersistenceService).removeJvm(id);
    }

    @Test
    public void testDeleteForcedStoppedJvm() {
        final CommandOutput mockCommandOutput = mock(CommandOutput.class);
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(Config.mockJvmPersistenceService.getJvm(id)).thenReturn(mockJvm);
        when(mockCommandOutput.getReturnCode()).thenReturn(new ExecReturnCode(0));
        when(Config.mockJvmControlService.controlJvm(any(ControlJvmRequest.class), eq(user))).thenReturn(mockCommandOutput);
        jvmService.deleteJvm(id, true, user);
        verify(Config.mockJvmControlService).controlJvm(any(ControlJvmRequest.class), eq(user));
        verify(Config.mockJvmPersistenceService).removeJvm(id);
    }

    @Test
    public void testDeleteNewJvmWithHardDeleteOption() {
        final CommandOutput mockCommandOutput = mock(CommandOutput.class);
        when(mockJvm.getState()).thenReturn(JvmState.JVM_NEW);
        when(Config.mockJvmPersistenceService.getJvm(id)).thenReturn(mockJvm);
        when(mockCommandOutput.getReturnCode()).thenReturn(new ExecReturnCode(0));
        when(Config.mockJvmControlService.controlJvm(any(ControlJvmRequest.class), eq(user))).thenReturn(mockCommandOutput);
        jvmService.deleteJvm(id, true, user);
        verify(Config.mockJvmControlService, never()).controlJvm(any(ControlJvmRequest.class), eq(user));
        verify(Config.mockJvmPersistenceService).removeJvm(id);
    }

    @Test
    public void testFailedDeleteServiceOfStoppedJvm() {
        final CommandOutput mockCommandOutput = mock(CommandOutput.class);
        when(mockJvm.getJvmName()).thenReturn("testJvm");
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(Config.mockJvmPersistenceService.getJvm(id)).thenReturn(mockJvm);
        when(mockCommandOutput.getReturnCode()).thenReturn(new ExecReturnCode(ExecReturnCode.JWALA_EXIT_NO_SUCH_SERVICE));
        when(Config.mockJvmControlService.controlJvm(any(ControlJvmRequest.class), eq(user))).thenReturn(mockCommandOutput);
        try {
            jvmService.deleteJvm(id, true, user);
        } catch (final JvmServiceException e) {
            assertTrue(e.getMessage().indexOf("Failed to delete the JVM service testJvm!") == 0);
        }
        verify(Config.mockJvmControlService).controlJvm(any(ControlJvmRequest.class), eq(user));
        verify(Config.mockJvmPersistenceService, never()).removeJvm(id);
    }

    @Test
    public void testDeleteNonStoppedAndNotNewJvm() {
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STARTED);
        when(Config.mockJvmPersistenceService.getJvm(id)).thenReturn(mockJvm);
        try {
            jvmService.deleteJvm(id, true, user);
            fail("Expecting to get a JvmServiceException!");
        } catch (final JvmServiceException e) {
            assertEquals("Please stop JVM null first before attempting to delete it", e.getMessage());
        }
        verify(Config.mockJvmPersistenceService, never()).removeJvm(id);
    }

    @Configuration
    static class Config {

        // Was initially using @Mock but an intermittent problem with testPerformDiagnosis happens with the said mocking approach.
        // Sometimes pingAndUpdateState of JvmServiceImpl throws a NPE at
        // jvmStateService.updateState(jvm.getId(), JvmState.JVM_STOPPED, StringUtils.EMPTY);
        // even though the mocks were initialized.
        static JvmPersistenceService mockJvmPersistenceService = mock(JvmPersistenceService.class);

        static GroupService mockGroupService = mock(GroupService.class);

        static User mockUser = mock(User.class);

        static ApplicationService mockApplicationService = mock(ApplicationService.class);

        static SimpMessagingTemplate mockMessagingTemplate = mock(SimpMessagingTemplate.class);

        static GroupStateNotificationService mockGroupStateNotificationService = mock(GroupStateNotificationService.class);

        static HttpComponentsClientHttpRequestFactory mockHttpClientFactory = mock(HttpComponentsClientHttpRequestFactory.class);

        static ClientFactoryHelper mockClientFactoryHelper = mock(ClientFactoryHelper.class);

        static ResourceService mockResourceService = mock(ResourceService.class);

        static JvmControlService mockJvmControlService = mock(JvmControlService.class);

        static BinaryDistributionService mockBinaryDistributionService = mock(BinaryDistributionService.class);

        static BinaryDistributionLockManager mockBinaryDistributionLockManager = mock(BinaryDistributionLockManager.class);

        static JvmStateService mockJvmStateService = mock(JvmStateService.class);

        static MessagingService mockMessagingService = mock(MessagingService.class);

        static HistoryService mockHistoryService = mock(HistoryService.class);

        static GroupPersistenceService mockGroupPersistenceService = mock(GroupPersistenceService.class);

        static HistoryFacadeService mockHistoryFacadeService = mock(HistoryFacadeService.class);

        static WebServerPersistenceService mockWebServerPersistenceService = mock(WebServerPersistenceService.class);

        static WebServer mockWebServer = mock(WebServer.class);

        @Bean
        public JvmPersistenceService getMockJvmPersistenceService() {
            return mockJvmPersistenceService;
        }

        @Bean
        public GroupService getMockGroupService() {
            return mockGroupService;
        }

        @Bean
        public User getMockUser() {
            return mockUser;
        }

        @Bean
        public ApplicationService getMockApplicationService() {
            return mockApplicationService;
        }

        @Bean
        public SimpMessagingTemplate getMockMessagingTemplate() {
            return mockMessagingTemplate;
        }

        @Bean
        public GroupStateNotificationService getMockGroupStateNotificationService() {
            return mockGroupStateNotificationService;
        }

        @Bean(name = "httpRequestFactory")
        public static HttpComponentsClientHttpRequestFactory getMockHttpClientFactory() {
            return mockHttpClientFactory;
        }

        @Bean
        public ClientFactoryHelper getMockClientFactoryHelper() {
            return mockClientFactoryHelper;
        }

        @Bean
        public ResourceService getMockResourceService() {
            return mockResourceService;
        }

        @Bean
        public JvmControlService getMockJvmControlService() {
            return mockJvmControlService;
        }

        @Bean
        public BinaryDistributionService getMockBinaryDistributionService() {
            return mockBinaryDistributionService;
        }

        @Bean
        public BinaryDistributionLockManager getMockBinaryDistributionLockManager() {
            return mockBinaryDistributionLockManager;
        }

        @Bean
        public WebServerPersistenceService getWebServerPersistenceService() {
            return mockWebServerPersistenceService;
        }

        @Bean
        public JvmStateService getMockJvmStateService() {
            return mockJvmStateService;
        }

        @Bean
        public static MessagingService getMockMessagingService() {
            return mockMessagingService;
        }

        @Bean
        public static HistoryService getMockHistoryService() {
            return mockHistoryService;
        }

        @Bean
        public static GroupPersistenceService getMockGroupPersistenceService() {
            return mockGroupPersistenceService;
        }

        @Bean
        public static HistoryFacadeService getMockHistoryFacadeService() {
            return mockHistoryFacadeService;
        }

        @Bean
        public JvmService getJvmService() {
            return new JvmServiceImpl(mockJvmPersistenceService, mockGroupPersistenceService, mockApplicationService,
                    mockMessagingTemplate, mockGroupStateNotificationService, mockResourceService, mockClientFactoryHelper,
                    "/topic/server-states", mockJvmControlService, mockBinaryDistributionService, mockBinaryDistributionLockManager,
                    mockHistoryFacadeService, new FileUtility());
        }

    }

}