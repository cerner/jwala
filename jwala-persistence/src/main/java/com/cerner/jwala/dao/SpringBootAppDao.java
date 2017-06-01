package com.cerner.jwala.dao;

import com.cerner.jwala.persistence.jpa.domain.JpaSpringBootApp;

import java.util.List;

/**
 * Created by Jlkwison on 6/1/2017
 */
public interface SpringBootAppDao {

    JpaSpringBootApp findById(Long id);

    JpaSpringBootApp find(String name);

    List<JpaSpringBootApp> findAll();

    JpaSpringBootApp create(JpaSpringBootApp media);

    void remove(JpaSpringBootApp media);

    JpaSpringBootApp update(JpaSpringBootApp media);
}
