package com.cerner.jwala.persistence.configuration.listener;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.cerner.jwala.persistence.service.ApplicationPersistenceService;

/**
 * Listen to events and eagerly initialize OpenJPA
 */
public class PersistenceApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            ApplicationContext applicationContext = event.getApplicationContext();
            final ApplicationPersistenceService applicationPersistenceService =
                    applicationContext.getBean(ApplicationPersistenceService.class);
            applicationPersistenceService.getApplications(); // read nothing.
        }      
    }
}
