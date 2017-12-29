package com.cerner.jwala.ws.rest.v1.service.user;

import io.swagger.annotations.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(value = "/user", tags = "user")
@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
public interface UserServiceRest {

    @POST
    @Path("/login")
    @ApiOperation(value = "Login to the application",
            response = Response.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Error login"))
    Response login(@Context HttpServletRequest request,
                   @ApiParam(value = "The user", required = true) @FormParam("userName") String userName,
                   @ApiParam(value = "The user's password", required = true) @FormParam("password") String password);

    @POST
    @Path("/logout")
    @ApiOperation(value = "Logout from the application",
            response = Response.class
    )
    Response logout(@Context HttpServletRequest request,
                    @Context HttpServletResponse response);
    
    @GET
    @Path("/isUserAdmin")
    @ApiOperation(value = "Checks if the current logged on user is an administrator for the application",
            response = String.class
    )
    Response isUserAdmin(@Context HttpServletRequest request,
                         @Context HttpServletResponse response);
}