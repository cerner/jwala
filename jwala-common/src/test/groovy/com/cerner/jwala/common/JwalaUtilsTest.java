package com.cerner.jwala.common;

import com.cerner.jwala.common.exception.ApplicationException;
import org.junit.Ignore;
import org.junit.Test;

import java.net.Inet4Address;
import java.net.UnknownHostException;

import static org.junit.Assert.*;

/**
 * Created by Arvindo Kinny on 5/24/2017.
 */
public class JwalaUtilsTest {

    /**
     * Test unknown host exception
     */
    @Ignore
    @Test(expected = ApplicationException.class)
    public void getHostAddress() {
        String host = "someunknownhost";
        JwalaUtils.getHostAddress(host);
    }
    @Test
    /**
     * Test domain to IP
     */
    public void getKnownHostAddress() {
        String host = "www.cerner.com";
        assertNotNull(JwalaUtils.getHostAddress(host));
    }
    @Test
    /**
     * Test IP from IP
     */
    public void testIPv4Address() throws UnknownHostException{
        String host = Inet4Address.getLocalHost().getHostAddress();
        assertEquals(host,JwalaUtils.getHostAddress(host));
    }
}