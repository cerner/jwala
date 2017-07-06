package com.cerner.jwala.common.request.app;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.request.Request;
import com.cerner.jwala.common.rule.MultipleRules;
import com.cerner.jwala.common.rule.app.ApplicationContextRule;
import com.cerner.jwala.common.rule.app.ApplicationIdRule;
import com.cerner.jwala.common.rule.app.ApplicationNameRule;
import com.cerner.jwala.common.rule.group.GroupIdRule;

import java.io.Serializable;

public class UpdateApplicationRequest implements Serializable, Request {

    private final Identifier<Application> id;
    private final Identifier<Group> newGroupId;
    private final String newWebAppContext;
    private final String newName;
    private final boolean newSecure;
    private final boolean newLoadBalanceAcrossServers;
    private final boolean unpackWar;
    private final String warDeployPath;

    public UpdateApplicationRequest(
            final Identifier<Application> theId,
            final Identifier<Group> theGroupId,
            final String theNewWebAppContext,
            final String theNewName,
            boolean theNewSecure,
            boolean theNewLoadBalanceAcrossServers,
            boolean unpackWar) {
        id = theId;
        newGroupId = theGroupId;
        newName = theNewName;
        newWebAppContext = theNewWebAppContext;
        newSecure = theNewSecure;
        newLoadBalanceAcrossServers = theNewLoadBalanceAcrossServers;
        this.unpackWar = unpackWar;
        this.warDeployPath = null;
    }

    public UpdateApplicationRequest(
            Identifier<Application> id,
            Identifier<Group> newGroupId,
            String newWebAppContext,
            String newName,
            boolean newSecure,
            boolean newLoadBalanceAcrossServers,
            boolean unpackWar,
            String warDeployPath) {

        this.id = id;
        this.newGroupId = newGroupId;
        this.newWebAppContext = newWebAppContext;
        this.newName = newName;
        this.newSecure = newSecure;
        this.newLoadBalanceAcrossServers = newLoadBalanceAcrossServers;
        this.unpackWar = unpackWar;
        this.warDeployPath = warDeployPath;
    }

    public Identifier<Application> getId() {
        return id;
    }

    public Identifier<Group> getNewGroupId() {
        return newGroupId;
    }

    public String getNewWebAppContext() {
        return newWebAppContext;
    }

    public String getNewName() {
        return newName;
    }

    public boolean isNewSecure() {
        return newSecure;
    }

    public boolean isNewLoadBalanceAcrossServers() {
        return newLoadBalanceAcrossServers;
    }

    public String getWarDeployPath() {
        return warDeployPath;
    }

    @Override
    public void validate() {
        new MultipleRules(new ApplicationIdRule(id),
                new GroupIdRule(newGroupId),
                new ApplicationNameRule(newName),
                new ApplicationContextRule(newWebAppContext)).validate();
    }

    public boolean isUnpackWar() {
        return unpackWar;
    }

    @Override
    public String toString() {
        return "UpdateApplicationRequest{" +
                "id=" + id +
                ", newGroupId=" + newGroupId +
                ", newWebAppContext='" + newWebAppContext + '\'' +
                ", newName='" + newName + '\'' +
                ", newSecure=" + newSecure +
                ", newLoadBalanceAcrossServers=" + newLoadBalanceAcrossServers +
                ", unpackWar=" + unpackWar +
                ", warDeployPath=" + warDeployPath +
                '}';
    }
}
