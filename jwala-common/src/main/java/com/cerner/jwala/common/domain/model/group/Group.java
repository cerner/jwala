package com.cerner.jwala.common.domain.model.group;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.DateTime;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Group {

    private final Identifier<Group> id;
    private final String name;
    private final Set<Jvm> jvms;
    private final Set<WebServer> webServers;
    private final Set<History> history;
    private final Set<Application> applications;

    public Group(final Identifier<Group> theId,
                 final String theName) {
        this(theId, theName, Collections.<Jvm> emptySet());
    }

    public Group(final Identifier<Group> theId,
                 final String theName,
                 final Set<Jvm> theJvms) {
        id = theId;
        name = theName;
        jvms = Collections.unmodifiableSet(new HashSet<>(theJvms));
        webServers = null;
        history = null;
        applications = null;
    }

    public Group(final Identifier<Group> theId,
                 final String theName,
                 final Set<Jvm> theJvms,
                 final Set<WebServer> theWebServers,
                 final Set<History> theHistory) {
        id = theId;
        name = theName;
        jvms = Collections.unmodifiableSet(new HashSet<>(theJvms));
        webServers = Collections.unmodifiableSet(new HashSet<>(theWebServers));
        history = theHistory;
        applications = null;
    }

    public Group(Identifier<Group> id, String name, Set<Jvm> jvms, Set<WebServer> webServers, Set<History> history, Set<Application> applications) {
        this.id = id;
        this.name = name;
        this.jvms = jvms;
        this.webServers = webServers;
        this.history = history;
        this.applications = applications;
    }

    public Identifier<Group> getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<Jvm> getJvms() {
        return jvms;
    }

    public Set<WebServer> getWebServers() {
        return webServers;
    }

    public Set<History> getHistory() {
        return history;
    }

    public Set<Application> getApplications() {
        return applications;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        Group rhs = (Group) obj;
        return new EqualsBuilder()
                .append(this.id, rhs.id)
                .append(this.name, rhs.name)
                .append(this.jvms, rhs.jvms)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(name)
                .append(jvms)
                .append(history)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "Group{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", jvms=" + (jvms != null ? jvms.size() : 0) +
                ", webServers=" + (webServers  != null ? webServers.size() : 0)+
                ", history=" + history +
                ", applications=" + (applications != null ? applications.size() : 0)+
                '}';
    }
}
