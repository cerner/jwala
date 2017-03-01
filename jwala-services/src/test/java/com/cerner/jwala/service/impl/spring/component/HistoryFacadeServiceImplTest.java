package com.cerner.jwala.service.impl.spring.component;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.persistence.jpa.domain.JpaGroup;
import com.cerner.jwala.persistence.jpa.domain.JpaHistory;
import com.cerner.jwala.persistence.jpa.type.EventType;
import com.cerner.jwala.service.HistoryFacadeService;
import com.cerner.jwala.service.HistoryService;
import com.cerner.jwala.service.MessagingService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;

import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Unit test for {@link HistoryFacadeServiceImpl}
 *
 * Created by Jedd Cuison on 11/9/2016
 */
public class HistoryFacadeServiceImplTest {

    @Mock
    private HistoryService mockHistoryService;

    @Mock
    private MessagingService mockMessagingService;

    @Mock
    private JpaHistory mockJpaHistory;

    @Mock
    private JpaGroup jpaGroup;

    private HistoryFacadeService historyFacadeService;

    final Group someGroup = new Group(new Identifier<Group>(1L), "someGroup");

    @Before
    @SuppressWarnings("all")
    public void setup() {
        initMocks(this);
        historyFacadeService = new HistoryFacadeServiceImpl(mockHistoryService, mockMessagingService);
        when(jpaGroup.getName()).thenReturn("mockGroup");
        when(mockJpaHistory.getGroup()).thenReturn(jpaGroup);
        when(mockHistoryService.createHistory(anyString(), anyList(), anyString(), eq(EventType.SYSTEM_INFO), anyString()))
                .thenReturn(Arrays.asList(mockJpaHistory));
    }

    @Test
    @SuppressWarnings("all")
    public void testWrite() throws Exception {
        historyFacadeService.write("someServer", Arrays.asList(someGroup), "some event", EventType.SYSTEM_INFO, "someUser");
    }

    @Test
    public void testWrite2() throws Exception {
        historyFacadeService.write("someServer", someGroup, "some event", EventType.SYSTEM_INFO, "someUser");
    }

}
