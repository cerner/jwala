package com.cerner.jwala.common.request.group;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.request.Request;
import com.cerner.jwala.common.rule.MultipleRules;
import com.cerner.jwala.common.rule.group.GroupIdRule;
import com.cerner.jwala.common.rule.jvm.JvmIdRule;

public abstract class AbstractJvmGroupRequest implements Request {

    private final Identifier<Group> groupId;
    private final Identifier<Jvm> jvmId;

    public AbstractJvmGroupRequest(final Identifier<Group> theGroupId,
                                   final Identifier<Jvm> theJvmId) {
        groupId = theGroupId;
        jvmId = theJvmId;
    }

    public Identifier<Group> getGroupId() {
        return groupId;
    }

    public Identifier<Jvm> getJvmId() {
        return jvmId;
    }

    @Override
    public void validate() throws BadRequestException {
        new MultipleRules(new GroupIdRule(groupId),
                                new JvmIdRule(jvmId)).validate();
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
        AbstractJvmGroupRequest rhs = (AbstractJvmGroupRequest) obj;
        return new EqualsBuilder()
                .append(this.groupId, rhs.groupId)
                .append(this.jvmId, rhs.jvmId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(groupId)
                .append(jvmId)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("groupId", groupId)
                .append("jvmId", jvmId)
                .toString();
    }
}
