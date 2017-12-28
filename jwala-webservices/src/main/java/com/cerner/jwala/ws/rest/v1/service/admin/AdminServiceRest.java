package com.cerner.jwala.ws.rest.v1.service.admin;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.security.core.GrantedAuthority;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.jar.Attributes;

@Api(value = "/admin", tags = "admin")
@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
public interface AdminServiceRest {

    @GET
    @Path("/properties/reload")
    @ApiOperation(value = "Reload the Jwala application properties",
            notes = "Some properties only reload on application restart",
            response = Map.class
    )
    Response reload();

    @GET
    @Path("/properties/view")
    @ApiOperation(value = "Get the Jwala application properties",
            notes = "Return type is a JSON object",
            response = Map.class
    )
    Response view();

    @POST
    @Path("/properties/encrypt")
    @ApiOperation(value = "Encrypt a clear text phrase using the encryptExpression defined in the Jwala application properties",
            response = String.class
    )
    Response encrypt(@ApiParam(value = "The string to decrypt", required = true) String cleartext);

    @GET
    @Path("/manifest")
    @ApiOperation(value = "Get the content of the MANIFEST.MF of the Jwala application",
            response = Attributes.class
    )
    Response manifest(@Context ServletContext context);

    @GET
    @Path("/auth/state")
    @ApiOperation(value = "Get the value if the jwala.authorization property",
            notes = "If true, then some functions are disabled for users not in the jwala.role.admin group; if false then all functionality is available to all users",
            response = String.class
    )
    Response isJwalaAuthorizationEnabled();

    @GET
    @Path("/context/authorization")
    @ApiOperation(value = "Get the authorization details",
            notes = "Returns the details of the authorization configuration",
            response = GrantedAuthority.class
    )
    Response getAuthorizationDetails();
}
