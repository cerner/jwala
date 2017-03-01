package com.cerner.jwala.common;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cerner.jwala.common.configuration.TestExecutionProfile;

public class IntegrationTestRule implements TestRule {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationTestRule.class);

    private boolean shouldRunIntegrationTests = false;

    public IntegrationTestRule() {
        final String runTestTypes = System.getProperty(TestExecutionProfile.RUN_TEST_TYPES);
        if (runTestTypes != null) {
            shouldRunIntegrationTests = TestExecutionProfile.INTEGRATION.contains(runTestTypes);
        }
    }

    public Statement apply(final Statement base,
                           final Description description) {
        if (shouldRunIntegrationTests) {
            return base;
        } else {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    LOGGER.info("Skipping test because it's an integration test and they have not been configured to run: {}", description);
                }
            };
        }
    }
}
