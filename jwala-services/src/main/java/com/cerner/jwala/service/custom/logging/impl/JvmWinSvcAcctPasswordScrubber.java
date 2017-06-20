package com.cerner.jwala.service.custom.logging.impl;

import com.cerner.jwala.common.domain.model.ssh.DecryptPassword;
import com.cerner.jwala.service.impl.spring.component.JvmWinSvcPwdCollectionServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Scrubs JVM Windows service account passwords from the logs
 *
 * Created by Jedd Cuison on 6/14/2017
 */
public class JvmWinSvcAcctPasswordScrubber extends PatternLayout {

    private static final String REPLACEMENT = "********";

    private DecryptPassword decryptor = new DecryptPassword();
    private Set<Level> levels = Collections.EMPTY_SET;
    private Set<String> applicableClasses = Collections.EMPTY_SET;
    private Set<String> applicableMethods = Collections.EMPTY_SET;

    @Override
    public String format(final LoggingEvent event) {

        // Since this class specifically handles JVM windows service passwords (svc_password) found in the logs,
        // we filter out events, classes and methods that we know won't log svc_password to minimize the impact
        // of this layout on the application's performance
        if (CollectionUtils.isEmpty(JvmWinSvcPwdCollectionServiceImpl.getIterable()) || isLevelExcluded(event)
                || isClassExcluded(event)) {
            return super.format(event);
        }

        final String msg = event.getMessage().toString();
        if (StringUtils.isNotEmpty(msg)) {
            for (final String password : JvmWinSvcPwdCollectionServiceImpl.getIterable()) {
                final String maskedMsg = msg.replaceAll(decryptor.decrypt(password), REPLACEMENT);
                if (!msg.equalsIgnoreCase(maskedMsg)) {
                    final Throwable throwable =
                            event.getThrowableInformation() != null ? event.getThrowableInformation().getThrowable() : null;

                    final LoggingEvent scrubbedEvent = new LoggingEvent(event.fqnOfCategoryClass, Logger.getLogger(
                            event.getLoggerName()), event.timeStamp, event.getLevel(), maskedMsg, throwable);

                    // This scrubber was intended for JVM Windows service installation therefore we can assume that
                    // an event only contains 1 sensitive item to be scrubbed
                    // NOTE: Making this class generic was avoided for performance considerations
                    return super.format(scrubbedEvent);
                }
            }
        }

        return super.format(event);
    }

    /**
     * Checks if event level is excluded from svc_password masking
     * @param event the logging event
     * @return true if the event level is excluded from processing
     */
    private boolean isLevelExcluded(final LoggingEvent event) {
        return  !levels.isEmpty() && !levels.contains(event.getLevel());
    }

    /**
     * Checks if event class is excluded from svc_password masking
     * @param event the logging event
     * @return true if the event class is excluded from processing
     */
    private boolean isClassExcluded(final LoggingEvent event) {
        return  !applicableClasses.isEmpty() && !applicableClasses.contains(event.getLocationInformation().getClassName());
    }

    /**
     * Checks if event class is excluded from svc_password masking
     * @param event the logging event
     * @return true if the event class is excluded from processing
     */
    private boolean isMethodExcluded(final LoggingEvent event) {
        return  !applicableMethods.isEmpty() && !applicableMethods.contains(event.getLocationInformation().getMethodName());
    }

    public void setLevels(final String levels) {
        if (StringUtils.isNotEmpty(levels)) {
            final String [] levelArray = levels.split(",");
            for (final String level: levelArray) {
                this.levels.add(Level.toLevel(level));
            }
        }
    }

    public void setApplicableClasses(final String applicableClasses) {
        if (StringUtils.isNotEmpty(applicableClasses)) {
            this.applicableClasses = new HashSet<>(Arrays.asList(applicableClasses.split(",")));
        }
    }

    public void setApplicableMethods(final String getApplicableMethods) {
        if (StringUtils.isNotEmpty(getApplicableMethods)) {
            this.applicableMethods = new HashSet<>(Arrays.asList(getApplicableMethods.split(",")));
        }
    }
}
