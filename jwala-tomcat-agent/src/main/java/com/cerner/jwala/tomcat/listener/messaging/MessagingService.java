package com.cerner.jwala.tomcat.listener.messaging;

/**
 * A contract that outlines what a messaging service can and should do
 *
 * Created by Jedd Cuison on 8/15/2016
 */
public interface MessagingService<T> {

    void init();

    void send(T msg);

    void destroy();
}
