package com.cerner.jwala.service.impl.spring.component;

import static com.cerner.jwala.common.properties.PropertyKeys.*;

import com.cerner.jwala.common.exec.RemoteExecCommand;
import com.cerner.jwala.common.jsch.JschService;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.cerner.jwala.common.properties.ApplicationProperties;
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

    private static final String DEFAULT_READ_REMOTE_OUTPUT_TIMEOUT = "180000";

    private final JschService jschService;

    @Autowired
    public JschRemoteCommandExecutorServiceImpl(final JschService jschService) {
        this.jschService = jschService;
    }

    @Override
    public RemoteCommandReturnInfo executeCommand(final RemoteExecCommand remoteExecCommand) {
        if (remoteExecCommand.getCommand().getRunInShell()) {
            final int shellReadRemoteOutputTimeout =
                    Integer.parseInt(ApplicationProperties.get(JSCH_SHELL_READ_REMOTE_OUTPUT_TIMEOUT.getPropertyName(),
                            DEFAULT_READ_REMOTE_OUTPUT_TIMEOUT));
            return jschService.runShellCommand(remoteExecCommand.getRemoteSystemConnection(),
                    remoteExecCommand.getCommand().toCommandString(), shellReadRemoteOutputTimeout);
        }
        final int execReadRemoteOutputTimeout =
                Integer.parseInt(ApplicationProperties.get(JSCH_EXEC_READ_REMOTE_OUTPUT_TIMEOUT.getPropertyName(),
                        DEFAULT_READ_REMOTE_OUTPUT_TIMEOUT));
        return jschService.runExecCommand(remoteExecCommand.getRemoteSystemConnection(),
                remoteExecCommand.getCommand().toCommandString(), execReadRemoteOutputTimeout);
    }

}
