package com.cerner.jwala.persistence.jpa.service.exception;

/**
 * Created by Jedd Cuison on 8/28/2015.
 */
public class ResourceTemplateUpdateException extends RuntimeException {

    /**
     * Exception wrapper for resource template update failures.
     *
     * @param entityName
     * @param resourceName
     */
    public ResourceTemplateUpdateException(final String entityName, final String resourceName) {
        super(resourceName + " of " + entityName + " update failed!");
    }

    /**
     * Exception wrapper for resource template update failures.
     *
     * @param entityName
     * @param resourceName
     * @param t
     */
    public ResourceTemplateUpdateException(final String entityName, final String resourceName,  final Throwable t) {
        super(resourceName + " of " + entityName + " update failed!", t);
    }

}
