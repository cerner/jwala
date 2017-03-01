<!-- Default application context, please change based on your web application requirements. -->
<Context docBase="\${STP_HOME}/app/webapps/healthcheck-webapp-1.0.1.war">

    <Listener className="com.siemens.cto.infrastructure.atomikos.AtomikosTaskManagerLifecycleListener"/>

    <Environment name="jvmInstanceName"
                 value="CTO-N9SF-LTST-HEALTH-CHECK-4.0-USMLVV1CTO4900-2"
                 type="java.lang.String"
                 override="false"/>

    <Environment name="roleMappingProperties"
        value="d:/stp/app/properties/hctRoleMapping.properties"
        type="java.lang.String"
        override="false" />

    <Resource name="jdbc/hct-xa"
              auth="Container"
              type="com.atomikos.jdbc.AtomikosDataSourceBean"
              factory="com.siemens.cto.infrastructure.atomikos.EnhancedTomcatAtomikosBeanFactory"
              uniqueResourceName="AtomikosJndiXaDataSource2"
              xaDataSourceClassName="com.microsoft.sqlserver.jdbc.SQLServerXADataSource"
              xaProperties.user="\${hct.db.user}"
              xaProperties.password="\${hct.db.password.encrypted}"
              xaProperties.URL="jdbc:sqlserver://\${hct.db.host};DatabaseName=\${hct.db.name};SelectMethod=cursor;"/>

    <Resource factory="org.apache.naming.factory.BeanFactory"
              name="jdbc/hct-ds"
              type="com.atomikos.jdbc.nonxa.AtomikosNonXADataSourceBean"
              url="jdbc:sqlserver://\${hct.db.host};DatabaseName=\${hct.db.name};SelectMethod=cursor;"
              driverClassName="com.microsoft.sqlserver.jdbc.SQLServerDriver"
              uniqueResourceName="healthCheck"
              user="\${hct.db.user}"
              password="\${hct.db.password.encrypted}"/>

    <Resource name="jms/hct-xacf"
              auth="Container"
              type="com.atomikos.jms.AtomikosConnectionFactoryBean"
              factory="com.siemens.cto.infrastructure.atomikos.EnhancedTomcatAtomikosBeanFactory"
              uniqueResourceName="AtomikosJndiJmsXaConnectionFactory-1.0"
              maxPoolSize="3"
              minPoolSize="1"
              ignoreSessionTransactedFlag="false"
              xaConnectionFactoryClassName="com.tibco.tibjms.TibjmsXAConnectionFactory"
              xaProperties.userName="\${hct.ems.user}"
              xaProperties.userPassword="\${hct.ems.password.encrypted}"
              xaProperties.serverUrl="\${hct.ems.url}"
              xaProperties.connAttemptCount="100"
              xaProperties.connAttemptDelay="1000"
              xaProperties.reconnAttemptCount="100"
              xaProperties.reconnAttemptDelay="1000"
              xaProperties.SSLEnableVerifyHost="true"
              xaProperties.SSLEnableVerifyHostName="false"
              xaProperties.SSLTrustedCertificate="\${STP_HOME}/app/data/security/ems/ctorootca.pem" />

    <Resource auth="Container"
              name="jms/hct-cf"
              factory="org.apache.naming.factory.BeanFactory"
              type="com.tibco.tibjms.TibjmsConnectionFactory"
              serverUrl="\${hct.ems.url}"
              userName="\${hct.ems.user}"
              userPassword="\${hct.ems.password.encrypted}"
              connAttemptCount="1"
              connAttemptDelay="1000"
              reconnAttemptCount="1"
              reconnAttemptDelay="1000"
              SSLEnableVerifyHost="true"
              SSLEnableVerifyHostName="false"
              SSLTrustedCertificate="\${STP_HOME}/app/data/security/ems/ctorootca.pem" />

    <Resource auth="Container"
              name="jms/healthCheckServiceDestination"
              factory="org.apache.naming.factory.BeanFactory"
              type="com.tibco.tibjms.TibjmsQueue"
              address="\${hct.ems.serviceQueue}"/>

    <Resource name="jms/healthCheckServiceReplyDestination"
              auth="Container"
              factory="org.apache.naming.factory.BeanFactory"
              type="com.tibco.tibjms.TibjmsQueue"
              address="\${hct.ems.serviceReplyQueue}"/>

    <Resource name="jms/healthCheckStatusDestination"
              auth="Container"
              factory="org.apache.naming.factory.BeanFactory"
              type="com.tibco.tibjms.TibjmsQueue"
              address="\${hct.ems.statusQueue}"/>

    <Resource name="wm/healthWorkManager"
              auth="Container"
              type="commonj.work.WorkManager"
              factory="de.myfoo.commonj.work.FooWorkManagerFactory"
              maxThreads="5"/>

    <Realm className="org.apache.catalina.realm.CombinedRealm">
        <Realm className="com.siemens.cto.security.tomcat.GsmRealm" />
        <Realm className="com.siemens.cto.security.tomcat.RoleMapperRealm"
            connectionURL="\${hct.ldap.url}"
            authentication="simple"
            connectionName="\${hct.ldap.connectionName}"
            connectionPassword="\${hct.ldap.password.encrypted}"
            userBase="\${hct.ldap.userBase}"
            userSearch="(sAMAccountName={0})"
            userRoleName="memberOf"
            userSubtree="true"
            roleBase="\${hct.ldap.roleBase}"
            roleName="sAMAccountName"
            roleNested="true"
            roleSubtree="true"
            roleSearch="(member={0})"
            referrals="follow" />
    </Realm>
</Context>

