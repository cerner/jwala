package com.cerner.jwala.service.webserver;

import static com.cerner.jwala.common.domain.model.id.Identifier.id;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.service.webserver.WorkersProperties;

public class WorkersPropertiesTest {

    @Test
    public void testNoAppsWorkerProperties() {
        WorkersProperties.Builder wb = new WorkersProperties.Builder();

        wb.setApps(Collections.<Application>emptyList());
        wb.setJvms(Collections.<Jvm>emptyList());
        wb.setLoadBalancerPortType("ajp");
        wb.setLoadBalancerType("sticky");
        wb.setStatusCssPath("abc.css");
        wb.setStickySession(1 /*STICKY*/ );

        WorkersProperties wp = wb.build();
        wp.toString();

        assertEquals(
"worker.list=status\n\n" +
"worker.status.type=status\n"+
"worker.status.css=abc.css",wp.toString());
    }


    @Test
    public void testOneJvmAppsWorkerProperties() {
        WorkersProperties.Builder wb = new WorkersProperties.Builder();

        Group group = new Group(id(0L, Group.class),"grp");
        Set<Group> groups = new HashSet<>();
        groups.add(new Group(group.getId(), group.getName()));
        Application app = new Application(Identifier.id(0L, Application.class), "", "", "/abc", group, true, true, false, "testWar.war");
        List<Application> apps = new ArrayList<>();
        apps.add(app);
        Jvm jvm = new Jvm(id(0L, Jvm.class), "jvm", "localhost", groups, 8080, 8081, 8082, 8083, 8084, new Path("/abc"),
                "EXAMPLE_OPTS=%someEnv%/someVal", JvmState.JVM_STOPPED, null, null, null, null, null);
        List<Jvm> jvms = new ArrayList<>();
        jvms.add(jvm);
        wb.setApps(apps);
        wb.setJvms(jvms);
        wb.setLoadBalancerPortType("ajp");
        wb.setLoadBalancerType("sticky");
        wb.setStatusCssPath("abc.css");
        wb.setStickySession(1 /*STICKY*/ );

        WorkersProperties wp = wb.build();
        wp.toString();

        assertEquals(
"worker.list=status,lb-\n\n" +
"worker.jvm.type=ajp\n"+
"worker.jvm.host=localhost\n"+
"worker.jvm.port=8084\n\n"+
"worker.lb-.type=sticky\n"+
"worker.lb-.balance_workers=jvm\n"+
"worker.lb-.sticky_session=1\n\n"+
"worker.status.type=status\n"+
"worker.status.css=abc.css",wp.toString());
    }

}