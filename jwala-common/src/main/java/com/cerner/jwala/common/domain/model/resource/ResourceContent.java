package com.cerner.jwala.common.domain.model.resource;

/**
 * Wraps the resource content and its meta data
 *
 * Created by Jedd Cuison on 7/18/2016
 */
public class ResourceContent {

    private final String metaData;
    private final String content;

    public ResourceContent(final String metaData, final String content) {
        this.metaData = metaData;
        this.content = content;
    }

    public String getMetaData() {
        return metaData;
    }

    public String getContent() {
        return content;
    }
}
