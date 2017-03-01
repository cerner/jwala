REM ${vars.'test.jwala.property'}

CALL d:\jwala\app\instances\\${jvm.jvmName}\bin\setenv.bat
CMD /C d:\jwala\apache-tomcat-7.0.55\core\bin\install-service.bat install ${jvm.jvmName}
CMD /C d:\jwala\apache-tomcat-7.0.55\core\bin\tomcat7 //US//${jvm.jvmName} ++JvmOptions %JAVA_SERVICE_OPTS%
SC CONFIG ${jvm.jvmName} start= auto