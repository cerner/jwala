#!/bin/sh
# Parameters
# $1 - Cygwin home directory
# $2 - CTP home directory 
echo "backing up Jwala db..."

export JWALA_VERSION=`$1/bin/cat $2/jwala.version.txt|$1/bin/grep jwala.new.running.version|$1/bin/awk -F'=' '{print $2}'`
export TOMCAT_VERSION=`$1/bin/ls $2/jwala-$JWALA_VERSION|$1/bin/grep -oP "([0-9]{1,}\.)+[0-9]{1,}"`
export DB_HOME=$2/jwala-$JWALA_VERSION/apache-tomcat-$TOMCAT_VERSION/data/db

if [ ! -f $DB_HOME/jwala.h2.db.bak ]; then
 $1/bin/cp $DB_HOME/jwala.h2.db $DB_HOME/jwala.h2.db.bak
 if [ $? -ne 0 ]; then
    echo "Failed to backup $DB_HOME/jwala.h2.db!"
    exit 1
 fi
 echo "Backup successful!"
 exit 0
fi
echo "Jwala db backup file already exists, backup skipped"