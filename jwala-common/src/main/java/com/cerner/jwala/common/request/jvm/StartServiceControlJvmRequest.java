package com.cerner.jwala.common.request.jvm;

import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;

/**
 * Created by Steven Ger on 4/9/17
 */
public class StartServiceControlJvmRequest  extends ControlJvmRequest {

    private final Jvm jvm;

    public StartServiceControlJvmRequest(final Jvm jvm) {
        super(jvm.getId(), JvmControlOperation.START);
        this.jvm = jvm;
    }

    @Override
    public String getMessage() {
        return "Starting JVM " + jvm.getJvmName() + " on host " + jvm.getHostName();
    }
}
