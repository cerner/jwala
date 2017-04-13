package com.cerner.jwala.common.request.jvm;

import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;

/**
 * Created on 4/13/2017.
 */
public class HeapDumpControlJvmRequest extends ControlJvmRequest {
    private final Jvm jvm;

    public HeapDumpControlJvmRequest(final Jvm jvm) {
        super(jvm.getId(), JvmControlOperation.HEAP_DUMP);
        this.jvm = jvm;
    }

    @Override
    public String getMessage() {
        return "Performing heap dump for JVM " + jvm.getJvmName() + " on host " + jvm.getHostName();
    }

}
