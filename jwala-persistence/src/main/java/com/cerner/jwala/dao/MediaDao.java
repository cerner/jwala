package com.cerner.jwala.dao;

import com.cerner.jwala.common.domain.model.media.MediaType;
import com.cerner.jwala.persistence.jpa.domain.JpaMedia;

import java.util.List;

/**
 * Persistence layer that deals with media
 *
 * Created by Jedd Anthony Cuison on 12/6/2016
 */
public interface MediaDao {

    JpaMedia findById(Long id);

    JpaMedia find(String name);

    JpaMedia findByNameAndType(String name, MediaType type);

    List<JpaMedia> findAll();

    JpaMedia create(JpaMedia media);

    void remove(JpaMedia media);

    JpaMedia update(JpaMedia media);
}
