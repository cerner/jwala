package com.cerner.jwala.ws.rest.v1.service.balancermanager;

import com.cerner.jwala.ws.rest.v1.provider.AuthUser;
import org.springframework.beans.factory.InitializingBean;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/balancermanager")
@Produces(MediaType.APPLICATION_JSON)
public interface BalancerManagerServiceRest {

    @POST
    @Path("/{groupName}")
    Response drainUserGroup(@PathParam("groupName") String groupName, String webServerNames,
                            @BeanParam final AuthUser authUser);

    @POST
    @Path("/{groupName}/{webServerName}")
    Response drainUserWebServer(@PathParam("groupName") String groupName, @PathParam("webServerName") String webServerName,
                                String jvmNames, @BeanParam AuthUser authUser);

    @POST
    @Path("/jvm/{jvmName}")
    Response drainUserJvm(@PathParam("jvmName") String jvmName, @BeanParam AuthUser authUser);

    @POST
    @Path("/jvm/{groupName}/{jvmName}")
    Response drainUserGroupJvm(@PathParam("groupName") String groupName, @PathParam("jvmName") String jvmName,
                               @BeanParam AuthUser authUser);

    @GET
    @Path("/{groupName}")
    // TODO: The path should be /{groupName}/drainStatus
    Response getGroupDrainStatus(@PathParam("groupName") String groupName, @BeanParam AuthUser authUser);

}