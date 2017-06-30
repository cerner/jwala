package com.cerner.jwala.persistence.jpa.domain;

import javax.persistence.*;

/**
 * An application is usually a web application stored in a war file
 *
 * The war file may be deployed to any number of JVMs, which happens
 * through the deploying the owning group to JVMs.
 *
 * Each Application is created and assigned to a group.
 *
 * For Health Check, where it might be deployed alongside another application,
 * the caller must create a group for health check, and a group for the
 * other application so that they can be deployed and managed.
 *
 * @author horspe00
 *
 */
@Entity
@Table(name = "app", uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
@NamedQueries({
    @NamedQuery(
        name=JpaApplication.QUERY_BY_GROUP_NAME,
        query="SELECT a FROM JpaApplication a WHERE a.group.name = :groupName"
    ),
    @NamedQuery(
            name=JpaApplication.QUERY_BY_JVM_ID,
            query="SELECT a FROM JpaApplication a WHERE a.group in (SELECT g FROM JpaGroup g WHERE g.jvms.id = :jvmId)"
    ),
    @NamedQuery(
        name=JpaApplication.QUERY_BY_GROUP_ID,
        query="SELECT a FROM JpaApplication a WHERE a.group.id= :groupId"
    ),
    @NamedQuery(
        name=JpaApplication.QUERY_BY_WEB_SERVER_NAME,
        query="SELECT a FROM JpaApplication a WHERE a.group in (:groups)"),
    @NamedQuery(
            name=JpaApplication.QUERY_BY_NAME,
            query="SELECT a FROM JpaApplication a WHERE a.name = :appName"),
    @NamedQuery(
            name=JpaApplication.QUERY_BY_GROUP_JVM_AND_APP_NAME,
            query="SELECT a FROM JpaApplication a WHERE a.name = :appName AND a.group in " +
                  "(SELECT g FROM JpaGroup g WHERE g.name = :groupName AND g.jvms.name = :jvmName)"),
    @NamedQuery(
            name=JpaApplication.QUERY_FIND_BY_GROUP_AND_APP_NAME,
            query="SELECT a FROM JpaApplication a WHERE a.name = :appName AND a.group.name = :groupName")
    })
public class JpaApplication extends AbstractEntity<JpaApplication> {

    public static final String QUERY_BY_GROUP_ID = "findApplicationsByGroupId";
    public static final String QUERY_BY_GROUP_NAME = "findApplicationsByGroupName";
    public static final String QUERY_BY_JVM_ID = "findApplicationsByJvmId";
    public static final String QUERY_BY_WEB_SERVER_NAME = "findApplicationsByWebServerName";
    public static final String QUERY_BY_NAME = "findApplicationByName";
    public static final String GROUP_ID_PARAM = "groupId";
    public static final String JVM_ID_PARAM = "jvmId";
    public static final String GROUP_NAME_PARAM = "groupName";
    public static final String WEB_SERVER_NAME_PARAM = "wsName";
    public static final String QUERY_BY_GROUP_JVM_AND_APP_NAME = "findApplicationByGroupJvmAndAppName";
    public static final String GROUP_LIST_PARAM = "groups";
    public static final String APP_NAME_PARAM = "appName";
    public static final String QUERY_FIND_BY_GROUP_AND_APP_NAME = "findApplicationByGroupAndAppName";

    public static final String QUERY_PARAM_APP_NAME = "appName";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    public Long id;

    @Column(nullable = false, unique = true)
    public String name;
    
    /**
     * Relationship stored in app.group to allow operations across a group
     * BUT, this does mean that a JpaApplication represents a single
     * (potentially deployed) application instance.
     */
    @ManyToOne(optional=true) public JpaGroup group;

    @Column(nullable = false, unique = false)
    private String webAppContext;

    @Column(nullable = true, unique = false)
    private String warPath;

    @Column(nullable = true, unique = false)
    private String warDeployPath;

    @SuppressWarnings("Unused")
    @Column(nullable = true)
    private String documentRoot; // potential addition to track the static content files TODO - coverage, et al.

    private boolean secure;

    private boolean loadBalanceAcrossServers;

    private boolean unpackWar;

    @Column(nullable = true, unique = false)
    private String warName;

    public void setWarPath(String aWarPath) {
        warPath = aWarPath;
    }

    public void setWebAppContext(String aWebAppContext) {
        this.webAppContext = aWebAppContext;
    }

    public void setGroup(JpaGroup jpaGroup) {
        this.group = jpaGroup;
    }
    
    public JpaGroup getGroup() {
        return this.group;
    }

    public String getWarPath() {
        return this.warPath;
    }
    
    public String getWebAppContext() {
        return this.webAppContext;
    }

    @Override
    public Long getId() {
        return this.id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public boolean isLoadBalanceAcrossServers() {
        return loadBalanceAcrossServers;
    }

    public void setLoadBalanceAcrossServers(boolean loadBalanceAcrossServers) {
        this.loadBalanceAcrossServers = loadBalanceAcrossServers;
    }

    public void setWarName(String warName) {
        this.warName = warName;
    }

    public String getWarName() {
        return warName;
    }

    public boolean isUnpackWar() {
        return unpackWar;
    }

    public void setUnpackWar(boolean unpackWar) {
        this.unpackWar = unpackWar;
    }

    public String getWarDeployPath() {
        return warDeployPath;
    }

    public void setWarDeployPath(String warDeployPath) {
        this.warDeployPath = warDeployPath;
    }

    @Override
    public String toString() {
        return "JpaApplication{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", group=" + group +
                ", webAppContext='" + webAppContext + '\'' +
                ", warPath='" + warPath + '\'' +
                ", warDeployPath='" + warDeployPath + '\'' +
                ", documentRoot='" + documentRoot + '\'' +
                ", secure=" + secure +
                ", loadBalanceAcrossServers=" + loadBalanceAcrossServers +
                ", unpackWar=" + unpackWar +
                ", warName='" + warName + '\'' +
                '}';
    }
}
