package com.cerner.jwala.ws.rest.v1.service.jvm.impl;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class JsonCreateJvmDeserializerTest {

    @Test
    public void testDeserializeJsonCreateJvm() throws Exception {
        final InputStream in = this.getClass().getResourceAsStream("/json-create-jvm-data.json");
        final String jsonData = IOUtils.toString(in, Charset.defaultCharset());

        final ObjectMapper mapper = new ObjectMapper();

        final JsonCreateJvm jsonCreateJvm = mapper.readValue(jsonData, JsonCreateJvm.class);
        assertEquals("my-jvm", jsonCreateJvm.getJvmName());
        assertEquals("some-host", jsonCreateJvm.getHostName());
        assertEquals("jwala", jsonCreateJvm.getUserName());
        assertEquals("/manager", jsonCreateJvm.getStatusPath());
        assertEquals("1", jsonCreateJvm.getJdkMediaId());
        assertTrue(StringUtils.isNotEmpty(jsonCreateJvm.getEncryptedPassword()));
        assertNotEquals("password", jsonCreateJvm.getEncryptedPassword());
        assertEquals("8893", jsonCreateJvm.getAjpPort());
        assertEquals("8889", jsonCreateJvm.getHttpPort());
        assertEquals("8890", jsonCreateJvm.getHttpsPort());
        assertEquals("8891", jsonCreateJvm.getRedirectPort());
        assertEquals("8892", jsonCreateJvm.getShutdownPort());
        assertEquals("1", jsonCreateJvm.getGroupIds().get(0).getGroupId());
    }

}