package com.cerner.jwala.ws.rest.v1.service.balancemanager.impl;

import com.cerner.jwala.common.domain.model.balancermanager.BalancerManagerState;
import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.service.balancermanager.BalancerManagerService;
import com.cerner.jwala.ws.rest.v1.provider.AuthUser;
import com.cerner.jwala.ws.rest.v1.service.balancermanager.BalancerManagerServiceRest;
import com.cerner.jwala.ws.rest.v1.service.balancermanager.impl.BalancerManagerServiceRestImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.ws.rs.core.Response;

import static org.jgroups.util.Util.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Unit test for {@link BalancerManagerServiceRest}
 *
 * Created by Jedd Cuison on 10/25/2016.
 */
public class BalancerManagerServiceRestImplTest {

    @Mock
    private BalancerManagerService mockBalancerManagerService;

    @Mock
    private AuthUser mockAuthUser;

    @Mock
    private BalancerManagerState mockBalancerManagerState;

    private BalancerManagerServiceRest balancerManagerServiceRest;

    @Before
    public void setup() {
        initMocks(this);
        when(mockAuthUser.getUserName()).thenReturn("user");
        balancerManagerServiceRest = new BalancerManagerServiceRestImpl(mockBalancerManagerService);
    }

    @Test
    public void testDrainUserGroup() {
        final Response response = balancerManagerServiceRest.drainUserGroup("Group Z", "zWebServer", mockAuthUser);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testDrainUserGroupWithInternalException() {
        when(mockBalancerManagerService.drainUserGroup(anyString(), anyString(), anyString()))
                .thenThrow(new InternalErrorException(FaultType.IO_EXCEPTION, "IO Exception"));
        final Response response = balancerManagerServiceRest.drainUserGroup("zGroup", "zWebServer", mockAuthUser);
        assertEquals(500, response.getStatus());
    }

    @Test
    public void testDrainUserWebServer() {
        balancerManagerServiceRest.drainUserWebServer("zGroup", "zWebServer", "xJvm, yJVM, zJvm", mockAuthUser);
    }

    @Test
    public void testDrainUserWebServerWithInternalException() {
        when(mockBalancerManagerService.drainUserWebServer(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new InternalErrorException(FaultType.IO_EXCEPTION, "IO Exception"));
        final Response response = balancerManagerServiceRest.drainUserWebServer("zGroup", "zWebServer", "xJvm, yJVM, zJvm", mockAuthUser);
        assertEquals(500, response.getStatus());
    }

    @Test
    public void testDrainUserJvm() {
        final Response response = balancerManagerServiceRest.drainUserJvm("zJvm", mockAuthUser);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testDrainUserGroupJvm() {
        final Response response = balancerManagerServiceRest.drainUserGroupJvm("zGroup", "zJvm", mockAuthUser);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testDrainUserGroupJvmWithInternalException() {
        when(mockBalancerManagerService.drainUserGroupJvm(anyString(), anyString(), anyString()))
                .thenThrow(new InternalErrorException(FaultType.IO_EXCEPTION, "IO Exception"));
        final Response response = balancerManagerServiceRest.drainUserGroupJvm("zGroup", "zJvm", mockAuthUser);
        assertEquals(500, response.getStatus());
    }

    @Test
    public void testGetGroupDrainStatus() {
        when(mockBalancerManagerService.getGroupDrainStatus(anyString(),anyString())).thenReturn(mockBalancerManagerState);
        final Response response = balancerManagerServiceRest.getGroupDrainStatus("zGroup", mockAuthUser);
        assertEquals(200, response.getStatus());
    }
}

