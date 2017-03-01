package com.cerner.jwala.common.rule;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.exception.MessageResponseStatus;

/**
 * Created by Jeffery Mahmood on 8/20/2015.
 */
public class ValidTemplateNameRule extends ValidNameRule {

    public ValidTemplateNameRule(final String theName) {
        super(theName);
    }

    @Override
    public boolean isValid() {
        return super.isValid() && name.endsWith(".tpl");
    }

    @Override
    protected MessageResponseStatus getMessageResponseStatus() {
        return FaultType.INVALID_TEMPLATE_NAME;
    }

    @Override
    protected String getMessage() {
        return "Not a valid template filename. Must end in .tpl";
    }
}
