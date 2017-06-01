package com.cerner.jwala.ws.rest.v1.service.springboot;

import com.cerner.jwala.persistence.jpa.domain.JpaSpringBootApp;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created on 6/1/2017.
 */
@Path("/springboot")
@Produces(MediaType.APPLICATION_JSON)
public interface SpringBootRestService {

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    Response createSpringBoot(final List<Attachment> attachments);

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    Response updateSpringBoot(JpaSpringBootApp springBoot);

    @DELETE
    @Path("/{springBootName}")
    Response removeSpringBoot(String name);

    @GET
    @Path("/{springBootName}")
    Response findSpringBoot(@PathParam("name") String springBootName);

    @PUT
    @Path("/control/{springBootName}/{command}")
    Response controlSpringBoot(@PathParam("springBootName") String name, @PathParam("command") String command);

    @PUT
    @Path("/generate/{springBootName}")
    Response generateAndDeploy(@PathParam("springBootName") String name);
}
