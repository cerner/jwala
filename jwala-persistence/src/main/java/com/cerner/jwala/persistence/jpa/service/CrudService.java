package com.cerner.jwala.persistence.jpa.service;

import com.cerner.jwala.persistence.jpa.domain.AbstractEntity;

import java.util.List;

public interface CrudService<T extends AbstractEntity<T>> {

    T findById(final Long id);

    List<T> findAll();

    T create(final T t);

    T update(final T entity);

    void remove(final T entity);

    void remove(Long id);
}

