#!/bin/bash

JWALA_EXIT_CODE_NO_SUCH_SERVICE=123
JWALA_EXIT_CODE_ABNORMAL_SUCCESS=126
JWALA_EXIT_CODE_NO_OP=127
JWALA_EXIT_CODE_SUCCESS=0
JWALA_EXIT_CODE_KILL=255
JWALA_EXIT_CODE_INVALID_OS=124

cygwin=false
linux=false
case "`uname`" in
CYGWIN*) cygwin=true;;
Linux*) linux=true;;
esac

if [ "$1" = "" -o "$2" = "" ]; then
    echo $0 not invoked with service name and timeout in seconds.
    exit $JWALA_EXIT_CODE_NO_OP
fi

if $cygwin; then
  export JVMINST=`sc queryex $1 | head -1 | /usr/bin/awk '{ sub(/:/,"",$4); print $4 }'`
  export JVMPID=`sc queryex $1 | grep PID | /usr/bin/awk '{ print $3 }'`
  if [ "$JVMINST" = "1060" ]; then
      sc queryex $1
      echo Service $1 not installed on server
      exit $JWALA_EXIT_CODE_NO_SUCH_SERVICE
  elif [ "$JVMPID" -ne "0" ]; then
      sc stop $1 > /dev/null
      export SCRETURN=$?
      if [ "$SCRETURN" -ne "0" ]; then
          echo `sc stop $1`
          exit $SCRETURN
      fi
      for (( c=1; c<=$2; c++ ))
      do
          sleep 1
          export JVMNEWPID=`sc queryex  $1 | grep PID | /usr/bin/awk '{ print $3 }'`
          if [ $JVMNEWPID -ne $JVMPID ]; then
              echo Successfully stopped service $1
              exit $JWALA_EXIT_CODE_SUCCESS
          fi
      done
      ( sc query $1 | tail -8 )
      kill -9 -f $JVMPID
      echo Service $1 with process id $JVMPID terminated.
      exit $JWALA_EXIT_CODE_KILL
  else
      echo The service has not been started.
      exit $JWALA_EXIT_CODE_ABNORMAL_SUCCESS
  fi
fi

if $linux; then
  os_version=$(uname -r)
  linux_7="el7"
  if [[ $os_version =~ $linux_7 ]];then
    sudo sudo systemctl stop $1
  else
    echo $os_version found but was expecting $linux_7
    echo Exiting with ERROR CODE $JWALA_EXIT_CODE_INVALID_OS
    exit $JWALA_EXIT_CODE_INVALID_OS;
  fi
fi
