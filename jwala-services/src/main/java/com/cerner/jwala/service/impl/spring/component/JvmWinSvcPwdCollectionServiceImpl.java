package com.cerner.jwala.service.impl.spring.component;

import com.cerner.jwala.service.CollectionService;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Jedd Cuison on 6/19/2017
 */
@Service
public class JvmWinSvcPwdCollectionServiceImpl implements CollectionService<String> {

    private Set<String> passwords = new CopyOnWriteArraySet<>();

    @Override
    public void add(final String password) {
        passwords.add(password);
    }

    @Override
    public void remove(final String password) {
        passwords.remove(password);
    }

    @Override
    public Set<String> getIterable() {
        return passwords;
    }

    @Override
    public void clear() {
        passwords.clear();
    }
}
