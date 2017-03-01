package com.cerner.jwala.web.security;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import com.cerner.jwala.common.properties.ApplicationProperties;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author Arvindo Kinny
 *
 */
@Configuration
@ComponentScan("com.cerner.jwala.web.security")
@EnableWebSecurity

public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    GrantedAuthoritiesMapperImpl grantedAuthoritiesMapper;

    // for Test Injection
    /**
     * @param grantedAuthoritiesMapper
     */
    public SecurityConfig(GrantedAuthoritiesMapperImpl grantedAuthoritiesMapper) {
        this.grantedAuthoritiesMapper = grantedAuthoritiesMapper;
    }

    private static Logger LOGGER = Logger.getLogger(SecurityConfig.class);
    private static final String JWALA_AUTH_ENABLED = "jwala.authorization";

    private static final String LOGIN_PAGE = "/login";
    private static final String LOGIN_API = "/**/user/login";
    private static final String LOGOUT_API = "/**/user/logout";

    private static final String GEN_PUBLIC_RESOURCES = "/gen-public-resources/**";
    private static final String PUBLIC_RESOURCES = "/public-resources/**";
    private static final String PAGE_CONSTANTS = "/page-constants";
    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.security.config.annotation.web.configuration.
     * WebSecurityConfigurerAdapter#configure(org.springframework.security.
     * config.annotation.web.builders.HttpSecurity)
     */

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        final String ADMIN = ApplicationProperties.get("jwala.role.admin");
        final String AUTH = ApplicationProperties.get(JWALA_AUTH_ENABLED, "true");

        http.authorizeRequests().antMatchers(GEN_PUBLIC_RESOURCES, PUBLIC_RESOURCES, LOGIN_PAGE, LOGIN_API, LOGOUT_API)
                .permitAll().and().formLogin().loginPage(LOGIN_PAGE).permitAll().and().authorizeRequests().anyRequest()
                .authenticated();
        http.csrf().disable();

        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_THREADLOCAL);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.security.config.annotation.web.configuration.
     * WebSecurityConfigurerAdapter#configure(org.springframework.security.
     * config.annotation.web.builders.WebSecurity)
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(PAGE_CONSTANTS);
    }

    /**
     * @param auth
     * @throws Exception
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Jwala AuthenticationManagerBuilder initialized");
        JwalaAuthenticationProvider provider = new JwalaAuthenticationProvider();
        auth.authenticationProvider(provider);
    }
}
