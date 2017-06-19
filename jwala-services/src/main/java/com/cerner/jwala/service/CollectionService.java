package com.cerner.jwala.service;

import java.util.Iterator;

/**
 * Created by Jedd Cuison on 6/16/2017
 */
public interface CollectionService<T> {

    void add(T obj);

    void remove(T obj);

    Iterable<T> getIterable();

    void clear();
}
