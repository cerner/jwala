package com.cerner.jwala.dao.impl;

import com.cerner.jwala.dao.SpringBootAppDao;
import com.cerner.jwala.persistence.jpa.domain.JpaSpringBootApp;
import com.cerner.jwala.persistence.jpa.service.impl.AbstractCrudServiceImpl;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;

/**
 * Created by Jlwksion on 6/1/2017.
 */
@Repository
public class SpringBootAppDaoImpl extends AbstractCrudServiceImpl<JpaSpringBootApp> implements SpringBootAppDao {

    @Override
    public JpaSpringBootApp find(final String name) {
        final Query q = entityManager.createNamedQuery(JpaSpringBootApp.QUERY_FIND_BY_NAME, JpaSpringBootApp.class);
        q.setParameter(JpaSpringBootApp.PARAM_NAME, name);
        return (JpaSpringBootApp) q.getSingleResult();
    }

}
