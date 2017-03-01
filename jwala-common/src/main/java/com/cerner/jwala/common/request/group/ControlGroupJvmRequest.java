package com.cerner.jwala.common.request.group;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;
import com.cerner.jwala.common.rule.group.GroupIdRule;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public class ControlGroupJvmRequest implements Serializable, GroupRequest {

    private final Identifier<Group> groupId;
    private final JvmControlOperation controlOperation;

    public ControlGroupJvmRequest(final Identifier<Group> theId,
                                  final JvmControlOperation theControlOperation) {
        groupId = theId;
        controlOperation = theControlOperation;
    }

    public Identifier<Group> getGroupId() {
        return groupId;
    }

    public JvmControlOperation getControlOperation() {
        return controlOperation;
    }

    @Override
    public void validate() {
        new GroupIdRule(groupId).validate();
    }

    @Override
    public String getType() {
        return "GroupJvm";
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
        ControlGroupJvmRequest rhs = (ControlGroupJvmRequest) obj;
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

    @Override
    public Long getId() {
        // TODO Auto-generated method stub
        return null;
    }

}
