package com.cerner.jwala.common.request.jvm;

import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;

/**
 * Created by Steven Ger on 4/2/17.
 */
public class DeleteServiceControlJvmRequest extends ControlJvmRequest {

    private final Jvm jvm;

    public DeleteServiceControlJvmRequest(final Jvm jvm) {
        super(jvm.getId(), JvmControlOperation.DELETE_SERVICE);
        this.jvm = jvm;
    }

    @Override
    public String getMessage() {
        return "Deleting JVM service " + jvm.getJvmName() + " on host " + jvm.getHostName();
    }
}
