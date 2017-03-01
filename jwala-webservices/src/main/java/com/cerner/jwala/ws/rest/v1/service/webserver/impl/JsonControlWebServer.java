package com.cerner.jwala.ws.rest.v1.service.webserver.impl;

import com.cerner.jwala.common.domain.model.webserver.WebServerControlOperation;
import com.cerner.jwala.ws.rest.v1.json.AbstractJsonDeserializer;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import java.io.IOException;

@JsonDeserialize(using = JsonControlWebServer.JsonControlWebServerDeserializer.class)
public class JsonControlWebServer {

    private final String controlOperation;

    public JsonControlWebServer(final String theControlOperation) {
        if (theControlOperation != null) {
            controlOperation = theControlOperation;
        } else {
            controlOperation = "";
        }
    }

    public WebServerControlOperation toControlOperation() {
        return WebServerControlOperation.convertFrom(controlOperation);
    }

    @Override
    public String toString() {
        return "JsonControlWebServer{" +
                "controlOperation='" + controlOperation + '\'' +
                '}';
    }

    static class JsonControlWebServerDeserializer extends AbstractJsonDeserializer<JsonControlWebServer> {

        public JsonControlWebServerDeserializer() {
        }

        @Override
        public JsonControlWebServer deserialize(final JsonParser jp,
                                          final DeserializationContext ctxt) throws IOException {

            final ObjectCodec obj = jp.getCodec();
            final JsonNode rootNode = obj.readTree(jp);
            final JsonNode operation = rootNode.get("controlOperation");

            return new JsonControlWebServer(operation.getTextValue());
        }
    }
}
