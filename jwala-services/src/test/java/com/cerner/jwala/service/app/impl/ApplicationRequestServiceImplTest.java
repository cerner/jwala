package com.cerner.jwala.service.app.impl;

import com.cerner.jwala.commandprocessor.impl.jsch.JschBuilder;
import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.app.ApplicationControlOperation;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.request.app.ControlApplicationRequest;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.exception.RemoteCommandFailureException;
import com.cerner.jwala.service.app.ApplicationCommandService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Jedd Cuison on 9/30/2015.
 */
public class ApplicationRequestServiceImplTest {

    private ApplicationCommandService applicationCommandService;
    private SshConfiguration sshConfiguration;
    private JschBuilder jschBuilder;

    @Before
    public void setup() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources");
        sshConfiguration = mock(SshConfiguration.class);
        jschBuilder = mock(JschBuilder.class);
        applicationCommandService = new ApplicationCommandServiceImpl(sshConfiguration, jschBuilder);
    }

    @After
    public void tearDown(){
        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test(expected = RemoteCommandFailureException.class)
    public void testSecureCopyConfFile() throws CommandFailureException{

        final Application mockApp = mock(Application.class);
        final Identifier<Application> appId = new Identifier<>(11L);
        when(mockApp.getId()).thenReturn(appId);
        when(sshConfiguration.getUserName()).thenReturn("user");
        when(sshConfiguration.getEncryptedPassword()).thenReturn("==oops==".toCharArray());
        when(sshConfiguration.getPort()).thenReturn(22);
        ControlApplicationRequest appRequest = new ControlApplicationRequest(appId, ApplicationControlOperation.SCP);
        applicationCommandService.controlApplication(appRequest, mockApp, "testHost", "source path", "dest path");
    }

}
