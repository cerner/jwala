package com.cerner.jwala.ws.rest.v1.exceptionmapper;

import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.ws.rest.v1.response.ResponseBuilder;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class InternalErrorExceptionMapper implements ExceptionMapper<InternalErrorException> {

    @Override
    public Response toResponse(final InternalErrorException exception) {
        return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR,
                                     exception);
    }
}
