package com.cerner.jwala.common.domain.model.state;

import java.util.Comparator;

/**
 * Describes a state that can be described as "operational".
 */
public interface OperationalState {

    class OperationalStateComparator implements Comparator<OperationalState> {

        @Override
        public int compare(final OperationalState state1, final OperationalState state2) {
            return state1.toPersistentString().compareTo(state2.toPersistentString());
        }
        
    }

    String toStateLabel();

    // TODO: Refactor method name when the state is saved to web server already since web server will be using state label e.g. STOPPED instead of the state name itself e.g. WEBSERVER_UNREACHABLE.
    String toPersistentString();

}
