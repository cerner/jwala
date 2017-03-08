#!/bin/bash
#
# httpd        Startup script for the Apache HTTP Server
#
# chkconfig: 2345 99 15
# description: The Apache HTTP Server is an efficient and extensible  \
#          server implementing the current HTTP standards.
# processname: httpd
#
### BEGIN INIT INFO
# Provides: httpd
# Required-Start: $local_fs $remote_fs $network $named
# Required-Stop: $local_fs $remote_fs $network
# Should-Start: distcache
# Short-Description: start and stop Apache HTTP Server
# Description: The Apache HTTP Server is an extensible server
#  implementing the current HTTP standards.
### END INIT INFO

# Source function library.
. /etc/rc.d/init.d/functions

HTTPD_LANG=${HTTPD_LANG-"C"}

INITLOGS_ARGS=""

apache_home=@APACHE_HOME@
httpd_conf=@HTTPD_CONF@
httpd=${HTTPD-$apache_home/bin/httpd}
prog=httpd
pidfile=${PIDFILE-$apache_home/logs/httpd.pid}
lockfile=${LOCKFILE-$apache_home/logs/httpd.lck}
RETVAL=0
STOP_TIMEOUT=${STOP_TIMEOUT-10}

# The semantics of these two functions differ from the way apachectl does
# things -- attempting to start while running is a failure, and shutdown
# when not running is also a failure.  So we just do it the way init scripts
# are expected to behave here.
start() {
        echo -n $"Starting $prog: "
        LANG=$HTTPD_LANG daemon --pidfile=${pidfile} $httpd -f $httpd_conf $OPTIONS
        RETVAL=$?
        echo
        [ $RETVAL = 0 ] && touch ${lockfile}
        return $RETVAL
}

# When stopping httpd, a delay (of default 10 second) is required
# before SIGKILLing the httpd parent; this gives enough time for the
# httpd parent to SIGKILL any errant children.
stop() {
 status -p ${pidfile} $httpd > /dev/null
 if [[ $? = 0 ]]; then
    echo -n $"Stopping $prog: "
    killproc -p ${pidfile} -d ${STOP_TIMEOUT} $httpd
 else
    echo -n $"Stopping $prog: "
    success
 fi
 RETVAL=$?
 echo
 [ $RETVAL = 0 ] && rm -f ${lockfile} ${pidfile}
}

# See how we were called.
case "$1" in
  start)
 start
 ;;
  stop)
 stop
 ;;
  status)
        status -p ${pidfile} $httpd
 RETVAL=$?
 ;;
  restart)
 stop
 start
 ;;
  *)
 echo $"Usage: $prog {start|stop|status|restart}"
 RETVAL=2
esac

exit $RETVAL
