package com.cerner.jwala.persistence.jpa.service.impl;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.request.group.AddJvmToGroupRequest;
import com.cerner.jwala.common.request.group.RemoveJvmFromGroupRequest;
import com.cerner.jwala.common.request.jvm.UploadJvmTemplateRequest;
import com.cerner.jwala.persistence.jpa.domain.JpaGroup;
import com.cerner.jwala.persistence.jpa.domain.JpaJvm;
import com.cerner.jwala.persistence.jpa.domain.builder.JpaGroupBuilder;
import com.cerner.jwala.persistence.jpa.service.CrudService;
import com.cerner.jwala.persistence.jpa.service.GroupCrudService;
import com.cerner.jwala.persistence.jpa.service.GroupJvmRelationshipService;
import com.cerner.jwala.persistence.jpa.service.JvmCrudService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GroupJvmRelationshipServiceImpl extends AbstractCrudServiceImpl<JpaGroup> implements GroupJvmRelationshipService, CrudService<JpaGroup> {

    private final GroupCrudService groupCrudService;
    private final JvmCrudService jvmCrudService;

    public GroupJvmRelationshipServiceImpl(final GroupCrudService theGroupCrudService,
                                           final JvmCrudService theJvmCrudService) {
        groupCrudService = theGroupCrudService;
        jvmCrudService = theJvmCrudService;
    }

    @Override
    public void addJvmToGroup(AddJvmToGroupRequest addJvmToGroupRequest) {

        final JpaGroup group = groupCrudService.getGroup(addJvmToGroupRequest.getGroupId());
        final JpaJvm jvm = jvmCrudService.getJvm(addJvmToGroupRequest.getJvmId());

        final List<JpaJvm> jvms;
        if (group.getJvms() != null) {
            jvms = group.getJvms();
        } else {
            jvms = new ArrayList<>();
            group.setJvms(jvms);
        }
        jvms.add(jvm);

        final List<JpaGroup> groups;
        if (jvm.getGroups() != null) {
            groups = jvm.getGroups();
        } else {
            groups = new ArrayList<>();
            jvm.setGroups(groups);
        }
        groups.add(group);

        entityManager.flush();
    }

    @Override
    public void removeJvmFromGroup(final RemoveJvmFromGroupRequest removeJvmFromGroupRequest) {

        final JpaGroup group = groupCrudService.getGroup(removeJvmFromGroupRequest.getGroupId());
        final JpaJvm jvm = jvmCrudService.getJvm(removeJvmFromGroupRequest.getJvmId());

        if (group.getJvms() != null) {
            final List<JpaJvm> jvms = group.getJvms();
            jvms.remove(jvm);
            if (jvm.getGroups() != null) {
                jvm.getGroups().remove(group);
            }
        }

        entityManager.flush();
    }

    @Override
    public void removeRelationshipsForGroup(final Identifier<Group> aGroupId) {

        final JpaGroup group = groupCrudService.getGroup(aGroupId);

        if (group.getJvms() != null) {
            final Iterator<JpaJvm> jvms = group.getJvms().iterator();
            while (jvms.hasNext()) {
                final JpaJvm jvm = jvms.next();
                if (jvm.getGroups() != null) {
                    jvm.getGroups().remove(group);
                }
                jvms.remove();
            }
        }

        entityManager.flush();
    }

    @Override
    public void removeRelationshipsForJvm(final Identifier<Jvm> aJvmId) {

        final JpaJvm jvm = jvmCrudService.getJvm(aJvmId);

        if (jvm.getGroups() != null) {
            final Iterator<JpaGroup> groups = jvm.getGroups().iterator();
            while (groups.hasNext()) {
                final JpaGroup group = groups.next();
                if (group.getJvms() != null) {
                    group.getJvms().remove(jvm);
                }
                groups.remove();
            }
        }

        entityManager.flush();
    }

    @Override
    public List<Group> findGroupsByJvm(Identifier<Jvm> id) {
        JpaJvm jpaJvm = jvmCrudService.getJvm(id);
        final List<JpaGroup> jpaGroups = jpaJvm.getGroups();
        List<Group> groupList = new ArrayList<>();
        for (JpaGroup jpaGroup : jpaGroups){
            groupList.add(new JpaGroupBuilder(jpaGroup).build());
        }
        return groupList;
    }

    @Override
    public void populateJvmConfig(List<UploadJvmTemplateRequest> uploadJvmTemplateCommands, User user, boolean overwriteExisting) {
        for (UploadJvmTemplateRequest uploadJvmTemplateRequest: uploadJvmTemplateCommands) {
            if (overwriteExisting || jvmCrudService.getJvmTemplate(uploadJvmTemplateRequest.getConfFileName(), uploadJvmTemplateRequest.getJvm().getId()).isEmpty()){
                jvmCrudService.uploadJvmConfigTemplate(uploadJvmTemplateRequest);
            }
        }
    }

}
