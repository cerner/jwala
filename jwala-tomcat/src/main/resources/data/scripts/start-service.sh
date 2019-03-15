#!/bin/bash

# return codes
JWALA_EXIT_CODE_NO_SUCH_SERVICE=123
JWALA_EXIT_CODE_ABNORMAL_SUCCESS=126
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

if [ "$1" = "" ]; then
    echo $0 not invoked with service name
    exit $JWALA_EXIT_CODE_NO_OP;
fi

if $cygwin; then
  export JVMINST=`sc queryex $1 | head -1 | /usr/bin/awk '{ sub(/:/,"",$4); print $4 }'`
  if [ "$JVMINST" = "1060" ]; then
      echo `sc queryex $1`
      exit $JWALA_EXIT_CODE_NO_SUCH_SERVICE
  else
      sc start $1 &> output
      if [ $? -ne 0 ]; then
          export SCRESULT=`sc start $1 | head -1 | /usr/bin/awk '{ sub(/:/,"",$4); print $4 }'`
          if [ "$SCRESULT" = "1056" ]; then
              echo Service $1 already started.
              rm output
              exit $JWALA_EXIT_CODE_ABNORMAL_SUCCESS
          fi
          if [ -s output]; then
              echo $1 failed to start and was not already started
          else
              cat output
          fi
          rm output
          exit $JWALA_EXIT_CODE_FAILED
      else
          echo Call to start service $1 was successful
          rm output
          exit $JWALA_EXIT_CODE_SUCCESS
      fi
  fi
fi

if $linux; then
  os_version=$(uname -r)
  linux_7="el7"
  if [[ $os_version =~ $linux_7 ]];then
    sudo systemctl start $1
  else
    echo $os_version found but was expecting $linux_7
    echo Exiting with ERROR CODE $JWALA_EXIT_CODE_INVALID_OS
    exit $JWALA_EXIT_CODE_INVALID_OS;
  fi
fi
