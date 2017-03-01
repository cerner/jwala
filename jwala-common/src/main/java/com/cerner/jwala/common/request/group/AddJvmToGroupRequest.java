package com.cerner.jwala.common.request.group;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.request.Request;

public class AddJvmToGroupRequest extends AbstractJvmGroupRequest implements Request {

    public AddJvmToGroupRequest(final Identifier<Group> theGroupId,
                                final Identifier<Jvm> theJvmId) {
        super(theGroupId,
              theJvmId);
    }
}
