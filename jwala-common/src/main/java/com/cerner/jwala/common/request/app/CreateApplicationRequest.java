package com.cerner.jwala.common.request.app;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.request.Request;
import com.cerner.jwala.common.rule.MultipleRules;
import com.cerner.jwala.common.rule.app.ApplicationContextRule;
import com.cerner.jwala.common.rule.app.ApplicationNameRule;
import com.cerner.jwala.common.rule.group.GroupIdRule;

import java.io.Serializable;

public class CreateApplicationRequest implements Serializable, Request {

    private String name;
    private String webAppContext;
    private Identifier<Group> groupId;
    private final boolean unpackWar;
    private boolean secure;
    private boolean loadBalanceAcrossServers;
    
    public CreateApplicationRequest(Identifier<Group> groupId,
                                    String name,
                                    String webAppContext,
                                    boolean secure,
                                    boolean loadBalanceAcrossServers,
                                    boolean unpackWar) {
        this.name = name;
        this.webAppContext = webAppContext;
        this.groupId = groupId;
        this.secure = secure;
        this.loadBalanceAcrossServers = loadBalanceAcrossServers;
        this.unpackWar = unpackWar;
    }

    public Identifier<Group> getGroupId() {
        return groupId;
    }

    public String getName() {
        return name;
    }
    public String getWebAppContext() {
        return webAppContext;
    }

    public boolean isSecure() {
        return secure;
    }

    public boolean isLoadBalanceAcrossServers() {
        return loadBalanceAcrossServers;
    }

    @Override
    public void validate() {
        new MultipleRules(new GroupIdRule(groupId),
                                new ApplicationNameRule(name),
                                new ApplicationContextRule(webAppContext)).validate();
    }

    public boolean isUnpackWar() {
        return unpackWar;
    }

    @Override
    public String toString() {
        return "CreateApplicationRequest{" +
                "name='" + name + '\'' +
                ", webAppContext='" + webAppContext + '\'' +
                ", groupId=" + groupId +
                ", unpackWar=" + unpackWar +
                ", secure=" + secure +
                ", loadBalanceAcrossServers=" + loadBalanceAcrossServers +
                '}';
    }
}
