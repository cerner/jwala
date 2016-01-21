package com.siemens.cto.aem.service.state.impl;

import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.OperationalState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.request.state.SetStateRequest;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.persistence.service.StatePersistenceService;
import com.siemens.cto.aem.service.spring.component.GrpStateComputationAndNotificationSvc;
import com.siemens.cto.aem.service.state.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

public abstract class StateServiceImpl<S, T extends OperationalState> implements StateService<S, T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateServiceImpl.class);

    private final StatePersistenceService<S, T> persistenceService;
    private final StateNotificationService notificationService;
    private final StateType stateType;
    private final GrpStateComputationAndNotificationSvc grpStateComputationAndNotificationSvc;

    public StateServiceImpl(final StatePersistenceService<S, T> thePersistenceService,
                            final StateNotificationService theNotificationService, final StateType theStateType,
                            final GrpStateComputationAndNotificationSvc grpStateComputationAndNotificationSvc) {
        persistenceService = thePersistenceService;
        notificationService = theNotificationService;
        stateType = theStateType;
        this.grpStateComputationAndNotificationSvc = grpStateComputationAndNotificationSvc;
    }

    @Override
    @Transactional
    public CurrentState<S, T> setCurrentState(final SetStateRequest<S, T> setStateRequest, final User aUser) {
        LOGGER.trace("Attempting to set state for {} {} ", stateType, setStateRequest);
        setStateRequest.validate();

        final CurrentState<S, T> currentState = persistenceService.getState(setStateRequest.getNewState().getId());
        if (currentState == null || !currentState.getState().equals(setStateRequest.getNewState().getState()) ||
            !currentState.getMessage().equalsIgnoreCase(setStateRequest.getNewState().getMessage())) {
            final CurrentState<S, T> updatedState = persistenceService.updateState(setStateRequest);
            updatedState.setUserId(aUser.getId());
            notificationService.notifyStateUpdated(updatedState);
            grpStateComputationAndNotificationSvc.computeAndNotify(updatedState.getId(), updatedState.getState());
            return updatedState;
        }
        return currentState;
    }

    @Override
    @Transactional(readOnly = true)
    public CurrentState<S, T> getCurrentState(final Identifier<S> anId) {
        LOGGER.trace("Getting state for {} {}", stateType, anId);
        CurrentState<S, T> state = persistenceService.getState(anId);
        if (state == null) {
            state = createUnknown(anId);
        }
        return state;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<CurrentState<S, T>> getCurrentStates(final Set<Identifier<S>> someIds) {
        LOGGER.trace("Getting states for {} {}", stateType, someIds);
        final Set<CurrentState<S, T>> results = new HashSet<>();
        for (final Identifier<S> id : someIds) {
            final CurrentState<S, T> currentState = getCurrentState(id);
            results.add(currentState);
        }
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<CurrentState<S, T>> getCurrentStates() {
        LOGGER.trace("Getting all states for {}", stateType);
        return persistenceService.getAllKnownStates();
    }

    /**
     * Accessor for derived class.
     */
    protected StateNotificationService getNotificationService() {
        return notificationService;
    }

    protected abstract CurrentState<S, T> createUnknown(final Identifier<S> anId);

}
