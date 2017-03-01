package com.cerner.jwala.ws.rest.v2.service;

/**
 *  Content that is passed to a {@link javax.ws.rs.core.Response}
 *
 * Created by Jedd Cuison on 8/14/2016.
 */
public class ResponseContent<T> {

    private final int status;
    private final String message;
    private final T content;

    public ResponseContent(final int status, final String message, final T content) {
        this.status = status;
        this.message = message;
        this.content = content;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getContent() {
        return content;
    }
}
