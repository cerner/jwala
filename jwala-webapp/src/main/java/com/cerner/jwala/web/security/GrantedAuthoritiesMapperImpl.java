package com.cerner.jwala.web.security;

import com.cerner.jwala.common.properties.ApplicationProperties;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Cerner
 *
 */
@Component
public class GrantedAuthoritiesMapperImpl implements GrantedAuthoritiesMapper {
    private static final String PROP_JWALA_ROLE_ADMIN = "jwala.role.admin";
    public final static String JWALA_ROLE_ADMIN = ApplicationProperties.get(PROP_JWALA_ROLE_ADMIN);

    /* (non-Javadoc)
     * @see org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper#mapAuthorities(java.util.Collection)
     */
    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Set<GrantedAuthority> roles = new HashSet<GrantedAuthority>();
        //Add only Jwala groups as authorities
        for (GrantedAuthority a : authorities) {
            if (JWALA_ROLE_ADMIN.equals(a.getAuthority())) {
                roles.add(new SimpleGrantedAuthority(JWALA_ROLE_ADMIN));
            }
        }
        return roles;
    }   
}
