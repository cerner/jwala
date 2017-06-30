package com.cerner.jwala.common.domain.model.app;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;

import java.io.Serializable;
import java.util.List;

public class Application implements Serializable {

    private Identifier<Application> id;

    private Group group;

    private String webAppContext;

    private String name;

    private String warPath;

    private boolean secure;

    private boolean loadBalanceAcrossServers;

    private boolean unpackWar;

    private String warName;

    private String warDeployPath;

    private List<Jvm> jvms;

    private Jvm parentJvm;

    public Application(final Identifier<Application> anId,
                       final String aName,
                       final String aWarPath,
                       final String aWebAppContext,
                       final Group aGroup,
                       final boolean secure,
                       final boolean loadBalanceAcrossServers,
                       final boolean unpackWar,
                       final String warName,
                       final String warDeployPath) {
        group = aGroup;
        id = anId;
        webAppContext = aWebAppContext;
        warPath = aWarPath;
        name = aName;
        this.secure = secure;
        this.loadBalanceAcrossServers = loadBalanceAcrossServers;
        this.unpackWar = unpackWar;
        this.warName = warName;
        this.warDeployPath = warDeployPath;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getWebAppContext() {
        return webAppContext;
    }

    public void setWebAppContext(String webAppContext) {
        this.webAppContext = webAppContext;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWarPath() {
        return warPath;
    }

    public void setWarPath(String warPath) {
        this.warPath = warPath;
    }

    public Identifier<Application> getId() {
        return id;
    }

    public void setId(Identifier<Application> id) {
        this.id = id;
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

    public String getWarName() {
        return warName;
    }

    public void setWarName(String warName) {
        this.warName = warName;
    }

    public boolean isUnpackWar() {
        return unpackWar;
    }

    public void setUnpackWar(boolean unpackWar) {
        this.unpackWar = unpackWar;
    }

    public List<Jvm> getJvms() {
        return jvms;
    }

    public void setJvms(List<Jvm> jvms) {
        this.jvms = jvms;
    }

    public Jvm getParentJvm() {
        return parentJvm;
    }

    public void setParentJvm(Jvm parentJvm) {
        this.parentJvm = parentJvm;
    }

    public String getWarDeployPath() {
        return warDeployPath;
    }

    public void setWarDeployPath(String warDeployPath) {
        this.warDeployPath = warDeployPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Application that = (Application) o;

        return new EqualsBuilder()
                .append(secure, that.secure)
                .append(loadBalanceAcrossServers, that.loadBalanceAcrossServers)
                .append(unpackWar, that.unpackWar)
                .append(id, that.id)
                .append(group, that.group)
                .append(webAppContext, that.webAppContext)
                .append(name, that.name)
                .append(warPath, that.warPath)
                .append(warName, that.warName)
                .append(jvms, that.jvms)
                .append(parentJvm, that.parentJvm)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(group)
                .append(webAppContext)
                .append(name)
                .append(warPath)
                .append(secure)
                .append(loadBalanceAcrossServers)
                .append(unpackWar)
                .append(warName)
                .append(jvms)
                .append(parentJvm)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "Application{" +
                "id=" + id +
                ", group=" + group +
                ", webAppContext='" + webAppContext + '\'' +
                ", name='" + name + '\'' +
                ", warPath='" + warPath + '\'' +
                ", secure=" + secure +
                ", loadBalanceAcrossServers=" + loadBalanceAcrossServers +
                ", unpackWar=" + unpackWar +
                ", warName='" + warName + '\'' +
                ", warDeployPath='" + warDeployPath + '\'' +
                ", jvms=" + jvms +
                ", parentJvm=" + parentJvm +
                '}';
    }

}
