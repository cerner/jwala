package com.cerner.jwala.service.binarydistribution;

import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.media.Media;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.ExecCommand;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.control.configuration.SshConfig;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.service.HistoryFacadeService;
import com.cerner.jwala.service.RemoteCommandExecutorService;
import com.cerner.jwala.service.binarydistribution.impl.BinaryDistributionServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.File;

import static com.cerner.jwala.control.AemControl.Properties.UNZIP_SCRIPT_NAME;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by LW044480 on 9/8/2016.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
        classes = {BinaryDistributionServiceImplTest.Config.class})
public class BinaryDistributionServiceImplTest {

    @Autowired
    private BinaryDistributionService binaryDistributionService;

    @Before
    public void setup() {
        initMocks(this);
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources");
    }

    @After
    public void tearDown() {
        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testRemoteFileCheck() throws CommandFailureException {
        String hostname = "localhost";
        String destination = "test1234";
        when(Config.mockBinaryDistributionControlService.checkFileExists(eq(hostname), eq(destination)))
                .thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        binaryDistributionService.remoteFileCheck(hostname, destination);
    }

    @Test(expected = InternalErrorException.class)
    public void testRemoteFileCheckException() throws CommandFailureException {
        String hostname = "localhost";
        String destination = "test1234";
        when(Config.mockBinaryDistributionControlService.checkFileExists(eq(hostname), eq(destination)))
                .thenThrow(new CommandFailureException(new ExecCommand("failed command"), new Throwable()));
        binaryDistributionService.remoteFileCheck(hostname, destination);
    }

    @Test
    public void testRemoteCreateDirectory() throws CommandFailureException {
        final String hostname = "localhost";
        final String destination = "testDest";
        when(Config.mockBinaryDistributionControlService.createDirectory(eq(hostname), eq(destination)))
                .thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        binaryDistributionService.remoteCreateDirectory(hostname, destination);
    }

    @Test(expected = InternalErrorException.class)
    public void testRemoteCreateDirectoryFail() throws CommandFailureException {
        final String hostname = "localhost";
        final String destination = "testDest";
        when(Config.mockBinaryDistributionControlService.createDirectory(eq(hostname), eq(destination)))
                .thenReturn(new CommandOutput(new ExecReturnCode(1), "FAIL", ""));
        binaryDistributionService.remoteCreateDirectory(hostname, destination);
    }

    @Test(expected = InternalErrorException.class)
    public void testRemoteCreateDirectoryException() throws CommandFailureException {
        final String hostname = "localhost";
        final String destination = "testDest";
        when(Config.mockBinaryDistributionControlService.createDirectory(eq(hostname), eq(destination)))
                .thenThrow(new CommandFailureException(new ExecCommand("failed command"), new Throwable()));
        binaryDistributionService.remoteCreateDirectory(hostname, destination);
    }

    @Test
    public void testRemoteSecureCopyFile() throws CommandFailureException {
        final String hostname = "localhost";
        final String source = "testSource";
        final String destination = "testDest";
        when(Config.mockBinaryDistributionControlService.secureCopyFile(eq(hostname), eq(source), eq(destination)))
                .thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        binaryDistributionService.remoteSecureCopyFile(hostname, source, destination);
    }

    @Test(expected = InternalErrorException.class)
    public void testRemoteSecureCopyFileFail() throws CommandFailureException {
        final String hostname = "localhost";
        final String source = "testSource";
        final String destination = "testDest";
        when(Config.mockBinaryDistributionControlService.secureCopyFile(eq(hostname), eq(source), eq(destination)))
                .thenReturn(new CommandOutput(new ExecReturnCode(1), "FAIL", ""));
        binaryDistributionService.remoteSecureCopyFile(hostname, source, destination);
    }

    @Test(expected = InternalErrorException.class)
    public void testRemoteSecureCopyFileException() throws CommandFailureException {
        final String hostname = "localhost";
        final String source = "testSource";
        final String destination = "testDest";
        when(Config.mockBinaryDistributionControlService.secureCopyFile(eq(hostname), eq(source), eq(destination)))
                .thenThrow(new CommandFailureException(new ExecCommand("failed command"), new Throwable()));
        binaryDistributionService.remoteSecureCopyFile(hostname, source, destination);
    }

    @Test
    public void testRemoteUnzipBinary() throws CommandFailureException {
        final String hostname = "localhost";
        when(Config.mockBinaryDistributionControlService.unzipBinary(eq(hostname), anyString(), anyString(), anyString()))
                .thenReturn(new CommandOutput(new ExecReturnCode(0), "", ""));
        binaryDistributionService.remoteUnzipBinary(hostname, "any", "any", "");
        verify(Config.mockBinaryDistributionControlService).unzipBinary(hostname, "any", "any", "");
    }

    @Test(expected = InternalErrorException.class)
    public void testRemoteUnzipBinaryFail() throws CommandFailureException {
        final String hostname = "localhost";
        when(Config.mockBinaryDistributionControlService.unzipBinary(eq(hostname), anyString(), anyString(), anyString()))
                .thenReturn(new CommandOutput(new ExecReturnCode(ExecReturnCode.JWALA_EXIT_CODE_NO_OP), "", ""));
        binaryDistributionService.remoteUnzipBinary(hostname, "any", "any", "");
    }

    @Test
    public void testRemoteDeleteBinary() throws CommandFailureException {
        final String hostname = "localhost";
        final String destination = "testDest";
        when(Config.mockBinaryDistributionControlService.deleteBinary(hostname, destination)).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        binaryDistributionService.remoteDeleteBinary(hostname, destination);
        verify(Config.mockBinaryDistributionControlService).deleteBinary(hostname, destination);
    }

    @Test(expected = InternalErrorException.class)
    public void testRemoteDeleteBinaryFail() throws CommandFailureException {
        final String hostname = "localhost";
        final String destination = "testDest";
        when(Config.mockBinaryDistributionControlService.deleteBinary(eq(hostname), eq(destination)))
                .thenReturn(new CommandOutput(new ExecReturnCode(1), "FAIL", ""));
        binaryDistributionService.remoteDeleteBinary(hostname, destination);
    }

    @Test(expected = InternalErrorException.class)
    public void testRemoteDeleteBinaryException() throws CommandFailureException {
        final String hostname = "localhost";
        final String destination = "testDest";
        when(Config.mockBinaryDistributionControlService.deleteBinary(eq(hostname), eq(destination)))
                .thenThrow(new CommandFailureException(new ExecCommand("failed command"), new Throwable()));
        binaryDistributionService.remoteDeleteBinary(hostname, destination);
    }

    @Test
    public void testChangeFileMode() throws CommandFailureException {
        final String hostname = "localhost";
        final String mode = "testMode";
        final String targetDir = "~/test";
        final String target = "testFile";
        when(Config.mockBinaryDistributionControlService.changeFileMode(eq(hostname), eq(mode), eq(targetDir), eq(target)))
                .thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        binaryDistributionService.changeFileMode(hostname, mode, targetDir, target);
        verify(Config.mockBinaryDistributionControlService).changeFileMode(hostname, mode, targetDir, target);
    }

    @Test(expected = InternalErrorException.class)
    public void testChangeFileModeFail() throws CommandFailureException {
        final String hostname = "localhost";
        final String mode = "testMode";
        final String targetDir = "~/test";
        final String target = "testFile";
        when(Config.mockBinaryDistributionControlService.changeFileMode(eq(hostname), eq(mode), eq(targetDir), eq(target)))
                .thenReturn(new CommandOutput(new ExecReturnCode(1), "FAIL", ""));
        binaryDistributionService.changeFileMode(hostname, mode, targetDir, target);
    }

    @Test(expected = InternalErrorException.class)
    public void testChangeFileModeException() throws CommandFailureException{
        final String hostname = "localhost";
        final String mode = "testMode";
        final String targetDir = "~/test";
        final String target = "testFile";
        when(Config.mockBinaryDistributionControlService.changeFileMode(eq(hostname), eq(mode), eq(targetDir), eq(target)))
                .thenThrow(new CommandFailureException(new ExecCommand("failed command"), new Throwable()));
        binaryDistributionService.changeFileMode(hostname, mode, targetDir, target);
    }

    @Test
    @Ignore
    public void testDistributeJdk() throws CommandFailureException {
        final String hostname = "localhost";
        final CommandOutput successfulCommandOutput = new CommandOutput(new ExecReturnCode(0), "SUCCESS", "");
        final CommandOutput unsuccessfulCommandOutout = new CommandOutput(new ExecReturnCode(1), "", "");
        final Jvm mockJvm = mock(Jvm.class);
        final Media mockJdkMedia = mock(Media.class);
        when(mockJvm.getJdkMedia()).thenReturn(mockJdkMedia);
        when(mockJvm.getHostName()).thenReturn(hostname);
        when(mockJdkMedia.getRemoteHostPath()).thenReturn("anywhere");
        when(mockJdkMedia.getMediaDir()).thenReturn("anywhere");
        when(Config.mockBinaryDistributionControlService.checkFileExists(anyString(), anyString())).thenReturn(unsuccessfulCommandOutout);
        when(Config.mockBinaryDistributionControlService.createDirectory(eq(hostname), anyString())).thenReturn(successfulCommandOutput);
        when(Config.mockBinaryDistributionControlService.secureCopyFile(eq(hostname), anyString(), anyString())).thenReturn(successfulCommandOutput);
        when(Config.mockBinaryDistributionControlService.unzipBinary(eq(hostname), anyString(), anyString(), anyString())).thenReturn(successfulCommandOutput);
        when(Config.mockBinaryDistributionControlService.deleteBinary(eq(hostname), anyString())).thenReturn(successfulCommandOutput);
        binaryDistributionService.distributeJdk(mockJvm);
        verify(Config.mockBinaryDistributionControlService).secureCopyFile(eq(hostname), anyString(), anyString());
    }

    @Test
    public void testDistributeJdkOnAnExistingOne() throws CommandFailureException {
        final String hostname = "localhost";
        final CommandOutput successfulCommandOutput = new CommandOutput(new ExecReturnCode(0), "SUCCESS", "");
        final Jvm mockJvm = mock(Jvm.class);
        final Media mockJdkMedia = mock(Media.class);
        when(mockJvm.getJdkMedia()).thenReturn(mockJdkMedia);
        when(mockJvm.getHostName()).thenReturn(hostname);
        when(mockJdkMedia.getRemoteHostPath()).thenReturn("anywhere");
        when(mockJdkMedia.getMediaDir()).thenReturn("anywhere");
        when(Config.mockBinaryDistributionControlService.checkFileExists(anyString(), anyString())).thenReturn(successfulCommandOutput);
        binaryDistributionService.distributeJdk(mockJvm);
        verify(Config.mockBinaryDistributionControlService, never()).secureCopyFile(eq(hostname), anyString(), anyString());
    }

    @Test
    @Ignore
    public void testDistributeWebServer() throws CommandFailureException {
        final File apache = new File(ApplicationProperties.get("remote.paths.apache.httpd"));
        final String webServerDir = apache.getName();
        final String webServerBinaryDeployDir = apache.getParentFile().getAbsolutePath().replaceAll("\\\\", "/");
        final String hostname = "localhost";
        final CommandOutput successfulCommandOutput = new CommandOutput(new ExecReturnCode(0), "SUCCESS", "");
        when(Config.mockBinaryDistributionControlService.createDirectory(eq(hostname), anyString())).thenReturn(successfulCommandOutput);
        when(Config.mockBinaryDistributionControlService.secureCopyFile(eq(hostname), anyString(), anyString())).thenReturn(successfulCommandOutput);
        when(Config.mockBinaryDistributionControlService.deleteBinary(eq(hostname), eq(webServerBinaryDeployDir + "/" + webServerDir + ".zip"))).thenReturn(successfulCommandOutput);
        when(Config.mockBinaryDistributionControlService.unzipBinary(eq(hostname), anyString(), anyString(), anyString())).thenReturn(successfulCommandOutput);
        when(Config.mockBinaryDistributionControlService.deleteBinary(eq(hostname), anyString())).thenReturn(successfulCommandOutput);
        binaryDistributionService.distributeWebServer(hostname);
        verify(Config.mockBinaryDistributionControlService).secureCopyFile(eq(hostname), anyString(), anyString());
    }

    @Test
    public void testPrepareUnzip() throws CommandFailureException {
        final String hostname = "localhost";
        final String jwalaScriptsPath = ApplicationProperties.get("remote.commands.user-scripts");
        final String remoteUnzipScriptPath = jwalaScriptsPath + "/" + UNZIP_SCRIPT_NAME;
        when(Config.mockBinaryDistributionControlService.checkFileExists(hostname, jwalaScriptsPath)).thenReturn(new CommandOutput(new ExecReturnCode(1), "FAIL", ""));
        when(Config.mockBinaryDistributionControlService.createDirectory(hostname, jwalaScriptsPath)).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(Config.mockBinaryDistributionControlService.checkFileExists(hostname, jwalaScriptsPath + "/unzip.exe")).thenReturn(new CommandOutput(new ExecReturnCode(1), "FAIL", ""));
        when(Config.mockBinaryDistributionControlService.checkFileExists(hostname, remoteUnzipScriptPath)).thenReturn(new CommandOutput(new ExecReturnCode(1), "FAIL", ""));
        when(Config.mockBinaryDistributionControlService.secureCopyFile(anyString(), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(Config.mockBinaryDistributionControlService.changeFileMode(anyString(), anyString(), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        binaryDistributionService.distributeUnzip(hostname);
    }

    @Configuration
    static class Config {

        @Mock
        static BinaryDistributionControlService mockBinaryDistributionControlService;

        @Mock
        static BinaryDistributionLockManager mockBinaryDistributionLockManager;

        @Mock
        static HistoryFacadeService historyFacadeService;

        @Mock
        static SshConfig mockSshConfig;

        @Mock
        static SshConfiguration mockSshConfiguration;

        @Mock
        static RemoteCommandExecutorService mockRemoteCommandExecutorService;


        public Config() {
            initMocks(this);
        }

        @Bean
        public BinaryDistributionControlService getMockBinaryDistributionControlService() {
            return mockBinaryDistributionControlService;
        }

        @Bean
        public BinaryDistributionLockManager getMockBinaryDistributionLockManager() {
            return mockBinaryDistributionLockManager;
        }

        @Bean
        public HistoryFacadeService getHistoryFacadeService() {
            return historyFacadeService;
        }

        @Bean
        public SshConfig getMockSshConfig() {
            return mockSshConfig;
        }

        @Bean
        @Scope("prototype")
        public BinaryDistributionService getBinaryDistributionService() {
            reset(mockBinaryDistributionControlService, mockBinaryDistributionLockManager,
                    historyFacadeService, mockSshConfig, mockSshConfiguration, mockRemoteCommandExecutorService);
            return new BinaryDistributionServiceImpl();
        }

        @Bean
        public static SshConfiguration getMockSshConfiguration() {
            return mockSshConfiguration;
        }

    }

}
