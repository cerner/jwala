package com.cerner.jwala.commandprocessor;

import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.exception.RemoteCommandFailureException;

import java.io.Closeable;

public interface CommandProcessor extends Closeable {

    ExecReturnCode getExecutionReturnCode();

    void processCommand() throws RemoteCommandFailureException;

    String getCommandOutputStr();

    String getErrorOutputStr();

}
