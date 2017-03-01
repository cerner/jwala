package com.cerner.jwala.control.configuration;

import com.cerner.jwala.commandprocessor.impl.jsch.JschBuilder;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.properties.ApplicationProperties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class AemSshConfig {

    @Bean
    public SshConfiguration getSshConfiguration() {

        final Properties sshProperties = ApplicationProperties.getProperties();

        final SshConfiguration configuration = new SshConfiguration(getStringPropertyFrom(sshProperties,
                                                                                          AemSshProperty.USER_NAME),
                                                                    getIntegerPropertyFrom(sshProperties,
                                                                                           AemSshProperty.PORT),
                                                                    getStringPropertyFrom(sshProperties,
                                                                                          AemSshProperty.PRIVATE_KEY_FILE),
                                                                    getStringPropertyFrom(sshProperties,
                                                                                          AemSshProperty.KNOWN_HOSTS_FILE),
                                                                    getStringPropertyFrom(sshProperties,
                                                                                           AemSshProperty.ENCRYPTED_PASSWORD).toCharArray());

        return configuration;
    }

    @Bean
    public JschBuilder getJschBuilder() {
        final SshConfiguration sshConfig = getSshConfiguration();
        final JschBuilder builder = new JschBuilder().setPrivateKeyFileName(sshConfig.getPrivateKeyFile())
                                                     .setKnownHostsFileName(sshConfig.getKnownHostsFile());

        return builder;
    }

    protected String getStringPropertyFrom(final Properties someProperties,
                                           final AemSshProperty aProperty) {
        return someProperties.getProperty(aProperty.getPropertyName(), null);
    }

    protected Integer getIntegerPropertyFrom(final Properties someProperties,
                                             final AemSshProperty aProperty) {
        return Integer.valueOf(getStringPropertyFrom(someProperties,
                                                     aProperty));
    }
}
