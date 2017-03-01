package com.cerner.jwala.ws.rest.v1.service.group.impl;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.request.group.UpdateGroupRequest;

import java.io.IOException;

@JsonDeserialize(using = JsonUpdateGroup.JsonUpdateGroupDeserializer.class)
public class JsonUpdateGroup {
    private final String id;
    private final String name;

    public JsonUpdateGroup(final String theId,
                           final String theName) {
        id = theId;
        name = theName;
    }

    public UpdateGroupRequest toUpdateGroupCommand() {
        try {
            final Identifier<Group> groupId = new Identifier<>(id);
            return new UpdateGroupRequest(groupId, name);
        } catch (final NumberFormatException nfe) {
            throw new BadRequestException(FaultType.INVALID_GROUP_NAME,
                                          nfe.getMessage(),
                                          nfe);
        }
    }

    static class JsonUpdateGroupDeserializer extends JsonDeserializer<JsonUpdateGroup> {

        public JsonUpdateGroupDeserializer() {
        }

        @Override
        public JsonUpdateGroup deserialize(final JsonParser jp,
                                           final DeserializationContext ctxt) throws IOException {

            final ObjectCodec obj = jp.getCodec();
            final JsonNode node = obj.readTree(jp);

            return new JsonUpdateGroup(node.get("id").getTextValue(),
                                       node.get("name").getTextValue());
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "JsonUpdateGroup{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
