package com.cerner.jwala.common.rule.group;

import org.junit.Test;

import com.cerner.jwala.common.exception.MessageResponseStatus;
import com.cerner.jwala.common.rule.group.GroupNameRule;

import static org.junit.Assert.assertEquals;

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
}
