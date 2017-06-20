package com.cerner.jwala.service.impl.spring.component;

import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.service.CollectionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Jedd Cuison on 6/19/2017
 */
@Service
public class JvmWinSvcPwdCollectionServiceImpl implements CollectionService<String> {

    private static final Set<String> PASSWORDS = new CopyOnWriteArraySet<>();

    public JvmWinSvcPwdCollectionServiceImpl(JvmPersistenceService jvmPersistenceService) {
        // Populate the map of items to remove in the logs
        final List<Jvm> jvms = jvmPersistenceService.getJvms();
        for (final Jvm jvm: jvms) {
            if (StringUtils.isNotEmpty(jvm.getEncryptedPassword())) {
                PASSWORDS.add(jvm.getEncryptedPassword());
            }
        }
    }

    @Override
    public void add(final String password) {
        PASSWORDS.add(password);
    }

    @Override
    public void remove(final String password) {
        PASSWORDS.remove(password);
    }

    @Override
    public void clear() {
        PASSWORDS.clear();
    }

    public static Set<String> getIterable() {
        return PASSWORDS;
    }

}
