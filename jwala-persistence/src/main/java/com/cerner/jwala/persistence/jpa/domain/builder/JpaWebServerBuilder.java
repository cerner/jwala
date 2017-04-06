package com.cerner.jwala.persistence.jpa.domain.builder;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.persistence.jpa.domain.JpaGroup;
import com.cerner.jwala.persistence.jpa.domain.JpaWebServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JpaWebServerBuilder {

    private JpaWebServer webServer;

    public JpaWebServerBuilder() {
    }

    public JpaWebServerBuilder(final JpaWebServer aWebServer) {
        webServer = aWebServer;
    }

    public WebServer build() {
        List<JpaGroup> jpaGroups = webServer.getGroups();
        List<Group> groups;
        if (jpaGroups != null) {
            groups = new ArrayList<>(webServer.getGroups().size());
            for (JpaGroup gid : webServer.getGroups()) {
                groups.add(new JpaGroupBuilder(gid).build());
            }
        } else {
            groups = Collections.emptyList();
        }
        return new WebServer(new Identifier<WebServer>(webServer.getId()),
                groups,
                webServer.getName(),
                webServer.getHost(),
                webServer.getPort(),
                webServer.getHttpsPort(),
                new Path(webServer.getStatusPath()),
                webServer.getState());
    }

}