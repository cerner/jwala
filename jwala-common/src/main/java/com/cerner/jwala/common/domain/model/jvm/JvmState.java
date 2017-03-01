package com.cerner.jwala.common.domain.model.jvm;

import com.cerner.jwala.common.domain.model.state.OperationalState;

/**
 * JvmState defines the known states for JVMs. <p>
 * JVMs utilize jwala-tomcat-agent code to send JVM state to Jwala. <p>
 * The translation is done using the enum names defined for JvmState here.
 *
 * @author Peter Horsfield
 */
public enum JvmState implements OperationalState {

    JVM_NEW(StateLabel.NEW, Started.NO),
    JVM_INITIALIZING(StateLabel.INITIALIZING, Started.YES),
    JVM_INITIALIZED(StateLabel.INITIALIZING, Started.YES),
    JVM_START(StateLabel.START_SENT, Started.YES),
    JVM_STARTING(StateLabel.STARTING, Started.YES),
    JVM_STARTED(StateLabel.STARTED, Started.YES),
    JVM_STOP(StateLabel.STOP_SENT, Started.YES),
    JVM_STOPPING(StateLabel.STOPPING, Started.YES),
    JVM_STOPPED(StateLabel.STOPPED, Started.NO),
    JVM_DESTROYING(StateLabel.DESTROYING, Started.YES),
    JVM_DESTROYED(StateLabel.DESTROYED, Started.NO),
    JVM_UNEXPECTED_STATE(StateLabel.UNEXPECTED_STATE, Started.NO),
    JVM_FAILED(StateLabel.FAILED, Started.NO),
    FORCED_STOPPED(StateLabel.FORCED_STOPPED, Started.NO),
    JVM_UNKNOWN(StateLabel.UNKNOWN, Started.NO);

    private boolean isStartedState;
    private final String stateLabel;

    JvmState(final String stateLabel, final boolean startedFlag) {
        this.stateLabel = stateLabel;
        this.isStartedState = startedFlag;
    }

    @Override
    public String toStateLabel() {
        return stateLabel;
    }

    @Override
    public String toPersistentString() {
        return name();
    }

    public boolean isStartedState() {
        return isStartedState;
    }

    private static class StateLabel {
        public static final String INITIALIZING = "INITIALIZING";
        public static final String NEW = "NEW";
        public static final String START_SENT = "START SENT";
        public static final String STARTING = "STARTING";
        public static final String STARTED = "STARTED";
        public static final String STOP_SENT = "STOP SENT";
        public static final String STOPPING = "STOPPING";
        public static final String STOPPED = "STOPPED";
        public static final String DESTROYING = "DESTROYING";
        public static final String DESTROYED = "DESTROYED";
        public static final String UNEXPECTED_STATE = "UNEXPECTED_STATE";
        public static final String FAILED = "FAILED";
        public static final String FORCED_STOPPED = "FORCE STOPPED";
        public static final String UNKNOWN = "UNKNOWN";
    }

    private static class Started {
        public static final boolean YES = true;
        public static final boolean NO = false;
    }

}
