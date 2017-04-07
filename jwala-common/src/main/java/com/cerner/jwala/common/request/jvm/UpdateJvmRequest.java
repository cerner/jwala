package com.cerner.jwala.common.request.jvm;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.media.Media;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.request.Request;
import com.cerner.jwala.common.request.group.AddJvmToGroupRequest;
import com.cerner.jwala.common.rule.*;
import com.cerner.jwala.common.rule.group.GroupIdsRule;
import com.cerner.jwala.common.rule.jvm.JvmIdRule;
import com.cerner.jwala.common.rule.jvm.JvmNameRule;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class UpdateJvmRequest implements Serializable, Request {

    private final Identifier<Jvm> id;
    private final String newJvmName;
    private final String newHostName;
    private final Integer newHttpPort;
    private final Integer newHttpsPort;
    private final Integer newRedirectPort;
    private final Integer newShutdownPort;
    private final Integer newAjpPort;
    private final Path newStatusPath;
    private final String newSystemProperties;
    private final String newUserName;
    private final String newEncryptedPassword;
    private final Identifier<Media> newJdkMediaId;
//    private final Identifier<Media> newTomcatMediaId;

    private final Set<Identifier<Group>> groupIds;

    public UpdateJvmRequest(final Identifier<Jvm> theId,
                            final String theNewJvmName,
                            final String theNewHostName,
                            final Set<Identifier<Group>> theGroupIds,
                            final Integer theNewHttpPort,
                            final Integer theNewHttpsPort,
                            final Integer theNewRedirectPort,
                            final Integer theNewShutdownPort,
                            final Integer theNewAjpPort,
                            final Path theNewStatusPath,
                            final String theNewSystemProperties,
                            final String theUserName,
                            final String theEncryptedPassword,
                            final Identifier<Media> theJdkMediaId/*,
                            final Identifier<Media> theTomcatMediaId*/) {
        id = theId;
        newJvmName = theNewJvmName;
        newHostName = theNewHostName;
        groupIds = Collections.unmodifiableSet(new HashSet<>(theGroupIds));
        newHttpPort = theNewHttpPort;
        newHttpsPort = theNewHttpsPort;
        newRedirectPort = theNewRedirectPort;
        newShutdownPort = theNewShutdownPort;
        newAjpPort = theNewAjpPort;
        newStatusPath = theNewStatusPath;
        newSystemProperties = theNewSystemProperties;
        newUserName = theUserName;
        newEncryptedPassword = theEncryptedPassword;
        newJdkMediaId = theJdkMediaId;
//        newTomcatMediaId = theTomcatMediaId;
    }

    public Identifier<Jvm> getId() {
        return id;
    }

    public String getNewJvmName() {
        return newJvmName;
    }

    public String getNewHostName() {
        return newHostName;
    }

    public Integer getNewHttpPort() {
        return newHttpPort;
    }

    public Integer getNewHttpsPort() {
        return newHttpsPort;
    }

    public Integer getNewRedirectPort() {
        return newRedirectPort;
    }

    public Integer getNewShutdownPort() {
        return newShutdownPort;
    }

    public Integer getNewAjpPort() {
        return newAjpPort;
    }

    public String getNewUserName() {
        return newUserName;
    }

    public String getNewEncryptedPassword() {
        return newEncryptedPassword;
    }

    public Identifier<Media> getNewJdkMediaId() {
        return newJdkMediaId;
    }

//    public Identifier<Media> getNewTomcatMediaId() {
//        return newTomcatMediaId;
//    }

    public String getNewSystemProperties() {return newSystemProperties;}

    public Set<AddJvmToGroupRequest> getAssignmentCommands() {
        return new AddJvmToGroupCommandSetBuilder(id,
                                                  groupIds).build();
    }

    public Path getNewStatusPath() {
        return newStatusPath;
    }

    @Override
    public void validate() {
        new MultipleRules(new JvmNameRule(newJvmName),
                          new HostNameRule(newHostName),
                          new StatusPathRule(newStatusPath),
                          new JvmIdRule(id),
                          new GroupIdsRule(groupIds),
                          new PortNumberRule(newHttpPort, FaultType.INVALID_JVM_HTTP_PORT),
                          new PortNumberRule(newHttpsPort, FaultType.INVALID_JVM_HTTPS_PORT, true),
                          new PortNumberRule(newRedirectPort, FaultType.INVALID_JVM_REDIRECT_PORT),
                          new ShutdownPortNumberRule(newShutdownPort, FaultType.INVALID_JVM_SHUTDOWN_PORT),
                          new PortNumberRule(newAjpPort, FaultType.INVALID_JVM_AJP_PORT),
                          new SpecialCharactersRule(newJvmName)).validate();
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
        UpdateJvmRequest rhs = (UpdateJvmRequest) obj;
        return new EqualsBuilder()
                .append(this.id, rhs.id)
                .append(this.newJvmName, rhs.newJvmName)
                .append(this.newHostName, rhs.newHostName)
                .append(this.newHttpPort, rhs.newHttpPort)
                .append(this.newHttpsPort, rhs.newHttpsPort)
                .append(this.newRedirectPort, rhs.newRedirectPort)
                .append(this.newShutdownPort, rhs.newShutdownPort)
                .append(this.newAjpPort, rhs.newAjpPort)
                .append(this.groupIds, rhs.groupIds)
                .append(this.newSystemProperties, rhs.newSystemProperties)
                .append(this.newUserName, rhs.newUserName)
                .append(this.newEncryptedPassword, rhs.newEncryptedPassword)
                .append(this.newJdkMediaId, rhs.newJdkMediaId)
//                .append(this.newTomcatMediaId, rhs.newTomcatMediaId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(newJvmName)
                .append(newHostName)
                .append(newHttpPort)
                .append(newHttpsPort)
                .append(newRedirectPort)
                .append(newShutdownPort)
                .append(newAjpPort)
                .append(groupIds)
                .append(newSystemProperties)
                .append(newUserName)
                .append(newEncryptedPassword)
                .append(newJdkMediaId)
//                .append(newTomcatMediaId)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "UpdateJvmRequest{" +
                "id=" + id +
                ", newJvmName='" + newJvmName + '\'' +
                ", newHostName='" + newHostName + '\'' +
                ", newHttpPort=" + newHttpPort +
                ", newHttpsPort=" + newHttpsPort +
                ", newRedirectPort=" + newRedirectPort +
                ", newShutdownPort=" + newShutdownPort +
                ", newAjpPort=" + newAjpPort +
                ", newStatusPath=" + newStatusPath +
                ", newSystemProperties='" + newSystemProperties + '\'' +
                ", newUserName='" + newUserName + '\'' +
                ", newEncryptedPassword='" + newEncryptedPassword + '\'' +
                ", newJdkMediaId='" + newJdkMediaId + '\'' +
//                ", newTomcatMediaId='" + newTomcatMediaId + '\'' +
                ", groupIds=" + groupIds +
                '}';
    }
}
