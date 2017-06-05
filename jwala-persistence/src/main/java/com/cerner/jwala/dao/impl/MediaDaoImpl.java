package com.cerner.jwala.dao.impl;

import com.cerner.jwala.common.domain.model.media.MediaType;
import com.cerner.jwala.dao.MediaDao;
import com.cerner.jwala.persistence.jpa.domain.JpaMedia;
import com.cerner.jwala.persistence.jpa.service.impl.AbstractCrudServiceImpl;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;

/**
 * DAO that handles persistence operations with {@link JpaMedia}
 *
 * Created by Jedd Anthony Cuison on 12/6/2016
 */
@Repository
public class MediaDaoImpl extends AbstractCrudServiceImpl<JpaMedia> implements MediaDao {

    @Override
    public JpaMedia find(final String name) {
        final Query q = entityManager.createNamedQuery(JpaMedia.QUERY_FIND_BY_NAME, JpaMedia.class);
        q.setParameter(JpaMedia.PARAM_NAME, name);
        return (JpaMedia) q.getSingleResult();
    }

    @Override
    public JpaMedia findByNameAndType(String name, MediaType type) {
        final Query q = entityManager.createNamedQuery(JpaMedia.QUERY_FIND_BY_NAME_TYPE, JpaMedia.class);
        q.setParameter(JpaMedia.PARAM_NAME, name);
        q.setParameter(JpaMedia.PARAM_TYPE, type);
        return (JpaMedia) q.getSingleResult();
    }

}
