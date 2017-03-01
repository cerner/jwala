package com.cerner.jwala.web.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import java.lang.management.ManagementFactory;

import com.cerner.jwala.common.properties.ApplicationProperties;
import org.apache.catalina.Engine;
import org.apache.catalina.Realm;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.catalina.realm.UserDatabaseRealm;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.core.Authentication;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * @author Arvindo Kinny
 *
 */
public class JwalaAuthenticationProviderTest {
    private static final Logger LOGGER = Logger.getLogger(JwalaAuthenticationProviderTest.class);



    private UserDatabaseRealm userDatabaseRealm= mock(UserDatabaseRealm.class);
    private GenericPrincipal principal = mock(GenericPrincipal.class);
    private ManagementFactory managementFactory = mock(ManagementFactory.class);
    private Engine engine = mock(Engine.class);
    private MBeanServer mBeanServer = mock(MBeanServer.class);
    private Authentication authentication = mock(Authentication.class);
    private String userName = "tomcat";
    private String password = "tomcat";

    ObjectName name;
    JwalaAuthenticationProvider jwalaAuthenticationProvider;

    @Before
    public void setUp() throws Exception {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");
        jwalaAuthenticationProvider = new JwalaAuthenticationProvider();
        userDatabaseRealm.setResourceName("tomcat-users.xml");
        name = new ObjectName("Catalina", "type", "Engine");
        when(jwalaAuthenticationProvider.getTomcatContextRealm()).thenReturn(userDatabaseRealm);
        when(ManagementFactory.getPlatformMBeanServer()).thenReturn(mBeanServer);
        when(JwalaAuthenticationProvider.getmBeanServer()).thenReturn(mBeanServer);
        when(mBeanServer.getAttribute(name, "managedResource")).thenReturn(engine);
        when(userDatabaseRealm.authenticate(userName, password)).thenReturn(principal);
        when(authentication.getName()).thenReturn("tomcat");
        when(authentication.getCredentials()).thenReturn("tomcat");
        when(principal.getRoles()).thenReturn(new String[]{"Tomcat Admin"});
    }
/*
    @Test
    public void testAuthenticate() throws Exception{
        assertNotNull(jwalaAuthenticationProvider.authenticate(authentication));
    }

    @Test(expected = BadCredentialsException.class)
    public void testFailAuthenticate() throws Exception{
        when(jwalaAuthenticationProvider.getTomcatContextRealm()).thenReturn(userDatabaseRealm);
        when(userDatabaseRealm.authenticate(userName, password)).thenReturn(null);
        assertEquals(BadCredentialsException.class, jwalaAuthenticationProvider.authenticate(authentication));
    }

    @Test(expected = ProviderNotFoundException.class)
    public void testRealmError() {
        try {
            when(jwalaAuthenticationProvider.getTomcatContextRealm()).thenThrow(AttributeNotFoundException.class);
        } catch (Exception e) {
            LOGGER.info("Ignore me", e);
        }
        assertEquals(ProviderNotFoundException.class, jwalaAuthenticationProvider.authenticate(authentication));
    }

    @Test
    public void testSupports() {
        assertEquals(true, jwalaAuthenticationProvider.supports(Authentication.class));
    }

    @Test
    public void testgetmBeanServer() {
        assertEquals(mBeanServer, jwalaAuthenticationProvider.getmBeanServer());
    }

    @Test
    public void testMalformedObjectNameException() throws Exception {
        when(mBeanServer.getAttribute(name, "managedResource")).thenThrow(MalformedObjectNameException.class);
        assertNull(jwalaAuthenticationProvider.getTomcatContextRealm());
    }*/
}
