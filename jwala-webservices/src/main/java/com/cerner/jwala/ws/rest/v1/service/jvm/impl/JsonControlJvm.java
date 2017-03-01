package com.cerner.jwala.ws.rest.v1.service.jvm.impl;

import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;
import com.cerner.jwala.ws.rest.v1.json.AbstractJsonDeserializer;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import java.io.IOException;

@JsonDeserialize(using = JsonControlJvm.JsonControlJvmDeserializer.class)
public class JsonControlJvm {

    private final String controlOperation;

    public JsonControlJvm(final String theControlOperation) {
        if (theControlOperation != null) {
            controlOperation = theControlOperation;
        } else {
            controlOperation = "";
        }
    }

    public JvmControlOperation toControlOperation() {
        return JvmControlOperation.convertFrom(controlOperation);
    }

    @Override
    public String toString() {
        return "JsonControlJvm{" +
                "controlOperation='" + controlOperation + '\'' +
                '}';
    }

    static class JsonControlJvmDeserializer extends AbstractJsonDeserializer<JsonControlJvm> {

        public JsonControlJvmDeserializer() {
        }

        @Override
        public JsonControlJvm deserialize(final JsonParser jp,
                                          final DeserializationContext ctxt) throws IOException {

            final ObjectCodec obj = jp.getCodec();
            final JsonNode rootNode = obj.readTree(jp);
            final JsonNode operation = rootNode.get("controlOperation");

            return new JsonControlJvm(operation.getTextValue());
        }
    }
}
