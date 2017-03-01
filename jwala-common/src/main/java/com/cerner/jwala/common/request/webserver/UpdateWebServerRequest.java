package com.cerner.jwala.common.request.webserver;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.request.Request;
import com.cerner.jwala.common.rule.*;
import com.cerner.jwala.common.rule.group.GroupIdsRule;
import com.cerner.jwala.common.rule.webserver.WebServerIdRule;
import com.cerner.jwala.common.rule.webserver.WebServerNameRule;

public class UpdateWebServerRequest implements Serializable, Request {

    private final Identifier<WebServer> id;
    private final Collection<Identifier<Group>> newGroupIds; //TODO (Corey) Peter Agrees: Change this to a Set all the way down the line...
    private final String newHost;
    private final String newName;
    private final Integer newPort;
    private final Integer newHttpsPort;
    private final Path newStatusPath;
    private final Path newSvrRoot;
    private final Path newDocRoot;
    private final Path newHttpConfigFile;
    private final WebServerReachableState state;
    private final String errorStatus;

    public UpdateWebServerRequest(final Identifier<WebServer> theId,
                                  final Collection<Identifier<Group>> theNewGroupIds,
                                  final String theNewName,
                                  final String theNewHost,
                                  final Integer theNewPort,
                                  final Integer theNewHttpsPort,
                                  final Path theNewStatusPath,
                                  final Path theNewHttpConfigFile,
                                  final Path theSvrRoot,
                                  final Path theDocRoot,
                                  final WebServerReachableState state,
                                  final String errorStatus) {
        id = theId;
        newHost = theNewHost;
        newPort = theNewPort;
        newHttpsPort = theNewHttpsPort;
        newName = theNewName;
        newGroupIds = Collections.unmodifiableCollection(new HashSet<>(theNewGroupIds));
        newStatusPath = theNewStatusPath;
        newHttpConfigFile = theNewHttpConfigFile;
        newSvrRoot = theSvrRoot;
        newDocRoot = theDocRoot;
        this.state = state;
        this.errorStatus = errorStatus;
    }

    public Identifier<WebServer> getId() {
        return id;
    }

    public String getNewName() {
        return newName;
    }

    public String getNewHost() {
        return newHost;
    }

    public Integer getNewPort() {
        return newPort;
    }

    public Integer getNewHttpsPort() {
        return newHttpsPort;
    }

    public Collection<Identifier<Group>> getNewGroupIds() {
        return newGroupIds;
    }

    public Path getNewStatusPath() {
        return newStatusPath;
    }

    public Path getNewHttpConfigFile() {
        return newHttpConfigFile;
    }

    public Path getNewSvrRoot() {
        return newSvrRoot;
    }

    public Path getNewDocRoot() {
        return newDocRoot;
    }

    public WebServerReachableState getState() {
        return state;
    }

    public String getErrorStatus() {
        return errorStatus;
    }

    @Override
    public void validate() {
        final MultipleRules mr =
                new MultipleRules(new WebServerNameRule(newName),
                        new HostNameRule(newHost),
                        new PortNumberRule(newPort, FaultType.INVALID_WEBSERVER_PORT),
                        new PortNumberRule(newHttpsPort, FaultType.INVALID_WEBSERVER_PORT, true),
                        new WebServerIdRule(id),
                        new GroupIdsRule(newGroupIds),
                        new StatusPathRule(newStatusPath));

        mr.validate();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UpdateWebServerRequest that = (UpdateWebServerRequest) o;

        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "UpdateWebServerRequest{" +
                "id=" + id +
                ", newGroupIds=" + newGroupIds +
                ", newHost='" + newHost + '\'' +
                ", newName='" + newName + '\'' +
                ", newPort=" + newPort +
                ", newHttpsPort=" + newHttpsPort +
                ", newStatusPath=" + newStatusPath +
                ", newSvrRoot=" + newSvrRoot +
                ", newDocRoot=" + newDocRoot +
                ", newHttpConfigFile=" + newHttpConfigFile +
                ", state=" + state +
                ", errorStatus='" + errorStatus + '\'' +
                '}';
    }

}