package com.cerner.jwala.common.domain.model.state;

import com.cerner.jwala.common.domain.model.state.message.StateKey;

public interface KeyValueStateConsumer {

    void set(final StateKey aKey,
             final String aValue);
}
