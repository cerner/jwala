package com.cerner.jwala.control.webserver.command;

/**
 * Created by Arvindo Kinny on 12/22/2016.
 */


import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerControlOperation;
import com.cerner.jwala.common.exec.ExecCommand;
import com.cerner.jwala.common.exec.RemoteExecCommand;
import com.cerner.jwala.common.exec.RemoteSystemConnection;
import com.cerner.jwala.common.exec.ShellCommand;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.properties.PropertyKeys;
import com.cerner.jwala.service.RemoteCommandExecutorService;
import com.cerner.jwala.service.exception.ApplicationServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
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

    /**
     *
     * @param webserver
     * @param operation
     * @return
     * @throws ApplicationServiceException
     */
    public RemoteCommandReturnInfo executeCommand(WebServer webserver, WebServerControlOperation operation) throws ApplicationServiceException{
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
        for (String command:commands.keySet()) {
            LOGGER.debug(command);
        }
    }

    @PostConstruct
    public void initWebServerCommands() {
        commands = new HashMap<>();
        // commands are added here using lambdas. It is also possible to dynamically add commands without editing the code.
        commands.put(WebServerControlOperation.START.getExternalValue(), (WebServer webServer)
                -> remoteCommandExecutorService.executeCommand(
                new RemoteExecCommand(getConnection(webServer),getShellCommand(START_SCRIPT_NAME.getValue(),
                                                                              webServer,
                                                                              webServer.getName()))));
        commands.put(WebServerControlOperation.STOP.getExternalValue(), (WebServer webServer)
                -> remoteCommandExecutorService.executeCommand(
                new RemoteExecCommand(getConnection(webServer),getShellCommand(STOP_SCRIPT_NAME.getValue(),
                                                                              webServer,
                                                                              webServer.getName()))));
        commands.put(WebServerControlOperation.DELETE_SERVICE.getExternalValue(), (WebServer webServer)
                -> remoteCommandExecutorService.executeCommand(
                new RemoteExecCommand(getConnection(webServer),getExecCommand(DELETE_SERVICE_SCRIPT_NAME.getValue(),
                                                                              webServer))));
        commands.put(WebServerControlOperation.INSTALL_SERVICE.getExternalValue(), (WebServer webServer)
                -> remoteCommandExecutorService.executeCommand(
                new RemoteExecCommand(getConnection(webServer),
                                      getShellCommand(INSTALL_SERVICE_WS_SERVICE_SCRIPT_NAME.getValue() ,
                                                     webServer,
                                                     ApplicationProperties.getRequired(PropertyKeys.REMOTE_PATHS_APACHE_HTTPD_CONF),
                                                     ApplicationProperties.getRequired(PropertyKeys.REMOTE_PATHS_APACHE_HTTPD)))));
        commands.put(WebServerControlOperation.VIEW_HTTP_CONFIG_FILE.getExternalValue(), (WebServer webServer)
                -> remoteCommandExecutorService.executeCommand(
                new RemoteExecCommand(getConnection(webServer),
                        new ExecCommand("cat", ApplicationProperties.getRequired(PropertyKeys.REMOTE_PATHS_APACHE_HTTPD_CONF)+"/httpd.conf"))));

    }

    /**
     *
     * @param webserver
     * @return
     */
    private RemoteSystemConnection getConnection(WebServer webserver) {
        return new RemoteSystemConnection(sshConfig.getUserName(), sshConfig.getEncryptedPassword(), webserver.getHost(), sshConfig.getPort());
    }

    /**
     * Get
     * @param scriptName
     * @return
     */
    private String getFullPathScript(String scriptName){
        return ApplicationProperties.getRequired(PropertyKeys.REMOTE_SCRIPT_DIR) + "/"+ scriptName;
    }

    /**
     *
     * @param scriptName
     * @param webserver
     * @return
     */
    private ExecCommand getExecCommand(String scriptName, WebServer webserver, String... params ){
        String paramConcat = "";
        for (String param:params) {
            paramConcat+=param + " ";
        }

        return new ExecCommand(getFullPathScript(scriptName), webserver.getName(), paramConcat);
    }
    /**
     *
     * @param scriptName
     * @param webserver
     * @return
     */
    private ExecCommand getShellCommand(String scriptName, WebServer webserver, String... params ){
        String paramConcat = "";
        for (String param:params) {
            paramConcat+=param + " ";
        }

        return new ShellCommand(getFullPathScript(scriptName), webserver.getName(), paramConcat);
    }
}
