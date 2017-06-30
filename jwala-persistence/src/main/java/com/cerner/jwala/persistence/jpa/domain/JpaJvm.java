package com.cerner.jwala.persistence.jpa.domain;

import com.cerner.jwala.common.domain.model.jvm.JvmState;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "jvm", uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
@NamedQueries({
        @NamedQuery(name = JpaJvm.QUERY_FIND_JVM_BY_GROUP_AND_JVM_NAME,
                query = "SELECT j FROM JpaJvm j WHERE LOWER(j.name) = LOWER(:jvmName) AND LOWER(j.groups.name) = LOWER(:groupName)"),
        @NamedQuery(name = JpaJvm.QUERY_UPDATE_STATE_BY_ID, query = "UPDATE JpaJvm j SET j.state = :state, j.lastUpdateDate = CURRENT_TIMESTAMP WHERE j.id = :id"),
        @NamedQuery(name = JpaJvm.QUERY_UPDATE_ERROR_STATUS_BY_ID, query = "UPDATE JpaJvm j SET j.errorStatus = :errorStatus, j.lastUpdateDate = CURRENT_TIMESTAMP  WHERE j.id = :id"),
        @NamedQuery(name = JpaJvm.QUERY_UPDATE_STATE_AND_ERR_STS_BY_ID, query = "UPDATE JpaJvm j SET j.state = :state, j.errorStatus = :errorStatus, j.lastUpdateDate = CURRENT_TIMESTAMP WHERE j.id = :id"),
        @NamedQuery(name = JpaJvm.QUERY_GET_JVM_COUNT_BY_STATE_AND_GROUP_NAME, query = "SELECT COUNT(1) FROM JpaJvm j WHERE j.state = :state AND LOWER(j.groups.name) = LOWER(:groupName)"),
        @NamedQuery(name = JpaJvm.QUERY_GET_JVM_COUNT_BY_GROUP_NAME, query = "SELECT COUNT(1) FROM JpaJvm j WHERE LOWER(j.groups.name) = LOWER(:groupName)"),
        @NamedQuery(name = JpaJvm.QUERY_GET_JVMS_BY_GROUP_NAME, query = "SELECT j FROM JpaJvm j WHERE LOWER(j.groups.name) = LOWER(:groupName) ORDER by j.name")
})
public class JpaJvm extends AbstractEntity<JpaJvm> {

    public static final String QUERY_FIND_JVM_BY_GROUP_AND_JVM_NAME = "findJvmByGroupAndJvmName";
    public static final String QUERY_UPDATE_STATE_BY_ID = "updateJvmStateById";
    public static final String QUERY_UPDATE_ERROR_STATUS_BY_ID = "updateJvmErrorStatusById";
    public static final String QUERY_UPDATE_STATE_AND_ERR_STS_BY_ID = "updateJvmStateAndErrStsById";
    public static final String QUERY_GET_JVM_COUNT_BY_STATE_AND_GROUP_NAME = "getJvmCountByStateAndGroupName";
    public static final String QUERY_GET_JVM_COUNT_BY_GROUP_NAME = "getJvmCountByGroupName";
    public static final java.lang.String QUERY_GET_JVMS_BY_GROUP_NAME = "getJvmsByGroupName";

    public static final String QUERY_PARAM_ID = "id";

    @Deprecated
    public static final String QUERY_PARAM_JVM_NAME = "jvmName";
    public static final String QUERY_PARAM_NAME = "name";

    public static final String QUERY_PARAM_STATE = "state";
    public static final String QUERY_PARAM_ERROR_STATUS = "errorStatus";
    public static final String QUERY_PARAM_GROUP_NAME = "groupName";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    public String name;

    private String hostName;

    @ManyToMany(mappedBy = "jvms", fetch = FetchType.EAGER)
    private List<JpaGroup> groups;

    @Column(nullable = false)
    private Integer httpPort;

    private Integer httpsPort;

    @Column(nullable = false)
    private Integer redirectPort;

    @Column(nullable = false)
    private Integer shutdownPort;

    @Column(nullable = false)
    private Integer ajpPort;

    @Column(nullable = false)
    private String statusPath;

    private String systemProperties;

    @Enumerated(EnumType.STRING)
    private JvmState state;

    @Column(name = "ERR_STS", length = 2147483647)
    private String errorStatus = "";

    @Column(nullable = true)
    private String userName;

    @Column(nullable = true)
    private String encryptedPassword;

    @OneToOne (targetEntity = JpaMedia.class)
    @Column(nullable = true)
    private JpaMedia jdkMedia;

    @OneToOne (targetEntity = JpaMedia.class)
    @Column(nullable = true)
    private JpaMedia tomcatMedia;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getStatusPath() {
        return statusPath;
    }

    public void setStatusPath(final String statusPath) {
        this.statusPath = statusPath;
    }

    public List<JpaGroup> getGroups() {
        return groups;
    }

    public void setGroups(final List<JpaGroup> groups) {
        this.groups = groups;
    }

    public Integer getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(Integer httpPort) {
        this.httpPort = httpPort;
    }

    public Integer getHttpsPort() {
        return httpsPort;
    }

    public void setHttpsPort(Integer httpsPort) {
        this.httpsPort = httpsPort;
    }

    public Integer getRedirectPort() {
        return redirectPort;
    }

    public void setRedirectPort(Integer redirectPort) {
        this.redirectPort = redirectPort;
    }

    public Integer getShutdownPort() {
        return shutdownPort;
    }

    public void setShutdownPort(Integer shutdownPort) {
        this.shutdownPort = shutdownPort;
    }

    public Integer getAjpPort() {
        return ajpPort;
    }

    public void setAjpPort(Integer ajpPort) {
        this.ajpPort = ajpPort;
    }

    public String getSystemProperties() {
        return systemProperties;
    }

    public void setSystemProperties(String systemProperties) {
        this.systemProperties = systemProperties;
    }

    public JvmState getState() {
        return state;
    }

    public void setState(JvmState state) {
        this.state = state;
    }

    public String getErrorStatus() {
        return errorStatus;
    }

    public void setErrorStatus(String errorStatus) {
        this.errorStatus = errorStatus;
    }

    @Override
    protected void prePersist() {
        super.prePersist();
        if (state == null) {
            state = JvmState.JVM_NEW;
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

        JpaJvm jpaJvm = (JpaJvm) o;

        return id.equals(jpaJvm.id);

    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "JpaJvm{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", hostName='" + hostName + '\'' +
                ", groups=" + groups +
                ", httpPort=" + httpPort +
                ", httpsPort=" + httpsPort +
                ", redirectPort=" + redirectPort +
                ", shutdownPort=" + shutdownPort +
                ", ajpPort=" + ajpPort +
                ", statusPath='" + statusPath + '\'' +
                ", systemProperties='" + systemProperties + '\'' +
                ", userName='" + (userName == null ? "<null>" : userName) + '\'' +
                '}';
    }

    public JpaMedia getJdkMedia() {
        return jdkMedia;
    }

    public JpaMedia getTomcatMedia() {
        return tomcatMedia;
    }

    public void setJdkMedia(JpaMedia jdkMedia) {
        this.jdkMedia = jdkMedia;
    }

    public void setTomcatMedia(JpaMedia tomcatMedia) {
        this.tomcatMedia = tomcatMedia;
    }
}