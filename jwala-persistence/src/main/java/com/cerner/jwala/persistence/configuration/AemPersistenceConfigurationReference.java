package com.cerner.jwala.persistence.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.cerner.jwala.persistence.configuration.listener.PersistenceApplicationListener;

@Configuration
@Import({AemJpaConfiguration.class,
         AemDaoConfiguration.class,
         AemPersistenceServiceConfiguration.class})
public class AemPersistenceConfigurationReference {
    
    public AemPersistenceConfigurationReference() {}

    @Bean
    public PersistenceApplicationListener getPersistenceApplicationListener() {
        return new PersistenceApplicationListener();
    }
    
}
