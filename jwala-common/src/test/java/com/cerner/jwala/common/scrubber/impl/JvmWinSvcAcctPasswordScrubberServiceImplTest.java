package com.cerner.jwala.common.scrubber.impl;

import com.cerner.jwala.common.domain.model.ssh.DecryptPassword;
import com.cerner.jwala.common.scrubber.ObjectStoreService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.concurrent.CopyOnWriteArraySet;

import static org.junit.Assert.*;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by Jedd Cuison on 6/20/2017
 */
public class JvmWinSvcAcctPasswordScrubberServiceImplTest {

    private JvmWinSvcAcctPasswordScrubberServiceImpl jvmWinSvcAcctPasswordScrubberService;

    private static String TEST_RAW_STR_1 = "D:/cygwin64/home/someUser>set svc_password=secret ";
    private static String SCRUBBED_STR_1 = "D:/cygwin64/home/someUser>set svc_password=******** ";
    private static String TEST_RAW_STR_2 = "Executing command \"~/.jwala/SOME-SERVER/install-service.sh SOME-SERVER " +
            "D:/ctp/app/instances apache-tomcat-7.0.55 \"the-user\" secret \"";
    private static String SCRUBBED_STR_2 = "Executing command \"~/.jwala/SOME-SERVER/install-service.sh SOME-SERVER " +
            "D:/ctp/app/instances apache-tomcat-7.0.55 \"the-user\" ******** \"";

    @Mock
    private ObjectStoreService<String> mockObjectStoreService;

    @Mock
    private DecryptPassword mockDecryptPassword;

    @Before
    public void setUp() {
        initMocks(this);
        final CopyOnWriteArraySet<String> copyOnWriteArraySet = new CopyOnWriteArraySet<>();
        copyOnWriteArraySet.add("secret");
        when(mockObjectStoreService.getIterable()).thenReturn(copyOnWriteArraySet);
        jvmWinSvcAcctPasswordScrubberService = new JvmWinSvcAcctPasswordScrubberServiceImpl(mockObjectStoreService,
                mockDecryptPassword);
    }

    @Test
    public void testScrubRawStr1() {
        when(mockDecryptPassword.decrypt("secret")).thenReturn("secret");
        final String result = jvmWinSvcAcctPasswordScrubberService.scrub(TEST_RAW_STR_1);
        assertEquals(SCRUBBED_STR_1, result);
    }

    @Test
    public void testScrubRawStr2() {
        when(mockDecryptPassword.decrypt("secret")).thenReturn("secret");
        final String result = jvmWinSvcAcctPasswordScrubberService.scrub(TEST_RAW_STR_2);
        assertEquals(SCRUBBED_STR_2, result);
    }
}