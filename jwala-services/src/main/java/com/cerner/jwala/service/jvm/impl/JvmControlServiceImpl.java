package com.cerner.jwala.service.jvm.impl;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.CommandOutputReturnCode;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.request.jvm.ControlJvmRequest;
import com.cerner.jwala.control.command.common.Command;
import com.cerner.jwala.control.command.common.ShellCommandFactory;
import com.cerner.jwala.control.jvm.command.JvmCommandFactory;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.persistence.jpa.domain.JpaHistory;
import com.cerner.jwala.persistence.jpa.type.EventType;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.service.HistoryFacadeService;
import com.cerner.jwala.service.exception.RemoteCommandExecutorServiceException;
import com.cerner.jwala.service.jvm.JvmControlService;
import com.cerner.jwala.service.jvm.JvmStateService;
import com.cerner.jwala.service.jvm.exception.JvmControlServiceException;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Paths;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

public class JvmControlServiceImpl implements JvmControlService {

    @Autowired
    private JvmCommandFactory jvmCommandFactory;

    @Autowired
    private ShellCommandFactory shellCommandFactory;

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmControlServiceImpl.class);
    private static final String FORCED_STOPPED = "FORCED STOPPED";
    private static final String JVM = "JVM ";

    private final JvmPersistenceService jvmPersistenceService;
    private final HistoryFacadeService historyFacadeService;
    private final JvmStateService jvmStateService;

    private static final int THREAD_SLEEP_DURATION = 1000;

    private static final String MSG_SERVICE_ALREADY_STARTED = "Service already started";
    private static final String MSG_SERVICE_ALREADY_STOPPED = "Service already stopped";

    public JvmControlServiceImpl(final JvmPersistenceService jvmPersistenceService,
                                 final JvmStateService jvmStateService,
                                 final HistoryFacadeService historyFacadeService) {
        this.jvmPersistenceService = jvmPersistenceService;
        this.jvmStateService = jvmStateService;
        this.historyFacadeService = historyFacadeService;
    }

    @Override
    public CommandOutput controlJvm(final ControlJvmRequest controlJvmRequest, final User aUser) {
        final JvmControlOperation controlOperation = controlJvmRequest.getControlOperation();
        LOGGER.debug("Control JVM request operation = {}", controlOperation.toString());
        final Jvm jvm = jvmPersistenceService.getJvm(controlJvmRequest.getJvmId());
        try {
            final String historyMessage = controlJvmRequest.getMessage() == null ? controlOperation.name() :
                    controlJvmRequest.getMessage();

            historyFacadeService.write(getServerName(jvm), new ArrayList<>(jvm.getGroups()), historyMessage, EventType.USER_ACTION_INFO, aUser.getId());

            RemoteCommandReturnInfo remoteCommandReturnInfo = jvmCommandFactory.executeCommand(jvm, controlJvmRequest.getControlOperation());
            CommandOutput commandOutput = new CommandOutput(new ExecReturnCode(remoteCommandReturnInfo.retCode),
                    remoteCommandReturnInfo.standardOuput, remoteCommandReturnInfo.errorOupout);

            final String standardOutput = commandOutput.getStandardOutput();
            final ExecReturnCode returnCode = commandOutput.getReturnCode();
            //What is this used for?
            if (StringUtils.isNotEmpty(standardOutput) && (JvmControlOperation.START.equals(controlOperation) ||
                    JvmControlOperation.STOP.equals(controlOperation))) {
                commandOutput.cleanStandardOutput();
                LOGGER.info("shell command output is {}", standardOutput);
            } else if (StringUtils.isNoneBlank(standardOutput) && JvmControlOperation.HEAP_DUMP.equals(controlOperation)
                    && returnCode.wasSuccessful()) {
                commandOutput.cleanHeapDumpStandardOutput();
            }
            LOGGER.debug("JvmCommand output return code = {}", returnCode);
            if (returnCode.wasSuccessful()) {
                if (JvmControlOperation.STOP.equals(controlOperation)) {
                    LOGGER.debug("Updating state to {}...", JvmState.JVM_STOPPED);
                    jvmStateService.updateState(jvm, JvmState.JVM_STOPPED);
                    LOGGER.debug("State successfully updated to {}", JvmState.JVM_STOPPED);
                }
            } else {
                // Process non successful return codes...
                String commandOutputReturnDescription = CommandOutputReturnCode.fromReturnCode(returnCode.getReturnCode()).getDesc();
                switch (returnCode.getReturnCode()) {
                    case ExecReturnCode.JWALA_EXIT_PROCESS_KILLED:
                        commandOutput = new CommandOutput(new ExecReturnCode(0), FORCED_STOPPED, commandOutput.getStandardError());
                        jvmStateService.updateState(jvm, JvmState.FORCED_STOPPED);
                        break;
                    case ExecReturnCode.JWALA_EXIT_CODE_ABNORMAL_SUCCESS:
                        int retCode = 0;
                        switch (controlJvmRequest.getControlOperation()) {
                            case START:
                                commandOutputReturnDescription = MSG_SERVICE_ALREADY_STARTED;
                                break;
                            case STOP:
                                commandOutputReturnDescription = MSG_SERVICE_ALREADY_STOPPED;
                                break;
                            default:
                                retCode = returnCode.getReturnCode();
                                break;
                        }

                        sendMessageToActionEventLog(aUser, jvm, commandOutputReturnDescription);

                        if (retCode == 0) {
                            commandOutput = new CommandOutput(new ExecReturnCode(retCode), commandOutputReturnDescription, null);
                        }
                        break;
                    case ExecReturnCode.JWALA_EXIT_CODE_NO_OP:
                        if (controlOperation.equals(JvmControlOperation.START) || controlOperation.equals(JvmControlOperation.STOP)) {
                            sendMessageToActionEventLog(aUser, jvm, commandOutputReturnDescription);
                        } else {
                            final String errorMsg = getCommandErrorMessage(commandOutput, returnCode, commandOutputReturnDescription);
                            sendMessageToActionEventLog(aUser, jvm, errorMsg);
                        }
                        break;
                    default:
                        final String errorMsg = getCommandErrorMessage(commandOutput, returnCode, commandOutputReturnDescription);
                        sendMessageToActionEventLog(aUser, jvm, errorMsg);
                        break;
                }
            }

            return commandOutput;
        } catch (final RemoteCommandExecutorServiceException e) {
            LOGGER.error(e.getMessage(), e);
            historyFacadeService.write(getServerName(jvm), new ArrayList<>(jvm.getGroups()), e.getMessage(), EventType.SYSTEM_ERROR,
                    aUser.getId());
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE,
                    "CommandFailureException when attempting to control a JVM: " + controlJvmRequest, e);
        }
    }

    private String getCommandErrorMessage(CommandOutput commandOutput, ExecReturnCode returnCode, String commandOutputReturnDescription) {
        return "JVM remote command FAILURE. Return code = "
                + returnCode.getReturnCode() + ", description = " +
                commandOutputReturnDescription + ", message = " + commandOutput.standardErrorOrStandardOut();
    }

    private void sendMessageToActionEventLog(User aUser, Jvm jvm, String commandOutputReturnDescription) {
        LOGGER.error(commandOutputReturnDescription);
        final String logString = commandOutputReturnDescription.length() <= JpaHistory.getMaxEventLen() ? commandOutputReturnDescription : commandOutputReturnDescription.substring(0, JpaHistory.getMaxEventLen());
        historyFacadeService.write(getServerName(jvm), new ArrayList<>(jvm.getGroups()), logString,
                EventType.SYSTEM_ERROR, aUser.getId());
    }

    @Override
    public CommandOutput controlJvmSynchronously(final ControlJvmRequest controlJvmRequest, final long timeout,
                                                 final User user) throws InterruptedException {

        final CommandOutput commandOutput = controlJvm(controlJvmRequest, user);
        if (commandOutput.getReturnCode().wasSuccessful()) {
            // Process start/stop operations only for now...
            switch (controlJvmRequest.getControlOperation()) {
                case START:
                    waitForState(controlJvmRequest, timeout, JvmState.JVM_STARTED);
                    break;
                case STOP:
                    waitForState(controlJvmRequest, timeout, JvmState.JVM_STOPPED, JvmState.FORCED_STOPPED);
                    break;
                case BACK_UP:
                case CHANGE_FILE_MODE:
                case CHECK_FILE_EXISTS:
                case CREATE_DIRECTORY:
                case DELETE_SERVICE:
                case DEPLOY_JVM_ARCHIVE:
                case HEAP_DUMP:
                case INSTALL_SERVICE:
                case SCP:
                case THREAD_DUMP:
                    throw new UnsupportedOperationException();
            }
        }
        return commandOutput;
    }

    /**
     * Loop until jvm state is in expected state
     *
     * @param controlJvmRequest {@link ControlJvmRequest}
     * @param timeout           the timeout in ms
     *                          Note: the remote command called before this method might also be waiting for service state like in
     *                          the case of "FORCED STOPPED" and if so a timeout will never occur here
     * @param expectedStates    expected {@link JvmState}
     * @throws InterruptedException
     */
    private void waitForState(final ControlJvmRequest controlJvmRequest, final long timeout,
                              final JvmState... expectedStates) throws InterruptedException {
        final long startTime = DateTime.now().getMillis();
        while (true) {
            final Jvm jvm = jvmPersistenceService.getJvm(controlJvmRequest.getJvmId());
            LOGGER.info("Retrieved jvm: {}", jvm);

            for (final JvmState jvmState : expectedStates) {
                if (jvmState.equals(jvm.getState())) {
                    return;
                }
            }

            if ((DateTime.now().getMillis() - startTime) > timeout) {
                throw new JvmControlServiceException("Timeout limit reached while waiting for JVM to " +
                        controlJvmRequest.getControlOperation().name());
            }
            Thread.sleep(THREAD_SLEEP_DURATION);
        }
    }

    /**
     * Get the server name prefixed by the server type - "JVM".
     *
     * @param jvm the {@link Jvm} object.
     * @return server name prefixed by "JVM".
     */
    private String getServerName(final Jvm jvm) {
        return JVM + " " + jvm.getJvmName();
    }

    @Override
    public CommandOutput secureCopyFile(final ControlJvmRequest secureCopyRequest, final String sourcePath,
                                        final String destPath, String userId, boolean overwrite) throws CommandFailureException {
        final Identifier<Jvm> jvmId = secureCopyRequest.getJvmId();

        final String event = secureCopyRequest.getControlOperation().name();
        final Jvm jvm = jvmPersistenceService.getJvm(jvmId);
        final int beginIndex = destPath.lastIndexOf("/");
        final String fileName = destPath.substring(beginIndex + 1, destPath.length());

        final String name = jvm.getJvmName();
        final String hostName = jvm.getHostName();
        final String parentDir;

        //TODOhandle ~
        if (destPath.startsWith("~")) {
            parentDir = destPath.substring(0, destPath.lastIndexOf("/"));
        } else {
            //derive parentDir
            parentDir = Paths.get(destPath).getParent().toString().replaceAll("\\\\", "/");
        }
        CommandOutput commandOutput = executeCreateDirectoryCommand(jvm, parentDir);

        if (commandOutput.getReturnCode().wasSuccessful()) {
            LOGGER.info("Successfully created parent dir {} on host {}", parentDir, hostName);
        } else {
            final String standardError = commandOutput.getStandardError().isEmpty() ? commandOutput.getStandardOutput() : commandOutput.getStandardError();
            LOGGER.error("create command failed with error trying to create parent directory {} on {} :: ERROR: {}", parentDir, hostName, standardError);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, standardError.isEmpty() ? CommandOutputReturnCode.fromReturnCode(commandOutput.getReturnCode().getReturnCode()).getDesc() : standardError);
        }
        commandOutput = executeCheckFileExistsCommand(jvm, destPath);

        final boolean fileExists = commandOutput.getReturnCode().wasSuccessful();
        if (fileExists && overwrite) {
            commandOutput = executeBackUpCommand(jvm, destPath);

            if (!commandOutput.getReturnCode().wasSuccessful()) {
                final String standardError = "Failed to back up the " + destPath + " for " + name + ".";
                LOGGER.error(standardError);
                throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, standardError);
            } else {
                LOGGER.info("Successfully backed up " + destPath + " at " + hostName);
            }

        } else if (fileExists) {
            // exit without deploying since the file exists and overwrite is false
            String message = MessageFormat.format("Skipping scp of file: {0} already exists and overwrite is set to false.", destPath);
            LOGGER.info(message);
            historyFacadeService.write(hostName, jvm.getGroups(), message, EventType.SYSTEM_INFO, userId);
            return new CommandOutput(new ExecReturnCode(0), message, "");
        }

        // don't add any usage of the jwala user internal directory to the history
        if (!ApplicationProperties.get("remote.commands.user-scripts").endsWith(fileName)) {
            final String eventDescription = event + " sending file " + fileName + " to remote path " + destPath + " on host " + jvm.getHostName();
            historyFacadeService.write(getServerName(jvm),
                    new ArrayList<>(jvm.getGroups()),
                    eventDescription,
                    EventType.USER_ACTION_INFO, userId);
        }

        RemoteCommandReturnInfo remoteCommandReturnInfo = shellCommandFactory.executeRemoteCommand(jvm.getHostName(),
                Command.SCP, sourcePath, destPath);
        return new CommandOutput(new ExecReturnCode(remoteCommandReturnInfo.retCode),
                remoteCommandReturnInfo.standardOuput, remoteCommandReturnInfo.errorOupout);
    }

    @Override
    public CommandOutput executeChangeFileModeCommand(final Jvm jvm, final String modifiedPermissions, final String targetAbsoluteDir, final String targetFile)
            throws CommandFailureException {
        RemoteCommandReturnInfo remoteCommandReturnInfo = shellCommandFactory.executeRemoteCommand(jvm.getHostName(),
                Command.CHANGE_FILE_MODE, modifiedPermissions, targetAbsoluteDir + "/" + targetFile);
        return new CommandOutput(new ExecReturnCode(remoteCommandReturnInfo.retCode),
                remoteCommandReturnInfo.standardOuput, remoteCommandReturnInfo.errorOupout);
    }


    @Override
    public CommandOutput executeCreateDirectoryCommand(final Jvm jvm, final String dirAbsolutePath) throws CommandFailureException {
        RemoteCommandReturnInfo remoteCommandReturnInfo = shellCommandFactory.executeRemoteCommand(jvm.getHostName(), Command.CREATE_DIR, dirAbsolutePath);
        return new CommandOutput(new ExecReturnCode(remoteCommandReturnInfo.retCode),
                remoteCommandReturnInfo.standardOuput, remoteCommandReturnInfo.errorOupout);

    }

    @Override
    public CommandOutput executeCheckFileExistsCommand(final Jvm jvm, final String filename) throws CommandFailureException {
        RemoteCommandReturnInfo remoteCommandReturnInfo = shellCommandFactory.executeRemoteCommand(jvm.getHostName(), Command.CHECK_FILE_EXISTS, filename);
        return new CommandOutput(new ExecReturnCode(remoteCommandReturnInfo.retCode),
                remoteCommandReturnInfo.standardOuput, remoteCommandReturnInfo.errorOupout);
    }

    @Override
    public CommandOutput executeBackUpCommand(final Jvm jvm, final String filename) throws CommandFailureException {
        final String currentDateSuffix = new SimpleDateFormat(".yyyyMMdd_HHmmss").format(Date.from(Instant.now()));
        final String destPathBackup = filename + currentDateSuffix;
        RemoteCommandReturnInfo remoteCommandReturnInfo = shellCommandFactory.executeRemoteCommand(jvm.getHostName(), Command.MOVE, filename, destPathBackup);
        return new CommandOutput(new ExecReturnCode(remoteCommandReturnInfo.retCode),
                remoteCommandReturnInfo.standardOuput, remoteCommandReturnInfo.errorOupout);
    }

    /**
     * @param jvmCommandFactory
     */
    public void setJvmCommandFactory(JvmCommandFactory jvmCommandFactory) {
        this.jvmCommandFactory = jvmCommandFactory;
    }
}
