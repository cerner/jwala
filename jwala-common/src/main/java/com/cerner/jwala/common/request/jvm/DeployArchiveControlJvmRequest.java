package com.cerner.jwala.common.request.jvm;

import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;

/**
 * Created by Steven Ger on 4/2/17.
 */
public class DeployArchiveControlJvmRequest extends ControlJvmRequest {

    private final Jvm jvm;

    public DeployArchiveControlJvmRequest(final Jvm jvm) {
        super(jvm.getId(), JvmControlOperation.DEPLOY_JVM_ARCHIVE);
        this.jvm = jvm;
    }

    @Override
    public String getMessage() {
        return "Deploying archived JVM to host " + jvm.getHostName();
    }
}

