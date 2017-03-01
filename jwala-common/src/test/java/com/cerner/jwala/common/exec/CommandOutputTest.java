package com.cerner.jwala.common.exec;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by Jeffery Mahmood on 6/19/2015.
 */
public class CommandOutputTest {
    private static final String STANDARD_OUTPUT_WITH_SPECIAL_CHARS =
            "Last login: Fri Jun 19 13:29:03 2015 from test-server\n" +
            "\u001B]0;~\u0007\n" +
            "\u001B[32mN9SFTomcatAdmin@someHost3773 \u001B[33m~\u001B[0m\n";
    private static final String STANDARD_OUTPUT_WITH_SPECIAL_CHARS_REMOVED =
            "Last login: Fri Jun 19 13:29:03 2015 from test-server\n" +
                    "\n" +
                    "N9SFTomcatAdmin@someHost3773 ~\n";
    private static final String STANDARD_OUTPUT_WITH_SHELL_INFO="`/usr/bin/cygpath d:/jwala/cerner/lib/scripts/start-service.sh` \"SOMEHOST-HEALTH-CHECK-4.0-someHost3773-1XD\" 120 \n" +
            "\n" +
            "exit\n" +
            "\n" +
            "Last login: Wed Jun 24 18:05:06 2015 from test-server\n" +
            "\u001B]0;\u0007~\n" +
            "\u001B[32mN9SFTomcatAdmin@someHost3773 \u001B[33m~\u001B[0m\n" +
            "$ `/usr/bin/cygpath d:/jwala/cerner/lib/scripts/start-service.sh` \"SOMEHOST- HEALTH-CHECK-4.0-someHost3773-1XD\" 120 \n" +
            "Service SOMEHOST-HEALTH-CHECK-4.0-someHost3773-1XD not installed on server\n" +
            "[SC] EnumQueryServicesStatus:OpenService FAILED 1060:\n" +
            "\n" +
            "The specified service does not exist as an installed service.\n" +
            "\n" +
            "\u001B]0;~\u0007\n" +
            "\u001B[32mN9SFTomcatAdmin@someHost3773 \u001B[33m~\u001B[0m\n" +
            "$ \n" +
            "\u001B]0;~\u0007\n" +
            "\u001B[32mN9SFTomcatAdmin@someHost3773 \u001B[33m~\u001B[0m\n" +
            "$ exit\n" +
            "logout\n";
    private static final String STANDARD_OUTPUT_WITH_SHELL_INFO_REMOVED ="Service SOMEHOST-HEALTH-CHECK-4.0-someHost3773-1XD not installed on server\n" +
            "[SC] EnumQueryServicesStatus:OpenService FAILED 1060:\n" +
            "\n" +
            "The specified service does not exist as an installed service.\n";

    @Test
    public void testCleanStandardOutput(){
        CommandOutput testObject = new CommandOutput(new ExecReturnCode(0), STANDARD_OUTPUT_WITH_SPECIAL_CHARS,"");
        testObject.cleanStandardOutput();
        assertEquals(STANDARD_OUTPUT_WITH_SPECIAL_CHARS_REMOVED, testObject.getStandardOutput());
    }

    @Test
    public void testCommandOutputEquals(){
        CommandOutput testObject1 = new CommandOutput(new ExecReturnCode(0), STANDARD_OUTPUT_WITH_SPECIAL_CHARS,"");
        CommandOutput testObject2 = new CommandOutput(new ExecReturnCode(0), STANDARD_OUTPUT_WITH_SPECIAL_CHARS,"");
        assertEquals(testObject1, testObject2);
    }

    @Test
    public void testCommandOutputNotEquals(){
        CommandOutput testObject1 = new CommandOutput(new ExecReturnCode(0), STANDARD_OUTPUT_WITH_SPECIAL_CHARS,"");
        CommandOutput testObject2 = new CommandOutput(new ExecReturnCode(0), STANDARD_OUTPUT_WITH_SHELL_INFO_REMOVED,"");
        assertNotEquals(testObject1, testObject2);
    }

    @Test
    public void testCommandOutputHashcode(){
        CommandOutput testObject1 = new CommandOutput(new ExecReturnCode(0), STANDARD_OUTPUT_WITH_SPECIAL_CHARS,"");
        CommandOutput testObject2 = new CommandOutput(new ExecReturnCode(0), STANDARD_OUTPUT_WITH_SPECIAL_CHARS,"");
        assertEquals(testObject1.hashCode(), testObject2.hashCode());
    }

    @Test
    public void testExtractMessageFromStandardOutput(){
        CommandOutput testObject = new CommandOutput(new ExecReturnCode(36), STANDARD_OUTPUT_WITH_SHELL_INFO,"");
        testObject.cleanStandardOutput();
        assertEquals(STANDARD_OUTPUT_WITH_SHELL_INFO_REMOVED, testObject.extractMessageFromStandardOutput());
    }

    @Test
    public void testCleanStandardOutForHeapDump() {
        CommandOutput commandOutput = new CommandOutput(new ExecReturnCode(0), "SHOULD NOT SHOW UP IN STANDARD OUT***heapdump-start***Dumping heap to d:/test/location/for/heap/dump \r\nHeap dump file created***heapdump-end***DO NOT SHOW UP EITHER", "");
        commandOutput.cleanHeapDumpStandardOutput();
        assertEquals("Dumping heap to d:/test/location/for/heap/dump \r\nHeap dump file created", commandOutput.getStandardOutput());
    }

    @Test
    public void testCleanStandardOutForHeapDumpNoEnd() {
        CommandOutput commandOutput = new CommandOutput(new ExecReturnCode(0), "SHOULD NOT SHOW UP IN STANDARD OUT***heapdump-start***Dumping heap to d:/test/location/for/heap/dump \r\nHeap dump file created", "");
        commandOutput.cleanHeapDumpStandardOutput();
        assertEquals("Dumping heap to d:/test/location/for/heap/dump \r\nHeap dump file created", commandOutput.getStandardOutput());
    }
}
