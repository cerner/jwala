package com.cerner.jwala.service.binarydistribution;

/**
 * Created by Arvindo Kinny on 10/11/2016.
 */
public interface BinaryDistributionLockManager {

    /**
     * This method marks a resource as locked using ReentrantReadWriteLock
     * @param resourseName
     */
    public void writeLock(String resourseName);

    /**
     * This method marks a resource as unlocked using ReentrantReadWriteLock
     * @param resourseName
     */
    public void writeUnlock(String resourseName);
}
