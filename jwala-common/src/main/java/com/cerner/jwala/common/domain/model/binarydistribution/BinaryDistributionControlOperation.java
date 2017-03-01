package com.cerner.jwala.common.domain.model.binarydistribution;

import static com.cerner.jwala.common.domain.model.binarydistribution.BinaryDistributionStatus.*;

/**
 * Created by Lin-Hung Wu  on 9/7/2016.
 */
public enum BinaryDistributionControlOperation {

    CHECK_FILE_EXISTS(CHECK_FAIL, CHECK_SUCCESSFUL),
    CREATE_DIRECTORY(MKDIR_FAIL, MKDIR_SUCCESSFUL),
    SCP(COPY_FAIL, COPY_SUCCESSFUL),
    DELETE_BINARY(DELETE_FAIL, DELETE_SUCCESSFUL),
    UNZIP_BINARY(UNZIP_FAIL, UNZIP_SUCCESSFUL),
    CHANGE_FILE_MODE(CHANGE_MODE_FAIL, CHANGE_MODE_SUCCESSFUL),
    UNAME(UNAME_FAIL, UNAME_SUCCESSFUL);

    private BinaryDistributionStatus fail, success;

    BinaryDistributionControlOperation(BinaryDistributionStatus fail, BinaryDistributionStatus success) {
        this.fail = fail;
        this.success = success;
    }

    public void setFail(BinaryDistributionStatus fail) {
        this.fail = fail;
    }

    public void setSuccess(BinaryDistributionStatus success) {
        this.success = success;
    }

    public BinaryDistributionStatus getFail() {
        return fail;
    }

    public BinaryDistributionStatus getSuccess() {
        return success;
    }
}