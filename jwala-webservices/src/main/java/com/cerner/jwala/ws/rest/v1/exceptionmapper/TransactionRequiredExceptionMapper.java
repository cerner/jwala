package com.cerner.jwala.ws.rest.v1.exceptionmapper;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.exception.FaultCodeException;
import com.cerner.jwala.ws.rest.v1.response.ResponseBuilder;

import org.apache.openjpa.persistence.TransactionRequiredException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class TransactionRequiredExceptionMapper implements ExceptionMapper<TransactionRequiredException> {

    @Override
    public Response toResponse(final TransactionRequiredException exception) {
        return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR,
                new FaultCodeException(FaultType.PERSISTENCE_ERROR, "Database transaction missing", exception));
    }
}
