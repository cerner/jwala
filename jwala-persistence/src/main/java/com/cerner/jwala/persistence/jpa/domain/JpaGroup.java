package com.cerner.jwala.persistence.jpa.domain;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Entity
@Table(name = "grp", uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
@NamedQueries({
        @NamedQuery(name = JpaGroup.QUERY_GET_GROUP_ID, query = "SELECT g.id FROM JpaGroup g WHERE g.name = :name"),
        @NamedQuery(name = JpaGroup.QUERY_GET_GROUP, query = "SELECT g FROM JpaGroup g WHERE g.id = :groupId"),
        @NamedQuery(name = JpaGroup.QUERY_GET_GROUPS_WITH_WEBSERVER, query = "SELECT g FROM JpaGroup g WHERE :webServer MEMBER OF g.webServers"),
        @NamedQuery(name = JpaGroup.QUERY_UPDATE_STATE_BY_ID, query = "UPDATE JpaGroup g SET g.stateName = :state WHERE g.id = :id"),
        @NamedQuery(name = JpaGroup.QUERY_GET_HOSTS_OF_A_GROUP, query="SELECT DISTINCT j.hostName FROM JpaGroup g JOIN g.jvms j WHERE g.name = :name")
})
public class JpaGroup extends AbstractEntity<JpaGroup> {

    public static final String QUERY_GET_GROUP_ID = "getGroupId";
    public static final String QUERY_GET_GROUP = "getGroup";
    public static final String QUERY_GET_GROUPS_WITH_WEBSERVER = "getGroupWithWebServer";
    public static final String QUERY_UPDATE_STATE_BY_ID = "updateStateById";
    public static final String QUERY_GET_HOSTS_OF_A_GROUP = "getHostsOfAGroup";

    public static final String QUERY_PARAM_ID = "id";
    public static final String QUERY_PARAM_STATE = "state";
    public static final String QUERY_PARAM_NAME = "name";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(nullable = false, unique = true)
    public String name;

    @ManyToMany
    @JoinTable(name = "GRP_JVM",
               joinColumns = {@JoinColumn(name = "GROUP_ID", referencedColumnName = "ID")},
               inverseJoinColumns = {@JoinColumn(name = "JVM_ID", referencedColumnName = "ID")},
               uniqueConstraints = @UniqueConstraint(columnNames = {"GROUP_ID", "JVM_ID"}))
    private List<JpaJvm> jvms = new ArrayList<>();

    @Column(name = "STATE")
    private String stateName;
    
    @Column(nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar stateUpdated;

    @ManyToMany
    @JoinTable(name = "WEBSERVER_GRP",
               joinColumns = {@JoinColumn(name = "GROUP_ID", referencedColumnName = "ID")},
               inverseJoinColumns = {@JoinColumn(name = "WEBSERVER_ID", referencedColumnName = "ID")},
               uniqueConstraints = @UniqueConstraint(columnNames = {"GROUP_ID", "WEBSERVER_ID"}))
    private List<JpaWebServer> webServers = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "groupId")
    private List<JpaHistory> history;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<JpaJvm> getJvms() {
        return jvms;
    }

    public void setJvms(final List<JpaJvm> jvms) {
        this.jvms = jvms;
    }
    
    public Calendar getStateUpdated() {
        return stateUpdated;
    }

    public void setStateUpdated(Calendar stateUpdated) {
        this.stateUpdated = stateUpdated;
    }

    public List<JpaWebServer> getWebServers() {
        return webServers;
    }

    public void setWebServers(List<JpaWebServer> webServers) {
        this.webServers = webServers;
    }

    public List<JpaHistory> getHistory() {
        return history;
    }

    public void setHistory(List<JpaHistory> history) {
        this.history = history;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final JpaGroup jpaGroup = (JpaGroup) o;

        return id != null ? id.equals(jpaGroup.id) : jpaGroup.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "JpaGroup{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", jvms=" + jvms +
                ", stateName='" + stateName + '\'' +
                ", stateUpdated=" + stateUpdated +
                ", webServers=" + webServers +
                ", history=" + history +
                '}';
    }
}
