package com.cerner.jwala.common.rule;

import com.cerner.jwala.common.exception.BadRequestException;

public interface Rule {

    boolean isValid();

    void validate() throws BadRequestException;

}
