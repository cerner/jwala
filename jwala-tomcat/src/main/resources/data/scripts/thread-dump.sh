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
export DATA_DIR=$4
export DUMP_FILE=$5

current_time=$(date +"%m-%d-%Y-%T")

if $linux; then
	echo $(<${JVM_INSTANCE_DIR}/logs/catalina.pid)
	thread_dump_output=$(/usr/bin/sudo -u tomcat ${JAVA_HOME}/bin/jstack $(<${JVM_INSTANCE_DIR}/logs/catalina.pid) 2>&1 )
    if echo "$thread_dump_output" | grep -iqF "Full thread dump"; then
	   echo "$thread_dump_output" |tee ${DATA_DIR}/$DUMP_FILE
	   echo "Creating thread-dump file at location: ${DATA_DIR}/$DUMP_FILE, "
	fi

fi

if $cygwin; then
    export JVMPID=`sc queryex $JVM_NAME | /usr/bin/grep PID | /usr/bin/awk '{ print $3 }'`
    thread_dump_output=$(${JAVA_HOME}/bin/jstack -l ${JVMPID} 2>&1 )
    if echo "$thread_dump_output" | grep -iqF "Full thread dump"; then
       echo "$thread_dump_output" |tee ${DATA_DIR}/$DUMP_FILE
	   echo "Creating Thread Dump file at location : ${DATA_DIR}/$DUMP_FILE, "
	fi
fi


