package com.cerner.jwala.common.domain.model.webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cerner.jwala.common.domain.model.state.OperationalState;

import java.util.HashMap;
import java.util.Map;

public enum WebServerReachableState implements OperationalState {

    WS_NEW("NEW"),
    WS_REACHABLE("STARTED"),
    WS_UNREACHABLE("STOPPED"),
    WS_UNEXPECTED_STATE("UNEXPECTED STATE"),
    WS_START_SENT("START SENT"),
    WS_STOP_SENT("STOP SENT"),
    WS_FAILED("FAILED"),
    FORCED_STOPPED("FORCED STOP");

    private static final Logger LOGGER = LoggerFactory.getLogger(WebServerReachableState.class);
    private static final Map<String, WebServerReachableState> LOOKUP_MAP = new HashMap<>(values().length);

    static {
        for (final WebServerReachableState state : values()) {
            LOOKUP_MAP.put(state.toPersistentString(), state);
        }
    }

    public static WebServerReachableState convertFrom(final String state) {
        if (LOOKUP_MAP.containsKey(state)) {
            return LOOKUP_MAP.get(state);
        }
        LOGGER.error("Unexpected webserver state:{0} from db! Returning WS_UNEXPECTED_STATE.", state);
        return WS_UNEXPECTED_STATE;
    }

    private final String externalName;

    WebServerReachableState(final String theExternalName) {
        externalName = theExternalName;
    }

    @Override
    public String toStateLabel() {
        return externalName;
    }

    @Override
    public String toPersistentString() {
        return name();
    }
}
