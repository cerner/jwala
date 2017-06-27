package com.cerner.jwala.common.scrubber.impl;

import com.cerner.jwala.common.domain.model.ssh.DecryptPassword;
import com.cerner.jwala.common.scrubber.ObjectStoreService;
import com.cerner.jwala.common.scrubber.ScrubberService;
import org.springframework.stereotype.Service;

/**
 * Created by Jedd Cuison on 6/20/2017
 */
@Service
public class JvmWinSvcAcctPasswordScrubberServiceImpl implements ScrubberService {

    private static final String REPLACEMENT = "********";

    private final ObjectStoreService<String> objectStoreService;
    private final DecryptPassword decryptor;

    public JvmWinSvcAcctPasswordScrubberServiceImpl(final ObjectStoreService<String> objectStoreService,
                                                    final DecryptPassword decryptor) {
        this.objectStoreService = objectStoreService;
        this.decryptor = decryptor;
    }

    @Override
    public String scrub(final String raw) {
        for (final String password : objectStoreService.getIterable()) {
            final String scrubbedStr = raw.replaceAll(decryptor.decrypt(password), REPLACEMENT);
            if (!raw.equalsIgnoreCase(scrubbedStr)) {
                return scrubbedStr;
            }
        }
        return raw;
    }
}
