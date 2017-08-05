#!/bin/sh
# Parameters
# $1 - Cygwin home
# $2 - CTP home
# $3 - Jwala service name (without the version)
# $4 - stop-service.sh location (this is needed to prevent intermittent file not found error)

export JWALA_VERSION=`$1/bin/cat $2/jwala.version.txt|$1/bin/grep -oP "([0-9]{1,}\.)+[0-9]{1,}"`
export TOMCAT_VERSION=`$1/bin/ls $2/jwala-$JWALA_VERSION|$1/bin/grep -oP "([0-9]{1,}\.)+[0-9]{1,}"`
export DB_HOME=$2/jwala-$JWALA_VERSION/apache-tomcat-$TOMCAT_VERSION/data/db
export JWALA_SERVICE_NAME=$3-$JWALA_VERSION

echo "restoring up Jwala db..."
echo "shutting down $JWALA_SERVICE_NAME..."
export STOP_STS=`$4/stop-service.sh $JWALA_SERVICE_NAME 180`
echo "$STOP_STS"
if [ "$STOP_STS" != "Successfully stopped service $JWALA_SERVICE_NAME" ]; then
    echo "Failed to stop $JWALA_SERVICE_NAME!"
    exit 1
fi
$1/bin/cp -f $DB_HOME/jwala.h2.db.bak $DB_HOME/jwala.h2.db
if [ $? -ne 0 ]; then
    echo "Failed to restore backup!"
    exit 1
fi
echo "starting $JWALA_SERVICE_NAME..."
net start $JWALA_SERVICE_NAME
if [ $? -ne 0 ]; then
    echo "Failed to start $JWALA_SERVICE_NAME!"
    exit 1
fi
echo "restore successful!"
exit 0