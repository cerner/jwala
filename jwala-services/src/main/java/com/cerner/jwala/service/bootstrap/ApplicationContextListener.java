package com.cerner.jwala.service.bootstrap;

import com.cerner.jwala.service.jvm.JvmService;
import com.cerner.jwala.service.media.MediaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

/**
 * The application startup listener that checks for upgrades.
 * <p>
 * The initial use case for this class was to check for backwards compatibility for the JDK media. The addition
 * of the JDK media to the JVM configuration introduced a dependency that needs to be fulfilled in order for the
 * JVM generation to work. For deployed instances of the application that are not configured with the JDK media, this
 * startup listener will configure the application with a default JDK for the JVMs.
 */
public class ApplicationContextListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(ApplicationContextListener.class);
    private static final String JWALA_BYPASS_JDK_MEDIA_BOOTSTRAP_CONFIGURATION = "jwala.jdk.media.bootstrap.configuration.bypass";

    @Autowired
    private MediaService mediaService;

    @Autowired
    private JvmService jvmService;

    /**
     * Implementation of the spring event listener interface.
     *
     * @param event the spring event from the application
     */
    @EventListener
    public void handleEvent(ApplicationEvent event) {
        // checking for start up event
        // order of events is BrokerAvailabilityEvent -> ContextRefreshedEvent[parent=null] -> ContextRefreshedEvent[with non-null parent]
        // so wait until the latest event is received: ContextRefreshedEvent[with non-null parent]

        // skip the BrokerAvailabilityEvent, and ignore all other events (SessionConnectedEvent, ServletRequestHandledEvent, ContextClosedEvent, etc.)
        if (!(event instanceof ContextRefreshedEvent)) {
            LOGGER.debug("Expecting ContextRefreshedEvent. Skipping.");
            return;
        }

        LOGGER.info("Received ContextRefreshedEvent {}", event);

        ContextRefreshedEvent crEvent = (ContextRefreshedEvent) event;
        final ApplicationContext applicationContext = crEvent.getApplicationContext();
        // skip the ContextRefreshedEvent[parent=null] but check for non-null context first
        if (null == applicationContext) {
            LOGGER.debug("Expecting non-null ApplicationContext. Skipping.");
            return;
        }
        if (null == applicationContext.getParent()) {
            LOGGER.debug("Expecting non-null ApplicationContext parent. Skipping.");
            return;
        }

        processBootstrapConfiguration();
    }

    /**
     * Run the upgrade steps
     */
    private void processBootstrapConfiguration() {
        LOGGER.info("Begin bootstrap configuration");
        LOGGER.info("End bootstrap configuration");
    }
}
