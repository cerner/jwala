package com.cerner.jwala.service.host.impl;

import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.exec.*;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.cerner.jwala.service.RemoteCommandExecutorService;
import com.cerner.jwala.service.host.HostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Arvindo Kinny on 12/13/2016.
 */
@Service
public class HostServiceImpl implements HostService {

    @Autowired
    private SshConfiguration sshConfig;
    @Autowired
    private RemoteCommandExecutorService remoteCommandExecutorService;

    private static final Logger LOGGER = LoggerFactory.getLogger(HostServiceImpl.class);

    @Override
    /**
     *
     */
    public String getUName(String hostName) {
        final ExecCommand execCommand = new ShellCommand("uname");
        final RemoteExecCommand remoteExecCommand = new RemoteExecCommand(new RemoteSystemConnection(sshConfig.getUserName(),
                sshConfig.getEncryptedPassword(), hostName, sshConfig.getPort()), execCommand);
        RemoteCommandReturnInfo remoteCommandReturnInfo = remoteCommandExecutorService.executeCommand(remoteExecCommand);

        CommandOutput commandOutput = new CommandOutput(new ExecReturnCode(remoteCommandReturnInfo.retCode),
                remoteCommandReturnInfo.standardOuput, remoteCommandReturnInfo.errorOupout);

        final String standardOutput = commandOutput.getStandardOutput();
        final ExecReturnCode returnCode = commandOutput.getReturnCode();
        LOGGER.info("Host OS: {}", standardOutput);
        return standardOutput;
    }

    public SshConfiguration getSshConfig() {
        return sshConfig;
    }

    public HostServiceImpl setSshConfig(SshConfiguration sshConfig) {
        this.sshConfig = sshConfig;
        return this;
    }

    public RemoteCommandExecutorService getRemoteCommandExecutorService() {
        return remoteCommandExecutorService;
    }

    public HostServiceImpl setRemoteCommandExecutorService(RemoteCommandExecutorService remoteCommandExecutorService) {
        this.remoteCommandExecutorService = remoteCommandExecutorService;
        return this;
    }
}
