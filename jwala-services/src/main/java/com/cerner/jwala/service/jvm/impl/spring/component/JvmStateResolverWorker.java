package com.cerner.jwala.service.jvm.impl.spring.component;

import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.domain.model.state.StateType;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.cerner.jwala.service.exception.RemoteCommandExecutorServiceException;
import com.cerner.jwala.service.jvm.JvmStateService;
import com.cerner.jwala.service.webserver.component.ClientFactoryHelper;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.Future;

/**
 * Resolves a JVM's state.
 * NOTE: For @Async to work, the worker must be in its own class separate from the caller the reason for this
 *       class' existence.
 *
 * Created by Jedd Cuison on 3/24/2016.
 */
@Service
public class JvmStateResolverWorker {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmStateResolverWorker.class);
    private static final String WINDOWS_SVC_STOPPED = "STOPPED";
    private static final String LINUX_SVC_STOPPED = "is stopped";
    private static final String NOT_RECEIVING_JVM_STATE_ERR_MSG = "Jwala not receiving updates from this JVM. " +
            "Possible causes are messaging settings in vars.properties are wrong, JVM is not functioning correctly " +
            "(configuration error(s) etc) even though the service is running.";

    private final ClientFactoryHelper clientFactoryHelper;

    @Autowired
    public JvmStateResolverWorker(final ClientFactoryHelper clientFactoryHelper) {
        this.clientFactoryHelper = clientFactoryHelper;
    }

    @Async("jvmTaskExecutor")
    public Future<CurrentState<Jvm, JvmState>> pingAndUpdateJvmState(final Jvm jvm, final JvmStateService jvmStateService) {
        LOGGER.debug("The reverse heartbeat has kicked in! This means that we're not receiving any states from Jvm {}@{}.",
                jvm.getJvmName(), jvm.getHostName());
        LOGGER.debug("+++ pingAndUpdateJvmState");
        ClientHttpResponse response = null;
        CurrentState<Jvm, JvmState> currentState = null;

        // if the jvm was just created do not check its state
        if (jvm.getState().equals(JvmState.JVM_NEW)){
            return new AsyncResult<>(new CurrentState<>(jvm.getId(), jvm.getState(), DateTime.now(), StateType.JVM));
        }

        try {
            response = clientFactoryHelper.requestGet(jvm.getStatusUri());
            LOGGER.debug(">>> Response = {} from JVM {}", response.getStatusCode(), jvm.getId().getId());
            if (response.getStatusCode() == HttpStatus.OK) {
                jvmStateService.updateNotInMemOrStaleState(jvm, JvmState.JVM_STARTED, StringUtils.EMPTY);
                currentState = new CurrentState<>(jvm.getId(), JvmState.JVM_STARTED, DateTime.now(), StateType.JVM);
            } else {
                currentState = verifyIfJvmWinServiceIsStoppedAndDoAnUpdate(jvm, jvmStateService);
            }
        } catch (final IOException ioe) {
            LOGGER.error("{} {}", jvm.getJvmName(), ioe.getMessage(), ioe);
            currentState = verifyIfJvmWinServiceIsStoppedAndDoAnUpdate(jvm, jvmStateService);
        } catch (final RuntimeException rte) {
            // This method is executed asynchronously and we do not want to interrupt the thread's lifecycle.
            LOGGER.error(rte.getMessage(), rte);
        } finally {
            if (response != null) {
                response.close();
                LOGGER.debug("response closed");
            }
            LOGGER.debug("--- pingAndUpdateJvmState");
        }
        return new AsyncResult<>(currentState);
    }

    /**
     * Verify if the JVM Window's service is stopped and update the state.
     * This method was intended to be called after an unsuccessful ping.
     * It verifies if the Windows service is stopped and if it is, then we can say that the JVM is stopped.
     * If the Window's service is not stopped and then we set the JVM state to UNKNOWN.
     * The reason for setting the JVM state to UNKNOWN even if the Window's service is running is because
     * the Window's service state is NOT THE SAME as the JVM state. There can be a case where the Window's service is
     * running but the JVM is not running as it should, meaning it's not serving the web applications and it's not
     * sending state messages.
     *
     * @param jvm the JVM
     * @param jvmStateService {@link JvmStateService}
     * @return {@link CurrentState}
     */
    private CurrentState<Jvm, JvmState> verifyIfJvmWinServiceIsStoppedAndDoAnUpdate(final Jvm jvm, final JvmStateService jvmStateService) {
        try {
            final RemoteCommandReturnInfo remoteCommandReturnInfo = jvmStateService.getServiceStatus(jvm);
            LOGGER.debug("RemoteCommandReturnInfo = {}", remoteCommandReturnInfo);
            if (isServiceStopped(remoteCommandReturnInfo)) {
                jvmStateService.updateNotInMemOrStaleState(jvm, JvmState.JVM_STOPPED, StringUtils.EMPTY);
                return new CurrentState<>(jvm.getId(), JvmState.JVM_STOPPED, DateTime.now(), StateType.JVM);
            }
            LOGGER.error("Did not get the expected conditions for the service in the stopped state! RemoteCommandReturnInfo = {}",
                    remoteCommandReturnInfo);
        } catch (final RemoteCommandExecutorServiceException rcese) {
            LOGGER.error("State verification of {}@{} via SSH failed! Please note that this has nothing to do with the " +
                    "JVM not receiving any state. This is just a redundancy check after an unsuccessful URL ping. " +
                    "Please check the next error message (JVM state listener is not receiving any state...) for possible " +
                    "causes.", jvm.getJvmName(), jvm.getHostName(), rcese);
        }

        LOGGER.error(NOT_RECEIVING_JVM_STATE_ERR_MSG);

        // The state should be unknown if we can't verify the JVM's state.
        // In addition, if we just leave the state as is and just report an error, if that state is in started,
        // the state resolver (reverse heartbeat) will always end up trying to ping it which will eat CPU resources.
        final JvmState state = JvmState.JVM_UNKNOWN;
        jvmStateService.updateNotInMemOrStaleState(jvm, state, NOT_RECEIVING_JVM_STATE_ERR_MSG);
        return new CurrentState<>(jvm.getId(), state, DateTime.now(), StateType.JVM);
    }

    private boolean isServiceStopped(RemoteCommandReturnInfo remoteCommandReturnInfo) {
        final int windowsRetCode = 0;
        final int linuxRetCode = 3;
        final boolean isWindowsServiceStopped = remoteCommandReturnInfo.retCode == windowsRetCode && remoteCommandReturnInfo.standardOuput.contains(WINDOWS_SVC_STOPPED);
        final boolean isLinuxServiceStopped = remoteCommandReturnInfo.retCode == linuxRetCode && remoteCommandReturnInfo.standardOuput.contains(LINUX_SVC_STOPPED);

        LOGGER.debug("Expecting {} and {}: {}", windowsRetCode, WINDOWS_SVC_STOPPED, isWindowsServiceStopped);
        LOGGER.debug("Expecting {} and {}: {}", linuxRetCode, LINUX_SVC_STOPPED, isLinuxServiceStopped);
        
        return isWindowsServiceStopped || isLinuxServiceStopped;
    }

}
