package com.cerner.jwala.ws.rest.v1.service.webserver.impl;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.id.IdentifierSetBuilder;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.request.webserver.UpdateWebServerRequest;
import com.cerner.jwala.ws.rest.v1.service.JsonDeserializationBehavior;
import com.cerner.jwala.ws.rest.v1.service.webserver.impl.JsonUpdateWebServer;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static com.cerner.jwala.ws.rest.v1.service.JsonDeserializationBehavior.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JsonUpdateWebServerDeserializerTest {

    private ObjectMapper mapper;

    @Before
    public void setup() {
        mapper = new JsonDeserializationBehavior().addMapping(JsonUpdateWebServer.class, new JsonUpdateWebServer.JsonUpdateWebServerDeserializer()).toObjectMapper();
    }

    @Test
    public void testDeserializeMultipleGroups() throws Exception {

        final String webServerId = "1";
        final String webServerName = "a web server name";
        final String hostName = "a host name";
        final String portNumber = "8080";
        final String httpsPort = "8009";
        final String firstGroupId = "1";
        final String secondGroupId = "2";
        final String statusPath = "/statusPath";
        final String httpConfigFile = null;
        final String svrRoot = null;
        final String docRoot = null;

        final String json = array(object(keyTextValue("webserverId", webServerId),
                                         keyTextValue("webserverName", webServerName),
                                         keyTextValue("hostName", hostName),
                                         keyTextValue("portNumber", portNumber),
                                         keyTextValue("httpsPort", httpsPort),
                                         keyTextValue("statusPath", statusPath),
                                         keyTextValue("httpConfigFile", httpConfigFile),
                                         keyTextValue("svrRoot", svrRoot),
                                         keyTextValue("docRoot", docRoot),
                                         keyValue("groupIds", array(object(keyTextValue("groupId",
                                                                                        firstGroupId)),
                                                                    object(keyTextValue("groupId",
                                                                                        secondGroupId))))));

        final JsonUpdateWebServer update = readValue(json);

        verifyAssertions(update,
                         webServerId,
                         webServerName,
                         hostName,
                         portNumber,
                         httpsPort,
                         statusPath,
                         httpConfigFile,
                         firstGroupId,
                         secondGroupId);
    }

    @Test
    public void testDeserializeSingleFromMultiple() throws Exception {

        final String webServerId = "1";
        final String webServerName = "a web server name";
        final String hostName = "a host name";
        final String portNumber = "8080";
        final String httpsPort = "8009";
        final String firstGroupId = "1";
        final String statusPath = "/statusPath";
        final String httpConfigFile = "d:/some-dir/httpd.conf";
        final String svrRoot = "./";
        final String docRoot = "htdocs";

        final String json = array(object(keyTextValue("webserverId", webServerId),
                                         keyTextValue("webserverName", webServerName),
                                         keyTextValue("hostName", hostName),
                                         keyTextValue("portNumber", portNumber),
                                         keyTextValue("httpsPort", httpsPort),
                                         keyTextValue("statusPath", statusPath),
                                         keyTextValue("httpConfigFile", httpConfigFile),
                                         keyTextValue("svrRoot", svrRoot),
                                         keyTextValue("docRoot", docRoot),
                                         keyValue("groupIds", array(object(keyTextValue("groupId",
                                                                                        firstGroupId))))));
        final JsonUpdateWebServer update = readValue(json);

        verifyAssertions(update,
                         webServerId,
                         webServerName,
                         hostName,
                         portNumber,
                         httpsPort,
                         statusPath,
                         httpConfigFile,
                         firstGroupId);
    }

    @Test
    public void testDeserializeSingle() throws Exception {

        final String webServerId = "1";
        final String webServerName = "a web server name";
        final String hostName = "a host name";
        final String portNumber = "8080";
        final String httpsPort = "8009";
        final String firstGroupId = "1";
        final String statusPath = "/statusPath";
        final String httpConfigFile = "d:/some-dir/httpd.conf";
        final String svrRoot = "./";
        final String docRoot = "htdocs";

        final String json = array(object(keyTextValue("webserverId", webServerId),
                                         keyTextValue("webserverName", webServerName),
                                         keyTextValue("hostName", hostName),
                                         keyTextValue("portNumber", portNumber),
                                         keyTextValue("httpsPort", httpsPort),
                                         keyTextValue("statusPath", statusPath),
                                         keyTextValue("httpConfigFile", httpConfigFile),
                                         keyTextValue("svrRoot", svrRoot),
                                         keyTextValue("docRoot", docRoot),
                                         keyTextValue("groupId", firstGroupId)));

        final JsonUpdateWebServer update = readValue(json);

        verifyAssertions(update,
                         webServerId,
                         webServerName,
                         hostName,
                         portNumber,
                         httpsPort,
                         statusPath,
                         httpConfigFile,
                         firstGroupId);
    }

    @Test(expected = IOException.class)
    public void testInvalidInput() throws Exception {

        final String json = "absdfl;jk;lkj;lkjjads";

        final JsonUpdateWebServer update = readValue(json);
    }

    @Test(expected = BadRequestException.class)
    public void testInvalidPortNumber() throws Exception {

        final String webServerId = "1";
        final String webServerName = "a web server name";
        final String hostName = "a host name";
        final String portNumber = "this port number is not a number";
        final String httpsPort = "2";
        final String firstGroupId = "1";
        final String statusPath = "/statusPath";
        final String httpConfigFile = "d:/some-dir/httpd.conf";
        final String svrRoot = "./";
        final String docRoot = "htdocs";

        final String json = array(object(keyTextValue("webserverId", webServerId),
                                         keyTextValue("webserverName", webServerName),
                                         keyTextValue("hostName", hostName),
                                         keyTextValue("portNumber", portNumber),
                                         keyTextValue("httpsPort", httpsPort),
                                         keyTextValue("statusPath", statusPath),
                                         keyTextValue("httpConfigFile", httpConfigFile),
                                         keyTextValue("svrRoot", svrRoot),
                                         keyTextValue("docRoot", docRoot),
                                         keyTextValue("groupId", firstGroupId)));

        final JsonUpdateWebServer update = readValue(json);
        final UpdateWebServerRequest updateCommand = update.toUpdateWebServerRequest();
    }

    @Test(expected = BadRequestException.class)
    public void testInvalidHttpsPortNumber() throws Exception {

        final String webServerId = "1";
        final String webServerName = "a web server name";
        final String hostName = "a host name";
        final String portNumber = "1";
        final String httpsPort = "this port number is not a number";
        final String firstGroupId = "1";
        final String statusPath = "/statusPath";
        final String httpConfigFile = "d:/some-dir/http.conf";
        final String svrRoot = "./";
        final String docRoot = "htdocs";

        final String json = array(object(keyTextValue("webserverId", webServerId),
                                         keyTextValue("webserverName", webServerName),
                                         keyTextValue("hostName", hostName),
                                         keyTextValue("portNumber", portNumber),
                                         keyTextValue("httpsPort", httpsPort),
                                         keyTextValue("statusPath", statusPath),
                                         keyTextValue("httpConfigFile", httpConfigFile),
                                         keyTextValue("svrRoot", svrRoot),
                                         keyTextValue("docRoot", docRoot),
                                         keyTextValue("groupId", firstGroupId)));

        final JsonUpdateWebServer update = readValue(json);
        final UpdateWebServerRequest updateCommand = update.toUpdateWebServerRequest();
    }

    @Test(expected = BadRequestException.class)
    public void testInvalidGroupIdentifier() throws Exception {

        final String webServerId = "1";
        final String webServerName = "a web server name";
        final String hostName = "a host name";
        final String portNumber = "80";
        final String httpsPort = "89";
        final String firstGroupId = "this is not a valid identifier";
        final String statusPath = "/statusPath";
        final String httpConfigFile = "d:/some-dir/http.conf";
        final String svrRoot = "./";
        final String docRoot = "htdocs";

        final String json = array(object(keyTextValue("webserverId", webServerId),
                                         keyTextValue("webserverName", webServerName),
                                         keyTextValue("hostName", hostName),
                                         keyTextValue("portNumber", portNumber),
                                         keyTextValue("httpsPort", httpsPort),
                                         keyTextValue("statusPath", statusPath),
                                         keyTextValue("httpConfigFile", httpConfigFile),
                                         keyTextValue("svrRoot", svrRoot),
                                         keyTextValue("docRoot", docRoot),
                                         keyTextValue("groupId", firstGroupId)));

        final JsonUpdateWebServer update = readValue(json);
        final UpdateWebServerRequest updateCommand = update.toUpdateWebServerRequest();
    }

    @Test(expected = BadRequestException.class)
    public void testInvalidWebServerIdentifier() throws Exception {

        final String webServerId = "this is not a valid identifier";
        final String webServerName = "a web server name";
        final String hostName = "a host name";
        final String portNumber = "80";
        final String httpsPort = "90";
        final String firstGroupId = "1";
        final String statusPath = "/statusPath";
        final String httpConfigFile = "d:/some-dir/http.conf";
        final String svrRoot = "./";
        final String docRoot = "htdocs";

        final String json = array(object(keyTextValue("webserverId", webServerId),
                                         keyTextValue("webserverName", webServerName),
                                         keyTextValue("hostName", hostName),
                                         keyTextValue("portNumber", portNumber),
                                         keyTextValue("httpsPort", httpsPort),
                                         keyTextValue("statusPath", statusPath),
                                         keyTextValue("httpConfigFile", httpConfigFile),
                                         keyTextValue("svrRoot", svrRoot),
                                         keyTextValue("docRoot", docRoot),
                                         keyTextValue("groupId", firstGroupId)));

        final JsonUpdateWebServer update = readValue(json);
        final UpdateWebServerRequest updateCommand = update.toUpdateWebServerRequest();
    }

    protected void verifyAssertions(final JsonUpdateWebServer anUpdate,
                                    final String aWebServerId,
                                    final String aWebServerName,
                                    final String aHostName,
                                    final String aPortNumber,
                                    final String aHttpsPort,
                                    final String aStatusPath,
                                    final String aHttpConfigFile,
                                    final String... groupIds) {

        final UpdateWebServerRequest updateCommand = anUpdate.toUpdateWebServerRequest();

        assertEquals(new Identifier<WebServer>(aWebServerId),
                     updateCommand.getId());
        assertEquals(aWebServerName,
                     updateCommand.getNewName());
        assertEquals(aHostName,
                     updateCommand.getNewHost());
        assertEquals(Integer.valueOf(aPortNumber),
                     updateCommand.getNewPort());

        assertEquals(Integer.valueOf(aHttpsPort),
                     updateCommand.getNewHttpsPort());

        assertCollectionEquals(new IdentifierSetBuilder(Arrays.asList(groupIds)).<Group>build(),
                               updateCommand.getNewGroupIds());

    }

    protected <T> void assertCollectionEquals(final Collection<T> anExpected,
                                              final Collection<T> anActual) {

        assertEquals(anExpected.size(),
                     anActual.size());
        assertTrue(anActual.containsAll(anExpected));
    }

    protected JsonUpdateWebServer readValue(final String someJson) throws IOException {
        return mapper.readValue(someJson, JsonUpdateWebServer.class);
    }
}
