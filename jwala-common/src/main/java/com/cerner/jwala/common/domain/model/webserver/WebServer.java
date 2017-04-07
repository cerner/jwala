package com.cerner.jwala.common.domain.model.webserver;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.domain.model.uri.UriBuilder;

import java.io.Serializable;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebServer implements Serializable {

    private final Identifier<WebServer> id;
    private final Map<Identifier<Group>, Group> groups = new ConcurrentHashMap<>();
    private final String host;
    private final String name;
    private final Integer port;
    private final Integer httpsPort;
    private final Path statusPath;
    private final WebServerReachableState state;

    /**
     * Constructor for a bare minimum web server with group details.
     *
     * @param theId     the id
     * @param theGroups the groups that the web server is assigned to.
     * @param theName   the name of the web server.
     */
    public WebServer(final Identifier<WebServer> theId,
                     final Collection<Group> theGroups,
                     final String theName) {
        id = theId;
        host = null;
        port = null;
        name = theName;
        httpsPort = null;
        statusPath = null;
        for (final Group grp : theGroups) {
            groups.put(grp.getId(), grp);
        }
        state = WebServerReachableState.WS_UNEXPECTED_STATE;
    }

    public WebServer(final Identifier<WebServer> theId,
                     final Collection<Group> theGroups,
                     final String theName,
                     final String theHost,
                     final Integer thePort,
                     final Integer theHttpsPort,
                     final Path theStatusPath,
                     final WebServerReachableState state) {
        id = theId;
        host = theHost;
        port = thePort;
        name = theName;
        httpsPort = theHttpsPort;
        statusPath = theStatusPath;
        for (final Group grp : theGroups) {
            groups.put(grp.getId(), grp);
        }
        this.state = state;
    }

    public WebServer(final Identifier<WebServer> id,
                     final String host,
                     final String name,
                     final Integer port,
                     final Integer httpsPort,
                     final Path statusPath,
                     final WebServerReachableState state){
        this.id = id;
        this.host = host;
        this.name = name;
        this.port = port;
        this.httpsPort = httpsPort;
        this.statusPath = statusPath;
        this.state = state;
    }

    public Identifier<WebServer> getId() {
        return id;
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

    public Collection<Group> getGroups() {
        return groups.values();
    }

    public Collection<Identifier<Group>> getGroupIds() {
        return groups.keySet();
    }

    public Path getStatusPath() {
        return statusPath;
    }

    public URI getStatusUri() {
        if (getStatusPath().startsWithHttp()) {
            return getStatusPath().toUri();
        }

        final UriBuilder builder = new UriBuilder().setHost(getHost())
                .setPort(getPort())
                .setHttpsPort(getHttpsPort())
                .setPath(getStatusPath());
        return builder.buildUnchecked();
    }

    public WebServerReachableState getState() {
        return state;
    }

    /**
     * Return stateLabel for UI
     * @return
     */
    public String getStateLabel() {
        if(state != null)
            return state.toStateLabel();
        else
            return "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WebServer webServer = (WebServer) o;

        return !(id != null ? !id.equals(webServer.id) : webServer.id != null);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "WebServer{" +
                "id=" + id +
                ", groups=" + groups +
                ", host='" + host + '\'' +
                ", name='" + name + '\'' +
                ", port=" + port +
                ", httpsPort=" + httpsPort +
                ", statusPath=" + statusPath +
                ", state=" + state +
                '}';
    }
}
