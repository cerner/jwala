package com.cerner.jwala.common.domain.model.group;

import java.io.Serializable;

import com.cerner.jwala.common.domain.model.id.Identifier;

public class History implements Serializable {

    private final Identifier<History> id;
    private final String history;

    public History(Identifier<History> id, String history) {
        this.id = id;
        this.history = history;
    }

    public Identifier<History> getId() {
        return id;
    }

    public String getHistory() {
        return history;
    }
}
