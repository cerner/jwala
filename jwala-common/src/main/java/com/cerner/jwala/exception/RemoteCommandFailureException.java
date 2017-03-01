package com.cerner.jwala.exception;

import com.cerner.jwala.common.exec.RemoteExecCommand;

public class RemoteCommandFailureException extends CommandFailureException {

    private final RemoteExecCommand command;

    public RemoteCommandFailureException(final RemoteExecCommand theCommand,
                                         final Throwable theCause) {
        super(theCommand.getCommand(),
              theCause);
        command = theCommand;
    }

    public RemoteExecCommand getRemoteCommand() {
        return command;
    }
}
