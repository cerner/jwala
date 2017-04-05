package com.cerner.jwala.common.rule;

import org.junit.Test;

import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.rule.StatusPathRule;

import static org.junit.Assert.*;

/**
 * Created by Jedd Cuison on 12/8/14.
 *
 * Tests for {@link com.cerner.jwala.common.rule.StatusPathRule}
 */
public class StatusPathRuleTest {

    @Test
    public void testValidStatusPathValues() {
        final String[] validValues = {"/abc", "/def", "/AReallyLongStatusPathGoesHere", "https://somehost:443/apache_pb.png"};

        for (final String val : validValues) {
            final StatusPathRule rule = new StatusPathRule(new Path(val));
            assertTrue(rule.isValid());
            rule.validate();
        }
    }

    @Test
    public void testInvalidHostNames() {
        final String[] invalidValues = {"", "      ", "uri_with_underscore", "uri with spaces", "uriwith@#$%^&*",
                                        "httpsx://somehost:443/apache_pb.png"};

        for (final String val : invalidValues) {
            final StatusPathRule rule = new StatusPathRule(new Path(val));
            assertFalse(rule.isValid());
            try {
                rule.validate();
                fail("Rule should not have validated");
            } catch(final BadRequestException bre) {
                assertTrue(true);
            }
        }
    }

}