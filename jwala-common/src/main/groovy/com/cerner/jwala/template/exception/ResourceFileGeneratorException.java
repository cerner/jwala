package com.cerner.jwala.template.exception;

import com.cerner.jwala.template.ResourceFileGenerator;

/**
 * Class wrapper for exceptions thrown from {@link ResourceFileGenerator}.
 *
 * Created by Jedd Cuison on 7/15/2016.
 */
public class ResourceFileGeneratorException extends RuntimeException {

    public ResourceFileGeneratorException(final String s, final Throwable throwable) {
        super(s, throwable);
    }
}
