package com.cerner.jwala.common.domain.model.balancermanager;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.List;

public class BalancerManagerStateTest {

    @Test
    public void testDrainStatus(){
        BalancerManagerState.GroupDrainStatus.WebServerDrainStatus.JvmDrainStatus jvm1DrainStatus = new BalancerManagerState.GroupDrainStatus.WebServerDrainStatus.JvmDrainStatus("url1","jvm1", "app1", "On", "Off", "Off", "Off");
        BalancerManagerState.GroupDrainStatus.WebServerDrainStatus.JvmDrainStatus jvm2DrainStatus = new BalancerManagerState.GroupDrainStatus.WebServerDrainStatus.JvmDrainStatus("url2","jvm2", "app1", "On", "Off", "Off", "Off");
        BalancerManagerState.GroupDrainStatus.WebServerDrainStatus.JvmDrainStatus jvm3DrainStatus = new BalancerManagerState.GroupDrainStatus.WebServerDrainStatus.JvmDrainStatus("url3","jvm3", "app1", "Off", "Off", "On", "Off");
        BalancerManagerState.GroupDrainStatus.WebServerDrainStatus.JvmDrainStatus jvm4DrainStatus = new BalancerManagerState.GroupDrainStatus.WebServerDrainStatus.JvmDrainStatus("url4","jvm4", "app1", "On", "Off", "Off", "Off");
        List<BalancerManagerState.GroupDrainStatus.WebServerDrainStatus.JvmDrainStatus> jvmDrainStatusList = new ArrayList<>();
        jvmDrainStatusList.add(jvm1DrainStatus);
        jvmDrainStatusList.add(jvm2DrainStatus);
        jvmDrainStatusList.add(jvm3DrainStatus);
        jvmDrainStatusList.add(jvm4DrainStatus);

        BalancerManagerState.GroupDrainStatus.WebServerDrainStatus webServer1DrainStatus = new BalancerManagerState.GroupDrainStatus.WebServerDrainStatus("webServer1", jvmDrainStatusList);
        BalancerManagerState.GroupDrainStatus.WebServerDrainStatus webServer2DrainStatus = new BalancerManagerState.GroupDrainStatus.WebServerDrainStatus("webServer2", jvmDrainStatusList);
        List<BalancerManagerState.GroupDrainStatus.WebServerDrainStatus> webServerDrainStatusList = new ArrayList<>();
        webServerDrainStatusList.add(webServer1DrainStatus);
        webServerDrainStatusList.add(webServer2DrainStatus);

        BalancerManagerState.GroupDrainStatus groupDrainStatus = new BalancerManagerState.GroupDrainStatus("group1",webServerDrainStatusList);
        List<BalancerManagerState.GroupDrainStatus> groupDrainStatusList = new ArrayList<>();
        groupDrainStatusList.add(groupDrainStatus);

        BalancerManagerState balancerManagerState = new BalancerManagerState(groupDrainStatusList);

        assertEquals(getExpectedString(), balancerManagerState.toString());
    }

    private String getExpectedString(){
        return "BalancerManagerState{groups=[BalancerManagerState{groupName='group1', webServers=[WebServerDrainStatus{webServerName='webServer1', webServer=[JvmDrainStatus{jvmName='jvm1', ignoreError='On', drainingMode='Off', disabled='Off', hotStandby='Off', appName='app1', workerUrl='url1'}, JvmDrainStatus{jvmName='jvm2', ignoreError='On', drainingMode='Off', disabled='Off', hotStandby='Off', appName='app1', workerUrl='url2'}, JvmDrainStatus{jvmName='jvm3', ignoreError='Off', drainingMode='Off', disabled='On', hotStandby='Off', appName='app1', workerUrl='url3'}, JvmDrainStatus{jvmName='jvm4', ignoreError='On', drainingMode='Off', disabled='Off', hotStandby='Off', appName='app1', workerUrl='url4'}]}, WebServerDrainStatus{webServerName='webServer2', webServer=[JvmDrainStatus{jvmName='jvm1', ignoreError='On', drainingMode='Off', disabled='Off', hotStandby='Off', appName='app1', workerUrl='url1'}, JvmDrainStatus{jvmName='jvm2', ignoreError='On', drainingMode='Off', disabled='Off', hotStandby='Off', appName='app1', workerUrl='url2'}, JvmDrainStatus{jvmName='jvm3', ignoreError='Off', drainingMode='Off', disabled='On', hotStandby='Off', appName='app1', workerUrl='url3'}, JvmDrainStatus{jvmName='jvm4', ignoreError='On', drainingMode='Off', disabled='Off', hotStandby='Off', appName='app1', workerUrl='url4'}]}]}]}";
    }
}