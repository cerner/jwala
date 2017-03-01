package com.cerner.jwala.commandprocessor.impl;

import com.cerner.jwala.commandprocessor.impl.jsch.JschBuilder;
import com.cerner.jwala.commandprocessor.jsch.impl.ChannelSessionKey;
import com.cerner.jwala.commandprocessor.jsch.impl.KeyedPooledJschChannelFactory;
import com.cerner.jwala.common.exec.RemoteSystemConnection;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;

import java.io.File;

public class CommonSshTestConfiguration {

    private static final String KNOWN_HOSTS_FILE = "known_hosts";
    private static final String PRIVATE_KEY = "test_private_key";

    private final JschBuilder builder;
    private final RemoteSystemConnection remoteSystemConnection;
    private final GenericKeyedObjectPool<ChannelSessionKey, Channel> channelPool;
    private String password;

    public CommonSshTestConfiguration() throws JSchException {
        //TODO (Corey) generalize this so that %HOME% works
        //TODO create duplicate set of keys without a passphrase for testing
        builder = new JschBuilder(getKnownHostsFile(),
                                  getPrivateKey());
        channelPool = new GenericKeyedObjectPool(new KeyedPooledJschChannelFactory(builder.build()));
        remoteSystemConnection = new RemoteSystemConnection("N9SFGLabTomcatAdmin",
                                                            /*"Passw0rd1"*/"===encryptedPassword===".toCharArray(),
                                                            "somehost0005",
                                                            22);
    }

    public JschBuilder getBuilder() {
        return builder;
    }

    public RemoteSystemConnection getRemoteSystemConnection() {
        return remoteSystemConnection;
    }

    public String getKnownHostsFile() {
        final File file = new File(this.getClass().getClassLoader().getResource(KNOWN_HOSTS_FILE).getFile());
        return file.getAbsolutePath();
    }

    public String getPrivateKey() {
        final File file = new File(this.getClass().getClassLoader().getResource(PRIVATE_KEY).getFile());
        return file.getAbsolutePath();
    }

    public char[] getPassword() {
        return remoteSystemConnection.getEncryptedPassword();
    }

    public GenericKeyedObjectPool<ChannelSessionKey, Channel> getChannelPool() {
        return channelPool;
    }

}
