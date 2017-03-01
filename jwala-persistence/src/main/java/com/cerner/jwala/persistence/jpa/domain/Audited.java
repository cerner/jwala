package com.cerner.jwala.persistence.jpa.domain;

import java.util.Calendar;

public interface Audited {
    public Calendar getCreateDate();

    public void setCreateDate(Calendar createDate);

    public Calendar getLastUpdateDate();

    public void setLastUpdateDate(Calendar lastUpdateDate);

    public String getCreateBy();

    public void setCreateBy(String createBy);

    public String getUpdateBy();

    public void setUpdateBy(String updateBy);
}
