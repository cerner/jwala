package com.cerner.jwala.persistence.jpa.service.exception;

import com.cerner.jwala.persistence.jpa.domain.JpaApplication;
import com.cerner.jwala.persistence.jpa.domain.JpaGroup;


import java.text.MessageFormat;

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


    /**
     * Exception wrapper for resource template update failures.
     *
     * @param application
     * @param group
     * @param t
     */
    public ResourceTemplateUpdateException(final JpaApplication application, final JpaGroup group, final Throwable t) {
        super(MessageFormat.format("Resource update for group {0} of application {1} failed.",group.getName(),
                application.getName()),t);
    }


    /**
     * Exception wrapper for resource template update failures.
     *
     * @param application
     * @param group
     * @param message
     */
    public ResourceTemplateUpdateException(final JpaApplication application, final JpaGroup group, final String
            message) {
        super(MessageFormat.format("Resource update for group {0} of application {1} failed {2}", group
                .getName(), application.getName(), message));
    }



}
