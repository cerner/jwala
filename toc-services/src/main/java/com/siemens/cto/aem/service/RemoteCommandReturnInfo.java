package com.siemens.cto.aem.service;

/**
 * Wrapper that contains information on a remote command execution.
 *
 * Created by JC043760 on 3/25/2016.
 */
public class RemoteCommandReturnInfo {

    public final int retCode;
    public final String standardOuput;
    public final String errorOupout;

    public RemoteCommandReturnInfo(final int retCode, final String standardOuput, final String errorOupout) {
        this.retCode = retCode;
        this.standardOuput = standardOuput;
        this.errorOupout = errorOupout;
    }

    @Override
    public String toString() {
        return "RemoteCommandReturnInfo{" +
                "retCode=" + retCode +
                ", standardOuput='" + standardOuput + '\'' +
                ", errorOupout='" + errorOupout + '\'' +
                '}';
    }

}