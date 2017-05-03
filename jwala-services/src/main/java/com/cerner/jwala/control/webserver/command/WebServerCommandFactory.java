package com.cerner.jwala.control.webserver.command;

/**
 * Created by Arvindo Kinny on 12/22/2016.
 */


import com.cerner.jwala.common.domain.model.resource.ResourceContent;
import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
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
import com.cerner.jwala.service.exception.WebServerCommandFactoryException;
import com.cerner.jwala.service.resource.ResourceContentGeneratorService;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.service.resource.impl.ResourceGeneratorType;
import com.cerner.jwala.service.webserver.exception.WebServerServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.MessageFormat;
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
    private SshConfiguration sshConfig;

    @Autowired
    private RemoteCommandExecutorService remoteCommandExecutorService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private BinaryDistributionControlService binaryDistributionControlService;

    @Autowired
    private ResourceContentGeneratorService resourceContentGeneratorService;

    /**
     * @param webserver the web server target for the command
     * @param operation the operation to be executed
     * @return the result of the executed command
     * @throws WebServerServiceException if the operation is not supported
     */
    public RemoteCommandReturnInfo executeCommand(WebServer webserver, WebServerControlOperation operation) throws ApplicationServiceException {
        if (commands.containsKey(operation.getExternalValue())) {
            return commands.get(operation.getExternalValue()).apply(webserver);
        }
        throw new WebServerServiceException("WebServerCommand  not found: " + operation.getExternalValue());
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

            final String apacheHttpdDir =
                    webServer.getApacheHttpdMedia().getRemoteDir().normalize().toString() + "/" +
                            webServer.getApacheHttpdMedia().getMediaDir().normalize().toString();

            return remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(webServer),
                    getShellCommand(installServiceWsScriptName, webServer, getHttpdConfPath(webServer), apacheHttpdDir)));
        });

        commands.put(WebServerControlOperation.VIEW_HTTP_CONFIG_FILE.getExternalValue(), (WebServer webServer)
                -> remoteCommandExecutorService.executeCommand(
                new RemoteExecCommand(getConnection(webServer),
                        new ExecCommand("cat", getHttpdConfPath(webServer)))));

    }

    private String getHttpdConfPath(final WebServer webServer) {
        final String wsName = webServer.getName();
        final ResourceIdentifier httpdConfResourceIdentifier = new ResourceIdentifier.Builder()
                .setResourceName("httpd.conf")
                .setWebServerName(webServer.getName())
                .build();
        final ResourceContent httpConfResourceContent = resourceService.getResourceContent(httpdConfResourceIdentifier);
        if (null == httpConfResourceContent || null == httpConfResourceContent.getMetaData() || httpConfResourceContent.getMetaData().isEmpty()) {
            final String errMsg = MessageFormat.format("No httpd.conf meta data for web server {0}", wsName);
            LOGGER.error(errMsg);
            throw new WebServerCommandFactoryException(errMsg);
        }

        ResourceTemplateMetaData metaData = null;
        try {
            metaData = resourceService.getMetaData(httpConfResourceContent.getMetaData());
        } catch (IOException e) {
            final String errMsg = MessageFormat.format("Failed to parse httpd.conf meta data for web server {0}", wsName);
            LOGGER.error(errMsg, e);
            throw new WebServerCommandFactoryException(errMsg);
        }

        final String httpdPath = resourceContentGeneratorService.generateContent("httpd.conf", metaData.getDeployPath(), null, webServer, ResourceGeneratorType.METADATA);
        final String httpdConfDeployName = resourceContentGeneratorService.generateContent("httpd.conf", metaData.getDeployFileName(), null, webServer, ResourceGeneratorType.METADATA);

        return httpdPath + "/" + httpdConfDeployName;
    }

    /**
     * Check to see if the script exists and copy it to the remote server if it doesn't
     * @param webserver the target web server of the script execution
     * @param scriptName the name of the script to be executed
     */
    private void checkExistsAndCopy(WebServer webserver, String scriptName) {
        final String destAbsolutePath = getFullPathScript(scriptName, webserver.getName());
        final CommandOutput fileExistsResult = binaryDistributionControlService.checkFileExists(webserver.getHost(), destAbsolutePath);
        if (!fileExistsResult.getReturnCode().wasSuccessful()) {
            copyScriptToRemoteDestination(webserver, scriptName, destAbsolutePath);
        } else {
            LOGGER.info("{} already exists. Continue with script execution", scriptName);
        }
    }

    /**
     * The script doesn't exist so copy it to the remote server. This method will create the parent directory of the
     * script if it doesn't exist, copy the script to that location, and then change the permissions of the script
     * to be executable.
     * @param webserver the target web server
     * @param scriptName the script to be executed
     * @param destAbsolutePath the destination of the script on the remote server
     */
    private void copyScriptToRemoteDestination(WebServer webserver, String scriptName, String destAbsolutePath) {
        LOGGER.info("{} does not exist at remote location. Performing secure copy.", scriptName);

        // Don't use java.io.File here to get the parent directory from getFullPathScript - we need to use the
        // path derived from the method in order to support deploying Web Servers across platforms (i.e. from a
        // Windows deployed jwala to a Linux remote host and vice versa).
        // So don't pass the script name here to just get the path of the parent directory
        final String destDir = getFullPathScript("", webserver.getName());
        final CommandOutput createDirResult = binaryDistributionControlService.createDirectory(webserver.getHost(), destDir);
        if (!createDirResult.getReturnCode().wasSuccessful()) {
            LOGGER.error("Failed to create directory {}", destDir);
            throw new WebServerServiceException("Failed to create directory " + destDir);
        }

        final CommandOutput copyResult = binaryDistributionControlService.secureCopyFile(
                webserver.getHost(),
                ApplicationProperties.getRequired("commands.scripts-path") + "/" + scriptName,
                destAbsolutePath);
        if (!copyResult.getReturnCode().wasSuccessful()) {
            LOGGER.error("Failed to secure copy {}", destAbsolutePath);
            throw new WebServerServiceException("Failed to secure copy " + destAbsolutePath);
        }
        LOGGER.info("Secure copy success to {}", destAbsolutePath);

        final CommandOutput fileModeResult = binaryDistributionControlService.changeFileMode(webserver.getHost(), "a+x", destDir, "*.sh");
        if (!fileModeResult.getReturnCode().wasSuccessful()) {
            LOGGER.error("Failed to make the files executable in {}", destDir);
            throw new WebServerServiceException("Failed to make the files executable in " + destDir);
        }
        LOGGER.info("Change file mode to executable success: {}", destDir);
    }

    /**
     *  Get the connection object to the remote system
     *
     * @param webserver provides the host for the connection
     * @return the connection object to the remote host
     */
    private RemoteSystemConnection getConnection(WebServer webserver) {
        return new RemoteSystemConnection(sshConfig.getUserName(), sshConfig.getEncryptedPassword(), webserver.getHost(), sshConfig.getPort());
    }

    /**
     * Get the full path to the script
     *
     * @param scriptName the name of the script
     * @param webServerName the name of the web server; used to create the directory where the script will reside
     * @return the absolute path of the script
     */
    private String getFullPathScript(String scriptName, String webServerName) {
        return ApplicationProperties.getRequired(PropertyKeys.REMOTE_SCRIPT_DIR) + "/" + webServerName + "/" + scriptName;
    }

    /**
     * Get the command object to be executed
     *
     * @param scriptName the name of the script
     * @param webserver the web server used to provide the directory name
     * @return the command object to execute
     */
    private ExecCommand getExecCommand(String scriptName, WebServer webserver, String... params) {
        String paramConcat = "";
        for (String param : params) {
            paramConcat += param + " ";
        }

        return new ExecCommand(getFullPathScript(scriptName, webserver.getName()), webserver.getName(), paramConcat);
    }

    /**
     * Get the shell command object to be executed
     *
     * @param scriptName the script name
     * @param webserver the name of the web server used to provide the directory name
     * @return the shell command object to execute
     */
    private ExecCommand getShellCommand(String scriptName, WebServer webserver, String... params) {
        String paramConcat = "";
        for (String param : params) {
            paramConcat += param + " ";
        }

        return new ShellCommand(getFullPathScript(scriptName, webserver.getName()), webserver.getName(), paramConcat);
    }
}
