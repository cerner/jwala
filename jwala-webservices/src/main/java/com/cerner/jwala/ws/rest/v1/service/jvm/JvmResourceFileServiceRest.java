package com.cerner.jwala.ws.rest.v1.service.jvm;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 * Contains REST services that has something to do with resource files e.g. resource file data maintenance, generation etc...
 *
 * Created by Jedd Cuison on 4/15/2016.
 */
@Path("/jvms/resource")
public interface JvmResourceFileServiceRest {

    /**
     * Generates and deploy a single JVM resource file as specified by the fileName I presume...have to confirm
     * @param jvmName the jvm name
     * @param templateName the resource file name ?
     * @return {@link Response}
     */
    @PUT
    @Path("/{jvmName}/conf/{fileName}")
    Response generateAndDeployFile(@PathParam("jvmName") String jvmName, @PathParam("fileName") String templateName);
}
