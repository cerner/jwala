package com.cerner.jwala.ws.rest.v1.service.group.impl;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.id.IdentifierSetBuilder;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.request.group.AddJvmToGroupRequest;
import com.cerner.jwala.common.request.group.AddJvmsToGroupRequest;
import com.cerner.jwala.ws.rest.v1.service.JsonDeserializationBehavior;
import com.cerner.jwala.ws.rest.v1.service.group.impl.JsonJvms;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import static com.cerner.jwala.ws.rest.v1.service.JsonDeserializationBehavior.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JsonJvmsDeserializerTest {

    private ObjectMapper mapper;

    @Before
    public void setup() {

        mapper = new JsonDeserializationBehavior().addMapping(JsonJvms.class, new JsonJvms.JsonJvmsDeserializer()).toObjectMapper();
    }

    @Test
    public void testSingleValueFromMultiple() throws Exception {

        final String json = object(keyValue("jvmIds", array(object(keyTextValue("jvmId", "1")))));

        final JsonJvms jvms = readJvms(json);

        verifyAssertions(jvms,
                "1");
    }

    @Test
    public void testMultipleValue() throws Exception {

        final String firstJvmId = "1";
        final String secondJvmId = "2";

        final String json = object(keyValue("jvmIds", array(object(keyTextValue("jvmId", firstJvmId)),
                                                            object(keyTextValue("jvmId", secondJvmId)))));

        final JsonJvms jvms = readJvms(json);

        verifyAssertions(jvms,
                         firstJvmId,
                         secondJvmId);
    }

    @Test
    public void testSingleValue() throws Exception {

        final String jvmId = "1";
        final String json = object(keyTextValue("jvmId", jvmId));

        final JsonJvms jvms = readJvms(json);

        verifyAssertions(jvms,
                         jvmId);
    }

    @Test(expected = IOException.class)
    public void testInvalidJson() throws Exception {

        final String json = "alksd';";

        final JsonJvms jvms = readJvms(json);
    }

    @Test(expected = BadRequestException.class)
    public void testInvalidIdentifier() throws Exception {

        final String jvmId = "this is not a valid identifier";
        final String json = object(keyTextValue("jvmId", jvmId));

        final JsonJvms jvms = readJvms(json);

        verifyAssertions(jvms,
                         jvmId);
    }

    protected void verifyAssertions(final JsonJvms someJvms,
                                    final String... expectedIds) {

        final Identifier<Group> groupId = new Identifier<>(123456L);
        final AddJvmsToGroupRequest addCommand = someJvms.toCommand(groupId);
        final Set<AddJvmToGroupRequest> jvmCommands = addCommand.toRequests();
        final Set<Identifier<Jvm>> expectedJvmIds = new IdentifierSetBuilder(Arrays.asList(expectedIds)).build();

        assertEquals(expectedJvmIds.size(),
                     jvmCommands.size());

        for (final AddJvmToGroupRequest jvmCommand : jvmCommands) {
            assertTrue(expectedJvmIds.contains(jvmCommand.getJvmId()));
            assertEquals(groupId,
                         jvmCommand.getGroupId());
        }
    }

    protected JsonJvms readJvms(final String someJson) throws IOException {
        return mapper.readValue(someJson, JsonJvms.class);
    }
}
