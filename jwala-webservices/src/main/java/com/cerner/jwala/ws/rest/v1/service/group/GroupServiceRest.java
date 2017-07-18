package com.cerner.jwala.ws.rest.v1.service.group;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;
import com.cerner.jwala.ws.rest.v1.provider.NameSearchParameterProvider;
import com.cerner.jwala.ws.rest.v1.service.group.impl.JsonControlGroup;
import com.cerner.jwala.ws.rest.v1.service.group.impl.JsonJvms;
import com.cerner.jwala.ws.rest.v1.service.group.impl.JsonUpdateGroup;
import com.cerner.jwala.ws.rest.v1.service.jvm.impl.JsonControlJvm;
import com.cerner.jwala.ws.rest.v1.service.webserver.impl.JsonControlWebServer;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/groups")
@Produces(MediaType.APPLICATION_JSON)

public interface GroupServiceRest {

    @GET
    Response getGroups(@BeanParam final NameSearchParameterProvider aGroupNameSearch,
                       @QueryParam("webServers") final boolean fetchWebServers);

    @GET
    @Path("/{groupIdOrName}")
    Response getGroup(@PathParam("groupIdOrName") String groupIdOrName,
                      @QueryParam("byName") @DefaultValue("false") boolean byName);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response createGroup(final String aNewGroupName,
                         @BeanParam final AuthenticatedUser aUser);

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    Response updateGroup(final JsonUpdateGroup anUpdatedGroup,
                         @BeanParam final AuthenticatedUser aUser);

    @DELETE
    @Path("/{groupIdOrName}")
    Response removeGroup(@PathParam("groupIdOrName") String name,
                         @QueryParam("byName") @DefaultValue("false") boolean byName);

    @POST
    @Path("/{groupId}/jvms")
    Response addJvmsToGroup(@PathParam("groupId") final Identifier<Group> aGroupId,
                            final JsonJvms someJvmsToAdd,
                            @BeanParam final AuthenticatedUser aUser);

    @DELETE
    @Path("/{groupId}/jvms/{jvmId}")
    Response removeJvmFromGroup(@PathParam("groupId") final Identifier<Group> aGroupId,
                                @PathParam("jvmId") final Identifier<Jvm> aJvmId,
                                @BeanParam final AuthenticatedUser aUser);

    @POST
    @Path("/{groupId}/jvms/commands")
    Response controlGroupJvms(@PathParam("groupId") final Identifier<Group> aGroupId,
                              final JsonControlJvm jvmControlOperation,
                              @BeanParam final AuthenticatedUser aUser);

    /*********************
     * ** JVM Templates ***
     *********************/
    @PUT
    @Path("/{groupName}/jvms/conf/{fileName}")
    Response generateAndDeployGroupJvmFile(@PathParam("groupName") String groupName, @PathParam("fileName") String fileName,
                                           @BeanParam AuthenticatedUser authUser);

    @GET
    @Path("/{groupName}/jvms/resources/name")
    Response getGroupJvmsResourceNames(@PathParam("groupName") final String groupName);

    @GET
    @Path("/{groupName}/jvms/resources/template/{resourceTemplateName}")
    Response getGroupJvmResourceTemplate(@PathParam("groupName") final String groupName,
                                         @PathParam("resourceTemplateName") final String resourceTemplateName,
                                         @QueryParam("tokensReplaced") final boolean tokensReplaced);

    @PUT
    @Path("/{groupName}/jvms/resources/preview/{resourceTemplateName}")
    @Consumes(MediaType.TEXT_PLAIN)
    Response previewGroupJvmResourceTemplate(@PathParam("groupName") String groupName,
                                             @PathParam("resourceTemplateName") final String resourceTemplateName,
                                             String template);

    @PUT
    @Path("/{groupName}/jvms/resources/template/{resourceTemplateName}")
    @Consumes(MediaType.TEXT_PLAIN)
    Response updateGroupJvmResourceTemplate(@PathParam("groupName") final String groupName,
                                            @PathParam("resourceTemplateName") final String resourceTemplateName,
                                            final String content);


    /****************************
     * ** Web Server Templates ***
     ****************************/
    @PUT
    @Path("/{groupName}/webservers/conf/{resourceFileName}")
    Response generateAndDeployGroupWebServersFile(@PathParam("groupName") final String groupName,
                                                  @PathParam("resourceFileName") @DefaultValue("httpd.conf")final String resourceFileName,
                                                  @BeanParam final AuthenticatedUser aUser);

    @GET
    @Path("/{groupName}/webservers/resources/name")
    Response getGroupWebServersResourceNames(@PathParam("groupName") final String groupName);

    @GET
    @Path("/{groupName}/webservers/resources/template/{resourceTemplateName}")
    Response getGroupWebServerResourceTemplate(@PathParam("groupName") final String groupName,
                                               @PathParam("resourceTemplateName") final String resourceTemplateName,
                                               @QueryParam("tokensReplaced") final boolean tokensReplaced);

    @PUT
    @Path("/{groupName}/webservers/resources/preview/{resourceTemplateName}")
    @Consumes(MediaType.TEXT_PLAIN)
    Response previewGroupWebServerResourceTemplate(@PathParam("groupName") String groupName,
                                                   @PathParam("resourceTemplateName") final String resourceTemplateName,
                                                   String template);


    @PUT
    @Path("/{groupName}/webservers/resources/template/{resourceTemplateName}")
    @Consumes(MediaType.TEXT_PLAIN)
    Response updateGroupWebServerResourceTemplate(@PathParam("groupName") final String groupName,
                                                  @PathParam("resourceTemplateName") final String resourceTemplateName,
                                                  final String content);

    /********************
     * ** App Template ***
     ********************/

    @GET
    @Path("/{groupName}/apps/resources/name")
    Response getGroupAppResourceNames(@PathParam("groupName") final String groupName);

    @GET
    @Path("/{groupName}/apps/resources/template/{resourceTemplateName}")
    Response getGroupAppResourceTemplate(@PathParam("groupName") final String groupName,
                                         String appName, @PathParam("resourceTemplateName") final String resourceTemplateName,
                                         @QueryParam("tokensReplaced") final boolean tokensReplaced);

    @PUT
    @Path("/{groupName}/{appName}/apps/resources/template/{resourceTemplateName}")
    @Consumes(MediaType.TEXT_PLAIN)
    Response updateGroupAppResourceTemplate(@PathParam("groupName") final String groupName, @PathParam("appName") String appName,
                                            @PathParam("resourceTemplateName") final String resourceTemplateName,
                                            final String content);

    @PUT
    @Path("/{groupName}/apps/resources/preview/{resourceTemplateName}/{appName}")
    @Consumes(MediaType.TEXT_PLAIN)
    Response previewGroupAppResourceTemplate(@PathParam("groupName") String groupName,
                                             @PathParam("resourceTemplateName") String resourceTemplateName,
                                             @PathParam("appName") String appName,
                                             String template);

    @PUT
    @Path("/{groupName}/apps/conf/{fileName}/{appName}")
    Response generateAndDeployGroupAppFile(@PathParam("groupName") final String groupName,
                                           @PathParam("fileName") final String fileName,
                                           @PathParam("appName") final String appName,
                                           @BeanParam final AuthenticatedUser aUser,
                                           @QueryParam("hostName") final String hostName);

    /************************
     * ** Control Commands ***
     ************************/

    @POST
    @Path("/{groupId}/commands")
    Response controlGroup(@PathParam("groupId") final Identifier<Group> aGroupId,
                          final JsonControlGroup groupControlOperation,
                          @BeanParam final AuthenticatedUser aUser);

    /**
     * Controls all groups.
     * @param jsonControlGroup contains the actual operation to execute e.g. start/stop. Please see {@link JsonControlGroup}.
     * @param authenticatedUser the user who made this control request
     * @return {@link Response}
     */
    @POST
    @Path("/commands")
    Response controlGroups(JsonControlGroup jsonControlGroup, @BeanParam AuthenticatedUser authenticatedUser);

    @POST
    @Path("/{groupId}/webservers/commands")
    Response controlGroupWebservers(@PathParam("groupId") final Identifier<Group> aGroupId,
                                    final JsonControlWebServer jsonControlWebServer,
                                    @BeanParam final AuthenticatedUser aUser);


    @POST
    @Path("/{groupId}/webservers/conf/deploy")
    Response generateGroupWebservers(@PathParam("groupId") final Identifier<Group> aGroupId,
                                     @BeanParam final AuthenticatedUser aUser);

    @POST
    @Path("/{groupId}/jvms/conf/deploy")
    Response generateGroupJvms(@PathParam("groupId") final Identifier<Group> aGroupId,
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
    Response getOtherGroupMembershipDetailsOfTheChildren(@PathParam("groupId") final Identifier<Group> id,
                                                         @QueryParam("groupChildType") final GroupChildType groupChildType);

    @GET
    @Path("/children/startedCount")
    Response getStartedWebServersAndJvmsCount();

    @GET
    @Path("/children/startedAndStoppedCount")
    Response getStartedAndStoppedWebServersAndJvmsCount();

    @GET
    @Path("/{groupName}/children/startedCount")
    Response getStartedWebServersAndJvmsCount(@PathParam("groupName") String groupName);

    @GET
    @Path("/{groupName}/children/startedAndStoppedCount")
    Response getStartedAndStoppedWebServersAndJvmsCount(@PathParam("groupName") String groupName);

    @GET
    @Path("/children/stoppedCount")
    Response getStoppedWebServersAndJvmsCount();

    @GET
    @Path("/{groupName}/children/stoppedCount")
    Response getStoppedWebServersAndJvmsCount(@PathParam("groupName") String groupName);

    @GET
    @Path("/{groupName}/jvms/allStopped")
    Response areAllJvmsStopped(@PathParam("groupName") String groupName);

    @GET
    @Path("/{groupName}/webservers/allStopped")
    Response areAllWebServersStopped(@PathParam("groupName") String groupName);

    /**
     * Get hosts of a group.
     * @param groupName the group's name
     * @return {@link Response} wrapping all the host names of a group
     */
    @GET
    @Path("/{groupName}/hosts")
    Response getHosts(@PathParam("groupName") String groupName);

    /**
     * Return all the unique host names configured for all the groups
     * @return a list of all the unique host names
     */
    @GET
    @Path("/hosts")
    Response getAllHosts();

    /**
     * Returns the state of a group
     * @param groupName the name of the group
     * @return state info of a group
     */
    @GET
    @Path("/{groupName}/state")
    Response getGroupState(@PathParam("groupName") String groupName);

    /**
     * Gets the state info of all groups
     * @return state info of all groups
     */
    @GET
    @Path("/state")
    Response getGroupStates();

}