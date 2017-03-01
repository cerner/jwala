package com.cerner.jwala.persistence.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cerner.jwala.persistence.jpa.service.HistoryCrudService;
import com.cerner.jwala.persistence.jpa.service.impl.HistoryCrudServiceImpl;

@Configuration
public class AemDaoConfiguration {

    @Bean
    public HistoryCrudService getHistoryDao() {
        return new HistoryCrudServiceImpl();
    }

}
