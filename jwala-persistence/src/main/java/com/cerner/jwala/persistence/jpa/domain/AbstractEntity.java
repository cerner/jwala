package com.cerner.jwala.persistence.jpa.domain;

import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.*;
import java.util.Calendar;

@MappedSuperclass
public abstract class AbstractEntity<T extends AbstractEntity<T>> implements Audited {

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "createDate")
    public Calendar createDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "lastUpdateDate")
    public Calendar lastUpdateDate;

    @Column(name = "createBy")
    public String createBy;

    @Column(name = "updateBy")
    public String updateBy;

    @Override
    public Calendar getCreateDate() {
        return createDate;
    }

    @Override
    public void setCreateDate(final Calendar createDate) {
        this.createDate = createDate;
    }

    @Override
    public Calendar getLastUpdateDate() {
        return lastUpdateDate;
    }

    @Override
    public void setLastUpdateDate(final Calendar lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    @Override
    public String getCreateBy() {
        return createBy;
    }

    @Override
    public void setCreateBy(final String createBy) {
        this.createBy = createBy;
    }

    @Override
    public String getUpdateBy() {
        return updateBy;
    }

    @Override
    public void setUpdateBy(final String updateBy) {
        this.updateBy = updateBy;
    }

    public abstract Long getId();

    @PrePersist
    protected void prePersist() {
        final Calendar now = Calendar.getInstance();
        setCreateDate(now);
        setCreateBy(getUserId(createBy));
        setLastUpdateDate(now);
    }

    @PreUpdate
    private void preUpdate() {
        final Calendar now = Calendar.getInstance();
        setLastUpdateDate(now);
        setUpdateBy(getUserId(updateBy));
    }

    /**
     * Gets the user name/ID from thread local
     * @param providedUserId the user id provided by through createBy or updateBy
     * @return the user id
     */
    private String getUserId(final String providedUserId) {
        if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null) {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        }
        return providedUserId;
    }

}
