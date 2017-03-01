package com.cerner.jwala.service.impl.spring.component;

import com.cerner.jwala.common.exec.ExecCommand;
import com.cerner.jwala.common.exec.RemoteExecCommand;
import com.cerner.jwala.common.exec.RemoteSystemConnection;
import com.cerner.jwala.common.jsch.JschService;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link JschRemoteCommandExecutorServiceImpl}
 *
 * Created by Jedd Cuison on 4/18/2016
 */
public class JschRemoteCommandExecutorServiceImplTest {

    private static final String SOME_OUTPUT = "some output";

    private JschRemoteCommandExecutorServiceImpl jschRemoteCommandExecutorService;

    @Mock
    private ExecCommand mockExecCommand;

    @Mock
    private RemoteExecCommand mockRemoteExecCommand;

    @Mock
    private JschService mockJschService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.jschRemoteCommandExecutorService = new JschRemoteCommandExecutorServiceImpl(mockJschService);
    }

    @Test
    public void testExecuteShellCommand() throws Exception {
        when(mockExecCommand.getRunInShell()).thenReturn(true);
        when(mockExecCommand.toCommandString()).thenReturn("some-command.sh");
        when(mockRemoteExecCommand.getCommand()).thenReturn(mockExecCommand);
        when(mockJschService.runShellCommand(any(RemoteSystemConnection.class), eq("some-command.sh"), anyLong()))
                .thenReturn(new RemoteCommandReturnInfo(0, "EXIT_CODE=0*** ", null));
        final RemoteCommandReturnInfo remoteCommandReturnInfo = jschRemoteCommandExecutorService
                .executeCommand(mockRemoteExecCommand);
        assertEquals(0, remoteCommandReturnInfo.retCode);
    }

    @Test
    public void testExecuteExecCommand() throws Exception {
        when(mockExecCommand.getRunInShell()).thenReturn(false);
        when(mockExecCommand.toCommandString()).thenReturn("sc query something");
        when(mockRemoteExecCommand.getCommand()).thenReturn(mockExecCommand);
        when(mockJschService.runExecCommand(any(RemoteSystemConnection.class), eq("sc query something"), anyLong()))
                .thenReturn(new RemoteCommandReturnInfo(0, SOME_OUTPUT, null));
        final RemoteCommandReturnInfo returnInfo = this.jschRemoteCommandExecutorService.executeCommand(mockRemoteExecCommand);
        assertEquals("some output", returnInfo.standardOuput);
    }

}
