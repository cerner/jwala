package com.cerner.jwala.commandprocessor.impl.jsch;

import com.cerner.jwala.common.exec.*;
import com.cerner.jwala.common.jsch.JschService;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.cerner.jwala.exception.RemoteCommandFailureException;
import com.jcraft.jsch.JSchException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JschCommandProcessorImplTest {

    @Mock
    private JschService mockJschService;

    private JschCommandProcessorImpl jschCommandProcessor;

    @Before
    public void setup() throws JSchException {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testProcessCommandExec() throws JSchException, IOException {
        when(mockJschService.runExecCommand(any(RemoteSystemConnection.class), anyString(), anyLong()))
                .thenReturn(new RemoteCommandReturnInfo(0, "std", "err"));
        jschCommandProcessor = new JschCommandProcessorImpl(new RemoteExecCommand(
                new RemoteSystemConnection("testUser", "==encryptedTestPassword==".toCharArray(), "testHost", 1111),
                new ExecCommand("scp ./jwala-services/src/test/resources/known_hosts destpath/testfile.txt".split(" "))),
                                mockJschService);
        try {
            jschCommandProcessor.processCommand();
            ExecReturnCode returnCode = jschCommandProcessor.getExecutionReturnCode();
            assertTrue(returnCode.getWasSuccessful());
        } catch (RemoteCommandFailureException e) {
            assertTrue("This should not fail ... " + e.getMessage(), false);
        }
    }

    @Test
    public void testProcessCommandShell() throws Exception {
        when(mockJschService.runShellCommand(any(RemoteSystemConnection.class), anyString(), anyLong()))
                .thenReturn(new RemoteCommandReturnInfo(0, "std", "err"));
        jschCommandProcessor = new JschCommandProcessorImpl(
                new RemoteExecCommand(new RemoteSystemConnection("testUser", "==encryptedTestPassword==".toCharArray(),
                                      "testHost", 1111),
                new ShellCommand("start", "jvm", "testShellCommand")), mockJschService);
        try {
            jschCommandProcessor.processCommand();
            assertTrue(jschCommandProcessor.getExecutionReturnCode().getWasSuccessful());
        } catch (RemoteCommandFailureException e) {
            assertTrue("This should not fail ... " + e.getMessage(), false);
        }
    }

}