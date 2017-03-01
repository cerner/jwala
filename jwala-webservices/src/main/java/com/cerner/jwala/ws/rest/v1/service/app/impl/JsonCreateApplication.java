package com.cerner.jwala.ws.rest.v1.service.app.impl;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.request.app.CreateApplicationRequest;

public class JsonCreateApplication {

    private String name;
    private Long groupId;
    private String webappContext;
    private boolean secure;
    private boolean loadBalanceAcrossServers;
    private boolean unpackWar;

    public JsonCreateApplication() {
    }

    public JsonCreateApplication(Long groupId2, String name2, String webappContext2, boolean secure, boolean loadBalanceAcrossServers, boolean unpackWar) {
        groupId = groupId2;
        name = name2;
        webappContext = webappContext2;
        this.secure = secure;
        this.loadBalanceAcrossServers = loadBalanceAcrossServers;
        this.unpackWar = unpackWar;
    }

    public CreateApplicationRequest toCreateCommand() {
        return new CreateApplicationRequest(
                Identifier.id(groupId, Group.class), name, webappContext, secure, loadBalanceAcrossServers, unpackWar);
    }


    @Override
    public Object clone() {
        return new JsonCreateApplication(
                getGroupId(),
                getName(),
                getWebappContext(),
                isSecure(),
                isLoadBalanceAcrossServers(),
                isUnpackWar());
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getGroupId())
                .append(getName())
                .append(getWebappContext())
                .append(isSecure())
                .append(isLoadBalanceAcrossServers())
                .append(isUnpackWar()).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
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
    
    /* test code:
     * assertEquals(testJca,testJca.clone())
     * assertEquals(testJca.hashCode(),testJca.clone().hashCode())
     */

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

    public void setUnpackWar(boolean unpack) {
        this.unpackWar = unpack;
    }

    public boolean isUnpackWar() {
        return unpackWar;
    }

    @Override
    public String toString() {
        return "JsonCreateApplication{" +
                "name='" + name + '\'' +
                ", groupId=" + groupId +
                ", webappContext='" + webappContext + '\'' +
                ", secure=" + secure +
                ", loadBalanceAcrossServers=" + loadBalanceAcrossServers +
                ", unpackWar=" + unpackWar +
                '}';
    }
}
