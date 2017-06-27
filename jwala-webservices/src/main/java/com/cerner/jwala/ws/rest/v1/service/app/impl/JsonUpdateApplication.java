package com.cerner.jwala.ws.rest.v1.service.app.impl;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.request.app.UpdateApplicationRequest;

public class JsonUpdateApplication {

    private boolean unpackWar;
    private Long webappId;
    private String name;
    private Long groupId;
    private String webappContext;
    private boolean secure;
    private boolean loadBalanceAcrossServers;
    private String warDeployPath;

    public JsonUpdateApplication() {
    }

    public JsonUpdateApplication(final Long groupId, String name,
                                 final String webappContext,
                                 final Long webappId,
                                 final boolean secure,
                                 final boolean loadBalanceAcrossServers,
                                 final boolean unpackWar,
                                 final String warDeployPath) {
        this.groupId = groupId;
        this.webappId = webappId;
        this.name = name;
        this.webappContext = webappContext;
        this.secure = secure;
        this.loadBalanceAcrossServers = loadBalanceAcrossServers;
        this.unpackWar = unpackWar;
        this.warDeployPath = warDeployPath;
    }

    public UpdateApplicationRequest toUpdateCommand() {
        return new UpdateApplicationRequest(
                Identifier.id(webappId, Application.class),
                Identifier.id(groupId, Group.class), webappContext, name, secure, loadBalanceAcrossServers, unpackWar, warDeployPath);
    }

    public Long getWebappId() {
        return webappId;
    }

    public void setWebappId(Long webappId) {
        this.webappId = webappId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getWebappContext() {
        return webappContext;
    }

    public void setWebappContext(String webappContext) {
        this.webappContext = webappContext;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public boolean isLoadBalanceAcrossServers() {
        return loadBalanceAcrossServers;
    }

    public void setLoadBalanceAcrossServers(boolean loadBalanceAcrossServers) {
        this.loadBalanceAcrossServers = loadBalanceAcrossServers;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getGroupId())
                .append(getName())
                .append(getWebappContext())
                .append(getWebappId())
                .append(isSecure())
                .append(isLoadBalanceAcrossServers())
                .append(isUnpackWar()).toHashCode();
    }

    @Override
    public String toString() {
        return "JsonUpdateApplication{" +
                "unpackWar=" + unpackWar +
                ", webappId=" + webappId +
                ", name='" + name + '\'' +
                ", groupId=" + groupId +
                ", webappContext='" + webappContext + '\'' +
                ", secure=" + secure +
                ", loadBalanceAcrossServers=" + loadBalanceAcrossServers +
                '}';
    }

    @Override
    public Object clone() {
        return new JsonUpdateApplication(
                getGroupId(),
                getName(),
                getWebappContext(),
                getWebappId(),
                isSecure(),
                isLoadBalanceAcrossServers(),
                isUnpackWar(),
                getWarDeployPath());
    }

    @Override
    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }

    public void setUnpackWar(boolean unpack) {
        this.unpackWar = unpack;
    }

    public boolean isUnpackWar() {
        return unpackWar;
    }

    public String getWarDeployPath() {
        return warDeployPath;
    }

    public void setWarDeployPath(String warDeployPath) {
        this.warDeployPath = warDeployPath;
    }
    
}