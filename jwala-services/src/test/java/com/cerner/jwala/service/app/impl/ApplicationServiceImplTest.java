package com.cerner.jwala.service.app.impl;

import com.cerner.jwala.common.JwalaUtils;
import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.resource.*;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.request.app.CreateApplicationRequest;
import com.cerner.jwala.common.request.app.UpdateApplicationRequest;
import com.cerner.jwala.common.request.app.UploadAppTemplateRequest;
import com.cerner.jwala.control.configuration.SshConfig;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.persistence.jpa.domain.JpaApplication;
import com.cerner.jwala.persistence.jpa.domain.JpaGroup;
import com.cerner.jwala.persistence.jpa.domain.JpaJvm;
import com.cerner.jwala.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.cerner.jwala.persistence.service.ApplicationPersistenceService;
import com.cerner.jwala.persistence.service.GroupPersistenceService;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.persistence.service.ResourceDao;
import com.cerner.jwala.service.HistoryFacadeService;
import com.cerner.jwala.service.app.ApplicationService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionControlService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionLockManager;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionService;
import com.cerner.jwala.service.binarydistribution.impl.BinaryDistributionLockManagerImpl;
import com.cerner.jwala.service.exception.ApplicationServiceException;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.service.resource.impl.ResourceGeneratorType;
import org.apache.tika.mime.MediaType;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {ApplicationServiceImplTest.Config.class})
@PrepareForTest(JwalaUtils.class )
public class ApplicationServiceImplTest {

    static final String META_DATA_TEST_VALUES = "{\"deployPath\":\"./test/deploy-path/conf/CatalinaSSL/localhost\",\"contentType\":\"text/xml\",\"entity\":{\"type\":\"APPLICATION\",\"target\":\"soarcom-hct\",\"group\":\"soarcom-616\",\"parentName\":null,\"deployToJvms\":true},\"templateName\":\"hctXmlTemplate.tpl\",\"deployFileName\":\"hct.xml\"}";

    private SshConfig sshConfig;

    @Autowired
    private ApplicationServiceImpl applicationService;

    private Group group;
    private Group group2;
    private Identifier<Group> groupId;
    private Identifier<Group> groupId2;

    private ArrayList<Application> applications2 = new ArrayList<>(1);

    private User testUser = new User("testUser");

    // Managed by setup/teardown
    ByteArrayInputStream uploadedFile;
    Application app;

    @BeforeClass
    public static void init() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources");
    }

    @Before
    public void setUp() {
        groupId = new Identifier<Group>(1L);
        groupId2 = new Identifier<Group>(2L);
        group = new Group(groupId, "the-ws-group-name");
        group2 = new Group(groupId2, "the-ws-group-name-2");
        try {
            PowerMockito.mockStatic(JwalaUtils.class);
            PowerMockito.when(JwalaUtils.getHostAddress("testServer")).thenReturn(Inet4Address.getLocalHost().getHostAddress());
            PowerMockito.when(JwalaUtils.getHostAddress("testServer2")).thenReturn(Inet4Address.getLocalHost().getHostAddress());
        }catch (UnknownHostException ex){
            ex.printStackTrace();
        }
        when(Config.mockApplication.getId()).thenReturn(new Identifier<Application>(1L));
        when(Config.mockApplication.getWarPath()).thenReturn("the-ws-group-name/jwala-1.0.war");
        when(Config.mockApplication.getName()).thenReturn("jwala 1.0");
        when(Config.mockApplication.getGroup()).thenReturn(group);
        when(Config.mockApplication.getWebAppContext()).thenReturn("/jwala");
        when(Config.mockApplication.isSecure()).thenReturn(true);

        when(Config.mockApplication2.getId()).thenReturn(new Identifier<Application>(2L));
        when(Config.mockApplication2.getWarPath()).thenReturn("the-ws-group-name-2/jwala-1.1.war");
        when(Config.mockApplication2.getName()).thenReturn("jwala 1.1");
        when(Config.mockApplication2.getGroup()).thenReturn(group2);
        when(Config.mockApplication2.getWebAppContext()).thenReturn("/jwala");
        when(Config.mockApplication2.isSecure()).thenReturn(false);

        applications2.add(Config.mockApplication);
        applications2.add(Config.mockApplication2);

        ByteBuffer buf = java.nio.ByteBuffer.allocate(2); // 2 byte file
        buf.asShortBuffer().put((short) 0xc0de);

        uploadedFile = new ByteArrayInputStream(buf.array());

        SshConfiguration mockSshConfig = mock(SshConfiguration.class);
        sshConfig = mock(SshConfig.class);
        when(mockSshConfig.getUserName()).thenReturn("mockUser");
        when(sshConfig.getSshConfiguration()).thenReturn(mockSshConfig);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSingleGet() {
        when(Config.applicationPersistenceService.getApplication(any(Identifier.class))).thenReturn(Config.mockApplication);
        final Application application = applicationService.getApplication(new Identifier<Application>(1L));
        assertEquals(new Identifier<Application>(1L), application.getId());
        assertEquals(groupId, application.getGroup().getId());
        assertEquals("jwala 1.0", application.getName());
        assertEquals("the-ws-group-name", application.getGroup().getName());
        assertEquals("the-ws-group-name/jwala-1.0.war", application.getWarPath());
    }

    @Test
    public void testGetApplicationByName() {
        when(Config.applicationPersistenceService.getApplication(anyString())).thenReturn(Config.mockApplication);
        final Application application = applicationService.getApplication("jwala 1.0");
        assertEquals(new Identifier<Application>(1L), application.getId());
        assertEquals(groupId, application.getGroup().getId());
        assertEquals("jwala 1.0", application.getName());
        assertEquals("the-ws-group-name", application.getGroup().getName());
        assertEquals("the-ws-group-name/jwala-1.0.war", application.getWarPath());
    }

    @Test
    public void testAllGet() {
        when(Config.applicationPersistenceService.getApplications()).thenReturn(applications2);
        final List<Application> apps = applicationService.getApplications();
        assertEquals(applications2.size(), apps.size());

        Application application = apps.get(0);
        assertEquals(new Identifier<Application>(1L), application.getId());
        assertEquals(groupId, application.getGroup().getId());
        assertEquals("jwala 1.0", application.getName());
        assertEquals("the-ws-group-name", application.getGroup().getName());
        assertEquals("the-ws-group-name/jwala-1.0.war", application.getWarPath());

        application = apps.get(1);
        assertEquals(new Identifier<Application>(2L), application.getId());
        assertEquals(groupId2, application.getGroup().getId());
        assertEquals("jwala 1.1", application.getName());
        assertEquals("the-ws-group-name-2", application.getGroup().getName());
        assertEquals("the-ws-group-name-2/jwala-1.1.war", application.getWarPath());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFindByGroupId() {
        when(Config.applicationPersistenceService.findApplicationsBelongingTo(any(Identifier.class))).thenReturn(applications2);
        final List<Application> apps = applicationService.findApplications(groupId);
        assertEquals(applications2.size(), apps.size());

        Application application = apps.get(1);

        assertEquals(new Identifier<Application>(2L), application.getId());
        assertEquals(groupId2, application.getGroup().getId());
        assertEquals("jwala 1.1", application.getName());
        assertEquals("the-ws-group-name-2", application.getGroup().getName());
        assertEquals("the-ws-group-name-2/jwala-1.1.war", application.getWarPath());
    }

    @SuppressWarnings("unchecked")
    @Test(expected = BadRequestException.class)
    public void testCreateBadRequest() {
        when(Config.applicationPersistenceService.createApplication(any(CreateApplicationRequest.class))).thenReturn(Config.mockApplication2);

        CreateApplicationRequest cac = new CreateApplicationRequest(Identifier.id(1L, Group.class), "", "", true, true, false);
        Application created = applicationService.createApplication(cac, new User("user"));

        assertTrue(created == Config.mockApplication2);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreate() {
        when(Config.applicationPersistenceService.createApplication(any(CreateApplicationRequest.class))).thenReturn(Config.mockApplication2);

        CreateApplicationRequest cac = new CreateApplicationRequest(Identifier.id(1L, Group.class), "wan", "/wan", true, true, false);
        Application created = applicationService.createApplication(cac, new User("user"));

        assertTrue(created == Config.mockApplication2);
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testUpdate() throws ApplicationServiceException,IOException {
            when(Config.applicationPersistenceService.updateApplication(any(UpdateApplicationRequest.class)))
                    .thenReturn(Config.mockApplication2);
        Group group = mock(Group.class);
        when(Config.mockApplication2.getName()).thenReturn("test-app-name");
        when(Config.mockApplication2.getWarName()).thenReturn("test-war-name");
        when(Config.mockApplication2.getGroup()).thenReturn(group);
        when(group.getName()).thenReturn("group1");
        long id = 22;
        JpaGroup jpaGroup = mock(JpaGroup.class);
        when(group.getId()).thenReturn(new Identifier<Group>(id));
        List<Long> listOfIds = new ArrayList<>();
        listOfIds.add((long)22);
        List<JpaGroup> jpaGroups = new ArrayList<>();
        jpaGroups.add(jpaGroup);
        when(Config.mockGroupPersistenceService.findGroups(listOfIds)).thenReturn(jpaGroups);
        JpaApplication jpaApplication = mock(JpaApplication.class);
        when(Config.applicationPersistenceService.getJpaApplication(Config.mockApplication2.getName())).thenReturn
                (jpaApplication);
        when(Config.mockGroupPersistenceService.getGroupAppResourceTemplateMetaData("group1", "test-war-name", "test-app-names"))
                .thenReturn
                ("{\"templateName\":\"test-template-name\", \"contentType\":\"application/zip\", \"deployFileName\":\"test-app.war\", \"deployPath\":\"/fake/deploy/path\", \"entity\":{}, \"unpack\":\"true\", \"overwrite\":\"true\"}");

        when(Config.mockResourceService.getMetaData(anyString())).thenReturn(new ResourceTemplateMetaData("test-template-name", MediaType.APPLICATION_ZIP, "deploy-file-name", "deploy-path", null, true, false, null));

        UpdateApplicationRequest cac = new UpdateApplicationRequest(Config.mockApplication2.getId(), Identifier.id(1L, Group.class), "wan", "/wan", true, true, false);
        Application created = applicationService.updateApplication(cac, new User("user"));

        assertTrue(created == Config.mockApplication2);
        verify(Config.mockGroupPersistenceService).updateGroupAppResourceMetaData(anyString(), anyString(), anyString(), anyString());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateAppWithNoWar() throws ApplicationServiceException,IOException {
        when(Config.applicationPersistenceService.updateApplication(any(UpdateApplicationRequest.class)))
                .thenReturn(Config.mockApplication2);
        Group group = mock(Group.class);
        when(Config.mockApplication2.getName()).thenReturn("test-app-name");
        when(Config.mockApplication2.getGroup()).thenReturn(group);
        when(group.getName()).thenReturn("group1");
        long id = 22;
        JpaGroup jpaGroup = mock(JpaGroup.class);
        when(group.getId()).thenReturn(new Identifier<Group>(id));
        List<Long> listOfIds = new ArrayList<>();
        listOfIds.add((long)22);
        List<JpaGroup> jpaGroups = new ArrayList<>();
        jpaGroups.add(jpaGroup);
        when(Config.mockGroupPersistenceService.findGroups(listOfIds)).thenReturn(jpaGroups);
        JpaApplication jpaApplication = mock(JpaApplication.class);
        when(Config.applicationPersistenceService.getJpaApplication(Config.mockApplication2.getName())).thenReturn
                (jpaApplication);
        doThrow(new ResourceTemplateUpdateException("test-app-name", "missing war")).when(Config.mockResourceDao).updateResourceGroup(jpaApplication, jpaGroup);

        UpdateApplicationRequest cac = new UpdateApplicationRequest(Config.mockApplication2.getId(), Identifier.id(1L, Group.class), "wan", "/wan", true, true, false);
        Application created = applicationService.updateApplication(cac, new User("user"));

        assertTrue(created == Config.mockApplication2);
        verify(Config.mockResourceDao, times(0)).updateResourceGroup(jpaApplication, jpaGroup);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRemove() {
        applicationService.removeApplication(Config.mockApplication.getId(), testUser);

        verify(Config.applicationPersistenceService, Mockito.times(1)).removeApplication(Mockito.any(Identifier.class));
    }

    @Test
    public void testGetResourceTemplateNames() {
        final String[] nameArray = {"hct.xml"};
        when(Config.applicationPersistenceService.getResourceTemplateNames(eq("hct"), anyString())).thenReturn(Arrays.asList(nameArray));
        final List names = applicationService.getResourceTemplateNames("hct", "any");
        assertEquals("hct.xml", names.get(0));
    }

    @Test
    public void testUpdateResourceTemplate() {
        applicationService.updateResourceTemplate("hct", "hct.xml", "content", "jvm1", "group1");
        verify(Config.applicationPersistenceService).updateResourceTemplate(eq("hct"), eq("hct.xml"), eq("content"), eq("jvm1"), eq("group1"));
    }

    @Test
    public void testDeployConf() throws CommandFailureException, IOException {
        final Jvm jvm = mock(Jvm.class);
        when(jvm.getHostName()).thenReturn("localhost");
        when(jvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(Config.jvmPersistenceService.findJvmByExactName(eq("jvm-1"))).thenReturn(jvm);
        final CommandOutput execData = mock(CommandOutput.class);
        when(execData.getReturnCode()).thenReturn(new ExecReturnCode(0));
/*
        when(remoteCommandExecutorImpl.executeRemoteCommand(
                anyString(), anyString(), any(ApplicationControlOperation.class), any(WindowsApplicationPlatformCommandProvider.class), anyString(), anyString())).thenReturn(execData);
        when(remoteCommandExecutorImpl.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.CHECK_FILE_EXISTS), any(WindowsApplicationPlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "", ""));
        when(remoteCommandExecutorImpl.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.CREATE_DIRECTORY), any(WindowsApplicationPlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "", ""));
*/
        when(Config.applicationPersistenceService.getResourceTemplate(eq("hct"), eq("hct.xml"), eq("jvm-1"), eq("hct-group"))).thenReturn("Test template");
        when(Config.applicationPersistenceService.findApplication(eq("hct"), eq("hct-group"), eq("jvm-1"))).thenReturn(Config.mockApplication);
        when(Config.applicationPersistenceService.getMetaData(anyString(), anyString(), anyString(), anyString())).thenReturn(META_DATA_TEST_VALUES);

        when(Config.jvmPersistenceService.findJvm(eq("jvm-1"), eq("hct-group"))).thenReturn(jvm);

        ResourceTemplateMetaData mockMetaData = mock(ResourceTemplateMetaData.class);
        when(mockMetaData.getDeployFileName()).thenReturn("hct.xml");
        when(mockMetaData.getDeployPath()).thenReturn("./test/deploy-path/conf/CatalinaSSL/localhost");
        when(mockMetaData.getContentType()).thenReturn(MediaType.APPLICATION_XML);
        when(Config.mockResourceService.getTokenizedMetaData(anyString(), any(Object.class), anyString())).thenReturn(mockMetaData);
        when(Config.mockResourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), any(), any(ResourceGeneratorType.class))).thenReturn("{\"deployPath\":\"./test/deploy-path/conf/CatalinaSSL/localhost\",\"contentType\":\"text/xml\",\"entity\":{\"type\":\"APPLICATION\",\"target\":\"soarcom-hct\",\"group\":\"soarcom-616\",\"parentName\":null,\"deployToJvms\":true},\"templateName\":\"hctXmlTemplate.tpl\",\"deployFileName\":\"hct.xml\"}");
        when(Config.mockResourceService.generateAndDeployFile(any(ResourceIdentifier.class), anyString(), anyString(), anyString())).thenReturn(execData);

        when(mockMetaData.isHotDeploy()).thenReturn(false);
        when(Config.mockResourceService.getResourceContent(any(ResourceIdentifier.class))).thenReturn(new ResourceContent("{\"test\":\"meta data\"}", "test resource content"));
        when(Config.mockResourceService.getTokenizedMetaData(anyString(), anyObject(), anyString())).thenReturn(mockMetaData);

        CommandOutput retExecData = applicationService.deployConf("hct", "hct-group", "jvm-1", "hct.xml", mock(ResourceGroup.class), testUser);
        assertTrue(retExecData.getReturnCode().wasSuccessful());

        when(Config.mockApplication.isSecure()).thenReturn(false);
        retExecData = applicationService.deployConf("hct", "hct-group", "jvm-1", "hct.xml", mock(ResourceGroup.class), testUser);
        assertTrue(retExecData.getReturnCode().wasSuccessful());

        when(Config.mockApplication.isSecure()).thenReturn(true);
        retExecData = applicationService.deployConf("hct", "hct-group", "jvm-1", "hct.xml", mock(ResourceGroup.class), testUser);
        assertTrue(retExecData.getReturnCode().wasSuccessful());

        // test errors
        when(execData.getReturnCode()).thenReturn(new ExecReturnCode(1));
        when(execData.getStandardError()).thenReturn("REMOTE COMMAND FAILURE");
/*        when(remoteCommandExecutorImpl.executeRemoteCommand(
                anyString(), anyString(), eq(ApplicationControlOperation.SCP), any(WebServerCommandFactory.class), anyString(), anyString())).thenReturn(execData);
        when(remoteCommandExecutorImpl.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.CHECK_FILE_EXISTS), any(WindowsApplicationPlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "", ""));
*/
        try {
            applicationService.deployConf("hct", "hct-group", "jvm-1", "hct.xml", mock(ResourceGroup.class), testUser);
        } catch (DeployApplicationConfException ee) {
            assertEquals("REMOTE COMMAND FAILURE", ee.getMessage());
        }
/*
        when(remoteCommandExecutorImpl.executeRemoteCommand(
                anyString(), anyString(), eq(ApplicationControlOperation.SCP), any(WindowsApplicationPlatformCommandProvider.class), anyString(), anyString())).thenThrow(new CommandFailureException(new ExecCommand("fail me"), new Throwable("should fail")));
        when(remoteCommandExecutorImpl.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.CHECK_FILE_EXISTS), any(WindowsApplicationPlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "", ""));
        */
        try {
            applicationService.deployConf("hct", "hct-group", "jvm-1", "hct.xml", mock(ResourceGroup.class), testUser);
        } catch (DeployApplicationConfException ee) {
            assertTrue(ee.getCause() instanceof CommandFailureException);
        }

    }

    @Test(expected = InternalErrorException.class)
    public void testDeployConfJvmNotStopped() throws IOException {
        reset(Config.jvmPersistenceService, Config.applicationPersistenceService, Config.mockResourceService);

        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STARTED);
        when(Config.jvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        when(Config.jvmPersistenceService.findJvm(anyString(), anyString())).thenReturn(mockJvm);
        when(Config.applicationPersistenceService.findApplication(anyString(), anyString(), anyString())).thenReturn(Config.mockApplication);
        when(Config.applicationPersistenceService.getResourceTemplate(anyString(), anyString(), anyString(), anyString())).thenReturn("IGNORED CONTENT");
        ResourceTemplateMetaData mockMetaData = mock(ResourceTemplateMetaData.class);
        when(mockMetaData.isHotDeploy()).thenReturn(false);
        when(Config.mockResourceService.getResourceContent(any(ResourceIdentifier.class))).thenReturn(new ResourceContent("{\"test\":\"meta data\"}", "test resource content"));
        when(Config.mockResourceService.getTokenizedMetaData(anyString(), anyObject(), anyString())).thenReturn(mockMetaData);
        applicationService.deployConf("testApp", "testGroup", "testJvm", "HttpSslConfTemplate.tpl", mock(ResourceGroup.class), testUser);

        verify(Config.mockResourceService, never()).generateAndDeployFile(any(ResourceIdentifier.class), anyString(), anyString(), anyString());
    }

    @Test
    public void testDeployConfJvmNotStoppedAndHotDeploy() throws IOException {
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STARTED);
        when(mockJvm.getJvmName()).thenReturn("jvm-name");
        when(Config.jvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        when(Config.jvmPersistenceService.findJvm(anyString(), anyString())).thenReturn(mockJvm);
        when(Config.applicationPersistenceService.findApplication(anyString(), anyString(), anyString())).thenReturn(Config.mockApplication);
        when(Config.applicationPersistenceService.getResourceTemplate(anyString(), anyString(), anyString(), anyString())).thenReturn("IGNORED CONTENT");
        ResourceTemplateMetaData mockMetaData = mock(ResourceTemplateMetaData.class);
        when(mockMetaData.isHotDeploy()).thenReturn(true);
        when(Config.mockResourceService.getResourceContent(any(ResourceIdentifier.class))).thenReturn(new ResourceContent("{\"test\":\"meta data\"}", "test resource content"));
        when(Config.mockResourceService.getTokenizedMetaData(anyString(), anyObject(), anyString())).thenReturn(mockMetaData);

        applicationService.deployConf("testApp", "testGroup", "testJvm", "HttpSslConfTemplate.tpl", mock(ResourceGroup.class), testUser);
        verify(Config.mockResourceService).generateAndDeployFile(any(ResourceIdentifier.class), anyString(), anyString(), anyString());
    }

    @Test (expected = ApplicationServiceException.class)
    public void testDeployConfJvmNotStoppedAndHotDeployThrowsException() throws IOException {
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STARTED);
        when(mockJvm.getJvmName()).thenReturn("jvm-name");
        when(Config.jvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        when(Config.jvmPersistenceService.findJvm(anyString(), anyString())).thenReturn(mockJvm);
        when(Config.applicationPersistenceService.findApplication(anyString(), anyString(), anyString())).thenReturn(Config.mockApplication);
        when(Config.applicationPersistenceService.getResourceTemplate(anyString(), anyString(), anyString(), anyString())).thenReturn("IGNORED CONTENT");
        ResourceTemplateMetaData mockMetaData = mock(ResourceTemplateMetaData.class);
        when(mockMetaData.isHotDeploy()).thenReturn(true);
        when(Config.mockResourceService.getResourceContent(any(ResourceIdentifier.class))).thenReturn(new ResourceContent("{\"test\":\"meta data\"}", "test resource content"));
        when(Config.mockResourceService.getTokenizedMetaData(anyString(), anyObject(), anyString())).thenThrow(new IOException("FAIL THIS TEST"));

        applicationService.deployConf("testApp", "testGroup", "testJvm", "HttpSslConfTemplate.tpl", mock(ResourceGroup.class), testUser);
        verify(Config.mockResourceService, never()).generateAndDeployFile(any(ResourceIdentifier.class), anyString(), anyString(), anyString());
    }

    @Test
    public void testPreviewResourceTemplate() {
        final Jvm jvm = mock(Jvm.class);
        when(Config.applicationPersistenceService.findApplication(eq("hct"), eq("hct-group"), eq("jvm-1"))).thenReturn(Config.mockApplication);
        when(Config.jvmPersistenceService.findJvm(eq("jvm-1"), eq("hct-group"))).thenReturn(jvm);
        final String preview = applicationService.previewResourceTemplate("myFile", "hct", "hct-group", "jvm-1", "Template contents", new ResourceGroup());
        verify(Config.mockResourceService).generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), any(Application.class), any(ResourceGeneratorType.class));
    }

    @Test
    public void testUploadTemplate() {
        final UploadAppTemplateRequest cmd = mock(UploadAppTemplateRequest.class);
        when(cmd.getConfFileName()).thenReturn("roleMapping.properties");
        when(cmd.getJvmName()).thenReturn("testJvmName");
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(111L));
        when(Config.jvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        JpaJvm mockJpaJvm = mock(JpaJvm.class);
        when(Config.jvmPersistenceService.getJpaJvm(any(Identifier.class), anyBoolean())).thenReturn(mockJpaJvm);
        applicationService.uploadAppTemplate(cmd);
        verify(cmd).validate();
        verify(Config.applicationPersistenceService).uploadAppTemplate(any(UploadAppTemplateRequest.class), any(JpaJvm.class));

        List<Jvm> jvmList = new ArrayList<>();
        jvmList.add(mockJvm);
        when(mockJvm.getJvmName()).thenReturn("testJvmName");
        when(cmd.getConfFileName()).thenReturn("hct.xml");
        applicationService.uploadAppTemplate(cmd);
        verify(cmd, times(2)).validate();
        verify(Config.applicationPersistenceService, times(2)).uploadAppTemplate(any(UploadAppTemplateRequest.class), any(JpaJvm.class));

        when(mockJvm.getJvmName()).thenReturn("notTestJvmName");
        applicationService.uploadAppTemplate(cmd);
        verify(cmd, times(3)).validate();
        verify(Config.applicationPersistenceService, times(3)).uploadAppTemplate(any(UploadAppTemplateRequest.class), any(JpaJvm.class));

    }

    @Test
    public void testFindApplicationsByJvmId() {
        final Identifier<Jvm> id = new Identifier<Jvm>(1l);
        applicationService.findApplicationsByJvmId(id);
        verify(Config.applicationPersistenceService).findApplicationsBelongingToJvm(eq(id));
    }


    @Test
    public void testAppDeployConf() throws IOException {
        reset(Config.mockResourceService);

        final String appName = "test-app";
        List<String> hosts = new ArrayList<>();
        hosts.add("testServer");
        hosts.add("testServer2");
        List<String> templateNames = new ArrayList<>();
        templateNames.add("");
        List<Jvm> jvms = new ArrayList<>();
        ResourceTemplateMetaData mockMetaData = mock(ResourceTemplateMetaData.class);
        Entity mockEntity = mock(Entity.class);
        Jvm mockJvm = mock(Jvm.class);
        jvms.add(mockJvm);
        Group mockGroup = mock(Group.class);
        CommandOutput mockCommandOutput = mock(CommandOutput.class);
        when(Config.applicationPersistenceService.getApplication(eq(appName))).thenReturn(Config.mockApplication);
        when(Config.mockApplication.getName()).thenReturn(appName);
        when(Config.mockGroupPersistenceService.getGroup(any(Identifier.class))).thenReturn(mockGroup);
        when(Config.mockApplication.getGroup()).thenReturn(mockGroup);
        when(mockGroup.getName()).thenReturn("test-group");
        when(mockGroup.getId()).thenReturn(new Identifier<Group>(1L));
       when(mockJvm.getHostName()).thenReturn("testserver");
        when(mockJvm.getState()).thenReturn(JvmState.JVM_NEW);
        when(Config.mockGroupPersistenceService.getHosts(anyString())).thenReturn(hosts);
        when(Config.mockGroupPersistenceService.getGroupAppResourceTemplateMetaData(anyString(), anyString(), anyString())).thenReturn("");
        when(Config.mockResourceService.getMetaData(anyString())).thenReturn(mockMetaData);
        when(mockMetaData.getEntity()).thenReturn(mockEntity);
        when(mockEntity.getDeployToJvms()).thenReturn(false);
        when(Config.mockGroupPersistenceService.getGroupAppsResourceTemplateNames(anyString(), anyString())).thenReturn(templateNames);
        when(Config.mockResourceService.generateAndDeployFile(any(ResourceIdentifier.class), anyString(), anyString(),
                anyString())).thenReturn(mockCommandOutput);
        when(mockCommandOutput.getReturnCode()).thenReturn(new ExecReturnCode(0));
        when(Config.jvmPersistenceService.getJvmsByGroupName(anyString())).thenReturn(jvms);
        applicationService.deployConf(appName, null, testUser);
        verify(Config.mockResourceService, times(2)).generateAndDeployFile(any(ResourceIdentifier.class), anyString(),
                anyString(), anyString());
    }

    @Test(expected = InternalErrorException.class)
    public void testAppDeployConfCommandFailure() throws IOException {
        reset(Config.mockResourceService);

        final String appName = "test-app";
        List<String> hosts = new ArrayList<>();
        hosts.add("testServer");
        hosts.add("testServer2");
        List<String> templateNames = new ArrayList<>();
        templateNames.add("");
        Set<Jvm> jvms = new HashSet<>();
        ResourceTemplateMetaData mockMetaData = mock(ResourceTemplateMetaData.class);
        Entity mockEntity = mock(Entity.class);
        Jvm mockJvm = mock(Jvm.class);
        jvms.add(mockJvm);
        Group mockGroup = mock(Group.class);
        CommandOutput mockCommandOutput = mock(CommandOutput.class);
        when(Config.applicationPersistenceService.getApplication(eq(appName))).thenReturn(Config.mockApplication);
        when(Config.mockApplication.getName()).thenReturn(appName);
        when(Config.mockGroupPersistenceService.getGroup(any(Identifier.class))).thenReturn(mockGroup);
        when(Config.mockApplication.getGroup()).thenReturn(mockGroup);
        when(mockGroup.getId()).thenReturn(new Identifier<Group>(1L));
        when(mockGroup.getJvms()).thenReturn(jvms);
        when(mockJvm.getHostName()).thenReturn("testserver");
        when(mockJvm.getState()).thenReturn(JvmState.JVM_NEW);
        when(Config.mockGroupPersistenceService.getHosts(anyString())).thenReturn(hosts);
        when(Config.mockGroupPersistenceService.getGroupAppResourceTemplateMetaData(anyString(), anyString(), anyString())).thenReturn("");
        when(Config.mockResourceService.getMetaData(anyString())).thenReturn(mockMetaData);
        when(mockMetaData.getEntity()).thenReturn(mockEntity);
        when(mockEntity.getDeployToJvms()).thenReturn(false);
        when(Config.mockGroupPersistenceService.getGroupAppsResourceTemplateNames(anyString(), anyString())).thenReturn(templateNames);
        when(Config.mockResourceService.generateAndDeployFile(any(ResourceIdentifier.class), anyString(), anyString(),
                anyString())).thenReturn(mockCommandOutput);
        when(mockCommandOutput.getReturnCode()).thenReturn(new ExecReturnCode(1));
        applicationService.deployConf(appName, null, testUser);
    }

    @Test(expected = InternalErrorException.class)
    public void testAppDeployConfNoHostFailure() {
        final String appName = "test-app";
        when(Config.applicationPersistenceService.getApplication(eq(appName))).thenReturn(Config.mockApplication);
        when(Config.mockGroupPersistenceService.getGroup(any(Identifier.class))).thenReturn(group);
        when(Config.mockApplication.getGroup()).thenReturn(group);
        when(Config.mockGroupPersistenceService.getHosts(anyString())).thenReturn(null);
        applicationService.deployConf(appName, null, testUser);
    }

    @Test(expected = ApplicationServiceException.class)
    public void testAppDeployConfJvmStatedFailure() {
        final String appName = "test-app";
        List<String> hosts = new ArrayList<>();
        hosts.add("testServer");
        hosts.add("testServer2");
        List<Jvm> jvms = new ArrayList<>();
        Jvm mockJvm = mock(Jvm.class);
        jvms.add(mockJvm);
        Group mockGroup = mock(Group.class);
        when(Config.applicationPersistenceService.getApplication(eq(appName))).thenReturn(Config.mockApplication);
        when(Config.mockGroupPersistenceService.getGroup(any(Identifier.class))).thenReturn(mockGroup);
        when(Config.mockApplication.getGroup()).thenReturn(mockGroup);

        when(mockGroup.getId()).thenReturn(new Identifier<Group>(1L));
        when(mockGroup.getName()).thenReturn("mockGroupName");
        when(mockJvm.getHostName()).thenReturn("testserver");
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STARTED);

        when(Config.mockGroupPersistenceService.getHosts(anyString())).thenReturn(hosts);

        when(Config.jvmPersistenceService.getJvmsByGroupName(anyString())).thenReturn(jvms);
        applicationService.deployConf(appName, null, testUser);
    }

    @Test(expected = InternalErrorException.class)
    public void testAppDeployConfIncorrectHostFailure() {
        final String appName = "test-app";
        List<String> hosts = new ArrayList<>();
        hosts.add("testServer");
        hosts.add("testServer2");
        when(Config.applicationPersistenceService.getApplication(eq(appName))).thenReturn(Config.mockApplication);
        when(Config.mockGroupPersistenceService.getGroup(any(Identifier.class))).thenReturn(group);
        when(Config.mockApplication.getGroup()).thenReturn(group);
        when(Config.mockGroupPersistenceService.getHosts(anyString())).thenReturn(hosts);
        applicationService.deployConf(appName, "test", testUser);
    }

    @Test(expected = InternalErrorException.class)
    public void testAppDeployConfResourceTemplateFailure() throws IOException {
        final String appName = "test-app";
        List<String> hosts = new ArrayList<>();
        hosts.add("testServer");
        hosts.add("testServer2");
        List<String> templateNames = new ArrayList<>();
        templateNames.add("");
        Set<Jvm> jvms = new HashSet<>();
        Jvm mockJvm = mock(Jvm.class);
        jvms.add(mockJvm);
        Group mockGroup = mock(Group.class);
        when(Config.applicationPersistenceService.getApplication(eq(appName))).thenReturn(Config.mockApplication);
        when(Config.mockGroupPersistenceService.getGroup(any(Identifier.class))).thenReturn(mockGroup);
        when(Config.mockApplication.getGroup()).thenReturn(mockGroup);
        when(mockGroup.getId()).thenReturn(new Identifier<Group>(1L));
        when(mockGroup.getJvms()).thenReturn(jvms);
        when(mockJvm.getHostName()).thenReturn("testserver");
        when(mockJvm.getState()).thenReturn(JvmState.JVM_NEW);
        when(Config.mockGroupPersistenceService.getHosts(anyString())).thenReturn(hosts);
        when(Config.mockGroupPersistenceService.getGroupAppResourceTemplateMetaData(anyString(), anyString(), anyString())).thenReturn("");
        when(Config.mockResourceService.getMetaData(anyString())).thenThrow(IOException.class);
        when(Config.mockGroupPersistenceService.getGroupAppsResourceTemplateNames(anyString(), anyString())).thenReturn(templateNames);
        applicationService.deployConf(appName, "testserver", testUser);
    }

    @Test
    public void testCopyApplicationWarToGroupHosts() throws IOException {
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getHostName()).thenReturn("mock-hostname");

        Group mockGroup = mock(Group.class);
        when(mockGroup.getId()).thenReturn(new Identifier<Group>(999L));
        when(mockGroup.getJvms()).thenReturn(Collections.singleton(mockJvm));

        Application mockApplicationForCopy = mock(Application.class);
        when(mockApplicationForCopy.getGroup()).thenReturn(mockGroup);
        when(mockApplicationForCopy.getWarPath()).thenReturn("./src/test/resources/archive/test_archive.war");
        when(mockApplicationForCopy.getWarName()).thenReturn("mock-application-war-name");
        when(mockApplicationForCopy.getName()).thenReturn("mock-application-name");
        when(mockApplicationForCopy.isUnpackWar()).thenReturn(false);

        when(Config.mockGroupPersistenceService.getGroup(any(Identifier.class))).thenReturn(mockGroup);
        when(Config.binaryDistributionControlService.createDirectory(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Create directory succeeded", ""));
        when(Config.binaryDistributionControlService.secureCopyFile(anyString(), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Secure copy succeeded", ""));

        ResourceTemplateMetaData mockResourceTemplateMetaData = mock(ResourceTemplateMetaData.class);
        when(mockResourceTemplateMetaData.getDeployPath()).thenReturn("C:/deploy/path");
        when(Config.mockResourceService.getResourceContent(any(ResourceIdentifier.class))).thenReturn(new ResourceContent("C:/deploy/path", "content"));
        when(Config.mockResourceService.getTokenizedMetaData(anyString(), any(Application.class), anyString())).thenReturn(mockResourceTemplateMetaData);

        applicationService.copyApplicationWarToGroupHosts(mockApplicationForCopy);

        verify(Config.binaryDistributionService, never()).distributeUnzip(anyString());
    }

    @Test
    public void testCopyApplicationWarToGroupHostsAndUnpack() {
        reset(Config.binaryDistributionControlService);

        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getHostName()).thenReturn("mock-hostname");

        Group mockGroup = mock(Group.class);
        when(mockGroup.getId()).thenReturn(new Identifier<Group>(999L));
        when(mockGroup.getJvms()).thenReturn(Collections.singleton(mockJvm));

        Application mockApplicationForCopy = mock(Application.class);
        when(mockApplicationForCopy.getGroup()).thenReturn(mockGroup);
        when(mockApplicationForCopy.getWarPath()).thenReturn("./src/test/resources/archive/test_archive.war");
        when(mockApplicationForCopy.getWarName()).thenReturn("mock-application-war-name");
        when(mockApplicationForCopy.getName()).thenReturn("mock-application-name");
        when(mockApplicationForCopy.isUnpackWar()).thenReturn(true);

        when(Config.mockGroupPersistenceService.getGroup(any(Identifier.class))).thenReturn(mockGroup);
        when(Config.binaryDistributionControlService.createDirectory(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Create directory succeeded", ""));
        when(Config.binaryDistributionControlService.secureCopyFile(anyString(), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Secure copy succeeded", ""));
        when(Config.binaryDistributionControlService.changeFileMode(anyString(), anyString(), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Change file mode succeeded", ""));
        when(Config.binaryDistributionControlService.checkFileExists(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "File exists succeeded", ""));
        when(Config.binaryDistributionControlService.backupFileWithMove(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Backup succeeded", ""));
        when(Config.binaryDistributionControlService.unzipBinary(anyString(), anyString(), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Unzip succeeded", ""));

        applicationService.copyApplicationWarToGroupHosts(mockApplicationForCopy);

        verify(Config.binaryDistributionControlService, times(1)).unzipBinary(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testCopyApplicationWarToHost() {
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getHostName()).thenReturn("mock-hostname");

        Group mockGroup = mock(Group.class);
        when(mockGroup.getId()).thenReturn(new Identifier<Group>(999L));
        when(mockGroup.getJvms()).thenReturn(Collections.singleton(mockJvm));

        Application mockApplicationForCopy = mock(Application.class);
        when(mockApplicationForCopy.getGroup()).thenReturn(mockGroup);
        when(mockApplicationForCopy.getWarPath()).thenReturn("./src/test/resources/archive/test_archive.war");
        when(mockApplicationForCopy.getWarName()).thenReturn("mock-application-war-name");
        when(mockApplicationForCopy.getName()).thenReturn("mock-application-name");
        when(mockApplicationForCopy.isUnpackWar()).thenReturn(false);

        when(Config.mockGroupPersistenceService.getGroup(any(Identifier.class))).thenReturn(mockGroup);
        when(Config.binaryDistributionControlService.createDirectory(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Create directory succeeded", ""));
        when(Config.binaryDistributionControlService.secureCopyFile(anyString(), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Secure copy succeeded", ""));

        applicationService.copyApplicationWarToHost(mockApplicationForCopy, "mock-hostname");

        verify(Config.binaryDistributionService, never()).distributeUnzip(anyString());
    }

    @Test
    public void testDeployApplicationResourcesToGroupHosts() throws IOException {
        reset(Config.mockResourceService);

        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getHostName()).thenReturn("mock-hostname");

        Group mockGroup = mock(Group.class);
        when(mockGroup.getName()).thenReturn("mock-group-name");
        when(mockGroup.getId()).thenReturn(new Identifier<Group>(999L));

        Application mockApplicationForDeploy = mock(Application.class);
        when(mockApplicationForDeploy.getGroup()).thenReturn(mockGroup);
        when(mockApplicationForDeploy.getWarPath()).thenReturn("./src/test/resources/archive/test_archive.war");
        when(mockApplicationForDeploy.getWarName()).thenReturn("mock-application-war-name");
        when(mockApplicationForDeploy.getName()).thenReturn("mock-application-name");
        when(mockApplicationForDeploy.isUnpackWar()).thenReturn(false);

        ResourceGroup mockResourceGroup = mock(ResourceGroup.class);

        Entity mockEntity = mock(Entity.class);
        when(mockEntity.getDeployToJvms()).thenReturn(false);

        ResourceTemplateMetaData mockResourceTemplateMetaData = mock(ResourceTemplateMetaData.class);
        when(mockResourceTemplateMetaData.getEntity()).thenReturn(mockEntity);

        when(Config.mockGroupPersistenceService.getGroupAppsResourceTemplateNames(anyString())).thenReturn(Collections.singletonList("mock-application-resource"));
        when(Config.mockGroupPersistenceService.getGroupAppResourceTemplateMetaData(anyString(), anyString(), anyString())).thenReturn("{\"fake\":\"meta-data\"}");
        when(Config.mockResourceService.getTokenizedMetaData(anyString(), anyObject(), anyString())).thenReturn(mockResourceTemplateMetaData);
        when(Config.mockResourceService.generateAndDeployFile(any(ResourceIdentifier.class), anyString(), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Generate and deploy succeeded", ""));
        when(Config.jvmPersistenceService.getJvmsByGroupName(anyString())).thenReturn(Collections.singletonList(mockJvm));

        applicationService.deployApplicationResourcesToGroupHosts("mock-group-name", mockApplicationForDeploy, mockResourceGroup);

        verify(Config.mockResourceService, times(1)).generateAndDeployFile(any(ResourceIdentifier.class), anyString(), anyString(), anyString());
    }

    static class Config {

        private static ApplicationPersistenceService applicationPersistenceService = mock(ApplicationPersistenceService.class);
        private static JvmPersistenceService jvmPersistenceService = mock(JvmPersistenceService.class);
        private static GroupPersistenceService mockGroupPersistenceService = mock(GroupPersistenceService.class);
        private static ResourceService mockResourceService = mock(ResourceService.class);
        private static BinaryDistributionService binaryDistributionService = mock(BinaryDistributionService.class);
        private static BinaryDistributionControlService binaryDistributionControlService = mock(BinaryDistributionControlService.class);
        private static HistoryFacadeService mockHistoryFacadeService = mock(HistoryFacadeService.class);
        private static BinaryDistributionLockManager lockManager = mock(BinaryDistributionLockManager.class);
        private static Application mockApplication = mock(Application.class);
        private static Application mockApplication2 = mock(Application.class);
        private static ResourceDao mockResourceDao = mock(ResourceDao.class);

        @Bean
        public ApplicationPersistenceService getApplicationPersistenceService() {
            return applicationPersistenceService;
        }

        @Bean
        public JvmPersistenceService getJvmPersistenceService() {
            return jvmPersistenceService;
        }

        @Bean
        public GroupPersistenceService getMockGroupPersistenceService() {
            return mockGroupPersistenceService;
        }

        @Bean
        public ResourceService getMockResourceService() {
            return mockResourceService;
        }

        @Bean
        public BinaryDistributionService getBinaryDistributionService() {
            return binaryDistributionService;
        }

        @Bean
        public HistoryFacadeService getMockHistoryFacadeService() {
            return mockHistoryFacadeService;
        }

        @Bean
        public BinaryDistributionControlService getBinaryDistributionControlService() {
            return binaryDistributionControlService;
        }

        @Bean
        public BinaryDistributionLockManager getLockManager() {
            return lockManager;
        }

        @Bean
        public ResourceDao getMockResourceDao() {
            return mockResourceDao;
        }

        @Bean
        public ApplicationService getApplicationService() {
            return new ApplicationServiceImpl(
                    applicationPersistenceService,
                    jvmPersistenceService,
                    mockGroupPersistenceService,
                    mockResourceService,
                    binaryDistributionService,
                    mockHistoryFacadeService,
                    new BinaryDistributionLockManagerImpl());
        }
    }
}
