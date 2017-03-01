package com.cerner.jwala.common.request.group;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.group.GroupControlOperation;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.rule.group.GroupIdRule;

import java.io.Serializable;

public class ControlGroupRequest implements Serializable, GroupRequest {

    private final Identifier<Group> groupId;
    private final GroupControlOperation controlOperation;

    public ControlGroupRequest(final Identifier<Group> theId,
                               final GroupControlOperation theControlOperation) {
        groupId = theId;
        controlOperation = theControlOperation;
    }

    public Identifier<Group> getGroupId() {
        return groupId;
    }

    public GroupControlOperation getControlOperation() {
        return controlOperation;
    }

    
    @Override
    public void validate() {
        // can only partially validate without state
        new GroupIdRule(groupId).validate();
    }

    @Override
    public String getType() {
        return "Group";
    }
    
    @Override
    public Long getId() {
        return groupId.getId();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        ControlGroupRequest rhs = (ControlGroupRequest) obj;
        return new EqualsBuilder()
                .append(this.groupId, rhs.groupId)
                .append(this.controlOperation, rhs.controlOperation)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(groupId)
                .append(controlOperation)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("groupId", groupId)
                .append("controlOperation", controlOperation)
                .toString();
    }

}
