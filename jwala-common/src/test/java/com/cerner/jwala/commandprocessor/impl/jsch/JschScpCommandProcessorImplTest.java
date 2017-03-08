package com.cerner.jwala.commandprocessor.impl.jsch;

import com.cerner.jwala.commandprocessor.CommandProcessor;
import com.cerner.jwala.common.exec.ExecCommand;
import com.cerner.jwala.common.exec.RemoteExecCommand;
import com.cerner.jwala.common.exec.RemoteSystemConnection;
import com.cerner.jwala.exception.RemoteCommandFailureException;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class JschScpCommandProcessorImplTest {

    @Mock
    private JSch mockJsch;

    @Mock
    private RemoteExecCommand mockRemoteExecCommand;

    private CommandProcessor jschScpCommandProcessor;

    private static final String PROPERTIES_ROOT_PATH = "PROPERTIES_ROOT_PATH";
    private String resourceDir;

    public JschScpCommandProcessorImplTest() {
        resourceDir = this.getClass().getClassLoader().getResource("vars.properties").getPath();
        resourceDir = resourceDir.substring(0, resourceDir.lastIndexOf("/"));
    }

    @Before
    public void setup() {
        System.setProperty(PROPERTIES_ROOT_PATH, resourceDir);
        initMocks(this);
        jschScpCommandProcessor = new JschScpCommandProcessorImpl(mockJsch, mockRemoteExecCommand);
    }

    @After
    public void tearDown() {
        System.clearProperty(PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testProcessCommand() throws Exception {
        final ExecCommand command = new ExecCommand("frag1", this.getClass().getClassLoader().getResource("jsch-scp.txt").getPath(), "frag3");
        final RemoteSystemConnection mockRemoteSystemConnection = mock(RemoteSystemConnection.class);
        when(mockRemoteExecCommand.getCommand()).thenReturn(command);
        when(mockRemoteExecCommand.getRemoteSystemConnection()).thenReturn(mockRemoteSystemConnection);
        when(mockRemoteSystemConnection.getEncryptedPassword()).thenReturn("#$@%aaa==".toCharArray());
        final Session mockSession = mock(Session.class);
        final ChannelExec mockChannelExec = mock(ChannelExec.class);
        when(mockChannelExec.getOutputStream()).thenReturn(mock(OutputStream.class));
        when(mockChannelExec.getInputStream()).thenReturn(new AckIn());
        when(mockSession.openChannel(eq("exec"))).thenReturn(mockChannelExec);
        when(mockJsch.getSession(anyString(), anyString(), anyInt())).thenReturn(mockSession);
        jschScpCommandProcessor.processCommand();
    }

    @Test(expected = RemoteCommandFailureException.class)
    public void testProcessCommandAckErr() throws Exception {
        final ExecCommand command = new ExecCommand("frag1", this.getClass().getClassLoader().getResource("jsch-scp.txt").getPath(), "frag3");
        final RemoteSystemConnection mockRemoteSystemConnection = mock(RemoteSystemConnection.class);
        when(mockRemoteExecCommand.getCommand()).thenReturn(command);
        when(mockRemoteExecCommand.getRemoteSystemConnection()).thenReturn(mockRemoteSystemConnection);
        when(mockRemoteSystemConnection.getEncryptedPassword()).thenReturn("#$@%aaa==".toCharArray());
        final Session mockSession = mock(Session.class);
        final ChannelExec mockChannelExec = mock(ChannelExec.class);
        final byte [] bytes = {5};
        when(mockChannelExec.getInputStream()).thenReturn(new ByteArrayInputStream(bytes));
        when(mockSession.openChannel(eq("exec"))).thenReturn(mockChannelExec);
        when(mockJsch.getSession(anyString(), anyString(), anyInt())).thenReturn(mockSession);
        jschScpCommandProcessor.processCommand();
    }

    static class AckIn extends InputStream {

        @Override
        public int available() throws IOException {
            return 1;
        }

        @Override
        public int read() throws IOException {
            return 0;
        }

    }

}

