package com.cerner.jwala.persistence.jpa.domain;

import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "webserver", uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
@NamedQueries({
        @NamedQuery(name = JpaWebServer.FIND_WEB_SERVER_BY_QUERY,
                query = "SELECT ws FROM JpaWebServer ws WHERE lower(ws.name) = lower(:wsName)"),
        @NamedQuery(name = JpaWebServer.FIND_JVMS_QUERY,
                query = "SELECT DISTINCT jvm FROM JpaJvm jvm JOIN jvm.groups g " +
                        "WHERE g.id IN (SELECT a.group FROM JpaApplication a " +
                        "WHERE a.group IN (:groups))"),
        @NamedQuery(name = JpaWebServer.QUERY_UPDATE_STATE_BY_ID, query = "UPDATE JpaWebServer w SET w.state = :state, w.lastUpdateDate = CURRENT_TIMESTAMP WHERE w.id = :id"),
        @NamedQuery(name = JpaWebServer.QUERY_UPDATE_ERROR_STATUS_BY_ID, query = "UPDATE JpaWebServer w SET w.lastUpdateDate = CURRENT_TIMESTAMP WHERE w.id = :id"),
        @NamedQuery(name = JpaWebServer.QUERY_UPDATE_STATE_AND_ERR_STS_BY_ID, query = "UPDATE JpaWebServer w SET w.state = :state, w.lastUpdateDate = CURRENT_TIMESTAMP WHERE w.id = :id"),
        @NamedQuery(name = JpaWebServer.QUERY_GET_WS_COUNT_BY_STATE_AND_GROUP_NAME, query = "SELECT COUNT(1) FROM JpaWebServer w WHERE w.state = :state AND w.groups.name = :groupName"),
        @NamedQuery(name = JpaWebServer.QUERY_GET_WS_COUNT_BY_GROUP_NAME, query = "SELECT COUNT(1) FROM JpaWebServer w WHERE w.groups.name = :groupName"),
        @NamedQuery(name = JpaWebServer.QUERY_GET_WS_AND_ITS_GROUPS,
                /* Why do we have DISTINCT ? This is to prevent getSingleResult for throwing NonUniqueResultException.
                   What's weird is that this query always returns 1 result when tested using getResultList! */
                query = "SELECT DISTINCT w FROM JpaWebServer w LEFT JOIN FETCH w.groups WHERE w.id = :id"),
        @NamedQuery(name = JpaWebServer.QUERY_GET_WS_BY_GROUP_NAME, query = "SELECT w FROM JpaWebServer w WHERE w.groups.name = :groupName"),
        @NamedQuery(name = JpaWebServer.FIND_WEBSERVER_BY_GROUP_QUERY, query = "SELECT w FROM JpaWebServer w WHERE w.name = :wsName AND w.groups.name = :groupName"),
        @NamedQuery(name = JpaWebServer.FIND_WEBSERVERS_BY_GROUPID, query = "SELECT j FROM JpaWebServer j WHERE :groupId MEMBER OF j.groups.id"),
        @NamedQuery(name = JpaWebServer.FIND_WEB_SERVER_BY_NAME_LIKE_QUERY, query= "SELECT g FROM JpaWebServer g WHERE g.name LIKE  ?1 ")
})
public class JpaWebServer extends AbstractEntity<JpaWebServer> {

    public static final String WEB_SERVER_PARAM_NAME = "wsName";
    public static final String FIND_WEB_SERVER_BY_QUERY = "findWebServerByNameQuery";
    public static final String FIND_WEB_SERVER_BY_NAME_LIKE_QUERY = "findWebServerByNameLikeQuery";
    public static final String FIND_JVMS_QUERY = "findJvmsQuery";
    public static final String FIND_WEBSERVER_BY_GROUP_QUERY = "findWebServerByGroupQuery";
    public static final String FIND_WEBSERVERS_BY_GROUPID = "findWebServersByGroupId";
    public static final String QUERY_UPDATE_STATE_BY_ID = "updateWebServerStateById";
    public static final String QUERY_UPDATE_ERROR_STATUS_BY_ID = "updateWebServerErrorStatusById";
    public static final String QUERY_UPDATE_STATE_AND_ERR_STS_BY_ID = "updateWebServerStateAndErrStsById";
    public static final String QUERY_GET_WS_COUNT_BY_STATE_AND_GROUP_NAME = "getWebServerCountByStateAndGroupName";
    public static final String QUERY_GET_WS_COUNT_BY_GROUP_NAME = "getWebServerCountByGroupName";
    public static final java.lang.String QUERY_GET_WS_AND_ITS_GROUPS = "getWebServerAndItsGroups";
    public static final String QUERY_GET_WS_BY_GROUP_NAME = "findWebServerByGroupName";

    public static final String QUERY_PARAM_ID = "id";
    public static final String QUERY_PARAM_STATE = "state";
    public static final String QUERY_PARAM_GROUP_NAME = "groupName";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String host;

    private String name;

    private Integer port;

    private Integer httpsPort;

    @Column(nullable = false)
    private String statusPath;

    @Enumerated(EnumType.STRING)
    private WebServerReachableState state;

    @ManyToMany(mappedBy = "webServers", fetch = FetchType.EAGER)
    private List<JpaGroup> groups = new ArrayList<>();

    @OneToOne (targetEntity = JpaMedia.class)
    @Column(nullable = true)
    private JpaMedia apacheHttpdMedia;

    public Long getId() {
        return id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public List<JpaGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<JpaGroup> newGroups) {
        groups = newGroups;
    }

    public Integer getHttpsPort() {
        return httpsPort;
    }

    public void setHttpsPort(Integer httpsPort) {
        this.httpsPort = httpsPort;
    }

    public String getStatusPath() {
        return statusPath;
    }

    public void setStatusPath(final String statusPath) {
        this.statusPath = statusPath;
    }

    public WebServerReachableState getState() {
        return state;
    }

    public void setState(WebServerReachableState state) {
        this.state = state;
    }

    public JpaMedia getApacheHttpdMedia() {
        return apacheHttpdMedia;
    }

    public void setApacheHttpdMedia(final JpaMedia apacheHttpdMedia) {
        this.apacheHttpdMedia = apacheHttpdMedia;
    }

    @Override
    protected void prePersist() {
        super.prePersist();
        if (state == null) {
            state = WebServerReachableState.WS_UNREACHABLE;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JpaWebServer that = (JpaWebServer) o;

        return !(id != null ? !id.equals(that.id) : that.id != null);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "JpaWebServer{" +
                "id=" + id +
                ", host='" + host + '\'' +
                ", name='" + name + '\'' +
                ", port=" + port +
                ", httpsPort=" + httpsPort +
                ", statusPath='" + statusPath + '\'' +
                '}';
    }

}
