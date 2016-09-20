package com.siemens.cto.aem.common.request.webserver;

import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.path.Path;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.common.request.Request;
import com.siemens.cto.aem.common.rule.HostNameRule;
import com.siemens.cto.aem.common.rule.MultipleRules;
import com.siemens.cto.aem.common.rule.PortNumberRule;
import com.siemens.cto.aem.common.rule.StatusPathRule;
import com.siemens.cto.aem.common.rule.group.GroupIdsRule;
import com.siemens.cto.aem.common.rule.webserver.WebServerNameRule;

import java.io.Serializable;
import java.util.Collection;

public class CreateWebServerRequest implements Serializable, Request {

    private static final long serialVersionUID = 1L;

    private final Collection<Identifier<Group>> groupIds;
    private final String host;
    private final String name;
    private final Integer port;
    private final Integer httpsPort;
    private final Path statusPath;
    private final Path svrRoot;
    private final Path docRoot;
    private final WebServerReachableState state;
    private final String errorStatus;

    public CreateWebServerRequest(final Collection<Identifier<Group>> theGroupIds,
                                  final String theName,
                                  final String theHost,
                                  final Integer thePort,
                                  final Integer theHttpsPort,
                                  final Path theStatusPath,
                                  final Path theSvrRoot,
                                  final Path theDocRoot,
                                  final WebServerReachableState state,
                                  final String errorStatus) {
        host = theHost;
        port = thePort;
        httpsPort = theHttpsPort;
        name = theName;
        groupIds = theGroupIds;
        docRoot = theDocRoot;
        this.state = state;
        this.errorStatus = errorStatus;
        svrRoot = theSvrRoot;
        statusPath = theStatusPath;
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

    public Path getSvrRoot() {
        return svrRoot;
    }

    public Path getDocRoot() {
        return docRoot;
    }

    public WebServerReachableState getState() {
        return state;
    }

    public String getErrorStatus() {
        return errorStatus;
    }

    @Override
    public void validate() {
        new MultipleRules(new WebServerNameRule(name),
                new HostNameRule(host),
                new PortNumberRule(port, AemFaultType.INVALID_WEBSERVER_PORT),
                new PortNumberRule(httpsPort, AemFaultType.INVALID_WEBSERVER_HTTPS_PORT, true),
                new GroupIdsRule(groupIds),
                new StatusPathRule(statusPath)).validate();
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
                ", svrRoot=" + svrRoot +
                ", docRoot=" + docRoot +
                ", state=" + state +
                ", errorStatus='" + errorStatus + '\'' +
                '}';
    }

}