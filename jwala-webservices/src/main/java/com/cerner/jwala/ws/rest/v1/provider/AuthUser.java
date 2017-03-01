package com.cerner.jwala.ws.rest.v1.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Wrapper to get current context user from a REST request
 *
 * Created by Jedd Cuison on 10/26/2016.
 */
public class AuthUser {

    final private static Logger LOGGER = LoggerFactory.getLogger(AuthUser.class);

    public String getUserName() {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        }
        LOGGER.error("Was not able to get the user name from the security context: {}!", SecurityContextHolder.getContext());
        return null;
    }
}
