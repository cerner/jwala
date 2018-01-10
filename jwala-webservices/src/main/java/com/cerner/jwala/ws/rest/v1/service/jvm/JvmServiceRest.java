package com.cerner.jwala.ws.rest.v1.service.jvm;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;
import com.cerner.jwala.ws.rest.v1.service.jvm.impl.JsonControlJvm;
import com.cerner.jwala.ws.rest.v1.service.jvm.impl.JsonCreateJvm;
import com.cerner.jwala.ws.rest.v1.service.jvm.impl.JsonUpdateJvm;
import io.swagger.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Api(value = "/jvms", tags = "jvms")
@Path("/jvms")
@Produces(MediaType.APPLICATION_JSON)
public interface JvmServiceRest {

    @GET
    @ApiOperation(value = "Get all the JVMs",
            response = List.class
    )
    Response getJvms();

    @GET
    @Path("/{jvmId}")
    @ApiOperation(value = "Get a single JVM by ID",
            response = Jvm.class
    )
    Response getJvm(@ApiParam(value = "The JVM's ID", required = true) @PathParam("jvmId") final Identifier<Jvm> aJvmId);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create a JVM on Jwala database, The actual JVM instance on the server is created only after generate operation",
            response = Jvm.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "JVM already exists"))
    Response createJvm(@ApiParam(value = "The configuration info for the JVM to be created", required = true) JsonCreateJvm jsonCreateJvm,
                       @ApiParam(value = "The authentication details of user") @BeanParam AuthenticatedUser aUser);

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update an existing JVM",
            response = Jvm.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Failed to update the JVM"))
    Response updateJvm(@ApiParam(value = "The configuration info for the JVM to be updated", required = true) JsonUpdateJvm aJvmToUpdate, @ApiParam(value = "If set to true then update jvm password") @QueryParam("updateJvmPassword") boolean updateJvmPassword,
                       @ApiParam(value = "The user authentication details") @BeanParam AuthenticatedUser aUser);

    @DELETE
    @Path("/{jvmId}")
    @ApiOperation(value = "Delete an existing JVM with the boolean hardDelete, when set to true it will delete JVM from services and Jwala database and when set to false it will only delete the JVM from Jwala database",
            response = Response.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Unable to delete JVM"))
    Response deleteJvm(@ApiParam(value = "The ID of the JVM to be deleted", required = true) @PathParam("jvmId") Identifier<Jvm> id, @ApiParam(value = "Deletes the JVM and its service when set to true; only deletes the JVM when false", required = true) @QueryParam("hardDelete") boolean hardDelete,
                       @ApiParam(value = "The authentication details of user") @BeanParam final AuthenticatedUser user);

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
    @ApiOperation(value = "Start/stop a JVM or take a heap dump",
            response = String.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "JVM operation unsuccessful"))
    Response controlJvm(@ApiParam(value = "The ID of the JVM", required = true) @PathParam("jvmId") final Identifier<Jvm> aJvmId,
                        @ApiParam(value = "The control operation to execute", required = true) final JsonControlJvm aJvmToControl,
                        @ApiParam(value = "If set to true then block the thread until the control operation returns", required = true) @QueryParam("wait") Boolean wait,
                        @ApiParam(value = "When 'wait=true' set a maximum time to block the thread", required = true) @QueryParam("timeout") Long waitTimeout,
                        @ApiParam(value = "The authentication details of user") @BeanParam final AuthenticatedUser aUser);

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
    @ApiResponses(@ApiResponse(code = 500, message = "Remote generation of JVM failed"))
    Response generateAndDeployJvm(@ApiParam(value = "The name of the JVM to generated", required = true) @PathParam("jvmName") final String jvmName,
                                  @ApiParam(value = "The authentication details of user") @BeanParam final AuthenticatedUser aUser);

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
    @ApiResponses(@ApiResponse(code = 500, message = "Failed to deploy JVM resource"))
    Response generateAndDeployFile(@ApiParam(value = "The name of the JVM associated with the resource", required = true) @PathParam("jvmName") final String jvmName,
                                   @ApiParam(value = "The name of the resource to be generated and deployed", required = true) @PathParam("fileName") final String fileName,
                                   @ApiParam(value = "The authentication details of user") @BeanParam final AuthenticatedUser aUser);

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
    Response diagnoseJvm(@ApiParam(value = "The id of the JVM whose state needs to be diagnosed", required = true) @PathParam("jvmId") final Identifier<Jvm> aJvmId,
                         @ApiParam(value = "The authentication details of user") @BeanParam final AuthenticatedUser aUser);

    @GET
    @Path("/{jvmName}/resources/name")
    @ApiOperation(value = "Get all the resources corresponding to a specific JVM",
            response = List.class
    )
    Response getResourceNames(@ApiParam(value = "The name of the JVM for which corresponding all resources are to be obtained", required = true) @PathParam("jvmName") final String jvmName);

    /**
     * Get resource template content.
     * @param jvmName JVM name.
     * @param resourceTemplateName the resource template name.
     * @param tokensReplaced flag that indicates whether to fetch the template with its tokens replaced by their mapped values from the db.
     * @return the template contents
     */
    @GET
    @Path("/{jvmName}/resources/template/{resourceTemplateName}")
    @ApiOperation(value = "Get the template of a file for the given JVM and file name",
            response = String.class
    )
    Response getResourceTemplate(@ApiParam(value = "The name of the JVM", required = true) @PathParam("jvmName") final String jvmName,
                                 @ApiParam(value = "The name of the resource", required = true) @PathParam("resourceTemplateName") final String resourceTemplateName,
                                 @ApiParam(value = "If true then return the generated template, else return the raw template", required = true) @QueryParam("tokensReplaced") final boolean tokensReplaced);

    @PUT
    @Path("/{jvmName}/resources/template/{resourceTemplateName}")
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Update an existing JVM template with the new content provided",
            response = String.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Failed to update the template"))
        // TODO: Pass authenticated user.
    Response updateResourceTemplate(@ApiParam(value = "The name of the JVM", required = true) @PathParam("jvmName") final String jvmName,
                                    @ApiParam(value = "The name of the resource", required = true) @PathParam("resourceTemplateName") final String resourceTemplateName,
                                    @ApiParam(value = "The new content of the resource", required = true) final String content);

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
    @ApiOperation(value = "View the contents of a JVM resource file",
            response = String.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Error previewing template"))
    Response previewResourceTemplate(@ApiParam(value = "The name of the JVM", required = true) @PathParam("jvmName") String jvmName,
                                     @ApiParam(value = "The name of the resource to preview", required = true) @PathParam("resourceTemplateName") final String resourceTemplateName,
                                     @ApiParam(value = "The name of the JVM's group", required = true) @MatrixParam("groupName") String groupName,
                                     @ApiParam(value = "The content to be generated", required = true) String template);

}
