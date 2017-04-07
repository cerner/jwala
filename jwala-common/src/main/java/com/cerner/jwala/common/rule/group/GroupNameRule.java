package com.cerner.jwala.common.rule.group;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.exception.MessageResponseStatus;
import com.cerner.jwala.common.rule.Rule;
import com.cerner.jwala.common.rule.ValidNameRule;

public class GroupNameRule extends ValidNameRule {
    public GroupNameRule(final String theName) {
        super(theName);
    }

    @Override
    protected MessageResponseStatus getMessageResponseStatus() {
        return FaultType.INVALID_GROUP_NAME;
    }


    @Override
    protected String getMessage() {
        return "Invalid Group Name: \"" + name + "\"";
    }

    @Override
    public void validate() throws BadRequestException {
        if (!isValid()) {
            throw new BadRequestException(getMessageResponseStatus(),
                    getMessage());
        }
    }

}
