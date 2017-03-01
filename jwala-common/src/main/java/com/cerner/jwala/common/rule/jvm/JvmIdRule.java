package com.cerner.jwala.common.rule.jvm;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.rule.Rule;
import com.cerner.jwala.common.rule.identifier.IdentifierRule;

public class JvmIdRule extends IdentifierRule<Jvm> implements Rule {

    public JvmIdRule(final Identifier<Jvm> theId) {
        super(theId,
              FaultType.JVM_NOT_SPECIFIED,
              "JVM Id was not specified");
    }
}
