#!/usr/bin/env bash
cygwin=false
linux=false
case "`uname`" in
CYGWIN*) cygwin=true;;
Linux*) linux=true;;
esac
export JAVA_HOME=$1
export JVM_INSTANCE_DIR=$2
export JVM_NAME=$3
if $linux; then
	echo $(<${JVM_INSTANCE_DIR}/logs/catalina.pid)
	/usr/bin/sudo -u tomcat ${JAVA_HOME}/bin/jstack $(<${JVM_INSTANCE_DIR}/logs/catalina.pid)
fi

if $cygwin; then
    export JVMPID=`sc queryex $JVM_NAME | /usr/bin/grep PID | /usr/bin/awk '{ print $3 }'`
	${JAVA_HOME}/bin/jstack -l ${JVMPID}
fi