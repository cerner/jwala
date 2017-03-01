package com.cerner.jwala.persistence.jpa.service.exception;

/**
 * Created by Jeffrey.Mahmood@Cerner.com on 9/27/2016.
 */
public class ResourceTemplateMetaDataUpdateException extends RuntimeException {

    /**
     * Exception wrapper for resource meta data update failures.
     *
     * @param entityName the JVM, Web Server, Web Application
     * @param resourceName the template name
     */
    public ResourceTemplateMetaDataUpdateException(final String entityName, final String resourceName) {
        super(resourceName + " of " + entityName + " meta data update failed!");
    }

    /**
     * Exception wrapper for resource template meta data update failures.
     *
     * @param entityName the JVM, Web Server, Web Application
     * @param resourceName the template name
     * @param t wrap an exception
     */
    public ResourceTemplateMetaDataUpdateException(final String entityName, final String resourceName,  final Throwable t) {
        super(resourceName + " of " + entityName + " meta data update failed!", t);
    }


}
