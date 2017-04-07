package com.cerner.jwala.common.rule;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.exception.MessageResponseStatus;

/**
 * Created by SB053052 on 4/7/2017.
 */
public class SpecialCharactersRule implements Rule {
    String name;

    public SpecialCharactersRule(String name) {
        this.name = name;
    }

    /**
     * checks for a string with last character space
     * checks for a string with first character space
     * checks that the string only contains alphanumeric+period+dash+underscore+space
     *
     * @return boolean
     */
    @Override
    public boolean isValid() {
        return this.name != null && !this.name.matches(".*[\\s]$") && !this.name.matches("^[\\s].*") &&
                this.name.matches("[A-Za-z0-9._\\s-]+");

    }

    public void validate() {
        if (!isValid()) {
            throw new BadRequestException(getInvalidSpecialCharactersStatus(), getSpecialCharactersMessage());
        }
    }

    protected MessageResponseStatus getInvalidSpecialCharactersStatus() {
        return FaultType.NAME_INVALID_SPECIAL_CHARACTERS;
    }

    protected String getSpecialCharactersMessage() {
        return "Name contains invalid special characters";
    }
}
