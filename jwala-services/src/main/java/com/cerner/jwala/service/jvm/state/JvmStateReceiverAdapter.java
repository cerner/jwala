package com.cerner.jwala.service.jvm.state;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.service.jvm.JvmStateService;
import org.apache.catalina.LifecycleState;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.NoResultException;
import java.util.HashMap;
import java.util.Map;

import static org.apache.cxf.interceptor.LoggingMessage.ID_KEY;

/**
 * The listener for JGroup messages
 */
public class JvmStateReceiverAdapter extends ReceiverAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmStateReceiverAdapter.class);
    public static final String STATE_KEY = "STATE";
    public static final String NAME_KEY = "NAME";

    private final JvmStateService jvmStateService;
    private final JvmPersistenceService jvmPersistenceService;

    private final static Map<LifecycleState, JvmState> LIFECYCLE_JWALA_JVM_STATE_REF_MAP = new HashMap<>();

    static {
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.DESTROYED, JvmState.JVM_STOPPED);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.DESTROYING, JvmState.JVM_STOPPING);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.FAILED, JvmState.JVM_FAILED);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.INITIALIZED, JvmState.JVM_INITIALIZED);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.INITIALIZING, JvmState.JVM_INITIALIZED);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.MUST_DESTROY, JvmState.JVM_STOPPING);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.MUST_STOP, JvmState.JVM_STOPPING);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.NEW, JvmState.JVM_INITIALIZED);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.STARTED, JvmState.JVM_STARTED);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.STARTING, JvmState.JVM_STARTING);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.STARTING_PREP, JvmState.JVM_STARTING);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.STOPPED, JvmState.JVM_STOPPED);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.STOPPING, JvmState.JVM_STOPPING);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.STOPPING_PREP, JvmState.JVM_STOPPING);
    }

    public JvmStateReceiverAdapter(final JvmStateService jvmStateService, final JvmPersistenceService jvmPersistenceService) {
        this.jvmStateService = jvmStateService;
        this.jvmPersistenceService = jvmPersistenceService;
    }

    @Override
    public void receive(final Message jGroupMsg) {
        final Map serverInfoMap = (Map) jGroupMsg.getObject();
        final Jvm jvm = getJvm(serverInfoMap);
        final JvmState jvmState = LIFECYCLE_JWALA_JVM_STATE_REF_MAP.get(serverInfoMap.get(STATE_KEY));

        if (jvm != null && !JvmState.JVM_STOPPED.equals(jvmState)) {
            jvmStateService.updateState(jvm, jvmState, StringUtils.EMPTY);
        } else if (jvm == null) {
            LOGGER.error("Cannot update the state since no JVM was found with the following details: {}", serverInfoMap);
        }
    }

    /**
     * Get the JVM with parameters provided in a map
     * @param serverInfoMap the map that contains the JVM id or name
     * @return {@link Jvm}
     */
    private Jvm getJvm(final Map serverInfoMap) {
        final String id = serverInfoMap.get(ID_KEY) instanceof String ? (String) serverInfoMap.get(ID_KEY) : null;
        if (id != null && NumberUtils.isNumber(id)) {
            return getJvmById(Long.parseLong(id));
        }
        // try to get the JVM by name instead
        return serverInfoMap.get(NAME_KEY) instanceof String ? getJvmByName((String) serverInfoMap.get(NAME_KEY)) : null;
    }

    /**
     * Get the JVM id
     * @param name the JVM name
     * @return the id
     */
    private Jvm getJvmByName(final String name) {
        LOGGER.debug("Retrieving JVM id with name = {}...", name);
        try {
            return jvmPersistenceService.findJvmByExactName(name);
        } catch (final NoResultException e) {
            LOGGER.error("Received a notification from a jvm named {} but Jwala doesn't know about a Jvm " +
                    "with that name!!!  ", name, e);
        }
        return null;
    }

    private Jvm getJvmById(final long id) {
        LOGGER.debug("Retrieving JVM id with id = {}...", id);
        try {
            return jvmPersistenceService.getJvm(new Identifier<Jvm>(id));
        } catch (NoResultException e) {
            LOGGER.error("Received a notification from a jvm named {} but Jwala doesn't know about a Jvm " +
                    "with that name!!!  ", id, e);
        }
        return null;
    }

    @Override
    public void viewAccepted(View view) {
        LOGGER.debug("JGroups coordinator cluster VIEW: {}", view.toString());
    }
}
