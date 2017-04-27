package com.cerner.jwala.common.domain.model.media;

import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * The list of media types
 *
 * Created by Jedd Anthony Cuison on 12/6/2016
 */
@JsonSerialize(using = MediaTypeSerializer.class)
public enum MediaType {
    APACHE("Apache HTTPD"), JDK("JDK"), TOMCAT("Apache Tomcat");

    final String displayName;

    MediaType(final String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
