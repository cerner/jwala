package com.cerner.jwala.service.custom.logging.impl;

import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmBuilder;
import com.cerner.jwala.common.domain.model.ssh.DecryptPassword;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.service.impl.spring.component.JvmWinSvcPwdCollectionServiceImpl;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Created by Jedd Cuison on 6/15/2017
 */
@RunWith(PowerMockRunner.class)
public class JvmWinSvcAcctPasswordScrubberTest {

    private JvmWinSvcAcctPasswordScrubber jvmWinSvcAcctPasswordScrubber;

    private JvmPersistenceService mockJvmPersistenceService;

    private DecryptPassword mockDecryptPassword;

    private LocationInfo mockLocationInfo;

    private LoggingEvent mockLoggingEvent;

    @Before
    public void setup() {
        final List<Jvm> jvms = new ArrayList<>();
        final Jvm jvm = new JvmBuilder().setEncryptedPassword("encrypted-password").build();
        jvms.add(jvm);

        mockJvmPersistenceService = mock(JvmPersistenceService.class);
        when(mockJvmPersistenceService.getJvms()).thenReturn(jvms);

        mockDecryptPassword = mock(DecryptPassword.class);
        when(mockDecryptPassword.decrypt("encrypted-password")).thenReturn("password");

        mockLocationInfo = mock(LocationInfo.class);
        when(mockLocationInfo.getClassName()).thenReturn("JschServiceImpl");
        when(mockLocationInfo.getMethodName()).thenReturn("runExecCommand");
        mockLoggingEvent = mock(LoggingEvent.class);
        when(mockLoggingEvent.getLocationInformation()).thenReturn(mockLocationInfo);

        when(mockLoggingEvent.getMessage()).thenReturn("install_service user password");
        when(mockLoggingEvent.getLoggerName()).thenReturn("loggerName");

        jvmWinSvcAcctPasswordScrubber = new JvmWinSvcAcctPasswordScrubber(mockJvmPersistenceService, mockDecryptPassword,
                new JvmWinSvcPwdCollectionServiceImpl());
    }

    @Test
    public void testFormat() {
        when(mockLoggingEvent.getLevel()).thenReturn(Level.DEBUG);
        assertEquals("install_service user ********\r\n", jvmWinSvcAcctPasswordScrubber.format(mockLoggingEvent));
    }

    @Test
    public void testFormatErrorLevel() {
        when(mockLoggingEvent.getLevel()).thenReturn(Level.ERROR);
        jvmWinSvcAcctPasswordScrubber.format(mockLoggingEvent);
        verify(mockLoggingEvent, never()).getMessage();
    }

    @Test
    public void testFormatExcludedClass() {
        when(mockLocationInfo.getClassName()).thenReturn("SomeClass");
        jvmWinSvcAcctPasswordScrubber.format(mockLoggingEvent);
        verify(mockLoggingEvent, never()).getMessage();
    }

    @Test
    public void testFormatExcludedMethod() {
        when(mockLocationInfo.getMethodName()).thenReturn("SomeMethod");
        jvmWinSvcAcctPasswordScrubber.format(mockLoggingEvent);
        verify(mockLoggingEvent, never()).getMessage();
    }
}