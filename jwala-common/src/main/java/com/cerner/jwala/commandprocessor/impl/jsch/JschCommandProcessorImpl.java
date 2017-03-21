package com.cerner.jwala.commandprocessor.impl.jsch;

import static com.cerner.jwala.common.properties.PropertyKeys.*;

import com.cerner.jwala.commandprocessor.CommandProcessor;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.exec.RemoteExecCommand;
import com.cerner.jwala.common.jsch.JschService;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.exception.RemoteCommandFailureException;

import java.io.IOException;

public class JschCommandProcessorImpl implements CommandProcessor {

    private static final String DEFAULT_READ_REMOTE_OUTPUT_TIMEOUT = "180000";

    private final JschService jschService;
    private final RemoteExecCommand remoteExecCommand;
    private ExecReturnCode returnCode;
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
            final int shellReadRemoteOutputTimeout =
                    Integer.parseInt(ApplicationProperties.get(JSCH_SHELL_READ_REMOTE_OUTPUT_TIMEOUT.getPropertyName(),
                                                               DEFAULT_READ_REMOTE_OUTPUT_TIMEOUT));
            remoteCommandReturnInfo = jschService.runShellCommand(remoteExecCommand.getRemoteSystemConnection(),
                    remoteExecCommand.getCommand().toCommandString(), shellReadRemoteOutputTimeout);
        } else {
            final int execReadRemoteOutputTimeout =
                    Integer.parseInt(ApplicationProperties.get(JSCH_EXEC_READ_REMOTE_OUTPUT_TIMEOUT.getPropertyName(),
                                                               DEFAULT_READ_REMOTE_OUTPUT_TIMEOUT));
            remoteCommandReturnInfo = jschService.runExecCommand(remoteExecCommand.getRemoteSystemConnection(),
                    remoteExecCommand.getCommand().toCommandString(), execReadRemoteOutputTimeout);

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
