package com.cerner.jwala.service.jvm.state;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.service.jvm.JvmStateService;
import org.apache.catalina.LifecycleState;
import org.apache.commons.lang3.StringUtils;
import org.jgroups.Message;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.persistence.NoResultException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Unit test for {@link JvmStateReceiverAdapter}
 *
 * Created by Jedd Cuison on 9/1/2016.
 */
public class JvmStateReceiverAdapterTest {

    private JvmStateReceiverAdapter jvmStateReceiverAdapter;

    private Message msg;

    @Mock
    private JvmStateService mockJvmStateService;

    @Mock
    private JvmPersistenceService mockJvmPersistenceService;

    @Before
    public void setup() {
        initMocks(this);
        jvmStateReceiverAdapter = new JvmStateReceiverAdapter(mockJvmStateService, mockJvmPersistenceService);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testReceive() throws Exception {
        final Identifier<Jvm> jvmId = new Identifier<>("1");
        Jvm jvm = new Jvm(jvmId, "jvm-name");

        final Map<Object, Object> serverInfoMap = new HashMap();
        serverInfoMap.put(JvmStateReceiverAdapter.NAME_KEY, jvm.getJvmName());
        serverInfoMap.put(JvmStateReceiverAdapter.STATE_KEY, LifecycleState.STOPPING);

        msg = new Message();
        msg.setObject(serverInfoMap);

        when (mockJvmPersistenceService.findJvmByExactName(jvm.getJvmName())).thenReturn(jvm);
        jvmStateReceiverAdapter.receive(msg);
        verify(mockJvmStateService).updateState(eq(jvm), eq(JvmState.JVM_STOPPING), eq(StringUtils.EMPTY));
    }


    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveWithRuntimeException() throws Exception {
        final Identifier<Jvm> jvmId = new Identifier<>("1");
        Jvm jvm = new Jvm(jvmId, "jvm-name");

        final Map<Object, Object> serverInfoMap = new HashMap();
        serverInfoMap.put(JvmStateReceiverAdapter.NAME_KEY, jvm.getJvmName());
        serverInfoMap.put(JvmStateReceiverAdapter.STATE_KEY, LifecycleState.STOPPING);

        msg = new Message();
        msg.setObject(serverInfoMap);

        when (mockJvmPersistenceService.findJvmByExactName(jvm.getJvmName())).thenThrow(new NoResultException(""));

        jvmStateReceiverAdapter.receive(msg);
        verify(mockJvmStateService, never()).updateState(eq(jvm), eq(JvmState.JVM_STOPPING), eq(StringUtils.EMPTY));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testReceiveNullId() {
        final Identifier<Jvm> jvmId = new Identifier<>("1");
        Jvm jvm = new Jvm(jvmId, "jvm-name");

        final Map<Object, Object> serverInfoMap = new HashMap();
        serverInfoMap.put(JvmStateReceiverAdapter.NAME_KEY, jvm.getJvmName());
        serverInfoMap.put(JvmStateReceiverAdapter.STATE_KEY, LifecycleState.STOPPING);

        msg = new Message();
        msg.setObject(serverInfoMap);

        when (mockJvmPersistenceService.findJvmByExactName(jvm.getJvmName())).thenReturn(null);

        jvmStateReceiverAdapter.receive(msg);
        verify(mockJvmStateService, never()).updateState(eq(jvm), eq(JvmState.JVM_STOPPING), eq(StringUtils.EMPTY));
    }

}