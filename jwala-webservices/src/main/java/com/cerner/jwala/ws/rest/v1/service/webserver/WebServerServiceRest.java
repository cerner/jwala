package com.cerner.jwala.ws.rest.v1.service.webserver;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;
import com.cerner.jwala.ws.rest.v1.service.webserver.impl.JsonControlWebServer;
import com.cerner.jwala.ws.rest.v1.service.webserver.impl.JsonCreateWebServer;
import com.cerner.jwala.ws.rest.v1.service.webserver.impl.JsonUpdateWebServer;
import io.swagger.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(value = "/webservers", tags = "webservers")
@Path("/webservers")
@Produces(MediaType.APPLICATION_JSON)
public interface WebServerServiceRest {

    @GET
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation(value = "Get all the web servers in a group",
            response = WebServer.class
    )
    Response getWebServers(@ApiParam(value = "The web servers' group ID", required = true) @QueryParam("groupId") final Identifier<Group> aGroupId);

    @GET
    @Path("/{webserverId}")
    @ApiOperation(value = "Get a single web server by ID",
            response = WebServer.class
    )
    Response getWebServer(@ApiParam(value = "The web server's ID", required = true) @PathParam("webserverId") final Identifier<WebServer> aWebServerId);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create a web server on Jwala database, The actual webserver instance on the server is created only after generate operation",
            response = WebServer.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Web Server already exists"))
    Response createWebServer(@ApiParam(value = "The configuration info for the web server to be created", required = true) final JsonCreateWebServer aWebServerToCreate,
                             @BeanParam final AuthenticatedUser aUser);

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update an existing web server",
            response = WebServer.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Failed to update the web server"))
    Response updateWebServer(@ApiParam(value = "The configuration info for the web server to be updated", required = true) final JsonUpdateWebServer aWebServerToUpdate,
                             @BeanParam final AuthenticatedUser aUser);

    @DELETE
    @Path("/{webserverId}")
    @ApiOperation(value = "Delete a web server with the boolean hardDelete, when set to true it will delete web server from services and Jwala database and when set to false it will only delete the web server from Jwala database",
            response = Response.class
    )
    Response deleteWebServer(@ApiParam(value = "The ID of the web server to be deleted", required = true) @PathParam("webserverId") final Identifier<WebServer> id,
                             @ApiParam(value = "Deletes the web server and its service when set to true; only deletes the web server when false", required = true) @QueryParam("hardDelete") boolean hardDelete,
                             @BeanParam final AuthenticatedUser user);

    @POST
    @Path("/{webServerId}/commands")
    @ApiOperation(value = "Start and stop a web server",
            response = String.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Web Server operation unsuccessful"))
    Response controlWebServer(@ApiParam(value = "The ID of the web server", required = true) @PathParam("webServerId") final Identifier<WebServer> aWebServerId,
                              @ApiParam(value = "The control operation to execute", required = true) final JsonControlWebServer aWebServerToControl,
                              @BeanParam final AuthenticatedUser aUser,
                              @ApiParam(value = "If set to true then block the thread until the control operation returns", required = true) @QueryParam("wait") final Boolean wait,
                              @ApiParam(value = "When 'wait=true' set a maximum time to block the thread", required = true) @QueryParam("timeout") final Long waitTimeout);

    @GET
    @Path("/{webServerName}/conf")
    @ApiOperation(value = "Generate the httpd.conf for the web server",
            response = String.class
    )
    Response generateConfig(@ApiParam(value = "The name of the web server to generate the httpd.conf", required = true) @PathParam("webServerName") final String webServerName);

    @PUT
    @Path("/{webServerName}/conf/{fileName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Generate and deploy a web server resource",
            response = WebServer.class
    )
    Response generateAndDeployConfig(@ApiParam(value = "The name of the web server associated with the resource", required = true) @PathParam("webServerName") final String webServerName,
                                     @ApiParam(value = "The name of the resource to be generated and deployed", required = true) @PathParam("fileName") String fileName,
                                     @BeanParam final AuthenticatedUser aUser);

    @PUT
    @Path("/{webServerName}/conf/deploy")
    @ApiOperation(value = "Generate and deploy all of the resources for a web server and create the service",
            response = WebServer.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Failed for web server"))
    Response generateAndDeployWebServer(@ApiParam(value = "The name of the web server to be generated and deployed", required = true) @PathParam("webServerName") final String aWebServerName, @BeanParam final AuthenticatedUser aUser);

    @GET
    @Path("/{webServerId}/conf/current")
    @ApiOperation(value = "Get the httpd.conf for a web server",
            response = CommandOutput.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Request failed"))
    Response getHttpdConfig(@ApiParam(value = "The ID of the web server", required = true) @PathParam("webServerId") final Identifier<WebServer> aWebServerId);

    @GET
    @Path("/{wsName}/resources/name")
    @ApiOperation(value = "Get all of the resource names for a web server",
            response = String.class
    )
    Response getResourceNames(@ApiParam(value = "The name of the web server", required = true) @PathParam("wsName") final String wsName);

    /**
     * Get resource template content.
     *
     * @param wsName               web server name.
     * @param resourceTemplateName the resource template name.
     * @param tokensReplaced       flag that indicates whether to fetch the template with its tokens replaced by their mapped values from the db.
     * @return the template contents
     */
    @GET
    @Path("/{wsName}/resources/template/{resourceTemplateName}")
    @ApiOperation(value = "Get a resource template for a web server",
            response = String.class
    )
    Response getResourceTemplate(@ApiParam(value = "The name of the web server", required = true) @PathParam("wsName") final String wsName,
                                 @ApiParam(value = "The name of the resource", required = true) @PathParam("resourceTemplateName") final String resourceTemplateName,
                                 @ApiParam(value = "If true then return the generated template, else return the raw template", required = true) @QueryParam("tokensReplaced") final boolean tokensReplaced);

    @PUT
    @Path("/{wsName}/resources/template/{resourceTemplateName}")
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Update a web server template",
            response = String.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Failed to update the template"))
    Response updateResourceTemplate(@ApiParam(value = "The name of the web server", required = true) @PathParam("wsName") final String wsName,
                                    @ApiParam(value = "The name of the resource", required = true) @PathParam("resourceTemplateName") final String resourceTemplateName,
                                    @ApiParam(value = "The new content of the resource", required = true) final String content);

    @PUT
    @Path("/{webServerName}/resources/preview/{resourceTemplateName}")
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Preview the generated content of a web server's resource template",
            response = String.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Error previewing template"))
    Response previewResourceTemplate(@ApiParam(value = "The name of the web server", required = true) @PathParam("webServerName") String webServerName,
                                     @ApiParam(value = "The name of the resource to preview", required = true) @PathParam("resourceTemplateName") final String resourceTemplateName,
                                     @ApiParam(value = "The name of the web server's group", required = true) @MatrixParam("groupName") String groupName,
                                     @ApiParam(value = "The content to be generated", required = true) String template);

}
