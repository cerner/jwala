package com.cerner.jwala.ws.rest.v1.service.git.impl;

import com.cerner.jwala.ws.rest.v1.service.git.GitServiceRest;
import org.apache.maven.shared.invoker.*;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.File;
import java.util.Arrays;

/**
 * Created by RS045609 on 6/1/2017.
 */
public class GitServiceRestImpl implements GitServiceRest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitServiceRestImpl.class);

    @Override
    public Response.Status cloneRepository(String gitUri, String path, Boolean flag) {
        LOGGER.info("The gitUri {0} and destination paths{1}", gitUri, path);
        Git git = null;
        if (path == null) {
            path = "";
        }
        File file = new File(path);
        try {
            git = Git.cloneRepository()
                    .setURI(gitUri)
                    .setDirectory(file)
                    .call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }

        if (flag == true) {
//             do a mvn build
            InvocationRequest request = new DefaultInvocationRequest();
            LOGGER.info("first");
//            request.getBaseDirectory();
            request.setPomFile(new File(file.getAbsolutePath() + "/pom.xml"));
            LOGGER.info("second");
            request.setGoals(Arrays.asList("clean", "package"));

            Invoker invoker = new DefaultInvoker();
            invoker.setMavenHome(new File(System.getenv("M2_HOME")));
            try {
                invoker.execute(request);
            } catch (MavenInvocationException e) {
                e.printStackTrace();
            }
        }
        return Response.Status.ACCEPTED;
    }

}