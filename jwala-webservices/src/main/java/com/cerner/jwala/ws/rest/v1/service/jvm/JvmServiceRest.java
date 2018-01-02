package com.cerner.jwala.ws.rest.v1.service.jvm;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;
import com.cerner.jwala.ws.rest.v1.service.jvm.impl.JsonControlJvm;
import com.cerner.jwala.ws.rest.v1.service.jvm.impl.JsonCreateJvm;
import com.cerner.jwala.ws.rest.v1.service.jvm.impl.JsonUpdateJvm;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Api(value = "/jvms", tags = "jvms")
@Path("/jvms")
@Produces(MediaType.APPLICATION_JSON)
public interface JvmServiceRest {

    @GET
    @ApiOperation(value = "Get all the jvms",
            response = List.class
    )
    Response getJvms();

    @GET
    @Path("/{jvmId}")
    @ApiOperation(value = "Get a single jvm by ID",
            response = Jvm.class
    )
    Response getJvm(@PathParam("jvmId") final Identifier<Jvm> aJvmId);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create a jvm",
            response = Jvm.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Jvm already exists"))
    Response createJvm(JsonCreateJvm jsonCreateJvm, @BeanParam AuthenticatedUser aUser);

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update an existing jvm",
            response = Jvm.class
    )
    Response updateJvm(JsonUpdateJvm aJvmToUpdate, @QueryParam("updateJvmPassword") boolean updateJvmPassword,
                       @BeanParam AuthenticatedUser aUser);

    @DELETE
    @Path("/{jvmId}")
    @ApiOperation(value = "Delete an existing jvm",
            response = Response.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Unable to delete jvm"))
    Response deleteJvm(@PathParam("jvmId") Identifier<Jvm> id, @QueryParam("hardDelete") boolean hardDelete,
                       @BeanParam final AuthenticatedUser user);

    /**
     * Control a JVM (e.g. start, stop)
     * @param aJvmId the id of the JVM
     * @param aJvmToControl {@link JsonControlJvm} contains control details
     * @param wait if true the REST service is executed synchronously
     * @param waitTimeout timeout duration while REST is waiting for the synchronous service to finish (in seconds)
     * @param aUser {@link AuthenticatedUser}
     * @return {@link Response}
     */
    @POST
    @Path("/{jvmId}/commands")
    @ApiOperation(value = "Start/stop a jvm or take a heap dump",
            response = String.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Jvm operation unsuccessful"))
    Response controlJvm(@PathParam("jvmId") final Identifier<Jvm> aJvmId,
                        final JsonControlJvm aJvmToControl,
                        @QueryParam("wait") Boolean wait,
                        @QueryParam("timeout") Long waitTimeout,
                        @BeanParam final AuthenticatedUser aUser);

    /**
     * Generate JVM config files then deploy the JVM.
     * @param jvmName the name of the JVM
     * @param aUser the user
     * @return {@link Response}
     */
    @PUT
    @Path("/{jvmName}/conf")
    @ApiOperation(value = "Generates and deploy all JVM resource files",
            response = Jvm.class
    )
    Response generateAndDeployJvm(@PathParam("jvmName") final String jvmName,
                                  @BeanParam final AuthenticatedUser aUser);

    /**
     * Generates and deploy a single JVM resource file as specified by the fileName I presume...have to confirm
     * @param jvmName the jvm name
     * @param fileName the resource file name ?
     * @param aUser the user
     * @return {@link Response}
     */
    @PUT
    @Path("/{jvmName}/conf/{fileName}")
    @ApiOperation(value = "Generates and deploy a single JVM resource file as specified by the fileName",
            response = Jvm.class
    )
    Response generateAndDeployFile(@PathParam("jvmName") final String jvmName,
                                   @PathParam("fileName") final String fileName,
                                   @BeanParam final AuthenticatedUser aUser);

    /**
     * Initiate a heartbeat followed by an SSH check
     * @param aJvmId id of the jvm to diagnose
     * @return A text response indicating whether the diagnose process was initiated.
     */
    @GET
    @Path("/{jvmId}/diagnosis")
    @ApiOperation(value = "Initiate a heartbeat followed by an SSH check",
            response = String.class
    )
    Response diagnoseJvm(@PathParam("jvmId") final Identifier<Jvm> aJvmId, @BeanParam final AuthenticatedUser aUser);

    @GET
    @Path("/{jvmName}/resources/name")
    @ApiOperation(value = "Get all the resources corresponding to a specific jvm",
            response = List.class
    )
    Response getResourceNames(@PathParam("jvmName") final String jvmName);

    /**
     * Get resource template content.
     * @param jvmName JVM name.
     * @param resourceTemplateName the resource template name.
     * @param tokensReplaced flag that indicates whether to fetch the template with its tokens replaced by their mapped values from the db.
     * @return the template contents
     */
    @GET
    @Path("/{jvmName}/resources/template/{resourceTemplateName}")
    @ApiOperation(value = "Get the template of a file for the given jvm and file name",
            response = String.class
    )
    Response getResourceTemplate(@PathParam("jvmName") final String jvmName,
                                 @PathParam("resourceTemplateName") final String resourceTemplateName,
                                 @QueryParam("tokensReplaced") final boolean tokensReplaced);

    @PUT
    @Path("/{jvmName}/resources/template/{resourceTemplateName}")
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Update an existing jvm template with the new content provided",
            response = String.class
    )
        // TODO: Pass authenticated user.
    Response updateResourceTemplate(@PathParam("jvmName") final String jvmName,
                                    @PathParam("resourceTemplateName") final String resourceTemplateName,
                                    final String content);

    /**
     * Request a preview a resource file.
     * @param jvmName the JVM name
     * @param groupName a group name
     * @param template a template
     * @return {@link Response}
     */
    @PUT
    @Path("/{jvmName}/resources/preview/{resourceTemplateName}")
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "View the contents of a jvm resource file",
            response = String.class
    )
    Response previewResourceTemplate(@PathParam("jvmName") String jvmName,
                                     @PathParam("resourceTemplateName") final String resourceTemplateName,
                                     @MatrixParam("groupName") String groupName,
                                     String template);

}
