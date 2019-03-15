#!/bin/bash

JWALA_EXIT_CODE_NO_OP=127
JWALA_EXIT_CODE_INVALID_OS=124

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
    exit $?
fi

if $linux; then
  get_version=$(uname -r)
  linux_7="el7"
  if [[ $get_version =~ $linux_7 ]];then
	  /usr/bin/sudo systemctl status $1
  else
    /usr/bin/echo Linux 6 found
    exit $JWALA_EXIT_CODE_INVALID_OS;
  fi
    exit $?
fi
  