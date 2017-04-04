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

if [ "$1" = "" -o "$2" = "" -o "$3" = "" ]; then
    echo $0 usage: unjar-jvm \<jvm-jar-file\> \<jvm-dir\> \<java-jar-path\>
    exit $JWALA_EXIT_CODE_NO_OP;
fi

if $cygwin; then

    export JAR_FILE=`/usr/bin/basename $1`
    export BACKUP_DATE=`/usr/bin/date +%Y%m%d_%H%M%S`
    export JVM_INSTANCE=`/usr/bin/basename $2`
    cd $2/..

    # back up current jvm directory
    /usr/bin/echo "Renaming the current JVM to $2.$BACKUP_DATE"
    if [ -d "$2" ]; then
        /usr/bin/mv $2 $2.$BACKUP_DATE
    fi
    /usr/bin/mkdir $2

    # extract the new configuration files
    /usr/bin/echo "Extracting $3"
    if [ ! -e "$3.exe" ]; then
      /usr/bin/echo JVM version not installed: $3 does not exist on this host
      exit $JWALA_EXIT_CODE_FAILED
    fi
    $3 xf `cygpath -wa $1`
    /usr/bin/rm $1
    #delete META-INF
    if [ -e "$2/../META-INF" ]; then
      rm -r "$2/../META-INF"
    fi
    /usr/bin/echo Deploy of $1 was successful
    exit $JWALA_EXIT_CODE_SUCCESS
fi


if $linux; then
    export JAR_FILE=`basename $1`
    export BACKUP_DATE=`date +%Y%m%d_%H%M%S`
    export JVM_INSTANCE=`basename $2`

    # back up current jvm directory
    if [ -d "$2" ]; then
        echo "Renaming the current JVM to $2.$BACKUP_DATE"
        mv $2 $2.$BACKUP_DATE
    fi
    if [ ! -e "$2" ]; then mkdir -p $2; fi;
    echo cd $2/..
    cd $2/..
    # extract the new configuration files
    echo "Extracting $3"
    if [ ! -e "$3" ]; then
      echo JVM version not installed: $3 does not exist on this host
      exit $JWALA_EXIT_CODE_FAILED
    fi
	echo executing $3 xf $1
	$3 xf $1
	echo deleting $1
	rm $1
    #delete META-INF
    if [ -e "$2/../META-INF" ]; then
	    rm -r "$2/../META-INF"
    fi
    echo Deploy of $1 was successful
    exit $JWALA_EXIT_CODE_SUCCESS

fi
