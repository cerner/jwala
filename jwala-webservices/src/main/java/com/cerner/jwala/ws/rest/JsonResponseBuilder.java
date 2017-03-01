package com.cerner.jwala.ws.rest;

import com.cerner.jwala.ws.rest.v2.service.ResponseContent;

import javax.ws.rs.core.Response;

/**
 * Builder for a {@link Response} that returns application/json content
 *
 * Created by Jedd Cuison on 8/14/2016.
 */
public class JsonResponseBuilder<T> {

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";

    private Response.Status status;
    private int statusCode;
    private String message;
    private T content;

    public JsonResponseBuilder setStatus(final Response.Status status) {
        this.status = status;
        return this;
    }

    public JsonResponseBuilder setStatusCode(final int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public JsonResponseBuilder setMessage(final String message) {
        this.message = message;
        return this;
    }

    @SuppressWarnings("unchecked")
    public JsonResponseBuilder setContent(final Object content) {
        this.content = (T) content;
        return this;
    }

    public Response build() {
        int statusCode = status != null ? status.getStatusCode() : this.statusCode;
        return Response.status(statusCode)
                       .header(CONTENT_TYPE, APPLICATION_JSON)
                       .entity(new ResponseContent<>(statusCode, message, content)).build();
    }
}
