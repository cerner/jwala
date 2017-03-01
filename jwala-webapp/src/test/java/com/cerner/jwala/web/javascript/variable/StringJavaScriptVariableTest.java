package com.cerner.jwala.web.javascript.variable;

import org.junit.Test;

import com.cerner.jwala.web.javascript.variable.StringJavaScriptVariable;

import static org.junit.Assert.assertEquals;

public class StringJavaScriptVariableTest {

    @Test
    public void testGetVariableValue() throws Exception {
        final String variableName = "theVariableName";
        final String variableValue = "theVariableValue";
        final String expectedValue = "\"" + variableValue + "\"";
        final StringJavaScriptVariable variable = new StringJavaScriptVariable(variableName,
                                                                               variableValue);

        assertEquals(variableName,
                     variable.getVariableName());
        assertEquals(expectedValue,
                     variable.getVariableValue());
    }
}
