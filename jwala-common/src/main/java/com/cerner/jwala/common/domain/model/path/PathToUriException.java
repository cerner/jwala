package com.cerner.jwala.common.domain.model.path;

import java.text.MessageFormat;

/**
 * Created by Jedd Cuison on 4/5/2017
 */
public class PathToUriException extends RuntimeException {

    public PathToUriException(final String path) {
        super(MessageFormat.format("Failed to convert path {0} to URI!", path));
    }

}
