package com.cerner.jwala.service.balancermanager.impl;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.balancermanager.BalancerManagerState;
import com.cerner.jwala.common.domain.model.balancermanager.WorkerStatusType;
import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.exception.ApplicationException;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.persistence.jpa.type.EventType;
import com.cerner.jwala.service.HistoryFacadeService;
import com.cerner.jwala.service.app.ApplicationService;
import com.cerner.jwala.service.balancermanager.BalancerManagerService;
import com.cerner.jwala.service.balancermanager.impl.xml.data.Manager;
import com.cerner.jwala.service.group.GroupService;
import com.cerner.jwala.service.jvm.JvmService;
import com.cerner.jwala.service.webserver.WebServerService;
import com.cerner.jwala.service.webserver.component.ClientFactoryHelper;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.sis.internal.jdk7.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class BalancerManagerServiceImpl implements BalancerManagerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BalancerManagerServiceImpl.class);

    private final GroupService groupService;
    private final ApplicationService applicationService;
    private final WebServerService webServerService;
    private final JvmService jvmService;

    private final ClientFactoryHelper clientFactoryHelper;
    private final HistoryFacadeService historyFacadeService;
    private final BalancerManagerHtmlParser balancerManagerHtmlParser;
    private final BalancerManagerXmlParser balancerManagerXmlParser;
    private final BalancerManagerHttpClient balancerManagerHttpClient;

    public BalancerManagerServiceImpl(final GroupService groupService,
                                      final ApplicationService applicationService,
                                      final WebServerService webServerService,
                                      final JvmService jvmService,
                                      final ClientFactoryHelper clientFactoryHelper,
                                      final BalancerManagerHtmlParser balancerManagerHtmlParser,
                                      final BalancerManagerXmlParser balancerManagerXmlParser,
                                      final BalancerManagerHttpClient balancerManagerHttpClient,
                                      final HistoryFacadeService historyFacadeService) {
        this.groupService = groupService;
        this.applicationService = applicationService;
        this.webServerService = webServerService;
        this.jvmService = jvmService;
        this.clientFactoryHelper = clientFactoryHelper;
        this.historyFacadeService = historyFacadeService;
        this.balancerManagerHtmlParser = balancerManagerHtmlParser;
        this.balancerManagerXmlParser = balancerManagerXmlParser;
        this.balancerManagerHttpClient = balancerManagerHttpClient;
    }

    @Override
    public BalancerManagerState drainUserGroup(final String groupName, final String webServers, final String user) {
        LOGGER.info("Entering drainUserGroup, groupName: {} webServers: {}", groupName, webServers);
        String[] webServerArray = getRequireWebServers(webServers);
        List<BalancerManagerState.GroupDrainStatus.WebServerDrainStatus> webServerDrainStatusList = new ArrayList<>();
        Group group = groupService.getGroup(groupName);
        List<WebServer> webServerList;
        if (webServerArray.length == 0) {
            webServerList = webServerService.findWebServers(group.getId());
        } else {
            webServerList = findMatchWebServers(webServerService.findWebServers(group.getId()), webServerArray);
        }
        checkGroupStatus(groupName);
        for (WebServer webServer : webServerList) {
            BalancerManagerState.GroupDrainStatus.WebServerDrainStatus webServerDrainStatus = doDrainAndgetDrainStatus(webServer, "", true, user);
            webServerDrainStatusList.add(webServerDrainStatus);
        }
        BalancerManagerState.GroupDrainStatus groupDrainStatus = new BalancerManagerState.GroupDrainStatus(groupName, webServerDrainStatusList);
        return new BalancerManagerState(Collections.singletonList(groupDrainStatus));
    }

    @Override
    public BalancerManagerState drainUserWebServer(final String groupName, final String webServerName, final String jvmNames, final String user) {
        LOGGER.info("Entering drainUserGroup, groupName: " + groupName + " webServerName: " + webServerName + " jvmNames: " + jvmNames);
        String[] jvmArray = getRequireJvms(jvmNames);
        checkStatus(webServerService.getWebServer(webServerName));
        List<BalancerManagerState.GroupDrainStatus.WebServerDrainStatus> webServerDrainStatusList = new ArrayList<>();
        WebServer webServer = webServerService.getWebServer(webServerName);
        if (jvmArray.length == 0) {
            BalancerManagerState.GroupDrainStatus.WebServerDrainStatus webServerDrainStatus = doDrainAndgetDrainStatus(webServer, "", true, user);
            webServerDrainStatusList.add(webServerDrainStatus);
        } else {
            for (String jvmName : jvmArray) {
                // check if JVM exists, if not throw exception
                findJvmIfExists(jvmName);
                BalancerManagerState.GroupDrainStatus.WebServerDrainStatus webServerDrainStatus = doDrainAndgetDrainStatus(webServer, jvmName, true, user);
                webServerDrainStatusList.add(webServerDrainStatus);
            }
        }
        BalancerManagerState.GroupDrainStatus groupDrainStatus = new BalancerManagerState.GroupDrainStatus(groupName, webServerDrainStatusList);
        List<BalancerManagerState.GroupDrainStatus> groupDrainStatusList = new ArrayList<>();
        groupDrainStatusList.add(groupDrainStatus);
        return new BalancerManagerState(groupDrainStatusList);
    }

    private Jvm findJvmIfExists(String jvmName) {
        Jvm jvm;
        try {
            jvm = jvmService.getJvm(jvmName);
        } catch (javax.persistence.NoResultException e) {
            LOGGER.error(e.getMessage(), e);
            String message = "Cannot find " + jvmName + ", please verify if it is valid jvmName";
            throw new InternalErrorException(FaultType.INVALID_WEBSERVER_OPERATION, message);
        }
        return jvm;
    }

    @Override
    public BalancerManagerState drainUserJvm(final String jvmName, final String user) {
        LOGGER.info("Entering drainUserGroup, jvmName: " + jvmName);
        Jvm jvm = findJvmIfExists(jvmName);
        Set<Group> groupSet = jvm.getGroups();
        List<BalancerManagerState.GroupDrainStatus> groupDrainStatusList = new ArrayList<>();
        for (Group group : groupSet) {
            String groupName = group.getName();
            List<WebServer> webServerList = webServerService.findWebServers(group.getId());
            checkGroupStatus(groupName);
            List<BalancerManagerState.GroupDrainStatus.WebServerDrainStatus> webServerDrainStatusList = new ArrayList<>();
            for (WebServer webServer : webServerList) {
                BalancerManagerState.GroupDrainStatus.WebServerDrainStatus webServerDrainStatus = doDrainAndgetDrainStatus(webServer, jvmName, true, user);
                webServerDrainStatusList.add(webServerDrainStatus);
            }
            BalancerManagerState.GroupDrainStatus groupDrainStatus = new BalancerManagerState.GroupDrainStatus(groupName, webServerDrainStatusList);
            groupDrainStatusList.add(groupDrainStatus);
        }
        return new BalancerManagerState(groupDrainStatusList);
    }

    @Override
    public BalancerManagerState drainUserGroupJvm(final String groupName, final String jvmName, final String user) {
        LOGGER.info("Entering drainUserGroupJvm, groupName: " + groupName + ", jvmName: " + jvmName);
        checkGroupStatus(groupName);
        Group group = groupService.getGroup(groupName);
        group = groupService.getGroupWithWebServers(group.getId());
        verifyJvmExistInGroup(group, jvmName);
        List<BalancerManagerState.GroupDrainStatus.WebServerDrainStatus> webServerDrainStatusList = new ArrayList<>();
        for (WebServer webServer : group.getWebServers()) {
            BalancerManagerState.GroupDrainStatus.WebServerDrainStatus webServerDrainStatus = doDrainAndgetDrainStatus(webServer, jvmName, true, user);
            webServerDrainStatusList.add(webServerDrainStatus);
        }
        BalancerManagerState.GroupDrainStatus groupDrainStatus = new BalancerManagerState.GroupDrainStatus(groupName, webServerDrainStatusList);
        List<BalancerManagerState.GroupDrainStatus> groupDrainStatusList = new ArrayList<>();
        groupDrainStatusList.add(groupDrainStatus);
        return new BalancerManagerState(groupDrainStatusList);
    }

    private void verifyJvmExistInGroup(Group group, String jvmName) {
        boolean found = false;
        for (Jvm jvm : group.getJvms()) {
            if (jvm.getJvmName().equalsIgnoreCase(jvmName)) {
                found = true;
                break;
            }
        }
        if (!found) {
            String message = "Cannot find " + jvmName + " in group: " + group.getName() + ", please verify if it is valid jvmName";
            throw new InternalErrorException(FaultType.INVALID_WEBSERVER_OPERATION, message);
        }
    }

    @Override
    public BalancerManagerState getGroupDrainStatus(final String groupName, final String user) {
        LOGGER.info("Entering getGroupDrainStatus: " + groupName);
        checkGroupStatus(groupName);
        List<BalancerManagerState.GroupDrainStatus.WebServerDrainStatus> webServerDrainStatusList = new ArrayList<>();
        Group group = groupService.getGroup(groupName);
        for (WebServer webServer : webServerService.findWebServers(group.getId())) {
            BalancerManagerState.GroupDrainStatus.WebServerDrainStatus webServerDrainStatus = doDrainAndgetDrainStatus(webServer, "", false, user);
            webServerDrainStatusList.add(webServerDrainStatus);
        }
        BalancerManagerState.GroupDrainStatus groupDrainStatus = new BalancerManagerState.GroupDrainStatus(groupName, webServerDrainStatusList);
        List<BalancerManagerState.GroupDrainStatus> groupDrainStatusList = new ArrayList<>();
        groupDrainStatusList.add(groupDrainStatus);
        return new BalancerManagerState(groupDrainStatusList);
    }

    public void checkGroupStatus(final String groupName) {
        final Group group = groupService.getGroup(groupName);
        List<WebServer> webServerList = webServerService.findWebServers(group.getId());
        for (WebServer webServer : webServerList) {
            if (!webServerService.isStarted(webServer)) {
                final String message = "The target Web Server " + webServer.getName() + " in group " + groupName + " must be STARTED before attempting to drain users";
                LOGGER.error(message);
                throw new InternalErrorException(FaultType.INVALID_WEBSERVER_OPERATION, message);
            }
        }
    }

    public void checkStatus(WebServer webServer) {
        if (!webServerService.isStarted(webServer)) {
            final String message = "The target Web Server " + webServer.getName() + " must be STARTED before attempting to drain users";
            LOGGER.error(message);
            throw new InternalErrorException(FaultType.INVALID_WEBSERVER_OPERATION, message);
        }
    }

    public String[] getRequireWebServers(final String webServers) {
        if (webServers.length() != 0) {
            return webServers.split(",");
        } else {
            return new String[0];
        }
    }

    private String[] getRequireJvms(final String jvms) {
        if (jvms.length() != 0) {
            return jvms.split(",");
        } else {
            return new String[0];
        }
    }

    public List<WebServer> findMatchWebServers(final List<WebServer> webServers, final String[] webServerArray) {
        LOGGER.info("Entering findMatchWebServers");
        List<WebServer> webServersMatch = new ArrayList<>();
        List<String> webServerNameMatch = new ArrayList<>();
        Map<Integer, String> webServerNamesIndex = new HashMap<>();
        for (WebServer webServer : webServers) {
            webServerNamesIndex.put(webServers.indexOf(webServer), webServer.getName());
        }
        for (String webServerArrayContentName : webServerArray) {
            if (webServerNamesIndex.containsValue(webServerArrayContentName)) {
                int index = 0;
                for (Map.Entry<Integer, String> entry : webServerNamesIndex.entrySet()) {
                    if (entry.getValue().equalsIgnoreCase(webServerArrayContentName)) {
                        index = entry.getKey();
                    }
                }
                webServersMatch.add(webServers.get(index));
                webServerNameMatch.add(webServers.get(index).getName());
            }
        }
        String wrongWebServers = "";
        for (String webServerArrayContentName : webServerArray) {
            if (!webServerNameMatch.contains(webServerArrayContentName)) {
                LOGGER.error("WebServer Name does not exist: " + webServerArrayContentName);
                wrongWebServers += webServerArrayContentName + ", ";
            }
        }
        if (wrongWebServers.length() != 0) {
            throw new InternalErrorException(FaultType.WEBSERVER_NOT_FOUND, wrongWebServers.substring(0, wrongWebServers.length() - 2) + " cannot be found in the group");
        }
        return webServersMatch;
    }

    private BalancerManagerState.GroupDrainStatus.WebServerDrainStatus doDrainAndgetDrainStatus(final WebServer webServer, final String jvmName, final boolean post,
                                                                                                final String user) {
        List<BalancerManagerState.GroupDrainStatus.WebServerDrainStatus.JvmDrainStatus> jvmDrainStatusList = prepareDrainWork(webServer, jvmName, post, user);
        return new BalancerManagerState.GroupDrainStatus.WebServerDrainStatus(webServer.getName(), jvmDrainStatusList);
    }

    private List<BalancerManagerState.GroupDrainStatus.WebServerDrainStatus.JvmDrainStatus> prepareDrainWork(final WebServer webServer, final String jvmName, final boolean post,
                                                                                                             final String user) {
        LOGGER.info("Entering prepareDrainWork");
        List<BalancerManagerState.GroupDrainStatus.WebServerDrainStatus.JvmDrainStatus> jvmDrainStatusList = new ArrayList<>();
        final String balancerManagerHtmlUrl = balancerManagerHtmlParser.getUrlPath(webServer.getHost());
        final String balancerManagerResponseHtml = getBalancerManagerResponse(balancerManagerHtmlUrl);
        final Map<String, String> balancers = balancerManagerHtmlParser.findBalancers(balancerManagerResponseHtml);
        for (Map.Entry<String, String> entry : balancers.entrySet()) {
            final String balancerName = entry.getKey();
            final String nonce = entry.getValue();
            final String balancerManagerXmlUrl = balancerManagerXmlParser.getUrlPath(webServer.getHost(), balancerName, nonce);
            final String balancerManagerResponseXml = getBalancerManagerResponse(balancerManagerXmlUrl);
            Manager manager = balancerManagerXmlParser.getWorkerXml(balancerManagerResponseXml);
            Map<String, String> workers;
            if ("".equals(jvmName)) {
                workers = balancerManagerXmlParser.getWorkers(manager, balancerName);
            } else {
                workers = balancerManagerXmlParser.getJvmWorkerByName(manager, balancerName, jvmName);
            }
            if (post) {
                doDrain(workers, balancerManagerHtmlUrl, webServer, balancerName, nonce, user);
            }
            for (String worker : workers.keySet()) {
                String workerUrl = balancerManagerHtmlParser.getWorkerUrlPath(webServer.getHost(), balancerName, nonce, worker);
                String workerHtml = getBalancerManagerResponse(workerUrl);
                Map<String, String> workerStatusMap = balancerManagerHtmlParser.findWorkerStatus(workerHtml);
                BalancerManagerState.GroupDrainStatus.WebServerDrainStatus.JvmDrainStatus jvmDrainStatus = new BalancerManagerState.GroupDrainStatus.WebServerDrainStatus.JvmDrainStatus(worker,
                        balancerManagerXmlParser.findJvmNameByWorker(worker),
                        findApplicationNameByWorker(worker),
                        workerStatusMap.get(WorkerStatusType.IGNORE_ERRORS.name()),
                        workerStatusMap.get(WorkerStatusType.DRAINING_MODE.name()),
                        workerStatusMap.get(WorkerStatusType.DISABLED.name()),
                        workerStatusMap.get(WorkerStatusType.HOT_STANDBY.name()));
                jvmDrainStatusList.add(jvmDrainStatus);
            }
        }
        return jvmDrainStatusList;
    }


    public String findApplicationNameByWorker(final String worker) {
        LOGGER.info("Entering findApplicationNameByWorker");
        String appName = "";
        int indexOfLastColon = worker.lastIndexOf(":");
        int firstIndexOfSlashAfterLastColon = worker.substring(indexOfLastColon).indexOf("/");
        int indexOfSlashAfterPort = indexOfLastColon + firstIndexOfSlashAfterLastColon;
        final String context = worker.substring(indexOfSlashAfterPort);
        LOGGER.info("context: " + context);
        List<Application> applications = applicationService.getApplications();
        for (Application application : applications) {
            if (application.getWebAppContext().equals(context)) {
                appName = application.getName();
                break;
            }
        }
        return appName;
    }


    private String getBalancerManagerResponse(final String statusUri) {
        LOGGER.info("Entering getBalancerManagerResponse: " + statusUri);
        try {
            return IOUtils.toString(clientFactoryHelper.getHttpsURLConnection(statusUri).getInputStream(),
                                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("io exception", e);
            throw new ApplicationException("Failed to get the response for Balancer Manager ", e);
        }
    }

    private void doDrain(final Map<String, String> workers,
                         final String balancerManagerurl,
                         final WebServer webServer,
                         final String balancerName,
                         final String nonce,
                         final String user) {
        LOGGER.info("Entering doDrain for worker size " + workers.size());
        for (String workerUrl : workers.keySet()) {
            final String message = "Drain request for " + workerUrl;
            sendMessage(webServer, message, user);
            try {
                CloseableHttpResponse response = balancerManagerHttpClient.doHttpClientPost(balancerManagerurl, getNvp(workerUrl, balancerName, nonce));
                LOGGER.info("response code: " + response.getStatusLine().getStatusCode());
                response.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                throw new ApplicationException(e); // TODO if one worker fails then the rest of the works won't run - is that the intended purpose? should we guarantee that all workers send the drain message? Jeff M 1/31/2017 from Brett's open source code review
            }
        }
    }

    private void sendMessage(final WebServer webServer, final String message, final String user) {
        LOGGER.info(message);
        historyFacadeService.write(webServer.getName(), new ArrayList<>(webServer.getGroups()), message, EventType.USER_ACTION_INFO, user);

    }

    public List<NameValuePair> getNvp(final String worker, final String balancerName, final String nonce) {
        List<NameValuePair> nvps = new ArrayList<>(4);
        nvps.add(new BasicNameValuePair("w_status_N", "1"));
        nvps.add(new BasicNameValuePair("b", balancerName));
        nvps.add(new BasicNameValuePair("w", worker));
        nvps.add(new BasicNameValuePair("nonce", nonce));
        return nvps;
    }

}
