package com.cerner.jwala.control.jvm.command;

import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.exec.RemoteExecCommand;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.service.RemoteCommandExecutorService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionControlService;
import com.cerner.jwala.service.exception.ApplicationServiceException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Created on 2/21/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {JvmCommandFactoryTest.Config.class})

public class JvmCommandFactoryTest {

    private static final RemoteCommandReturnInfo SUCCESS_REMOTE_COMMAND_INFO = new RemoteCommandReturnInfo(0, "standard out", "");

    @Autowired
    JvmCommandFactory jvmCommandFactory;

    @Before
    public void setup() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources");
        reset(
                Config.mockSshConfig,
                Config.mockBinaryDistributionControlService,
                Config.mockRemoteCommandExecutorService
        );
    }

    @Test(expected = ApplicationServiceException.class)
    public void testExecuteCommandFileExists() {
        Jvm mockJvm = mock(Jvm.class);
        jvmCommandFactory.executeCommand(mockJvm, JvmControlOperation.CHECK_FILE_EXISTS);
    }

    @Test
    public void testStartJvm() {
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getJvmName()).thenReturn("test-jvm-command-factory");

        when(Config.mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(SUCCESS_REMOTE_COMMAND_INFO);

        when(Config.mockBinaryDistributionControlService.checkFileExists(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "File exists", ""));

        RemoteCommandReturnInfo commandReturnInfo = jvmCommandFactory.executeCommand(mockJvm, JvmControlOperation.START);

        assertEquals(SUCCESS_REMOTE_COMMAND_INFO, commandReturnInfo);
        verify(Config.mockSshConfig, times(1)).getEncryptedPassword();
        verify(Config.mockSshConfig, times(1)).getUserName();
    }

    @Test
    public void testStopJvm() {
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getJvmName()).thenReturn("test-jvm-command-factory");

        when(Config.mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(SUCCESS_REMOTE_COMMAND_INFO);

        when(Config.mockBinaryDistributionControlService.checkFileExists(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "File exists", ""));

        RemoteCommandReturnInfo commandReturnInfo = jvmCommandFactory.executeCommand(mockJvm, JvmControlOperation.STOP);

        assertEquals(SUCCESS_REMOTE_COMMAND_INFO, commandReturnInfo);
        verify(Config.mockSshConfig, times(1)).getEncryptedPassword();
        verify(Config.mockSshConfig, times(1)).getUserName();
    }

    @Test
    public void testJvmThreadDump() {
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getJvmName()).thenReturn("test-jvm-command-factory");

        when(Config.mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(SUCCESS_REMOTE_COMMAND_INFO);

        when(Config.mockBinaryDistributionControlService.checkFileExists(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "File exists", ""));

        RemoteCommandReturnInfo commandReturnInfo = jvmCommandFactory.executeCommand(mockJvm, JvmControlOperation.THREAD_DUMP);

        assertEquals(SUCCESS_REMOTE_COMMAND_INFO, commandReturnInfo);
        verify(Config.mockSshConfig, times(1)).getEncryptedPassword();
        verify(Config.mockSshConfig, times(1)).getUserName();
    }

    @Test
    public void testJvmHeapDump() {
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getJvmName()).thenReturn("test-jvm-command-factory");

        when(Config.mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(SUCCESS_REMOTE_COMMAND_INFO);

        when(Config.mockBinaryDistributionControlService.checkFileExists(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "File exists", ""));

        RemoteCommandReturnInfo commandReturnInfo = jvmCommandFactory.executeCommand(mockJvm, JvmControlOperation.HEAP_DUMP);

        assertEquals(SUCCESS_REMOTE_COMMAND_INFO, commandReturnInfo);
        verify(Config.mockSshConfig, times(1)).getEncryptedPassword();
        verify(Config.mockSshConfig, times(1)).getUserName();
    }

    @Test
    public void testJvmDeployConfigArchive() {
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getJvmName()).thenReturn("test-jvm-command-factory");

        when(Config.mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(SUCCESS_REMOTE_COMMAND_INFO);

        when(Config.mockBinaryDistributionControlService.checkFileExists(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "File exists", ""));

        RemoteCommandReturnInfo commandReturnInfo = jvmCommandFactory.executeCommand(mockJvm, JvmControlOperation.DEPLOY_CONFIG_ARCHIVE);

        assertEquals(SUCCESS_REMOTE_COMMAND_INFO, commandReturnInfo);
        verify(Config.mockSshConfig, times(1)).getEncryptedPassword();
        verify(Config.mockSshConfig, times(1)).getUserName();
    }

    @Test
    public void testJvmInstallService() {
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getJvmName()).thenReturn("test-jvm-command-factory");

        when(Config.mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(SUCCESS_REMOTE_COMMAND_INFO);

        when(Config.mockBinaryDistributionControlService.checkFileExists(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "File exists", ""));

        RemoteCommandReturnInfo commandReturnInfo = jvmCommandFactory.executeCommand(mockJvm, JvmControlOperation.INSTALL_SERVICE);

        assertEquals(SUCCESS_REMOTE_COMMAND_INFO, commandReturnInfo);
        verify(Config.mockSshConfig, times(1)).getEncryptedPassword();
        verify(Config.mockSshConfig, times(1)).getUserName();
    }

    @Test
    public void testJvmDeleteService() {
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getJvmName()).thenReturn("test-jvm-command-factory");

        when(Config.mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(SUCCESS_REMOTE_COMMAND_INFO);

        when(Config.mockBinaryDistributionControlService.checkFileExists(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "File exists", ""));

        RemoteCommandReturnInfo commandReturnInfo = jvmCommandFactory.executeCommand(mockJvm, JvmControlOperation.DELETE_SERVICE);

        assertEquals(SUCCESS_REMOTE_COMMAND_INFO, commandReturnInfo);
        verify(Config.mockSshConfig, times(1)).getEncryptedPassword();
        verify(Config.mockSshConfig, times(1)).getUserName();
    }

    @Configuration
    static class Config {

        private static final SshConfiguration mockSshConfig = mock(SshConfiguration.class);
        private static final RemoteCommandExecutorService mockRemoteCommandExecutorService = mock(RemoteCommandExecutorService.class);
        private static final BinaryDistributionControlService mockBinaryDistributionControlService = mock(BinaryDistributionControlService.class);

        @Bean
        public SshConfiguration getSshConfig() {
            return mockSshConfig;
        }

        @Bean
        public RemoteCommandExecutorService getRemoteCommandExecutorService() {
            return mockRemoteCommandExecutorService;
        }

        @Bean
        public BinaryDistributionControlService getMockBinaryDistributionControlService() {
            return mockBinaryDistributionControlService;
        }

        @Bean
        public JvmCommandFactory getJvmCommandFactory() {
            return new JvmCommandFactory();
        }

    }
}
