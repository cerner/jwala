package com.cerner.jwala.service.binarydistribution;

import com.cerner.jwala.commandprocessor.impl.jsch.JschBuilder;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.exec.RemoteExecCommand;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.control.configuration.SshConfig;
import com.cerner.jwala.service.RemoteCommandExecutorService;
import com.cerner.jwala.service.binarydistribution.impl.BinaryDistributionControlServiceImpl;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
                      classes = {BinaryDistributionControlServiceImplTest.Config.class})
public class BinaryDistributionControlServiceImplTest {

    @Autowired
    private ApplicationContext appContext;

    @Autowired
    private BinaryDistributionControlService binaryDistributionControlService;

    @Before
    public void setup() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources");
    }

    @After
    public void tearDown() {
        reset(Config.mockSshConfiguration);
        reset(Config.mockSshConfig);
        reset(Config.mockRemoteCommandExecutorService);
        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testSecureCopyFile() throws JSchException, IOException {
        final JschBuilder mockJschBuilder = mock(JschBuilder.class);
        final JSch mockJsch = mock(JSch.class);
        final Session mockSession = mock(Session.class);
        final ChannelExec mockChannelExec = mock(ChannelExec.class);
        final byte [] bytes = {0};
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(mockChannelExec.getInputStream()).thenReturn(new TestInputStream());
        when(mockChannelExec.getOutputStream()).thenReturn(out);
        when(mockSession.openChannel(eq("exec"))).thenReturn(mockChannelExec);
        when(mockJsch.getSession(anyString(), anyString(), anyInt())).thenReturn(mockSession);
        when(mockJschBuilder.build()).thenReturn(mockJsch);
        when(Config.mockSshConfig.getJschBuilder()).thenReturn(mockJschBuilder);
        when(Config.mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(mock(RemoteCommandReturnInfo.class));
        final String source = BinaryDistributionControlServiceImplTest.class.getClassLoader().getResource("binarydistribution/copy.txt").getPath();
        binaryDistributionControlService.secureCopyFile("someHost", source, "./build/tmp");
        verify(Config.mockSshConfig).getJschBuilder();
        assertEquals("C0644 12 copy.txt\nsome content\0", out.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void testCreateDirectory() {
        when(Config.mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(mock(RemoteCommandReturnInfo.class));
        binaryDistributionControlService.createDirectory("localhost", "/build/tmp");
        verify(Config.mockRemoteCommandExecutorService).executeCommand(any(RemoteExecCommand.class));
    }

    @Test
    public void testCheckFileExists() {
        when(Config.mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(mock(RemoteCommandReturnInfo.class));
        binaryDistributionControlService.checkFileExists("localhost", "/build/tmp");
        verify(Config.mockRemoteCommandExecutorService).executeCommand(any(RemoteExecCommand.class));
    }

    @Test
    public void testUnzipBinary() {
        when(Config.mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(mock(RemoteCommandReturnInfo.class));
        binaryDistributionControlService.unzipBinary("localhost", "file.zip", "dest", "exclude");
        verify(Config.mockRemoteCommandExecutorService).executeCommand(any(RemoteExecCommand.class));
    }

    @Test
    public void testDeleteBinary() {
        when(Config.mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(mock(RemoteCommandReturnInfo.class));
        binaryDistributionControlService.deleteBinary("localhost", "/build/tmp");
        verify(Config.mockRemoteCommandExecutorService).executeCommand(any(RemoteExecCommand.class));
    }

    @Configuration
    static class Config {

        @Mock
        static SshConfiguration mockSshConfiguration;

        @Mock
        static SshConfig mockSshConfig;

        @Mock
        static RemoteCommandExecutorService mockRemoteCommandExecutorService;

        public Config() {
            initMocks(this);
        }

        @Bean
        public SshConfiguration getSshConfiguration() {
            return mockSshConfiguration;
        }

        @Bean
        public SshConfig getSshConfig() {
            return mockSshConfig;
        }

        @Bean
        public RemoteCommandExecutorService getRemoteCommandExecutorService() {
            return mockRemoteCommandExecutorService;
        }

        @Bean
        @Scope("prototype")
        public BinaryDistributionControlService getBinaryDistributionControlService() {
            return new BinaryDistributionControlServiceImpl();
        }

    }

    static class TestInputStream extends InputStream {

        private int available = 1;

        @Override
        public int available() throws IOException {
            if (available == 0) {
                available = 1;
            }
            return available;
        }

        @Override
        public int read() throws IOException {
            available = 0;
            return 0;
        }

    }

}
