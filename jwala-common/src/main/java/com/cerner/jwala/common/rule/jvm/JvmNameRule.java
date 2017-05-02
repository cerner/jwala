package com.cerner.jwala.common.rule.jvm;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.exception.MessageResponseStatus;
import com.cerner.jwala.common.rule.ValidNameRule;

public class JvmNameRule extends ValidNameRule {

    public static final int MAX_LENGTH = 64;

    public JvmNameRule(final String theName) {
        super(theName);
    }

    @Override
    protected MessageResponseStatus getMessageResponseStatus() {
        return FaultType.INVALID_JVM_NAME;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && name.length() <= MAX_LENGTH;
    }

    @Override
    protected String getMessage() {
        return name != null && name.length() > MAX_LENGTH ? "Length of JVM should not exceed "+ MAX_LENGTH+ " characters" : "Invalid Jvm Name : \"" + name + "\"";
    }

}
