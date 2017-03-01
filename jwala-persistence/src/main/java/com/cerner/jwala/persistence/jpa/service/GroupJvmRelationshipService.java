package com.cerner.jwala.persistence.jpa.service;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.request.group.AddJvmToGroupRequest;
import com.cerner.jwala.common.request.group.RemoveJvmFromGroupRequest;
import com.cerner.jwala.common.request.jvm.UploadJvmTemplateRequest;
import com.cerner.jwala.persistence.jpa.domain.JpaGroup;

import java.util.List;

public interface GroupJvmRelationshipService extends CrudService<JpaGroup> {

    void addJvmToGroup(AddJvmToGroupRequest addJvmToGroupRequest);

    void removeJvmFromGroup(RemoveJvmFromGroupRequest removeJvmFromGroupRequest);

    void removeRelationshipsForGroup(final Identifier<Group> aGroupId);

    void removeRelationshipsForJvm(final Identifier<Jvm> aJvmId);

    void populateJvmConfig(List<UploadJvmTemplateRequest> uploadJvmTemplateCommands, User user, boolean overwriteExisting);

    List<Group> findGroupsByJvm(Identifier<Jvm> id);
}
