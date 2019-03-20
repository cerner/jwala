[Unit]
Description=Apache Web Server
After=network.target remote-fs.target nss-lookup.target

[Service]
Type=forking

PIDFile=@APACHE_HOME@/logs/httpd-@WSNAME@.pid
EnvironmentFile=@HTTPD_CONF@

ExecStart=@APACHE_HOME@/bin/httpd -f @HTTPD_CONF@
ExecReload=@APACHE_HOME@/bin/httpd -k restart
ExecStop=@APACHE_HOME@/bin/httpd -k stop

PrivateTmp=true
LimitNOFILE=infinity

[Install]
WantedBy=multi-user.target