package com.cerner.jwala.service;

/**
 * Message template that is sent via {@link MessagingService}
 *
 * Created by Jedd Cuison on 11/2/2016
 */
public class Message<B> {

    private Type type;
    private B body;

    public Message(final Type type, final B body) {
        this.type = type;
        this.body = body;
    }

    public Type getType() {
        return type;
    }

    public B getBody() {
        return body;
    }

    // Create additional types if needed
    public enum Type {
        HISTORY, STATE
    }
}
