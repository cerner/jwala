package com.cerner.jwala.service.jvm.impl;

import com.cerner.jwala.common.domain.model.jvm.JvmState;

/**
 * Wrapper for Jvm http request result
 * Created by Jedd Cuison on 3/16/2017
 */
public class JvmHttpRequestResult {

    public final JvmState jvmState;
    public final String details;

    public JvmHttpRequestResult(final JvmState jvmState, final String details) {
        this.jvmState = jvmState;
        this.details = details;
    }

}