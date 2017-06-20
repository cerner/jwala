package com.cerner.jwala.common.scrubber.impl;

import com.cerner.jwala.common.domain.model.ssh.DecryptPassword;
import com.cerner.jwala.common.scrubber.KeywordSetWrapperService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.junit.Assert.*;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by Jedd Cuison on 6/20/2017
 */
public class JvmWinSvcAcctPasswordScrubberServiceImplTest {

    private JvmWinSvcAcctPasswordScrubberServiceImpl jvmWinSvcAcctPasswordScrubberService;

    @Mock
    private DecryptPassword mockDecryptPassword;

    @Before
    public void setUp() {
        initMocks(this);
        KeywordSetWrapperService.copyOnWriteArraySet.clear();
        KeywordSetWrapperService.copyOnWriteArraySet.add("secret");
        jvmWinSvcAcctPasswordScrubberService = new JvmWinSvcAcctPasswordScrubberServiceImpl(mockDecryptPassword);
    }

    @Test
    public void testScrub() throws Exception {
        when(mockDecryptPassword.decrypt("secret")).thenReturn("secret");
        final String result = jvmWinSvcAcctPasswordScrubberService.scrub("password = secret");
        assertEquals("password = ********", result);
    }
}