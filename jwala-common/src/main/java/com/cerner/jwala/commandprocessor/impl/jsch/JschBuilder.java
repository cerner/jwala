package com.cerner.jwala.commandprocessor.impl.jsch;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;

public class JschBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(JschBuilder.class);
    private String knownHostsFileName;
    private String privateKeyFileName;

    public JschBuilder() {
    }

    public JschBuilder(final String aKnownHostsFileName, final String aPrivateKeyFileName) {
        knownHostsFileName = aKnownHostsFileName;
        privateKeyFileName = aPrivateKeyFileName;
    }

    public JschBuilder setKnownHostsFileName(final String aKnownHostsFileName) {
        knownHostsFileName = aKnownHostsFileName;
        return this;
    }

    public JschBuilder setPrivateKeyFileName(final String aPrivateKeyFileName) {
        privateKeyFileName = aPrivateKeyFileName;
        return this;
    }

    public JSch build() throws JSchException {
        LOGGER.debug("Initializing JSch Logger");
        JSch.setLogger(new JschLogger());
        final JSch jsch = new JSch();
        try {
            if (null != knownHostsFileName && new File(knownHostsFileName).exists()) {
                jsch.setKnownHosts(knownHostsFileName);
            }
            if (null != privateKeyFileName && new File(privateKeyFileName).exists()) {
                jsch.addIdentity(privateKeyFileName);
            }
        } catch (JSchException e) {
            LOGGER.error("Could not access known hosts or private key file.", e);
            if (!(e.getCause() instanceof FileNotFoundException)) {
                throw new JSchException();
            }
        }
        return jsch;
    }
}
