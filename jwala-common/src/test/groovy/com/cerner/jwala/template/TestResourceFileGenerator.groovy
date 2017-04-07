package com.cerner.jwala.template

import com.cerner.jwala.common.domain.model.app.Application
import com.cerner.jwala.common.domain.model.group.Group
import com.cerner.jwala.common.domain.model.group.History
import com.cerner.jwala.common.domain.model.id.Identifier
import com.cerner.jwala.common.domain.model.jvm.Jvm
import com.cerner.jwala.common.domain.model.jvm.JvmState
import com.cerner.jwala.common.domain.model.path.Path
import com.cerner.jwala.common.domain.model.resource.ResourceGroup
import com.cerner.jwala.common.domain.model.webserver.WebServer
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState
import com.cerner.jwala.common.properties.ApplicationProperties

class TestResourceFileGenerator extends GroovyTestCase {

    static final String INSTALL_SERVICE_WSBAT_TEMPLATE_TPL_PATH = "/install-service-http.bat.tpl";
    static final String INSTALL_SERVICE_JVM_TEMPLATE_TPL_PATH = "/install-service-jvm.bat.tpl";

    LinkedHashSet<Jvm> jvms
    LinkedHashSet<WebServer> webServers
    LinkedHashSet<Group> groupHashSet
    WebServer webServer
    Jvm jvm
    Application app
    ResourceGroup resourceGroup;
    Group group;

    void setUp() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources");
        println System.getProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
        File f = new File(System.getProperty(ApplicationProperties.PROPERTIES_ROOT_PATH) + "/vars.properties")
        println f.absolutePath
        println f.exists()
        def apps = new LinkedHashSet<Application>()
        groupHashSet = new LinkedHashSet<Group>();

        createTestJvmsAndWebServers(groupHashSet)
        group = new Group(new Identifier<Group>(1111L), "groupName", jvms, webServers, new HashSet<History>(), apps)
        groupHashSet.add(group);
        app = new Application(new Identifier<Application>(111L), "hello-world-1", "d:/jwala/app/archive", "/hello-world-1", group, true, true, false, "testWar.war")
        app.setParentJvm(jvm);

        apps.add(app)
        apps.add(new Application(new Identifier<Application>(222L), "hello-world-2", "d:/jwala/app/archive", "/hello-world-2", group, true, true, false, "testWar.war"))
        apps.add(new Application(new Identifier<Application>(333L), "hello-world-3", "d:/jwala/app/archive", "/hello-world-3", group, true, true, false, "testWar.war"))

        // do it again to associate the group with the jvms and web servers
        createTestJvmsAndWebServers(groupHashSet, group)
    }

    private void createTestJvmsAndWebServers(HashSet<Group> groupHashSet) {
        webServer = new WebServer(new Identifier<WebServer>(1L), groupHashSet, "Apache2.4", "localhost", 80, 443,
                new Path("/statusPath"), WebServerReachableState.WS_UNREACHABLE);
        jvm = new Jvm(new Identifier<Jvm>(11L), "tc1", "someHostGenerateMe", groupHashSet, 11010, 11011, 11012, -1, 11013,
                new Path("/statusPath"), "EXAMPLE_OPTS=%someEvn%/someVal", JvmState.JVM_STOPPED, "", null, null, null, null, null, "", null)

        webServers = new HashSet<>()
        webServers.add(webServer)

        jvms = new HashSet<>()
        jvms.add(jvm)
        jvms.add(new Jvm(new Identifier<Jvm>(22L), "tc2", "someHostGenerateMe", groupHashSet, 11020, 11021, 11022, -1, 11023,
                new Path("/statusPath"), "EXAMPLE_OPTS=%someEvn%/someVal", JvmState.JVM_STOPPED, "", null, null, null, null, null, "", null))

    }

    private void createTestJvmsAndWebServers(HashSet<Group> groupHashSet, Group group) {
        groupHashSet.add(group);
        webServer = new WebServer(new Identifier<WebServer>(1L), "localhost", "Apache2.4", 80, 443,
                new Path("/statusPath"), WebServerReachableState.WS_UNREACHABLE);
        jvm = new Jvm(new Identifier<Jvm>(11L), "tc1", "someHostGenerateMe", groupHashSet, 11010, 11011, 11012, -1, 11013,
                new Path("/statusPath"), "EXAMPLE_OPTS=%someEvn%/someVal", JvmState.JVM_STOPPED, "", null, null, null, null, null, "", null)

        webServers = new HashSet<>()
        webServers.add(webServer)

        jvms = new HashSet<>()
        jvms.add(jvm)
        jvms.add(new Jvm(new Identifier<Jvm>(22L), "tc2", "someHostGenerateMe", groupHashSet, 11020, 11021, 11022, -1, 11023,
                new Path("/statusPath"), "EXAMPLE_OPTS=%someEvn%/someVal", JvmState.JVM_STOPPED, "", null, null, null, null, null, null, null))

    }

    void testGenerateHttpdConfConfigFile() {
        File httpdTemplate = new File("./src/test/resources/HttpdConfTemplate.tpl");
        resourceGroup = new ResourceGroup(new ArrayList<Group>(groupHashSet));
        def generatedText = ResourceFileGenerator.generateResourceConfig("HttpdConfTemplate.tpl", httpdTemplate.text, resourceGroup, webServer);
        def expectedText = new File("./src/test/resources/HttpdConfTemplate-EXPECTED.conf").text
        assertEquals(removeCarriageReturnsAndNewLines(expectedText), removeCarriageReturnsAndNewLines(generatedText));
    }

    void testGenerateInstallServiceBatConfigFile(){
        File httpdTemplate = new File("./src/test/resources" + INSTALL_SERVICE_JVM_TEMPLATE_TPL_PATH);
        resourceGroup = new ResourceGroup(new ArrayList<Group>(groupHashSet));
        def generatedText = ResourceFileGenerator.generateResourceConfig(httpdTemplate.getName(), httpdTemplate.text, resourceGroup, jvm);
        def expectedText = new File("./src/test/resources/install-service-jvm-EXPECTED.bat").text
        assertEquals(removeCarriageReturnsAndNewLines(expectedText), removeCarriageReturnsAndNewLines(generatedText));
    }

    void testGenerateInstallServiceWSBatConfigFile() {
        File httpdTemplate = new File("./src/test/resources" + INSTALL_SERVICE_WSBAT_TEMPLATE_TPL_PATH);
        resourceGroup = new ResourceGroup(new ArrayList<Group>(groupHashSet));
        def generatedText = ResourceFileGenerator.generateResourceConfig(httpdTemplate.getName(), httpdTemplate.text, resourceGroup, webServer);
        def expectedText = new File("./src/test/resources/install-service-http-EXPECTED.bat").text
        assertEquals(removeCarriageReturnsAndNewLines(expectedText), removeCarriageReturnsAndNewLines(generatedText));
    }

    void testGenerateSetenvBatConfigFile() {
        File httpdTemplate = new File("./src/test/resources/SetenvBatTemplate.tpl");
        resourceGroup = new ResourceGroup(new ArrayList<Group>(groupHashSet));
        def generatedText = ResourceFileGenerator.generateResourceConfig(httpdTemplate.getName(), httpdTemplate.text, resourceGroup, jvm);
        def expectedText = new File("./src/test/resources/SetenvBatTemplate-EXPECTED.bat").text
        assertEquals(removeCarriageReturnsAndNewLines(expectedText), removeCarriageReturnsAndNewLines(generatedText));
    }

    void testGenerateLargeWebXmlConfigFile() {
        File httpdTemplate = new File("./src/test/resources/web.xml.tpl");
        resourceGroup = new ResourceGroup(new ArrayList<Group>(groupHashSet));
        def generatedText = ResourceFileGenerator.generateResourceConfig("web.xml.tpl", httpdTemplate.text, resourceGroup, jvm);
        def expectedText = new File("./src/test/resources/web-EXPECTED.xml").text
        assertEquals(removeCarriageReturnsAndNewLines(expectedText), removeCarriageReturnsAndNewLines(generatedText));
    }

    private static String removeCarriageReturnsAndNewLines(String s) {
        return s.replaceAll("\\r", "").replaceAll("\\n", "")
    }


}
