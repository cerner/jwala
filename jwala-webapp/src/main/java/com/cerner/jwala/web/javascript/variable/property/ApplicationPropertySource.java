package com.cerner.jwala.web.javascript.variable.property;

import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.web.javascript.variable.JavaScriptVariable;
import com.cerner.jwala.web.javascript.variable.JavaScriptVariableSource;

import java.util.HashSet;
import java.util.Set;

public class ApplicationPropertySource implements JavaScriptVariableSource {

    private final ApplicationProperties source;

    public ApplicationPropertySource(final ApplicationProperties theSource) {
        source = theSource;
    }

    @Override
    public Set<JavaScriptVariable> createVariables() {
        final Set<JavaScriptVariable> variables = new HashSet<>();
        for (final ApplicationPropertySourceDefinition definition : ApplicationPropertySourceDefinition.values()) {
            variables.add(definition.toVariable(source));
        }
        return variables;
    }
}
