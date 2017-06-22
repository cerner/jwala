package com.cerner.jwala.service.impl.spring.component;

import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by Jedd Cuison on 6/20/2017
 */
public class JvmWinSvcPwdStoreServiceImplTest {

    private JvmWinSvcPwdStoreServiceImpl jvmWinSvcPwdStoreService;

    @Mock
    private JvmPersistenceService mockJvmPersistenceService;

    @Mock
    private Jvm mockJvm;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        final List<Jvm> mockJvms = new ArrayList<>();
        when(mockJvm.getEncryptedPassword()).thenReturn("$#$%#$%$#^&&==");
        mockJvms.add(mockJvm);
        when(mockJvmPersistenceService.getJvms()).thenReturn(mockJvms);
        jvmWinSvcPwdStoreService = new JvmWinSvcPwdStoreServiceImpl(mockJvmPersistenceService);
    }

    @Test
    public void testAdd() throws Exception {
        jvmWinSvcPwdStoreService.add("@@@!!!@@@@!!==");
        assertEquals(2, CollectionUtils.size(jvmWinSvcPwdStoreService.getIterable()));
    }

    @Test
    public void testRemove() throws Exception {
        jvmWinSvcPwdStoreService.remove("$#$%#$%$#^&&==");
        assertEquals(0, CollectionUtils.size(jvmWinSvcPwdStoreService.getIterable()));
    }

    @Test
    public void testClear() throws Exception {
        jvmWinSvcPwdStoreService.clear();
        assertEquals(0, CollectionUtils.size(jvmWinSvcPwdStoreService.getIterable()));
    }
}