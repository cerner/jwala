@ECHO OFF

${vars['remote.paths.apache.httpd']}\bin\httpd -k install -n ${webServer.name} -f ${webServer.httpConfigFile.path}
CMD /C SC config ${webServer.name} DisplayName= "Apache ${webServer.name}"
POPD
