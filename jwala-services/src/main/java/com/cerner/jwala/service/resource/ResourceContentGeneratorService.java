package com.cerner.jwala.service.resource;

import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.service.resource.impl.ResourceGeneratorType;

/**
 * Contract for a service that generates resource content
 *
 * Created by Jedd Cuison on 7/26/2016
 */
public interface ResourceContentGeneratorService {

    /**
     * Generate a resource content from a template by merging data
     * @param template the template
     * @param entity an entity that contains data to map to the template e.g. JVM, WebServer etc...
     * @return the content (template + data)
     */
    <T> String generateContent(final String fileName, final String template, final ResourceGroup resourceGroup, T entity, ResourceGeneratorType resourceGeneratorType);

}
