package com.cerner.jwala.ws.rest.v1.service.resource;

import com.cerner.jwala.common.domain.model.resource.ResourceContent;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.persistence.jpa.domain.JpaMedia;
import com.cerner.jwala.service.resource.impl.CreateResourceResponseWrapper;
import io.swagger.annotations.*;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.springframework.beans.factory.InitializingBean;

import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * RESTFul service for resource related operations
 *
 * Created by Eric Pinder on 3/16/2015.
 */
@Api(value = "/resources", tags = "resources")
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
    @ApiOperation(value = "Create a resource template (legacy)",
            notes = "This is a legacy method that needs to be deprecated. Use the create resource method that takes deployFileName as a path parameter and uses the matrix parameters",
            response = CreateResourceResponseWrapper.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Invalid number of attachments"))
    Response createTemplate(@ApiParam(value = "The list of attachments  including the template and the meta data file", required = true) List<Attachment> attachments,
                            @ApiParam(value = "The target entity associated with the template resource", required = true) @PathParam("targetName") final String targetName,
                            @BeanParam AuthenticatedUser user);

    @GET
    @Path("/data")
    @ApiOperation(value = "Get the entity topology for the resources",
            response = ResourceGroup.class
    )
    Response getResourceAttrData();

    /**
     * Gets the resource data topology.
     *
     * @return resource JSON data topology wrapped by {@link Response}.
     */
    @GET
    @Path("/topology")
    @ApiOperation(value = "Get the entity topology for the resources",
            response = ResourceGroup.class
    )
    Response getResourceTopology();

    @GET
    @Path("/{groupName}/{appName}/name")
    @ApiOperation(value = "Get the names of all the resources for an application",
            response = String.class
    )
    Response getApplicationResourceNames(@ApiParam(value = "The application's group name", required = true) @PathParam("groupName") String groupName,
                                         @ApiParam(value = "The application name", required = true) @PathParam("appName") String appName);

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
    @ApiOperation(value = "Get an application resource's content",
            response = String.class
    )
    Response getAppTemplate(@ApiParam(value = "The application's group name", required = true) @PathParam("groupName") String groupName,
                            @ApiParam(value = "The application name", required = true) @PathParam("appName") String appName,
                            @ApiParam(value = "The application's resource name", required = true) @PathParam("templateName") String templateName);

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
    @ApiOperation(value = "Check if a resource file exists for a specific JVM, application, or web server",
            response = Map.class
    )
    Response checkFileExists(@ApiParam(value = "The group name to check") @QueryParam("group") String groupName,
                             @ApiParam(value = "The JVM to check") @QueryParam("jvm") String jvmName,
                             @ApiParam(value = "The application to check") @QueryParam("webapp") String webappName,
                             @ApiParam(value = "The web server to check") @QueryParam("webserver") String webserverName,
                             @ApiParam(value = "The name of the resource to check") @PathParam("fileName") String fileName);

    /**
     * Creates a resource
     * @param deployFilename the name of the resource when deployed*
     * @param createResourceParam contains information on who owns the resource to be created  @return {@link Response}
     * @param attachments a list of attached data (deploy filename, deploy path , content type and template data)
     */
    @POST
    @Path("/{deployFilename}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation(value = "Create a resource template",
            notes = "This is the preferred method for creating a resource",
            response = CreateResourceResponseWrapper.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Failed to create resource"))
    Response createResource(@ApiParam(value = "The name of the resource to be created", required = true) @PathParam("deployFilename") String deployFilename,
                            @ApiParam(value = "The target entity to be associated with the resource", required = true) @MatrixParam("") CreateResourceParam createResourceParam,
                            @ApiParam(value = "The resource file to upload", required = true) List<Attachment> attachments);

    @DELETE
    @Path("/template/{name}")
    @Deprecated
    @ApiOperation(value = "Delete a resource",
            notes = "DEPRECATED METHOD. Use the /templates API for deleting resources.",
            response = Integer.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Resource not found"))
    Response deleteResource(@ApiParam(value = "The name of the resource to be deleted", required = true) @PathParam("name") String templateName,
                            @ApiParam(value = "The target entity associated with the resource to be deleted", required = true) @MatrixParam("") ResourceHierarchyParam resourceHierarchyParam,
                            @BeanParam AuthenticatedUser user);

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
    @ApiOperation(value = "Delete a resource or mulitple resources",
            notes = "This is the preferred method for deleting resources",
            response = Integer.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Resource not found"))
    Response deleteResources(@ApiParam(value = "The list of the name of the resources to be deleted", required = true) @MatrixParam("name") String[] templateNameArray,
                             @ApiParam(value = "The parameters that identify the entity associated with the resource to be deleted", required = true) @MatrixParam("") ResourceHierarchyParam resourceHierarchyParam,
                             @BeanParam AuthenticatedUser user);

    /**
     * Get the template content
     *
     * @param resourceName           the template name
     * @param resourceHierarchyParam the group, JVM, webserver, web app hierarchy info
     * @return the content of the template
     */
    @GET
    @Path("/{resourceName}/content")
    @ApiOperation(value = "Get the template content",
            response = ResourceContent.class
    )
    Response getResourceContent(@ApiParam(value = "The name of the resource", required = true) @PathParam("resourceName") String resourceName,
                                @ApiParam(value = "The parameters that identify the entity associated with the resource", required = true) @MatrixParam("") ResourceHierarchyParam resourceHierarchyParam);

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
    @ApiOperation(value = "Update the template content",
            response = String.class
    )
    Response updateResourceContent(@ApiParam(value = "The name of the resource to be updated", required = true) @PathParam("resourceName") String resourceName,
                                   @ApiParam(value = "The parameters that identify the entity associated with the resource", required = true) @MatrixParam("") ResourceHierarchyParam resourceHierarchyParam,
                                   @ApiParam(value = "The new resource content", required = true) final String content);

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
    @ApiOperation(value = "Update the template meta data",
            response = String.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Failed to update the meta data"))
    Response updateResourceMetaData(@ApiParam(value = "The name of the resource to be updated", required = true) @PathParam("resourceName") String resourceName,
                                    @ApiParam(value = "The parameters that identify the entity associated with the resource", required = true) @MatrixParam("") ResourceHierarchyParam resourceHierarchyParam,
                                    @ApiParam(value = "The new meta data", required = true) final String metaData);

    /**
     * Preview the template content
     *
     * @param resourceHierarchyParam the group, JVM, web server, web app hierarchy info
     * @return the saved content
     */
    @PUT
    @Path("/template/preview/{resourceName}")
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Preview the template content",
            response = String.class
    )
    Response previewResourceContent(@ApiParam(value = "The name of the resource to be updated", required = true) @PathParam("resourceName") String resourceName,
                                    @ApiParam(value = "The parameters that identify the entity associated with the resource", required = true) @MatrixParam("") ResourceHierarchyParam resourceHierarchyParam,
                                    @ApiParam(value = "The content to be previewed", required = true) final String content);

    /**
     * Get the key/value pairings for any external properties that were loaded
     *
     * @return the key/value pairings for any external properties
     */
    @GET
    @Path("/properties")
    // TODO return mime type text/plain
//    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Get the key/value pairings for any external properties that were loaded",
            response = Map.class
    )
    Response getExternalProperties();

    /**
     * Return the properties file as a download
     *
     * @return a link to download the external properties file
     */
    @GET
    @Path("/properties/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @ApiOperation(value = "Return the external properties file as a download",
            response = File.class
    )
    Response getExternalPropertiesDownload();

    @GET
    @Path("/properties/view")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Return the external properties as a string",
            response = String.class
    )
    Response getExternalPropertiesView();

    /**
     * Upload the external properties file
     *
     * @param user a logged in user who's calling this service  @return {@link Response}
     */
    @POST
    @Path("/properties")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation(value = "Upload the external properties file",
            response = CreateResourceResponseWrapper.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "File exceptions thrown while attempting to upload external properties file"))
    Response uploadExternalProperties(@BeanParam AuthenticatedUser user);

    /**
     * Get the name of any templates that were loaded for a resource
     *
     * @return the names of the resource files for a given entity
     */
    @GET
    @Path("templates/names")
    @ApiOperation(value = "Get the name of any templates that were loaded for a resource",
            response = String.class
    )
    Response getResourcesFileNames(@ApiParam(value = "The parameters that identify the entity associated with the resource", required = true) @MatrixParam("") final ResourceHierarchyParam resourceHierarchyParam);

}
