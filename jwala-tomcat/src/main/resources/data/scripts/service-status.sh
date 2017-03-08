#!/bin/bash

JWALA_EXIT_CODE_NO_OP=127

cygwin=false
linux=false
case "`uname`" in
CYGWIN*) cygwin=true;;
Linux*) linux=true;;
esac

if [ "$1" = "" ]; then
    echo $0 not invoked with service name
    exit $JWALA_EXIT_CODE_NO_OP
fi

if $cygwin; then
    sc queryex $1 | grep STATE
    exit 0
fi

if $linux; then
    /usr/bin/sudo /sbin/service $1 status
fi