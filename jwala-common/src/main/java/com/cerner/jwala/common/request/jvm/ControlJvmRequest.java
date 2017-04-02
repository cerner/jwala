package com.cerner.jwala.common.request.jvm;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.request.Request;
import com.cerner.jwala.common.rule.jvm.JvmIdRule;

import java.io.Serializable;

public class ControlJvmRequest implements Serializable, Request {

    private final Identifier<Jvm> jvmId;
    private final JvmControlOperation controlOperation;
    private final String message;

    public ControlJvmRequest(final Identifier<Jvm> theId,
                             final JvmControlOperation theControlOperation) {
        this.jvmId = theId;
        this.controlOperation = theControlOperation;
        this.message = null;
    }

    public ControlJvmRequest(final Identifier<Jvm> theId,
                             final JvmControlOperation theControlOperation,
                             final String message) {
        this.jvmId = theId;
        this.controlOperation = theControlOperation;
        this.message = message;
    }

    public Identifier<Jvm> getJvmId() {
        return jvmId;
    }

    public JvmControlOperation getControlOperation() {
        return controlOperation;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void validate() {
        new JvmIdRule(jvmId).validate();
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
        ControlJvmRequest rhs = (ControlJvmRequest) obj;
        return new EqualsBuilder()
                .append(this.jvmId, rhs.jvmId)
                .append(this.controlOperation, rhs.controlOperation)
                .append(this.message, rhs.message)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(jvmId)
                .append(controlOperation)
                .append(message)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("jvmId", jvmId)
                .append("controlOperation", controlOperation)
                .append("message", message)
                .toString();
    }
}
