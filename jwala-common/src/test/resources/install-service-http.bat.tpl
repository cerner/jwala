PUSHD  d:\jwala\apache-httpd-2.4.10
d:\jwala\apache-httpd-2.4.10\bin\httpd -k install -n ${webServer.name} -f d:\jwala\app\data\httpd\httpd.conf
CMD /C SC config ${webServer.name} DisplayName= "Apache ${webServer.name}"
POPD