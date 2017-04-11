package com.cerner.jwala.control.command.common;

/**
 * Created by Arvindo Kinny on 01/09/2017.
 */


import com.cerner.jwala.commandprocessor.impl.jsch.JschScpCommandProcessorImpl;
import com.cerner.jwala.common.exception.ApplicationException;
import com.cerner.jwala.common.exec.*;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.cerner.jwala.control.configuration.SshConfig;
import com.cerner.jwala.service.RemoteCommandExecutorService;
import com.cerner.jwala.service.exception.ApplicationServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Stream;

/**
 * The CommandFactory class.<br/>
 */
@Component
public class ShellCommandFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShellCommandFactory.class);
    private HashMap<String, RemoteShellCommand> commands;

    @Autowired
    private SshConfig sshConfig;

    @Autowired
    protected RemoteCommandExecutorService remoteCommandExecutorService;

    /**
     *
     * @param host
     * @param operation
     * @param paramaters
     * @return
     * @throws ApplicationServiceException
     */
    public RemoteCommandReturnInfo executeRemoteCommand(String host, Command operation, String... paramaters) throws ApplicationServiceException{
        if (commands.containsKey(operation.name())) {
            return commands.get(operation.name()).apply(host, paramaters);
        }
        throw new ApplicationServiceException("RemoteShellCommand not found");
    }

    @PostConstruct
    public void initApplicationCommands() {
        commands = new HashMap<>();
        // commands are added here using lambdas. It is also possible to dynamically add commands without editing the code.
        commands.put(Command.CHECK_FILE_EXISTS.name(), (String host, String... params)
                -> remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(host),new ExecCommand(String.format(Command.CHECK_FILE_EXISTS.get(), params[0])))));
        commands.put(Command.CHANGE_FILE_MODE.name(), (String host, String... params)
                -> remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(host),new ExecCommand(concatArray(Command.CHANGE_FILE_MODE.get(), params)))));
        commands.put(Command.CREATE_DIR.name(), (String host, String... params)
                -> remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(host),new ExecCommand(String.format(Command.CREATE_DIR.get(), params[0], params[0])))));
        commands.put(Command.MOVE.name(), (String host, String... params)
                -> remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(host),new ExecCommand(String.format(Command.MOVE.get(), params[0], params[1])))));
        commands.put(Command.SCP.name(), (String host, String... params)
                -> executeSCP(host, params[0], params[1]));

    }

    private RemoteSystemConnection getConnection(String host) {
        return new RemoteSystemConnection(sshConfig.getSshConfiguration().getUserName(), sshConfig.getSshConfiguration().getEncryptedPassword(), host, sshConfig.getSshConfiguration().getPort());
    }

    private String[] concatArray(String command, String... parameters){
        return Stream.concat(Arrays.stream(new String[]{command}), Arrays.stream(parameters)).toArray(String[]::new);

    }

    private RemoteCommandReturnInfo executeSCP(String hostname, String source, String destination){
        //TODO Refactor jscp
        try {
            RemoteExecCommand command = new RemoteExecCommand(getConnection(hostname),  new ExecCommand(Command.SCP.get(), source, destination));
            final JschScpCommandProcessorImpl jschScpCommandProcessor = new JschScpCommandProcessorImpl(sshConfig.getJschBuilder().build(), command);
            jschScpCommandProcessor.processCommand();
            return  new RemoteCommandReturnInfo(jschScpCommandProcessor.getExecutionReturnCode().getReturnCode(),
                    jschScpCommandProcessor.getCommandOutputStr(), jschScpCommandProcessor.getErrorOutputStr());
        } catch (Exception ex) {
            throw new ApplicationException(ex);
        }
    }
}
