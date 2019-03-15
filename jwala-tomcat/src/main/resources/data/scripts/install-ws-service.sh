#!/bin/bash

JWALA_EXIT_CODE_NO_OP=127
JWALA_EXIT_CODE_SUCCESS=0
JWALA_EXIT_CODE_FAILED=1
JWALA_EXIT_CODE_INVALID_OS=124

cygwin=false
linux=false
case "`uname`" in
CYGWIN*) cygwin=true;;
Linux*) linux=true;;
esac

if [ "$1" = "" -o "$2" == "" -o "$3" == "" ]; then
    echo $0 not invoked with service name or the httpd.conf path or the apache httpd path
    exit $JWALA_EXIT_CODE_NO_OP;
fi

if $cygwin; then
  export WSINST=`sc queryex $1 | head -1 | awk '{ sub(/:/,"",$4); print $4 }'`
  if [ "$WSINST" = "1060" ]; then
      echo Service $1 not installed on server, continuing with install
  else
      /usr/bin/echo Service $1 already exists. Exiting.
      exit $JWALA_EXIT_CODE_FAILED
  fi

  #pwd -P is a clever unix trick to get the absolute path to this dir
  `pwd -P`/.jwala/$1/install-service-http.bat $2

  for (( c=1; c<=5; c++ ))
  do
    /usr/bin/sleep 1
  done

    export WSINST=`sc queryex $1 | head -1 | awk '{ sub(/:/,"",$4); print $4 }'`
    if [ "$WSINST" = "1060" ]; then
        /usr/bin/echo Failed to install service $1
        exit $JWALA_EXIT_CODE_FAILED
    fi
    /usr/bin/echo Invoke of service $1 was successful
    exit $JWALA_EXIT_CODE_SUCCESS
fi

if $linux; then
  # Need to pass $3 for apache home ex: /opt/ctp/apache-httpd-2.4.20, remote.paths.apache.httpd from vars.properties
  os_version=$(uname -r)
  linux_7="el7"
  if [[ $os_version =~ $linux_7 ]];then
    echo "Linux version 7 found"
    pushd $(dirname $0)
    sed -e "s/@APACHE_HOME@/${3//\//\\/}/g" -e "s/@HTTPD_CONF@/${2//\//\\/}/g" -e "s/@WSNAME@/$1/g" linux/httpd-ws-service.sh> $1
    /bin/chmod 755 $1
    /usr/bin/sudo cp $1 /etc/systemd/system
    /usr/bin/sudo systemctl enable $1
  else
    echo $os_version found but was expecting $linux_7
    echo Exiting with ERROR CODE $JWALA_EXIT_CODE_INVALID_OS
    exit $JWALA_EXIT_CODE_INVALID_OS;
  fi
  popd
fi
