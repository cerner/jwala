package com.cerner.jwala.common.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.exception.BadRequestException;

import java.net.URI;
import java.net.URISyntaxException;

public class StatusPathRule implements Rule {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatusPathRule.class);
    
    private final Path statusPath;

    public StatusPathRule(final Path thePath) {
        statusPath = thePath;
    }

    @Override
    public boolean isValid() {
        if (statusPath != null && statusPath.isAbsolute()) {
            try {
                new URI("http", null, "hostName", 8080, statusPath.getPath(), "", "");
            } catch (URISyntaxException e) {
                LOGGER.trace("Failed test for a valid status path", e);
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public void validate() throws BadRequestException {
        if (!isValid()) {
            throw new BadRequestException(FaultType.INVALID_STATUS_PATH,
                                          "Invalid status path URL : \"" + statusPath + "\"");
        }
    }
}