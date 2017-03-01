package com.cerner.jwala.service.balancermanager.impl.xml.data;

import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link Manager}
 * Created by JC043760 on 2/14/2017.
 */
public class ManagerTest {

    @Test
    public void testManager() throws JAXBException, IOException {
        final JAXBContext jaxbContext = JAXBContext.newInstance(Manager.class);
        final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        final Manager mgr = (Manager) jaxbUnmarshaller.unmarshal(this.getClass().getResourceAsStream("/balancermanager/balancer-manager-response.xml"));
        assertEquals("Manager{balancers=[Balancer{name='balancer://lb-health-check-4.0', stickysession='JSESSIONID', nofailover='On', timeout='0', lbmethod='byrequests', scolonpathdelim='On', workers=[Worker{name='https://somehost0057:9101/hct', scheme='https', hostname='somehost0057', loadfactor=1, port=9101, min=0, smax=1000, max=1000, ttl=300, keepalive='On', status='OK', retries=0, lbstatus=0, transferred=0, read=0, elected=0, route='SOMEHOST-HEALTH-CHECK-4.0-somehost0057-1', redirect='', busy=0, lbset=0, retry=0}, Worker{name='https://somehost0057:9111/hct', scheme='https', hostname='somehost0057', loadfactor=1, port=9111, min=0, smax=1000, max=1000, ttl=300, keepalive='On', status='OK', retries=0, lbstatus=0, transferred=0, read=0, elected=0, route='SOMEHOST-HEALTH-CHECK-4.0-somehost0057-2', redirect='', busy=0, lbset=0, retry=0}, Worker{name='https://somehost0057:9121/hct', scheme='https', hostname='somehost0057', loadfactor=1, port=9121, min=0, smax=1000, max=1000, ttl=300, keepalive='On', status='OK', retries=0, lbstatus=0, transferred=0, read=0, elected=0, route='SOMEHOST-HEALTH-CHECK-4.0-somehost0057-3', redirect='', busy=0, lbset=0, retry=0}, Worker{name='https://somehost0058:9101/hct', scheme='https', hostname='somehost0058', loadfactor=1, port=9101, min=0, smax=1000, max=1000, ttl=300, keepalive='On', status='OK', retries=0, lbstatus=0, transferred=0, read=0, elected=0, route='SOMEHOST-HEALTH-CHECK-4.0-somehost0058-1', redirect='', busy=0, lbset=0, retry=0}, Worker{name='https://somehost0058:9111/hct', scheme='https', hostname='somehost0058', loadfactor=1, port=9111, min=0, smax=1000, max=1000, ttl=300, keepalive='On', status='OK', retries=0, lbstatus=0, transferred=0, read=0, elected=0, route='SOMEHOST-HEALTH-CHECK-4.0-somehost0058-2', redirect='', busy=0, lbset=0, retry=0}, Worker{name='https://somehost0058:9121/hct', scheme='https', hostname='somehost0058', loadfactor=1, port=9121, min=0, smax=1000, max=1000, ttl=300, keepalive='On', status='OK', retries=0, lbstatus=0, transferred=0, read=0, elected=0, route='SOMEHOST-HEALTH-CHECK-4.0-somehost0058-3', redirect='', busy=0, lbset=0, retry=0}]}, Balancer{name='balancer://lb-health-check-4.0', stickysession='JSESSIONID', nofailover='On', timeout='0', lbmethod='byrequests', scolonpathdelim='On', workers=[]}]}", mgr.toString());
        assertEquals("[Balancer{name='balancer://lb-health-check-4.0', stickysession='JSESSIONID', nofailover='On', timeout='0', lbmethod='byrequests', scolonpathdelim='On', workers=[Worker{name='https://somehost0057:9101/hct', scheme='https', hostname='somehost0057', loadfactor=1, port=9101, min=0, smax=1000, max=1000, ttl=300, keepalive='On', status='OK', retries=0, lbstatus=0, transferred=0, read=0, elected=0, route='SOMEHOST-HEALTH-CHECK-4.0-somehost0057-1', redirect='', busy=0, lbset=0, retry=0}, Worker{name='https://somehost0057:9111/hct', scheme='https', hostname='somehost0057', loadfactor=1, port=9111, min=0, smax=1000, max=1000, ttl=300, keepalive='On', status='OK', retries=0, lbstatus=0, transferred=0, read=0, elected=0, route='SOMEHOST-HEALTH-CHECK-4.0-somehost0057-2', redirect='', busy=0, lbset=0, retry=0}, Worker{name='https://somehost0057:9121/hct', scheme='https', hostname='somehost0057', loadfactor=1, port=9121, min=0, smax=1000, max=1000, ttl=300, keepalive='On', status='OK', retries=0, lbstatus=0, transferred=0, read=0, elected=0, route='SOMEHOST-HEALTH-CHECK-4.0-somehost0057-3', redirect='', busy=0, lbset=0, retry=0}, Worker{name='https://somehost0058:9101/hct', scheme='https', hostname='somehost0058', loadfactor=1, port=9101, min=0, smax=1000, max=1000, ttl=300, keepalive='On', status='OK', retries=0, lbstatus=0, transferred=0, read=0, elected=0, route='SOMEHOST-HEALTH-CHECK-4.0-somehost0058-1', redirect='', busy=0, lbset=0, retry=0}, Worker{name='https://somehost0058:9111/hct', scheme='https', hostname='somehost0058', loadfactor=1, port=9111, min=0, smax=1000, max=1000, ttl=300, keepalive='On', status='OK', retries=0, lbstatus=0, transferred=0, read=0, elected=0, route='SOMEHOST-HEALTH-CHECK-4.0-somehost0058-2', redirect='', busy=0, lbset=0, retry=0}, Worker{name='https://somehost0058:9121/hct', scheme='https', hostname='somehost0058', loadfactor=1, port=9121, min=0, smax=1000, max=1000, ttl=300, keepalive='On', status='OK', retries=0, lbstatus=0, transferred=0, read=0, elected=0, route='SOMEHOST-HEALTH-CHECK-4.0-somehost0058-3', redirect='', busy=0, lbset=0, retry=0}]}, Balancer{name='balancer://lb-health-check-4.0', stickysession='JSESSIONID', nofailover='On', timeout='0', lbmethod='byrequests', scolonpathdelim='On', workers=[]}]", mgr.getBalancers().toString());
        final Manager.Balancer balancer =mgr.getBalancers().get(0);
        assertEquals("byrequests", balancer.getLbmethod());
        assertEquals("balancer://lb-health-check-4.0", balancer.getName());
        assertEquals("On", balancer.getNofailover());
        assertEquals("On", balancer.getScolonpathdelim());
        assertEquals("JSESSIONID", balancer.getStickysession());
        assertEquals("0", balancer.getTimeout());
        final Manager.Balancer.Worker worker = mgr.getBalancers().get(0).getWorkers().get(0);
        assertEquals("https://somehost0057:9101/hct", worker.getName());
        assertEquals(0, worker.getBusy());
        assertEquals(0, worker.getElected());
        assertEquals("somehost0057", worker.getHostname());
        assertEquals("On", worker.getKeepalive());
        assertEquals(0, worker.getLbset());
        assertEquals(0, worker.getLbstatus());
        assertEquals(1, worker.getLoadfactor());
        assertEquals(1000, worker.getMax());
        assertEquals(0, worker.getMin());
        assertEquals(9101, worker.getPort());
        assertEquals(1000, worker.getMax());
        assertEquals(0, worker.getRead());
        assertEquals("", worker.getRedirect());
        assertEquals(0, worker.getElected());
        assertEquals(0, worker.getRetries());
        assertEquals(0, worker.getRetry());
        assertEquals("SOMEHOST-HEALTH-CHECK-4.0-somehost0057-1", worker.getRoute());
        assertEquals(0, worker.getTransferred());
        assertEquals(300, worker.getTtl());
        assertEquals(1000, worker.getSmax());
        assertEquals("OK", worker.getStatus());
        assertEquals("https", worker.getScheme());
    }

}
