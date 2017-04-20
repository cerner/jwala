package com.cerner.jwala.common.domain.model.ssh;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.exception.InternalErrorException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class SshConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(SshConfiguration.class);

    private final String userName;
    private final Integer port;
    private final String privateKeyFile;
    private final String knownHostsFile;
    private final char[] encPassword;

    public SshConfiguration(final String theUserName,
                            final Integer thePort,
                            final String thePrivateKeyFile,
                            final String theKnownHostsFile,
                            final char[] theEncPassword) {


        if (StringUtils.isBlank(theUserName) || StringUtils.isBlank(new String(theEncPassword))) {
            String message = "Startup Aborted: Jwala SSH username, port and password properties not found in vars.properties!!!";
            LOGGER.error(message);
            throw new InternalErrorException(FaultType.SSH_CONFIG_MISSING, message);
        }
        userName = theUserName;
        port = thePort;
        privateKeyFile = thePrivateKeyFile;
        knownHostsFile = theKnownHostsFile;
        encPassword = Arrays.copyOf(theEncPassword, theEncPassword.length);
    }

    public String getUserName() {
        return userName;
    }

    public Integer getPort() {
        return port;
    }

    public String getPrivateKeyFile() {
        return privateKeyFile;
    }

    public String getKnownHostsFile() {
        return knownHostsFile;
    }

    public char[] getEncryptedPassword() {
        return Arrays.copyOf(encPassword, encPassword.length);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        SshConfiguration rhs = (SshConfiguration) obj;
        return new EqualsBuilder()
                .append(this.userName, rhs.userName)
                .append(this.port, rhs.port)
                .append(this.privateKeyFile, rhs.privateKeyFile)
                .append(this.knownHostsFile, rhs.knownHostsFile)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(userName)
                .append(port)
                .append(privateKeyFile)
                .append(knownHostsFile)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("userName", userName)
                .append("port", port)
                .append("privateKeyFile", privateKeyFile)
                .append("knownHostsFile", knownHostsFile)
                .toString();
    }

}
