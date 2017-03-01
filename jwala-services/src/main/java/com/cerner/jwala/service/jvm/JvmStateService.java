package com.cerner.jwala.service.jvm;

import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;

/**
 * The contract for a JVM state related services.
 *
 * Created by Jedd Cuison on 3/22/2016.
 */
public interface JvmStateService {

    /**
     * Check all JVM states individually then update the state persistence and in-memory context when there's a state change.
     */
    void verifyAndUpdateNotInMemOrStaleStates();

    /**
     * Set persistence context and in-memory state of a JVM whose state is not yet in the application context JVM state
     * map (in-memory) or whose state is in started but is stale.
     * @param jvm the {@link Jvm} object.
     * @param state {@link JvmState}
     * @param errMsg the error message.
     */
    void updateNotInMemOrStaleState(final Jvm jvm, final JvmState state, final String errMsg);

    /**
     * Retrieve the status of a JVM which is running as a window's service.
     * @param jvm {@link Jvm}
     */
    RemoteCommandReturnInfo getServiceStatus(final Jvm jvm);

    /**
     * Update JVM state.
     * @param jvm the JVM.
     * @param state {@link JvmState}
     */
    void updateState(Jvm jvm, JvmState state);

    /**
     * Update JVM state.
     * @param jvm The jvm
     * @param state {@link JvmState}
     * @param errMsg the error message
     */
    void updateState(Jvm jvm, JvmState state, String errMsg);

}
