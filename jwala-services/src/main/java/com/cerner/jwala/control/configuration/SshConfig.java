package com.cerner.jwala.control.configuration;

import com.cerner.jwala.commandprocessor.impl.jsch.JschBuilder;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.properties.ApplicationProperties;

import com.cerner.jwala.common.properties.PropertyKeys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class SshConfig {

    private static final Integer DEFAULT_SSH_PORT = 22;

    @Bean
    public SshConfiguration getSshConfiguration() {

        return new SshConfiguration(ApplicationProperties.get(PropertyKeys.USER_NAME),
                ApplicationProperties.getAsInteger(PropertyKeys.PORT, DEFAULT_SSH_PORT),
                ApplicationProperties.get(PropertyKeys.PRIVATE_KEY_FILE),
                ApplicationProperties.get(PropertyKeys.KNOWN_HOSTS_FILE),
                ApplicationProperties.get(PropertyKeys.ENCRYPTED_PASSWORD).toCharArray());
    }

    @Bean
    public JschBuilder getJschBuilder() {
        final SshConfiguration sshConfig = getSshConfiguration();
        return new JschBuilder()
                .setPrivateKeyFileName(sshConfig.getPrivateKeyFile())
                .setKnownHostsFileName(sshConfig.getKnownHostsFile());

    }
}
