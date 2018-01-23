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

if [ ! -d ${JVM_INSTANCE_DIR}/thread_dump_reports} ]; then
      mkdir -p ${JVM_INSTANCE_DIR}/thread_dump_reports
fi

current_time=$(date +"%m-%d-%Y-%T")

if $linux; then
	echo $(<${JVM_INSTANCE_DIR}/logs/catalina.pid)
	/usr/bin/sudo -u tomcat ${JAVA_HOME}/bin/jstack $(<${JVM_INSTANCE_DIR}/logs/catalina.pid) 2>&1 | tee  ${JVM_INSTANCE_DIR}/thread_dump_reports/thread_dump_$current_time

fi

if $cygwin; then
    export JVMPID=`sc queryex $JVM_NAME | /usr/bin/grep PID | /usr/bin/awk '{ print $3 }'`
	${JAVA_HOME}/bin/jstack -l ${JVMPID} 2>&1 |tee ${JVM_INSTANCE_DIR}/thread_dump_reports/thread_dump_$current_time
fi

echo '***message-start***'
echo "Thread-dump file created at $JVM_INSTANCE_DIR//thread_dump_reports/thread_dump_$current_time for jvm $JVM_NAME"
echo '***message-end***'