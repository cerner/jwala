package com.cerner.jwala.common.request.jvm;

import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static junit.framework.Assert.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by Jedd Cuison on 4/18/2017
 */
public class ControlJvmRequestFactoryTest {

    @Mock
    private Jvm mockJvm;

    private ControlJvmRequest req;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void testCreate() throws Exception {
        req = ControlJvmRequestFactory.create(JvmControlOperation.START, mockJvm);
        assertEquals(req.getClass(), StartServiceControlJvmRequest.class);

        req = ControlJvmRequestFactory.create(JvmControlOperation.STOP, mockJvm);
        assertEquals(req.getClass(), StopServiceControlJvmRequest.class);

        req = ControlJvmRequestFactory.create(JvmControlOperation.DELETE_SERVICE, mockJvm);
        assertEquals(req.getClass(), DeleteServiceControlJvmRequest.class);

        req = ControlJvmRequestFactory.create(JvmControlOperation.DEPLOY_JVM_ARCHIVE, mockJvm);
        assertEquals(req.getClass(), DeployArchiveControlJvmRequest.class);

        req = ControlJvmRequestFactory.create(JvmControlOperation.INSTALL_SERVICE, mockJvm);
        assertEquals(req.getClass(),  InstallServiceControlJvmRequest.class);

        req = ControlJvmRequestFactory.create(JvmControlOperation.HEAP_DUMP, mockJvm);
        assertEquals(req.getClass(), HeapDumpControlJvmRequest.class);
    }

    @Test(expected = UnsupportedJvmControlOperationException.class)
    public void testUnsupportedRequest() {
        ControlJvmRequestFactory.create(JvmControlOperation.BACK_UP, mockJvm);
    }

}