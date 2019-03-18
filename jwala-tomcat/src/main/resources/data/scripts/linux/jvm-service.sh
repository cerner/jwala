#!/bin/sh

[Unit]
Description= jwala service file
After=network.target

[Service]
Type=forking
PIDFile="@TOMCAT_HOME@/logs/catalina.pid"

# Define the tomcat username
User=tomcat
Group=tomcat

Environment="CATALINA_BASE=@TOMCAT_HOME@"
Environment="CATALINA_HOME=@TOMCAT_HOME@"
Environment="CATALINA_PID=@TOMCAT_HOME@/logs/catalina.pid"
Environment="CATALINA_HOME_BIN=@TOMCAT_HOME@/bin"

ExecStart="@TOMCAT_HOME@/bin/startup.sh"
ExecStop=@TOMCAT_HOME@/bin/catalina.sh stop

[Install]
WantedBy=multi-user.target
