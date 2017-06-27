package com.cerner.jwala.service.impl.spring.component;

import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.scrubber.ObjectStoreService;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Jedd Cuison on 6/19/2017
 */
@Service
public class JvmWinSvcPwdStoreServiceImpl implements ObjectStoreService<String> {

    private final Set<String> copyOnWriteArraySet = new CopyOnWriteArraySet<>();

    public JvmWinSvcPwdStoreServiceImpl(final JvmPersistenceService jvmPersistenceService) {
        // Populate the map of items to remove in the logs
        final List<Jvm> jvms = jvmPersistenceService.getJvms();
        for (final Jvm jvm: jvms) {
            if (StringUtils.isNotEmpty(jvm.getEncryptedPassword())) {
                copyOnWriteArraySet.add(jvm.getEncryptedPassword());
            }
        }
    }

    @Override
    public void add(final String password) {
        copyOnWriteArraySet.add(password);
    }

    @Override
    public void remove(final String password) {
        copyOnWriteArraySet.remove(password);
    }

    @Override
    public void clear() {
        copyOnWriteArraySet.clear();
    }

    @Override
    public Iterable<String> getIterable() {
        return copyOnWriteArraySet;
    }
}