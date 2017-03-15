package com.cerner.jwala.service.jvm.impl;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.exception.ExternalSystemErrorException;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.exec.RemoteExecCommand;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.request.jvm.ControlJvmRequest;
import com.cerner.jwala.control.command.common.Command;
import com.cerner.jwala.control.command.common.ShellCommandFactory;
import com.cerner.jwala.control.configuration.AemSshConfig;
import com.cerner.jwala.control.jvm.command.JvmCommandFactory;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.persistence.jpa.domain.JpaGroup;
import com.cerner.jwala.persistence.jpa.type.EventType;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.service.HistoryFacadeService;
import com.cerner.jwala.service.RemoteCommandExecutorService;
import com.cerner.jwala.service.VerificationBehaviorSupport;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionControlService;
import com.cerner.jwala.service.jvm.JvmControlService;
import com.cerner.jwala.service.jvm.JvmStateService;
import com.cerner.jwala.service.jvm.exception.JvmControlServiceException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {JvmControlServiceImplTest.Config.class})
public class JvmControlServiceImplTest extends VerificationBehaviorSupport {

    @Autowired
    private JvmControlService jvmControlService;

    private User user;

    private List<JpaGroup> groups = new ArrayList<>();

    public JvmControlServiceImplTest() {
        initMocks(this);
        this.groups.add(new JpaGroup());
    }

    @Before
    public void setup() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources");
        user = new User("unused");
    }

    @Test
    public void testVerificationOfBehaviorForSuccess() throws Exception {
        final ControlJvmRequest controlCommand = mock(ControlJvmRequest.class);
        final Jvm jvm = mock(Jvm.class);
        final String jvmName = "mockJvmName";
        String jvmHost = "mockJvmHost";
        when(jvm.getJvmName()).thenReturn(jvmName);
        when(jvm.getHostName()).thenReturn(jvmHost);
        final Identifier<Jvm> jvmId = mock(Identifier.class);
        final JvmControlOperation controlOperation = JvmControlOperation.STOP;
        final CommandOutput mockExecData = mock(CommandOutput.class);

        when(jvm.getId()).thenReturn(Identifier.<Jvm>id(1L));
        when(controlCommand.getJvmId()).thenReturn(jvmId);
        when(controlCommand.getControlOperation()).thenReturn(controlOperation);
        when(Config.mockJvmPersistenceService.getJvm(jvmId)).thenReturn(jvm);
        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(0));
        when(Config.mockJvmCommandFactory.executeCommand(any(Jvm.class), any(JvmControlOperation.class))).thenReturn(new RemoteCommandReturnInfo(0, "", ""));

        jvmControlService.controlJvm(controlCommand, user);

        verify(Config.mockJvmPersistenceService, times(1)).getJvm(eq(jvmId));
        verify(Config.mockJvmCommandFactory).executeCommand(any(Jvm.class), any(JvmControlOperation.class));
        verify(Config.mockJvmStateService, times(1)).updateState(any(Jvm.class), any(JvmState.class));
        verify(Config.mockHistoryFacadeService).write(anyString(), anyList(), anyString(), any(EventType.class), anyString());

        // test other command codes
        when(Config.mockJvmCommandFactory.executeCommand(any(Jvm.class), any(JvmControlOperation.class))).thenReturn(new RemoteCommandReturnInfo(ExecReturnCode.JWALA_EXIT_CODE_ABNORMAL_SUCCESS, "Abnormal success", ""));
        CommandOutput returnOutput = jvmControlService.controlJvm(controlCommand, user);
        // abnormal success is not a successful return code
        assertTrue(returnOutput.getReturnCode().getWasSuccessful());

        when(jvm.getState()).thenReturn(JvmState.JVM_STARTED);
        when(Config.mockJvmCommandFactory.executeCommand(any(Jvm.class), any(JvmControlOperation.class))).thenReturn(new RemoteCommandReturnInfo(ExecReturnCode.JWALA_EXIT_CODE_NO_OP, "No op", ""));
        returnOutput = jvmControlService.controlJvm(controlCommand, user);
        assertFalse(returnOutput.getReturnCode().getWasSuccessful());

        when(Config.mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(ExecReturnCode.JWALA_EXIT_CODE_FAST_FAIL, "", "Fast Fail"));
        try {
            jvmControlService.controlJvm(controlCommand, user);
        } catch (ExternalSystemErrorException ee) {
            assertEquals(ee.getMessageResponseStatus(), FaultType.FAST_FAIL);
        }

        when(Config.mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(ExecReturnCode.JWALA_EXIT_NO_SUCH_SERVICE, "", "No such service"));
        try {
            jvmControlService.controlJvm(controlCommand, user);
        } catch (ExternalSystemErrorException ee) {
            assertEquals(ee.getMessageResponseStatus(), FaultType.REMOTE_COMMAND_FAILURE);
        }

        when(Config.mockJvmCommandFactory.executeCommand(any(Jvm.class), any(JvmControlOperation.class))).thenReturn(new RemoteCommandReturnInfo(ExecReturnCode.JWALA_EXIT_PROCESS_KILLED, "", "Process killed"));
        returnOutput = jvmControlService.controlJvm(controlCommand, user);
        assertEquals("FORCED STOPPED", returnOutput.getStandardOutput());

        when(Config.mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(88, "", "Process default error"));
        try {
            jvmControlService.controlJvm(controlCommand, user);
        } catch (ExternalSystemErrorException ee) {
            assertEquals(ee.getMessageResponseStatus(), FaultType.REMOTE_COMMAND_FAILURE);
        }

        when(controlCommand.getControlOperation()).thenReturn(JvmControlOperation.HEAP_DUMP);

        when(Config.mockJvmCommandFactory.executeCommand(any(Jvm.class), any(JvmControlOperation.class))).thenReturn(new RemoteCommandReturnInfo(ExecReturnCode.JWALA_EXIT_CODE_ABNORMAL_SUCCESS, "Abnormal success", ""));
        returnOutput = jvmControlService.controlJvm(controlCommand, user);
        assertFalse(returnOutput.getReturnCode().getWasSuccessful());

        when(controlCommand.getControlOperation()).thenReturn(JvmControlOperation.START);
        when(Config.mockJvmCommandFactory.executeCommand(any(Jvm.class), any(JvmControlOperation.class))).thenReturn(new RemoteCommandReturnInfo(88, "The requested service has already been started", ""));
        returnOutput = jvmControlService.controlJvm(controlCommand, user);
        assertFalse(returnOutput.getReturnCode().getWasSuccessful());
    }

    @Test
    public void testVerificationOfBehaviorForOtherReturnCodes() throws CommandFailureException {
        final ControlJvmRequest controlCommand = mock(ControlJvmRequest.class);
        final Identifier<Jvm> jvmId = new Identifier<>(1L);
        final Jvm mockJvm = mock(Jvm.class);

        when(mockJvm.getId()).thenReturn(Identifier.<Jvm>id(jvmId.getId()));
        when(mockJvm.getJvmName()).thenReturn("testJvmName");
        when(mockJvm.getHostName()).thenReturn("testJvmHost");
        when(Config.mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(mockJvm);

        when(controlCommand.getControlOperation()).thenReturn(JvmControlOperation.START);
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STARTED);
        when(controlCommand.getJvmId()).thenReturn(jvmId);
        when(Config.mockJvmCommandFactory.executeCommand(any(Jvm.class), any(JvmControlOperation.class))).thenReturn(new RemoteCommandReturnInfo(ExecReturnCode.JWALA_EXIT_CODE_FAST_FAIL, "Test standard out when START or STOP", "Test standard error"));

        jvmControlService.controlJvm(controlCommand, user);
        verify(Config.mockHistoryFacadeService, times(2)).write(anyString(), anyList(), anyString(), any(EventType.class), anyString());

        when(Config.mockJvmCommandFactory.executeCommand(any(Jvm.class), any(JvmControlOperation.class))).thenReturn(new RemoteCommandReturnInfo(ExecReturnCode.JWALA_EXIT_NO_SUCH_SERVICE, "Test standard out when START or STOP", "Test standard error"));
        jvmControlService.controlJvm(controlCommand, user);

        when(Config.mockJvmCommandFactory.executeCommand(any(Jvm.class), any(JvmControlOperation.class))).thenReturn(new RemoteCommandReturnInfo(ExecReturnCode.JWALA_EXIT_PROCESS_KILLED, "Test standard out when START or STOP", "Test standard error"));
        final CommandOutput commandOutput = jvmControlService.controlJvm(controlCommand, user);
        assertTrue(commandOutput.getReturnCode().wasSuccessful());

        when(Config.mockJvmCommandFactory.executeCommand(any(Jvm.class), any(JvmControlOperation.class))).thenReturn(new RemoteCommandReturnInfo(88, "", "Test standard error or out"));
        when(controlCommand.getControlOperation()).thenReturn(JvmControlOperation.HEAP_DUMP);

        jvmControlService.controlJvm(controlCommand, user);
    }

    @Test
    public void testSecureCopyConfFile() throws CommandFailureException {
        when(Config.mockShellCommandFactory.executeRemoteCommand(anyString(), eq(Command.CREATE_DIR), anyString())).thenReturn(mock(RemoteCommandReturnInfo.class));
        when(Config.mockShellCommandFactory.executeRemoteCommand(anyString(), eq(Command.MOVE), anyString(), anyString())).thenReturn(mock(RemoteCommandReturnInfo.class));
        when(Config.mockShellCommandFactory.executeRemoteCommand(anyString(), eq(Command.CHECK_FILE_EXISTS), anyString())).thenReturn(mock(RemoteCommandReturnInfo.class));
        when(Config.mockShellCommandFactory.executeRemoteCommand(anyString(), eq(Command.CHANGE_FILE_MODE), any(String[].class))).thenReturn(mock(RemoteCommandReturnInfo.class));

        final Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getHostName()).thenReturn("host");
        when(Config.mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(mockJvm);
        when(Config.mockShellCommandFactory.executeRemoteCommand(eq("host"), eq(Command.SCP), anyString(), anyString())).thenReturn(mock(RemoteCommandReturnInfo.class));

        final ControlJvmRequest mockControlJvmRequest = mock(ControlJvmRequest.class);
        when(mockControlJvmRequest.getControlOperation()).thenReturn(JvmControlOperation.SCP);
        jvmControlService.secureCopyFile(mockControlJvmRequest, "./source/filename", "./dest/filename", "user-id", true);
        verify(Config.mockShellCommandFactory).executeRemoteCommand("host", Command.CREATE_DIR, "./dest");
        verify(Config.mockShellCommandFactory).executeRemoteCommand("host", Command.SCP, "./source/filename", "./dest/filename");
    }

    @Test
    public void testSecureCopyConfFileOverwriteFalseAndFileExists() throws CommandFailureException {
        when(Config.mockShellCommandFactory.executeRemoteCommand(anyString(), eq(Command.CREATE_DIR), anyString())).thenReturn(mock(RemoteCommandReturnInfo.class));
        when(Config.mockShellCommandFactory.executeRemoteCommand(anyString(), eq(Command.MOVE), anyString(), anyString())).thenReturn(mock(RemoteCommandReturnInfo.class));
        when(Config.mockShellCommandFactory.executeRemoteCommand(anyString(), eq(Command.CHECK_FILE_EXISTS), anyString())).thenReturn(mock(RemoteCommandReturnInfo.class));
        when(Config.mockShellCommandFactory.executeRemoteCommand(anyString(), eq(Command.CHANGE_FILE_MODE), any(String[].class))).thenReturn(mock(RemoteCommandReturnInfo.class));

        final Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getHostName()).thenReturn("host");
        when(Config.mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(mockJvm);
        when(Config.mockShellCommandFactory.executeRemoteCommand(eq("host"), eq(Command.SCP), anyString(), anyString())).thenReturn(mock(RemoteCommandReturnInfo.class));

        final ControlJvmRequest mockControlJvmRequest = mock(ControlJvmRequest.class);
        when(mockControlJvmRequest.getControlOperation()).thenReturn(JvmControlOperation.SCP);
        CommandOutput result = jvmControlService.secureCopyFile(mockControlJvmRequest, "./source/filename", "./dest/filename", "user-id", false);
        verify(Config.mockShellCommandFactory).executeRemoteCommand("host", Command.CREATE_DIR, "./dest");
        verify(Config.mockShellCommandFactory, never()).executeRemoteCommand("host", Command.SCP, "./source/filename", "./dest/filename");
        assertEquals("Skipping scp of file: ./dest/filename already exists and overwrite is set to false.", result.getStandardOutput());
    }

    @Test(expected = InternalErrorException.class)
    public void testSecureCopyConfFileFailsBackup() throws CommandFailureException {
        when(Config.mockShellCommandFactory.executeRemoteCommand(anyString(), eq(Command.CREATE_DIR), anyString())).thenReturn(mock(RemoteCommandReturnInfo.class));
        when(Config.mockShellCommandFactory.executeRemoteCommand(anyString(), eq(Command.CHECK_FILE_EXISTS), anyString())).thenReturn(mock(RemoteCommandReturnInfo.class));
        when(Config.mockShellCommandFactory.executeRemoteCommand(anyString(), eq(Command.MOVE), anyString(), anyString())).thenReturn(new RemoteCommandReturnInfo(1, "", "FAILED BACK UP"));

        final Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getHostName()).thenReturn("host");
        when(Config.mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(mockJvm);
        when(Config.mockShellCommandFactory.executeRemoteCommand(eq("host"), eq(Command.SCP), anyString(), anyString())).thenReturn(mock(RemoteCommandReturnInfo.class));

        final ControlJvmRequest mockControlJvmRequest = mock(ControlJvmRequest.class);
        when(mockControlJvmRequest.getControlOperation()).thenReturn(JvmControlOperation.SCP);
        jvmControlService.secureCopyFile(mockControlJvmRequest, "./source/path", "./dest/path", "user-id", true);
    }

    @Test(expected = InternalErrorException.class)
    public void testSecureCopyConfFileFailsCreateDirectory() throws CommandFailureException {
        when(Config.mockShellCommandFactory.executeRemoteCommand(anyString(), eq(Command.CREATE_DIR), anyString())).thenReturn(new RemoteCommandReturnInfo(1, "", "FAILED BACK UP"));

        final Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getHostName()).thenReturn("host");
        when(Config.mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(mockJvm);
        when(Config.mockShellCommandFactory.executeRemoteCommand(eq("host"), eq(Command.SCP), anyString(), anyString())).thenReturn(mock(RemoteCommandReturnInfo.class));

        final ControlJvmRequest mockControlJvmRequest = mock(ControlJvmRequest.class);
        when(mockControlJvmRequest.getControlOperation()).thenReturn(JvmControlOperation.SCP);
        jvmControlService.secureCopyFile(mockControlJvmRequest, "./source/path", "./dest/path", "user-id", true);
    }

    @Test
    public void testChangeFileMode() throws CommandFailureException {
        final Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getHostName()).thenReturn("test-host");
        when(Config.mockShellCommandFactory.executeRemoteCommand(anyString(), eq(Command.CHANGE_FILE_MODE), anyString(), anyString())).thenReturn(mock(RemoteCommandReturnInfo.class));
        jvmControlService.executeChangeFileModeCommand(mockJvm, "777", "./target", "*");
        verify(Config.mockShellCommandFactory).executeRemoteCommand("test-host", Command.CHANGE_FILE_MODE, "777", "./target/*");
    }

    @Test
    public void testCreateDirectory() throws CommandFailureException {
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getHostName()).thenReturn("test-host");
        when(Config.mockShellCommandFactory.executeRemoteCommand(anyString(), eq(Command.CREATE_DIR), anyString())).thenReturn(mock(RemoteCommandReturnInfo.class));
        jvmControlService.executeCreateDirectoryCommand(mockJvm, "./target");
        verify(Config.mockShellCommandFactory).executeRemoteCommand("test-host", Command.CREATE_DIR, "./target");
    }

    @Test
    public void testStartControlJvmSynchronously() throws InterruptedException {
        final Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STARTED);
        when(Config.mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(mockJvm);
        when(Config.mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(0, "Success!", ""));
        when(Config.mockJvmCommandFactory.executeCommand(mockJvm, JvmControlOperation.START)).thenReturn(new RemoteCommandReturnInfo(0, "Success!", ""));
        final ControlJvmRequest controlJvmRequest = new ControlJvmRequest(new Identifier<Jvm>("1"), JvmControlOperation.START);
        final CommandOutput commandOutput = jvmControlService.controlJvmSynchronously(controlJvmRequest, 60000, new User("jedi"));
        assertTrue(commandOutput.getReturnCode().getWasSuccessful());
    }

    @Test
    public void testStopControlJvmSynchronously() throws InterruptedException {
        final Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getHostName()).thenReturn("mockJvmHost");
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(Config.mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(mockJvm);
        when(Config.mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(0, "Success!", ""));
        when(Config.mockJvmCommandFactory.executeCommand(mockJvm, JvmControlOperation.STOP)).thenReturn(new RemoteCommandReturnInfo(0, "Success!", ""));
        final ControlJvmRequest controlJvmRequest = new ControlJvmRequest(new Identifier<Jvm>("1"), JvmControlOperation.STOP);
        final CommandOutput commandOutput = jvmControlService.controlJvmSynchronously(controlJvmRequest, 60000, new User("jedi"));
        assertTrue(commandOutput.getReturnCode().getWasSuccessful());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testHeapDumpControlJvmSynchronously() throws InterruptedException {
        final Jvm mockJvm = mock(Jvm.class);
        when(Config.mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(mockJvm);
        when(Config.mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(0, "***heapdump-start***hi there***heapdump-end***", ""));
        when(Config.mockJvmCommandFactory.executeCommand(mockJvm, JvmControlOperation.HEAP_DUMP)).thenReturn(new RemoteCommandReturnInfo(0, "***heapdump-start***hi there***heapdump-end***", ""));
        final ControlJvmRequest controlJvmRequest = new ControlJvmRequest(new Identifier<Jvm>("1"), JvmControlOperation.HEAP_DUMP);
        final CommandOutput commandOutput = jvmControlService.controlJvmSynchronously(controlJvmRequest, 60000, new User("jedi"));
    }

    @Test(expected = JvmControlServiceException.class)
    public void testControlJvmSynchronouslyOnTimeout() throws InterruptedException {
        final Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPING);
        when(Config.mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(mockJvm);
        when(Config.mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(0, "Success!", ""));
        final ControlJvmRequest controlJvmRequest = new ControlJvmRequest(new Identifier<Jvm>("1"), JvmControlOperation.STOP);
        when(Config.mockJvmCommandFactory.executeCommand(any(Jvm.class), any(JvmControlOperation.class))).thenReturn(mock(RemoteCommandReturnInfo.class));
        final CommandOutput commandOutput = jvmControlService.controlJvmSynchronously(controlJvmRequest, 3000, new User("jedi"));
    }

    @ContextConfiguration
    static class Config {

        @Mock
        static JvmCommandFactory mockJvmCommandFactory;

        @Mock
        static BinaryDistributionControlService mockBinaryDistributionControlService;

        @Mock
        static AemSshConfig mockAemSshConfig;

        @Mock
        static ShellCommandFactory mockShellCommandFactory;

        @Mock
        static HistoryFacadeService mockHistoryFacadeService;

        @Mock
        static JvmStateService mockJvmStateService;

        @Mock
        static RemoteCommandExecutorService mockRemoteCommandExecutorService;

        @Mock
        static SshConfiguration mockSshConfig;

        @Mock
        static JvmPersistenceService mockJvmPersistenceService;

        public Config() {
            initMocks(this);
        }

        @Bean
        public JvmCommandFactory getJvmCommandFactory() {
            return mockJvmCommandFactory;
        }

        @Bean
        public BinaryDistributionControlService getMockBinaryDistributionControlService() {
            return mockBinaryDistributionControlService;
        }

        @Bean
        public static AemSshConfig getAemSshConfig() {
            return mockAemSshConfig;
        }

        @Bean
        public ShellCommandFactory getShellCommandFactory() {
            return mockShellCommandFactory;
        }

        @Bean
        public HistoryFacadeService getHistoryFacadeService() {
            return mockHistoryFacadeService;
        }

        @Bean
        public JvmStateService getJvmStateService() {
            return mockJvmStateService;
        }

        @Bean
        public RemoteCommandExecutorService getRemoteCommandExecutorService() {
            return mockRemoteCommandExecutorService;
        }

        @Bean
        public SshConfiguration getSshConfig() {
            return mockSshConfig;
        }

        @Bean
        public JvmPersistenceService getJvmPersistenceService() {
            return mockJvmPersistenceService;
        }

        @Bean
        @Scope("prototype")
        public JvmControlService getJvmControlService() {
            reset(mockJvmCommandFactory, mockAemSshConfig, mockShellCommandFactory, mockHistoryFacadeService,
                    mockJvmStateService, mockRemoteCommandExecutorService, mockSshConfig,
                    mockJvmPersistenceService);

            return new JvmControlServiceImpl(
                    mockJvmPersistenceService,
                    mockJvmStateService,
                    mockHistoryFacadeService);
        }

    }

}
