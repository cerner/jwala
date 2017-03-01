package com.cerner.jwala.exception;

import com.cerner.jwala.common.exec.ExecCommand;

public class CommandFailureException extends RuntimeException {

    private final ExecCommand command;

    public CommandFailureException(final ExecCommand aCommand,
                                   final Throwable aCause) {
        super(aCause);
        command = aCommand;
    }

    public ExecCommand getCommand() {
        return command;
    }

}
