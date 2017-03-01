package com.cerner.jwala.commandprocessor.jsch.impl;

import com.cerner.jwala.common.exec.RemoteSystemConnection;

/**
 * A key that identifies a channel's session.
 * <p/>
 * Created by Jedd Cuison on 2/26/2016.
 */
public class ChannelSessionKey {

    public final RemoteSystemConnection remoteSystemConnection;
    public final ChannelType channelType;

    public ChannelSessionKey(final RemoteSystemConnection remoteSystemConnection, final ChannelType channelType) {
        this.remoteSystemConnection = remoteSystemConnection;
        this.channelType = channelType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ChannelSessionKey that = (ChannelSessionKey) o;

        if (!remoteSystemConnection.equals(that.remoteSystemConnection)) {
            return false;
        }
        return channelType == that.channelType;

    }

    @Override
    public int hashCode() {
        int result = remoteSystemConnection.hashCode();
        result = 31 * result + channelType.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ChannelSessionKey{" +
                "remoteSystemConnection=" + remoteSystemConnection +
                ", channelType=" + channelType +
                '}';
    }

}
