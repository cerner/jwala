package com.cerner.jwala.persistence.jpa.service.exception;

/**
 * Created by Jedd Cuison on 8/26/2015.
 */
public class NonRetrievableResourceTemplateContentException extends RuntimeException {

    /**
     * Exception wrapper for failure to retrieve resource template content.
     *
     * @param entityName
     * @param resourceTemplateName
     * @param t
     */
    public NonRetrievableResourceTemplateContentException(final String entityName, final String resourceTemplateName, final Throwable t) {
        super("Can't retrieve contents of " + resourceTemplateName + " of " + entityName, t);
    }

}
