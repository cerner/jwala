package com.cerner.jwala.ws.rest.v1.service.git.impl;

import com.cerner.jwala.ws.rest.v1.service.git.GitServiceRest;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.Response;

/**
 * Created by RS045609 on 6/1/2017.
 */
public class GitServiceRestImpl implements GitServiceRest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitServiceRestImpl.class);

    @Override
    public Response.Status cloneRepository(@FormParam("gitUri") String gitUri) throws GitAPIException {
        Git git = Git.cloneRepository()
                .setURI("https://github.com/praveensayani/Online-Career-Center.git")
                .call();
        LOGGER.info("sharvari : ", git);

        return Response.Status.ACCEPTED;
    }

    @Override
    public Response.Status getSomething() {
        return Response.Status.ACCEPTED;
    }

    
}
