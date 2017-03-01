package com.cerner.jwala.ws.rest.v2.service.group;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Describes group related operations
 *
 * Created by Jedd Cuison on 7/29/2016.
 */
@Path("/groups")
@Produces(MediaType.APPLICATION_JSON)
public interface GroupServiceRest {

    @GET
    @Path("/{name}")
    Response getGroup(@PathParam("name") String name);

}
