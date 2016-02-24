package com.siemens.cto.aem.commandprocessor.impl.jsch;

import com.jcraft.jsch.JSch;
import com.siemens.cto.aem.commandprocessor.CommandProcessor;
import com.siemens.cto.aem.commandprocessor.CommandProcessorBuilder;
import com.siemens.cto.aem.commandprocessor.jsch.JschChannelService;
import com.siemens.cto.aem.common.exec.RemoteExecCommand;
import com.siemens.cto.aem.exception.RemoteCommandFailureException;

public class JschCommandProcessorBuilder implements CommandProcessorBuilder {

    private JSch jsch;
    private RemoteExecCommand remoteCommand;
    private JschChannelService jschChannelService;

    public JschCommandProcessorBuilder() {
    }

    public JschCommandProcessorBuilder setJsch(final JSch aJsch) {
        jsch = aJsch;
        return this;
    }

    public JschCommandProcessorBuilder setRemoteCommand(final RemoteExecCommand aRemoteCommand) {
        remoteCommand = aRemoteCommand;
        return this;
    }

    public JschCommandProcessorBuilder setJschChannelService(final JschChannelService jschChannelService) {
        this.jschChannelService = jschChannelService;
        return this;
    }

    @Override
    public CommandProcessor build() throws RemoteCommandFailureException {
        return new JschCommandProcessorImpl(jsch, remoteCommand, jschChannelService);
    }

}
