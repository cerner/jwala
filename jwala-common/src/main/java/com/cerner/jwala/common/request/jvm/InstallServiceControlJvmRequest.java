package com.cerner.jwala.common.request.jvm;

import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;

/**
 * Created by Steven Ger on 4/2/17.
 */
public class InstallServiceControlJvmRequest extends ControlJvmRequest {

    private final Jvm jvm;

    public InstallServiceControlJvmRequest(final Jvm jvm) {
        super(jvm.getId(), JvmControlOperation.INSTALL_SERVICE);
        this.jvm = jvm;
    }

    @Override
    public String getMessage() {
        return "Installing JVM service " + jvm.getJvmName() + " on host " + jvm.getHostName();
    }
}
