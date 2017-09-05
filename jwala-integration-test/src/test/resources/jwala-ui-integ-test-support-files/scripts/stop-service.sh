#!/bin/bash

JWALA_EXIT_CODE_NO_SUCH_SERVICE=123
JWALA_EXIT_CODE_ABNORMAL_SUCCESS=126
JWALA_EXIT_CODE_NO_OP=127
JWALA_EXIT_CODE_SUCCESS=0
JWALA_EXIT_CODE_KILL=255

if [ "$1" = "" -o "$2" = "" ]; then
    /usr/bin/echo $0 not invoked with service name and timeout in seconds.
    exit $JWALA_EXIT_CODE_NO_OP
fi
export JVMINST=`sc queryex $1 | /usr/bin/head -1 | /usr/bin/awk '{ sub(/:/,"",$4); print $4 }'`
export JVMPID=`sc queryex $1 | /usr/bin/grep PID | /usr/bin/awk '{ print $3 }'`
if [ "$JVMINST" = "1060" ]; then
    sc queryex $1
    /usr/bin/echo Service $1 not installed on server
    exit $JWALA_EXIT_CODE_NO_SUCH_SERVICE
elif [ "$JVMPID" -ne "0" ]; then
    sc stop $1 > /dev/null
    export SCRETURN=$?
    if [ "$SCRETURN" -ne "0" ]; then
        /usr/bin/echo `sc stop $1`
        exit $SCRETURN
    fi
    for (( c=1; c<=$2; c++ ))
    do
        /usr/bin/sleep 1
        export JVMNEWPID=`sc queryex  $1 | /usr/bin/grep PID | /usr/bin/awk '{ print $3 }'`
        if [ $JVMNEWPID -ne $JVMPID ]; then
            /usr/bin/echo Successfully stopped service $1
            exit $JWALA_EXIT_CODE_SUCCESS
        fi
    done
    ( sc query $1 | /usr/bin/tail -8 )
    /usr/bin/kill -9 -f $JVMPID
    /usr/bin/echo Service $1 with process id $JVMPID terminated.
    exit $JWALA_EXIT_CODE_KILL
else
    /usr/bin/echo The service has not been started.
    exit $JWALA_EXIT_CODE_ABNORMAL_SUCCESS
fi
