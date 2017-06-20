package com.cerner.jwala.common.scrubber.impl;

import com.cerner.jwala.common.domain.model.ssh.DecryptPassword;
import com.cerner.jwala.common.scrubber.KeywordSetWrapperService;
import com.cerner.jwala.common.scrubber.ScrubberService;
import org.springframework.stereotype.Service;

/**
 * Created by Jedd Cuison on 6/20/2017
 */
@Service
public class JvmWinSvcAcctPasswordScrubberServiceImpl implements ScrubberService {

    private static final String REPLACEMENT = "********";

    private final DecryptPassword decryptor;

    public JvmWinSvcAcctPasswordScrubberServiceImpl(final DecryptPassword decryptor) {
        this.decryptor = decryptor;
    }

    @Override
    public String scrub(final String raw) {
        for (final String password : KeywordSetWrapperService.copyOnWriteArraySet) {
            final String scrubbedStr = raw.replaceAll(decryptor.decrypt(password), REPLACEMENT);
            if (!raw.equalsIgnoreCase(scrubbedStr)) {
                return scrubbedStr;
            }
        }
        return raw;
    }
}
