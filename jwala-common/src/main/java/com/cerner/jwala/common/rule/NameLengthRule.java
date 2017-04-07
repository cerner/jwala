package com.cerner.jwala.common.rule;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.exception.MessageResponseStatus;

/**
 * Created by SB053052 on 4/7/2017.
 */
public class NameLengthRule implements Rule{
    public static final int MAX_LENGTH=255;
    String name;
     public NameLengthRule(String name){
         this.name=name;
     }

    public boolean isValid() {
        return this.name!=null && this.name.length() <= MAX_LENGTH;
    }

    public void validate() {
        if (!isValid()) {
            throw new BadRequestException(getLongGroupNameStatus(), getLengthMessage());
        }
    }

    protected MessageResponseStatus getLongGroupNameStatus() { return FaultType.NAME_TOO_LONG; }

    protected String getLengthMessage(){ return "Name is too long. Please enter a name lesser than 256 characters";}
}
