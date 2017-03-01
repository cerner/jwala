package com.cerner.jwala.ws.rest.v2.service.jvm;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Contract for Jvm related services
 *
 * Created by Jedd Cuison on 8/9/2016.
 */
@Path("/jvms")
@Produces(MediaType.APPLICATION_JSON)
public interface JvmServiceRest {

    /**
     * Get a JVM
     * @param name the name of the JVM
     * @return response that wraps a JVM object
     */
    @GET
    @Path("/{name}")
    Response getJvm(@PathParam("name") String name);

    /**
     * Create a JVM
     * @param jvmRequestData {@link JvmRequestData}
     * @return response wrapper that tells whether the operation was successful or not
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response createJvm(JvmRequestData jvmRequestData);

    /**
     * Update a JVM
     * @param jvmRequestData {@link JvmRequestData}
     * @return response wrapper that tells whether the operation was successful or not
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    Response updateJvm(JvmRequestData jvmRequestData);

    /**
     * Delete a JVM
     * @param name the JVM name
     * @return response wrapper that tells whether the operation was successful or not
     */
    @DELETE
    @Path("/{name}")
    Response deleteJvm(@PathParam("name") String name);

    /**
     * Control a JVM e.g start or stop a JVM
     * @param jvmName the name of the JVM
     * @param jvmControlDataRequest {@link JvmControlDataRequest}
     * @return response wrapper that tells whether the operation was successful or not
     */
    @POST
    @Path("/{name}/commands")
    @Consumes(MediaType.APPLICATION_JSON)
    Response controlJvm(@PathParam("name") String jvmName, JvmControlDataRequest jvmControlDataRequest);
}
