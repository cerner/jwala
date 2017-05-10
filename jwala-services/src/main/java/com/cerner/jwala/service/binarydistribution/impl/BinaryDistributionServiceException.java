package com.cerner.jwala.service.binarydistribution.impl;

/**
 * Wrapper for BinaryDistributionService related errors and exceptions
 * Created by Jedd Cuison on 4/20/2017
 */
public class BinaryDistributionServiceException extends RuntimeException {

    public BinaryDistributionServiceException(final String message) {
        super(message);
    }

}
