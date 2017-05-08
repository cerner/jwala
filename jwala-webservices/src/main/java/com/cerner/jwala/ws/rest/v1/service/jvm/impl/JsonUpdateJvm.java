package com.cerner.jwala.ws.rest.v1.service.jvm.impl;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.media.Media;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.request.jvm.UpdateJvmRequest;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonUpdateJvm extends JsonCreateJvm {

    private String jvmId;

    // Required by Jackson deserializer
    public JsonUpdateJvm() {
    }

    public JsonUpdateJvm(final String theJvmId,
                         final String theJvmName,
                         final String theHostName,
                         final Set<String> someGroupIds,
                         final String theHttpPort,
                         final String theHttpsPort,
                         final String theRedirectPort,
                         final String theShutdownPort,
                         final String theAjpPort,
                         final String theStatusPath,
                         final String theSystemProperties,
                         final String theUserName,
                         final String theEncryptedPassword,
                         final String theJdkMediaId,
                         final String tomcatMediaId) {
        jvmId = theJvmId;
        setJvmName(theJvmName);
        setHostName(theHostName);

        final List<GroupIdWrapper> groupIdWrappers = new ArrayList<>();
        for (final String id : someGroupIds) {
            final GroupIdWrapper groupIdWrapper = new GroupIdWrapper();
            groupIdWrapper.setGroupId(id);
            groupIdWrappers.add(groupIdWrapper);
        }
        setGroupIds(groupIdWrappers);

        setHttpPort(theHttpPort);
        setHttpsPort(theHttpsPort);
        setRedirectPort(theRedirectPort);
        setShutdownPort(theShutdownPort);
        setAjpPort(theAjpPort);
        setStatusPath(theStatusPath);
        setSystemProperties(theSystemProperties);
        setUserName(theUserName);
        setEncryptedPassword(theEncryptedPassword);
        setJdkMediaId(theJdkMediaId);
        setTomcatMediaId(tomcatMediaId);
    }

    public UpdateJvmRequest toUpdateJvmRequest() throws BadRequestException {

        Identifier<Media> jdkMediaId = null;
        if (getJdkMediaId() != null && !getJdkMediaId().isEmpty()) {
            jdkMediaId = new Identifier<>(getJdkMediaId());
        }

        Identifier<Media> tomcatMediaId = null;
        if (getTomcatMediaId() != null && !getTomcatMediaId().isEmpty()) {
            tomcatMediaId = new Identifier<>(getTomcatMediaId());
        }

        return new UpdateJvmRequest(convertJvmId(),
                getJvmName(),
                getHostName(),
                convertGroupIds(),
                JsonUtilJvm.stringToInteger(getHttpPort()),
                JsonUtilJvm.stringToInteger(getHttpsPort()),
                JsonUtilJvm.stringToInteger(getRedirectPort()),
                JsonUtilJvm.stringToInteger(getShutdownPort()),
                JsonUtilJvm.stringToInteger(getAjpPort()),
                new Path(getStatusPath()),
                getSystemProperties(),
                getUserName(),
                getEncryptedPassword(),
                jdkMediaId,
                tomcatMediaId);
    }

    private Identifier<Jvm> convertJvmId() {
        try {
            return new Identifier<>(jvmId);
        } catch (final NumberFormatException nfe) {
            throw new BadRequestException(FaultType.INVALID_IDENTIFIER, nfe.getMessage(), nfe);
        }
    }

    public String getJvmId() {
        return jvmId;
    }

    public void setJvmId(String jvmId) {
        this.jvmId = jvmId;
    }

}
