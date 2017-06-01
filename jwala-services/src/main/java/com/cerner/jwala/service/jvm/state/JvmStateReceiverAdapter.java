package com.cerner.jwala.service.jvm.state;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.service.jvm.JvmStateService;
import org.apache.catalina.LifecycleState;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.NoResultException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The listener for JGroup messages
 */
public class JvmStateReceiverAdapter extends ReceiverAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmStateReceiverAdapter.class);
    static final String STATE_KEY = "STATE";
    static final String NAME_KEY = "NAME";
    private static final String ID_KEY = "ID";

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
        final JvmState jvmState = getJvmState(serverInfoMap);

        if (jvm != null && !JvmState.JVM_STOPPED.equals(jvmState)) {
            jvmStateService.updateState(jvm, jvmState, StringUtils.EMPTY);
        } else if (jvm == null) {
            LOGGER.error("Cannot update the state since no JVM was found with the following details: {}", serverInfoMap);
        }
    }

    private JvmState getJvmState(final Map serverInfoMap) {

        // first check if the key types are String to support the latest version
        final Set keys = serverInfoMap.keySet();
        if (CollectionUtils.isEmpty(keys)) {
            return null;
        }

        if (serverInfoMap.containsKey(STATE_KEY)) {
            // the latest version sends the Tomcat Lifecycle State
            // so we need to convert the Tomcat Lifecycle State to the JvmState
            return LIFECYCLE_JWALA_JVM_STATE_REF_MAP.get(serverInfoMap.get(STATE_KEY));
        }

        // assume the message is from the initial version of the Jvm state reporter, in which case the value is already returned as a string JvmState
        try {
            final Object initialKey = keys.iterator().next();
            final Field idKey = initialKey.getClass().getDeclaredField(STATE_KEY);
            final String jvmStateString = (String) serverInfoMap.get(idKey.get(initialKey));
            return JvmState.valueOf(jvmStateString);
        } catch (NoSuchFieldException e) {
            LOGGER.error("Failed to find STATE key as ReportingJmsMessageKey in message: {}", serverInfoMap, e);
            return null;
        } catch (IllegalAccessException e) {
            LOGGER.error("Failed to convert STATE key to ReportingJmsMessageKey in message: {}", serverInfoMap, e);
            return null;
        }

    }

    /**
     * Get the JVM with parameters provided in a map
     * @param serverInfoMap the map that contains the JVM id or name
     * @return {@link Jvm}
     */
    private Jvm getJvm(final Map serverInfoMap) {
        final String id = getStringFromMessageMap(serverInfoMap, ID_KEY);
        if (id != null && NumberUtils.isNumber(id)) {
            return getJvmById(Long.parseLong(id));
        }
        // try to find the JVM by name instead
        return getJvmByName(getStringFromMessageMap(serverInfoMap, NAME_KEY));
    }

    private String getStringFromMessageMap(final Map serverInfoMap, final String key) {

        // check for a String key first to support the latest version
        final Set keys = serverInfoMap.keySet();
        if (CollectionUtils.isEmpty(keys)) {
            return null;
        }

        if (serverInfoMap.containsKey(key)) {
            return (String) serverInfoMap.get(key);
        }

        // assume the message is from the initial version of the JVM state reporter
        try {
            final Object initialKey = keys.iterator().next();
            final Field idKey = initialKey.getClass().getDeclaredField(key);
            return (String) serverInfoMap.get(idKey.get(initialKey));
        } catch (NoSuchFieldException e) {
            LOGGER.error("Failed to find key {} as ReportingJmsMessageKey in message: {}", key, serverInfoMap, e);
            return null;
        } catch (IllegalAccessException e) {
            LOGGER.error("Failed to convert {} key to ReportingJmsMessageKey in message: {}", key, serverInfoMap, e);
            return null;
        }
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
