#! /bash/sh
parent_path=$( cd "$(dirname "${BASH_SOURCE}")" ; pwd -P )
if [ -z "$JAVA_HOME" ]; then
	export JAVA_HOME=/opt/ctp/jdk1.8.0_92
fi
echo "using JAVA_HOME $JAVA_HOME" 
export JRE_HOME=$JAVA_HOME/jre

export CATALINA_HOME=$parent_path/..
export CATALINA_BASE=$parent_path/..

export START_PATH=$CATALINA_HOME
export LD_LIBRARY_PATH=$CATALINA_HOME/lib:$LD_LIBRARY_PATH

export JMX_OPTS="-Dcom.sun.management.jmxremote.ssl=false"
export JMX_OPTS="${JMX_OPTS} -Dcom.sun.management.jmxremote.authenticate=false"

export SSL_OPTS="-Dhttps.protocols=TLSv1.2"

export SSL_DEBUG_OPTS="-Djavax.net.debug=ssl"

export CATALINA_OPTS="-XX:PermSize=512m -XX:MaxPermSize=512m"
export JWALA_OPTS=-DPROPERTIES_ROOT_PATH=../data/properties

export LOG_ROOT_DIR=../logs
export LOG_OPTS="-Dlog.root.dir=${LOG_ROOT_DIR}"
export LOG_OPTS="${LOG_OPTS} -Dlog4j.configuration=log4j.xml -Dlog4j.debug=true"

export LOGIN_CONFIG="-Djava.security.auth.login.config=${PROPERTIES_PATH}/jaas.config"

export APR_OPTS="-Djava.library.path=${CATALINA_HOME}/lib"

export PROD_OPTS="${APR_OPTS} ${JWALA_OPTS} ${SSL_OPTS} ${JMX_OPTS} ${CATALINA_OPTS} ${LOG_OPTS} ${LOGIN_CONFIG} "
export DEBUG_OPTS="${PROD_OPTS}"
echo $PROD_OPTS
export JAVA_SERVICE_OPTS="${PROD_OPTS}"
export JAVA_OPTS="${DEBUG_OPTS}"
