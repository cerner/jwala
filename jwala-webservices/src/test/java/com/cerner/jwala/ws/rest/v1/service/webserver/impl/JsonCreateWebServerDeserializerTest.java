package com.cerner.jwala.ws.rest.v1.service.webserver.impl;

import com.cerner.jwala.common.domain.model.id.IdentifierSetBuilder;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.request.webserver.CreateWebServerRequest;
import com.cerner.jwala.ws.rest.v1.service.JsonDeserializationBehavior;
import com.cerner.jwala.ws.rest.v1.service.webserver.impl.JsonCreateWebServer;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static com.cerner.jwala.ws.rest.v1.service.JsonDeserializationBehavior.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JsonCreateWebServerDeserializerTest {

    private static final String webserverName = "webserverName";
    private static final String hostName = "localhost";
    private static final String portNumber = "10000";
    private static final String httpsPort = "20000";
    private static final String groupIdOne = "1";
    private static final String groupIdTwo = "2";
    private static final String statusPath = "/statusPath";
    private static final String httpConfigFile = "d:/some-dir/httpd.conf";
    private static final String SVR_ROOT = "./";
    private static final String DOC_ROOT = "htdocs";

    private ObjectMapper mapper;

    @Before
    public void setup() {
        mapper = new JsonDeserializationBehavior().addMapping(JsonCreateWebServer.class,
                                                              new JsonCreateWebServer.JsonCreateWebServerDeserializer())
                                                  .toObjectMapper();
    }

    @Test
    public void testDeserializeMultipleGroups() throws Exception {
        String json = array(object(keyTextValue("webserverName", webserverName),
                                   keyTextValue("hostName", hostName),
                                   keyTextValue("portNumber", portNumber),
                                   keyTextValue("httpsPort", httpsPort),
                                   keyTextValue("statusPath", statusPath),
                                   keyTextValue("httpConfigFile", httpConfigFile),
                                   keyTextValue("svrRoot", SVR_ROOT),
                                   keyTextValue("docRoot", DOC_ROOT),
                                   keyValue("groupIds",
                                            array(object(keyTextValue("groupId", groupIdOne)),
                                                  object(keyTextValue("groupId", groupIdTwo))))));
        final JsonCreateWebServer create = readValue(json);
        verifyAssertions(create, webserverName, hostName, groupIdOne, groupIdTwo);
    }

    @Test
    public void testDeserializeSingleGroup() throws Exception {
        String json = array(object(keyTextValue("webserverName", webserverName),
                                   keyTextValue("hostName", hostName),
                                   keyTextValue("portNumber", portNumber),
                                   keyTextValue("httpsPort", httpsPort),
                                   keyTextValue("statusPath", statusPath),
                                   keyTextValue("httpConfigFile", httpConfigFile),
                                   keyTextValue("svrRoot", SVR_ROOT),
                                   keyTextValue("docRoot", DOC_ROOT),
                                   keyValue("groupIds", array(object(keyTextValue("groupId", groupIdOne))))));
        final JsonCreateWebServer create = readValue(json);
        verifyAssertions(create, webserverName, hostName, groupIdOne);
    }

    @Test
    public void testDeserializeNoGroup() throws Exception {
        String json = array(object(keyTextValue("webserverName", webserverName),
                                   keyTextValue("hostName", hostName),
                                   keyTextValue("portNumber", portNumber),
                                   keyTextValue("httpsPort", httpsPort),
                                   keyTextValue("statusPath", statusPath),
                                   keyTextValue("httpConfigFile", httpConfigFile),
                                   keyTextValue("svrRoot", SVR_ROOT),
                                   keyTextValue("docRoot", DOC_ROOT)));
        final JsonCreateWebServer create = readValue(json);
        verifyAssertions(create, webserverName, hostName);
    }

    @Test(expected = BadRequestException.class)
    public void testInvalidPortNumber() throws Exception {
        String json = array(object(keyTextValue("webserverName", webserverName),
                                   keyTextValue("hostName", hostName),
                                   keyTextValue("portNumber", "abcd"),
                                   keyTextValue("httpsPort", "312"),
                                   keyTextValue("statusPath", statusPath),
                                   keyTextValue("httpConfigFile", httpConfigFile),
                                   keyTextValue("svrRoot", SVR_ROOT),
                                   keyTextValue("docRoot", DOC_ROOT)));
        final JsonCreateWebServer create = readValue(json);
        create.toCreateWebServerRequest();
    }

    @Test(expected = BadRequestException.class)
    public void testInvalidHttpsPort() throws Exception {
        String json = array(object(keyTextValue("webserverName", webserverName),
                                   keyTextValue("hostName", hostName),
                                   keyTextValue("portNumber", "321"),
                                   keyTextValue("httpsPort", "sxxs"),
                                   keyTextValue("statusPath", statusPath),
                                   keyTextValue("httpConfigFile", httpConfigFile),
                                   keyTextValue("svrRoot", SVR_ROOT),
                                   keyTextValue("docRoot", DOC_ROOT)));
        final JsonCreateWebServer create = readValue(json);
        create.toCreateWebServerRequest();
    }

    @Test(expected = IOException.class)
    public void testInvalidInput() throws Exception {

        final String json = "absdfl;jk;lkj;lkjjads";

        readValue(json);
    }

    protected void verifyAssertions(final JsonCreateWebServer aCreate, final String aWebServerName,
            final String aHostName, final String... groupIds) {
        CreateWebServerRequest createCommand = aCreate.toCreateWebServerRequest();

        assertEquals(aWebServerName, createCommand.getName());
        assertEquals(aHostName, createCommand.getHost());
        assertEquals(groupIds.length, createCommand.getGroups().size());
        assertTrue(new IdentifierSetBuilder(Arrays.asList(groupIds)).build().containsAll(createCommand.getGroups()));
        assertEquals(groupIds.length, createCommand.getGroups().size());

    }

    protected JsonCreateWebServer readValue(final String someJson) throws IOException {
        return mapper.readValue(someJson, JsonCreateWebServer.class);
    }
}
