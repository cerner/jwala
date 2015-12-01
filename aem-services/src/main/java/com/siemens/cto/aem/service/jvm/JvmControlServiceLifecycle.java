package com.siemens.cto.aem.service.jvm;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.request.jvm.ControlJvmRequest;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.user.User;

public interface JvmControlServiceLifecycle {

    /**
     * Set state, return previous state.
     */
    CurrentState<Jvm, JvmState> startState(final ControlJvmRequest aCommand,
                    final User aUser);

    /**
     * Set state to previous state; no message.
     */
    void revertState(CurrentState<Jvm, JvmState> aJvmState,
                        final User aUser);

    void startStateWithMessage(final Identifier<Jvm> aJvmId,
                               final JvmState aJvmState,
                               final String aMessage,
                               final User aUser);


    void notifyMessageOnly(Identifier<Jvm> jvmId, String result, User aUser);

    /* Set completed state to confirmed state, no message. */
    void completeState(ControlJvmRequest aCommand, User aUser);
}