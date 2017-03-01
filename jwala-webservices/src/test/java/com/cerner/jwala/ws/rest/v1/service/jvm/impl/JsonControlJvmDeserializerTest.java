package com.cerner.jwala.ws.rest.v1.service.jvm.impl;

import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.ws.rest.v1.service.JsonDeserializationBehavior;
import com.cerner.jwala.ws.rest.v1.service.jvm.impl.JsonControlJvm;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.cerner.jwala.ws.rest.v1.service.JsonDeserializationBehavior.keyTextValue;
import static com.cerner.jwala.ws.rest.v1.service.JsonDeserializationBehavior.object;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class JsonControlJvmDeserializerTest {

    private ObjectMapper mapper;

    @Before
    public void setup() {
        mapper = new JsonDeserializationBehavior().addMapping(JsonControlJvm.class, new JsonControlJvm.JsonControlJvmDeserializer()).toObjectMapper();
    }

    @Test
    public void testDeserializeValidOperation() throws Exception {

        final JvmControlOperation requestedOperation = JvmControlOperation.START;
        final String json = object(keyTextValue("controlOperation", requestedOperation.getExternalValue()));
        final JsonControlJvm control = readValue(json);
        final JvmControlOperation operation = control.toControlOperation();

        assertEquals(requestedOperation,
                     operation);
    }

    @Test(expected = BadRequestException.class)
    public void testDeserializeInvalidOperation() throws Exception {

        final String json = object(keyTextValue("controlOperation", "gibberish"));
        final JsonControlJvm control = readValue(json);
        final JvmControlOperation operation = control.toControlOperation();
        fail("Control Operation should have been invalid");
    }

    @Test(expected = BadRequestException.class)
    public void testNoOperation() throws Exception {

        final String json = "{\"controlOperation\": null}";
        final JsonControlJvm control = readValue(json);
        final JvmControlOperation operation = control.toControlOperation();
        fail("Control Operation should have been invalid");
    }

    protected JsonControlJvm readValue(final String someJson) throws IOException {
        return mapper.readValue(someJson, JsonControlJvm.class);
    }
}
