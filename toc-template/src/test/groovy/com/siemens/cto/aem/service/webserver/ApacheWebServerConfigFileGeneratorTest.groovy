package com.siemens.cto.aem.service.webserver

import com.siemens.cto.aem.domain.model.app.Application
import com.siemens.cto.aem.domain.model.group.LiteGroup
import com.siemens.cto.aem.domain.model.jvm.Jvm
import com.siemens.cto.aem.service.webserver.exception.HttpdConfigTemplateNotFoundException

/**
 * Unit test for {@link ApacheWebServerConfigFileGenerator}
 *
 * Created by Z003BPEJ on 6/23/14.
 */
class ApacheWebServerConfigFileGeneratorTest extends GroovyTestCase {

    List<Jvm> jvms
    List<Application> apps

    void setUp() {
        jvms = new ArrayList<>()
        jvms.add(new Jvm(null, "tc1", "165.226.8.129", new HashSet<LiteGroup>(), null, null, null, null, 8009))
        jvms.add(new Jvm(null, "t c 2", "165.22 6.8.129", new HashSet<LiteGroup>(), null, null, null, null, 8109))

        apps = new ArrayList<>()
        apps.add(new Application(null, "hello-world-1", null, "/hello-world-1", null))
        apps.add(new Application(null, "hello-world-2", null, "/hello-world-2", null))
        apps.add(new Application(null, "hello-world-3", null, "/hello-world-3", null))
    }

    void testGetHttpdConf() {
        final String refFileText = removeCarriageReturnsAndNewLines(this.getClass().getResource("/httpd.conf").text)
        assert refFileText == removeCarriageReturnsAndNewLines(
                ApacheWebServerConfigFileGenerator.getHttpdConf("Apache2.4", "/httpd-conf.tpl", apps))
    }

    void testGetHttpdConfWithSsl() {
        final String refFileText = removeCarriageReturnsAndNewLines(this.getClass().getResource("/httpd-ssl.conf").text)
        assert refFileText == removeCarriageReturnsAndNewLines(
                ApacheWebServerConfigFileGenerator.getHttpdConf("Apache2.4", "/httpd-ssl-conf.tpl", apps))
    }

    void testGetHttpdConfMissingTemplate() {
        shouldFail(HttpdConfigTemplateNotFoundException) {
            ApacheWebServerConfigFileGenerator.getHttpdConf("Apache2.4", "/httpd-conf-fictitious.tpl", apps)
        }
    }

    void testGetWorkersProperties() {
        final String refFileText =
                removeCarriageReturnsAndNewLines(this.getClass().getResource("/workers.properties").text)
        assert refFileText.equalsIgnoreCase(
                removeCarriageReturnsAndNewLines(
                    ApacheWebServerConfigFileGenerator
                            .getWorkersProperties("Apache2.4", "/workers-properties.tpl", jvms, apps)
                                .replaceAll("(?m)^[ \\t]*\\r?\\n","")))
    }

    void testGetWorkerPropertiesMissingTemplate() {
        shouldFail(HttpdConfigTemplateNotFoundException) {
            ApacheWebServerConfigFileGenerator
                    .getWorkersProperties("Apache2.4", "/workers-properties-fictitious.tpl", jvms, apps)
        }
    }

    private String removeCarriageReturnsAndNewLines(String s) {
        return s.replaceAll("\\r", "").replaceAll("\\n", "")
    }

}