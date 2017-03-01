package com.cerner.jwala.service.binarydistribution.impl;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Arvindo Kinny on 10/11/2016.
 */
public class BinaryDistributionLockManagerImplTest {
    BinaryDistributionLockManagerImpl binaryDistributionLockManager = new BinaryDistributionLockManagerImpl();
    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void writeLock() throws Exception {
        binaryDistributionLockManager.writeLock("TEST-JVM");
        binaryDistributionLockManager.writeUnlock("TEST-JVM");
    }

}