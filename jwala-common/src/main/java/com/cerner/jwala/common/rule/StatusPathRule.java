package com.cerner.jwala.common.rule;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.exception.BadRequestException;

public class StatusPathRule implements Rule {
    private final Path statusPath;

    public StatusPathRule(final Path thePath) {
        statusPath = thePath;
    }

    @Override
    public boolean isValid() {
        return !statusPath.isEmpty() && (statusPath.startsWithHttp() && statusPath.isValidUrl() ||
                statusPath.isValidUri(Path.HTTP, "dummy", 80));
    }

    @Override
    public void validate() throws BadRequestException {
        if (!isValid()) {
            throw new BadRequestException(FaultType.INVALID_STATUS_PATH,
                                          "Invalid status path URL : \"" + statusPath + "\"");
        }
    }
}