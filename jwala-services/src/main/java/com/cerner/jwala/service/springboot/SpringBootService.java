package com.cerner.jwala.service.springboot;

import com.cerner.jwala.persistence.jpa.domain.JpaSpringBootApp;

import java.io.FileNotFoundException;
import java.util.Map;

/**
 * Created on 6/1/2017.
 */
public interface SpringBootService {

    JpaSpringBootApp controlSpringBoot(String name, String command);

    JpaSpringBootApp generateAndDeploy(String name) throws FileNotFoundException;

    JpaSpringBootApp createSpringBoot(Map<String, Object> springBootDataMap, Map<String, Object> springBootFileDataMap);

    JpaSpringBootApp update(JpaSpringBootApp springBootApp);

    void remove(String name);

    JpaSpringBootApp find(String name);
}
