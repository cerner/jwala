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

    public JsonUpdateApplication() {
    }

    public JsonUpdateApplication(Long groupId, String name,
                                 String webappContext,
                                 Long webappId,
                                 boolean secure,
                                 boolean loadBalanceAcrossServers,
                                 boolean unpackWar) {
        this.groupId = groupId;
        this.webappId = webappId;
        this.name = name;
        this.webappContext = webappContext;
        this.secure = secure;
        this.loadBalanceAcrossServers = loadBalanceAcrossServers;
        this.unpackWar = unpackWar;
    }

    public UpdateApplicationRequest toUpdateCommand() {
        return new UpdateApplicationRequest(
                Identifier.id(webappId, Application.class),
                Identifier.id(groupId, Group.class), webappContext, name, secure, loadBalanceAcrossServers, unpackWar);
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
                isLoadBalanceAcrossServers(), isUnpackWar());
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
    
    /* test code:
     * assertEquals(testJua,testJua.clone())
     * assertEquals(testJua.hashCode(),testJua.clone().hashCode())
     */

}