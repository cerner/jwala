package com.cerner.jwala.service.media;

/**
 * Exception wrapper for {@link MediaService} errors
 *
 * Created by Jedd Cuison on 12/29/2016
 */
public class MediaServiceException extends RuntimeException {

    public MediaServiceException(final String msg) {
        super(msg);
    }

}
