package com.cerner.jwala.service.binarydistribution.impl;

import com.cerner.jwala.commandprocessor.impl.jsch.JschScpCommandProcessorImpl;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.exception.ApplicationException;
import com.cerner.jwala.common.exec.*;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.properties.PropertyKeys;
import com.cerner.jwala.control.configuration.SshConfig;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.service.RemoteCommandExecutorService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

import static com.cerner.jwala.control.AemControl.Properties.UNZIP_SCRIPT_NAME;

/**
 * Created by Arvindo Kinny on 10/11/2016.
 */
public class BinaryDistributionControlServiceImpl implements BinaryDistributionControlService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BinaryDistributionControlServiceImpl.class);

    @Autowired
    private  SshConfiguration sshConfiguration;

    @Autowired
    private SshConfig sshConfig;

    @Autowired
    private RemoteCommandExecutorService remoteCommandExecutorService;

    private static String CREATE_DIR="if [ ! -e \"%s\" ]; then mkdir -p %s; fi;";
    private static String REMOVE="rm";
    private static String SECURE_COPY = "scp";
    private static String TEST = "test -e";
    private static String CHMOD = "chmod";
    private static String MOVE = "mv";
    private static String COPY = "cp";

    @Override
    public CommandOutput secureCopyFile(final String hostname, final String source, final String destination) throws CommandFailureException  {
        RemoteExecCommand command = new RemoteExecCommand(getConnection(hostname),  new ExecCommand(SECURE_COPY, source, destination));
        try {
            final JschScpCommandProcessorImpl jschScpCommandProcessor = new JschScpCommandProcessorImpl(sshConfig.getJschBuilder().build(), command);
            jschScpCommandProcessor.processCommand();
            jschScpCommandProcessor.close();
            return  new CommandOutput(new ExecReturnCode(jschScpCommandProcessor.getExecutionReturnCode().getReturnCode()),
                    jschScpCommandProcessor.getCommandOutputStr(), jschScpCommandProcessor.getErrorOutputStr());
        } catch (Exception ex) {
            throw new ApplicationException(ex);
        }
    }

    @Override
    public CommandOutput createDirectory(final String hostname, final String destination) throws CommandFailureException {
        ExecCommand command = new ExecCommand(String.format(CREATE_DIR, destination, destination));
        RemoteCommandReturnInfo remoteCommandReturnInfo = remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(hostname), command  ));
        CommandOutput commandOutput = new CommandOutput(new ExecReturnCode(remoteCommandReturnInfo.retCode),
                remoteCommandReturnInfo.standardOuput, remoteCommandReturnInfo.errorOupout);
        return commandOutput;
    }

    @Override
    public CommandOutput checkFileExists(final String hostname, final String destination) throws CommandFailureException {
        RemoteCommandReturnInfo remoteCommandReturnInfo = remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(hostname),  new ExecCommand(TEST, destination)));
        CommandOutput commandOutput = new CommandOutput(new ExecReturnCode(remoteCommandReturnInfo.retCode),
                remoteCommandReturnInfo.standardOuput, remoteCommandReturnInfo.errorOupout);
        return commandOutput;
    }

    @Override
    public CommandOutput unzipBinary(final String hostname, final String zipPath, final String destination, final String exclude) throws CommandFailureException {
        String remoteUnZipScriptPath = ApplicationProperties.getRequired(PropertyKeys.REMOTE_SCRIPT_DIR) + "/" + UNZIP_SCRIPT_NAME;
        RemoteCommandReturnInfo remoteCommandReturnInfo = remoteCommandExecutorService.executeCommand(
                                                          new RemoteExecCommand(getConnection(hostname),
                                                                  new ExecCommand(remoteUnZipScriptPath, zipPath, destination, exclude)));
        CommandOutput commandOutput = new CommandOutput(new ExecReturnCode(remoteCommandReturnInfo.retCode),
                remoteCommandReturnInfo.standardOuput, remoteCommandReturnInfo.errorOupout);
        return commandOutput;
    }

    @Override
    public CommandOutput deleteBinary(final String hostname, final String destination) throws CommandFailureException {
        RemoteCommandReturnInfo remoteCommandReturnInfo = remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(hostname),  new ExecCommand(REMOVE, destination)));
        CommandOutput commandOutput = new CommandOutput(new ExecReturnCode(remoteCommandReturnInfo.retCode),
                remoteCommandReturnInfo.standardOuput, remoteCommandReturnInfo.errorOupout);
        return commandOutput;
    }

    @Override
    public CommandOutput changeFileMode(String hostname, String mode, String targetDir, String target) throws CommandFailureException {
        RemoteCommandReturnInfo remoteCommandReturnInfo = remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(hostname),  new ExecCommand(CHMOD, mode, targetDir+"/"+target )));
        CommandOutput commandOutput = new CommandOutput(new ExecReturnCode(remoteCommandReturnInfo.retCode),
                remoteCommandReturnInfo.standardOuput, remoteCommandReturnInfo.errorOupout);
        return commandOutput;
    }

    @Override
    public CommandOutput backupFileWithCopy(String hostname, String remotePath) throws CommandFailureException {
        return executeBackup(hostname, remotePath, COPY);
    }

    @Override
    public CommandOutput backupFileWithMove(final String hostname, final String remotePath) throws CommandFailureException {
        return executeBackup(hostname, remotePath, MOVE);
    }

    private CommandOutput executeBackup(final String hostname, final String remotePath, String command) {
        final String currentDateSuffix = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Date.from(Instant.now()));
        final String destPathBackup = remotePath + "." + currentDateSuffix;
        RemoteCommandReturnInfo remoteCommandReturnInfo = remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(hostname),  new ExecCommand(command, remotePath, destPathBackup)));
        CommandOutput commandOutput = new CommandOutput(new ExecReturnCode(remoteCommandReturnInfo.retCode),
                remoteCommandReturnInfo.standardOuput, remoteCommandReturnInfo.errorOupout);
        return commandOutput;
    }

    /**
     *
     * @param host
     * @return
     */
    private RemoteSystemConnection getConnection(String host) {
        return new RemoteSystemConnection(sshConfiguration.getUserName(), sshConfiguration.getEncryptedPassword(), host, sshConfiguration.getPort());
    }
}

