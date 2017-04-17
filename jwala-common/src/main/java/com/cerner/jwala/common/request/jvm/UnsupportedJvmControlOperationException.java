package com.cerner.jwala.common.request.jvm;

import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;

/**
 * Created by Steven Ger on 4/9/17.
 */
public class UnsupportedJvmControlOperationException extends RuntimeException {

    public UnsupportedJvmControlOperationException(JvmControlOperation jvmControlOperation) {
        super("JvmControlOperation " + jvmControlOperation + " not supported.");
    }
}
