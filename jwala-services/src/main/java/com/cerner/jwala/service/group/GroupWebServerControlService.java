package com.cerner.jwala.service.group;

import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.request.webserver.ControlGroupWebServerRequest;

public interface GroupWebServerControlService {

    void controlGroup(final ControlGroupWebServerRequest controlGroupWebServerRequest, final User aUser);

    /**
     * Control all web servers.
     * @param controlGroupWebServerRequest {@link ControlGroupWebServerRequest}
     * @param user the user who's executed this method
     */
    void controlAllWebSevers(ControlGroupWebServerRequest controlGroupWebServerRequest, User user);

}
