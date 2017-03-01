package com.cerner.jwala.common.rule.jvm;

import java.util.Set;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.rule.Rule;
import com.cerner.jwala.common.rule.identifier.MultipleIdentifiersRule;

public class JvmIdsRule extends MultipleIdentifiersRule<Jvm> implements Rule {

    public JvmIdsRule(final Set<Identifier<Jvm>> theIds) {
        super(theIds);
    }

    @Override
    protected Rule createRule(final Identifier<Jvm> anId) {
        return new JvmIdRule(anId);
    }
}
