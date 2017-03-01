package com.cerner.jwala.control.configuration;

import com.cerner.jwala.common.properties.ApplicationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class AemCommandExecutorConfig {

    @Bean(destroyMethod = "shutdownNow")
    protected ExecutorService getExecutorService() {
        return Executors.newFixedThreadPool(Integer.parseInt(ApplicationProperties.get("command.executor.fixed.thread.pool", "150")));
    }

}
