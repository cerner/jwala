package com.cerner.jwala.common;

import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.rule.SpecialCharactersRule;
import com.cerner.jwala.common.rule.group.GroupNameRule;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by SB053052 on 4/7/2017.
 */
public class SpecialCharactersRuleTest {

    @Test
    public void testValidNames() {
        final String[] validNames = {"abc", "def", "123", "group123", "group_123."};

        for (final String name : validNames) {
            final SpecialCharactersRule rule = new SpecialCharactersRule(name);
            assertTrue(rule.isValid());
            rule.validate();
        }
    }

    @Test
    public void testInvalidNames() {
        final String[] invalidNames = {"$group*", "\\group", "/group", "  myGroup", "myGroup ", " myGroup   ", "ws:"};

        for (final String name : invalidNames) {
            final SpecialCharactersRule rule = new SpecialCharactersRule(name);
            assertFalse(rule.isValid());
            try {
                rule.validate();
            } catch (final BadRequestException bre) {
                assertTrue(true);
            }
        }
    }

}
