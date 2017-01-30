SET JAVA_HOME=d:\stp\jdk1.8.0_92
SET JRE_HOME=%JAVA_HOME%\jre

SET CATALINA_HOME=d:/stp/app/instances/${jvm.jvmName}/apache-tomcat-7.0.55
SET CATALINA_OPTS=-XX:PermSize=512m -XX:MaxPermSize=512m 

SET PROPERTIES_ROOT_PATH=d:\stp\app\properties
SET STP_OPTS=-DPROPERTIES_ROOT_PATH=%PROPERTIES_ROOT_PATH%

SET STP_PROPERTIES_DIR=d:\stp\app\properties
SET STP_OPTS=%STP_OPTS% -DSTP_PROPERTIES_DIR=%STP_PROPERTIES_DIR%

SET LOG_ROOT_DIR=d:/stp/app/instances/${jvm.jvmName}/apache-tomcat-7.0.55/logs
SET LOG_OPTS=-Dlog.root.dir=%LOG_ROOT_DIR%
SET LOG_OPTS=%LOG_OPTS% -Dlog4j.configuration=log4j.xml -Dlog4j.debug=true

SET LOGIN_CONFIG=-Djava.security.auth.login.config=%PROPERTIES_PATH%\jaas.config

SET APR_OPTS=-Djava.library.path=%CATALINA_HOME%\bin

SET PROD_OPTS=%APR_OPTS% %STP_OPTS% %SSL_OPTS% %JMX_OPTS% %ATOMIKOS_OPTS% %CATALINA_OPTS% %LOG_OPTS% %LOGIN_CONFIG% %APP_DYNAMICS_OPTS%

SET DEBUG_OPTS=%PROD_OPTS%

SET JAVA_SERVICE_OPTS=%PROD_OPTS: =#%
SET JAVA_OPTS=%DEBUG_OPTS%
