package com.siemens.cto.aem.ws.rest.v1.service.jvm.impl;

import com.siemens.cto.aem.request.group.AddJvmToGroupRequest;
import com.siemens.cto.aem.request.jvm.UpdateJvmRequest;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.id.IdentifierSetBuilder;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.ws.rest.v1.service.JsonDeserializationBehavior;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import static com.siemens.cto.aem.ws.rest.v1.service.JsonDeserializationBehavior.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JsonUpdateJvmDeserializerTest {

    private ObjectMapper mapper;

    private static final String httpPort = "5";
    private static final String httpsPort = "4";
    private static final String redirectPort = "3";
    private static final String shutdownPort = "2";
    private static final String ajpPort = "1";
    private static final String statusPath = "/statusPath";
    private static final String systemProperties = "EXAMPLE_OPTS=%someEnv%/someVal";

    @Before
    public void setUp() {
        mapper = new JsonDeserializationBehavior().addMapping(JsonUpdateJvm.class, new JsonUpdateJvm.JsonUpdateJvmDeserializer()).toObjectMapper();
    }

    @Test
    public void testDeserializeUpdateMultipleGroups() throws Exception {

        final String jvmId = "1";
        final String jvmName = "a jvm name";
        final String hostName = "a host name";
        final String firstGroupId = "1";
        final String secondGroupId = "2";

        final String json = object(keyTextValue("jvmId", jvmId),
                                   keyTextValue("jvmName", jvmName),
                                   keyTextValue("hostName", hostName),
                                   keyValue("groupIds", array(object(keyTextValue("groupId", firstGroupId)),
                                                              object(keyTextValue("groupId", secondGroupId)))),
                                   keyTextValue("httpPort", httpPort),
                                   keyTextValue("httpsPort", httpsPort),
                                   keyTextValue("redirectPort", redirectPort),
                                   keyTextValue("shutdownPort", shutdownPort),
                                   keyTextValue("ajpPort", ajpPort),
                                   keyTextValue("statusPath", statusPath),
                                   keyTextValue("systemProperties", systemProperties));

        final JsonUpdateJvm update = readValue(json);

        verifyAssertions(update,
                         jvmId,
                         jvmName,
                         hostName,
                         httpPort,
                         httpsPort,
                         redirectPort,
                         shutdownPort,
                         ajpPort,
                         statusPath,
                         systemProperties,
                         firstGroupId,
                         secondGroupId);
    }

    @Test
    public void testDeserializeUpdateSingleGroupFromMultiple() throws Exception {

        final String jvmId = "1";
        final String jvmName = "a jvm name";
        final String hostName = "a host name";
        final String firstGroupId = "1";

        final String json = object(keyTextValue("jvmId", jvmId),
                                   keyTextValue("jvmName", jvmName),
                                   keyTextValue("hostName", hostName),
                                   keyValue("groupIds", array(object(keyTextValue("groupId",
                                                                                  firstGroupId)))),
                                   keyTextValue("httpPort", httpPort),
                                   keyTextValue("httpsPort", httpsPort),
                                   keyTextValue("redirectPort", redirectPort),
                                   keyTextValue("shutdownPort", shutdownPort),
                                   keyTextValue("ajpPort", ajpPort),
                                   keyTextValue("statusPath", statusPath),
                                   keyTextValue("systemProperties", systemProperties));

        final JsonUpdateJvm update = readValue(json);

        verifyAssertions(update,
                         jvmId,
                         jvmName,
                         hostName,
                         httpPort,
                         httpsPort,
                         redirectPort,
                         shutdownPort,
                         ajpPort,
                         statusPath,
                         systemProperties,
                         firstGroupId);
    }

    @Test
    public void testDeserializeSingleGroup() throws Exception {

        final String jvmId = "1";
        final String jvmName = "a jvm name";
        final String hostName = "a host name";
        final String firstGroupId = "1";

        final String json = object(keyTextValue("jvmId", jvmId),
                                   keyTextValue("jvmName", jvmName),
                                   keyTextValue("hostName", hostName),
                                   keyTextValue("groupId", firstGroupId),
                                   keyTextValue("httpPort", httpPort),
                                   keyTextValue("httpsPort", httpsPort),
                                   keyTextValue("redirectPort", redirectPort),
                                   keyTextValue("shutdownPort", shutdownPort),
                                   keyTextValue("ajpPort", ajpPort),
                                   keyTextValue("statusPath", statusPath),
                                   keyTextValue("systemProperties", systemProperties));

        final JsonUpdateJvm update = readValue(json);

        verifyAssertions(update,
                         jvmId,
                         jvmName,
                         hostName,
                         httpPort,
                         httpsPort,
                         redirectPort,
                         shutdownPort,
                         ajpPort,
                         statusPath,
                         systemProperties,
                         firstGroupId);
    }

    @Test
    public void testDeserializeNoGroups() throws Exception {

        final String jvmId = "1";
        final String jvmName = "a jvm name";
        final String hostName = "a host name";

        final String json = object(keyTextValue("jvmId", jvmId),
                                   keyTextValue("jvmName", jvmName),
                                   keyTextValue("hostName", hostName),
                                   keyTextValue("httpPort", httpPort),
                                   keyTextValue("httpsPort", httpsPort),
                                   keyTextValue("redirectPort", redirectPort),
                                   keyTextValue("shutdownPort", shutdownPort),
                                   keyTextValue("ajpPort", ajpPort),
                                   keyTextValue("statusPath", statusPath),
                                   keyTextValue("systemProperties", systemProperties));

        final JsonUpdateJvm update = readValue(json);

        verifyAssertions(update,
                         jvmId,
                         jvmName,
                         hostName,
                         httpPort,
                         httpsPort,
                         redirectPort,
                         shutdownPort,
                         ajpPort,
                         statusPath,
                         systemProperties);
    }

    @Test(expected = BadRequestException.class)
    public void testDeserializeBadGroupIdentifier() throws Exception {

        final String jvmId = "1";
        final String jvmName = "a jvm name";
        final String hostName = "a host name";
        final String firstGroupId = "this is not a valid group identifier";

        final String json = object(keyTextValue("jvmId", jvmId),
                                   keyTextValue("jvmName", jvmName),
                                   keyTextValue("hostName", hostName),
                                   keyTextValue("groupId", firstGroupId),
                                   keyTextValue("httpPort", httpPort),
                                   keyTextValue("httpsPort", httpsPort),
                                   keyTextValue("redirectPort", redirectPort),
                                   keyTextValue("shutdownPort", shutdownPort),
                                   keyTextValue("ajpPort", ajpPort),
                                   keyTextValue("statusPath", statusPath),
                                   keyTextValue("systemProperties", systemProperties));

        final JsonUpdateJvm update = readValue(json);

        verifyAssertions(update,
                         jvmId,
                         jvmName,
                         hostName,
                         httpPort,
                         httpsPort,
                         redirectPort,
                         shutdownPort,
                         ajpPort,
                         statusPath,
                         systemProperties,
                         firstGroupId);
    }

    @Test(expected = BadRequestException.class)
    public void testDeserializeBadJvmIdentifier() throws Exception {

        final String jvmId = "this is not a valid JVM identifier";
        final String jvmName = "a jvm name";
        final String hostName = "a host name";
        final String firstGroupId = "1";

        final String json = object(keyTextValue("jvmId", jvmId),
                                   keyTextValue("jvmName", jvmName),
                                   keyTextValue("hostName", hostName),
                                   keyTextValue("groupId", firstGroupId),
                                   keyTextValue("httpPort", httpPort),
                                   keyTextValue("httpsPort", httpsPort),
                                   keyTextValue("redirectPort", redirectPort),
                                   keyTextValue("shutdownPort", shutdownPort),
                                   keyTextValue("ajpPort", ajpPort),
                                   keyTextValue("statusPath", statusPath),
                                   keyTextValue("systemProperties", systemProperties));

        final JsonUpdateJvm update = readValue(json);

        verifyAssertions(update,
                         jvmId,
                         jvmName,
                         hostName,
                         httpPort,
                         httpsPort,
                         redirectPort,
                         shutdownPort,
                         ajpPort,
                         statusPath,
                         systemProperties,
                         firstGroupId);
    }

    protected JsonUpdateJvm readValue(final String someJson) throws IOException {
        return mapper.readValue(someJson, JsonUpdateJvm.class);
    }

    private String integerToString(Integer i) {
        return i == null ? "" : i.toString();
    }

    protected void verifyAssertions(final JsonUpdateJvm anUpdate,
                                    final String aJvmId,
                                    final String aJvmName,
                                    final String aHostName,
                                    final String aHttpPort,
                                    final String aHttpsPort,
                                    final String aRedirectPort,
                                    final String aShutdownPort,
                                    final String aAjpPort,
                                    final String aStatusPath,
                                    final String aSystemProperties,
                                    final String ... someGroupIds) {

        final UpdateJvmRequest update = anUpdate.toUpdateJvmCommand();

        assertEquals(new Identifier<Jvm>(aJvmId),
                     update.getId());
        assertEquals(aJvmName,
                     update.getNewJvmName());
        assertEquals(aHostName,
                     update.getNewHostName());
        assertEquals(aHttpPort,
                     integerToString(update.getNewHttpPort()));
        assertEquals(aHttpsPort,
                     integerToString(update.getNewHttpsPort()));
        assertEquals(aRedirectPort,
                     integerToString(update.getNewRedirectPort()));
        assertEquals(aShutdownPort,
                     integerToString(update.getNewShutdownPort()));
        assertEquals(aAjpPort,
                     integerToString(update.getNewAjpPort()));
        assertEquals(aStatusPath,
                     update.getNewStatusPath().getUriPath());
        assertEquals(aSystemProperties,
                update.getNewSystemProperties());
        assertEquals(someGroupIds.length,
                     update.getAssignmentCommands().size());
        final Set<Identifier<Group>> expectedGroupIds = new IdentifierSetBuilder(Arrays.asList(someGroupIds)).build();
        for (final AddJvmToGroupRequest addCommand : update.getAssignmentCommands()) {
            assertTrue(expectedGroupIds.contains(addCommand.getGroupId()));
        }
    }
}
