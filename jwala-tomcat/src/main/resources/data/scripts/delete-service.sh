#!/bin/bash

cygwin=false
linux=false
case "`uname`" in
CYGWIN*) cygwin=true;;
Linux*) linux=true;;
esac

if [ "$1" = "" ]; then
    echo $0 not invoked with service name or instances folder path
    exit $JWALA_EXIT_CODE_NO_OP;
fi

if $cygwin; then
    sc delete $1
fi

if $linux; then
    if $linux; then
        if [ test -e "/etc/init.d/$1" ]; then
          echo delete /etc/init.d/$1
          /usr/bin/sudo rm /etc/init.d/$1
        fi
    fi
fi