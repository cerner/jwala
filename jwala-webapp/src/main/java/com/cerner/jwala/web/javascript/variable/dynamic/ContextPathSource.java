package com.cerner.jwala.web.javascript.variable.dynamic;

import javax.servlet.ServletContext;

import com.cerner.jwala.web.javascript.variable.AbstractSingleVariableSource;
import com.cerner.jwala.web.javascript.variable.JavaScriptVariable;
import com.cerner.jwala.web.javascript.variable.JavaScriptVariableSource;
import com.cerner.jwala.web.javascript.variable.StringJavaScriptVariable;

public class ContextPathSource extends AbstractSingleVariableSource implements JavaScriptVariableSource {

    private final ServletContext servletContext;

    public ContextPathSource(final ServletContext theServletContext) {
        servletContext = theServletContext;
    }

    @Override
    protected JavaScriptVariable createSingleVariable() {
        return new StringJavaScriptVariable("contextPath",
                                            servletContext.getContextPath());
    }
}
