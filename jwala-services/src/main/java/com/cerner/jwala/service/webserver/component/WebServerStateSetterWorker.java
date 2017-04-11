package com.cerner.jwala.service.webserver.component;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.domain.model.webserver.WebServerState;
import com.cerner.jwala.service.MessagingService;
import com.cerner.jwala.service.group.GroupStateNotificationService;
import com.cerner.jwala.service.state.InMemoryStateManagerService;
import com.cerner.jwala.service.webserver.WebServerService;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Sets a web server's state. This class is meant to be a spring bean wherein its "work" method pingWebServer
 * is ran asynchronously as a spun up thread.
 * <p>
 * Note!!! This class has be given its own package named "component" to denote it as a Spring component
 * that is subject to component scanning. In addition, this was also done to avoid the problem of it's unit test
 * which uses Spring config and component scanning from picking up other Spring components that it does not need
 * which also results to spring bean definition problems.
 * <p>
 * Created by Jedd Cuison on 6/25/2015.
 */
@Service
public class WebServerStateSetterWorker {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebServerStateSetterWorker.class);
    private static final String RESPONSE_NOT_OK_MSG = "Request for {0} failed with a response code of {1}!";

    private final InMemoryStateManagerService<Identifier<WebServer>, WebServerReachableState> inMemoryStateManagerService;
    private final WebServerService webServerService;
    private final MessagingService messagingService;
    private final GroupStateNotificationService groupStateNotificationService;

    private HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();


    private final Map<Identifier<WebServer>, WebServerReachableState> webServerLastPersistedStateMap = new ConcurrentHashMap<>();
    private final Map<Identifier<WebServer>, String> webServerLastPersistedErrorStatusMap = new ConcurrentHashMap<>();

    private final Set<Identifier<WebServer>> webServersToPing = new CopyOnWriteArraySet<>();

    public WebServerStateSetterWorker(@Qualifier("webServerInMemoryStateManagerService")
                                      final InMemoryStateManagerService<Identifier<WebServer>, WebServerReachableState> inMemoryStateManagerService,
                                      final WebServerService webServerService,
                                      final MessagingService messagingService,
                                      final GroupStateNotificationService groupStateNotificationService,
                                      @Qualifier("httpRequestFactory")
                                      final HttpComponentsClientHttpRequestFactory httpRequestFactory) {
        this.inMemoryStateManagerService = inMemoryStateManagerService;
        this.webServerService = webServerService;
        this.messagingService = messagingService;
        this.groupStateNotificationService = groupStateNotificationService;
        this.httpRequestFactory = httpRequestFactory;
    }

    /**
     * Ping the web server via http get.
     *
     * @param webServer the web server to ping.
     */
    @Async("webServerTaskExecutor")
    public void pingWebServer(final WebServer webServer) {

        if (!webServerCanBePinged(webServer)) {
            return;
        }

        synchronized (webServersToPing) {
            if (webServersToPing.contains(webServer.getId())) {
                LOGGER.debug("List of web servers currently being pinged: {}", webServersToPing);
                LOGGER.debug("Cannot ping web server {} since it is currently being pinged", webServer.getName(),
                             webServer);
                return;
            }
            webServersToPing.add(webServer.getId());
        }

        LOGGER.debug("Requesting {} for web server {}", webServer.getStatusUri(), webServer.getName());
        ClientHttpResponse response = null;
        try {
            final ClientHttpRequest request;
            request = httpRequestFactory.createRequest(webServer.getStatusUri(), HttpMethod.GET);
            response = request.execute();
            LOGGER.debug("Web server {} status code = {}", webServer.getName(), response.getStatusCode());

            if (HttpStatus.OK.equals(response.getStatusCode())) {
                setState(webServer, WebServerReachableState.WS_REACHABLE, StringUtils.EMPTY);
            } else {
                setState(webServer, WebServerReachableState.WS_UNREACHABLE,
                        MessageFormat.format(RESPONSE_NOT_OK_MSG, webServer.getStatusUri(), response.getStatusCode()));
            }
        } catch (final IOException e) {
            if (!WebServerReachableState.WS_UNREACHABLE.equals(webServer.getState())) {
                LOGGER.warn("Failed to ping {}!", webServer.getName(), e);
                setState(webServer, WebServerReachableState.WS_UNREACHABLE, StringUtils.EMPTY);
            }
        } finally {
            if (response != null) {
                response.close();
            }
            webServersToPing.remove(webServer.getId());
        }

    }

    /**
     * Checks if web server can be pinged and logs the reason if it cannot be pinged
     * @param webServer the web server
     * @return true if it can be pinged
     */
    private boolean webServerCanBePinged(final WebServer webServer) {
        if (WebServerReachableState.WS_NEW.equals(webServer.getState())) {
            return false;
        }

        if (isWebServerBusy(webServer)) {
            LOGGER.debug("Cannot ping web server {} since it is busy. Details: {}", webServer.getName(), webServer);
            return false;
        }
        return true;
    }

    /**
     * Checks if a web server is either starting or stopping or is down (in a failed state).
     *
     * @param webServer the webServer
     * @return true if web server is starting or stopping
     */
    private boolean isWebServerBusy(final WebServer webServer) {
        final WebServerReachableState webServerState = inMemoryStateManagerService.get(webServer.getId());
        return webServerState == WebServerReachableState.WS_START_SENT || webServerState == WebServerReachableState.WS_STOP_SENT;
    }

    /**
     * Sets the web server state if the web server is not starting or stopping.
     *
     * @param webServer               the web server
     * @param webServerReachableState {@link com.cerner.jwala.common.domain.model.webserver.WebServerReachableState}
     * @param msg                     a message
     */
    private void setState(final WebServer webServer, final WebServerReachableState webServerReachableState, final String msg) {
        if (!isWebServerBusy(webServer) && checkStateChangedAndOrMsgNotEmpty(webServer, webServerReachableState, msg)) {
            webServerService.updateState(webServer.getId(), webServerReachableState, msg);
            messagingService.send(new WebServerState(webServer.getId(), webServerReachableState, DateTime.now()));
            groupStateNotificationService.retrieveStateAndSend(webServer.getId(), WebServer.class);
        }
    }

    /**
     * Check if state has changed or if message is not empty. Sets webServerLastPersistedStateMap and
     * webServerLastPersistedErrorStatusMap.
     *
     * @param webServer               {@link WebServer}
     * @param webServerReachableState {@link WebServerReachableState}
     * @param msg                     a message (usually an error message)
     * @return true of the state is not the same compared to the previous state or if there's a message (error message)
     */
    private boolean checkStateChangedAndOrMsgNotEmpty(final WebServer webServer, final WebServerReachableState webServerReachableState, final String msg) {
        boolean stateChangedAndOrMsgNotEmpty = false;

        synchronized (webServerLastPersistedStateMap) {
            if (webServerHasNewState(webServer, webServerReachableState)) {
                webServerLastPersistedStateMap.put(webServer.getId(), webServerReachableState);
                stateChangedAndOrMsgNotEmpty = true;
            }

            if (webserverHasNewError(webServer, msg)) {
                webServerLastPersistedErrorStatusMap.put(webServer.getId(), msg);
                stateChangedAndOrMsgNotEmpty = true;
            }
        }
        return stateChangedAndOrMsgNotEmpty;
    }

    private boolean webServerHasNewState(final WebServer webServer, final WebServerReachableState webServerReachableState) {
        return !webServerLastPersistedStateMap.containsKey(webServer.getId()) ||
                !webServerLastPersistedStateMap.get(webServer.getId()).equals(webServerReachableState);
    }

    private boolean webserverHasNewError(WebServer webServer, String msg) {
        return StringUtils.isNotEmpty(msg) && (!webServerLastPersistedErrorStatusMap.containsKey(webServer.getId()) ||
                !webServerLastPersistedErrorStatusMap.get(webServer.getId()).equals(msg));
    }

}
