package com.cerner.jwala.common.domain.model.jvm;

import org.junit.Test;

import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;
import com.cerner.jwala.common.exception.BadRequestException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JvmControlOperationTest {

    @Test
    public void testStart() {
        final String[] startValues = {"start", "START", "StArT"};

        for (final String start : startValues) {
            assertEquals(JvmControlOperation.START,
                         JvmControlOperation.convertFrom(start));
        }
    }

    @Test
    public void testStop() {
        final String[] stopValues = {"stop", "STOP", "StOp"};

        for (final String stop : stopValues) {
            assertEquals(JvmControlOperation.STOP,
                         JvmControlOperation.convertFrom(stop));
        }
    }

    @Test
    public void testInvalidValues() {
        final String[] invalidValues = {"abc", "123", "", "startStop"};

        for (final String invalid : invalidValues) {
            try {
                JvmControlOperation.convertFrom(invalid);
            } catch (final BadRequestException bre) {
                assertTrue(true);
            }
        }
    }
}
