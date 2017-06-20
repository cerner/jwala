package com.cerner.jwala.common.scrubber;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Jedd Cuison on 6/16/2017
 */
public interface KeywordSetWrapperService {

    Set<String> copyOnWriteArraySet = new CopyOnWriteArraySet<>();

    void add(String obj);

    void remove(String obj);

    void clear();
}
