package com.cerner.jwala.service.app.impl;

import com.cerner.jwala.commandprocessor.impl.jsch.JschBuilder;
import com.cerner.jwala.commandprocessor.impl.jsch.JschScpCmdProcessorImpl;
import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.exec.*;
import com.cerner.jwala.common.request.app.ControlApplicationRequest;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.service.app.ApplicationCommandService;
import com.jcraft.jsch.JSchException;

/**
 * An implementation of ApplicationCommandService.
 * <p/>
 * Created by Jedd Cuison on 9/9/2015.
 */
public class ApplicationCommandServiceImpl implements ApplicationCommandService {

    private final SshConfiguration sshConfig;
    private JschBuilder jschBuilder;

    public ApplicationCommandServiceImpl(final SshConfiguration sshConfig, JschBuilder jschBuilder) {
        this.sshConfig = sshConfig;
        this.jschBuilder = jschBuilder;
    }

    @Override
    public CommandOutput controlApplication(ControlApplicationRequest applicationRequest, Application app, String... params) throws CommandFailureException {
        RemoteSystemConnection remoteConnection = new RemoteSystemConnection(
                sshConfig.getUserName(),
                sshConfig.getEncryptedPassword(),
                params[0],
                sshConfig.getPort());
        ExecCommand execCommand = new ExecCommand("secure-copy", params[1], params[2]);
        RemoteExecCommand remoteCommand = new RemoteExecCommand(remoteConnection, execCommand);
        try {
            final JschScpCmdProcessorImpl jschScpCommandProcessor = new JschScpCmdProcessorImpl(jschBuilder.build(), remoteCommand);
            jschScpCommandProcessor.processCommand();
            // if processCommand fails it throws an exception before completing
            return new CommandOutput(new ExecReturnCode(0), "", "");
        } catch (JSchException e) {
            throw new CommandFailureException(execCommand, new Throwable(e));
        }
    }
}
