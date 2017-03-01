package com.cerner.jwala.common.rule;

import org.junit.Test;

import com.cerner.jwala.common.rule.HostNameRule;

import static org.junit.Assert.assertEquals;

public class HostNameRuleTest {
    HostNameRule hnrOne = new HostNameRule("Name");

    @Test
    public void testGetMessageResponseStatus() {
        assertEquals("InvalidHostName", hnrOne.getMessageResponseStatus().getMessage());
    }

    @Test
    public void testGetMessage() {
        assertEquals("Invalid Host Name : \"Name\"", hnrOne.getMessage());
    }
}
