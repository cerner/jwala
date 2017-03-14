package com.cerner.jwala.control.webserver.command;

/**
 * Created by Arvindo Kinny on 12/22/2016.
 */


import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerControlOperation;
import com.cerner.jwala.common.exec.*;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.properties.PropertyKeys;
import com.cerner.jwala.service.RemoteCommandExecutorService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionControlService;
import com.cerner.jwala.service.exception.ApplicationServiceException;
import com.cerner.jwala.service.webserver.exception.WebServerServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.HashMap;

import static com.cerner.jwala.control.AemControl.Properties.*;


/**
 * The WebServerCommandFactory class.<br/>
 */
@Component
public class WebServerCommandFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebServerCommandFactory.class);

    private HashMap<String, WebServerCommand> commands;

    @Autowired
    protected SshConfiguration sshConfig;

    @Autowired
    protected RemoteCommandExecutorService remoteCommandExecutorService;

    @Autowired
    protected BinaryDistributionControlService binaryDistributionControlService;

    /**
     * @param webserver
     * @param operation
     * @return
     * @throws ApplicationServiceException
     */
    public RemoteCommandReturnInfo executeCommand(WebServer webserver, WebServerControlOperation operation) throws ApplicationServiceException {
        if (commands.containsKey(operation.getExternalValue())) {
            return commands.get(operation.getExternalValue()).apply(webserver);
        }
        throw new ApplicationServiceException("WebServerCommand  not found: " + operation.getExternalValue());
    }

    /**
     *
     */
    public void listCommands() {
        LOGGER.debug("Available webserver commands");
        for (String command : commands.keySet()) {
            LOGGER.debug(command);
        }
    }

    @PostConstruct
    public void initWebServerCommands() {
        commands = new HashMap<>();
        // commands are added here using lambdas. It is also possible to dynamically add commands without editing the code.
        commands.put(WebServerControlOperation.START.getExternalValue(), webServer -> {
            final String startScriptName = START_SCRIPT_NAME.getValue();
            checkExistsAndCopy(webServer, startScriptName);
            return remoteCommandExecutorService.executeCommand(
                    new RemoteExecCommand(getConnection(webServer), getShellCommand(startScriptName,
                            webServer,
                            webServer.getName())));
        });


        commands.put(WebServerControlOperation.STOP.getExternalValue(), webServer -> {
            final String stopScriptName = STOP_SCRIPT_NAME.getValue();
            checkExistsAndCopy(webServer, stopScriptName);
            return remoteCommandExecutorService.executeCommand(
                    new RemoteExecCommand(getConnection(webServer), getShellCommand(stopScriptName,
                            webServer,
                            webServer.getName())));
        });

        commands.put(WebServerControlOperation.DELETE_SERVICE.getExternalValue(), webServer -> {
            final String deleteServiceScriptName = DELETE_SERVICE_SCRIPT_NAME.getValue();
            checkExistsAndCopy(webServer, deleteServiceScriptName);
            return remoteCommandExecutorService.executeCommand(
                    new RemoteExecCommand(getConnection(webServer), getExecCommand(deleteServiceScriptName,
                            webServer)));
        });

        commands.put(WebServerControlOperation.INSTALL_SERVICE.getExternalValue(), webServer -> {
            final String installServiceWsScriptName = INSTALL_WS_SERVICE_SCRIPT_NAME.getValue();
            checkExistsAndCopy(webServer, installServiceWsScriptName);
            return remoteCommandExecutorService.executeCommand(
                    new RemoteExecCommand(getConnection(webServer),
                            getShellCommand(installServiceWsScriptName,
                                    webServer,
                                    ApplicationProperties.getRequired(PropertyKeys.REMOTE_PATHS_APACHE_HTTPD_CONF),
                                    ApplicationProperties.getRequired(PropertyKeys.REMOTE_PATHS_APACHE_HTTPD))));
        });

        commands.put(WebServerControlOperation.VIEW_HTTP_CONFIG_FILE.getExternalValue(), (WebServer webServer)
                -> remoteCommandExecutorService.executeCommand(
                new RemoteExecCommand(getConnection(webServer),
                        new ExecCommand("cat", ApplicationProperties.getRequired(PropertyKeys.REMOTE_PATHS_APACHE_HTTPD_CONF) + "/httpd.conf"))));

    }

    private void checkExistsAndCopy(WebServer webserver, String scriptName) {
        final String destAbsolutePath = getFullPathScript(scriptName, webserver.getName());
        CommandOutput fileExistsResult = binaryDistributionControlService.checkFileExists(webserver.getHost(), destAbsolutePath);
        if (!fileExistsResult.getReturnCode().wasSuccessful()) {
            copyScriptToRemoteDestination(webserver, scriptName, destAbsolutePath);
        } else {
            LOGGER.info("{} already exists. Continue with script execution", scriptName);
        }
    }

    private void copyScriptToRemoteDestination(WebServer webserver, String scriptName, String destAbsolutePath) {
        LOGGER.info("{} does not exist at remote location. Performing secure copy.", scriptName);

        final String destDir = new File(getFullPathScript(scriptName, webserver.getName())).getParent();
        CommandOutput createDirResult = binaryDistributionControlService.createDirectory(webserver.getHost(), destDir);
        if (!createDirResult.getReturnCode().wasSuccessful()) {
            LOGGER.error("Failed to create directory {}", destDir);
            throw new WebServerServiceException("Failed to create directory " + destDir);
        }

        CommandOutput copyResult = binaryDistributionControlService.secureCopyFile(webserver.getHost(), ApplicationProperties.getRequired("commands.scripts-path") + "/" + scriptName, destAbsolutePath);
        if (copyResult.getReturnCode().wasSuccessful()) {
            LOGGER.info("Secure copy success to {}", destAbsolutePath);
        } else {
            LOGGER.error("Failed to secure copy {}", destAbsolutePath);
            throw new WebServerServiceException("Failed to secure copy " + destAbsolutePath);
        }

        CommandOutput fileModeResult = binaryDistributionControlService.changeFileMode(webserver.getHost(), "a+x", destDir, "*.sh");
        if (!fileModeResult.getReturnCode().wasSuccessful()) {
            LOGGER.error("Failed to make the files executable in {}", destDir);
            throw new WebServerServiceException("Failed to make the files executable in " + destDir);
        }
    }

    /**
     * @param webserver
     * @return
     */
    private RemoteSystemConnection getConnection(WebServer webserver) {
        return new RemoteSystemConnection(sshConfig.getUserName(), sshConfig.getEncryptedPassword(), webserver.getHost(), sshConfig.getPort());
    }

    /**
     * Get
     *
     * @param scriptName
     * @param webServerName
     * @return
     */
    private String getFullPathScript(String scriptName, String webServerName) {
        return ApplicationProperties.getRequired(PropertyKeys.REMOTE_SCRIPT_DIR) + "/" + webServerName + "/" + scriptName;
    }

    /**
     * @param scriptName
     * @param webserver
     * @return
     */
    private ExecCommand getExecCommand(String scriptName, WebServer webserver, String... params) {
        String paramConcat = "";
        for (String param : params) {
            paramConcat += param + " ";
        }

        return new ExecCommand(getFullPathScript(scriptName, webserver.getName()), webserver.getName(), paramConcat);
    }

    /**
     * @param scriptName
     * @param webserver
     * @return
     */
    private ExecCommand getShellCommand(String scriptName, WebServer webserver, String... params) {
        String paramConcat = "";
        for (String param : params) {
            paramConcat += param + " ";
        }

        return new ShellCommand(getFullPathScript(scriptName, webserver.getName()), webserver.getName(), paramConcat);
    }
}
