package com.cerner.jwala.common.domain.model.id;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

public class Identifier<T> implements Serializable {

    private final Long id;

    public Identifier(final String id) {
        this(Long.valueOf(id));
    }

    public Identifier(final Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    /**
     * Helper method to return an identifier templated by a type
     */
    public static <U> Identifier<U> id(final Long longId) {
        return new Identifier<>(longId);
    }

    /**
     * Helper method to return an identifier templated by a type
     */
    public static <U> Identifier<U> id(final Long longId, final Class<U> clazz) {
        return new Identifier<>(longId);
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
        Identifier rhs = (Identifier) obj;
        return new EqualsBuilder()
                .append(this.id, rhs.id)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", id)
                .toString();
    }

}
