package com.cerner.jwala.ws.rest.v1.response;

import javax.ws.rs.core.Response;

import com.cerner.jwala.common.exception.FaultCodeException;
import com.cerner.jwala.common.exception.Success;

import java.util.Map;

public class ResponseBuilder {

    public static Response ok() {
        return new ResponseBuilder().build();
    }

    public static Response ok(final Object someContent) {
        return new ResponseBuilder().applicationResponseContent(someContent).build();
    }

    public static Response created(final Object someContent) {
        return new ResponseBuilder().status(Response.Status.CREATED).applicationResponseContent(someContent).build();
    }

    public static Response notOk(final Response.Status aStatus,
                                 final FaultCodeException aFaultCode) {
        return new ResponseBuilder(aStatus).applicationResponse(new ApplicationResponse(aFaultCode.getMessageResponseStatus(),
                aFaultCode.getMessage())).build();
    }

    private ApplicationResponse applicationResponse;
    private Response.Status status;

    public ResponseBuilder() {
        this(Response.Status.OK);
    }

    public ResponseBuilder(final Response.Status aStatus) {
        status = aStatus;
    }

    public ResponseBuilder applicationResponseContent(final Object someContent) {
        return applicationResponse(new ApplicationResponse(Success.SUCCESS,
                someContent));
    }

    public ResponseBuilder applicationResponse(final ApplicationResponse anApplicationResponse) {
        applicationResponse = anApplicationResponse;
        return this;
    }

    public ResponseBuilder status(final Response.Status aStatus) {
        status = aStatus;
        return this;
    }

    public Response build() {
        return Response.status(status).entity(applicationResponse).build();
    }

    public static Response notOkWithDetails(Response.Status aStatus, FaultCodeException aFaultCode, Map<String, String> errorDetails) {
        String message = aFaultCode.getMessage();
        if (message.contains("InternalErrorException: ")) {
            int colonIndex = message.indexOf(": ");
            message = message.substring(colonIndex + 2, message.length());
        }
        errorDetails.put("message", message);
        return new ResponseBuilder(aStatus).applicationResponse(new ApplicationResponse(aFaultCode.getMessageResponseStatus(),
                errorDetails)).build();
    }
}
