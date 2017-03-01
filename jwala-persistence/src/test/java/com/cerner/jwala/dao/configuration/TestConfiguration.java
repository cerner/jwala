package com.cerner.jwala.dao.configuration;

import org.h2.Driver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.OpenJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Generic configuration for testing DAOs
 *
 * Created by Jedd Anthony Cuison on 12/6/2016
 */
@Configuration
public class TestConfiguration {

    @Bean
    public DataSource getDataSource() {
        return new SimpleDriverDataSource(new Driver(), "jdbc:h2:mem:test-persistence;DB_CLOSE_DELAY=-1;LOCK_MODE=0",
                                          "sa", "");
    }

    @Bean(name = "openJpaProperties")
    public Properties getJpaProperties() {
        final Properties properties = new Properties();
        properties.setProperty("openjpa.jdbc.DBDictionary", "org.apache.openjpa.jdbc.sql.H2Dictionary");
        properties.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(schemaAction='drop,add',ForeignKeys=true)");
        properties.setProperty("openjpa.Log", "DefaultLevel=INFO");
        properties.setProperty("openjpa.InitializeEagerly", "true");
        return properties;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean getEntityManagerFactoryBean() {
        final LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(new OpenJpaVendorAdapter());
        factory.setPersistenceXmlLocation("classpath:META-INF/test-persistence.xml");
        factory.setDataSource(getDataSource());
        factory.setJpaProperties(getJpaProperties());
        return factory;
    }

    @Bean
    public PlatformTransactionManager getTransactionManager(LocalContainerEntityManagerFactoryBean factoryBean) {
        return new JpaTransactionManager(factoryBean.getObject());
    }

}
