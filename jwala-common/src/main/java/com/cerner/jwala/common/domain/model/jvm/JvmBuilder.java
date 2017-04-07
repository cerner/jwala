package com.cerner.jwala.common.domain.model.jvm;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.media.Media;
import com.cerner.jwala.common.domain.model.path.Path;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Builds a {@link Jvm} from individual values.
 */
public class JvmBuilder {
    private Identifier<Jvm> id;
    private String name;
    private String hostName;
    private Path statusPath;
    private Set<Group> groups = new HashSet<>();
    private Integer httpPort;
    private Integer httpsPort;
    private Integer redirectPort;
    private Integer shutdownPort;
    private Integer ajpPort;
    private String systemProperties;
    private JvmState state;
    private String errorStatus;
    private Calendar lastUpdatedDate;
    private String userName;
    private String encryptedPassword;
    private Media jdkMedia;
    private Media tomcatMedia;
    private String javaHome;
    private List<Application> webApps;

    public JvmBuilder setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public JvmBuilder setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
        return this;
    }

    public JvmBuilder setId(final Identifier<Jvm> anId) {
        id = anId;
        return this;
    }

    public JvmBuilder setGroups(final Set<Group> someGroups) {
        groups = someGroups;
        return this;
    }

    public JvmBuilder setName(final String aName) {
        name = aName;
        return this;
    }

    public JvmBuilder setHostName(final String aHostName) {
        hostName = aHostName;
        return this;
    }

    public JvmBuilder setStatusPath(final Path aStatusPath) {
        statusPath = aStatusPath;
        return this;
    }

    public JvmBuilder setHttpPort(Integer httpPort) {
        this.httpPort = httpPort;
        return this;
    }

    public JvmBuilder setHttpsPort(Integer httpsPort) {
        this.httpsPort = httpsPort;
        return this;
    }

    public JvmBuilder setRedirectPort(Integer redirectPort) {
        this.redirectPort = redirectPort;
        return this;
    }

    public JvmBuilder setShutdownPort(Integer shutdownPort) {
        this.shutdownPort = shutdownPort;
        return this;
    }

    public JvmBuilder setAjpPort(Integer ajpPort) {
        this.ajpPort = ajpPort;
        return this;
    }

    public JvmBuilder setSystemProperties(String systemProperties) {
        this.systemProperties = systemProperties;
        return this;
    }

    public JvmState getState() {
        return state;
    }

    public JvmBuilder setState(JvmState state) {
        this.state = state;
        return this;
    }

    public String getErrorStatus() {
        return errorStatus;
    }

    public JvmBuilder setErrorStatus(String errorStatus) {
        this.errorStatus = errorStatus;
        return this;
    }

    public JvmBuilder setLastUpdatedDate(Calendar lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
        return this;
    }

    public Jvm build() {
        return new Jvm(id, name, hostName, groups, httpPort, httpsPort, redirectPort, shutdownPort, ajpPort, statusPath,
                       systemProperties, state, errorStatus, lastUpdatedDate, userName, encryptedPassword, jdkMedia,
                       tomcatMedia, javaHome, webApps);
    }


    public JvmBuilder setJdkMedia(Media jdkMedia) {
        this.jdkMedia = jdkMedia;
        return this;
    }

    public JvmBuilder setTomcatMedia(Media tomcatMedia) {
        this.tomcatMedia = tomcatMedia;
        return this;
    }

    public String getJavaHome() {
        return javaHome;
    }

    public JvmBuilder setJavaHome(String javaHome) {
        this.javaHome = javaHome;
        return this;
    }

    public JvmBuilder setWebApps(final List<Application> webApps) {
        this.webApps = webApps;
        return this;
    }
}