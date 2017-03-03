#!/bin/sh

### BEGIN INIT INFO
# Provides:          tomcat @JVM_NAME@
# Required-Start:    $remote_fs $syslog
# Required-Stop:     $remote_fs $syslog
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: Start and stop Apache Tomcat
# Description:       Enable Apache Tomcat service provided by daemon.
# chkconfig: 2345 99 10
### END INIT INFO

# Source LSB function library.
if [ -r /lib/lsb/init-functions ]; then
    . /lib/lsb/init-functions
else
    . /etc/init.d/functions
fi

TOMCAT_HOME=@TOMCAT_HOME@
TOMCAT_HOME_BIN=$TOMCAT_HOME/bin

# Define the tomcat username
TOMCAT_USER="${TOMCAT_USER:-tomcat}"

# Define the tomcat group
TOMCAT_GROUP="${TOMCAT_GROUP:-`id -gn $TOMCAT_USER`}"

CATALINA_PID=$TOMCAT_HOME/logs/catalina.pid
export CATALINA_PID

RETVAL="0"

# For SELinux we need to use 'runuser' not 'su'
if [ -x "/sbin/runuser" ]; then
    SU="/sbin/runuser -s /bin/sh"
else
    SU="/bin/su -s /bin/sh"
fi

function checkpidfile()
{
   if [ -f "${CATALINA_PID}" ]; then
      read kpid < ${CATALINA_PID}
      if [ -d "/proc/${kpid}" ]; then
          # The pid file exists and the process is running
          RETVAL="0"
         return
      else
        # The pid file exists but the process is not running
         RETVAL="1"
         return
      fi
   else
      # pid file does not exist and program is not running
      RETVAL="3"
      return
  fi
}

function status()
{
   checkpidfile
   if [ "$RETVAL" -eq "0" ]; then
      echo "${NAME} (pid ${kpid}) is running..."
      success
   elif [ "$RETVAL" -eq "1" ]; then
      echo "PID file exists, but process is not running"
      failure
   else
      pid="$(/usr/bin/pgrep -d , -u ${TOMCAT_USER} -G ${TOMCAT_GROUP} java)"
      if [ -z "$pid" ]; then
          echo "${NAME} is stopped"
          success
          RETVAL="3"
      else
          echo "${NAME} (pid ${kpid}) is running..."
          success
          RETVAL="0"
      fi
  fi
}

start() {
    echo -n "Starting Tomcat @JVM_NAME@"
    # chmod -R 755 ${TOMCAT_USER}:${TOMCAT_GROUP} $TOMCAT_HOME/bin
    chown -R ${TOMCAT_USER}:${TOMCAT_GROUP} $TOMCAT_HOME/work
    chown -R ${TOMCAT_USER}:${TOMCAT_GROUP} $TOMCAT_HOME/logs
    $SU - $TOMCAT_USER -c "pushd $TOMCAT_HOME_BIN; ./startup.sh; popd"
    echo "."
}

stop() {
    echo -n "Stopping Tomcat @JVM_NAME@"
    $SU - $TOMCAT_USER -c "pushd $TOMCAT_HOME_BIN; ./catalina.sh stop 20 -force; popd"
    echo "."
}

function version(){
    $SU - $TOMCAT_USER -c "pushd $TOMCAT_HOME_BIN; ./catalina.sh version; popd"
}

case "$1" in
    start)
        start
        ;;
    stop)
        stop
        ;;
    status)
        status
        ;;
    version)
        version
        ;;
    restart)
        stop
        sleep 15
        start
        ;;
    *)
        echo "Usage: tomcat {start|stop|restart|status|version}"
        exit 1
esac
exit $RETVAL
