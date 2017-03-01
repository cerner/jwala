package com.cerner.jwala.common.rule.jvm;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.rule.AbstractIdRuleTest;
import com.cerner.jwala.common.rule.Rule;
import com.cerner.jwala.common.rule.jvm.JvmIdRule;

public class JvmIdRuleTest extends AbstractIdRuleTest {

    @Override
    protected Rule createValidRule() {
        return new JvmIdRule(new Identifier<Jvm>(1L));
    }

    @Override
    protected Rule createInvalidRule() {
        return new JvmIdRule(null);
    }
}
