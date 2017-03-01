package com.cerner.jwala.service.repository;

/**
 * {@link com.cerner.jwala.service.repository.RepositoryService} implementation for resources
 *
 * Created by Jedd Cuison on 12/16/2016
 */
public class RepositoryServiceException extends RuntimeException {

    public RepositoryServiceException(final String message) {
        super(message);
    }

    public RepositoryServiceException(final String message, final Throwable t) {
        super(message, t);
    }

}
