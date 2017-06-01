package com.cerner.jwala.ws.rest.v1.service.git;

import org.eclipse.jgit.api.errors.GitAPIException;

import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
    Response.Status cloneRepository(@MatrixParam("gitUri") String gitUri) throws GitAPIException;

}
