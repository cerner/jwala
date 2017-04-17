package com.cerner.jwala.common.domain.model.jvm;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.exception.BadRequestException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.cerner.jwala.common.domain.model.jvm.JvmState.JVM_START;
import static com.cerner.jwala.common.domain.model.jvm.JvmState.JVM_STOP;

/**
 * Enumeration of control operations that can be executed against a JVM
 */
public enum JvmControlOperation {


    START("start", JVM_START),
    STOP("stop", JVM_STOP),
    THREAD_DUMP("threadDump", null),
    HEAP_DUMP("heapDump", null),
    DEPLOY_JVM_ARCHIVE("deployConfigArchive", null),
    DELETE_SERVICE("deleteService", null),
    INSTALL_SERVICE("installService", null),
    SCP("secureCopy", null),
    BACK_UP("backupFile", null),
    CREATE_DIRECTORY("createDirectory", null),
    CHANGE_FILE_MODE("changeFileMode", null),
    CHECK_FILE_EXISTS("checkFileExists", null),
    CHECK_SERVICE_STATUS("checkServiceStatus", null);

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
