[Unit]
Description= jwala service file
After=network.target

[Service]
Type=forking

# Define the tomcat username
User=tomcat

#Environment="JAVA_HOME=/usr/lib/jvm/jre"
#Environment="JAVA_OPTS=-Djava.security.egd=file:///dev/urandom"

Environment="CATALINA_BASE=@TOMCAT_HOME@"
Environment="CATALINA_HOME=@TOMCAT_HOME@"
Environment="CATALINA_PID=@TOMCAT_HOME@/logs/catalina.pid"
Environment="CATALINA_HOME_BIN=@TOMCAT_HOME@/bin"

ExecStart=/opt/ctp/jwala-0.0.245/apache-tomcat-7.0.55/bin/startup.sh
ExecStop=/opt/ctp/jwala-0.0.245/apache-tomcat-7.0.55/bin/catalina.sh 

[Install]
WantedBy=multi-user.target
