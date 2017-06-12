package com.cerner.jwala.service.custom.logging;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;

import java.util.*;

/**
 * Masks the Windows Service Password (svc_password)
 * Created by Jedd Cuison on 6/6/2017
 */
public class WinSvcPasswordMaskingLayout extends PatternLayout {

    private String forClass;
    private Set<String> forMethods; // Search is faster for Set

    @Override
    public String format(final LoggingEvent event) {

        // Since this class specifically handles windows service passwords (svc_password) found in the logs,
        // we filter out events, classes and methods that we know won't log svc_password to minimize the impact
        // of this layout on the application's performance
        if (event.getLevel() != Level.DEBUG || isClassExcluded(event) || isMethodExcluded(event)) {
            return super.format(event);
        }

        final String msg = event.getRenderedMessage();
        String maskedMsg = StringUtils.EMPTY;

        // This layout is specific for svc_password masking therefore the code is straight forward and not generic
        if (StringUtils.indexOf(msg, "install-service.sh") > 0) {
            int index = msg.lastIndexOf(' ', msg.lastIndexOf(' ') - 1);
            maskedMsg = msg.substring(0, index) + " ******** \"";
        } else if (StringUtils.contains(msg, "set svc_password=")) {
            maskedMsg = "set svc_password=********";
        }

        if (StringUtils.isNotEmpty(maskedMsg)) {
            final Throwable throwable =
                    event.getThrowableInformation() != null ? event.getThrowableInformation().getThrowable() : null;

            final LoggingEvent maskedEvent = new LoggingEvent(event.fqnOfCategoryClass, Logger.getLogger(event.getLoggerName()),
                    event.timeStamp, event.getLevel(), maskedMsg, throwable);

            return super.format(maskedEvent);
        }
        return super.format(event);
    }

    /**
     * Checks if event class is excluded from svc_password masking
     * @param event the logging event
     * @return true if the event class is excluded from processing
     */
    private boolean isClassExcluded(final LoggingEvent event) {
        return StringUtils.isNotEmpty(forClass) &&
                !forClass.equalsIgnoreCase(event.getLocationInformation().getClassName());
    }

    /**
     * Checks if event method is excluded from svc_password masking
     * @param event the logging event
     * @return true if the event method is excluded from processing
     */
    private boolean isMethodExcluded(final LoggingEvent event) {
        return !forMethods.isEmpty() && !forMethods.contains(event.getLocationInformation().getMethodName());
    }

    public void setForClass(final String forClass) {
        this.forClass = forClass;
    }

    @SuppressWarnings("unchecked")
    public void setForMethods(final String forMethods) {
        if (StringUtils.isNotEmpty(forMethods)) {
            this.forMethods = new HashSet<>(Arrays.asList(forMethods.split(",")));
            return;
        }
        this.forMethods = Collections.EMPTY_SET;
    }
}
