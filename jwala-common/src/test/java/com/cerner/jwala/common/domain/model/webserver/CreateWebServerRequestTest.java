package com.cerner.jwala.common.domain.model.webserver;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.request.webserver.CreateWebServerRequest;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class CreateWebServerRequestTest {

    private static final String HOST = "host";
    private static final String NAME = "name";
    private static final Path STATUS_PATH = new Path("/statusPath");
    private static final Integer portNumber = 10000;
    private static final Integer httpsPort = 20000;

    private final List<Identifier<Group>> groupIds = new ArrayList<>();

    private final Collection<Identifier<Group>> groupIdsFour = Collections.singletonList(new Identifier<Group>(111L));

    private final CreateWebServerRequest webServer =
            new CreateWebServerRequest(groupIds, NAME, HOST, portNumber, httpsPort, STATUS_PATH,
                    WebServerReachableState.WS_UNREACHABLE);
    private final CreateWebServerRequest webServerTen =
            new CreateWebServerRequest(groupIdsFour, "otherName", HOST, portNumber, httpsPort,
                    STATUS_PATH, WebServerReachableState.WS_UNREACHABLE);

    @Test
    public void testGetGroups() {
        assertEquals(0, webServer.getGroups().size());
    }

    @Test
    public void testGetName() {
        assertEquals(NAME, webServer.getName());
    }

    @Test
    public void testGetHost() {
        assertEquals(HOST, webServer.getHost());
    }

    @Test
    public void testGetPort() {
        assertEquals(portNumber, webServer.getPort());
    }

    @Test
    public void testGetStatusPath() {
        assertEquals(STATUS_PATH, webServer.getStatusPath());
    }

    @Test
    public void testValidateCommand() {
        webServerTen.validate();
    }

    @Test (expected = BadRequestException.class)
    public void testValidateCommandNoGroupIds() {
        webServer.validate();
    }

    @Test(expected = BadRequestException.class)
    public void testInvalidPath() {
        final CreateWebServerRequest invalidPath =
                new CreateWebServerRequest(groupIdsFour, "otherName", HOST, 0, 0, new Path("abc"),
                        WebServerReachableState.WS_UNREACHABLE);
        invalidPath.validate();
    }

    @Test(expected = BadRequestException.class)
    public void testInvalidFileSystemPath() {
        final CreateWebServerRequest invalidPath =
                new CreateWebServerRequest(groupIdsFour, "otherName", HOST, 0, 0, new Path("/abc"),
                        WebServerReachableState.WS_UNREACHABLE);
        invalidPath.validate();
    }
}
