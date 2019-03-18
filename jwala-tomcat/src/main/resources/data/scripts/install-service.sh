#!/bin/bash
JWALA_EXIT_CODE_NO_OP=127
JWALA_EXIT_CODE_SUCCESS=0
JWALA_EXIT_CODE_FAILED=1
JWALA_EXIT_CODE_INVALID_OS=124
TOMCAT_USER="${TOMCAT_USER:-tomcat}"
# Define the tomcat group
TOMCAT_GROUP="${TOMCAT_GROUP:-`id -gn $TOMCAT_USER`}"
TOMCAT_HOME=$2/$1/$3
cygwin=false
linux=false
case "`uname`" in
CYGWIN*) cygwin=true;;
Linux*) linux=true;;
esac

if [ "$1" = "" -o "$2" = "" ]; then
    echo $0 not invoked with service name or instances folder path
    exit $JWALA_EXIT_CODE_NO_OP;
fi

if $cygwin; then
  export JVMINST=`sc queryex $1 | head -1 | awk '{ sub(/:/,"",$4); print $4 }'`
  if [ "$JVMINST" = "1060" ]; then
    echo Service $1 not installed on server, continuing with install
  else
    echo Service $1 already exists
    exit $JWALA_EXIT_CODE_FAILED
  fi

  $2/$1/$3/bin/install-service.bat "$4" "${5//&/^&}"
  export EXIT_CODE=$?
  if [ "$EXIT_CODE" -ne "0" ]; then
    echo Failed to install service $1
    exit $JWALA_EXIT_CODE_FAILED
  fi

  for (( c=1; c<=5; c++ ))
  do
    sleep 1
  done

  export JVMINST=`sc queryex $1 | head -1 | awk '{ sub(/:/,"",$4); print $4 }'`
  if [ "$JVMINST" = "1060" ]; then
      echo Failed to install service $1
      exit $JWALA_EXIT_CODE_FAILED
  fi
  echo Invoke of service $1 was successful
  exit $JWALA_EXIT_CODE_SUCCESS
fi

if $linux; then
  os_version=$(uname -r)
  linux_7="el7"
  service_file=$1".service"
  if [[ $os_version =~ $linux_7 ]];then
    pushd $(dirname $0)
    chown -R ${TOMCAT_USER}:${TOMCAT_GROUP} $TOMCAT_HOME/work
    chown -R ${TOMCAT_USER}:${TOMCAT_GROUP} $TOMCAT_HOME/logs
    chown -R ${TOMCAT_USER}:${TOMCAT_GROUP} $TOMCAT_HOME/temp
    chown -R ${TOMCAT_USER}:${TOMCAT_GROUP} $TOMCAT_HOME/data
    sed -e "s/@TOMCAT_HOME@/${2//\//\\/}\\/$1\\/$3/g" -e "s/@JVM_NAME@/$1/g" linux/jvm-service.sh> $service_file
    chmod 755 $service_file
    sudo cp $service_file /etc/systemd/system
    sudo systemctl daemon-reload
    sudo systemctl enable $1
  else
    echo $os_version found but was expecting $linux_7
    echo Exiting with ERROR CODE $JWALA_EXIT_CODE_INVALID_OS
    exit $JWALA_EXIT_CODE_INVALID_OS;
  fi 
  popd
fi
