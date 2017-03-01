package com.cerner.jwala.web.javascript.variable;

public class StringJavaScriptVariable extends JavaScriptVariable {

    private static final String QUOTE = "\"";

    public StringJavaScriptVariable(final String theVariableName,
                                    final String theVariableValue) {
        super(theVariableName,
              theVariableValue);
    }

    @Override
    public String getVariableValue() {
        return quote(super.getVariableValue());
    }

    private String quote(final String aValue) {
        return QUOTE + aValue + QUOTE;
    }
}
