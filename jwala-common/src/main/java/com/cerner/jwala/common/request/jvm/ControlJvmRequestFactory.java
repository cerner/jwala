package com.cerner.jwala.common.request.jvm;

import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;

/**
 * Created by Steven Ger on 4/9/17.
 */
public class ControlJvmRequestFactory {

    public static ControlJvmRequest create(JvmControlOperation jvmControlOperation, Jvm jvm) {
        if (jvmControlOperation == null) {
            throw new IllegalArgumentException("jvmControlOperation is required");
        }

        if (jvm == null) {
            throw new IllegalArgumentException("jvm is required");
        }

        switch (jvmControlOperation) {
            case START: return new StartServiceControlJvmRequest(jvm);
            case STOP: return new StopServiceControlJvmRequest(jvm);
            case DELETE_SERVICE: return new DeleteServiceControlJvmRequest(jvm);
            case DEPLOY_JVM_ARCHIVE: return new DeployArchiveControlJvmRequest(jvm);
            case INSTALL_SERVICE: return new InstallServiceControlJvmRequest(jvm);
            case HEAP_DUMP: return new HeapDumpControlJvmRequest(jvm);
            case THREAD_DUMP: return new ThreadDumpControlJvmRequest(jvm);

            default: throw new UnsupportedJvmControlOperationException(jvmControlOperation);
        }
    }
}
