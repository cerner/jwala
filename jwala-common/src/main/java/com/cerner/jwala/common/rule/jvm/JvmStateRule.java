package com.cerner.jwala.common.rule.jvm;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.rule.Rule;

public class JvmStateRule implements Rule {

    private final JvmState jvmState;

    public JvmStateRule(final JvmState theJvmState) {
        jvmState = theJvmState;
    }

    @Override
    public boolean isValid() {
        return jvmState != null;
    }

    @Override
    public void validate() throws BadRequestException {
        if (!isValid()) {
            throw new BadRequestException(FaultType.JVM_STATE_NOT_SPECIFIED,
                                          "A non-null JVM State was not specified");
        }
    }
}
