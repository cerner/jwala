package com.cerner.jwala.common.request.jvm;

import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;

/**
 * Class which encapsulates the request for generation of thread dump for a jvm
 */
public class ThreadDumpControlJvmRequest extends ControlJvmRequest {
    private final Jvm jvm;

    public ThreadDumpControlJvmRequest(final Jvm jvm) {
        super(jvm.getId(), JvmControlOperation.THREAD_DUMP);
        this.jvm = jvm;
    }

    /***
     *
      * @return String the string that will be displayed on event log
     */
    @Override
    public String getMessage() {
        return "Performing thread dump for JVM " + jvm.getJvmName() + " on host " + jvm.getHostName();
    }

}
