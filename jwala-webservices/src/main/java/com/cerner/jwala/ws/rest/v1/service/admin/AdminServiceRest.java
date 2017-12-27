package com.cerner.jwala.ws.rest.v1.service.admin;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(value = "/admin", tags = "admin")
@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
public interface AdminServiceRest {

    @GET
    @Path("/properties/reload")
    @ApiOperation(value = "Reload the application properties",
            notes = "Some properties only reload on application restart",
            response = Response.class
    )
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
