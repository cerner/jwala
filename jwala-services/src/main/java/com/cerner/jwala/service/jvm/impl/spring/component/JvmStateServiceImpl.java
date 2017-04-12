package com.cerner.jwala.service.jvm.impl.spring.component;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.domain.model.state.StateType;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.cerner.jwala.control.jvm.command.JvmCommandFactory;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.service.MessagingService;
import com.cerner.jwala.service.group.GroupStateNotificationService;
import com.cerner.jwala.service.jvm.JvmStateService;
import com.cerner.jwala.service.state.InMemoryStateManagerService;
import de.jkeylockmanager.manager.KeyLockManager;
import de.jkeylockmanager.manager.LockCallback;
import de.jkeylockmanager.manager.implementation.lockstripe.StripedKeyLockManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * {@link JvmStateService} implementation.
 * <p>
 * Created by Jedd Cuison on 3/22/2016.
 */
@Service
public class JvmStateServiceImpl implements JvmStateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmStateServiceImpl.class);

    private static final Map<Identifier<Jvm>, Future<CurrentState<Jvm, JvmState>>> PING_FUTURE_MAP = new ConcurrentHashMap<>();

    private final JvmPersistenceService jvmPersistenceService;
    private final InMemoryStateManagerService<Identifier<Jvm>, CurrentState<Jvm, JvmState>> inMemoryStateManagerService;
    private final JvmStateResolverWorker jvmStateResolverWorker;
    private final long jvmStateUpdateInterval;
    private final MessagingService messagingService;
    private final GroupStateNotificationService groupStateNotificationService;
    private final JvmCommandFactory jvmCommandFactory;
    private final SshConfiguration sshConfig;
    private final KeyLockManager lockManager;

    @Autowired
    public JvmStateServiceImpl(final JvmPersistenceService jvmPersistenceService,
                               @Qualifier("jvmInMemoryStateManagerService")
                               final InMemoryStateManagerService<Identifier<Jvm>, CurrentState<Jvm, JvmState>> inMemoryStateManagerService,
                               final JvmStateResolverWorker jvmStateResolverWorker,
                               final MessagingService messagingService,
                               final GroupStateNotificationService groupStateNotificationService,
                               @Value("${jvm.state.update.interval:60000}")
                               final long jvmStateUpdateInterval,
                               final JvmCommandFactory jvmCommandFactory,
                               final SshConfiguration sshConfig,
                               @Value("${jvm.state.key.lock.timeout.millis:600000}")
                               final long lockTimeout,
                               @Value("${jvm.state.key.lock.stripe.count:120}")
                               final int keyLockStripeCount) {
        this.jvmPersistenceService = jvmPersistenceService;
        this.inMemoryStateManagerService = inMemoryStateManagerService;
        this.jvmStateResolverWorker = jvmStateResolverWorker;
        this.jvmStateUpdateInterval = jvmStateUpdateInterval;
        this.messagingService = messagingService;
        this.groupStateNotificationService = groupStateNotificationService;
        this.jvmCommandFactory = jvmCommandFactory;
        this.sshConfig = sshConfig;
        lockManager = new StripedKeyLockManager(lockTimeout, TimeUnit.MILLISECONDS, keyLockStripeCount);

        initInMemoryStateService();
    }

    private void initInMemoryStateService() {
        for (Jvm jvm : jvmPersistenceService.getJvms()) {
            final Date lastUpdateDate = jvmPersistenceService.getJpaJvm(jvm.getId(), false).getLastUpdateDate().getTime();
            inMemoryStateManagerService.put(jvm.getId(), new CurrentState<>(jvm.getId(), jvm.getState(), new DateTime(lastUpdateDate), StateType.JVM));
        }
    }

    @Override
    @Scheduled(fixedDelayString = "${ping.jvm.period.millis}")
    public void verifyAndUpdateJvmStates() {
        final List<Jvm> jvms = jvmPersistenceService.getJvms();
        if (CollectionUtils.isEmpty(jvms)) {
            LOGGER.warn("No JVMs found to ping.");
            return;
        }

        for (final Jvm jvm : jvms) {
            if (stateNotInMemory(jvm) || isValidState(jvm) && isFutureNilOrDone(jvm) && isStale(jvm)) {
                LOGGER.debug("Pinging JVM {} ...", jvm.getJvmName());
                PING_FUTURE_MAP.put(jvm.getId(), jvmStateResolverWorker.pingAndUpdateJvmState(jvm, this));
                LOGGER.debug("Pinged JVM {}", jvm.getJvmName());
            }
        }
    }

    private boolean isFutureNilOrDone(Jvm jvm) {
        final Future<CurrentState<Jvm, JvmState>> pingFuture = PING_FUTURE_MAP.get(jvm.getId());
        return pingFuture==null || pingFuture.isDone();
    }

    private boolean isValidState(Jvm jvm) {
        return isStarted(jvm) || isStopping(jvm);
    }

    /**
     * Check if the JVM's state is stale by checking the state's time stamp.
     *
     * @param jvm {@link Jvm}
     * @return true if the state is stale.
     */
    protected boolean isStale(final Jvm jvm) {
        CurrentState<Jvm, JvmState> jvmCurrentState = inMemoryStateManagerService.get(jvm.getId());
        if (jvmCurrentState == null) {
            return false;
        }

        if (jvmCurrentState.getAsOf() == null) {
            return false;
        }

        final long interval = DateTime.now().getMillis() - jvmCurrentState.getAsOf().getMillis();
        if (interval > jvmStateUpdateInterval) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("JVM {}'s state is stale. Interval since last update = {} sec!", jvm.getJvmName(), interval / 1000);
            }
            return true;
        }

        return false;
    }

    /**
     * Checks if a JVM's state is set to started.
     *
     * @param jvm {@link Jvm}
     * @return true if the state is started.
     */
    protected boolean isStarted(final Jvm jvm) {
        CurrentState<Jvm, JvmState> jvmCurrentState = inMemoryStateManagerService.get(jvm.getId());
        return jvmCurrentState != null && JvmState.JVM_STARTED.equals(jvmCurrentState.getState());
    }

    /**
     * Checks if a JVM's state is set to stopping.
     *
     * @param jvm {@link Jvm}
     * @return true if the state is started.
     */
    protected boolean isStopping(final Jvm jvm) {
        CurrentState<Jvm, JvmState> jvmCurrentState = inMemoryStateManagerService.get(jvm.getId());
        return jvmCurrentState != null && JvmState.JVM_STOPPING.equals(jvmCurrentState.getState());
    }

    @Override
    public void updateNotInMemOrStaleState(final Jvm jvm, final JvmState state, final String errMsg) {
        // Check again before updating to make sure that nothing has change after pinging the JVM.
        if (!stateNotInMemory(jvm) || isStarted(jvm) || isStopping(jvm) && isStale(jvm)) {
            LOGGER.debug("Updating state of JVM {} ...", jvm.getJvmName());
            updateState(jvm, state, errMsg);
            LOGGER.debug("Updated state of JVM {}!", jvm.getJvmName());
        }
    }

    /**
     * Check if the state in the application context state map
     *
     * @param jvm {@link Jvm}
     * @return true if the state of a certain JVM is in the application context state map.
     */
    protected boolean stateNotInMemory(Jvm jvm) {
        return !inMemoryStateManagerService.containsKey(jvm.getId());
    }

    @Override
    public RemoteCommandReturnInfo getServiceStatus(final Jvm jvm) {
        return jvmCommandFactory.executeCommand(jvm, JvmControlOperation.CHECK_SERVICE_STATUS);
    }

    @Override
    public void updateState(Jvm jvm, final JvmState state) {
        updateState(jvm, state, StringUtils.EMPTY);
    }

    @Override
    public void updateState(final Jvm jvm, final JvmState state, final String errMsg) {
        final Identifier<Jvm> id = jvm.getId();

        lockManager.executeLocked(id, new LockCallback() {
            @Override
            public void doInLock() {
                // If the JVM is already stopped and the new state is stopping, don't do anything!
                // We can't go from stopped to stopping. The stopped state that the JvmControlService issued has
                // the last say for it means that the (windows) service has already stopped.
                LOGGER.debug(">>>>>> JVM {}: {}", jvm.getJvmName(), jvm.getState());
                if (JvmState.JVM_STOPPING.equals(state) && isJvmStateStoppingOrStopped(jvm.getId())) {
                    LOGGER.warn("Ignoring {} state since the JVM is currently stopping/stopped.", state);
                    return;
                }

                CurrentState<Jvm, JvmState> jvmCurrentState = new CurrentState<>(id, state, DateTime.now(), StateType.JVM, errMsg);
                if (isStateChangedAndOrMsgNotEmpty(jvm, state, errMsg)) {
                    LOGGER.debug("Updating Jvm {} state with state = {}, msg = {}.", jvm.getJvmName(), state, errMsg);
                    jvmPersistenceService.updateState(id, state, errMsg);
                    messagingService.send(jvmCurrentState);
                    groupStateNotificationService.retrieveStateAndSend(id, Jvm.class);
                }

                inMemoryStateManagerService.put(id, jvmCurrentState);
            }
        });
    }

    /**
     * Checks if in-memory jvm state is stopping or stopped, if in-memory state is not available it compares it in
     * the state in the persistence context
     * @param id the JVM id
     * @return true if JVM state is stopping or stopped
     */
    private boolean isJvmStateStoppingOrStopped(final Identifier<Jvm> id) {
        final CurrentState<Jvm, JvmState> currentState = inMemoryStateManagerService.get(id);
        final JvmState state = currentState == null ? jvmPersistenceService.getJvm(id).getState() : currentState.getState();
        return JvmState.JVM_STOPPING.equals(state) || JvmState.JVM_STOPPED.equals(state);
    }

    /**
     * Check if the state has changed and-or message is not empty.
     *
     * @param jvm    The jvm
     * @param state  the state
     * @param errMsg error message
     * @return returns true if the state is not the same compared to the previous state or if there's a message (error message)
     */
    protected boolean isStateChangedAndOrMsgNotEmpty(Jvm jvm, final JvmState state, final String errMsg) {
        final Identifier<Jvm> id = jvm.getId();

        CurrentState<Jvm, JvmState> jvmCurrentState = inMemoryStateManagerService.get(id);

        if (jvmCurrentState == null) {
            return false;
        }

        JvmState jvmState = jvmCurrentState.getState();
        final boolean stateChanged = !jvmState.equals(state);

        if (stateChanged) {
            LOGGER.debug("Jvm state for jvm {} changed from {} to {}", jvm.getJvmName(), jvmState, state);
        }

        String jvmMessage = jvmCurrentState.getMessage();
        final boolean msgChanged = !jvmMessage.equals(errMsg);

        if (msgChanged) {
            LOGGER.debug("Jvm message for jvm {} changed from {} to {}", jvm.getJvmName(), jvmState, state);
        }

        return stateChanged || msgChanged;
    }

}