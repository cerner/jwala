#!/bin/bash

JWALA_EXIT_CODE_INVALID_OS=124
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
  os_version=$(uname -r)
  linux_7="el7"
  if [[ $os_version =~ $linux_7 ]];then
    echo systemctl disable  $1
    sudo systemctl disable  $1
    echo delete /etc/systemd/system/$1
    sudo rm /etc/systemd/system/$1
    echo reload systemctl
    sudo systemctl daemon-reload
    echo reset all the units state
    sudo systemctl reset-failed
  else
    echo $os_version found but was expecting $linux_7
    echo Exiting with ERROR CODE $JWALA_EXIT_CODE_INVALID_OS
    exit $JWALA_EXIT_CODE_INVALID_OS;
  fi
fi
