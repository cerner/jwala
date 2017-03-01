#!/usr/bin/env bash
cygwin=false
linux=false
case "`uname`" in
CYGWIN*) cygwin=true;;
Linux*) linux=true;;
esac
export JAVA_HOME=$1
export DATA_DIR=$2
export DUMP_FILE=$3
export DUMP_LIVE=$4
export JVM_INSTANCE_PATH=$5
export JVM_NAME=$6
echo '***heapdump-start***'
if $linux; then
	mkdir -p ${DATA_DIR}
	/usr/bin/sudo -u tomcat ${JAVA_HOME}/bin/jmap -dump:${DUMP_LIVE}format=b,file=${DATA_DIR}/${DUMP_FILE} $(<${JVM_INSTANCE_PATH}/logs/catalina.pid)
fi
if $cygwin; then
	export JVMINST=`sc queryex $JVM_NAME | /usr/bin/head -1 | /usr/bin/awk '{ sub(/:/,"",$4); print $4 }'`
    export JVMPID=`sc queryex $JVM_NAME | /usr/bin/grep PID | /usr/bin/awk '{ print $3 }'`
    mkdir -p ${DATA_DIR}
    ${JAVA_HOME}/bin/jmap -dump:${DUMP_LIVE}format=b,file=${DATA_DIR}/${DUMP_FILE} ${JVMPID}
fi
echo '***heapdump-end***'