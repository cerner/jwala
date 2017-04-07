package com.cerner.jwala.service.jvm.impl.spring.component;

import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.domain.model.state.StateType;
import com.cerner.jwala.persistence.jpa.type.EventType;
import com.cerner.jwala.service.HistoryFacadeService;
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
import java.text.MessageFormat;
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

    @Autowired
    private ClientFactoryHelper clientFactoryHelper;

    @Autowired
    private HistoryFacadeService historyFacadeService;

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
            LOGGER.debug("Response = {} from JVM {}", response.getStatusCode(), jvm.getJvmName());
            jvmStateService.updateNotInMemOrStaleState(jvm, JvmState.JVM_STARTED, StringUtils.EMPTY);
            currentState = new CurrentState<>(jvm.getId(), JvmState.JVM_STARTED, DateTime.now(), StateType.JVM);
        } catch (final IOException ioe) {
            LOGGER.warn("{} {} {}", jvm.getJvmName(), ioe.getMessage(), "Setting JVM state to STOPPED.", ioe);
            jvmStateService.updateNotInMemOrStaleState(jvm, JvmState.JVM_STOPPED, StringUtils.EMPTY);
            currentState = new CurrentState<>(jvm.getId(), JvmState.JVM_STOPPED, DateTime.now(), StateType.JVM);
        } catch (final RuntimeException rte) {
            // This method is executed asynchronously and we do not want to interrupt the thread's lifecycle so we
            // just catch and log runtime exceptions instead of rethrowing it
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

}
