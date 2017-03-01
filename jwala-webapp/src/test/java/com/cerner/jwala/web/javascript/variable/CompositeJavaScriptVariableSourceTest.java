package com.cerner.jwala.web.javascript.variable;

import org.junit.Test;

import com.cerner.jwala.web.javascript.variable.CompositeJavaScriptVariableSource;
import com.cerner.jwala.web.javascript.variable.JavaScriptVariable;
import com.cerner.jwala.web.javascript.variable.JavaScriptVariableSource;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CompositeJavaScriptVariableSourceTest {

    @Test
    public void testCreateVariables() throws Exception {

        final JavaScriptVariableSource[] sources = mockSources(5);

        final CompositeJavaScriptVariableSource compositeSource = new CompositeJavaScriptVariableSource(sources);

        final Set<JavaScriptVariable> actualVariables = compositeSource.createVariables();

        assertEquals(sources.length,
                     actualVariables.size());
        for (final JavaScriptVariable variable : actualVariables) {
            assertTrue(variable.getVariableName().startsWith("key"));
            assertTrue(variable.getVariableValue().startsWith("value"));
        }
    }

    private JavaScriptVariableSource[] mockSources(final int aNumber) {
        final JavaScriptVariableSource[] sources = new JavaScriptVariableSource[aNumber];
        for (int i = 0; i < aNumber; i++) {
            final JavaScriptVariableSource source = mock(JavaScriptVariableSource.class);
            when(source.createVariables()).thenReturn(mockSourceSet(i + 1));
            sources[i] = source;
        }
        return sources;
    }

    private Set<JavaScriptVariable> mockSourceSet(final int anIndex) {
        final Set<JavaScriptVariable> variables = new HashSet<>(1);
        final JavaScriptVariable variable = new JavaScriptVariable("key" + String.valueOf(anIndex),
                                                                   "value" + String .valueOf(anIndex));
        variables.add(variable);
        return variables;
    }
}
