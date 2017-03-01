package com.cerner.jwala.common.domain.model.binarydistribution;

/**
 * Created by Lin-Hung Wu on 9/7/2016.
 */
public enum BinaryDistributionStatus {

    CHECK_FAIL("check fail"),
    CHECK_SUCCESSFUL("check successful"),
    COPY_FAIL("copy fail"),
    COPY_SUCCESSFUL("copy successful"),
    DELETE_FAIL("delete fail"),
    DELETE_SUCCESSFUL("delete successful"),
    MKDIR_FAIL("mkdir fail"),
    MKDIR_SUCCESSFUL("mkdir successful"),
    UNZIP_FAIL("unzip fail"),
    UNZIP_SUCCESSFUL("unzip successful"),
    CHANGE_MODE_FAIL("change mode fail"),
    CHANGE_MODE_SUCCESSFUL("change mode successful"),
    UNAME_FAIL("uname fail"),
    UNAME_SUCCESSFUL("uname  successful");
    private String message;
    BinaryDistributionStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
