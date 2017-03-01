package com.cerner.jwala.ws.rest.v1.exceptionmapper;

import com.cerner.jwala.common.exception.ExternalSystemErrorException;
import com.cerner.jwala.ws.rest.v1.response.ResponseBuilder;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class ExternalSystemErrorExceptionMapper implements ExceptionMapper<ExternalSystemErrorException> {

    @Override
    public Response toResponse(final ExternalSystemErrorException exception) {
        return ResponseBuilder.notOk(Response.Status.BAD_GATEWAY,
                                     exception);
    }
}
