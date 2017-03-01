package com.cerner.jwala.ws.rest.v2.service.jvm;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;

/**
 * POJO that wraps JVM related data
 *
 * Created by Jedd Cuison on 8/9/2016.
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class JvmRequestData {

    private String jvmName;
    private String hostName;
    private String statusPath;
    private String [] groupNames;
    private String httpPort;
    private String httpsPort;
    private String redirectPort;
    private String shutdownPort;
    private String ajpPort;
    private String systemProperties;
    private String userName;
    private String encryptedPassword;

    public String getJvmName() {
        return jvmName;
    }

    public void setJvmName(String jvmName) {
        this.jvmName = jvmName;
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

    public void setStatusPath(String statusPath) {
        this.statusPath = statusPath;
    }

    public String getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(String httpPort) {
        this.httpPort = httpPort;
    }

    public String getHttpsPort() {
        return httpsPort;
    }

    public void setHttpsPort(String httpsPort) {
        this.httpsPort = httpsPort;
    }

    public String getRedirectPort() {
        return redirectPort;
    }

    public void setRedirectPort(String redirectPort) {
        this.redirectPort = redirectPort;
    }

    public String getShutdownPort() {
        return shutdownPort;
    }

    public void setShutdownPort(String shutdownPort) {
        this.shutdownPort = shutdownPort;
    }

    public String getAjpPort() {
        return ajpPort;
    }

    public void setAjpPort(String ajpPort) {
        this.ajpPort = ajpPort;
    }

    public String getSystemProperties() {
        return systemProperties;
    }

    public void setSystemProperties(String systemProperties) {
        this.systemProperties = systemProperties;
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

    public String[] getGroupNames() {
        return groupNames;
    }

    public void setGroupNames(String [] groupNames) {
        this.groupNames = Arrays.copyOf(groupNames, groupNames.length, String[].class);
    }
}
