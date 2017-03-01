package com.cerner.jwala.service.state.impl;

import com.cerner.jwala.service.state.InMemoryStateManagerService;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements {@link InMemoryStateManagerService} using a {@link HashMap}.
 *
 * Created by Jedd Cuison on 3/25/2016.
 */
public class InMemoryStateManagerServiceImpl<K, V>  implements InMemoryStateManagerService<K, V>  {

    private final ConcurrentHashMap<K, V> stateMap = new ConcurrentHashMap<>();

    @Override
    public void put(K key, V val) {
        stateMap.put(key, val);
    }

    @Override
    public V get(K key) {
        return stateMap.get(key);
    }

    @Override
    public void remove(K key) {
        stateMap.remove(key);
    }

    @Override
    public boolean containsKey(K key) {
        return stateMap.containsKey(key);
    }
}
