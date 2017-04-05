package com.cerner.jwala.common.rule.group;

import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.rule.ValidNameRule;
import junit.framework.TestCase;
import org.junit.Test;

import com.cerner.jwala.common.exception.MessageResponseStatus;
import com.cerner.jwala.common.rule.group.GroupNameRule;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GroupNameRuleTest {
    private final GroupNameRule rule = new GroupNameRule("");

    @Test
    public void testGetMessageResponseStatus() {
        final MessageResponseStatus messageResponseStatus = rule.getMessageResponseStatus();
        assertEquals("InvalidGroupName", messageResponseStatus.getMessage());
    }

    @Test
    public void testGetMessage() {
        final String message = rule.getMessage();
        assertEquals("Invalid Group Name: \"\"", message);
    }

    @Test
    public void testValidNames() {
        final String[] validNames = {"abc", "def", "123","group123","group_123."};

        for (final String name : validNames) {
            final GroupNameRule rule = new GroupNameRule(name);
            assertTrue(rule.isValid());
            rule.validate();
        }
    }

    @Test
    public void testInvalidNames() {
        final String[] invalidNames = {"", "    ", null,"$group*","\\group","/group","  myGroup","myGroup "," myGroup   "};

        for (final String name : invalidNames) {
            final GroupNameRule rule = new GroupNameRule(name);
            assertFalse(rule.isValid());
            try {
                rule.validate();
            } catch (final BadRequestException bre) {
                assertTrue(true);
            }
        }
    }

    @Test(expected=BadRequestException.class)
    public void testLongGroupName(){
        String veryLongString="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        GroupNameRule rule=new GroupNameRule(veryLongString);
            rule.validate();
    }

}
