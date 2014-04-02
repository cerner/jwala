package com.siemens.cto.aem.service.webserver.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.common.configuration.TestExecutionProfile;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.service.configuration.TestJpaConfiguration;
import com.siemens.cto.aem.persistence.dao.group.GroupDao;
import com.siemens.cto.aem.persistence.dao.group.impl.jpa.JpaGroupDaoImpl;
import com.siemens.cto.aem.persistence.dao.webserver.WebServerDao;
import com.siemens.cto.aem.persistence.dao.webserver.impl.jpa.JpaWebServerDaoImpl;
import com.siemens.cto.aem.service.webserver.WebServerService;

@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
		WebServerServiceImplIntegrationTest.CommonConfiguration.class,
		TestJpaConfiguration.class })
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@RunWith(SpringJUnit4ClassRunner.class)
@EnableTransactionManagement
@Transactional
public class WebServerServiceImplIntegrationTest {

	@Configuration
	static class CommonConfiguration {

		@Bean
		public WebServerDao getWebServerDao() {
			return new JpaWebServerDaoImpl();
		}

		@Bean
		public GroupDao getGroupDao() {
			return new JpaGroupDaoImpl();
		}
	}	
	
	@Autowired
	private WebServerDao           webServerDao;
	
	private WebServerService   cut;

    @Before
    public void setup() { 
        cut = new WebServerServiceImpl(webServerDao);
    }

	@Test(expected = NotFoundException.class)
	public void testServiceLayer() {
	    cut.getWebServer(new Identifier<WebServer>(0L));
	}
	
	
}
