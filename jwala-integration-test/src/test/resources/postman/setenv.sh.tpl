#! /bash/sh
parent_path=$( cd "$(dirname "${BASH_SOURCE}")" ; pwd -P )

export JAVA_HOME="/opt/ctp/jdk1.8.0_92"
export JRE_HOME="\${JAVA_HOME}/jre"
export STP_HOME="/opt/ctp"

export CATALINA_HOME="\${parent_path}/.."
export CATALINA_BASE="\${parent_path}/.."
export CATALINA_PID="\${CATALINA_HOME}/logs/catalina.pid"

export START_PATH="\${CATALINA_HOME}"
export LD_LIBRARY_PATH="\${CATALINA_HOME}/lib:${LD_LIBRARY_PATH}"

export JMX_OPTS="-Dcom.sun.management.jmxremote.ssl=false"
export JMX_OPTS="\${JMX_OPTS} -Dcom.sun.management.jmxremote.authenticate=false"

export SSL_OPTS="-Dhttps.protocols=TLSv1.2"

export SSL_DEBUG_OPTS="-Djavax.net.debug=ssl"

export ATOMIKOS_OPTS="-Dcom.atomikos.icatch.tm_unique_name=${jvm.jvmName}"

export STP_PS_LOC="/opt/ctp/app/properties/propertySource.properties"

export CATALINA_OPTS="-XX:PermSize=512m -XX:MaxPermSize=512m -DSTP_HOME=\${STP_HOME} -Dgsm.classloader.url=\${STP_HOME}/app/lib/ctp_platform_tc7-1.2.14/gsm -Dcom.siemens.cto.infrastructure.properties.propertySourceLocations=\${STP_PS_LOC}"

export PROPERTIES_ROOT_PATH="/opt/ctp/app/properties"
export STP_OPTS="-DPROPERTIES_ROOT_PATH=\${PROPERTIES_ROOT_PATH}"

export STP_PROPERTIES_DIR="/opt/ctp/app/properties"
export SET STP_OPTS="\${STP_OPTS} -DSTP_PROPERTIES_DIR=\${STP_PROPERTIES_DIR}"

export LOG_ROOT_DIR="/opt/ctp/app/instances/${jvm.jvmName}/apache-tomcat-7.0.55/logs"
export LOG_OPTS="-Dlog.root.dir=\${LOG_ROOT_DIR}"
export LOG_OPTS="\${LOG_OPTS} -Dlog4j.configuration=log4j.xml -Dlog4j.debug=true"

export LOGIN_CONFIG="-Djava.security.auth.login.config=\${PROPERTIES_PATH}/jaas.config"

export APR_OPTS="-Djava.library.path=\${CATALINA_HOME}/lib"

export APP_DYNAMICS_OPTS="-javaagent:/opt/AppDynamics/current/javaagent.jar"
export APP_DYNAMICS_OPTS="\${APP_DYNAMICS_OPTS} -Dappdynamics.agent.tierName=HEALTH-CHECK -Dappdynamics.agent.nodeName=${jvm.jvmName}"
export APP_DYNAMICS_OPTS="\${APP_DYNAMICS_OPTS} -Dappdynamics.agent.logs.dir=/opt/AppDynamics/logs/"

export PROD_OPTS="\${APR_OPTS} \${STP_OPTS} \${SSL_OPTS} \${JMX_OPTS} \${ATOMIKOS_OPTS} \${CATALINA_OPTS} \${LOG_OPTS} \${LOGIN_CONFIG} \${APP_DYNAMICS_OPTS}"

export DEBUG_OPTS="\${PROD_OPTS}"

export JAVA_SERVICE_OPTS="\${PROD_OPTS}"
export JAVA_OPTS="\${DEBUG_OPTS}"

