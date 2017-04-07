package com.cerner.jwala.ws.rest.v1.service.webserver;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;
import com.cerner.jwala.ws.rest.v1.service.webserver.impl.JsonControlWebServer;
import com.cerner.jwala.ws.rest.v1.service.webserver.impl.JsonCreateWebServer;
import com.cerner.jwala.ws.rest.v1.service.webserver.impl.JsonUpdateWebServer;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/webservers")
@Produces(MediaType.APPLICATION_JSON)
public interface WebServerServiceRest {

    @GET
    Response getWebServers(@QueryParam("groupId") final Identifier<Group> aGroupId);

    @GET
    @Path("/{webserverId}")
    Response getWebServer(@PathParam("webserverId") final Identifier<WebServer> aWebServerId);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response createWebServer(final JsonCreateWebServer aWebServerToCreate,
                             @BeanParam final AuthenticatedUser aUser);

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    Response updateWebServer(final JsonUpdateWebServer aWebServerToUpdate,
                             @BeanParam final AuthenticatedUser aUser);

    @DELETE
    @Path("/{webserverId}")
    Response deleteWebServer(@PathParam("webserverId") final Identifier<WebServer> id, @QueryParam("hardDelete") boolean hardDelete,
                             @BeanParam final AuthenticatedUser user);

    @POST
    @Path("/{webServerId}/commands")
    Response controlWebServer(@PathParam("webServerId") final Identifier<WebServer> aWebServerId,
                              final JsonControlWebServer aWebServerToControl,
                              @BeanParam final AuthenticatedUser aUser,
                              @QueryParam("wait") final Boolean wait,
                              @QueryParam("timeout") final Long waitTimeout);

    @GET
    @Path("/{webServerName}/conf")
    Response generateConfig(@PathParam("webServerName") final String webServerName);

    @PUT
    @Path("/{webServerName}/conf/{fileName}")
    @Consumes(MediaType.APPLICATION_JSON)
    Response generateAndDeployConfig(@PathParam("webServerName") final String webServerName, @PathParam("fileName") String fileName,@BeanParam final AuthenticatedUser aUser);

    @PUT
    @Path("/{webServerName}/conf/deploy")
    Response generateAndDeployWebServer(@PathParam("webServerName") final String aWebServerName, @BeanParam final AuthenticatedUser aUser);

    @GET
    @Path("/{webServerId}/conf/current")
    Response getHttpdConfig(@PathParam("webServerId") final Identifier<WebServer> aWebServerId);

    @GET
    @Path("/{wsName}/resources/name")
    Response getResourceNames(@PathParam("wsName") final String wsName);

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
    Response getResourceTemplate(@PathParam("wsName") final String wsName,
                                 @PathParam("resourceTemplateName") final String resourceTemplateName,
                                 @QueryParam("tokensReplaced") final boolean tokensReplaced);

    @PUT
    @Path("/{wsName}/resources/template/{resourceTemplateName}")
    @Consumes(MediaType.TEXT_PLAIN)
    Response updateResourceTemplate(@PathParam("wsName") final String wsName,
                                    @PathParam("resourceTemplateName") final String resourceTemplateName,
                                    final String content);

    @PUT
    @Path("/{webServerName}/resources/preview/{resourceTemplateName}")
    @Consumes(MediaType.TEXT_PLAIN)
    Response previewResourceTemplate(@PathParam("webServerName") String webServerName,
                                     @PathParam("resourceTemplateName") final String resourceTemplateName,
                                     @MatrixParam("groupName") String groupName,
                                     String template);

}
