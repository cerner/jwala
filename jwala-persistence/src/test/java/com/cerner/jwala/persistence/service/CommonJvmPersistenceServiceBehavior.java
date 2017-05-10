package com.cerner.jwala.persistence.service;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.media.Media;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.request.jvm.CreateJvmRequest;
import com.cerner.jwala.common.request.jvm.UpdateJvmRequest;

import java.util.Collections;

public class CommonJvmPersistenceServiceBehavior {

    private final JvmPersistenceService jvmPersistenceService;
    private boolean updateJvmPassword = true;

    public CommonJvmPersistenceServiceBehavior(final JvmPersistenceService jvmPersistenceService) {
        this.jvmPersistenceService = jvmPersistenceService;
    }

    public Jvm createJvm(final String aJvmName,
                         final String aHostName,
                         final Integer aHttpPort,
                         final Integer aHttpsPort,
                         final Integer aRedirectPort,
                         final Integer aShutdownPort,
                         final Integer aAjpPort,
                         final String aUserId,
                         final Path aStatusPath,
                         final String aSystemProperties,
                         final String aUserName,
                         final String anEncryptedPassword,
                         final Identifier<Media> jdkMediaId,
                         final Identifier<Media> tomcatMediaId) {

        final CreateJvmRequest createJvmRequest = createCreateJvmRequest(aJvmName,
                aHostName,
                aHttpPort,
                aHttpsPort,
                aRedirectPort,
                aShutdownPort,
                aAjpPort,
                aUserId,
                aStatusPath,
                aSystemProperties,
                aUserName,
                anEncryptedPassword,
                jdkMediaId,
                tomcatMediaId);

        return jvmPersistenceService.createJvm(createJvmRequest);
    }

    public Jvm updateJvm(final Identifier<Jvm> aJvmId,
                         final String aNewJvmName,
                         final String aNewHostName,
                         final Integer aNewHttpPort,
                         final Integer aNewHttpsPort,
                         final Integer aNewRedirectPort,
                         final Integer aNewShutdownPort,
                         final Integer aNewAjpPort,
                         final String aUserId,
                         final Path aStatusPath,
                         final String aSystemProperties,
                         final String aUserName,
                         final String anEncryptedPassword,
                         final Identifier<Media> aJdkMediaId,
                         final Identifier<Media> aTomcatMediaId) {

        final UpdateJvmRequest updateJvmRequest = createUpdateJvmRequest(aJvmId,
                aNewJvmName,
                aNewHostName,
                aNewHttpPort,
                aNewHttpsPort,
                aNewRedirectPort,
                aNewShutdownPort,
                aNewAjpPort,
                aUserId,
                aStatusPath,
                aSystemProperties,
                aUserName,
                anEncryptedPassword,
                aJdkMediaId,
                aTomcatMediaId);

        return jvmPersistenceService.updateJvm(updateJvmRequest, updateJvmPassword);
    }

    protected CreateJvmRequest createCreateJvmRequest(final String aJvmName,
                                                      final String aJvmHostName,
                                                      final Integer httpPort,
                                                      final Integer httpsPort,
                                                      final Integer redirectPort,
                                                      final Integer shutdownPort,
                                                      final Integer ajpPort,
                                                      final String aUserId,
                                                      final Path aStatusPath,
                                                      final String aSystemProperties,
                                                      final String aUserName,
                                                      final String anEncryptedPassword,
                                                      final Identifier<Media> jdkMediaId,
                                                      final Identifier<Media> tomcatMediaId) {

        return new CreateJvmRequest(aJvmName,
                aJvmHostName,
                httpPort,
                httpsPort,
                redirectPort,
                shutdownPort,
                ajpPort,
                aStatusPath,
                aSystemProperties,
                aUserName,
                anEncryptedPassword,
                jdkMediaId,
                tomcatMediaId);
    }

    protected UpdateJvmRequest createUpdateJvmRequest(final Identifier<Jvm> aJvmId,
                                                      final String aNewJvmName,
                                                      final String aNewHostName,
                                                      final Integer aNewHttpPort,
                                                      final Integer aNewHttpsPort,
                                                      final Integer aNewRedirectPort,
                                                      final Integer aNewShutdownPort,
                                                      final Integer aNewAjpPort,
                                                      final String aUserId,
                                                      final Path aStatusPath,
                                                      final String systemProperties,  
                                                      final String aUserName,
                                                      final String anEncryptedPassword,
                                                      final Identifier<Media> aJdkMediaId,
                                                      final Identifier<Media> aTomcatMediaId) {

        return new UpdateJvmRequest(aJvmId,
                aNewJvmName,
                aNewHostName,
                Collections.<Identifier<Group>>emptySet(),
                aNewHttpPort,
                aNewHttpsPort,
                aNewRedirectPort,
                aNewShutdownPort,
                aNewAjpPort,
                aStatusPath,
                systemProperties, 
                aUserName,
                anEncryptedPassword,
                aJdkMediaId,
                aTomcatMediaId);
    }

    public void setUpdateJvmPassword(boolean updateJvmPassword) {
        this.updateJvmPassword = updateJvmPassword;
    }
}