package com.cerner.jwala.service.springboot;

import com.cerner.jwala.persistence.jpa.domain.JpaSpringBootApp;

import java.util.Map;

/**
 * Created on 6/1/2017.
 */
public interface SpringBootService {

    JpaSpringBootApp controlSpringBoot(String name, String command);

    JpaSpringBootApp generateAndDeploy(String name);

    JpaSpringBootApp createSpringBoot(Map<String, String> springBootDataMap, Map<String, Object> springBootFileDataMap);

    JpaSpringBootApp update(JpaSpringBootApp springBootApp);

    void remove(JpaSpringBootApp springBootApp);

    JpaSpringBootApp find(String name);
}
