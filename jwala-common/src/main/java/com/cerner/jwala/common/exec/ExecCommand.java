package com.cerner.jwala.common.exec;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ExecCommand implements Serializable {

    protected final List<String> commandFragments;
    protected boolean runInShell = false;

    public ExecCommand(final String... theCommandFragments) {
        this(Arrays.asList(theCommandFragments));
    }

    public ExecCommand(final List<String> theCommandFragments) {
        List<String> formattedFragments = new ArrayList<>();
        for (String fragment : theCommandFragments) {
            formattedFragments.add(fragment.replace("\\", "/"));
        }
        commandFragments = Collections.unmodifiableList(formattedFragments);
    }

    public ExecCommand(final List<String> theCommandFragmentsReplace, final List<String> theCommandFragmentsNotReplace){
        List<String> formattedFragments = new ArrayList<>();
        for (String fragment : theCommandFragmentsReplace) {
            formattedFragments.add(fragment.replace("\\", "/"));
        }
        formattedFragments.addAll(theCommandFragmentsNotReplace);
        commandFragments = Collections.unmodifiableList(formattedFragments);
    }

    public List<String> getCommandFragments() {
        return commandFragments;
    }

    public String toCommandString() {
        final StringBuilder builder = new StringBuilder();
        for (final String fragment : commandFragments) {
            builder.append(fragment);
            builder.append(" ");
        }
        return builder.toString();
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
        ExecCommand rhs = (ExecCommand) obj;
        return new EqualsBuilder()
                .append(this.commandFragments, rhs.commandFragments)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(commandFragments)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("commandFragments", commandFragments)
                .toString();
    }

    public boolean getRunInShell() {
        return runInShell;
    }
}
