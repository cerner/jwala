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
    sc query $1
    if [ $? -eq 0 ]; then
        echo delete $1
        sc delete $1
    fi
fi

if $linux; then
    if $linux; then
        if [ test -e "/etc/init.d/$1" ]; then
          echo delete /etc/init.d/$1
          /usr/bin/sudo rm /etc/init.d/$1
        fi
    fi
fi