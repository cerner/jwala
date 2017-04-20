package com.cerner.jwala.common.domain.model.state;

import com.cerner.jwala.common.domain.model.id.Identifier;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.joda.time.DateTime;

public class CurrentState<S, T extends OperationalState>  {

    public static final String DEFAULT_EMPTY_MESSAGE = "";

    private final Identifier<S> id;
    private final T state;
    private final DateTime asOf;
    private final StateType type;
    private final String message;
    private final Long webServerCount;
    private final Long webServerStartedCount;
    private final Long webServerStoppedCount;
    private final Long webServerForciblyStoppedCount;
    private final Long jvmCount;
    private final Long jvmStartedCount;
    private final Long jvmStoppedCount;
    private final Long jvmForciblyStoppedCount;

    public CurrentState(final Identifier<S> id, final T state, final DateTime asOf, final StateType type, final String message) {
        this.id = id;
        this.state = state;
        this.asOf = asOf;
        this.type = type;
        this.message = message;
        this.webServerCount = null;
        this.webServerStartedCount = null;
        this.webServerStoppedCount = null;
        this.webServerForciblyStoppedCount = null;
        this.jvmCount = null;
        this.jvmStartedCount = null;
        this.jvmStoppedCount = null;
        this.jvmForciblyStoppedCount = null;
    }

    public CurrentState(final Identifier<S> id, final T state, final DateTime asOf, final StateType type) {
        this(id, state, asOf, type, DEFAULT_EMPTY_MESSAGE);
    }

    public CurrentState(final Identifier<S> id, final T state, final DateTime asOf, final StateType type,
                        final Long webServerCount, final Long webServerStartedCount, final Long webServerStoppedCount,
                        final Long webServerForciblyStoppedCount, final Long jvmCount, final Long jvmStartedCount,
                        final Long jvmStoppedCount, final Long jvmForciblyStoppedCount, final String message) {
        this.id = id;
        this.state = state;
        this.asOf = asOf;
        this.type = type;
        this.webServerCount = webServerCount;
        this.webServerStartedCount = webServerStartedCount;
        this.webServerStoppedCount = webServerStoppedCount;
        this.webServerForciblyStoppedCount = webServerForciblyStoppedCount;
        this.jvmCount = jvmCount;
        this.jvmStartedCount = jvmStartedCount;
        this.jvmStoppedCount = jvmStoppedCount;
        this.jvmForciblyStoppedCount = jvmForciblyStoppedCount;
        this.message = message;
    }

    public CurrentState(final Identifier<S> id, final T state, final DateTime asOf, final StateType type,
                        final Long webServerCount, final Long webServerStartedCount, final Long webServerStoppedCount,
                        final Long webServerForciblyStoppedCount, final Long jvmCount, final Long jvmStartedCount,
                        final Long jvmStoppedCount, final Long jvmForciblyStoppedCount) {
        this(id, state, asOf, type,
                webServerCount, webServerStartedCount, webServerStoppedCount, webServerForciblyStoppedCount,
                jvmCount, jvmStartedCount, jvmStoppedCount, jvmForciblyStoppedCount, DEFAULT_EMPTY_MESSAGE);
    }

    public Identifier<S> getId() {
        return id;
    }

    public T getState() {
        return state;
    }

    public String getStateString() {
        return state != null ? state.toStateLabel() : "";
    }

    public boolean hasMessage() {
        return message != null && !"".equals(message.trim());
    }

    public DateTime getAsOf() {
        return asOf;
    }

    public StateType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    @SuppressWarnings("unchecked")
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
        CurrentState<S, T> rhs = (CurrentState<S, T>) obj;
        return new EqualsBuilder()
                .append(this.id, rhs.id)
                .append(this.state, rhs.state)
                .append(this.asOf, rhs.asOf)
                .append(this.type, rhs.type)
                .append(this.message, rhs.message)
                .append(this.webServerCount, rhs.webServerCount)
                .append(this.webServerStartedCount, rhs.webServerStartedCount)
                .append(this.webServerStoppedCount, rhs.webServerStoppedCount)
                .append(this.webServerForciblyStoppedCount, rhs.webServerForciblyStoppedCount)
                .append(this.jvmCount, rhs.jvmCount)
                .append(this.jvmStartedCount, rhs.jvmStartedCount)
                .append(this.jvmStoppedCount, rhs.jvmStoppedCount)
                .append(this.jvmForciblyStoppedCount, rhs.jvmForciblyStoppedCount)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(state)
                .append(asOf)
                .append(type)
                .append(message)
                .append(webServerCount)
                .append(webServerStartedCount)
                .append(webServerStoppedCount)
                .append(webServerForciblyStoppedCount)
                .append(jvmCount)
                .append(jvmStartedCount)
                .append(jvmStoppedCount)
                .append(jvmForciblyStoppedCount)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE)
                .append("id", id)
                .append("state", state)
                .append("asOf", asOf)
                .append("type", type)
                .append("message", message)
                .append("webServerCount", webServerCount)
                .append("webServerStartedCount", webServerStartedCount)
                .append("webServerStoppedCount", webServerStoppedCount)
                .append("webServerForciblyStoppedCount", webServerForciblyStoppedCount)
                .append("jvmCount", jvmCount)
                .append("jvmStartedCount", jvmStartedCount)
                .append("jvmStoppedCount", jvmStoppedCount)
                .append("jvmForciblyStoppedCount", jvmForciblyStoppedCount)
                .toString();
    }

    public Long getWebServerCount() {
        return webServerCount;
    }

    public Long getWebServerStartedCount() {
        return webServerStartedCount;
    }

    public Long getWebServerStoppedCount() {
        return webServerStoppedCount;
    }

    public Long getWebServerForciblyStoppedCount() {
        return webServerForciblyStoppedCount;
    }

    public Long getJvmCount() {
        return jvmCount;
    }

    public Long getJvmStartedCount() {
        return jvmStartedCount;
    }

    public Long getJvmStoppedCount() {
        return jvmStoppedCount;
    }

    public Long getJvmForciblyStoppedCount() {
        return jvmForciblyStoppedCount;
    }
}
