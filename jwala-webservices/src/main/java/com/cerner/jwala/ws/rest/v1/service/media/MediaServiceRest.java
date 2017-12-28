package com.cerner.jwala.ws.rest.v1.service.media;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.persistence.jpa.domain.JpaMedia;
import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;
import io.swagger.annotations.*;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * The REST service contract
 */
@Api(value = "/media", tags = "media")
@Path("/media")
@Produces(MediaType.APPLICATION_JSON)
public interface MediaServiceRest {

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation(value = "Create a media to be used by the JVMs and Web Servers",
            response = JpaMedia.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Failed to retrieve attachments"))
    Response createMedia(@ApiParam(value = "The media binary and deployment attributes", required = true) final List<Attachment> attachments);

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update an existing media",
            response = JpaMedia.class
    )
    Response updateMedia(@ApiParam(value = "The media to be updated", required = true) JpaMedia media, @BeanParam AuthenticatedUser aUser);

    @DELETE
    @Path("/{mediaName}/{mediaType}")
    @ApiOperation(value = "Delete a media entry",
            response = Response.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Failed to retrieve attachments"))
    Response removeMedia(@ApiParam(value = "The name of the media to be deleted", required = true) @PathParam("mediaName") String name,
                         @ApiParam(value = "The type of the media to be deleted", required = true) @PathParam("mediaType") String type, @BeanParam AuthenticatedUser aUser);

    @GET
    @ApiOperation(value = "Get a media entry by its name or ID",
            notes = "When no ID is specified the name attribute is used, if no name attribute is specified either then all of the media entries are returned",
            response = JpaMedia.class
    )
    Response getMedia(@ApiParam(value = "The ID of the media") @MatrixParam("id") Long id,
                      @ApiParam(value = "The name of the media") @MatrixParam("name") String mediaName, @BeanParam AuthenticatedUser aUser);

    @GET
    @Path("/types")
    @ApiOperation(value = "Get all of the media types currently configured",
            response = JpaMedia.class
    )
    Response getMediaTypes();

}
