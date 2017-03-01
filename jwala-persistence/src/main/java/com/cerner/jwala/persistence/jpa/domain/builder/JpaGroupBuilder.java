package com.cerner.jwala.persistence.jpa.domain.builder;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.group.History;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.persistence.jpa.domain.JpaGroup;
import com.cerner.jwala.persistence.jpa.domain.JpaHistory;
import com.cerner.jwala.persistence.jpa.domain.JpaJvm;
import com.cerner.jwala.persistence.jpa.domain.JpaWebServer;
import org.joda.time.Chronology;
import org.joda.time.DateTime;

import java.util.HashSet;
import java.util.Set;

public class JpaGroupBuilder {

    public static final Chronology USE_DEFAULT_CHRONOLOGY = null;
    private JpaGroup group;
    private boolean fetchWebServers = false;

    public JpaGroupBuilder() {
    }

    public JpaGroupBuilder(final JpaGroup aGroup) {
        group = aGroup;
    }

    public JpaGroupBuilder setGroup(final JpaGroup aGroup) {
        group = aGroup;
        return this;
    }

    public Group build() {
        if (fetchWebServers) {
            return new Group(new Identifier<Group>(group.getId()),
                    group.getName(),
                    getJvms(),
                    getWebServers(),
                    getHistory());
        }
        return new Group(new Identifier<Group>(group.getId()),
                group.getName(),
                getJvms());
    }

    private DateTime getAsOf() {
        if (group.getStateUpdated() != null) {
            return new DateTime(group.getStateUpdated(),
                    USE_DEFAULT_CHRONOLOGY);
        }

        return null;
    }

    protected Set<Jvm> getJvms() {
        final Set<Jvm> jvms = new HashSet<>();
        if (group.getJvms() != null) {
            final JvmBuilder builder = new JvmBuilder();
            for (final JpaJvm jpaJvm : group.getJvms()) {
                jvms.add(builder.setJpaJvm(jpaJvm).build());
            }
        }

        return jvms;
    }

    protected Set<WebServer> getWebServers() {
        final Set<WebServer> webServers = new HashSet<>();
        if (group.getWebServers() != null) {
            for (final JpaWebServer jpaWebServer : group.getWebServers()) {
                webServers.add(new JpaWebServerBuilder(jpaWebServer).build());
            }
        }

        return webServers;
    }

    protected Set<History> getHistory() {
        final Set<History> history = new HashSet<>();
        if (group.getHistory() != null) {
            for (final JpaHistory jpaHistory : group.getHistory()) {
                history.add(new JpaHistoryBuilder(jpaHistory).build());
            }
        }

        return history;
    }

    public JpaGroupBuilder setFetchWebServers(boolean fetchWebServers) {
        this.fetchWebServers = fetchWebServers;
        return this;
    }
}
