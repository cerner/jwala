package com.cerner.jwala.common.exec;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public class RemoteExecCommand implements Serializable {

    private final RemoteSystemConnection remoteSystemConnection;
    private final ExecCommand command;

    public RemoteExecCommand(final RemoteSystemConnection theRemoteSystemConnection,
                             final ExecCommand theCommand) {
        remoteSystemConnection = theRemoteSystemConnection;
        command = theCommand;
    }

    public RemoteSystemConnection getRemoteSystemConnection() {
        return remoteSystemConnection;
    }

    public ExecCommand getCommand() {
        return command;
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
        RemoteExecCommand rhs = (RemoteExecCommand) obj;
        return new EqualsBuilder()
                .append(this.remoteSystemConnection, rhs.remoteSystemConnection)
                .append(this.command, rhs.command)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(remoteSystemConnection)
                .append(command)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("remoteSystemConnection", remoteSystemConnection)
                .append("command", command)
                .toString();
    }
}
