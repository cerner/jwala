package com.cerner.jwala.files.configuration;

import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.files.*;
import com.cerner.jwala.files.impl.*;
import com.cerner.jwala.files.resources.ResourceTypeDeserializer;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import java.io.IOException;

@Configuration
public class JwalaFileManagerConfiguration {
    
    /**
     * Look up path properties from the application properties
     * @return A wrapper around Path properties
     */
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.INTERFACES) 
    public FilesConfiguration getFilesConfiguration() {
        return new PropertyFilesConfigurationImpl(ApplicationProperties.getProperties());
    }

    @Bean NameSynthesizer getNameSynthesizer() {
        return new DefaultNameSynthesizer();
    }
    
    @Bean WebArchiveManager getWebArchiveManager() {
        return new WebArchiveManagerImpl();
    }
    
    @Bean
    RepositoryService getFileSystemStorage() throws IOException {
        return new LocalFileSystemRepositoryServiceImpl();
    }

    @Bean
    public FileManager getFileManager() {
        return new FileManagerImpl();
    }   
    
    @Bean
    public ResourceTypeDeserializer getResourceTypeDeserializer() { 
        return new ResourceTypeDeserializer();
    }
}