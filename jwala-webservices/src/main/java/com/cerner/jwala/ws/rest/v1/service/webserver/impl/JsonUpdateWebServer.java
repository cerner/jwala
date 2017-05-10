package com.cerner.jwala.ws.rest.v1.service.webserver.impl;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.id.IdentifierSetBuilder;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.request.webserver.UpdateWebServerRequest;
import com.cerner.jwala.ws.rest.v1.json.AbstractJsonDeserializer;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import java.io.IOException;
import java.util.Set;

@JsonDeserialize(using = JsonUpdateWebServer.JsonUpdateWebServerDeserializer.class)
public class JsonUpdateWebServer {

    private final Set<String> groupIds;
    private final String webServerId;
    private final String webServerName;
    private final String portNumber;
    private final String httpsPort;
    private final String hostName;
    private final String statusPath;
    private final String apacheHttpdMediaId;

    public JsonUpdateWebServer(final String aWebServerId,
                               final String aWebServerName,
                               final String aHostName,
                               final String aPortNumber,
                               final String aHttpsPort,
                               final Set<String> someGroupIds,
                               final String aStatusPath,
                               final String apacheHttpdMediaId) {

        webServerName = aWebServerName;
        hostName = aHostName;
        portNumber = aPortNumber;
        httpsPort = aHttpsPort;
        webServerId = aWebServerId;
        groupIds = someGroupIds;
        statusPath = aStatusPath;
        this.apacheHttpdMediaId = apacheHttpdMediaId;
    }

    public UpdateWebServerRequest toUpdateWebServerRequest() {
        final Set<Identifier<Group>> groups = new IdentifierSetBuilder(groupIds).build();
        return new UpdateWebServerRequest(convertWebServerId(), groups, webServerName, hostName, convertPortNumber(),
                convertHttpsPortNumber(), new Path(statusPath), WebServerReachableState.WS_UNREACHABLE, apacheHttpdMediaId);
    }

    protected Identifier<WebServer> convertWebServerId() {
        try {
            return new Identifier<>(webServerId);
        } catch (final NumberFormatException nfe) {
            throw new BadRequestException(FaultType.INVALID_IDENTIFIER, nfe.getMessage(), nfe);
        }
    }

    protected Integer convertPortNumber() {
        try {
            return Integer.valueOf(portNumber);
        } catch (final NumberFormatException nfe) {
            throw new BadRequestException(FaultType.INVALID_WEBSERVER_PORT, nfe.getMessage(), nfe);
        }
    }

    protected Integer convertHttpsPortNumber() {
        try {
            if (httpsPort != null && !"".equals(httpsPort.trim())) {
                return Integer.valueOf(httpsPort);
            }
            return null;
        } catch (final NumberFormatException nfe) {
            throw new BadRequestException(FaultType.INVALID_WEBSERVER_HTTPS_PORT, nfe.getMessage(), nfe);
        }
    }

    @Override
    public String toString() {
        return "JsonUpdateWebServer{" +
                "groupIds=" + groupIds +
                ", webServerId='" + webServerId + '\'' +
                ", webServerName='" + webServerName + '\'' +
                ", portNumber='" + portNumber + '\'' +
                ", httpsPort='" + httpsPort + '\'' +
                ", hostName='" + hostName + '\'' +
                ", statusPath='" + statusPath + '\'' +
                '}';
    }

    static class JsonUpdateWebServerDeserializer extends AbstractJsonDeserializer<JsonUpdateWebServer> {

        public JsonUpdateWebServerDeserializer() {
        }
        @Override
        public JsonUpdateWebServer deserialize(final JsonParser jp, final DeserializationContext ctxt)
                throws IOException {

            final ObjectCodec obj = jp.getCodec();
            final JsonNode node = obj.readTree(jp).get(0);

            final Set<String> groupIds = deserializeGroupIdentifiers(node);
            return new JsonUpdateWebServer(node.get("webserverId").getValueAsText(),
                                           node.get("webserverName").getTextValue(),
                                           node.get("hostName").getTextValue(),
                                           node.get("portNumber").getValueAsText(),
                                           node.get("httpsPort").getValueAsText(),
                                           groupIds,
                                           node.get("statusPath").getTextValue(),
                                           node.get("apacheHttpdMediaId").getTextValue());
        }
    }
}