package com.cerner.jwala.common.request.app;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.app.ApplicationControlOperation;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.request.Request;
import com.cerner.jwala.common.rule.app.ApplicationIdRule;

import java.io.Serializable;

public class ControlApplicationRequest implements Serializable, Request {

    private final Identifier<Application> appId;
    private final ApplicationControlOperation controlOperation;

    public ControlApplicationRequest(final Identifier<Application> theId,
                                     final ApplicationControlOperation theControlOperation) {
        appId = theId;
        controlOperation = theControlOperation;
    }

    public Identifier<Application> getAppId() {
        return appId;
    }

    public ApplicationControlOperation getControlOperation() {
        return controlOperation;
    }

    @Override
    public void validate() {
        new ApplicationIdRule(appId).validate();
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
        ControlApplicationRequest rhs = (ControlApplicationRequest) obj;
        return new EqualsBuilder()
                .append(this.appId, rhs.appId)
                .append(this.controlOperation, rhs.controlOperation)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(appId)
                .append(controlOperation)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("appId", appId)
                .append("controlOperation", controlOperation)
                .toString();
    }
}
