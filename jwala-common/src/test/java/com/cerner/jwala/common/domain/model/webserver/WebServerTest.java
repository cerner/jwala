package com.cerner.jwala.common.domain.model.webserver;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.domain.model.uri.UriBuilder;
import org.junit.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class WebServerTest {

    private static final String HOST = "host";
    private static final String NAME = "name";
    private static final Path STATUS_PATH = new Path("/the status path");
    private static final Path HTTP_CONFIG_FILE = new Path("d:/some-dir/httpd.conf");
    private static final Integer port = 10000;
    private static final Integer httpsPort = 20000;
    private static final Long id = 1L;
    private static final Identifier<WebServer> wsId = new Identifier<>(id);
    private final List<Group> groups = new ArrayList<>();
    private static final Path SVR_ROOT = new Path("./");
    private static final Path DOC_ROOT = new Path("htdocs");
    private final WebServer ws = new WebServer(wsId, groups, NAME, HOST, port, httpsPort, STATUS_PATH, WebServerReachableState.WS_UNREACHABLE);

    @Test
    public void testGetId() {
        assertEquals(wsId, ws.getId());
    }

    @Test
    public void testGetName() {
        assertEquals(NAME, ws.getName());
    }

    @Test
    public void testGetHost() {
        assertEquals(HOST, ws.getHost());
    }

    @Test
    public void testGetPort() {
        assertEquals(port, ws.getPort());
    }

    @Test
    public void testGetGroups() {
        assertEquals(0, ws.getGroups().size());
    }

    @Test
    public void testGetGroupIds() {
        assertEquals(0, ws.getGroupIds().size());
    }

    @Test
    public void testStatusUri() throws Exception {
        final URI expectedUri = new UriBuilder().setHost("my.expected.host.example.com")
                                                .setPort(12345)
                                                .setPath(STATUS_PATH).buildUnchecked();
        final WebServer webServer = new WebServer(new Identifier<WebServer>(123456L),
                                                  Collections.<Group>emptySet(),
                                                  "name",
                                                  expectedUri.getHost(),
                                                  expectedUri.getPort(),
                                                  99,
                                                  STATUS_PATH,
                                                  WebServerReachableState.WS_UNREACHABLE);
        final URI actualUri = webServer.getStatusUri();
        assertEquals(expectedUri,
                     actualUri);
    }
}
