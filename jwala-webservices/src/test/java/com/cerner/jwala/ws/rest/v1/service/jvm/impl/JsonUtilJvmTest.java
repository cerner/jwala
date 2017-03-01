package com.cerner.jwala.ws.rest.v1.service.jvm.impl;

import org.junit.Test;

import com.cerner.jwala.ws.rest.v1.service.jvm.impl.JsonUtilJvm;

import static org.junit.Assert.assertEquals;

/**
 * Created by Jedd Cuison on 6/3/14.
 */
public class JsonUtilJvmTest {

    @Test
    public void testStringToInteger() {
        assertEquals(Integer.valueOf("5"), JsonUtilJvm.stringToInteger("5"));
    }

    @Test
    public void testStringToIntegerNonNumeric() {
        assertEquals(null, JsonUtilJvm.stringToInteger(""));
        assertEquals(null, JsonUtilJvm.stringToInteger(" "));
        assertEquals(null, JsonUtilJvm.stringToInteger("ASD$#@"));
    }

    @Test
    public void testStringToIntegerOutOfScope() {
        assertEquals(null, JsonUtilJvm.stringToInteger("123456676989084241314253456457568687686745345"));
    }

    @Test
    public void testStringToIntegerDecimalValues() {
        assertEquals(null, JsonUtilJvm.stringToInteger("3.55"));
    }

}
