package com.cerner.jwala.ws.rest.v1.service.balancermanager.impl;

import com.cerner.jwala.common.domain.model.balancermanager.BalancerManagerState;
import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.exception.FaultCodeException;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.service.balancermanager.BalancerManagerService;
import com.cerner.jwala.ws.rest.v1.provider.AuthUser;
import com.cerner.jwala.ws.rest.v1.response.ResponseBuilder;
import com.cerner.jwala.ws.rest.v1.service.balancermanager.BalancerManagerServiceRest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;

public class BalancerManagerServiceRestImpl implements BalancerManagerServiceRest {

    final BalancerManagerService balancerManagerService;

    private static final Logger LOGGER = LoggerFactory.getLogger(BalancerManagerServiceRestImpl.class);

    public BalancerManagerServiceRestImpl(final BalancerManagerService balancerManagerService) {
        this.balancerManagerService = balancerManagerService;
    }

    @Override
    public Response drainUserGroup(final String groupName, final String webServerNames, final AuthUser authUser) {
        try {
            BalancerManagerState balancerManagerState = balancerManagerService.drainUserGroup(groupName, webServerNames,
                    authUser.getUserName());
            return ResponseBuilder.ok(balancerManagerState);
        } catch (InternalErrorException iee) {
            LOGGER.error("Failed to drain web servers {} of group {}!", webServerNames, groupName, iee);
            final String message = "Please make sure that the following web servers " + webServerNames + " in group " + groupName + " are STARTED before draining.";
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    FaultType.SERVICE_EXCEPTION, iee.getMessage() + " " + message, iee));
        }
    }

    @Override
    public Response drainUserWebServer(final String groupName, final String webServerName, final String jvmNames,
                                       final AuthUser authUser) {
        try {
            BalancerManagerState balancerManagerState = balancerManagerService
                    .drainUserWebServer(groupName, webServerName, jvmNames, authUser.getUserName());
            return ResponseBuilder.ok(balancerManagerState);
        } catch (InternalErrorException iee) {
            LOGGER.error("Drain web server error ", iee.getMessage());
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    FaultType.INVALID_WEBSERVER_OPERATION, iee.getMessage(), iee));
        }
    }

    @Override
    public Response drainUserJvm(final String jvmName, final AuthUser authUser) {
        BalancerManagerState balancerManagerState = balancerManagerService.drainUserJvm(jvmName, authUser.getUserName());
        return ResponseBuilder.ok(balancerManagerState);
    }

    @Override
    public Response drainUserGroupJvm(final String groupName, final String jvmName, final AuthUser authUser) {
        try {
            BalancerManagerState balancerManagerState = balancerManagerService.drainUserGroupJvm(groupName, jvmName,
                    authUser.getUserName());
            return ResponseBuilder.ok(balancerManagerState);
        } catch (InternalErrorException iee) {
            LOGGER.error("Drain jvm error ", iee.getMessage());
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    FaultType.INVALID_WEBSERVER_OPERATION, iee.getMessage(), iee));
        }
    }

    @Override
    public Response getGroupDrainStatus(final String groupName, final AuthUser authUser) {
        BalancerManagerState balancerManagerState = balancerManagerService.getGroupDrainStatus(groupName, authUser.getUserName());
        return ResponseBuilder.ok(balancerManagerState);
    }

}
