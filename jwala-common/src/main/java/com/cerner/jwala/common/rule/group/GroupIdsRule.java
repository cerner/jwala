package com.cerner.jwala.common.rule.group;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.rule.Rule;
import com.cerner.jwala.common.rule.identifier.MultipleIdentifiersRule;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class GroupIdsRule extends MultipleIdentifiersRule<Group> implements Rule {

    public GroupIdsRule(final Collection<Identifier<Group>> theGroupIds) {
        this(new HashSet<>(theGroupIds));
    }

    public GroupIdsRule(final Set<Identifier<Group>> theGroupIds) {
        super(theGroupIds);
        if (theGroupIds.isEmpty()){
            rules.add(createRule(null));
        }
    }

    @Override
    protected Rule createRule(final Identifier<Group> anId) {
        return new GroupIdRule(anId);
    }
}
