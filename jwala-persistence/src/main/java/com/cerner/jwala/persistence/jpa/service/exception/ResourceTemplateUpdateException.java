package com.cerner.jwala.persistence.jpa.service.exception;

import com.cerner.jwala.persistence.jpa.domain.JpaApplication;
import com.cerner.jwala.persistence.jpa.domain.JpaGroup;

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

    /*
    * @param resourceName
    * @Param JpaGroup
     */
    public ResourceTemplateUpdateException(final JpaApplication application, final JpaGroup group){
        super("Resource update for group "+group.getName()+" of application "+ application.getName()+" failed.");
    }



}
