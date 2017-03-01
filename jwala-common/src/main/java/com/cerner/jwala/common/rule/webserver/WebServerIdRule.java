package com.cerner.jwala.common.rule.webserver;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.rule.Rule;

public class WebServerIdRule implements Rule {

    private final Identifier<WebServer> webServerId;

    public WebServerIdRule(final Identifier<WebServer> theId) {
        webServerId = theId;
    }

    @Override
    public boolean isValid() {
        return webServerId != null;
    }

    @Override
    public void validate() throws BadRequestException {
        if (!isValid()) {
            throw new BadRequestException(FaultType.WEBSERVER_NOT_SPECIFIED,
                                          "WebServer Id was not specified");
        }
    }
}
