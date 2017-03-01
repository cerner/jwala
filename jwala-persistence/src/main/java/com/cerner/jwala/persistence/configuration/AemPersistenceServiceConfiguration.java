package com.cerner.jwala.persistence.configuration;

import com.cerner.jwala.persistence.jpa.service.*;
import com.cerner.jwala.persistence.jpa.service.impl.*;
import com.cerner.jwala.persistence.service.*;
import com.cerner.jwala.persistence.service.impl.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Configuration
@ComponentScan({"com.cerner.jwala.dao.impl"})
public class AemPersistenceServiceConfiguration {

    @PersistenceContext(unitName = "jwala-unit")
    private EntityManager em;

    @Bean
    public EntityManager getEntityManager() {
        return em;
    }

    @Bean
    public ResourcePersistenceService getResourcePersistenceService() {
        return new JpaResourcePersistenceServiceImpl();
    }

    @Bean(name="jvmPersistenceService")
    public JvmPersistenceService getJvmPersistenceService() {
        return new JpaJvmPersistenceServiceImpl(getJvmCrudService(), getApplicationCrudService(), getGroupJvmRelationshipService());
    }

    @Bean(name="groupPersistenceService")
    public GroupPersistenceService getGroupPersistenceService() {
        return new JpaGroupPersistenceServiceImpl(getGroupCrudService(),
                                                  getGroupJvmRelationshipService());
    }

    @Bean
    protected GroupJvmRelationshipService getGroupJvmRelationshipService() {
        return new GroupJvmRelationshipServiceImpl(getGroupCrudService(),
                                                   getJvmCrudService());
    }

    @Bean
    protected GroupCrudService getGroupCrudService() {
        return new GroupCrudServiceImpl();
    }

    @Bean
    protected JvmCrudService getJvmCrudService() {
        return new JvmCrudServiceImpl();
    }

    @Bean
    protected ApplicationCrudService getApplicationCrudService() {
        return new ApplicationCrudServiceImpl();
    }

    @Bean
    public ApplicationPersistenceService getApplicationPersistenceService() {
        return new JpaApplicationPersistenceServiceImpl(getApplicationCrudService(), getGroupCrudService());
    }

    @Bean(name = "webServerPersistenceService")
    public WebServerPersistenceService getWebServerPersistenceService() {
        return new WebServerPersistenceServiceImpl(getGroupCrudService(), getWebserverCrudService());
    }

    @Bean(name="webServerCrudService")
    public WebServerCrudService getWebserverCrudService() {
        return new WebServerCrudServiceImpl();
    }

}
