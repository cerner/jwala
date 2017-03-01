package com.cerner.jwala.ws.rest.v1.service.jvm;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;
import com.cerner.jwala.ws.rest.v1.service.jvm.impl.JsonControlJvm;
import com.cerner.jwala.ws.rest.v1.service.jvm.impl.JsonCreateJvm;
import com.cerner.jwala.ws.rest.v1.service.jvm.impl.JsonUpdateJvm;

import org.springframework.beans.factory.InitializingBean;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/jvms")
@Produces(MediaType.APPLICATION_JSON)
public interface JvmServiceRest {

    @GET
    Response getJvms();

    @GET
    @Path("/{jvmId}")
    Response getJvm(@PathParam("jvmId") final Identifier<Jvm> aJvmId);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response createJvm(JsonCreateJvm jsonCreateJvm, @BeanParam AuthenticatedUser aUser);

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    Response updateJvm(JsonUpdateJvm aJvmToUpdate, @QueryParam("updateJvmPassword") boolean updateJvmPassword,
                       @BeanParam AuthenticatedUser aUser);

    @DELETE
    @Path("/{jvmId}")
    Response removeJvm(@PathParam("jvmId") final Identifier<Jvm> aJvmId,
                       @BeanParam final AuthenticatedUser aUser);

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
    Response diagnoseJvm(@PathParam("jvmId") final Identifier<Jvm> aJvmId, @BeanParam final AuthenticatedUser aUser);

    @GET
    @Path("/{jvmName}/resources/name")
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
    Response getResourceTemplate(@PathParam("jvmName") final String jvmName,
                                 @PathParam("resourceTemplateName") final String resourceTemplateName,
                                 @QueryParam("tokensReplaced") final boolean tokensReplaced);

    @PUT
    @Path("/{jvmName}/resources/template/{resourceTemplateName}")
    @Consumes(MediaType.TEXT_PLAIN)
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
    Response previewResourceTemplate(@PathParam("jvmName") String jvmName,
                                     @PathParam("resourceTemplateName") final String resourceTemplateName,
                                     @MatrixParam("groupName") String groupName,
                                     String template);

}
