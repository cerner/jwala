package com.cerner.jwala.commandprocessor.jsch.impl;

/**
 * JSCH Chanel Type enum.
 *
 * Created by Jedd Cuison on 2/25/2016.
 */
public enum ChannelType {
    SHELL("shell"), EXEC("exec");

    // This is the "exact" word that will be used to open a JSCH channel. This should not be thought of as the enum item name!
    private final String channelType;

    ChannelType(final String channelType) {
        this.channelType = channelType;
    }

    public String getChannelType() {
        return channelType;
    }

}
