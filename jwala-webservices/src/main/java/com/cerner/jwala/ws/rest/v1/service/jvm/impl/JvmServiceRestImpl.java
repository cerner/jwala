package com.cerner.jwala.ws.rest.v1.service.jvm.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityExistsException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.exception.FaultCodeException;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.CommandOutputReturnCode;
import com.cerner.jwala.common.request.jvm.ControlJvmRequest;
import com.cerner.jwala.service.jvm.JvmControlService;
import com.cerner.jwala.service.jvm.JvmService;
import com.cerner.jwala.service.jvm.exception.JvmControlServiceException;
import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;
import com.cerner.jwala.ws.rest.v1.response.ResponseBuilder;
import com.cerner.jwala.ws.rest.v1.service.jvm.JvmServiceRest;

public class JvmServiceRestImpl implements JvmServiceRest {

    private static final long DEFAULT_WAIT_TIMEOUT = 300000L;
    private static final Logger LOGGER = LoggerFactory.getLogger(JvmServiceRestImpl.class);

    private final JvmService jvmService;
    private final JvmControlService jvmControlService;

    @Context
    private MessageContext context;

    public JvmServiceRestImpl(final JvmService theJvmService,
                              final JvmControlService theJvmControlService) {
        jvmService = theJvmService;
        jvmControlService = theJvmControlService;
    }

    @Override
    public Response getJvms() {
        LOGGER.debug("Get JVMs requested");
        final List<Jvm> jvms = new ArrayList<>();
        for (Jvm jvm : jvmService.getJvms()) {
            jvms.add(jvm.toJvmWithoutEncrytedPassword());
        }
        return ResponseBuilder.ok(jvms);
    }

    @Override
    public Response getJvm(final Identifier<Jvm> aJvmId) {
        LOGGER.debug("Get JVM requested: {}", aJvmId);
        Jvm aJvm = jvmService.getJvm(aJvmId).toJvmWithoutEncrytedPassword();
        return ResponseBuilder.ok(aJvm);
    }

    @Override
    public Response createJvm(final JsonCreateJvm jsonCreateJvm, final AuthenticatedUser aUser) {
        try {
            final User user = aUser.getUser();
            LOGGER.info("Create JVM requested: {} by user {}", jsonCreateJvm, user.getId());
            Jvm jvm = jvmService.createJvm(jsonCreateJvm.toCreateAndAddRequest(), user);
            return ResponseBuilder.created(jvm);
        } catch (EntityExistsException eee) {
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    FaultType.DUPLICATE_JVM_NAME, eee.getMessage(), eee));
        }
    }

    @Override
    public Response updateJvm(final JsonUpdateJvm aJvmToUpdate, final boolean updateJvmPassword, final AuthenticatedUser aUser) {
        LOGGER.info("Update JVM requested: {} by user {}", aJvmToUpdate, aUser.getUser().getId());
        try {
            return ResponseBuilder.ok(jvmService.updateJvm(aJvmToUpdate.toUpdateJvmRequest(), updateJvmPassword));
        } catch (EntityExistsException eee) {
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    FaultType.DUPLICATE_JVM_NAME, eee.getMessage(), eee));
        }
    }

    @Override
    public Response deleteJvm(final Identifier<Jvm> id, final boolean hardDelete, final AuthenticatedUser user) {
        LOGGER.info("Delete JVM requested: JVM id = {}, user = {} & hardDelete = {}", id, user.getUser().getId(), hardDelete);
        jvmService.deleteJvm(id, hardDelete, user.getUser());
        return Response.noContent().build();
    }

    @Override
    public Response controlJvm(final Identifier<Jvm> aJvmId, final JsonControlJvm aJvmToControl, Boolean wait,
                               Long waitTimeout, final AuthenticatedUser aUser) {
        LOGGER.debug("Control JVM requested: {} {} by user {} with wait = {} and timeout = {}s", aJvmId, aJvmToControl,
                aUser.getUser().getId(), wait, waitTimeout);

        final CommandOutput commandOutput;
        final ControlJvmRequest controlJvmRequest = new ControlJvmRequest(aJvmId, aJvmToControl.toControlOperation());
        if (Boolean.TRUE.equals(wait)) {
            waitTimeout = waitTimeout == null ? DEFAULT_WAIT_TIMEOUT : waitTimeout * 1000; // waitTimeout is in seconds, need to convert to ms
            try {
                commandOutput = jvmControlService.controlJvmSynchronously(controlJvmRequest, waitTimeout, aUser.getUser());
            } catch (final InterruptedException | JvmControlServiceException e) {
                LOGGER.error("Control a JVM synchronously has failed!", e);
                return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR,
                        new FaultCodeException(FaultType.SERVICE_EXCEPTION, e.getMessage()));
            }
        } else {
            commandOutput = jvmControlService.controlJvm(controlJvmRequest, aUser.getUser());
        }

        if (commandOutput.getReturnCode().wasSuccessful()) {
            return ResponseBuilder.ok(commandOutput);
        } else {
            final String standardError = commandOutput.getStandardError();
            final String standardOutput = commandOutput.getStandardOutput();
            String errMessage = standardError != null && !standardError.isEmpty() ? standardError : standardOutput;
            LOGGER.error("Control JVM unsuccessful: " + errMessage);
            throw new InternalErrorException(FaultType.CONTROL_OPERATION_UNSUCCESSFUL, CommandOutputReturnCode.fromReturnCode(commandOutput.getReturnCode().getReturnCode()).getDesc());
        }
    }

    @Override
    public Response generateAndDeployJvm(final String jvmName, final AuthenticatedUser user) {
        LOGGER.info("Generate and deploy JVM {} by user {}", jvmName, user.getUser().getId());
        return ResponseBuilder.ok(jvmService.generateAndDeployJvm(jvmName, user.getUser()));
    }

    @Override
    public Response generateAndDeployFile(final String jvmName, final String fileName, AuthenticatedUser user) {
        LOGGER.info("Generate and deploy file {} to JVM {} by user {}", fileName, jvmName, user.getUser().getId());
        return ResponseBuilder.ok(jvmService.generateAndDeployFile(jvmName, fileName, user.getUser()));
    }

    @Override
    public Response diagnoseJvm(Identifier<Jvm> aJvmId, final AuthenticatedUser aUser) {
        LOGGER.info("Diagnose JVM {} called by user {}", aJvmId, aUser.getUser().getId());
        jvmService.performDiagnosis(aJvmId, aUser.getUser());
        return Response.ok().build();
    }

    @Override
    public Response getResourceNames(final String jvmName) {
        LOGGER.debug("Get resource names {}", jvmName);
        return ResponseBuilder.ok(jvmService.getResourceTemplateNames(jvmName));
    }

    @Override
    public Response getResourceTemplate(final String jvmName, final String resourceTemplateName,
                                        final boolean tokensReplaced) {
        LOGGER.debug("Get resource template {} for JVM {} : tokens replaced={}", resourceTemplateName, jvmName, tokensReplaced);
        return ResponseBuilder.ok(jvmService.getResourceTemplate(jvmName, resourceTemplateName, tokensReplaced));
    }

    @Override
    public Response updateResourceTemplate(final String jvmName, final String resourceTemplateName,
                                           final String content) {
        LOGGER.info("Update the resource template {} for JVM {}", resourceTemplateName, jvmName);
        LOGGER.debug(content);

        final String someContent = jvmService.updateResourceTemplate(jvmName, resourceTemplateName, content);
        if (someContent != null) {
            return ResponseBuilder.ok(someContent);
        } else {
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    FaultType.PERSISTENCE_ERROR, "Failed to update the template " + resourceTemplateName + " for " + jvmName + ". See the log for more details."));
        }
    }

    @Override
    public Response previewResourceTemplate(final String jvmName, final String fileName, final String groupName, final String template) {
        LOGGER.debug("Preview resource template for JVM {} in group {} with content {}", jvmName, groupName, template);
        try {
            return ResponseBuilder.ok(jvmService.previewResourceTemplate(fileName, jvmName, groupName, template));
        } catch (RuntimeException rte) {
            LOGGER.debug("Error previewing resource.", rte);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    FaultType.INVALID_TEMPLATE, rte.getMessage()));
        }
    }
}
