package com.cerner.jwala.ws.rest.v1.service.git;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by RS045609 on 6/1/2017.
 */
@Path("/git")
@Produces(MediaType.APPLICATION_JSON)
public interface GitServiceRest {

    @POST
    @Path("/clone")
    Response.Status cloneRepository(@MatrixParam("gitUri") String gitUri,
                                    @MatrixParam("path") String path,
                                    @MatrixParam("flag") Boolean flag);

}
