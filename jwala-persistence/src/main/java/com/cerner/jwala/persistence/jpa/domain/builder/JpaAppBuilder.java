package com.cerner.jwala.persistence.jpa.domain.builder;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.persistence.jpa.domain.JpaApplication;

public class JpaAppBuilder {

    public static Application appFrom(JpaApplication jpaApp) {

        return new Application(
                Identifier.<Application>id(jpaApp.getId()),
                jpaApp.getName(), 
                jpaApp.getWarPath(), 
                jpaApp.getWebAppContext(), 
                jpaApp.getGroup() != null ? new JpaGroupBuilder(jpaApp.getGroup()).build() : null,
                jpaApp.isSecure(),
                jpaApp.isLoadBalanceAcrossServers(),
                jpaApp.isUnpackWar(),
                jpaApp.getWarName());

    }
}
