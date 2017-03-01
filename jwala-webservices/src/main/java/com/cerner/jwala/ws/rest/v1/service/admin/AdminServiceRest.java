package com.cerner.jwala.ws.rest.v1.service.admin;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
public interface AdminServiceRest {

    @GET
    @Path("/properties/reload")
    Response reload();

    @GET
    @Path("/properties/view")
    Response view();

    @POST
    @Path("/properties/encrypt")
    Response encrypt(String cleartext);

    @GET
    @Path("/manifest")
    Response manifest(@Context ServletContext context);

    @GET
    @Path("/auth/state")
    Response isJwalaAuthorizationEnabled();

    @GET
    @Path("/context/authorization")
    Response getAuthorizationDetails();
}
