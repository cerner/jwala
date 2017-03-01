package com.cerner.jwala.service.group;

import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.request.group.ControlGroupRequest;

public interface GroupControlService {

    void controlGroup(ControlGroupRequest controlGroupRequest, User aUser);

    void controlGroups(ControlGroupRequest controlGroupRequest, User user);
}
