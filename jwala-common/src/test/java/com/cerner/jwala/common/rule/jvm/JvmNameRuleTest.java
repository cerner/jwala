package com.cerner.jwala.common.rule.jvm;

import org.junit.Test;

import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.rule.jvm.JvmNameRule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JvmNameRuleTest {

    @Test
    public void testValidNames() {
        final String[] validNames = {"abc", "def", "_-", "123j .","64MycharacterNameIsValidNameShouldPassTest1234567891011213141516"};

        for (final String name : validNames) {
            final JvmNameRule rule = new JvmNameRule(name);
            assertTrue(rule.isValid());
            rule.validate();
        }
    }

    @Test
    public void testInvalidNames() {
        final String[] invalidNames = {"", "    ", null,"65charactersNameHereCheckforInvalidity123456789101112131415161718"};

        for (final String name : invalidNames) {
            final JvmNameRule rule = new JvmNameRule(name);
            assertFalse(rule.isValid());
            try {
                rule.validate();
            } catch (final BadRequestException bre) {
                assertTrue(true);
            }
        }
    }
}
