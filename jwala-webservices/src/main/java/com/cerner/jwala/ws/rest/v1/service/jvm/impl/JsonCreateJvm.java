package com.cerner.jwala.ws.rest.v1.service.jvm.impl;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.request.jvm.CreateJvmAndAddToGroupsRequest;
import com.cerner.jwala.ws.rest.v1.service.json.PasswordDeserializer;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.*;

@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonCreateJvm {

    private String jvmName;
    private String hostName;
    private String httpPort;
    private String httpsPort;
    private String redirectPort;
    private String shutdownPort;
    private String ajpPort;
    private String statusPath;
    private String systemProperties;
    private String userName;

    @JsonDeserialize(using = PasswordDeserializer.class)
    private String encryptedPassword;

    private String jdkMediaId;
    private String tomcatMediaId;

    private List<GroupIdWrapper> groupIds;

    // Required by Jackson deserializer
    public JsonCreateJvm() {}

    public JsonCreateJvm(final String theJvmName,
                         final String theHostName,
                         final String theHttpPort,
                         final String theHttpsPort,
                         final String theRedirectPort,
                         final String theShutdownPort,
                         final String theAjpPort,
                         final String theStatusPath,
                         final String theSystemProperties,
                         final String theUsername,
                         final String theEncryptedPassword,
                         final String jdkMediaId,
                         final String tomcatMediaId) {
        this(theJvmName,
             theHostName,
             Collections.<String>emptySet(),
             theHttpPort,
             theHttpsPort,
             theRedirectPort,
             theShutdownPort,
             theAjpPort,
             theStatusPath,
             theSystemProperties,
             theUsername,
             theEncryptedPassword,
             jdkMediaId,
             tomcatMediaId);
    }

    public JsonCreateJvm(final String theJvmName,
                         final String theHostName,
                         final Set<String> someGroupIds,
                         final String theHttpPort,
                         final String theHttpsPort,
                         final String theRedirectPort,
                         final String theShutdownPort,
                         final String theAjpPort,
                         final String theStatusPath,
                         final String theSystemProperties,
                         final String theUsername,
                         final String theEncrypedPassword,
                         final String theJdkMediaId,
                         final String tomcatMediaId) {
        jvmName = theJvmName;
        hostName = theHostName;
        httpPort = theHttpPort;
        httpsPort = theHttpsPort;
        redirectPort = theRedirectPort;
        shutdownPort = theShutdownPort;
        ajpPort = theAjpPort;
        statusPath = theStatusPath;
        systemProperties = theSystemProperties;

        groupIds = new ArrayList<>();
        for (final String id : someGroupIds) {
            final GroupIdWrapper groupIdWrapper = new GroupIdWrapper();
            groupIdWrapper.setGroupId(id);
            groupIds.add(groupIdWrapper);
        }

        userName = theUsername;
        encryptedPassword = theEncrypedPassword;
        jdkMediaId = theJdkMediaId;
        this.tomcatMediaId = tomcatMediaId;
    }

    public CreateJvmAndAddToGroupsRequest toCreateAndAddRequest() {
        final Set<Identifier<Group>> groups = convertGroupIds();

        return new CreateJvmAndAddToGroupsRequest(jvmName,
                hostName,
                groups,
                JsonUtilJvm.stringToInteger(httpPort),
                JsonUtilJvm.stringToInteger(httpsPort),
                JsonUtilJvm.stringToInteger(redirectPort),
                JsonUtilJvm.stringToInteger(shutdownPort),
                JsonUtilJvm.stringToInteger(ajpPort),
                new Path(statusPath),
                systemProperties,
                userName,
                encryptedPassword,
                jdkMediaId == null ? null : new Identifier<>(Long.parseLong(jdkMediaId)),
                tomcatMediaId == null ? null : new Identifier<>(Long.parseLong(tomcatMediaId)));
    }

    @SuppressWarnings("unchecked")
    protected Set<Identifier<Group>> convertGroupIds() {
        final Set groupIdSet = new HashSet<>();
        for (final GroupIdWrapper groupIdWrapper : groupIds) {
            groupIdSet.add(new Identifier<>(groupIdWrapper.getGroupId()));
        }
        return groupIdSet;
    }

    /*** Setters and Getters: Start ***/

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

    public String getStatusPath() {
        return statusPath;
    }

    public void setStatusPath(String statusPath) {
        this.statusPath = statusPath;
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

    public String getJdkMediaId() {
        return jdkMediaId;
    }

    public void setJdkMediaId(String jdkMediaId) {
        this.jdkMediaId = jdkMediaId;
    }

    public String getTomcatMediaId() {
        return tomcatMediaId;
    }

    public void setTomcatMediaId(String tomcatMediaId) {
        this.tomcatMediaId = tomcatMediaId;
    }

    public List<GroupIdWrapper> getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(List<GroupIdWrapper> groupIds) {
        this.groupIds = groupIds;
    }

    /*** Setters and Getters: End ***/

    @Override
    public String toString() {
        return "JsonCreateJvm{" +
                "jvmName='" + jvmName + '\'' +
                ", hostName='" + hostName + '\'' +
                ", httpPort='" + httpPort + '\'' +
                ", httpsPort='" + httpsPort + '\'' +
                ", redirectPort='" + redirectPort + '\'' +
                ", shutdownPort='" + shutdownPort + '\'' +
                ", ajpPort='" + ajpPort + '\'' +
                ", statusPath='" + statusPath + '\'' +
                ", systemProperties='" + systemProperties + '\'' +
                ", userName='" + userName + '\'' +
                ", encryptedPassword='" + encryptedPassword + '\'' +
                ", jdkMediaId='" + jdkMediaId + '\'' +
                ", tomcatMediaId='" + tomcatMediaId + '\'' +
                ", groupIds=" + groupIds +
                '}';
    }
}
