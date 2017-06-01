package com.cerner.jwala.ws.rest.v1.service.git;

import org.eclipse.jgit.api.errors.GitAPIException;

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
    @Consumes(MediaType.TEXT_HTML)
    Response.Status cloneRepository(@MatrixParam("gitUri") String gitUri) throws GitAPIException;

    @GET
    @Path("/")
    Response.Status getSomething();

}
