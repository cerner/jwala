package com.cerner.jwala.common.request.group;

import com.cerner.jwala.common.request.Request;
import com.cerner.jwala.common.rule.MultipleRules;
import com.cerner.jwala.common.rule.NameLengthRule;
import com.cerner.jwala.common.rule.group.GroupIdRule;
import com.cerner.jwala.common.rule.group.GroupNameRule;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

public class CreateGroupRequest implements Serializable, Request {

    private final String groupName;

    public CreateGroupRequest(final String theGroupName) {
        groupName = theGroupName;
    }

    public String getGroupName() {
        return groupName;
    }

    @Override
    public void validate() {
        new MultipleRules(new GroupNameRule(groupName), new NameLengthRule(groupName)).validate();
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
        CreateGroupRequest rhs = (CreateGroupRequest) obj;
        return new EqualsBuilder()
                .append(this.groupName, rhs.groupName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(groupName)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "CreateGroupRequest{" +
                "groupName='" + groupName + '\'' +
                '}';
    }
}
