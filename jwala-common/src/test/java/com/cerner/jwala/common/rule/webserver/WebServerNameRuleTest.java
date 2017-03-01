package com.cerner.jwala.common.rule.webserver;

import org.junit.Test;

import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.rule.webserver.WebServerNameRule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WebServerNameRuleTest {

    @Test
    public void testValidNames() {
        final String[] validNames = {"abc", "def", "a really long name"};

        for (final String name : validNames) {
            final WebServerNameRule rule = new WebServerNameRule(name);
            assertTrue(rule.isValid());
            rule.validate();
        }
    }

    @Test
    public void testInvalidNames() {
        final String[] invalidNames = {"", "    ", null};

        for (final String name : invalidNames) {
            final WebServerNameRule rule = new WebServerNameRule(name);
            assertFalse(rule.isValid());
            try {
                rule.validate();
            } catch (final BadRequestException bre) {
                assertTrue(true);
            }
        }
    }
}
