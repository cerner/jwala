package com.cerner.jwala.service.configuration.service;

import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.control.configuration.AemControlConfigReference;
import com.cerner.jwala.files.FilesConfiguration;
import com.cerner.jwala.files.impl.PropertyFilesConfigurationImpl;
import com.cerner.jwala.persistence.configuration.AemPersistenceConfigurationReference;
import com.cerner.jwala.service.configuration.WebSocketConfig;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.*;

@Configuration
@Import({AemServiceConfiguration.class,
         AemPersistenceConfigurationReference.class,
         AemControlConfigReference.class,
         WebSocketConfig.class,
         ResourceHandlerConfiguration.class})
public class AemServiceConfigReference {

    /**
     * Look up path properties from the application properties
     * @return A wrapper around Path properties
     */
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.INTERFACES)
    public FilesConfiguration getFilesConfiguration() {
        return new PropertyFilesConfigurationImpl(ApplicationProperties.getProperties());
    }

}
