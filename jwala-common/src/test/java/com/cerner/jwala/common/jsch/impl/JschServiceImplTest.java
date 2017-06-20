package com.cerner.jwala.common.jsch.impl;

import com.cerner.jwala.commandprocessor.jsch.impl.ChannelSessionKey;
import com.cerner.jwala.commandprocessor.jsch.impl.ChannelType;
import com.cerner.jwala.common.exec.RemoteSystemConnection;
import com.cerner.jwala.common.jsch.JschService;
import com.cerner.jwala.common.jsch.JschServiceException;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.cerner.jwala.common.scrubber.ScrubberService;
import com.jcraft.jsch.*;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Unit test for {@link JschServiceImpl}
 *
 * Created by Jedd Cuison on 12/26/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {JschServiceImplTest.Config.class})
public class JschServiceImplTest {

    @Autowired
    private JschService jschService;

    @Mock
    private ChannelShell mockChannelShell;

    @Mock
    private ChannelExec mockChannelExec;

    private InputStream in;

    @Mock
    private OutputStream mockOut;

    @Mock
    private RemoteSystemConnection mockRemoteSystemConnection;

    @Mock
    private Session mockSession;

    @Before
    public void setup() {
        initMocks(this);
        in = new ByteArrayInputStream("test".getBytes());
    }

    @Test
    public void testRunCommandUsingChannelShell() throws Exception {
        when(mockChannelShell.getInputStream()).thenReturn(new ByteArrayInputStream("EXIT_CODE=0*** \0xff".getBytes()));
        when(mockChannelShell.getOutputStream()).thenReturn(mockOut);
        when(mockChannelShell.isConnected()).thenReturn(true);
        when(Config.getMockPool().borrowObject(any(ChannelSessionKey.class))).thenReturn(mockChannelShell);
        final RemoteCommandReturnInfo result = jschService.runShellCommand(mockRemoteSystemConnection, "scp", 500);
        verify(mockOut, times(6)).write(any(byte[].class));
        verify(mockOut).flush();
        assertEquals("EXIT_CODE=0*** \0xff", result.standardOuput);
    }

    @Test(expected = JschServiceException.class)
    public void testRunCommandUsingChannelShellAndTimesOut() throws Exception {
        when(mockChannelShell.getInputStream()).thenReturn(new ByteArrayInputStream("output without eof".getBytes()));
        when(mockChannelShell.getOutputStream()).thenReturn(mockOut);
        when(mockChannelShell.isConnected()).thenReturn(true);
        when(Config.getMockPool().borrowObject(any(ChannelSessionKey.class))).thenReturn(mockChannelShell);
        final RemoteCommandReturnInfo result = jschService.runShellCommand(mockRemoteSystemConnection, "scp", 500);
        verify(mockOut, times(6)).write(any(byte[].class));
        verify(mockOut).flush();
        assertEquals("", result.standardOuput);
    }

    @Test
    public void testRunCommandUsingChannelExec() throws IOException, JSchException {
        when(mockChannelExec.getInputStream()).thenReturn(in);
        when(mockChannelExec.getExitStatus()).thenReturn(0);
        when(Config.mockJsch.getSession(anyString(), anyString(), anyInt())).thenReturn(mockSession);
        when(mockSession.openChannel(eq(ChannelType.EXEC.getChannelType()))).thenReturn(mockChannelExec);
        when(mockChannelExec.isClosed()).thenReturn(true);
        jschService.runExecCommand(mockRemoteSystemConnection, "scp", 0);
        verify(mockChannelExec).setCommand(any(byte[].class));
        verify(mockChannelExec).connect(anyInt());
    }

    @Test
    public void testRunCommandUsingChannelExecWithExitStatusNotZero() throws IOException, JSchException {
        when(mockChannelExec.getInputStream()).thenReturn(in);
        when(mockChannelExec.getErrStream()).thenReturn(new ByteArrayInputStream("invalid command".getBytes()));
        when(mockChannelExec.getExitStatus()).thenReturn(1);
        when(Config.mockJsch.getSession(anyString(), anyString(), anyInt())).thenReturn(mockSession);
        when(mockSession.openChannel(eq(ChannelType.EXEC.getChannelType()))).thenReturn(mockChannelExec);
        when(mockChannelExec.isClosed()).thenReturn(true);
        jschService.runExecCommand(mockRemoteSystemConnection, "scp", 0);
        verify(mockChannelExec).setCommand(any(byte[].class));
        verify(mockChannelExec).connect(anyInt());
    }

    @Configuration
    static class Config {

        public static final JSch mockJsch = mock(JSch.class);

        @SuppressWarnings("unchecked")
        public static final GenericKeyedObjectPool<ChannelSessionKey, Channel> mockPool = mock(GenericKeyedObjectPool.class);

        public static final ScrubberService MOCK_SCRUBBER_SERVICE = mock(ScrubberService.class);

        @Bean
        public JSch getMockJsch() {
            return mockJsch;
        }

        @Bean
        public static GenericKeyedObjectPool<ChannelSessionKey, Channel> getMockPool() {
            return mockPool;
        }

        @Bean
        public ScrubberService getScrubberService() {
            return MOCK_SCRUBBER_SERVICE;
        }

        @Bean
        public JschService getJschService() {
            return new JschServiceImpl();
        }

    }

}