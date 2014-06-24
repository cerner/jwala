package com.siemens.cto.aem.service.webserver

import com.siemens.cto.aem.domain.model.app.Application
import com.siemens.cto.aem.service.webserver.exception.HttpdConfigTemplateNotFoundException

/**
 * Unit test for {@link HttpdConfigGenerator}
 *
 * Created by Z003BPEJ on 6/23/14.
 */
class HttpdConfigGeneratorTest extends GroovyTestCase {

    def List<Application> apps
    def result

    void setUp() {
        apps = new ArrayList<>()
        apps.add(new Application(null, "hello-world-1", null, "/hello-world-1", null))
        apps.add(new Application(null, "hello-world-2", null, "/hello-world-2", null))

        result = this.getClass().getResource("/httpd.conf").text.replaceAll("\\s+","")
    }

    void testGetHttpdConf() {
        assert result == HttpdConfigGenerator.getHttpdConf("/httpd-conf.tpl", apps).replaceAll("\\s+","")
    }

    void testGetHttpdConfMissingTemplate() {
        shouldFail(HttpdConfigTemplateNotFoundException) {
            HttpdConfigGenerator.getHttpdConf("/httpd-conf-fictitious.tpl", apps)
        }
    }

}