package com.cerner.jwala.common.rule.group;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.rule.Rule;
import com.cerner.jwala.common.rule.identifier.IdentifierRule;

public class GroupIdRule extends IdentifierRule<Group> implements Rule {

    public GroupIdRule(final Identifier<Group> theId) {
        super(theId,
              FaultType.GROUP_NOT_SPECIFIED,
              "Group Id was not specified");
    }
}
