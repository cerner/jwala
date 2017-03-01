package com.cerner.jwala.service.impl.spring.component;

import com.cerner.jwala.common.exec.RemoteExecCommand;
import com.cerner.jwala.common.jsch.JschService;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.cerner.jwala.service.RemoteCommandExecutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link RemoteCommandExecutorService} using JSCH.
 * <p/>
 * Created by Jedd Cuison on 3/25/2016.
 */
@Service
public class JschRemoteCommandExecutorServiceImpl implements RemoteCommandExecutorService {

    private static final int SHELL_REMOTE_OUTPUT_READ_WAIT_TIME = 180000;
    private static final int EXEC_REMOTE_OUTPUT_READ_WAIT_TIME = 3000;

    private final JschService jschService;

    @Autowired
    public JschRemoteCommandExecutorServiceImpl(final JschService jschService) {
        this.jschService = jschService;
    }

    @Override
    public RemoteCommandReturnInfo executeCommand(final RemoteExecCommand remoteExecCommand) {
        if (remoteExecCommand.getCommand().getRunInShell()) {
            return jschService.runShellCommand(remoteExecCommand.getRemoteSystemConnection(),
                    remoteExecCommand.getCommand().toCommandString(), SHELL_REMOTE_OUTPUT_READ_WAIT_TIME);
        }
        return jschService.runExecCommand(remoteExecCommand.getRemoteSystemConnection(),
                remoteExecCommand.getCommand().toCommandString(), EXEC_REMOTE_OUTPUT_READ_WAIT_TIME);
    }

}
