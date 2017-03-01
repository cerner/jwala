package com.cerner.jwala.service.impl;

import com.cerner.jwala.service.DbServerServiceException;
import org.h2.tools.Server;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created on 10/10/2016.
 */
public class H2WebServerServiceImplTest {

    private H2WebServerServiceImpl service;
    private static final String DEFAULT_WEBSERVER_PARAM = "-webSSL,-webPort,8888";
    private static final String DEFAULT_TCPSERVER_PARAM = "-tcpPort,9999";


    @Before
    public void setUp() {
        service = new H2WebServerServiceImpl(DEFAULT_WEBSERVER_PARAM);
    }

    @Test
    public void testCreateServer() {
        String[] params = new String[0];
        Server result = service.createServer(params);
        assertNotNull(result.getPort());
        assertEquals("default status", "Not started", result.getStatus());
        // default URL is created using the IP address of the running machine so just test for not null
        assertNotNull("default URL", result.getURL());
        assertNotNull("default service", result.getService());
    }

    @Test (expected = DbServerServiceException.class)
    public void testCreateServerThrowsException() {
        String[] badParams = new String[]{"-webPort", "ERROR"};
            service.createServer(badParams);
    }
}
