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

public class JsonUpdateJvmDeserializerTest {

    @Test
    public void testDeserializeJsonUpdateJvm() throws Exception {
        final InputStream in = this.getClass().getResourceAsStream("/json-update-jvm-data.json");
        final String jsonData = IOUtils.toString(in, Charset.defaultCharset());

        final ObjectMapper mapper = new ObjectMapper();

        final JsonUpdateJvm jsonUpdateJvm = mapper.readValue(jsonData, JsonUpdateJvm.class);
        assertEquals("1", jsonUpdateJvm.getJvmId());
        assertEquals("my-jvm", jsonUpdateJvm.getJvmName());
        assertEquals("some-host", jsonUpdateJvm.getHostName());
        assertEquals("jwala", jsonUpdateJvm.getUserName());
        assertEquals("/manager", jsonUpdateJvm.getStatusPath());
        assertEquals("1", jsonUpdateJvm.getJdkMediaId());
        assertTrue(StringUtils.isNotEmpty(jsonUpdateJvm.getEncryptedPassword()));
        assertNotEquals("password", jsonUpdateJvm.getEncryptedPassword());
        assertEquals("8893", jsonUpdateJvm.getAjpPort());
        assertEquals("8889", jsonUpdateJvm.getHttpPort());
        assertEquals("8890", jsonUpdateJvm.getHttpsPort());
        assertEquals("8891", jsonUpdateJvm.getRedirectPort());
        assertEquals("8892", jsonUpdateJvm.getShutdownPort());
        assertEquals("1", jsonUpdateJvm.getGroupIds().get(0).getGroupId());
    }

}
