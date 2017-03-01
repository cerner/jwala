package com.cerner.jwala.web.security;

import java.lang.management.ManagementFactory;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.apache.catalina.Engine;
import org.apache.catalina.Realm;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.catalina.realm.NullRealm;
import org.apache.log4j.Logger;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.cerner.jwala.common.properties.ApplicationProperties;

/**
 * @author Arvindo Kinny
 *         Jwala Authentication Provider
 */
public class JwalaAuthenticationProvider implements AuthenticationProvider {
    private static final Logger LOGGER = Logger.getLogger(JwalaAuthenticationProvider.class);
    /**
     *
     * @param authentication
     * @return Authentication
     */
    @Override
    public Authentication authenticate(Authentication authentication) {
        Realm realm;
        Set<GrantedAuthority> auths = new HashSet<>();
        try {
            realm = getTomcatContextRealm();
            if(realm instanceof NullRealm) {
                throw new ProviderNotFoundException("No Realms configured for Jwala to Authenticate");
            }
            Principal principal = realm.authenticate(authentication.getName(),
                    authentication.getCredentials().toString());
            if (principal == null) {
                throw new BadCredentialsException("Username or Password not found.");
            } else {
                if (principal instanceof GenericPrincipal) {
                    String[] roles = ((GenericPrincipal) principal).getRoles();
                    for (String role : roles) {
                        auths.add(new SimpleGrantedAuthority(role));
                    }
                }
                GrantedAuthoritiesMapperImpl grantedAuthoritiesMapper = new GrantedAuthoritiesMapperImpl();
                return new UsernamePasswordAuthenticationToken(authentication.getName(),
                        authentication.getCredentials(), grantedAuthoritiesMapper.mapAuthorities(auths));
            }
        } catch (AttributeNotFoundException | InstanceNotFoundException | MBeanException | ReflectionException e) {
            LOGGER.error("Error getting realms", e);
            throw new ProviderNotFoundException(e.getMessage());
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return true;
    }

    /**
     *
     * @return Tomcat Realms
     * @throws AttributeNotFoundException
     * @throws InstanceNotFoundException
     * @throws MBeanException
     * @throws ReflectionException
     */
    public Realm getTomcatContextRealm() throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException {
        try {
            ObjectName name = new ObjectName("Catalina", "type", "Engine");
            Engine engine = (Engine) JwalaAuthenticationProvider.getmBeanServer().getAttribute(name, "managedResource");
            return engine.getRealm();
        } catch (MalformedObjectNameException ex) {
            LOGGER.error("Invalid Realm", ex);
        }
        return null;
    }

    public static MBeanServer getmBeanServer() {
        return DeferredLoader.PLATFORM_MBEAN_SERVER;
    }

    private static final class DeferredLoader {
        public static final MBeanServer PLATFORM_MBEAN_SERVER = ManagementFactory.getPlatformMBeanServer();
    }

}
