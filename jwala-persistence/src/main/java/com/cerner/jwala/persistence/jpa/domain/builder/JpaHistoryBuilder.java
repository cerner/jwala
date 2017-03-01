package com.cerner.jwala.persistence.jpa.domain.builder;

import com.cerner.jwala.common.domain.model.group.History;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.persistence.jpa.domain.JpaHistory;

public class JpaHistoryBuilder {

    private JpaHistory jpaHistory;

    public JpaHistoryBuilder(JpaHistory jpaHistory) {
        this.jpaHistory = jpaHistory;
    }

    public History build() {
        return new History(new Identifier<History>(jpaHistory.getId()), jpaHistory.getEvent());
    }
}
