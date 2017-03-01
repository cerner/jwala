package com.cerner.jwala.ws.rest.v1.service.webserver.impl;

import com.cerner.jwala.common.domain.model.webserver.WebServerControlOperation;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.ws.rest.v1.service.JsonDeserializationBehavior;
import com.cerner.jwala.ws.rest.v1.service.webserver.impl.JsonControlWebServer;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.cerner.jwala.ws.rest.v1.service.JsonDeserializationBehavior.keyTextValue;
import static com.cerner.jwala.ws.rest.v1.service.JsonDeserializationBehavior.object;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class JsonControlWebServerDeserializerTest {

    private ObjectMapper mapper;

    @Before
    public void setup() {
        mapper = new JsonDeserializationBehavior().addMapping(JsonControlWebServer.class, new JsonControlWebServer.JsonControlWebServerDeserializer()).toObjectMapper();
    }

    @Test
    public void testDeserializeValidOperation() throws Exception {

        final WebServerControlOperation requestedOperation = WebServerControlOperation.START;
        final String json = object(keyTextValue("controlOperation", requestedOperation.getExternalValue()));
        final JsonControlWebServer control = readValue(json);
        final WebServerControlOperation operation = control.toControlOperation();

        assertEquals(requestedOperation,
                     operation);
    }

    @Test(expected = BadRequestException.class)
    public void testDeserializeInvalidOperation() throws Exception {

        final String json = object(keyTextValue("controlOperation", "gibberish"));
        final JsonControlWebServer control = readValue(json);
        final WebServerControlOperation operation = control.toControlOperation();
        fail("Control Operation should have been invalid");
    }

    @Test(expected = BadRequestException.class)
    public void testNoOperation() throws Exception {

        final String json = "{\"controlOperation\": null}";
        final JsonControlWebServer control = readValue(json);
        final WebServerControlOperation operation = control.toControlOperation();
        fail("Control Operation should have been invalid");
    }

    protected JsonControlWebServer readValue(final String someJson) throws IOException {
        return mapper.readValue(someJson, JsonControlWebServer.class);
    }
}
