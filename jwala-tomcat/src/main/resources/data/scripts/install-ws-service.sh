#!/bin/bash

JWALA_EXIT_CODE_NO_OP=127
JWALA_EXIT_CODE_SUCCESS=0
JWALA_EXIT_CODE_FAILED=1

cygwin=false
linux=false
case "`uname`" in
CYGWIN*) cygwin=true;;
Linux*) linux=true;;
esac

if [ "$1" = "" ]; then
    echo $0 not invoked with service name
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
  `pwd -P`/.jwala/install-service-http.bat

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
  	pushd $(dirname $0)
  sed -e "s/@APACHE_HOME@/${3//\//\\/}/g" -e "s/@HTTPD_CONF@/${2//\//\\/}\\/httpd.conf/g" linux/httpd-ws-service.sh> $1
  /bin/chmod 755 $1
  /usr/bin/sudo cp $1 /etc/init.d
  /usr/bin/sudo /sbin/chkconfig --add $1
  popd
fi