package com.cerner.jwala.common.rule.identifier;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.rule.Rule;

import java.util.HashSet;
import java.util.Set;

public abstract class MultipleIdentifiersRule<T> implements Rule {

    protected final Set<Rule> rules;

    public MultipleIdentifiersRule(final Set<Identifier<T>> theIds) {
        rules = new HashSet<>();
        for (final Identifier<T> id : theIds) {
            rules.add(createRule(id));
        }
    }

    @Override
    public boolean isValid() {
        for (final Rule rule : rules) {
            if (!rule.isValid()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void validate() throws BadRequestException {
        for (final Rule rule : rules) {
            rule.validate();
        }
    }

    protected abstract Rule createRule(final Identifier<T> anId);

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
        MultipleIdentifiersRule rhs = (MultipleIdentifiersRule) obj;
        return new EqualsBuilder()
                .append(this.rules, rhs.rules)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(rules)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("rules", rules)
                .toString();
    }
}
