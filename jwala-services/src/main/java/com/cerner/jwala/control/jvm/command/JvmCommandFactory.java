package com.cerner.jwala.control.jvm.command;

/**
 * Created by Arvindo Kinny on 12/22/2016.
 */


import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;
import com.cerner.jwala.common.domain.model.ssh.DecryptPassword;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.exec.*;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.properties.PropertyKeys;
import com.cerner.jwala.service.RemoteCommandExecutorService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionControlService;
import com.cerner.jwala.service.exception.ApplicationServiceException;
import com.cerner.jwala.service.jvm.exception.JvmServiceException;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.cerner.jwala.control.AemControl.Properties.*;


/**
 * The CommandFactory class.<br/>
 */
@Component
public class JvmCommandFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(JvmCommandFactory.class);

    private HashMap<String, JvmCommand> commands;

    @Autowired
    protected SshConfiguration sshConfig;

    @Autowired
    private RemoteCommandExecutorService remoteCommandExecutorService;

    @Autowired
    private BinaryDistributionControlService binaryDistributionControlService;

    /**
     * @param jvm
     * @param operation
     * @return
     * @throws ApplicationServiceException
     */
    public RemoteCommandReturnInfo executeCommand(Jvm jvm, JvmControlOperation operation) throws ApplicationServiceException {
        if (commands.containsKey(operation.getExternalValue())) {
            return commands.get(operation.getExternalValue()).execute(jvm);
        }
        throw new ApplicationServiceException("JvmCommand not implemented: " + operation.getExternalValue());
    }

    /* Factory pattern */
    @PostConstruct
    public void initJvmCommands() {
        commands = new HashMap<>();
        commands.put(JvmControlOperation.START.getExternalValue(), jvm -> {
            final String startScriptName = START_SCRIPT_NAME.getValue();
            checkExistsAndCopy(jvm, startScriptName);
            return remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(jvm), getShellCommand(startScriptName, jvm)));
        });
        commands.put(JvmControlOperation.STOP.getExternalValue(), jvm -> {
            checkExistsAndCopy(jvm, STOP_SCRIPT_NAME.getValue());
            return remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(jvm), getShellCommandForStopService(jvm)));
        });
        commands.put(JvmControlOperation.THREAD_DUMP.getExternalValue(), jvm -> {
            final String threadDumpScriptName = THREAD_DUMP_SCRIPT_NAME.getValue();
            checkExistsAndCopy(jvm, threadDumpScriptName);
            return remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(jvm), getExecCommandForThreadDump(threadDumpScriptName, jvm)));
        });
        commands.put(JvmControlOperation.HEAP_DUMP.getExternalValue(), jvm -> {
            final String heapDumpScriptName = HEAP_DUMP_SCRIPT_NAME.getValue();
            checkExistsAndCopy(jvm, heapDumpScriptName);
            return remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(jvm), getExecCommandForHeapDump(heapDumpScriptName, jvm)));
        });
        commands.put(JvmControlOperation.DEPLOY_JVM_ARCHIVE.getExternalValue(), jvm -> {
            checkExistsAndCopy(jvm, DEPLOY_CONFIG_ARCHIVE_SCRIPT_NAME.getValue());
            return remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(jvm), getExecCommandForDeploy(jvm)));
        });
        commands.put(JvmControlOperation.INSTALL_SERVICE.getExternalValue(), jvm -> {
            checkExistsAndCopy(jvm, INSTALL_SERVICE_SCRIPT_NAME.getValue());
            return remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(jvm), getExecCommandForInstallService(jvm)));
        });
        commands.put(JvmControlOperation.DELETE_SERVICE.getExternalValue(), jvm -> {
            checkExistsAndCopy(jvm, DELETE_SERVICE_SCRIPT_NAME.getValue());
            return remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(jvm), getExecCommandForDeleteService(jvm)));
        });
        commands.put(JvmControlOperation.CHECK_SERVICE_STATUS.getExternalValue(), jvm -> {
            checkExistsAndCopy(jvm, SERVICE_STATUS_SCRIPT_NAME.getValue());
            return remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(jvm), getExecCommandForCheckServiceStatus(jvm)));
        });

    }

    private void checkExistsAndCopy(Jvm jvm, String scriptName) {
        final String destAbsolutePath = getFullPathScript(jvm, scriptName);
        CommandOutput fileExistsResult = binaryDistributionControlService.checkFileExists(jvm.getHostName(), destAbsolutePath);
        if (!fileExistsResult.getReturnCode().wasSuccessful()) {
            copyScriptToRemoteDestination(jvm, scriptName, destAbsolutePath);
        } else {
            LOGGER.info("{} already exists. Continue with script execution", scriptName);
        }
    }

    private void copyScriptToRemoteDestination(Jvm jvm, String scriptName, String destAbsolutePath) {
        LOGGER.info("{} does not exist at remote location. Performing secure copy.", scriptName);

        // Don't use java.io.File here to get the parent directory from getFullPathScript - we need to use the
        // path derived from the method in order to support deploying JVMs across platforms (i.e. from a
        // Windows deployed jwala to a Linux remote host and vice versa).
        // So don't pass the script name here to just get the path of the parent directory
        final String destDir = getFullPathScript(jvm, "");
        CommandOutput createDirResult = binaryDistributionControlService.createDirectory(jvm.getHostName(), destDir);
        if (!createDirResult.getReturnCode().wasSuccessful()) {
            LOGGER.error("Failed to create directory {}", destDir);
            throw new JvmServiceException("Failed to create directory " + destDir);
        }

        CommandOutput copyResult = binaryDistributionControlService.secureCopyFile(jvm.getHostName(), ApplicationProperties.getRequired("commands.scripts-path") + "/" + scriptName, destAbsolutePath);
        if (copyResult.getReturnCode().wasSuccessful()) {
            LOGGER.info("Secure copy success to {}", destAbsolutePath);
        } else {
            LOGGER.error("Failed to secure copy {}", destAbsolutePath);
            throw new JvmServiceException("Failed to secure copy " + destAbsolutePath);
        }

        CommandOutput fileModeResult = binaryDistributionControlService.changeFileMode(jvm.getHostName(), "a+x", destDir, "*.sh");
        if (!fileModeResult.getReturnCode().wasSuccessful()) {
            LOGGER.error("Failed to make the files executable in {}", destDir);
            throw new JvmServiceException("Failed to make the files executable in " + destDir);
        }
    }

    /**
     * @param jvm
     * @return
     */
    private RemoteSystemConnection getConnection(Jvm jvm) {
        return new RemoteSystemConnection(sshConfig.getUserName(), sshConfig.getEncryptedPassword(), jvm.getHostName(), sshConfig.getPort());
    }

    /**
     * Get
     *
     * @param jvm
     * @param scriptName
     * @return
     */
    private String getFullPathScript(Jvm jvm, String scriptName) {
        return ApplicationProperties.getRequired(PropertyKeys.REMOTE_SCRIPT_DIR) + "/" + jvm.getJvmName() + "/" + scriptName;
    }

    /**
     * Generate parameters for JVM Heap dump
     *
     * @param scriptName
     * @param jvm
     * @return
     */
    private ExecCommand getExecCommandForHeapDump(String scriptName, Jvm jvm) {
        final String trimmedJvmName = StringUtils.deleteWhitespace(jvm.getJvmName());

        final String jvmRootDir = Paths.get(jvm.getTomcatMedia().getRemoteDir().toString() + '/' + trimmedJvmName + '/' +
                jvm.getTomcatMedia().getMediaDir()).normalize().toString();

        final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd.HHmmss");
        final String dumpFile = "heapDump." + trimmedJvmName + "." + formatter.print(DateTime.now());

        final String dumpLiveStr = ApplicationProperties.getAsBoolean(PropertyKeys.JMAP_DUMP_LIVE_ENABLED.name()) ? "live," : "\"\"";

        return new ExecCommand(getFullPathScript(jvm, scriptName), jvm.getJavaHome(),
                ApplicationProperties.get(PropertyKeys.REMOTE_JAWALA_DATA_DIR), dumpFile, dumpLiveStr, jvmRootDir,
                jvm.getJvmName());
    }

    /**
     * Generate parameters for Thread dump
     *
     * @param scriptName
     * @param jvm
     * @return
     */
    private ExecCommand getExecCommandForThreadDump(String scriptName, Jvm jvm) {
        final String jvmRootDir = Paths.get(jvm.getTomcatMedia().getRemoteDir().toString() + '/' +
                StringUtils.deleteWhitespace(jvm.getJvmName()) + '/' + jvm.getTomcatMedia().getMediaDir())
                .normalize().toString();
        return new ExecCommand(getFullPathScript(jvm, scriptName), jvm.getJavaHome(), jvmRootDir, jvm.getJvmName());
    }

    /**
     * @param scriptName
     * @param jvm
     * @return
     */
    private ExecCommand getShellCommand(String scriptName, Jvm jvm) {
        return new ShellCommand(getFullPathScript(jvm, scriptName), jvm.getJvmName());
    }

    /**
     * Method to generate remote command for extracting jar for jvm
     *
     * @param jvm
     * @return
     */
    private ExecCommand getExecCommandForDeploy(Jvm jvm) {
        final String remoteScriptDir = ApplicationProperties.getRequired(PropertyKeys.REMOTE_SCRIPT_DIR);

        final String trimmedJvmName = StringUtils.deleteWhitespace(jvm.getJvmName());
        final String archivePath = Paths.get(remoteScriptDir + '/' + trimmedJvmName + '/'
                + DEPLOY_CONFIG_ARCHIVE_SCRIPT_NAME).normalize().toString();
        final String jvmJarFile = Paths.get(remoteScriptDir + '/' + trimmedJvmName + ".jar").normalize().toString();
        final String jvmInstanceDir = Paths.get(jvm.getTomcatMedia().getRemoteDir().toString() + '/' + trimmedJvmName)
                .normalize().toString();
        final String jdkJarDir = Paths.get(jvm.getJavaHome() + "/bin/jar").normalize().toString();

        return new ExecCommand(archivePath, jvmJarFile, jvmInstanceDir, jdkJarDir);
    }

    /**
     * Method to generate remote command for installing service for jvm
     *
     * @param jvm
     * @return
     */
    private ExecCommand getExecCommandForInstallService(Jvm jvm) {
        final String userName;
        final String encryptedPassword;

        if (jvm.getUserName() != null) {
            userName = jvm.getUserName();
            encryptedPassword = jvm.getEncryptedPassword();
        } else {
            userName = null;
            encryptedPassword = null;
        }

        final String quotedUsername;
        if (userName != null && userName.length() > 0) {
            quotedUsername = "\"" + userName + "\"";
        } else {
            quotedUsername = "";
        }
        final String decryptedPassword = encryptedPassword != null && encryptedPassword.length() > 0 ? new DecryptPassword().decrypt(encryptedPassword) : "";

        List<String> formatStrings = Arrays.asList(getFullPathScript(jvm, INSTALL_SERVICE_SCRIPT_NAME.getValue()),
                jvm.getJvmName(), jvm.getTomcatMedia().getRemoteDir().normalize().toString(),
                jvm.getTomcatMedia().getMediaDir().toString());
        List<String> unformatStrings = Arrays.asList(quotedUsername, decryptedPassword);

        return new ExecCommand(formatStrings, unformatStrings);
    }

    private ExecCommand getExecCommandForDeleteService(Jvm jvm) {
        //copy delete script
        return new ExecCommand(ApplicationProperties.getRequired(PropertyKeys.REMOTE_SCRIPT_DIR) +
                "/" + jvm.getJvmName() + "/" +
                DELETE_SERVICE_SCRIPT_NAME.getValue(), jvm.getJvmName());
    }

    private ExecCommand getExecCommandForCheckServiceStatus(Jvm jvm) {
        return new ExecCommand(getFullPathScript(jvm, SERVICE_STATUS_SCRIPT_NAME.getValue()), jvm.getJvmName());
    }

    private ExecCommand getShellCommandForStopService(Jvm jvm) {
        return new ShellCommand(getFullPathScript(jvm, STOP_SCRIPT_NAME.getValue()), jvm.getJvmName(), SLEEP_TIME.getValue());
    }
}
