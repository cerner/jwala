@ECHO OFF

${webServer.apacheHttpdMedia.remoteDir}\\${webServer.apacheHttpdMedia.rootDir}\bin\httpd -k install -n ${webServer.name} -f %1
CMD /C SC config ${webServer.name} DisplayName= "Apache ${webServer.name}"
POPD
