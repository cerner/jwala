package com.cerner.jwala.common.request.jvm;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.media.Media;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.request.Request;
import com.cerner.jwala.common.rule.*;
import com.cerner.jwala.common.rule.jvm.JvmNameRule;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

public class CreateJvmRequest implements Serializable, Request {

    private final String jvmName;
    private final String hostName;

    // JVM ports
    private final Integer httpPort;
    private final Integer httpsPort;
    private final Integer redirectPort;
    private final Integer shutdownPort;
    private final Integer ajpPort;

    private final Path statusPath;

    private final String systemsProperties;
    private final String userName;
    private final String encryptedPassword;
    private final Identifier<Media> jdkMediaId;
    private final Identifier<Media> tomcatMediaId;

    public CreateJvmRequest(final String theName,
                            final String theHostName,
                            final Integer theHttpPort,
                            final Integer theHttpsPort,
                            final Integer theRedirectPort,
                            final Integer theShutdownPort,
                            final Integer theAjpPort,
                            final Path theStatusPath,
                            final String theSystemProperties,
                            final String theUserName,
                            final String theEncryptedPassword,
                            final Identifier<Media> jdkMediaId,
                            final Identifier<Media> tomcatMediaId) {
        jvmName = theName;
        hostName = theHostName;
        httpPort = theHttpPort;
        httpsPort = theHttpsPort;
        redirectPort = theRedirectPort;
        shutdownPort = theShutdownPort;
        ajpPort = theAjpPort;
        statusPath = theStatusPath;
        systemsProperties = theSystemProperties;
        userName = theUserName;
        encryptedPassword = theEncryptedPassword;
        this.jdkMediaId = jdkMediaId;
        this.tomcatMediaId = tomcatMediaId;
    }

    public String getJvmName() {
        return jvmName;
    }

    public String getHostName() {
        return hostName;
    }

    public Integer getHttpPort() {
        return httpPort;
    }

    public Integer getHttpsPort() {
        return httpsPort;
    }

    public Integer getRedirectPort() {
        return redirectPort;
    }

    public Integer getShutdownPort() {
        return shutdownPort;
    }

    public Integer getAjpPort() {
        return ajpPort;
    }

    public Path getStatusPath() {
        return statusPath;
    }

    public String getSystemProperties() {
        return systemsProperties;
    }

    public String getUserName() {
        return userName;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    @Override
    public void validate() {
        new MultipleRules(new JvmNameRule(jvmName),
                          new HostNameRule(hostName),
                          new StatusPathRule(statusPath),
                          new PortNumberRule(httpPort, FaultType.INVALID_JVM_HTTP_PORT),
                          new PortNumberRule(httpsPort, FaultType.INVALID_JVM_HTTPS_PORT, true),
                          new PortNumberRule(redirectPort, FaultType.INVALID_JVM_REDIRECT_PORT),
                          new ShutdownPortNumberRule(shutdownPort, FaultType.INVALID_JVM_SHUTDOWN_PORT),
                          new PortNumberRule(ajpPort, FaultType.INVALID_JVM_AJP_PORT)).validate();
    }

    public Identifier<Media> getJdkMediaId() {
        return jdkMediaId;
    }

    public Identifier<Media> getTomcatMediaId() {
        return tomcatMediaId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        CreateJvmRequest that = (CreateJvmRequest) o;

        return new EqualsBuilder()
                .append(jvmName, that.jvmName)
                .append(hostName, that.hostName)
                .append(httpPort, that.httpPort)
                .append(httpsPort, that.httpsPort)
                .append(redirectPort, that.redirectPort)
                .append(shutdownPort, that.shutdownPort)
                .append(ajpPort, that.ajpPort)
                .append(statusPath, that.statusPath)
                .append(systemsProperties, that.systemsProperties)
                .append(userName, that.userName)
                .append(encryptedPassword, that.encryptedPassword)
                .append(jdkMediaId, that.jdkMediaId)
                .append(tomcatMediaId, that.tomcatMediaId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(jvmName)
                .append(hostName)
                .append(httpPort)
                .append(httpsPort)
                .append(redirectPort)
                .append(shutdownPort)
                .append(ajpPort)
                .append(statusPath)
                .append(systemsProperties)
                .append(userName)
                .append(encryptedPassword)
                .append(jdkMediaId)
                .append(tomcatMediaId)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "CreateJvmRequest{" +
                "jvmName='" + jvmName + '\'' +
                ", hostName='" + hostName + '\'' +
                ", httpPort=" + httpPort +
                ", httpsPort=" + httpsPort +
                ", redirectPort=" + redirectPort +
                ", shutdownPort=" + shutdownPort +
                ", ajpPort=" + ajpPort +
                ", statusPath=" + statusPath +
                ", systemsProperties='" + systemsProperties + '\'' +
                ", userName='" + userName + '\'' +
                ", encryptedPassword='" + encryptedPassword + '\'' +
                ", jdkMediaId='" + jdkMediaId + '\'' +
                ", tomcatMediaId='" + tomcatMediaId + '\'' +
                '}';
    }
}
