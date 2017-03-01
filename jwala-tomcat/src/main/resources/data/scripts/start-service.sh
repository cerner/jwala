#!/bin/bash

# return codes
JWALA_EXIT_CODE_NO_SUCH_SERVICE=123
JWALA_EXIT_CODE_ABNORMAL_SUCCESS=126
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
  export JVMINST=`sc queryex $1 | /usr/bin/head -1 | /usr/bin/awk '{ sub(/:/,"",$4); print $4 }'`
  if [ "$JVMINST" = "1060" ]; then
      /usr/bin/echo `sc queryex $1`
      exit $JWALA_EXIT_CODE_NO_SUCH_SERVICE
  else
      sc start $1 &> output
      if [ $? -ne 0 ]; then
          export SCRESULT=`sc start $1 | /usr/bin/head -1 | /usr/bin/awk '{ sub(/:/,"",$4); print $4 }'`
          if [ "$SCRESULT" = "1056" ]; then
              /usr/bin/echo Service $1 already started.
              rm output
              exit $JWALA_EXIT_CODE_ABNORMAL_SUCCESS
          fi
          if [ -s output]; then
              /usr/bin/echo $1 failed to start and was not already started
          else
              /usr/bin/cat output
          fi
          rm output
          exit $JWALA_EXIT_CODE_FAILED
      else
          /usr/bin/echo Call to start service $1 was successful
          rm output
          exit $JWALA_EXIT_CODE_SUCCESS
      fi
  fi
fi

if $linux; then
  /usr/bin/sudo /sbin/service $1 start
fi
