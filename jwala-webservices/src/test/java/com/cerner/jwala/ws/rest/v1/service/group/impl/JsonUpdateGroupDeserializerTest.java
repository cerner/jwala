package com.cerner.jwala.ws.rest.v1.service.group.impl;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.request.group.UpdateGroupRequest;
import com.cerner.jwala.ws.rest.v1.service.JsonDeserializationBehavior;
import com.cerner.jwala.ws.rest.v1.service.group.impl.JsonUpdateGroup;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.cerner.jwala.ws.rest.v1.service.JsonDeserializationBehavior.keyTextValue;
import static com.cerner.jwala.ws.rest.v1.service.JsonDeserializationBehavior.object;
import static org.junit.Assert.assertEquals;

public class JsonUpdateGroupDeserializerTest {

    private ObjectMapper mapper;

    @Before
    public void setup() {
        mapper = new JsonDeserializationBehavior().addMapping(JsonUpdateGroup.class, new JsonUpdateGroup.JsonUpdateGroupDeserializer()).toObjectMapper();
    }

    @Test
    public void testDeserialize() throws Exception {

        final String groupId = "1";
        final String groupName = "a group name";
        final String json = object(keyTextValue("id", groupId),
                                   keyTextValue("name", groupName));

        final JsonUpdateGroup update = readUpdate(json);

        verifyAssertions(update,
                         groupId,
                         groupName);
    }

    @Test(expected = IOException.class)
    public void testDeserializeInvalidJson() throws Exception {

        final String json = "alksdjfl;askdjga;lskda;sdlf4kjas;df4kljasd;f";

        final JsonUpdateGroup update = readUpdate(json);
    }

    @Test(expected = BadRequestException.class)
    public void testBadGroupIdentifier() throws Exception {

        final String groupId = "this is not a valid group identifier";
        final String groupName = "a group name";
        final String json = object(keyTextValue("id", groupId),
                                   keyTextValue("name", groupName));

        final JsonUpdateGroup update = readUpdate(json);

        verifyAssertions(update,
                         groupId,
                         groupName);
    }

    protected JsonUpdateGroup readUpdate(final String someJson) throws IOException {
        return mapper.readValue(someJson, JsonUpdateGroup.class);
    }

    protected void verifyAssertions(final JsonUpdateGroup anUpdate,
                                    final String aGroupId,
                                    final String aGroupName) {

        final UpdateGroupRequest updateCommand = anUpdate.toUpdateGroupCommand();

        assertEquals(new Identifier<Group>(aGroupId),
                     updateCommand.getId());
        assertEquals(aGroupName,
                     updateCommand.getNewName());
    }
}
