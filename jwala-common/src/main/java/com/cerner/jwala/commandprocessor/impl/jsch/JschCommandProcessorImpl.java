package com.cerner.jwala.commandprocessor.impl.jsch;

import com.cerner.jwala.commandprocessor.CommandProcessor;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.exec.RemoteExecCommand;
import com.cerner.jwala.common.jsch.JschService;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.cerner.jwala.exception.RemoteCommandFailureException;

import java.io.IOException;

public class JschCommandProcessorImpl implements CommandProcessor {

    private final JschService jschService;
    private final RemoteExecCommand remoteExecCommand;

    private ExecReturnCode returnCode;

    private static final int SHELL_REMOTE_OUTPUT_READ_WAIT_TIME = 180000;
    private static final int EXEC_REMOTE_OUTPUT_READ_WAIT_TIME = 5000;
    private String commandOutputStr;
    private String errorOutputStr;

    public JschCommandProcessorImpl(final RemoteExecCommand remoteExecCommand,
                                    final JschService jschService) {
        this.jschService = jschService;
        this.remoteExecCommand = remoteExecCommand;
    }

    @Override
    public ExecReturnCode getExecutionReturnCode() {
        return returnCode;
    }

    @Override
    public void processCommand() throws RemoteCommandFailureException {
        final RemoteCommandReturnInfo remoteCommandReturnInfo;
        if (remoteExecCommand.getCommand().getRunInShell()) {
            remoteCommandReturnInfo = jschService.runShellCommand(remoteExecCommand.getRemoteSystemConnection(),
                    remoteExecCommand.getCommand().toCommandString(), SHELL_REMOTE_OUTPUT_READ_WAIT_TIME);
        } else {
            remoteCommandReturnInfo = jschService.runExecCommand(remoteExecCommand.getRemoteSystemConnection(),
                    remoteExecCommand.getCommand().toCommandString(), EXEC_REMOTE_OUTPUT_READ_WAIT_TIME);

        }

        returnCode = new ExecReturnCode(remoteCommandReturnInfo.retCode);
        commandOutputStr = remoteCommandReturnInfo.standardOuput;
        errorOutputStr = remoteCommandReturnInfo.errorOupout;
    }

    @Override
    public String getCommandOutputStr() {
        return commandOutputStr;
    }

    @Override
    public String getErrorOutputStr() {
        return errorOutputStr;
    }

    @Override
    public void close() throws IOException {}

}
