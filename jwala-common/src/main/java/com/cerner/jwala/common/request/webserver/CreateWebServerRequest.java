package com.cerner.jwala.common.request.webserver;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.media.Media;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.request.Request;
import com.cerner.jwala.common.rule.*;
import com.cerner.jwala.common.rule.group.GroupIdsRule;
import com.cerner.jwala.common.rule.webserver.WebServerNameRule;

import java.io.Serializable;
import java.util.Collection;

public class CreateWebServerRequest implements Serializable, Request {

    private final Collection<Identifier<Group>> groupIds;
    private final String host;
    private final String name;
    private final Integer port;
    private final Integer httpsPort;
    private final Path statusPath;
    private final WebServerReachableState state;
    private final Media apacheHttpdMedia;

    public CreateWebServerRequest(final Collection<Identifier<Group>> theGroupIds,
                                  final String theName,
                                  final String theHost,
                                  final Integer thePort,
                                  final Integer theHttpsPort,
                                  final Path theStatusPath,
                                  final WebServerReachableState state,
                                  final Media apacheHttpdMedia) {
        host = theHost;
        port = thePort;
        httpsPort = theHttpsPort;
        name = theName;
        groupIds = theGroupIds;
        this.state = state;
        statusPath = theStatusPath;
        this.apacheHttpdMedia = apacheHttpdMedia;
    }

    public Collection<Identifier<Group>> getGroups() {
        return groupIds;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public Integer getHttpsPort() {
        return httpsPort;
    }

    public Path getStatusPath() {
        return statusPath;
    }

    public WebServerReachableState getState() {
        return state;
    }

    public Media getApacheHttpdMedia() {
        return apacheHttpdMedia;
    }

    @Override
    public void validate() {
        new MultipleRules(new WebServerNameRule(name),
                new HostNameRule(host),
                new PortNumberRule(port, FaultType.INVALID_WEBSERVER_PORT),
                new PortNumberRule(httpsPort, FaultType.INVALID_WEBSERVER_HTTPS_PORT, true),
                new GroupIdsRule(groupIds),
                new StatusPathRule(statusPath),
                new SpecialCharactersRule(name)).validate();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CreateWebServerRequest that = (CreateWebServerRequest) o;

        return name.equals(that.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "CreateWebServerRequest{" +
                "groupIds=" + groupIds +
                ", host='" + host + '\'' +
                ", name='" + name + '\'' +
                ", port=" + port +
                ", httpsPort=" + httpsPort +
                ", statusPath=" + statusPath +
                ", state=" + state +
                '}';
    }

}