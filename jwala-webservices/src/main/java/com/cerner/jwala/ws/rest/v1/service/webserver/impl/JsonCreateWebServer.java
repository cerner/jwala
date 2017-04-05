package com.cerner.jwala.ws.rest.v1.service.webserver.impl;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.id.IdentifierSetBuilder;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.request.webserver.CreateWebServerRequest;
import com.cerner.jwala.ws.rest.v1.json.AbstractJsonDeserializer;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@JsonDeserialize(using = JsonCreateWebServer.JsonCreateWebServerDeserializer.class)
public class JsonCreateWebServer {

    private final Set<String> groupIds;
    private final String webserverName;
    private final String portNumber;
    private final String hostName;
    private final String httpsPort;
    private final String statusPath;
    private final String svrRoot;
    private final String docRoot;

    public JsonCreateWebServer(final String theName,
                               final String theHostName,
                               final String thePortNumber,
                               final String theHttpsPort,
                               final Set<String> theGroupIds,
                               final String theStatusPath,
                               final String theSvrRoot,
                               final String theDocRoot) {
        webserverName = theName;
        hostName = theHostName;
        portNumber = thePortNumber;
        httpsPort = theHttpsPort;
        groupIds = Collections.unmodifiableSet(new HashSet<>(theGroupIds));
        statusPath = theStatusPath;
        svrRoot = theSvrRoot;
        docRoot = theDocRoot;
    }

    public CreateWebServerRequest toCreateWebServerRequest() {
        final Set<Identifier<Group>> ids = new IdentifierSetBuilder(groupIds).build();

        final Integer port = convertFrom(portNumber,
                FaultType.INVALID_WEBSERVER_PORT);
        final Integer securePort = convertIfPresentFrom(httpsPort,
                FaultType.INVALID_WEBSERVER_HTTPS_PORT,
                null);

        return new CreateWebServerRequest(ids, webserverName, hostName, port, securePort, new Path(statusPath),
                WebServerReachableState.WS_NEW);
    }

    private Integer convertFrom(final String aValue,
                                FaultType aFaultType) {
        try {
            return Integer.valueOf(aValue);
        } catch (final NumberFormatException nfe) {
            throw new BadRequestException(aFaultType,
                    nfe.getMessage(),
                    nfe);
        }
    }

    private Integer convertIfPresentFrom(final String aValue,
                                         final FaultType aFaultType,
                                         final Integer aDefault) {
        if (aValue != null && !"".equals(aValue.trim())) {
            return convertFrom(aValue,
                    aFaultType);
        }

        return aDefault;
    }

    @Override
    public String toString() {
        return "JsonCreateWebServer{" +
                "groupIds=" + groupIds +
                ", webserverName='" + webserverName + '\'' +
                ", portNumber='" + portNumber + '\'' +
                ", hostName='" + hostName + '\'' +
                ", httpsPort='" + httpsPort + '\'' +
                ", statusPath='" + statusPath + '\'' +
                ", svrRoot='" + svrRoot + '\'' +
                ", docRoot='" + docRoot + '\'' +
                '}';
    }

    static class JsonCreateWebServerDeserializer extends AbstractJsonDeserializer<JsonCreateWebServer> {
        public JsonCreateWebServerDeserializer() {
        }

        @Override
        public JsonCreateWebServer deserialize(final JsonParser jp, final DeserializationContext ctxt)
                throws IOException {

            final ObjectCodec obj = jp.getCodec();
            final JsonNode node = obj.readTree(jp).get(0);

            final JsonCreateWebServer jcws = new JsonCreateWebServer(node.get("webserverName").getTextValue(),
                    node.get("hostName").getTextValue(),
                    node.get("portNumber").getValueAsText(),
                    node.get("httpsPort").getValueAsText(),
                       deserializeGroupIdentifiers(node),
                    node.get("statusPath").getTextValue(),
                    node.get("svrRoot").getTextValue(),
                    node.get("docRoot").getTextValue());
            return jcws;
        }
    }
}
