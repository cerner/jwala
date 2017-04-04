package com.cerner.jwala.common.rule.group;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.exception.BadRequestException;
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


    protected MessageResponseStatus getMessageResponseGroupLengthStatus() { return FaultType.GROUP_NAME_TOO_LONG; }

    @Override
    protected String getMessage() {
        return "Invalid Group Name: \"" + name + "\"";
    }

    protected String getMessageGroupLength(){ return "Group name is too long. Please enter a Group name lesser than 256 characters";}

    @Override
    public void validate() throws BadRequestException {
        if (!isValid()) {
            throw new BadRequestException(getMessageResponseStatus(),
                    getMessage());
        }
        if(!isValidLength()){
            throw new BadRequestException(getMessageResponseGroupLengthStatus(),getMessageGroupLength());
        }
    }


    /**
     * Checks for null
     * Checks for an empty string +only spaces
     * checks for a string with last character space
     * checks for a string with first character space
     * checks that the string only contains alphanumeric+period+dash+underscore+space
     *
     * @return boolean
     */
    @Override
    public boolean isValid() {return name != null && !"".equals(name.trim()) && !name.matches(".*[\\s]$") && !name.matches("^[\\s].*") && name.matches("[A-Za-z0-9._\\s-]+");
    }

    public boolean isValidLength(){return name.length()<256;}
}
