package com.cerner.jwala.common.exec;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ExecCommandTest {

    @Test
    public void testExecCommandDual(){
        List<String> formatStrings = Arrays.asList("abc\\aaa", "12345\\");
        List<String> unformatStrings = Arrays.asList("xyz\\bbb", "67890\\");
        List<String> result = new ExecCommand(formatStrings, unformatStrings).getCommandFragments();
        List<String> expected = Arrays.asList("abc/aaa", "12345/", "xyz\\bbb", "67890\\");
        assertEquals(result, expected);
    }
}
