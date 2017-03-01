package com.cerner.jwala.ws.rest.v1.service.user;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
public interface UserServiceRest {

    @POST
    @Path("/login")
    Response login(@Context HttpServletRequest request,
                   @FormParam("userName") String userName,
                   @FormParam("password") String password);

    @POST
    @Path("/logout")
    Response logout(@Context HttpServletRequest request, 
                    @Context HttpServletResponse response);
    
    @GET
    @Path("/isUserAdmin")
    Response isUserAdmin(@Context HttpServletRequest request, 
                         @Context HttpServletResponse response);
}