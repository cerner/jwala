package com.cerner.jwala.common.exec;

import java.io.Serializable;
import java.util.Arrays;

public class RemoteSystemConnection implements Serializable {

    private final String user;
    private final String host;
    private final Integer port;
    private final char[] encryptedPassword;

    public RemoteSystemConnection(final String theUser,
                                  final char[] theEncryptedPassword,
                                  final String theHost,
                                  final Integer thePort) {
        user = theUser;
        host = theHost;
        port = thePort;
        encryptedPassword = theEncryptedPassword != null ? Arrays.copyOf(theEncryptedPassword, theEncryptedPassword.length) : new char[]{};
    }

    public String getUser() {
        return user;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public char[] getEncryptedPassword() {
        return Arrays.copyOf(encryptedPassword, encryptedPassword.length);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RemoteSystemConnection that = (RemoteSystemConnection) o;

        if (!user.equals(that.user)) {
            return false;
        }
        if (!host.equals(that.host)) {
            return false;
        }
        return port.equals(that.port);

    }

    @Override
    public int hashCode() {
        int result = user.hashCode();
        result = 31 * result + host.hashCode();
        result = 31 * result + port.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "RemoteSystemConnection{" +
                "user='" + user + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                '}';
    }

}
