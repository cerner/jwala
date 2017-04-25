package com.cerner.jwala.common.domain.model.media;

/**
 * The list of media types
 *
 * Created by Jedd Anthony Cuison on 12/6/2016
 */
public enum MediaType {
    APACHE("Apache HTTPD"), JDK("JDK"), TOMCAT("Apache Tomcat");

    final String name;

    MediaType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
