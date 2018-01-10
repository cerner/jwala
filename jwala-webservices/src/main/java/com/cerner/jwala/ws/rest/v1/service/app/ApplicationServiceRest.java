package com.cerner.jwala.ws.rest.v1.service.app;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.service.exception.ApplicationServiceException;
import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;
import com.cerner.jwala.ws.rest.v1.service.app.impl.JsonCreateApplication;
import com.cerner.jwala.ws.rest.v1.service.app.impl.JsonUpdateApplication;
import io.swagger.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(value = "/applications", tags = "applications")
@Path("/applications")
@Produces(MediaType.APPLICATION_JSON)
public interface ApplicationServiceRest {

    @GET
    @ApiOperation(value = "Get the data for all of the applications configured for a group",
            response = Application.class
    )
    Response getApplications(@ApiParam(value = "The group ID to query", required = true) @QueryParam("group.id") final Identifier<Group> aGroupId);

    @GET
    @Path("/{applicationId}")
    @ApiOperation(value = "Get the data for a specific application by the application ID",
            response = Application.class
    )
    Response getApplication(@ApiParam(value = "The application ID to query", required = true) @PathParam("applicationId") final Identifier<Application> anAppId);

    @GET
    @Path("/application")
    @ApiOperation(value = "Get the data for a specific application by the application name",
            response = Application.class
    )
    Response getApplicationByName(@ApiParam(value = "The application name to query", required = true) @MatrixParam("name") String name);

    @GET
    @Path("/jvm/{jvmId}")
    @ApiOperation(value = "Get the data for all applications associated with a JVM by ID",
            response = Application.class
    )
    Response findApplicationsByJvmId(@ApiParam(value = "The JVM ID to query", required = true) @PathParam("jvmId") final Identifier<Jvm> aJvmId);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create a new application",
            response = Application.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "An application already exists with the provided name"))
    Response createApplication(@ApiParam(value = "The application configuration to use", required = true) final JsonCreateApplication anAppToCreate,
                               @ApiParam(value = "The authentication details of user") @BeanParam final AuthenticatedUser aUser);

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update an existing application",
            response = Application.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "The application update failed"))
    Response updateApplication(@ApiParam(value = "The updated application configuration to use", required = true) final JsonUpdateApplication appsToUpdate,
                               @ApiParam(value = "The authentication details of user") @BeanParam final AuthenticatedUser aUser) throws ApplicationServiceException;

    @DELETE
    @Path("/{applicationId}")
    @ApiOperation(value = "Delete an existing application by ID",
            response = Response.class
    )
    Response removeApplication(@ApiParam(value = "The application ID to query", required = true) @PathParam("applicationId") final Identifier<Application> anAppToRemove,
                               @ApiParam(value = "The authentication details of user") @BeanParam final AuthenticatedUser aUser);

    @PUT
    @Path("/{applicationId}/war/deploy")
    @ApiOperation(value = "Deploy the war associated with the application",
            notes = "The application is deployed to all the hosts configured for the group",
            response = Application.class
    )
    Response deployWebArchive(@ApiParam(value = "The application ID to query", required = true) @PathParam("applicationId") final Identifier<Application> anAppToGet,
                              @ApiParam(value = "The authentication details of user") @BeanParam final AuthenticatedUser aUser);

    @PUT
    @Path("/{applicationId}/war/deploy/{hostName}")
    @ApiOperation(value = "Deploy the war associated with the application to a specific host",
            notes = "The application is deployed only to the host specified",
            response = Application.class
    )
    Response deployWebArchive(@ApiParam(value = "The application ID to query", required = true) @PathParam("applicationId") final Identifier<Application> anAppToGet,
                              @ApiParam(value = "The hostname where the application will be deployed", required = true) @PathParam("hostName") String hostName);

    @GET
    @Path("/{jvmName}/{appName}/resources/name")
    @ApiOperation(value = "Get the resources and template names associated with the application and JVM",
            response = String.class
    )
    Response getResourceNames(@ApiParam(value = "The application name to query", required = true) @PathParam("appName") String appName,
                              @ApiParam(value = "The JVM name to query", required = true) @PathParam("jvmName") String jvmName);

    @PUT
    @Path("/{appName}/resources/template/{resourceTemplateName}")
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Update the resource template for the application",
            response = String.class
    )
    @ApiResponses (@ApiResponse(code = 500, message = "Failed to update the resource template"))
    Response updateResourceTemplate(@ApiParam(value = "The application name to query", required = true) @PathParam("appName") final String appName,
                                    @ApiParam(value = "The name of the resource template to update", required = true) @PathParam("resourceTemplateName") final String resourceTemplateName,
                                    @ApiParam(value = "The JVM name to query", required = true) @MatrixParam("jvmName") final String jvmName,
                                    @ApiParam(value = "The group name to query", required = true) @MatrixParam("groupName") final String groupName,
                                    @ApiParam(value = "The new content of the resource template", required = true) final String content);

    @PUT
    @Path("/{appName}/conf/{resourceTemplateName}")
    @ApiOperation(value = "Deploy a specific application resource",
            response = CommandOutput.class
    )
    Response deployConf(@ApiParam(value = "The application name to query", required = true) @PathParam("appName") String appName,
                        @ApiParam(value = "The group name to query", required = true) @MatrixParam("groupName") String groupName,
                        @ApiParam(value = "The JVM name to query", required = true) @MatrixParam("jvmName") String jvmName,
                        @ApiParam(value = "The resource template name to query", required = true) @PathParam("resourceTemplateName") String resourceTemplateName,
                        @ApiParam(value = "The authentication details of user") @BeanParam AuthenticatedUser aUser);

    @PUT
    @Path("/{appName}/resources/preview/{resourceTemplateName}")
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Get the template content after running it through the template engine",
            response = String.class
    )
    @ApiResponses (@ApiResponse(code = 500, message = "Error previewing template"))
    Response previewResourceTemplate(@ApiParam(value = "The application name to query", required = true) @PathParam("appName") String appName,
                                     @ApiParam(value = "The group name to query", required = true) @MatrixParam("groupName") String groupName,
                                     @ApiParam(value = "The JVM name to query", required = true) @MatrixParam("jvmName") String jvmName,
                                     @ApiParam(value = "The resource template name to query", required = true) @PathParam("resourceTemplateName") String resourceTemplateName,
                                     @ApiParam(value = "The template content to generate", required = true) String template);

    @PUT
    @Path("/{appName}/conf")
    @ApiOperation(value = "Deploy all of the application resources",
            notes = "Does not deploy any resources associated to a JVM and application",
            response = String.class
    )
    Response deployConf(@ApiParam(value = "The application name to query", required = true) @PathParam("appName") String appName,
                        @ApiParam(value = "The authentication details of user") @BeanParam AuthenticatedUser aUser,
                        @ApiParam(value = "The hostname of the target node") @QueryParam("hostName") String hostName);

    @GET
    @Path("/fileExists")
    @ApiOperation(value = "Check if a file exists on a node",
            response = CommandOutput.class
    )
    Response checkIfFileExists(@ApiParam(value = "The absolute path of the file to check", required = true) @QueryParam("filePath") String filePath,
                               @ApiParam(value = "The authentication details of user") @BeanParam AuthenticatedUser aUser,
                               @ApiParam(value = "The name of the host where the file exists", required = true) @QueryParam("hostName") String hostName);

}
