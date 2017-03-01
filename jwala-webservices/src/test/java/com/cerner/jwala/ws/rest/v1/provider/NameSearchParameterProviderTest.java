package com.cerner.jwala.ws.rest.v1.provider;

import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import com.cerner.jwala.ws.rest.v1.provider.NameSearchParameterProvider;

import static org.junit.Assert.*;

@RunWith(Theories.class)
public class NameSearchParameterProviderTest {

    @Test
    public void testNameIsPresent() {

        final String name = "This Name Is Present";
        final NameSearchParameterProvider provider = new NameSearchParameterProvider(name);

        assertTrue(provider.isNamePresent());
        assertEquals(name,
                     provider.getName());
    }

    @DataPoints
    public static String[] notPresentNames() {
        return new String[] {"", "    ", null};
    }

    @Theory(nullsAccepted = true)
    public void testNameIsNotPresent(final String aPotentialName) {

        final NameSearchParameterProvider provider = new NameSearchParameterProvider(aPotentialName);

        assertFalse(provider.isNamePresent());
    }
}
