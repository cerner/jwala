package com.cerner.jwala.service.webserver.impl;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerControlOperation;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.CommandOutputReturnCode;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.cerner.jwala.common.request.webserver.ControlWebServerRequest;
import com.cerner.jwala.control.webserver.command.WebServerCommandFactory;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.persistence.jpa.type.EventType;
import com.cerner.jwala.service.HistoryFacadeService;
import com.cerner.jwala.service.binarydistribution.DistributionService;
import com.cerner.jwala.service.exception.RemoteCommandExecutorServiceException;
import com.cerner.jwala.service.webserver.WebServerControlService;
import com.cerner.jwala.service.webserver.WebServerService;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.util.ArrayList;

import static com.cerner.jwala.common.domain.model.webserver.WebServerControlOperation.START;
import static com.cerner.jwala.common.domain.model.webserver.WebServerControlOperation.STOP;

public class WebServerControlServiceImpl implements WebServerControlService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebServerControlServiceImpl.class);
    private static final int SLEEP_DURATION = 1000;
    private static final String MSG_SERVICE_ALREADY_STARTED = "Service already started";
    private static final String MSG_SERVICE_ALREADY_STOPPED = "Service already stopped";
    private static final String FORCED_STOPPED = "FORCED STOPPED";
    private static final String WEB_SERVER = "Web Server";

    @Value("${spring.messaging.topic.serverStates:/topic/server-states}")
    protected String topicServerStates;

    @Autowired
    private WebServerCommandFactory webServerCommandFactory;

    @Autowired
    private DistributionService distributionService;

    @Autowired
    private WebServerService webServerService;

    @Autowired
    private HistoryFacadeService historyFacadeService;

    @Autowired
    private SshConfiguration sshConfig;


    @Override
    public CommandOutput controlWebServer(final ControlWebServerRequest controlWebServerRequest, final User aUser) {
        final WebServerControlOperation controlOperation = controlWebServerRequest.getControlOperation();
        final WebServer webServer = webServerService.getWebServer(controlWebServerRequest.getWebServerId());
        try {
            final String event = controlOperation.getOperationState() == null ?
                    controlOperation.name() : controlOperation.getOperationState().toStateLabel();

            historyFacadeService.write(getServerName(webServer), new ArrayList<>(webServer.getGroups()), event, EventType.USER_ACTION_INFO, aUser.getId());
            RemoteCommandReturnInfo remoteCommandReturnInfo = webServerCommandFactory.executeCommand(webServer, controlOperation);

            CommandOutput commandOutput = new CommandOutput(new ExecReturnCode(remoteCommandReturnInfo.retCode),
                    remoteCommandReturnInfo.standardOuput, remoteCommandReturnInfo.errorOupout);

            final String standardOutput = commandOutput.getStandardOutput();
            if (StringUtils.isNotEmpty(standardOutput) && (START.equals(controlOperation) ||
                    STOP.equals(controlOperation))) {
                commandOutput.cleanStandardOutput();
                LOGGER.info("shell command output {}", standardOutput);
            }

            // Process non successful return codes...
            if (!commandOutput.getReturnCode().wasSuccessful()) {
                final Integer returnCode = commandOutput.getReturnCode().getReturnCode();
                String commandOutputReturnDescription = CommandOutputReturnCode.fromReturnCode(returnCode).getDesc();
                switch (returnCode) {
                    case ExecReturnCode.JWALA_EXIT_PROCESS_KILLED:
                        commandOutput = new CommandOutput(new ExecReturnCode(0), FORCED_STOPPED, commandOutput.getStandardError());
                        webServerService.updateState(webServer.getId(), WebServerReachableState.FORCED_STOPPED, "");
                        break;
                    case ExecReturnCode.JWALA_EXIT_CODE_ABNORMAL_SUCCESS:
                        int retCode = 0;
                        switch (controlOperation) {
                            case START:
                                commandOutputReturnDescription = MSG_SERVICE_ALREADY_STARTED;
                                break;
                            case STOP:
                                commandOutputReturnDescription = MSG_SERVICE_ALREADY_STOPPED;
                                break;
                            default:
                                retCode = returnCode;
                                break;
                        }

                        sendMessageToActionEventLogs(aUser, webServer, commandOutputReturnDescription);

                        if (retCode == 0) {
                            commandOutput = new CommandOutput(new ExecReturnCode(retCode), commandOutputReturnDescription, null);
                        }
                        break;
                    case ExecReturnCode.JWALA_EXIT_NO_SUCH_SERVICE:
                        if (controlOperation.equals(START) || controlOperation.equals(STOP)) {
                            sendMessageToActionEventLogs(aUser, webServer, commandOutputReturnDescription);
                        } else {
                            final String errorMsg = createCommandErrorMessage(commandOutput, returnCode, controlOperation.getExternalValue());
                            sendMessageToActionEventLogs(aUser, webServer, errorMsg);
                        }

                        break;
                    default:
                        final String errorMsg = createCommandErrorMessage(commandOutput, returnCode, controlOperation.getExternalValue());
                        sendMessageToActionEventLogs(aUser, webServer, errorMsg);
                        break;
                }
            }
            return commandOutput;
        } catch (final RemoteCommandExecutorServiceException e) {
            LOGGER.error(e.getMessage(), e);

            historyFacadeService.write(getServerName(webServer), new ArrayList<>(webServer.getGroups()), e.getMessage(),
                    EventType.SYSTEM_ERROR, aUser.getId());

            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE,
                    "CommandFailureException when attempting to control a JVM: " + controlWebServerRequest, e);
        }
    }

    private String createCommandErrorMessage(CommandOutput commandOutput, Integer returnCode, String commandName) {
        return "Web Server control command [" + commandName + "] was not successful! Return code = "
                + returnCode + ", description = " +
                CommandOutputReturnCode.fromReturnCode(returnCode).getDesc() +
                ", message = " + commandOutput.standardErrorOrStandardOut();
    }

    private void sendMessageToActionEventLogs(User aUser, WebServer webServer, String commandOutputReturnDescription) {
        LOGGER.error(commandOutputReturnDescription);
        historyFacadeService.write(getServerName(webServer), new ArrayList<>(webServer.getGroups()), commandOutputReturnDescription,
                EventType.SYSTEM_ERROR, aUser.getId());
    }

    /**
     * Get the server name prefixed by the server type - "Web Server".
     *
     * @param webServer the {@link WebServer} object.
     * @return server name prefixed by "Web Server".
     */
    private String getServerName(WebServer webServer) {
        return WEB_SERVER + " " + webServer.getName();
    }

    @Override
    public void secureCopyFile(final String aWebServerName, final String sourcePath, final String destPath, String userId) throws CommandFailureException {
        final WebServer aWebServer = webServerService.getWebServer(aWebServerName);
        final String fileName = new File(destPath).getName();
        if (destPath.endsWith(fileName)) {
            historyFacadeService.write(getServerName(aWebServer), new ArrayList<>(aWebServer.getGroups()),
                    WebServerControlOperation.SCP.name() + " " + fileName, EventType.USER_ACTION_INFO, userId);
        }
        if(distributionService.remoteFileCheck(aWebServer.getHost(),destPath)){
            LOGGER.info("Found the file {}", destPath);
            distributionService.backupFile(aWebServer.getHost(),destPath);
        }
        distributionService.remoteSecureCopyFile(aWebServer.getHost(), sourcePath,destPath);
    }

    @Override
    public void createDirectory(WebServer webServer, String dirAbsolutePath) throws CommandFailureException {
        distributionService.remoteCreateDirectory(webServer.getHost(), dirAbsolutePath);
    }

    @Override
    public void changeFileMode(WebServer webServer, String fileMode, String targetDirPath, String targetFile) throws CommandFailureException {
        distributionService.changeFileMode(webServer.getHost(),fileMode, targetDirPath, targetFile);
    }

    @Override
    public boolean waitForState(ControlWebServerRequest controlWebServerRequest, final Long waitTimeout) {
        final Long startTime = DateTime.now().getMillis();
        final WebServerControlOperation webServerControlOperation = controlWebServerRequest.getControlOperation();
        while (true) {
            final WebServer webServer = webServerService.getWebServer(controlWebServerRequest.getWebServerId());
            LOGGER.info("Retrieved web server: {}", webServer);
            switch (webServerControlOperation) {
                case START:
                    if (webServer.getState() == WebServerReachableState.WS_REACHABLE) {
                        return true;
                    }
                    break;
                case STOP:
                    if (webServer.getState() == WebServerReachableState.WS_UNREACHABLE ||
                            webServer.getState() == WebServerReachableState.FORCED_STOPPED) {
                        return true;
                    }
                    break;
                default:
                    throw new InternalErrorException(FaultType.SERVICE_EXCEPTION, "JvmCommand: " + webServerControlOperation.toString() + " not supported");
            }
            if (DateTime.now().getMillis() - startTime > waitTimeout) {
                LOGGER.warn("Timeout reached to get the state for webserver: {}", webServer.getName());
                break;
            }
            try {
                Thread.sleep(SLEEP_DURATION);
            } catch (InterruptedException e) {
                LOGGER.error("Error with Thread.sleep", e);
                throw new InternalErrorException(FaultType.SERVICE_EXCEPTION, "Error with waiting for state for WebServer: " + webServer.getName(), e);
            }
        }
        return false;
    }

}