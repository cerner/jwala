package com.cerner.jwala.ws.rest.v1.service.resource;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.springframework.beans.factory.InitializingBean;

import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * RESTFul service for resource related operations
 *
 * Created by Eric Pinder on 3/16/2015.
 */
@Path("/resources")
@Produces(MediaType.APPLICATION_JSON)
public interface ResourceServiceRest extends InitializingBean {

    /**
     * Creates a template file and it's corresponding JSON meta data file.
     * A template file is used when generating the actual resource file what will be deployed to a JVM or web server.
     *
     * @param attachments contains the template's meta data and main content
     * @param targetName  the resource's name when deployed e.g. jwala.properties
     * @param user        a logged in user who's calling this service
     * @return {@link Response}
     */
    @POST
    @Path("/template/{targetName: .*}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    Response createTemplate(List<Attachment> attachments, @PathParam("targetName") final String targetName, @BeanParam AuthenticatedUser user);

    @GET
    @Path("/data")
    Response getResourceAttrData();

    /**
     * Gets the resource data topology.
     *
     * @return resource JSON data topology wrapped by {@link Response}.
     */
    @GET
    @Path("/topology")
    Response getResourceTopology();

    @GET
    @Path("/{groupName}/{appName}/name")
    Response getApplicationResourceNames(@PathParam("groupName") String groupName, @PathParam("appName") String appName);

    /**
     * Gets an application's resource template.
     *
     * @param groupName    the group the application belongs to
     * @param appName      the application name
     * @param templateName the template name
     * @return {@link Response}
     */
    @GET
    @Path("/{groupName}/{appName}/{templateName}")
    Response getAppTemplate(@PathParam("groupName") String groupName, @PathParam("appName") String appName,
                            @PathParam("templateName") String templateName);

    /**
     * Checks if a group/jvm/webapp/webserver contains a resource file.
     *
     * @param groupName     name of the group under which the resource file should exist or the jvm/webapp/webvserver should exist
     * @param jvmName       name of the jvm under which the resource file should exist
     * @param webappName    name of the webapp under which the resource file should exist
     * @param webserverName name of the webserver under which the resource file should exist
     * @param fileName      name of the resource file that is being searched
     * @return returns a json string with the information about the file {@link Response}
     */
    @GET
    @Path("/exists/{fileName}")
    Response checkFileExists(@QueryParam("group") String groupName,
                             @QueryParam("jvm") String jvmName,
                             @QueryParam("webapp") String webappName,
                             @QueryParam("webserver") String webserverName,
                             @PathParam("fileName") String fileName);

    /**
     * Creates a resource
     * @param deployFilename the name of the resource when deployed*
     * @param createResourceParam contains information on who owns the resource to be created  @return {@link Response}
     * @param attachments a list of attached data (deploy filename, deploy path , content type and template data)
     */
    @POST
    @Path("/{deployFilename}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    Response createResource(@PathParam("deployFilename") String deployFilename, @MatrixParam("") CreateResourceParam createResourceParam,
                            List<Attachment> attachments);

    @DELETE
    @Path("/template/{name}")
    @Deprecated
    Response deleteResource(@PathParam("name") String templateName, @MatrixParam("") ResourceHierarchyParam resourceHierarchyParam, @BeanParam AuthenticatedUser user);

    /**
     * Delete resources.
     *
     * @param templateNameArray      contains the template names of resource to delete
     * @param resourceHierarchyParam the entity hierarchy that describes where the resource belongs to
     * @param user                   the user
     * @return a wrapper class that contains the number of records that were deleted
     */
    @DELETE
    @Path("/templates")
    Response deleteResources(@MatrixParam("name") String[] templateNameArray, @MatrixParam("") ResourceHierarchyParam resourceHierarchyParam, @BeanParam AuthenticatedUser user);

    /**
     * Get the template content
     *
     * @param resourceName           the template name
     * @param resourceHierarchyParam the group, JVM, webserver, web app hierarchy info
     * @return the content of the template
     */
    @GET
    @Path("/{resourceName}/content")
    Response getResourceContent(@PathParam("resourceName") String resourceName, @MatrixParam("") ResourceHierarchyParam resourceHierarchyParam);

    /**
     * Update the template content
     *
     * @param resourceName           the template name
     * @param resourceHierarchyParam the group, JVM, web server, web app hierarchy info
     * @return the saved content
     */
    @PUT
    @Path("/template/{resourceName}")
    @Consumes(MediaType.TEXT_PLAIN)
    Response updateResourceContent(@PathParam("resourceName") String resourceName, @MatrixParam("") ResourceHierarchyParam resourceHierarchyParam, final String content);

    /**
     * Update the template meta data
     *
     * @param resourceName           the template name
     * @param resourceHierarchyParam the group, JVM, web server, web app hierarchy info
     * @return the saved meta data
     */
    @PUT
    @Path("/template/metadata/{resourceName}")
    @Consumes(MediaType.TEXT_PLAIN)
    Response updateResourceMetaData(@PathParam("resourceName") String resourceName, @MatrixParam("") ResourceHierarchyParam resourceHierarchyParam, final String metaData);

    /**
     * Preview the template content
     *
     * @param resourceHierarchyParam the group, JVM, web server, web app hierarchy info
     * @return the saved content
     */
    @PUT
    @Path("/template/preview/{resourceName}")
    @Consumes(MediaType.TEXT_PLAIN)
    Response previewResourceContent(@PathParam("resourceName") String resourceName, @MatrixParam("") ResourceHierarchyParam resourceHierarchyParam, final String content);

    /**
     * Get the key/value pairings for any external properties that were loaded
     *
     * @return the key/value pairings for any external properties
     */
    @GET
    @Path("/properties")
    // TODO return mime type text/plain
//    @Produces(MediaType.TEXT_PLAIN)
    Response getExternalProperties();

    /**
     * Return the properties file as a download
     *
     * @return a link to download the external properties file
     */
    @GET
    @Path("/properties/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    Response getExternalPropertiesDownload();

    @GET
    @Path("/properties/view")
    @Produces(MediaType.TEXT_PLAIN)
    Response getExternalPropertiesView();

    /**
     * Upload the external properties file
     *
     * @param user a logged in user who's calling this service  @return {@link Response}
     */
    @POST
    @Path("/properties")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    Response uploadExternalProperties(@BeanParam AuthenticatedUser user);

    /**
     * Get the name of any templates that were loaded for a resource
     *
     * @return the names of the resource files for a given entity
     */
    @GET
    @Path("templates/names")
    Response getResourcesFileNames(@MatrixParam("") final ResourceHierarchyParam resourceHierarchyParam);

}
