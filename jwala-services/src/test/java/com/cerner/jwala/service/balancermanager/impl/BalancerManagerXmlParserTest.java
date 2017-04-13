package com.cerner.jwala.service.balancermanager.impl;

import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.service.balancermanager.impl.xml.data.Manager;
import com.cerner.jwala.service.jvm.JvmService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created on 2/22/2017.
 */
public class BalancerManagerXmlParserTest {

    private static final String BALANCER_MANAGER_CONTENT = "<?xml version='1.0' encoding='UTF-8' ?>\n" +
            "<httpd:manager xmlns:httpd='http://httpd.apache.org'>\n" +
            "  <httpd:balancers>\n" +
            "    <httpd:balancer>\n" +
            "      <httpd:name>balancer://lb-myapp</httpd:name>\n" +
            "      <httpd:stickysession>JSESSIONID</httpd:stickysession>\n" +
            "      <httpd:nofailover>On</httpd:nofailover>\n" +
            "      <httpd:timeout>0</httpd:timeout>      <httpd:maxattempts>5</httpd:maxattempts>\n" +
            "      <httpd:lbmethod>byrequests</httpd:lbmethod>\n" +
            "      <httpd:scolonpathdelim>On</httpd:scolonpathdelim>\n" +
            "      <httpd:workers>\n" +
            "        <httpd:worker>\n" +
            "          <httpd:name>https://localhost:9121/hct</httpd:name>\n" +
            "          <httpd:scheme>https</httpd:scheme>\n" +
            "          <httpd:hostname>localhost</httpd:hostname>\n" +
            "          <httpd:loadfactor>1</httpd:loadfactor>\n" +
            "          <httpd:port>9121</httpd:port>\n" +
            "          <httpd:min>0</httpd:min>\n" +
            "          <httpd:smax>1000</httpd:smax>\n" +
            "          <httpd:max>1000</httpd:max>\n" +
            "          <httpd:ttl>300</httpd:ttl>\n" +
            "          <httpd:keepalive>On</httpd:keepalive>\n" +
            "          <httpd:status>OK</httpd:status>\n" +
            "          <httpd:retries>0</httpd:retries>\n" +
            "          <httpd:lbstatus>0</httpd:lbstatus>\n" +
            "          <httpd:loadfactor>1</httpd:loadfactor>\n" +
            "          <httpd:transferred>0</httpd:transferred>\n" +
            "          <httpd:read>0</httpd:read>\n" +
            "          <httpd:elected>0</httpd:elected>\n" +
            "          <httpd:route>jvm-localhost-3</httpd:route>\n" +
            "          <httpd:redirect></httpd:redirect>\n" +
            "          <httpd:busy>0</httpd:busy>\n" +
            "          <httpd:lbset>0</httpd:lbset>\n" +
            "          <httpd:retry>0</httpd:retry>\n" +
            "        </httpd:worker>\n" +
            "      </httpd:workers>\n" +
            "    </httpd:balancer>\n" +
            "  </httpd:balancers>\n" +
            "</httpd:manager>\n";
    @Mock
    JvmService mockJvmService;

    BalancerManagerXmlParser balancerManagerXmlParser;

    @Before
    public void setup() {
        mockJvmService = mock(JvmService.class);
        balancerManagerXmlParser = new BalancerManagerXmlParser(mockJvmService);
    }

    @Test
    public void testBalancerManagerGetUrlPath() {
        final String host = "test-hostname";
        final String balancerName = "test-balancer-name";
        final String nonce = "test-nonce";
        final int httpsPort = 444;
        String result = balancerManagerXmlParser.getUrlPath(host, httpsPort, balancerName, nonce);
        assertEquals(MessageFormat.format("https://{0}:{1}/balancer-manager?b={2}&xml=1&nonce={3}", host, httpsPort, balancerName, nonce), result);
    }

    @Test
    public void testManager() {
        Manager manager = balancerManagerXmlParser.getWorkerXml(BALANCER_MANAGER_CONTENT);

        final List<Manager.Balancer> balancers = manager.getBalancers();
        assertEquals(1, balancers.size());
        assertEquals(1, balancers.get(0).getWorkers().size());

        final String balancerName = "lb-myapp";
        Map<String, String> workers = balancerManagerXmlParser.getWorkers(manager, balancerName);
        assertEquals(1, workers.size());

        Map<String, String> jvmWorkers = balancerManagerXmlParser.getJvmWorkerByName(manager, balancerName, "jvm-localhost-3");
        assertEquals(1, jvmWorkers.size());
    }

    @Test
    public void testGetJvmByWorker() {
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getJvmName()).thenReturn("jvm-localhost-3");
        when(mockJvm.getHostName()).thenReturn("localhost");
        when(mockJvm.getHttpPort()).thenReturn(9120);
        when(mockJvm.getHttpsPort()).thenReturn(9121);
        when(mockJvm.getAjpPort()).thenReturn(9122);
        when(mockJvmService.getJvms()).thenReturn(Collections.singletonList(mockJvm));

        String jvmName = balancerManagerXmlParser.findJvmNameByWorker("https://localhost:9121/hct");
        assertEquals("jvm-localhost-3", jvmName);

        jvmName = balancerManagerXmlParser.findJvmNameByWorker("http://localhost:9120/hct");
        assertEquals("jvm-localhost-3", jvmName);

        jvmName = balancerManagerXmlParser.findJvmNameByWorker("ajp://localhost:9122/hct");
        assertEquals("jvm-localhost-3", jvmName);
    }


}
