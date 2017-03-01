package com.cerner.jwala.web.javascript.variable;

import java.util.*;

public class CompositeJavaScriptVariableSource implements JavaScriptVariableSource {

    private final List<JavaScriptVariableSource> sources;

    public CompositeJavaScriptVariableSource(final JavaScriptVariableSource... theSources) {
        sources = new ArrayList<>(Arrays.asList(theSources));
    }

    @Override
    public Set<JavaScriptVariable> createVariables() {
        final Set<JavaScriptVariable> variables = new HashSet<>();

        for (final JavaScriptVariableSource source : sources) {
            variables.addAll(source.createVariables());
        }

        return variables;
    }
}
