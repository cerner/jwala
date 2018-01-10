package com.cerner.jwala.ws.rest.v1.service.balancermanager;

import com.cerner.jwala.common.domain.model.balancermanager.BalancerManagerState;
import com.cerner.jwala.ws.rest.v1.provider.AuthUser;
import io.swagger.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(value = "/balancermanager", tags = "balancermanager")
@Path("/balancermanager")
@Produces(MediaType.APPLICATION_JSON)
public interface BalancerManagerServiceRest {

    @POST
    @Path("/{groupName}")
    @ApiOperation(value = "Drain the Web Servers in the group",
            notes = "A list of web server names can be used to filter the web servers in the group (optional)",
            response = BalancerManagerState.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Failed to drain the web servers"))
    Response drainUserGroup(@ApiParam(value = "The name of the group to drain all the web servers", required = true) @PathParam("groupName") String groupName,
                            @ApiParam(value = "The name of the the web servers to drain") String webServerNames,
                            @ApiParam(value = "The authentication details of user") @BeanParam final AuthUser authUser);

    @POST
    @Path("/{groupName}/{webServerName}")
    @ApiOperation(value = "Drain a single web server in a group",
            response = BalancerManagerState.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Drain web server error"))
    Response drainUserWebServer(@ApiParam(value = "The name of the group to drain the web server", required = true) @PathParam("groupName") String groupName,
                                @ApiParam(value = "The name of the web server to drain", required = true) @PathParam("webServerName") String webServerName,
                                String jvmNames, @ApiParam(value = "The authentication details of user") @BeanParam AuthUser authUser);

    @POST
    @Path("/jvm/{jvmName}")
    @ApiOperation(value = "Drain a single JVM",
            response = BalancerManagerState.class
    )
    Response drainUserJvm(@ApiParam(value = "The name of the JVM to drain", required = true) @PathParam("jvmName") String jvmName, @ApiParam(value = "The authentication details of user") @BeanParam AuthUser authUser);

    @POST
    @Path("/jvm/{groupName}/{jvmName}")
    @ApiOperation(value = "Drain a single JVM in a specific group",
            response = BalancerManagerState.class
    )
    @ApiResponses(@ApiResponse(code = 500, message = "Drain JVM error"))
    Response drainUserGroupJvm(@ApiParam(value = "The JVM's group name", required = true) @PathParam("groupName") String groupName,
                               @ApiParam(value = "The name of the JVM to drain", required = true) @PathParam("jvmName") String jvmName,
                               @ApiParam(value = "The authentication details of user") @BeanParam AuthUser authUser);

    @GET
    @Path("/{groupName}")
    // TODO: The path should be /{groupName}/drainStatus
    @ApiOperation(value = "Get the status of any drained web servers and JVMs belonging to the group",
            response = BalancerManagerState.class
    )
    Response getGroupDrainStatus(@ApiParam(value = "The name of the group to query for the status", required = true) @PathParam("groupName") String groupName, @ApiParam(value = "The authentication details of user") @BeanParam AuthUser authUser);

}