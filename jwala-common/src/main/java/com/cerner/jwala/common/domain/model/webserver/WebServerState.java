package com.cerner.jwala.common.domain.model.webserver;

import org.joda.time.DateTime;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.domain.model.state.StateType;

public class WebServerState extends CurrentState<WebServer, WebServerReachableState> {

    public WebServerState(final Identifier<WebServer> theId,
                          final WebServerReachableState theState,
                          final DateTime theAsOf) {
        super(theId,
              theState,
              theAsOf,
              StateType.WEB_SERVER);
    }
}
