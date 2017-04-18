package com.cerner.jwala.common.request.jvm;

import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;

/**
 * Created by Jedd Cuison on 4/18/2017
 */
public class StopServiceControlJvmRequest extends ControlJvmRequest {

    private final Jvm jvm;

    public StopServiceControlJvmRequest(final Jvm jvm) {
        super(jvm.getId(), JvmControlOperation.STOP);
        this.jvm = jvm;
    }

    @Override
    public String getMessage() {
        return "Stopping JVM " + jvm.getJvmName() + " on host " + jvm.getHostName();
    }

}
