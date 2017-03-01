package com.cerner.jwala.service.resource.impl;

import com.cerner.jwala.persistence.jpa.domain.resource.config.template.ConfigTemplate;

/**
 * A response object wrapper the object being the meta data of a successfully created resource template.
 *
 * Created by Jedd Cuison on 4/13/2016.
 */
public class CreateResourceResponseWrapper {

    private final ConfigTemplate configTemplate;

    public CreateResourceResponseWrapper(final ConfigTemplate configTemplate) {
        this.configTemplate = configTemplate;
    }

    public String getMetaData() {
        return configTemplate != null ? configTemplate.getMetaData() : "{}";
    }
}
