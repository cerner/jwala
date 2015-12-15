package com.siemens.cto.aem.persistence.configuration;

import com.siemens.cto.aem.persistence.dao.HistoryDao;
import com.siemens.cto.aem.persistence.dao.ApplicationDao;
import com.siemens.cto.aem.persistence.dao.impl.JpaApplicationDaoImpl;
import com.siemens.cto.aem.persistence.dao.GroupDao;
import com.siemens.cto.aem.persistence.dao.impl.JpaGroupDaoImpl;
import com.siemens.cto.aem.persistence.dao.impl.HistoryDaoImpl;
import com.siemens.cto.aem.persistence.dao.JvmDao;
import com.siemens.cto.aem.persistence.dao.impl.JpaJvmDaoImpl;
import com.siemens.cto.aem.persistence.dao.WebServerDao;
import com.siemens.cto.aem.persistence.dao.impl.JpaWebServerDaoImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AemDaoConfiguration {

    @Autowired
    private AemDataSourceConfiguration dataSourceConfiguration;

    @Bean
    public GroupDao getGroupDao() {
        return new JpaGroupDaoImpl();
    }

    @Bean
    public JvmDao getJvmDao() {
        return new JpaJvmDaoImpl();
    }

    @Deprecated // TODO (Peter) Needs replacing with a PersistenceService
    @Bean(name = "webServerDao")
    public WebServerDao getWebServerDao() {
        return new JpaWebServerDaoImpl();
    }

    @Bean
    public ApplicationDao getApplicationDao() {
        return new JpaApplicationDaoImpl();
    }

    @Bean
    public HistoryDao getHistoryDao() {
        return new HistoryDaoImpl();
    }

}
