package com.cerner.jwala.common.domain.model.balancermanager;

public enum WorkerStatusType
{
    IGNORE_ERRORS("<input name='w_status_I' id='w_status_I' value='1' type=radio> <br/>"),
    DRAINING_MODE("<input name='w_status_N' id='w_status_N' value='1' type=radio> <br/>"),
    DISABLED("<input name='w_status_D' id='w_status_D' value='1' type=radio> <br/>"),
    HOT_STANDBY("<input name='w_status_H' id='w_status_H' value='1' type=radio> <br/>");

    private String value;

    WorkerStatusType(final String value) {
        this.value = value;
    }

    public String getValue(){
        return value;
    }

    @Override
    public String toString(){
        return this.getValue();
    }
}
