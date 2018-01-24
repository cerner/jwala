package com.cerner.jwala.ws.rest.v1.service.group;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;
import com.cerner.jwala.ws.rest.v1.provider.NameSearchParameterProvider;
import com.cerner.jwala.ws.rest.v1.service.group.impl.JsonControlGroup;
import com.cerner.jwala.ws.rest.v1.service.group.impl.JsonJvms;
import com.cerner.jwala.ws.rest.v1.service.group.impl.JsonUpdateGroup;
import com.cerner.jwala.ws.rest.v1.service.jvm.impl.JsonControlJvm;
import com.cerner.jwala.ws.rest.v1.service.webserver.impl.JsonControlWebServer;
import io.swagger.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@Api(value = "/groups", tags = "groups")
@Path("/groups")
@Produces(MediaType.APPLICATION_JSON)

public interface GroupServiceRest {

    @GET
    @ApiOperation(value = "Get the data for all of the groups",
            response = Group.class
    )
    Response getGroups(@BeanParam final NameSearchParameterProvider aGroupNameSearch,
                       @ApiParam(value = "The boolean value to fetch web servers data", required = true) @QueryParam("webServers") final boolean fetchWebServers);

    @GET
    @Path("/{groupIdOrName}")
    @ApiOperation(value = "Get the data for a specific Group by the Group ID or Name",
            response = Group.class
    )
    Response getGroup(@ApiParam(value = "The group id or name to query") @PathParam("groupIdOrName") String groupIdOrName,
                      @ApiParam(value = "The boolean value to search group by name") @QueryParam("byName") @DefaultValue("false") boolean byName);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create a new group",
            response = Group.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Group already exists with the provided name"))
    Response createGroup(@ApiParam(value = "The group name to use") final String aNewGroupName,
                         @BeanParam final AuthenticatedUser aUser);

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update an existing group",
            response = Group.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Failed to update group"))
    Response updateGroup(@ApiParam(value = "The updated group details") final JsonUpdateGroup anUpdatedGroup,
                         @BeanParam final AuthenticatedUser aUser);

    @DELETE
    @Path("/{groupIdOrName}")
    @ApiOperation(value = "Delete an existing group by ID or name",
            response = Response.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Failed to remove group"))
    Response removeGroup(@ApiParam(value = "The group ID or name to query") @PathParam("groupIdOrName") String name,
                         @ApiParam(value = "The boolean value to delete group by name") @QueryParam("byName") @DefaultValue("false") boolean byName);

    @POST
    @Path("/{groupId}/jvms")
    @ApiOperation(value = "Add JVMs to a group",
            response = Group.class
    )
    Response addJvmsToGroup(@ApiParam(value = "The group ID to query") @PathParam("groupId") final Identifier<Group> aGroupId,
                            @ApiParam(value = "The JVMs to add in a group") final JsonJvms someJvmsToAdd,
                            @BeanParam final AuthenticatedUser aUser);

    @DELETE
    @Path("/{groupId}/jvms/{jvmId}")
    @ApiOperation(value = "Remove JVMS from a group",
            response = Group.class
    )
    Response removeJvmFromGroup(@ApiParam(value = "The group ID to query") @PathParam("groupId") final Identifier<Group> aGroupId,
                                @ApiParam(value = "The JVM IDs to remove from group") @PathParam("jvmId") final Identifier<Jvm> aJvmId,
                                @BeanParam final AuthenticatedUser aUser);

    @POST
    @Path("/{groupId}/jvms/commands")
    @ApiOperation(value = "Start all the JVMs of a group",
            response = CommandOutput.class
    )
    Response controlGroupJvms(@ApiParam(value = "The group ID to query") @PathParam("groupId") final Identifier<Group> aGroupId,
                              @ApiParam(value = "The control group JVMs operation") final JsonControlJvm jvmControlOperation,
                              @BeanParam final AuthenticatedUser aUser);

    /*********************
     * ** JVM Templates ***
     *********************/
    @PUT
    @Path("/{groupName}/jvms/conf/{fileName}")
    @ApiOperation(value = "Generate and deploy group JVM file",
            response = Group.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Failed for generate and deploy a group JVM resource file"))
    Response generateAndDeployGroupJvmFile(@ApiParam(value = "The group name to query") @PathParam("groupName") String groupName,
                                           @ApiParam(value = "The file name to deploy") @PathParam("fileName") String fileName,
                                           @BeanParam AuthenticatedUser authUser);

    @GET
    @Path("/{groupName}/jvms/resources/name")
    @ApiOperation(value = "Get group JVM resource names",
            response = String.class
    )
    Response getGroupJvmsResourceNames(@ApiParam(value = "The group name to query")@PathParam("groupName") final String groupName);

    @GET
    @Path("/{groupName}/jvms/resources/template/{resourceTemplateName}")
    @ApiOperation(value = "Get group JVM resource template",
            response = String.class
    )
    Response getGroupJvmResourceTemplate(@ApiParam(value = "The group name to query") @PathParam("groupName") final String groupName,
                                         @ApiParam(value = "The resource template name") @PathParam("resourceTemplateName") final String resourceTemplateName,
                                         @ApiParam(value = "boolean value for token replaced") @QueryParam("tokensReplaced") final boolean tokensReplaced);

    @PUT
    @Path("/{groupName}/jvms/resources/preview/{resourceTemplateName}")
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Preview group JVM resource template",
            response = String.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Failed to preview group JVM resource template"))
    Response previewGroupJvmResourceTemplate(@ApiParam(value = "The group name to query") @PathParam("groupName") String groupName,
                                             @ApiParam(value = "The resource template name") @PathParam("resourceTemplateName") final String resourceTemplateName,
                                             @ApiParam(value = "The group JVM resource template") String template);

    @PUT
    @Path("/{groupName}/jvms/resources/template/{resourceTemplateName}")
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Update group JVM resource template",
            response = String.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Failed to update group JVM resource template"))
    Response updateGroupJvmResourceTemplate(@ApiParam(value = "The group name to query") @PathParam("groupName") final String groupName,
                                            @ApiParam(value = "The resource template name") @PathParam("resourceTemplateName") final String resourceTemplateName,
                                            @ApiParam(value = "The updated group JVM resource template content") final String content);


    /****************************
     * ** Web Server Templates ***
     ****************************/
    @PUT
    @Path("/{groupName}/webservers/conf/{resourceFileName}")
    @ApiOperation(value = "Generate and deploy group web servers file",
            response = String.class
    )
    Response generateAndDeployGroupWebServersFile(@ApiParam(value = "The group name to query") @PathParam("groupName") final String groupName,
                                                  @ApiParam(value = "The resource file name")@PathParam("resourceFileName") @DefaultValue("httpd.conf")final String resourceFileName,
                                                  @BeanParam final AuthenticatedUser aUser);

    @GET
    @Path("/{groupName}/webservers/resources/name")
    @ApiOperation(value = "Get group web server resource names",
            response = List.class
    )
    Response getGroupWebServersResourceNames(@ApiParam(value = "The group name to query") @PathParam("groupName") final String groupName);

    @GET
    @Path("/{groupName}/webservers/resources/template/{resourceTemplateName}")
    @ApiOperation(value = "Get group web server resource template",
            response = String.class
    )
    Response getGroupWebServerResourceTemplate(@ApiParam(value = "The group name to query") @PathParam("groupName") final String groupName,
                                               @ApiParam(value = "The resource template name") @PathParam("resourceTemplateName") final String resourceTemplateName,
                                               @ApiParam(value = "The boolean value for tokens replaced") @QueryParam("tokensReplaced") final boolean tokensReplaced);

    @PUT
    @Path("/{groupName}/webservers/resources/preview/{resourceTemplateName}")
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Preview group web server resource template",
            response = String.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Failed to preview group web server resource template"))
    Response previewGroupWebServerResourceTemplate(@ApiParam(value = "The group name to query") @PathParam("groupName") String groupName,
                                                   @ApiParam(value = "The resource template name") @PathParam("resourceTemplateName") final String resourceTemplateName,
                                                   @ApiParam(value = "The web server resource resource template") String template);


    @PUT
    @Path("/{groupName}/webservers/resources/template/{resourceTemplateName}")
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Update group web server resource template",
            response = String.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Failed to update group web server resource template"))
    Response updateGroupWebServerResourceTemplate(@ApiParam(value = "The group name to query") @PathParam("groupName") final String groupName,
                                                  @ApiParam(value = "The resource template name") @PathParam("resourceTemplateName") final String resourceTemplateName,
                                                  @ApiParam(value = "The updated web server resource template content") final String content);

    /********************
     * ** App Template ***
     ********************/

    @GET
    @Path("/{groupName}/apps/resources/name")
    @ApiOperation(value = "Get group app resource names",
            response = List.class
    )
    Response getGroupAppResourceNames(@ApiParam(value = "The group name to query") @PathParam("groupName") final String groupName);

    @GET
    @Path("/{groupName}/apps/resources/template/{resourceTemplateName}")
    @ApiOperation(value = "Get group app resource template",
            response = String.class
    )
    Response getGroupAppResourceTemplate(@ApiParam(value = "The group name to query") @PathParam("groupName") final String groupName,
                                         @ApiParam(value = "The application name") String appName, @ApiParam(value = "The resource template name") @PathParam("resourceTemplateName") final String resourceTemplateName,
                                         @ApiParam(value = "boolean value for tokens replaced")@QueryParam("tokensReplaced") final boolean tokensReplaced);

    @PUT
    @Path("/{groupName}/{appName}/apps/resources/template/{resourceTemplateName}")
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Update group application resource template",
            response = String.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Failed to group application resource template"))
    Response updateGroupAppResourceTemplate(@ApiParam(value = "The group name to query") @PathParam("groupName") final String groupName, @PathParam("appName") String appName,
                                            @ApiParam(value = "The resource template name")@PathParam("resourceTemplateName") final String resourceTemplateName,
                                            @ApiParam(value = "The resource template updated content") final String content);

    @PUT
    @Path("/{groupName}/apps/resources/preview/{resourceTemplateName}/{appName}")
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Preview group application resource names",
            response = String.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Failed to preview group application resource template"))
    Response previewGroupAppResourceTemplate(@ApiParam(value = "The group name to query") @PathParam("groupName") String groupName,
                                             @ApiParam(value = "The resource template name") @PathParam("resourceTemplateName") String resourceTemplateName,
                                             @ApiParam(value = "The application name")@PathParam("appName") String appName,
                                             @ApiParam(value = "The resource template content") String template);

    @PUT
    @Path("/{groupName}/apps/conf/{fileName}/{appName}")
    @ApiOperation(value = "Generate and deploy group application file",
            response = Group.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Failed to generate and deploy group application file"))
    Response generateAndDeployGroupAppFile(@ApiParam(value = "The group name to query") @PathParam("groupName") final String groupName,
                                           @ApiParam(value = "The file name to deploy") @PathParam("fileName") final String fileName,
                                           @ApiParam(value = "The application name") @PathParam("appName") final String appName,
                                           @BeanParam final AuthenticatedUser aUser,
                                           @ApiParam(value = "The hostname of remote server") @QueryParam("hostName") final String hostName);

    /************************
     * ** Control Commands ***
     ************************/

    @POST
    @Path("/{groupId}/commands")
    @ApiOperation(value = "Control group start or stop",
            response = CommandOutput.class
    )
    Response controlGroup(@ApiParam(value = "The group ID to query") @PathParam("groupId") final Identifier<Group> aGroupId,
                          @ApiParam(value = "The control group operation") final JsonControlGroup groupControlOperation,
                          @BeanParam final AuthenticatedUser aUser);

    /**
     * Controls all groups.
     * @param jsonControlGroup contains the actual operation to execute e.g. start/stop. Please see {@link JsonControlGroup}.
     * @param authenticatedUser the user who made this control request
     * @return {@link Response}
     */
    @POST
    @Path("/commands")
    @ApiOperation(value = "The control all groups start or stop",
            response = CommandOutput.class
    )
    Response controlGroups(@ApiParam(value = "The control group operation for all groups") JsonControlGroup jsonControlGroup, @BeanParam AuthenticatedUser authenticatedUser);

    @POST
    @Path("/{groupId}/webservers/commands")
    @ApiOperation(value = "Start all the web servers of a group",
            response = CommandOutput.class
    )
    Response controlGroupWebservers(@ApiParam(value = "The group ID to query") @PathParam("groupId") final Identifier<Group> aGroupId,
                                    @ApiParam(value = "The control group operation to start or stop all web servers") final JsonControlWebServer jsonControlWebServer,
                                    @BeanParam final AuthenticatedUser aUser);


    @POST
    @Path("/{groupId}/webservers/conf/deploy")
    @ApiOperation(value = "Generate group web servers",
            response = Group.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Failed to generate group web servers"))
    Response generateGroupWebservers(@ApiParam(value = "The group ID to query") @PathParam("groupId") final Identifier<Group> aGroupId,
                                     @BeanParam final AuthenticatedUser aUser);

    @POST
    @Path("/{groupId}/jvms/conf/deploy")
    @ApiOperation(value = "Generate group JVMs",
            response = Group.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Failed to generate group JVMs"))
    Response generateGroupJvms(@ApiParam(value = "The group ID to query") @PathParam("groupId") final Identifier<Group> aGroupId,
                               @BeanParam final AuthenticatedUser aUser);

    /**
     * Gets the membership details of a group's children in other groups (e.g. jvm1 is a member of group2, group3)
     * Note: The group specified by id will not be included hence the word "Other" in the method name.
     *
     * @param id             the id of the group
     * @param groupChildType the child type to get details on
     * @return membership details of a group's children
     */
    @GET
    @Path("/{groupId}/children/otherGroup/connectionDetails")
    @ApiOperation(value = "Get other group membership details of the children",
            response = WebServer.class
    )
    Response getOtherGroupMembershipDetailsOfTheChildren(@ApiParam(value = "The group ID to query") @PathParam("groupId") final Identifier<Group> id,
                                                         @ApiParam(value = "The group child type") @QueryParam("groupChildType") final GroupChildType groupChildType);

    @GET
    @Path("/children/startedCount")
    @ApiOperation(value = "Get number of started web servers and JVMs count",
            response = Long.class
    )
    Response getStartedWebServersAndJvmsCount();

    @GET
    @Path("/children/startedAndStoppedCount")
    @ApiOperation(value = "Get number of started and stopped web servers, JVMs count",
            response = Long.class
    )
    Response getStartedAndStoppedWebServersAndJvmsCount();

    @GET
    @Path("/{groupName}/children/startedCount")
    @ApiOperation(value = "Get number of started web servers and JVMs count of a group",
            response = Long.class
    )
    Response getStartedWebServersAndJvmsCount(@ApiParam(value = "The group name to query") @PathParam("groupName") String groupName);

    @GET
    @Path("/{groupName}/children/startedAndStoppedCount")
    @ApiOperation(value = "Get number of started and stopped web servers, JVMs count of a group",
            response = Long.class
    )
    Response getStartedAndStoppedWebServersAndJvmsCount(@ApiParam(value = "The group name to query") @PathParam("groupName") String groupName);

    @GET
    @Path("/children/stoppedCount")
    @ApiOperation(value = "Get stopped web servers and JVMs count",
            response = Long.class
    )
    Response getStoppedWebServersAndJvmsCount();

    @GET
    @Path("/{groupName}/children/stoppedCount")
    @ApiOperation(value = "Get stopped web servers and JVMs count of a group",
            response = Long.class
    )
    Response getStoppedWebServersAndJvmsCount(@ApiParam(value = "The group name to query") @PathParam("groupName") String groupName);

    @GET
    @Path("/{groupName}/jvms/allStopped")
    @ApiOperation(value = "check if all JVMs of a group are stopped",
            response = Map.class
    )
    Response areAllJvmsStopped(@ApiParam(value = "The group name to query") @PathParam("groupName") String groupName);

    @GET
    @Path("/{groupName}/webservers/allStopped")
    @ApiOperation(value = "check if all web servers of a group are stopped",
            response = Map.class
    )
    Response areAllWebServersStopped(@ApiParam(value = "The group name to query") @PathParam("groupName") String groupName);

    /**
     * Get hosts of a group.
     * @param groupName the group's name
     * @return {@link Response} wrapping all the host names of a group
     */
    @GET
    @Path("/{groupName}/hosts")
    @ApiOperation(value = "Get all hosts of a group",
            response = List.class
    )
    Response getHosts(@ApiParam(value = "The group name to query") @PathParam("groupName") String groupName);

    /**
     * Return all the unique host names configured for all the groups
     * @return a list of all the unique host names
     */
    @GET
    @Path("/hosts")
    @ApiOperation(value = "Get all hosts",
            response = List.class
    )
    Response getAllHosts();

    /**
     * Returns the state of a group
     * @param groupName the name of the group
     * @return state info of a group
     */
    @GET
    @Path("/{groupName}/state")
    @ApiOperation(value = "Get group state of a specific group",
            response = CurrentState.class
    )
    Response getGroupState(@ApiParam(value = "The group name to query") @PathParam("groupName") String groupName);

    /**
     * Gets the state info of all groups
     * @return state info of all groups
     */
    @GET
    @Path("/state")
    @ApiOperation(value = "Get all group states",
            response = Map.class
    )
    Response getGroupStates();

}