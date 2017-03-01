package com.cerner.jwala.persistence.service;

import com.cerner.jwala.common.domain.model.group.*;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.request.group.AddJvmToGroupRequest;
import com.cerner.jwala.common.request.group.CreateGroupRequest;
import com.cerner.jwala.common.request.group.RemoveJvmFromGroupRequest;
import com.cerner.jwala.common.request.group.UpdateGroupRequest;
import com.cerner.jwala.persistence.service.GroupPersistenceService;

public class CommonGroupPersistenceServiceBehavior {

    private final GroupPersistenceService groupPersistenceService;

    public CommonGroupPersistenceServiceBehavior(final GroupPersistenceService theGroupService) {
        groupPersistenceService = theGroupService;
    }

    public Group createGroup(final String aGroupName, final String aUserId) {
        return groupPersistenceService.createGroup(new CreateGroupRequest(aGroupName));
    }

    public Group updateGroup(final Identifier<Group> aGroupId,
                                final String aNewGroupName,
                                final String aUserId) {

        return groupPersistenceService.updateGroup(new UpdateGroupRequest(aGroupId, aNewGroupName));
    }

    public void addJvmToGroup(final Identifier<Group> aGroupId,
                              final Identifier<Jvm> aJvmId,
                              final String aUserId) {

        groupPersistenceService.addJvmToGroup(new AddJvmToGroupRequest(aGroupId, aJvmId));
    }

    public void removeJvmFromGroup(final Identifier<Group> aGroupId,
                                      final Identifier<Jvm> aJvmId,
                                      final String aUserId) {

        groupPersistenceService.removeJvmFromGroup(new RemoveJvmFromGroupRequest(aGroupId, aJvmId));
    }

}
