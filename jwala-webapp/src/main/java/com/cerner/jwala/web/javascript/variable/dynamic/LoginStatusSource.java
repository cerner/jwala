package com.cerner.jwala.web.javascript.variable.dynamic;

import javax.servlet.http.HttpServletRequest;

import com.cerner.jwala.web.javascript.variable.AbstractSingleVariableSource;
import com.cerner.jwala.web.javascript.variable.JavaScriptVariable;
import com.cerner.jwala.web.javascript.variable.JavaScriptVariableSource;
import com.cerner.jwala.web.javascript.variable.StringJavaScriptVariable;

public class LoginStatusSource extends AbstractSingleVariableSource implements JavaScriptVariableSource {

    private final HttpServletRequest request;

    public LoginStatusSource(final HttpServletRequest theRequest) {
        request = theRequest;
    }

    @Override
    protected JavaScriptVariable createSingleVariable() {
        return new StringJavaScriptVariable("loginStatus",
                                            String.valueOf(request.getParameter("status")));
    }
}
