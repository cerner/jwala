package com.cerner.jwala.ws.rest;

import com.cerner.jwala.common.exception.InternalErrorException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.Set;
import java.util.Map;

/**
 * Handler for internal server errors in the REST layer
 *
 * Note: This handler's purpose is to intercept uncaught errors so that they won't bubble up to the UI.
 * It does not replace proper error handling in the REST layer.
 *
 * Created by Jedd Cuison on 8/11/2016
 */
public class RestServiceErrorHandler implements ExceptionMapper {
    private final static Logger LOGGER = LoggerFactory.getLogger(RestServiceErrorHandler.class);

    @Override
    public Response toResponse(final Throwable t) {
        LOGGER.error(t.getMessage(), t);

        final int status;
        final String msg;
        if (t instanceof WebApplicationException) {
            status = ((WebApplicationException) t).getResponse().getStatus();
            msg = t.getMessage();
        } else if (t instanceof ConstraintViolationException) {
            final StringBuilder msgBuilder = new StringBuilder();
            final Set<ConstraintViolation<?>> cvSet = ((ConstraintViolationException) t).getConstraintViolations();
            for (ConstraintViolation<?> aCvSet : cvSet) {
                msgBuilder.append(aCvSet.getMessage()).append("\n");
            }
            status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
            msg = msgBuilder.toString();
        } else {
            status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
            msg = t.getMessage();
        }

        final JsonResponseBuilder jsonResponseBuilder = new JsonResponseBuilder().setStatusCode(status).setMessage(msg);
        if (t instanceof InternalErrorException && ((InternalErrorException) t).getErrorDetails() != null) {
            final Map errorDetails = ((InternalErrorException) t).getErrorDetails();
            if (errorDetails.isEmpty()) {
                jsonResponseBuilder.setContent(ExceptionUtils.getStackTrace(t));
            } else {
                jsonResponseBuilder.setContent(errorDetails);
            }
        } else {
            jsonResponseBuilder.setContent(ExceptionUtils.getStackTrace(t));
        }

        return jsonResponseBuilder.build();
    }
}
