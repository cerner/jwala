package com.cerner.jwala.service.custom.logging;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import org.junit.*;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link WinSvcPasswordMaskingLayout}
 * Created by Jedd Cuison on 6/7/2017
 */
public class WinSvcPasswordMaskingLayoutTest {

    private static final String LOG_MSG_WITH_PASSWORD_1 = "Executing command \"~/.jwala/SOME-SERVER" +
            "/install-service.sh SOME-SERVER D:/ctp/app/instances " +
            "apache-tomcat-7.0.55 \"the-user\" the-password \"";
    private static final String LOG_MSG_WITH_PASSWORD_2 = "D:\\cygwin64\\home\\N9SFGLabTomcatAdmin>set svc_password=the-password ";
    private static final String UNMASKED_PASSWORD_SNIPPET_1 = "\"the-user\" the-password \"";
    private static final String UNMASKED_PASSWORD_SNIPPET_2 = "svc_password=the-password ";
    public static final String PASSWORD_MASKING_RESULT_1 = "\"the-user\" ******** ";
    public static final String PASSWORD_MASKING_RESULT_2 = "svc_password=********";

    private static String logMsg;

    private Logger logger;

    @Before
    public void init() {
        final TestAppender testAppender = new TestAppender();
        final WinSvcPasswordMaskingLayout layout = new WinSvcPasswordMaskingLayout();
        layout.setForClass(WinSvcPasswordMaskingLayoutTest.class.getName());
        layout.setForMethods("testMaskSvcPassword,testLogLevelNotDebug");
        testAppender.setLayout(layout);

        logger = Logger.getLogger(WinSvcPasswordMaskingLayoutTest.class);
        logger.addAppender(testAppender);
        logger.getAllAppenders();
    }

    @After
    public void destroy() throws IOException {
        logMsg = null;
    }

    @Test
    public void testMaskSvcPassword() {
        logger.debug(LOG_MSG_WITH_PASSWORD_1);
        assertTrue(logMsg.contains(PASSWORD_MASKING_RESULT_1));
        logger.debug(LOG_MSG_WITH_PASSWORD_2);
        assertTrue(logMsg.contains(PASSWORD_MASKING_RESULT_2));
    }

    @Test
    public void testLogLevelNotDebug() {
        logger.info(LOG_MSG_WITH_PASSWORD_1);
        assertTrue(logMsg.contains(UNMASKED_PASSWORD_SNIPPET_1));
        logger.error(LOG_MSG_WITH_PASSWORD_2);
        assertTrue(logMsg.contains(UNMASKED_PASSWORD_SNIPPET_2));
    }

    /**
     * This method is not in log4j.xml therefore masking will not be applied
     */
    @Test
    public void testMethodExcludedFromMasking() {
        logger.debug(LOG_MSG_WITH_PASSWORD_1);
        assertTrue(logMsg.contains(UNMASKED_PASSWORD_SNIPPET_1));
        logger.debug(LOG_MSG_WITH_PASSWORD_2);
        assertTrue(logMsg.contains(UNMASKED_PASSWORD_SNIPPET_2));
    }

    /**
     *  The class used in this test is not in log4j.xml therefore masking will not be applied
     */
    @Test
    public void testClassExcludedFromMasking() {
        final SomeClassThatLogs someClassThatLogs = new SomeClassThatLogs(logger);
        someClassThatLogs.writeLog1();
        assertTrue(logMsg.contains(UNMASKED_PASSWORD_SNIPPET_1));
        someClassThatLogs.writeLog2();
        assertTrue(logMsg.contains(UNMASKED_PASSWORD_SNIPPET_2));
    }

    /**
     * A class used to test class "svc_password masking exclusion"
     */
    private static class SomeClassThatLogs {

        private final Logger logger;

        public SomeClassThatLogs(final Logger logger) {
            this.logger = logger;
        }

        void writeLog1() {
            logger.debug(LOG_MSG_WITH_PASSWORD_1);
        }

        void writeLog2() {
            logger.debug(LOG_MSG_WITH_PASSWORD_2);
        }
    }

    /**
     * An appender to facilitate in the testing of log messages
     */
    private static class TestAppender extends AppenderSkeleton {

        @Override
        protected void append(final LoggingEvent event) {
            logMsg = getLayout().format(event);
        }

        @Override
        public void close() {

        }

        @Override
        public boolean requiresLayout() {
            return true;
        }
    }

}