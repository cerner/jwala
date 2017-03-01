package com.cerner.jwala.common.request.jvm;

import java.util.HashSet;
import java.util.Set;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.request.group.AddJvmToGroupRequest;

public class AddJvmToGroupCommandSetBuilder {

    private Identifier<Jvm> jvmId;
    private Set<Identifier<Group>> groupIds;

    public AddJvmToGroupCommandSetBuilder() {
    }

    public AddJvmToGroupCommandSetBuilder(final Identifier<Jvm> aJvmId,
                                          final Set<Identifier<Group>> someGroupIds) {
        jvmId = aJvmId;
        groupIds = someGroupIds;
    }

    public AddJvmToGroupCommandSetBuilder setJvmId(final Identifier<Jvm> aJvmId) {
        jvmId = aJvmId;
        return this;
    }

    public AddJvmToGroupCommandSetBuilder setGroupIds(final Set<Identifier<Group>> someGroupIds) {
        groupIds = someGroupIds;
        return this;
    }

    public Set<AddJvmToGroupRequest> build() {
        final Set<AddJvmToGroupRequest> commands = new HashSet<>();
        for (final Identifier<Group> groupId : groupIds) {
            commands.add(new AddJvmToGroupRequest(groupId,
                                                  jvmId));
        }
        return commands;
    }
}
