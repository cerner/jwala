package com.cerner.jwala.service.group;

import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.request.group.ControlGroupJvmRequest;

public interface GroupJvmControlService {

    void controlGroup(final ControlGroupJvmRequest controlGroupJvmRequest, final User aUser);
    
    /**
     * Control all JVMs.
     * @param controlGroupJvmRequest {@link ControlGroupJvmRequest}
     * @param user the user who's executing this method
     */
    void controlAllJvms(ControlGroupJvmRequest controlGroupJvmRequest, User user);
}
