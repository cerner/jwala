package com.cerner.jwala.service.custom.logging.impl;

import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.ssh.DecryptPassword;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.service.CollectionService;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Scrubs JVM Windows service account passwords from the logs
 *
 * Created by Jedd Cuison on 6/14/2017
 */
@Component
public class JvmWinSvcAcctPasswordScrubber extends PatternLayout {

    public static final String REPLACEMENT = "********";
    private final CollectionService<String> pwdCollectionService;
    private DecryptPassword decryptor;
    private final String [] includedMethodsArray = {"getExecRemoteCommandReturnInfo", "runExecCommand"};
    private final Set<String> includedMethods = new HashSet<>(Arrays.asList(includedMethodsArray));

    public JvmWinSvcAcctPasswordScrubber(final JvmPersistenceService jvmPersistenceService,
                                         final DecryptPassword decryptor,
                                         final CollectionService<String> pwdCollectionService) {
        this.decryptor = decryptor;
        this.pwdCollectionService = pwdCollectionService;

        // Populate the map of items to remove in the logs
        final List<Jvm> jvms = jvmPersistenceService.getJvms();
        for (final Jvm jvm: jvms) {
            if (StringUtils.isNotEmpty(jvm.getEncryptedPassword())) {
                this.pwdCollectionService.add(jvm.getEncryptedPassword());
            }
        }

        // Attach this layout to all appenders to intercept logging
        final Enumeration enumeration = Logger.getRootLogger().getAllAppenders();
        while (enumeration.hasMoreElements()) {
            ((Appender) enumeration.nextElement()).setLayout(this);
        }
    }

    @Override
    public String format(final LoggingEvent event) {

        // Since this class specifically handles JVM windows service passwords (svc_password) found in the logs,
        // we filter out events, classes and methods that we know won't log svc_password to minimize the impact
        // of this layout on the application's performance
        if (event.getLevel() != Level.DEBUG || isClassExcluded(event) || isMethodExcluded(event)) {
            return super.format(event);
        }

        final String msg = event.getMessage().toString();
        for (final String password : pwdCollectionService.getIterable()) {
            if (StringUtils.isNotEmpty(msg)) {
                final Throwable throwable =
                        event.getThrowableInformation() != null ? event.getThrowableInformation().getThrowable() : null;

                final LoggingEvent scrubbedEvent = new LoggingEvent(event.fqnOfCategoryClass, Logger.getLogger(
                        event.getLoggerName()), event.timeStamp, event.getLevel(), msg.replaceAll(
                        decryptor.decrypt(password), REPLACEMENT), throwable);

                // This scrubber was intended for JVM Windows service installation therefore we can assume that
                // an event only contains 1 sensitive item to be scrubbed
                // NOTE: Making this class generic was avoided for performance considerations
                return super.format(scrubbedEvent);
            }
        }

        return super.format(event);
    }

    /**
     * Checks if event class is excluded from svc_password masking
     * @param event the logging event
     * @return true if the event class is excluded from processing
     */
    private boolean isClassExcluded(final LoggingEvent event) {
        return !"JschServiceImpl".equalsIgnoreCase(event.getLocationInformation().getClassName());
    }

    /**
     * Checks if event method is excluded from svc_password masking
     * @param event the logging event
     * @return true if the event method is excluded from processing
     */
    private boolean isMethodExcluded(final LoggingEvent event) {
        return !includedMethods.isEmpty() && !includedMethods.contains(event.getLocationInformation().getMethodName());
    }

}
