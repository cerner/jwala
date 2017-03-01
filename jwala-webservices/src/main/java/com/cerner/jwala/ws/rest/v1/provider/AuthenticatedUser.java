package com.cerner.jwala.ws.rest.v1.provider;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import com.cerner.jwala.common.domain.model.user.User;
import org.springframework.security.authentication.InternalAuthenticationServiceException;

public class AuthenticatedUser {

    @Context
    private SecurityContext context;

    public AuthenticatedUser() {
    }

    public AuthenticatedUser(final SecurityContext theSecurityContext) {
        context = theSecurityContext;
    }

    /**
     *
     * @return user
     */
    public User getUser() {
        if(context.getUserPrincipal() == null) {
            throw new InternalAuthenticationServiceException("User not found");
        }
        return new User(context.getUserPrincipal().getName());
    }
}