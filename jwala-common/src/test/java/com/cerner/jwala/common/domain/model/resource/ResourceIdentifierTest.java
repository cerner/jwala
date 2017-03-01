package com.cerner.jwala.common.domain.model.resource;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Stevne Ger on 12/9/16.
 */
public class ResourceIdentifierTest {

    @Test
    public void testIsEqualsSucceed() {

        ResourceIdentifier resourceIdentifier =
                new ResourceIdentifier.
                        Builder().
                        setGroupName("a group").
                        setJvmName("a jvm").
                        build();

        ResourceIdentifier anotherResourceIdentifier =
                new ResourceIdentifier.
                        Builder().
                        setGroupName("a group").
                        setJvmName("a jvm").
                        build();
        assertTrue(resourceIdentifier.equals(anotherResourceIdentifier));
    }

    @Test
    public void testIsEqualsFail() {

        ResourceIdentifier resourceIdentifier =
                new ResourceIdentifier.
                        Builder().
                        setGroupName("a group").
                        setJvmName("a jvm").
                        build();

        ResourceIdentifier anotherResourceIdentifier =
                new ResourceIdentifier.
                        Builder().
                        setGroupName("another group").
                        setJvmName("another jvm").
                        build();
        assertTrue(!resourceIdentifier.equals(anotherResourceIdentifier));
    }
}
