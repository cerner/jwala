package com.cerner.jwala.common.scrubber;

/**
 * Defines a bare minimum collection
 *
 * Created by Jedd Cuison on 6/16/2017
 */
public interface ObjectStoreService<T> {

    void add(T obj);

    void remove(T obj);

    void clear();

    Iterable<T> getIterable();
}
