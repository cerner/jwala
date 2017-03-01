package com.cerner.jwala.ws.rest.v2.service.jvm;

import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.service.jvm.JvmService;
import com.cerner.jwala.service.jvm.exception.JvmServiceException;
import com.cerner.jwala.ws.rest.JsonResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.ws.rs.core.Response;

/**
 * Implements {JvmServiceRest}
 *
 * Created by Jedd Cuison on 8/9/2016.
 */
public class JvmServiceRestImpl implements JvmServiceRest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmServiceRestImpl.class);

    private final JvmService jvmService;

    public JvmServiceRestImpl(final JvmService jvmService) {
        this.jvmService = jvmService;
    }

    @Override
    public Response getJvm(final String name) {
        LOGGER.debug("Get JVM requested: {}", name);
        return new JsonResponseBuilder<Jvm>().setStatus(Response.Status.OK).setContent(jvmService.getJvm(name)).build();
    }

    @Override
    public Response createJvm(final JvmRequestData jvmRequestData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Response updateJvm(final JvmRequestData jvmRequestData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Response deleteJvm(final String name) {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        try {
            jvmService.deleteJvm(name, auth.getName());
            return Response.ok().build();
        } catch (final JvmServiceException e) {
            LOGGER.error(e.getMessage(), e);
            return new JsonResponseBuilder<String>().setStatus(Response.Status.INTERNAL_SERVER_ERROR)
                    .setContent(e.getMessage()).build();
        }
    }

    @Override
    public Response controlJvm(final String name, final JvmControlDataRequest jvmControlDataRequest) {
        throw new UnsupportedOperationException();
    }
}
