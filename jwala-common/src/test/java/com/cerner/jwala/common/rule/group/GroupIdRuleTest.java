package com.cerner.jwala.common.rule.group;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.rule.AbstractIdRuleTest;
import com.cerner.jwala.common.rule.Rule;
import com.cerner.jwala.common.rule.group.GroupIdRule;

public class GroupIdRuleTest extends AbstractIdRuleTest {

    @Override
    protected Rule createValidRule() {
        return new GroupIdRule(new Identifier<Group>(1L));
    }

    @Override
    protected Rule createInvalidRule() {
        return new GroupIdRule(null);
    }
}
