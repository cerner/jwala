package com.cerner.jwala.common.rule.group;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.exception.MessageResponseStatus;
import com.cerner.jwala.common.rule.Rule;
import com.cerner.jwala.common.rule.ValidNameRule;

public class GroupNameRule extends ValidNameRule implements Rule {

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

    /**
     * 1)Checks for null
     2)Checks for an empty string +only spaces
     3)checks for a string with last character space
     4)checks for a string with first character space
     5) checks that the string only contains alphanumeric+period+dash+underscore+space
     * @return boolean
     */
    @Override
    public boolean isValid() {return name != null && !"".equals(name.trim()) && !name.matches(".*[\\s]$") && !name.matches("^[\\s].*") && name.matches("[A-Za-z0-9._\\s-]+");
    }
}
