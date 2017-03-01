package com.cerner.jwala.common.rule.webserver;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.rule.AbstractIdRuleTest;
import com.cerner.jwala.common.rule.Rule;
import com.cerner.jwala.common.rule.webserver.WebServerIdRule;

public class WebServerIdRuleTest extends AbstractIdRuleTest {

    @Override
    protected Rule createValidRule() {
        return new WebServerIdRule(new Identifier<WebServer>(1L));
    }

    @Override
    protected Rule createInvalidRule() {
        return new WebServerIdRule(null);
    }
}
