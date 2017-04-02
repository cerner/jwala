package com.cerner.jwala.common.request.jvm;

import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;

/**
 * Created by Steven Ger on 4/2/17.
 */
public class DeployArchiveControlJvmRequest extends ControlJvmRequest {

    private final Jvm jvm;
    private final String jvmJar;

    public DeployArchiveControlJvmRequest(final Jvm jvm, final String jvmJar) {
        super(jvm.getId(), JvmControlOperation.DEPLOY_JVM_ARCHIVE);
        this.jvm = jvm;
        this.jvmJar = jvmJar;
    }

    @Override
    public String getMessage() {
        return "Deploying JVM archive jar " + jvmJar + " to host " + jvm.getHostName();
    }
}

