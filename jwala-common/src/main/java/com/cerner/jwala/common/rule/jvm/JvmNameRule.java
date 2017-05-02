package com.cerner.jwala.common.rule.jvm;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.exception.MessageResponseStatus;
import com.cerner.jwala.common.rule.ValidNameRule;

public class JvmNameRule extends ValidNameRule {

    public JvmNameRule(final String theName) {
        super(theName);
    }

    @Override
    protected MessageResponseStatus getMessageResponseStatus() {
        return FaultType.INVALID_JVM_NAME;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && name.length() < 65;
    }

    @Override
    protected String getMessage() {
        return name != null && name.length() > 64 ? "Length of JVM should not exceed 64 characters" : "Invalid Jvm Name : \"" + name + "\"";
    }

}
