package com.cerner.jwala.control.jvm.command;

import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;
import com.cerner.jwala.common.domain.model.media.Media;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.exec.RemoteExecCommand;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.service.RemoteCommandExecutorService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionControlService;
import com.cerner.jwala.service.exception.ApplicationServiceException;
import com.cerner.jwala.service.jvm.exception.JvmServiceException;
import com.cerner.jwala.service.resource.ResourceContentGeneratorService;
import com.cerner.jwala.service.resource.impl.ResourceGeneratorType;
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
import java.nio.file.Path;
import java.nio.file.Paths;

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
        when(mockJvm.getJavaHome()).thenReturn("c:/jdk/java-home");

        Media mockTomcatMedia = mock(Media.class);
        Path mockPath = mock(Path.class);
        when(mockTomcatMedia.getRootDir()).thenReturn(mockPath);
        when(mockTomcatMedia.getRemoteDir()).thenReturn(Paths.get("d:/ctp/app/instance"));
        when(mockPath.toString()).thenReturn("test-tomcat-7");
        when(mockJvm.getTomcatMedia()).thenReturn(mockTomcatMedia);

        when(Config.mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(SUCCESS_REMOTE_COMMAND_INFO);

        when(Config.mockBinaryDistributionControlService.checkFileExists(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "File exists", ""));

        when(Config.mockResourceContentGeneratorService.generateContent(anyString(), anyString(), any(ResourceGroup.class), anyObject(), any(ResourceGeneratorType.class))).thenReturn("c:/jdk/java-home");

        RemoteCommandReturnInfo commandReturnInfo = jvmCommandFactory.executeCommand(mockJvm, JvmControlOperation.THREAD_DUMP);

        assertEquals(SUCCESS_REMOTE_COMMAND_INFO, commandReturnInfo);
        verify(Config.mockSshConfig, times(1)).getEncryptedPassword();
        verify(Config.mockSshConfig, times(1)).getUserName();
    }

    @Test
    public void testJvmHeapDump() {
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getJvmName()).thenReturn("test-jvm-command-factory");
        when(mockJvm.getJavaHome()).thenReturn("c:/jdk/java-home");

        Media mockTomcatMedia = mock(Media.class);
        Path mockPath = mock(Path.class);
        when(mockTomcatMedia.getRootDir()).thenReturn(mockPath);
        when(mockTomcatMedia.getRemoteDir()).thenReturn(Paths.get("d:/ctp/app/instance"));
        when(mockPath.toString()).thenReturn("test-tomcat-7");
        when(mockJvm.getTomcatMedia()).thenReturn(mockTomcatMedia);

        when(Config.mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(SUCCESS_REMOTE_COMMAND_INFO);

        when(Config.mockBinaryDistributionControlService.checkFileExists(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "File exists", ""));

        when(Config.mockResourceContentGeneratorService.generateContent(anyString(), anyString(), any(ResourceGroup.class), anyObject(), any(ResourceGeneratorType.class))).thenReturn("c:/jdk/java-home");

        RemoteCommandReturnInfo commandReturnInfo = jvmCommandFactory.executeCommand(mockJvm, JvmControlOperation.HEAP_DUMP);

        assertEquals(SUCCESS_REMOTE_COMMAND_INFO, commandReturnInfo);
        verify(Config.mockSshConfig, times(1)).getEncryptedPassword();
        verify(Config.mockSshConfig, times(1)).getUserName();
    }

    @Test
    public void testJvmDeployConfigArchive() {
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getJvmName()).thenReturn("test-jvm-command-factory");
        when(mockJvm.getJavaHome()).thenReturn("c:/jdk/java-home");

        final Media tomcatMedia = new Media();
        tomcatMedia.setRemoteDir(Paths.get("d:/ctp/app/instance"));
        when(mockJvm.getTomcatMedia()).thenReturn(tomcatMedia);

        when(Config.mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(SUCCESS_REMOTE_COMMAND_INFO);

        when(Config.mockBinaryDistributionControlService.checkFileExists(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "File exists", ""));

        RemoteCommandReturnInfo commandReturnInfo = jvmCommandFactory.executeCommand(mockJvm, JvmControlOperation.DEPLOY_JVM_ARCHIVE);

        assertEquals(SUCCESS_REMOTE_COMMAND_INFO, commandReturnInfo);
        verify(Config.mockSshConfig, times(1)).getEncryptedPassword();
        verify(Config.mockSshConfig, times(1)).getUserName();
    }

    @Test
    public void testJvmInstallService() {
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getJvmName()).thenReturn("test-jvm-command-factory");

        Media mockTomcatMedia = mock(Media.class);
        Path mockPath = mock(Path.class);
        when(mockTomcatMedia.getRootDir()).thenReturn(mockPath);
        when(mockTomcatMedia.getRemoteDir()).thenReturn(Paths.get("d:/ctp/app/instance"));
        when(mockPath.toString()).thenReturn("test-tomcat-7");

        when(mockJvm.getTomcatMedia()).thenReturn(mockTomcatMedia);
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

    @Test
    public void testJvmDeleteServiceWithSecureCopy() {
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getJvmName()).thenReturn("test-jvm-command-factory");

        when(Config.mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(SUCCESS_REMOTE_COMMAND_INFO);

        when(Config.mockBinaryDistributionControlService.checkFileExists(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "File doesn't exist - create directory, copy script, and make executable", ""));
        when(Config.mockBinaryDistributionControlService.createDirectory(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Successfully created directory", ""));
        when(Config.mockBinaryDistributionControlService.secureCopyFile(anyString(), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Successfully copied script", ""));
        when(Config.mockBinaryDistributionControlService.changeFileMode(anyString(), anyString(), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Successfully changed file mode", ""));

        RemoteCommandReturnInfo commandReturnInfo = jvmCommandFactory.executeCommand(mockJvm, JvmControlOperation.DELETE_SERVICE);

        assertEquals(SUCCESS_REMOTE_COMMAND_INFO, commandReturnInfo);
        verify(Config.mockSshConfig, times(1)).getEncryptedPassword();
        verify(Config.mockSshConfig, times(1)).getUserName();
    }

    @Test(expected = JvmServiceException.class)
    public void testJvmDeleteServiceFailsMakeDirectory() {
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getJvmName()).thenReturn("test-jvm-command-factory");

        when(Config.mockBinaryDistributionControlService.checkFileExists(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "File doesn't exist - create directory, copy script, and make executable", ""));
        when(Config.mockBinaryDistributionControlService.createDirectory(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "", "Failed to create directory"));

        RemoteCommandReturnInfo commandReturnInfo = jvmCommandFactory.executeCommand(mockJvm, JvmControlOperation.DELETE_SERVICE);

        assertEquals(SUCCESS_REMOTE_COMMAND_INFO, commandReturnInfo);
        verify(Config.mockSshConfig, times(1)).getEncryptedPassword();
        verify(Config.mockSshConfig, times(1)).getUserName();
        verify(Config.mockBinaryDistributionControlService, never()).secureCopyFile(anyString(), anyString(), anyString());
        verify(Config.mockBinaryDistributionControlService, never()).changeFileMode(anyString(), anyString(), anyString(), anyString());
        verify(Config.mockRemoteCommandExecutorService, never()).executeCommand(any(RemoteExecCommand.class));
    }

    @Test(expected = JvmServiceException.class)
    public void testJvmDeleteServiceFailsSecureCopy() {
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getJvmName()).thenReturn("test-jvm-command-factory");

        when(Config.mockBinaryDistributionControlService.checkFileExists(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "File doesn't exist - create directory, copy script, and make executable", ""));
        when(Config.mockBinaryDistributionControlService.createDirectory(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Successfully created directory", ""));
        when(Config.mockBinaryDistributionControlService.secureCopyFile(anyString(), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "", "Failed secure copy"));

        RemoteCommandReturnInfo commandReturnInfo = jvmCommandFactory.executeCommand(mockJvm, JvmControlOperation.DELETE_SERVICE);

        assertEquals(SUCCESS_REMOTE_COMMAND_INFO, commandReturnInfo);
        verify(Config.mockSshConfig, times(1)).getEncryptedPassword();
        verify(Config.mockSshConfig, times(1)).getUserName();
        verify(Config.mockBinaryDistributionControlService, never()).changeFileMode(anyString(), anyString(), anyString(), anyString());
        verify(Config.mockRemoteCommandExecutorService, never()).executeCommand(any(RemoteExecCommand.class));
    }

    @Test(expected = JvmServiceException.class)
    public void testJvmDeleteServiceFailsChangeFileMode() {
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getJvmName()).thenReturn("test-jvm-command-factory");

        when(Config.mockBinaryDistributionControlService.checkFileExists(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "File doesn't exist - create directory, copy script, and make executable", ""));
        when(Config.mockBinaryDistributionControlService.createDirectory(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Successfully created directory", ""));
        when(Config.mockBinaryDistributionControlService.secureCopyFile(anyString(), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Successfully secure copied script", ""));
        when(Config.mockBinaryDistributionControlService.changeFileMode(anyString(), anyString(), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "", "Failed to change the file mode"));

        RemoteCommandReturnInfo commandReturnInfo = jvmCommandFactory.executeCommand(mockJvm, JvmControlOperation.DELETE_SERVICE);

        assertEquals(SUCCESS_REMOTE_COMMAND_INFO, commandReturnInfo);
        verify(Config.mockSshConfig, times(1)).getEncryptedPassword();
        verify(Config.mockSshConfig, times(1)).getUserName();
        verify(Config.mockRemoteCommandExecutorService, never()).executeCommand(any(RemoteExecCommand.class));
    }

    @Configuration
    static class Config {

        private static final SshConfiguration mockSshConfig = mock(SshConfiguration.class);
        private static final RemoteCommandExecutorService mockRemoteCommandExecutorService = mock(RemoteCommandExecutorService.class);
        private static final BinaryDistributionControlService mockBinaryDistributionControlService = mock(BinaryDistributionControlService.class);
        private static final ResourceContentGeneratorService mockResourceContentGeneratorService = mock(ResourceContentGeneratorService.class);

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

        @Bean
        public ResourceContentGeneratorService getMockResourceContentGeneratorService() {
            return mockResourceContentGeneratorService;
        }
    }
}
