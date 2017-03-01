package com.cerner.jwala.common.domain.model.jvm;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.exception.BadRequestException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.cerner.jwala.common.domain.AemDomain.NO_JVM_IN_PROGRESS_STATE;
import static com.cerner.jwala.common.domain.model.jvm.JvmState.JVM_START;
import static com.cerner.jwala.common.domain.model.jvm.JvmState.JVM_STOP;

/**
 * Enumeration of control operations that can be executed against a JVM
 */
public enum JvmControlOperation {


    START("start", JVM_START),

    STOP("stop", JVM_STOP),

    THREAD_DUMP(
            "threadDump", NO_JVM_IN_PROGRESS_STATE),

    HEAP_DUMP("heapDump", NO_JVM_IN_PROGRESS_STATE),

    DEPLOY_CONFIG_ARCHIVE("deployConfigArchive", NO_JVM_IN_PROGRESS_STATE),

    DELETE_SERVICE("deleteService", NO_JVM_IN_PROGRESS_STATE),

    INSTALL_SERVICE("installService", NO_JVM_IN_PROGRESS_STATE),

    SCP("secureCopy", NO_JVM_IN_PROGRESS_STATE),

    BACK_UP("backupFile", NO_JVM_IN_PROGRESS_STATE),

    CREATE_DIRECTORY("createDirectory", NO_JVM_IN_PROGRESS_STATE),

    CHANGE_FILE_MODE("changeFileMode", NO_JVM_IN_PROGRESS_STATE),

    CHECK_FILE_EXISTS("checkFileExists", NO_JVM_IN_PROGRESS_STATE);

    private static final Map<String, JvmControlOperation> LOOKUP_MAP = new HashMap<>();

    static {
        for (final JvmControlOperation operation : values()) {
            LOOKUP_MAP.put(operation.operationValue.toLowerCase(Locale.US), operation);
        }
    }

    private final String operationValue;
    private final JvmState operationState;
    
    JvmControlOperation(final String theValue,
                        final JvmState theOperationJvmState) {
        operationValue = theValue;
        operationState = theOperationJvmState;
    }

    public static JvmControlOperation convertFrom(final String aValue) {
        final String value = aValue.toLowerCase(Locale.US);
        JvmControlOperation retVal = LOOKUP_MAP.get(value);
        if (null == retVal) {
            throw new BadRequestException(FaultType.INVALID_JVM_OPERATION, "Invalid operation: " + aValue);
        } else {
            return retVal;
        }
    }

    public String getExternalValue() {
        return operationValue;
    }

    public JvmState getOperationState() {
        return operationState;
    }
}
