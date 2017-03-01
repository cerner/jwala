package com.cerner.jwala.common.rule.app;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.rule.Rule;
import com.cerner.jwala.common.rule.identifier.IdentifierRule;

public class ApplicationIdRule extends IdentifierRule<Application> implements Rule {

    public ApplicationIdRule(final Identifier<Application> theId) {
        super(theId,
              FaultType.APPLICATION_NOT_SPECIFIED,
              "Application Id was not specified");
    }

}
