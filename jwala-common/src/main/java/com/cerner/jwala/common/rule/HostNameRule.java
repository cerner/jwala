package com.cerner.jwala.common.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.exception.MessageResponseStatus;

import java.net.URI;
import java.net.URISyntaxException;

public class HostNameRule extends ValidNameRule {

    private static final Logger LOGGER = LoggerFactory.getLogger(HostNameRule.class);
    
    public HostNameRule(final String theName) {
        super(theName);
    }

    @Override
    public MessageResponseStatus getMessageResponseStatus() {
        return FaultType.INVALID_HOST_NAME;
    }

    @Override
    public String getMessage() {
        return "Invalid Host Name : \"" + name + "\"";
    }

    @Override
    public boolean isValid() {
        if (super.isValid()) {
            try {
                new URI("http", null, name, 8080, "/somePath", "", "");
            } catch (URISyntaxException e) {
                LOGGER.trace("Failed test for a valid URL", e);
                return false;
            }
            return true;
        }
        return false;
    }

}
