package com.cerner.jwala.service.balancermanager;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.balancermanager.BalancerManagerState;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.service.HistoryFacadeService;
import com.cerner.jwala.service.app.ApplicationService;
import com.cerner.jwala.service.balancermanager.impl.BalancerManagerHtmlParser;
import com.cerner.jwala.service.balancermanager.impl.BalancerManagerHttpClient;
import com.cerner.jwala.service.balancermanager.impl.BalancerManagerServiceImpl;
import com.cerner.jwala.service.balancermanager.impl.BalancerManagerXmlParser;
import com.cerner.jwala.service.balancermanager.impl.xml.data.Manager;
import com.cerner.jwala.service.group.GroupService;
import com.cerner.jwala.service.jvm.JvmService;
import com.cerner.jwala.service.webserver.WebServerService;
import com.cerner.jwala.service.webserver.component.ClientFactoryHelper;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.openjpa.persistence.NoResultException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayInputStream;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class BalanceManagerServiceImplTest {

    private BalancerManagerService balanceManagerService;

    @Mock
    private GroupService mockGroupService;

    @Mock
    private ApplicationService mockApplicationService;

    @Mock
    private WebServerService mockWebServerService;

    @Mock
    private JvmService mockJvmService;

    @Mock
    private ClientFactoryHelper mockClientFactoryHelper;

    @Mock
    private BalancerManagerHtmlParser mockBalancerManagerHtmlParser;

    @Mock
    private BalancerManagerXmlParser mockBalancerManagerXmlParser;

    @Mock
    private BalancerManagerHttpClient mockBalancerManagerHttpClient;

    @Mock
    private HistoryFacadeService mockHistoryFacadeService;

    @Before
    public void setup() {
        initMocks(this);
        reset(mockBalancerManagerHtmlParser);
        balanceManagerService = new BalancerManagerServiceImpl(mockGroupService, mockApplicationService, mockWebServerService,
                mockJvmService, mockClientFactoryHelper, mockBalancerManagerHtmlParser, mockBalancerManagerXmlParser,
                mockBalancerManagerHttpClient, mockHistoryFacadeService);
    }

    @Test
    public void testDrainUserGroup() throws Exception {
        final Group mockGroup = mock(Group.class);
        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroup);

        final List<WebServer> webServerList = new ArrayList<>();
        final WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServer.getName()).thenReturn("webServer1");
        when(mockWebServer.getState()).thenReturn(WebServerReachableState.WS_REACHABLE);
        webServerList.add(mockWebServer);

        when(mockWebServerService.isStarted(mockWebServer)).thenReturn(true);

        when(mockWebServerService.findWebServers(any(Identifier.class))).thenReturn(webServerList);

        final HttpsURLConnection mockHttpsUrlConnection = mock(HttpsURLConnection.class);
        when(mockHttpsUrlConnection.getInputStream()).thenReturn(new ByteArrayInputStream("hello".getBytes()));
        when(mockClientFactoryHelper.getHttpsURLConnection(anyString())).thenReturn(mockHttpsUrlConnection);

        final Map<String, String> balancerMap = new HashMap<>();
        balancerMap.put("balKey", "balVal");
        when(mockBalancerManagerHtmlParser.findBalancers(anyString())).thenReturn(balancerMap);

        final Map<String, String> workerMap = new HashMap<>();
        workerMap.put("https://somehost:9101/hct", "any");
        final List<Application> appList = new ArrayList<>();
        final Application mockApp = mock(Application.class);
        final Set<Jvm> jvmSet = new HashSet<>();
        final Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getJvmName()).thenReturn("jvm1");
        when(mockJvm.getHostName()).thenReturn("host");
        jvmSet.add(mockJvm);
        when(mockGroup.getJvms()).thenReturn(jvmSet);
        when(mockApp.getGroup()).thenReturn(mockGroup);
        when(mockApp.getWebAppContext()).thenReturn("/hct");
        when(mockApp.isSecure()).thenReturn(true); // https
        appList.add(mockApp);
        when(mockApplicationService.getApplications()).thenReturn(appList);

        when(mockBalancerManagerXmlParser.getWorkers(any(Manager.class), anyString())).thenReturn(workerMap);

        final CloseableHttpResponse mockCloseableHttpResponse = mock(CloseableHttpResponse.class);
        when(mockCloseableHttpResponse.getStatusLine()).thenReturn(mock(StatusLine.class));

        when(mockBalancerManagerHttpClient.doHttpClientPost(anyString(), anyList())).thenReturn(mockCloseableHttpResponse);

        final BalancerManagerState balancerManagerState = balanceManagerService.drainUserGroup("group1", "webServer1", "user1");
        assertEquals("group1", balancerManagerState.getGroups().get(0).getGroupName());
        verify(mockBalancerManagerHtmlParser).getWorkerUrlPath(anyString(), anyString(), anyString(),
                eq("https://somehost:9101/hct"));
    }

    @Test
    public void testDrainUserGroupNoWebServersSpecified() throws Exception {
        final Group mockGroup = mock(Group.class);

        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroup);

        final List<WebServer> webServerList = new ArrayList<>();
        final WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServer.getName()).thenReturn("webServer1");
        when(mockWebServer.getState()).thenReturn(WebServerReachableState.WS_REACHABLE);
        webServerList.add(mockWebServer);

        when(mockWebServerService.isStarted(mockWebServer)).thenReturn(true);

        when(mockWebServerService.findWebServers(any(Identifier.class))).thenReturn(webServerList);

        final HttpsURLConnection mockHttpsUrlConnection = mock(HttpsURLConnection.class);
        when(mockHttpsUrlConnection.getInputStream()).thenReturn(new ByteArrayInputStream("hello".getBytes()));
        when(mockClientFactoryHelper.getHttpsURLConnection(anyString())).thenReturn(mockHttpsUrlConnection);

        final Map<String, String> balancerMap = new HashMap<>();
        balancerMap.put("balKey", "balVal");
        when(mockBalancerManagerHtmlParser.findBalancers(anyString())).thenReturn(balancerMap);

        final Map<String, String> workerMap = new HashMap<>();
        workerMap.put("https://somehost:9101/hct", "any");
        final List<Application> appList = new ArrayList<>();
        final Application mockApp = mock(Application.class);
        final Set<Jvm> jvmSet = new HashSet<>();
        final Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getJvmName()).thenReturn("jvm1");
        when(mockJvm.getHostName()).thenReturn("host");
        jvmSet.add(mockJvm);
        when(mockGroup.getJvms()).thenReturn(jvmSet);
        when(mockApp.getGroup()).thenReturn(mockGroup);
        when(mockApp.getWebAppContext()).thenReturn("/hct");
        when(mockApp.isSecure()).thenReturn(true); // https
        appList.add(mockApp);
        when(mockApplicationService.getApplications()).thenReturn(appList);

        when(mockBalancerManagerXmlParser.getWorkers(any(Manager.class), anyString())).thenReturn(workerMap);

        final CloseableHttpResponse mockCloseableHttpResponse = mock(CloseableHttpResponse.class);
        when(mockCloseableHttpResponse.getStatusLine()).thenReturn(mock(StatusLine.class));

        when(mockBalancerManagerHttpClient.doHttpClientPost(anyString(), anyList())).thenReturn(mockCloseableHttpResponse);

        final BalancerManagerState balancerManagerState = balanceManagerService.drainUserGroup("group1", "", "user1");
        assertEquals("group1", balancerManagerState.getGroups().get(0).getGroupName());
        verify(mockBalancerManagerHtmlParser).getWorkerUrlPath(anyString(), anyString(), anyString(),
                eq("https://somehost:9101/hct"));
    }

    @Test
    public void testDrainUserWebServerHttps() throws Exception {
        final WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServerService.getWebServer(eq("webServer1"))).thenReturn(mockWebServer);
        when(mockWebServerService.isStarted(mockWebServer)).thenReturn(true);
        when(mockJvmService.getJvm(eq("jvm1"))).thenReturn(mock(Jvm.class));

        final HttpsURLConnection mockHttpsUrlConnection = mock(HttpsURLConnection.class);
        when(mockHttpsUrlConnection.getInputStream()).thenReturn(new ByteArrayInputStream("hello".getBytes()));
        when(mockClientFactoryHelper.getHttpsURLConnection(anyString())).thenReturn(mockHttpsUrlConnection);

        final Map<String, String> balancerMap = new HashMap<>();
        balancerMap.put("balKey", "balVal");
        when(mockBalancerManagerHtmlParser.findBalancers(anyString())).thenReturn(balancerMap);

        final Map<String, String> workerMap = new HashMap<>();
        workerMap.put("https://somehost:9101/hct", "any");
        final List<Application> appList = new ArrayList<>();
        final Application mockApp = mock(Application.class);
        final Group mockGroup = mock(Group.class);
        final Set<Jvm> jvmSet = new HashSet<>();
        final Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getJvmName()).thenReturn("jvm1");
        when(mockJvm.getHostName()).thenReturn("host");
        jvmSet.add(mockJvm);
        when(mockGroup.getJvms()).thenReturn(jvmSet);
        when(mockApp.getGroup()).thenReturn(mockGroup);
        when(mockApp.getWebAppContext()).thenReturn("/hct");
        when(mockApp.isSecure()).thenReturn(true); // https
        appList.add(mockApp);
        when(mockApplicationService.getApplications()).thenReturn(appList);

        when(mockBalancerManagerXmlParser.getJvmWorkerByName(any(Manager.class), anyString(), anyString())).thenReturn(workerMap);

        final CloseableHttpResponse mockCloseableHttpResponse = mock(CloseableHttpResponse.class);
        when(mockCloseableHttpResponse.getStatusLine()).thenReturn(mock(StatusLine.class));

        when(mockBalancerManagerHttpClient.doHttpClientPost(anyString(), anyList())).thenReturn(mockCloseableHttpResponse);

        final BalancerManagerState balancerManagerState = balanceManagerService.drainUserWebServer("group1", "webServer1", "jvm1", "user1");
        assertEquals("group1", balancerManagerState.getGroups().get(0).getGroupName());
        verify(mockBalancerManagerHtmlParser).getWorkerUrlPath(anyString(), anyString(), anyString(),
                eq("https://somehost:9101/hct"));
    }

    @Test
    public void testDrainUserWebServerHttp() throws Exception {
        final WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServerService.getWebServer(eq("webServer1"))).thenReturn(mockWebServer);
        when(mockWebServerService.isStarted(mockWebServer)).thenReturn(true);
        when(mockJvmService.getJvm(eq("jvm1"))).thenReturn(mock(Jvm.class));

        final HttpsURLConnection mockHttpsUrlConnection = mock(HttpsURLConnection.class);
        when(mockHttpsUrlConnection.getInputStream()).thenReturn(new ByteArrayInputStream("hello".getBytes()));
        when(mockClientFactoryHelper.getHttpsURLConnection(anyString())).thenReturn(mockHttpsUrlConnection);

        final Map<String, String> balancerMap = new HashMap<>();
        balancerMap.put("balKey", "balVal");
        when(mockBalancerManagerHtmlParser.findBalancers(anyString())).thenReturn(balancerMap);

        final Map<String, String> workerMap = new HashMap<>();
        workerMap.put("http://somehost:9101/hct", "any");
        final List<Application> appList = new ArrayList<>();
        final Application mockApp = mock(Application.class);
        final Group mockGroup = mock(Group.class);
        final Set<Jvm> jvmSet = new HashSet<>();
        final Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getJvmName()).thenReturn("jvm1");
        when(mockJvm.getHostName()).thenReturn("host");
        jvmSet.add(mockJvm);
        when(mockGroup.getJvms()).thenReturn(jvmSet);
        when(mockApp.getGroup()).thenReturn(mockGroup);
        when(mockApp.getWebAppContext()).thenReturn("/hct");
        when(mockApp.isSecure()).thenReturn(false); // http
        appList.add(mockApp);
        when(mockApplicationService.getApplications()).thenReturn(appList);

        when(mockBalancerManagerXmlParser.getJvmWorkerByName(any(Manager.class), anyString(), anyString())).thenReturn(workerMap);

        final CloseableHttpResponse mockCloseableHttpResponse = mock(CloseableHttpResponse.class);
        when(mockCloseableHttpResponse.getStatusLine()).thenReturn(mock(StatusLine.class));

        when(mockBalancerManagerHttpClient.doHttpClientPost(anyString(), anyList())).thenReturn(mockCloseableHttpResponse);

        final BalancerManagerState balancerManagerState = balanceManagerService.drainUserWebServer("group1", "webServer1", "jvm1", "user1");
        System.out.println(balancerManagerState);
        assertEquals("group1", balancerManagerState.getGroups().get(0).getGroupName());
        verify(mockBalancerManagerHtmlParser).getWorkerUrlPath(anyString(), anyString(), anyString(),
                eq("http://somehost:9101/hct"));
    }

    @Test(expected = InternalErrorException.class)
    public void testDrainUserWebServerJvmNotFound() throws Exception {
        final WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServerService.getWebServer(eq("webServer1"))).thenReturn(mockWebServer);
        when(mockWebServerService.isStarted(mockWebServer)).thenReturn(true);
        when(mockJvmService.getJvm("jvm1")).thenThrow(NoResultException.class);
        balanceManagerService.drainUserWebServer("group1", "webServer1", "jvm1", "user1");
    }

    @Test
    public void testDrainUserWebServerNoJvmsSpecified() throws Exception {
        final WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServerService.getWebServer(eq("webServer1"))).thenReturn(mockWebServer);
        when(mockWebServerService.isStarted(mockWebServer)).thenReturn(true);
        when(mockJvmService.getJvm(eq("jvm1"))).thenReturn(mock(Jvm.class));

        final HttpsURLConnection mockHttpsUrlConnection = mock(HttpsURLConnection.class);
        when(mockHttpsUrlConnection.getInputStream()).thenReturn(new ByteArrayInputStream("hello".getBytes()));
        when(mockClientFactoryHelper.getHttpsURLConnection(anyString())).thenReturn(mockHttpsUrlConnection);

        final Map<String, String> balancerMap = new HashMap<>();
        balancerMap.put("balKey", "balVal");
        when(mockBalancerManagerHtmlParser.findBalancers(anyString())).thenReturn(balancerMap);

        final Map<String, String> workerMap = new HashMap<>();
        workerMap.put("https://somehost:9101/hct", "any");
        final List<Application> appList = new ArrayList<>();
        final Application mockApp = mock(Application.class);
        when(mockApp.getWebAppContext()).thenReturn("/hct");
        appList.add(mockApp);
        when(mockApplicationService.getApplications()).thenReturn(appList);

        when(mockBalancerManagerXmlParser.getWorkers(any(Manager.class), anyString())).thenReturn(workerMap);

        final CloseableHttpResponse mockCloseableHttpResponse = mock(CloseableHttpResponse.class);
        when(mockCloseableHttpResponse.getStatusLine()).thenReturn(mock(StatusLine.class));

        when(mockBalancerManagerHttpClient.doHttpClientPost(anyString(), anyList())).thenReturn(mockCloseableHttpResponse);

        final BalancerManagerState balancerManagerState = balanceManagerService.drainUserWebServer("group1", "webServer1", "", "user1");
        assertEquals("group1", balancerManagerState.getGroups().get(0).getGroupName());
        verify(mockBalancerManagerHtmlParser).getWorkerUrlPath(anyString(), anyString(), anyString(),
                eq("https://somehost:9101/hct"));
    }

    @Test
    public void testDrainUserJvm() throws Exception {
        final Jvm mockJvm = mock(Jvm.class);
        final Set<Group> groupSet = new HashSet<>();
        final Group mockGroup = mock(Group.class);
        groupSet.add(mockGroup);
        when(mockJvm.getGroups()).thenReturn(groupSet);
        when(mockJvmService.getJvm(eq("jvm1"))).thenReturn(mockJvm);

        final List<WebServer> webServerList = new ArrayList<>();
        final WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServer.getName()).thenReturn("webServer1");
        when(mockWebServer.getState()).thenReturn(WebServerReachableState.WS_REACHABLE);
        webServerList.add(mockWebServer);

        when(mockWebServerService.isStarted(mockWebServer)).thenReturn(true);

        when(mockBalancerManagerHtmlParser.getUrlPath(anyString())).thenReturn("any");

        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroup);

        final HttpsURLConnection mockHttpsUrlConnection = mock(HttpsURLConnection.class);
        when(mockHttpsUrlConnection.getInputStream()).thenReturn(new ByteArrayInputStream("hello".getBytes()));
        when(mockClientFactoryHelper.getHttpsURLConnection(anyString())).thenReturn(mockHttpsUrlConnection);

        when(mockWebServerService.findWebServers(any(Identifier.class))).thenReturn(webServerList);

        final Map<String, String> balancerMap = new HashMap<>();
        balancerMap.put("balKey", "balVal");
        when(mockBalancerManagerHtmlParser.findBalancers(anyString())).thenReturn(balancerMap);

        final Map<String, String> workerMap = new HashMap<>();
        workerMap.put("https://somehost:9101/hct", "any");
        final List<Application> appList = new ArrayList<>();
        final Application mockApp = mock(Application.class);
        final Set<Jvm> jvmSet = new HashSet<>();
        when(mockJvm.getJvmName()).thenReturn("jvm1");
        when(mockJvm.getHostName()).thenReturn("host");
        jvmSet.add(mockJvm);
        when(mockGroup.getJvms()).thenReturn(jvmSet);
        when(mockApp.getGroup()).thenReturn(mockGroup);
        when(mockApp.getWebAppContext()).thenReturn("/hct");
        when(mockApp.isSecure()).thenReturn(true); // https
        appList.add(mockApp);
        when(mockApplicationService.getApplications()).thenReturn(appList);

        when(mockBalancerManagerXmlParser.getJvmWorkerByName(any(Manager.class), anyString(), anyString())).thenReturn(workerMap);

        final CloseableHttpResponse mockCloseableHttpResponse = mock(CloseableHttpResponse.class);
        when(mockCloseableHttpResponse.getStatusLine()).thenReturn(mock(StatusLine.class));

        when(mockBalancerManagerHttpClient.doHttpClientPost(anyString(), anyList())).thenReturn(mockCloseableHttpResponse);

        final BalancerManagerState balancerManagerState = balanceManagerService.drainUserJvm("jvm1", "user1");
        System.out.println(balancerManagerState);
        assertEquals("webServer1", balancerManagerState.getGroups().get(0).getwebServers().get(0).getWebServerName());
        verify(mockBalancerManagerHtmlParser).getWorkerUrlPath(anyString(), anyString(), anyString(),
                eq("https://somehost:9101/hct"));
    }

    @Test
    public void testDrainUserGroupJvm() throws Exception {
        final Group mockGroup = mock(Group.class);
        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroup);
        when(mockGroupService.getGroupWithWebServers(any(Identifier.class))).thenReturn(mockGroup);

        final Set<WebServer> webServerSet = new HashSet<>();
        final WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServer.getName()).thenReturn("webServer1");
        when(mockWebServer.getState()).thenReturn(WebServerReachableState.WS_REACHABLE);
        webServerSet.add(mockWebServer);

        when(mockWebServerService.isStarted(mockWebServer)).thenReturn(true);

        final Set<Jvm> jvmSet = new HashSet<>();
        final Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getJvmName()).thenReturn("jvm1");
        when(mockJvm.getHostName()).thenReturn("host");
        jvmSet.add(mockJvm);
        when(mockGroup.getJvms()).thenReturn(jvmSet);

        when(mockGroup.getWebServers()).thenReturn(webServerSet);

        final HttpsURLConnection mockHttpsUrlConnection = mock(HttpsURLConnection.class);
        when(mockHttpsUrlConnection.getInputStream()).thenReturn(new ByteArrayInputStream("hello".getBytes()));
        when(mockClientFactoryHelper.getHttpsURLConnection(anyString())).thenReturn(mockHttpsUrlConnection);

        final Map<String, String> balancerMap = new HashMap<>();
        balancerMap.put("balKey", "balVal");
        when(mockBalancerManagerHtmlParser.findBalancers(anyString())).thenReturn(balancerMap);

        final Map<String, String> workerMap = new HashMap<>();
        workerMap.put("https://somehost:9101/hct", "any");
        final List<Application> appList = new ArrayList<>();
        final Application mockApp = mock(Application.class);
        when(mockJvm.getJvmName()).thenReturn("jvm1");
        when(mockJvm.getHostName()).thenReturn("host");
        jvmSet.add(mockJvm);
        when(mockGroup.getJvms()).thenReturn(jvmSet);
        when(mockApp.getGroup()).thenReturn(mockGroup);
        when(mockApp.getWebAppContext()).thenReturn("/hct");
        when(mockApp.isSecure()).thenReturn(true); // https
        appList.add(mockApp);
        when(mockApplicationService.getApplications()).thenReturn(appList);
        when(mockBalancerManagerXmlParser.getJvmWorkerByName(any(Manager.class), anyString(), anyString())).thenReturn(workerMap);

        final CloseableHttpResponse mockCloseableHttpResponse = mock(CloseableHttpResponse.class);
        when(mockCloseableHttpResponse.getStatusLine()).thenReturn(mock(StatusLine.class));

        when(mockBalancerManagerHttpClient.doHttpClientPost(anyString(), anyList())).thenReturn(mockCloseableHttpResponse);

        final BalancerManagerState balancerManagerState = balanceManagerService.drainUserGroupJvm("group1", "jvm1", "user1");
        assertEquals("group1", balancerManagerState.getGroups().get(0).getGroupName());
        verify(mockBalancerManagerHtmlParser).getWorkerUrlPath(anyString(), anyString(), anyString(),
                eq("https://somehost:9101/hct"));
    }

    @Test
    public void testGetGroupDrainStatus() throws Exception {
        final Group mockGroup = mock(Group.class);
        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroup);

        final Set<WebServer> webServerSet = new HashSet<>();
        final WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServer.getName()).thenReturn("webServer1");
        when(mockWebServer.getState()).thenReturn(WebServerReachableState.WS_REACHABLE);
        webServerSet.add(mockWebServer);

        when(mockWebServerService.isStarted(mockWebServer)).thenReturn(true);

        final List<WebServer> webServerList = new ArrayList<>();

        when(mockWebServer.getName()).thenReturn("webServer1");
        when(mockWebServer.getState()).thenReturn(WebServerReachableState.WS_REACHABLE);
        webServerList.add(mockWebServer);

        when(mockWebServerService.isStarted(mockWebServer)).thenReturn(true);

        when(mockBalancerManagerHtmlParser.getUrlPath(anyString())).thenReturn("any");

        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroup);

        final HttpsURLConnection mockHttpsUrlConnection = mock(HttpsURLConnection.class);
        when(mockHttpsUrlConnection.getInputStream()).thenReturn(new ByteArrayInputStream("hello".getBytes()));
        when(mockClientFactoryHelper.getHttpsURLConnection(anyString())).thenReturn(mockHttpsUrlConnection);

        when(mockWebServerService.findWebServers(any(Identifier.class))).thenReturn(webServerList);

        final Map<String, String> balancerMap = new HashMap<>();
        balancerMap.put("balKey", "balVal");
        when(mockBalancerManagerHtmlParser.findBalancers(anyString())).thenReturn(balancerMap);

        final Map<String, String> workerMap = new HashMap<>();
        workerMap.put("https://somehost:9101/hct", "any");
        final List<Application> appList = new ArrayList<>();
        final Application mockApp = mock(Application.class);
        final Set<Jvm> jvmSet = new HashSet<>();
        final Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getJvmName()).thenReturn("jvm1");
        when(mockJvm.getHostName()).thenReturn("host");
        jvmSet.add(mockJvm);
        when(mockGroup.getJvms()).thenReturn(jvmSet);
        when(mockApp.getGroup()).thenReturn(mockGroup);
        when(mockApp.getWebAppContext()).thenReturn("/hct");
        when(mockApp.isSecure()).thenReturn(true); // https
        appList.add(mockApp);
        when(mockApplicationService.getApplications()).thenReturn(appList);

        when(mockBalancerManagerXmlParser.getWorkers(any(Manager.class), anyString())).thenReturn(workerMap);

        final CloseableHttpResponse mockCloseableHttpResponse = mock(CloseableHttpResponse.class);
        when(mockCloseableHttpResponse.getStatusLine()).thenReturn(mock(StatusLine.class));

        // when(mockBalancerManagerHttpClient.doHttpClientPost(anyString(), anyList())).thenReturn(mockCloseableHttpResponse);

        final BalancerManagerState balancerManagerState = balanceManagerService.getGroupDrainStatus("group1", "user1");
        assertEquals("group1", balancerManagerState.getGroups().get(0).getGroupName());
        verify(mockBalancerManagerHtmlParser).getWorkerUrlPath(anyString(), anyString(), anyString(),
                eq("https://somehost:9101/hct"));
    }

    @Test
    public void testDrainUserJvmMultiApp() throws Exception {


    }
}