package com.cerner.jwala.commandprocessor.impl.jsch;

import com.cerner.jwala.commandprocessor.impl.CommonSshTestConfiguration;
import com.cerner.jwala.common.exec.ExecCommand;
import com.cerner.jwala.common.exec.RemoteExecCommand;
import com.cerner.jwala.common.exec.RemoteSystemConnection;
import com.cerner.jwala.exception.ExitCodeNotAvailableException;
import com.cerner.jwala.exception.RemoteCommandFailureException;
import com.jcraft.jsch.JSchException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
// TODO: Make ssh server should be self contained or permanent. The server that this test connects to changes from time to time thus it fails on occasions.
public class JschRequestProcessorImplTest {


    private JschBuilder builder;
    private RemoteSystemConnection remoteSystemConnection;

    @Before
    public void setup() throws JSchException {
        final CommonSshTestConfiguration config = new CommonSshTestConfiguration();
        builder = config.getBuilder();
        remoteSystemConnection = config.getRemoteSystemConnection();
    }

    @Test(expected = ExitCodeNotAvailableException.class)
    public void testGetReturnCodeBeforeFinishing() throws Exception {
        final RemoteExecCommand remoteExecCommand = new RemoteExecCommand(remoteSystemConnection, new ExecCommand("vi"));
/*
        final JschCommandProcessorImpl sshProcessor = new JschCommandProcessorImpl(builder.build(), remoteExecCommand,
                null);

        sshProcessor.processCommand();
        final ExecReturnCode returnCode = sshProcessor.getExecutionReturnCode();
*/
    }

    @Test(expected = RemoteCommandFailureException.class)
    public void testBadRemoteCommand() throws Exception {
        final RemoteExecCommand remoteExecCommand =
                new RemoteExecCommand(new RemoteSystemConnection("abc", "==encrypted123546==".toCharArray(), "example.com", 123456), new ExecCommand("vi"));
 /*       final JschCommandProcessorImpl jschCommandProcessor = new JschCommandProcessorImpl(builder.build(), remoteExecCommand,
                null);
        jschCommandProcessor.processCommand();
 */   }

}
