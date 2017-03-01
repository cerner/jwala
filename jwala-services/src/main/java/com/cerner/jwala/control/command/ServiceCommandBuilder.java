package com.cerner.jwala.control.command;

import com.cerner.jwala.common.exec.ExecCommand;

public interface ServiceCommandBuilder {

    ExecCommand buildCommandForService(final String aServiceName, final String...aParams);
}
