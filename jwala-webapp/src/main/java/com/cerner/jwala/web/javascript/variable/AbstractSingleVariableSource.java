package com.cerner.jwala.web.javascript.variable;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractSingleVariableSource implements JavaScriptVariableSource {

    @Override
    public Set<JavaScriptVariable> createVariables() {
        final Set<JavaScriptVariable> variable = new HashSet<>(1);
        variable.add(createSingleVariable());
        return variable;
    }

    protected abstract JavaScriptVariable createSingleVariable();
}
