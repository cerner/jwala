package com.cerner.jwala.ws.rest.v1.service.git.impl;

import com.cerner.jwala.ws.rest.v1.service.git.GitServiceRest;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import javax.ws.rs.MatrixParam;
import javax.ws.rs.core.Response;

/**
 * Created by RS045609 on 6/1/2017.
 */
public class GitServiceRestImpl implements GitServiceRest {
    @Override
    public Response.Status cloneRepository (@MatrixParam("gitUri") String gitUri) throws GitAPIException {
        Git git = Git.cloneRepository()
                .setURI("https://github.com/eclipse/jgit.git")
                .call();
        return Response.Status.OK;
    }

}
