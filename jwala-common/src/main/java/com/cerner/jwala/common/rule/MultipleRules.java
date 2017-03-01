package com.cerner.jwala.common.rule;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.cerner.jwala.common.exception.BadRequestException;

import java.util.Arrays;
import java.util.List;

public class MultipleRules implements Rule {

    private final List<Rule> rules;

    public MultipleRules(final List<Rule> someRules) {
        rules = someRules;
    }

    public MultipleRules(final Rule... someRules) {
        this(Arrays.asList(someRules));
    }

    @Override
    public void validate() throws BadRequestException {
        for (final Rule rule : rules) {
            rule.validate();
        }
    }

    @Override
    public boolean isValid() {
        for (final Rule rule : rules) {
            if(!rule.isValid()) {
                return false;
            }
        }
        return true;
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
        MultipleRules rhs = (MultipleRules) obj;
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
