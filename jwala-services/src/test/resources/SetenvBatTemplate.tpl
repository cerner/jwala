:: -------------------------
:: Set the JWALA_HOME variable
:: -------------------------

CALL:jwalaSetHome
SET JWALA_HOME_UNIX=%JWALA_HOME:\=/%

:: ------------------------------------------------------------------------------
:: Use fn jwalaSet to set any path variables. This allows the 'jwala' folder to be
:: moved anywhere in the file system. EPM properties (marked by @) determine 
:: the location at build time and jwalaSet will adapt the path based on the current
:: location of the 'jwala' folder.  Relocation of the 'jwala' dir is needed primarily
:: for developer environments. 
:: ------------------------------------------------------------------------------

CALL:jwalaSet JAVA_HOME d:\jwala\jdk1.7.0_45
SET JRE_HOME=%JAVA_HOME%\jre

CALL:jwalaSet CATALINA_HOME d:\jwala\cerner\apache-tomcat-7.0.55\core
CALL:jwalaSet CATALINA_BASE d:\jwala\cerner\instances\jvm-1

REM JMX_OPTS port settings deprecated in favor of a lifecycle listener in server.xml
SET JMX_OPTS=-Dcom.sun.management.jmxremote.ssl=false
SET JMX_OPTS=%JMX_OPTS% -Dcom.sun.management.jmxremote.authenticate=false
REM SET JMX_OPTS=%JMX_OPTS% -Dcom.sun.management.jmxremote.password.file=%CATALINA_BASE%/conf/jmxremote.password
REM SET JMX_OPTS=%JMX_OPTS% -Dcom.sun.management.jmxremote.access.file=%CATALINA_BASE%/conf/jmxremote.access

REM SET SSL_OPTS=-Ddeployment.security.SSLv2Hello=false -Ddeployment.security.SSLv3=false 
REM SET SSL_OPTS=%SSL_OPTS% -Ddeployment.security.TLSv1=false -Ddeployment.security.TLSv1.1=false 
REM SET SSL_OPTS=%SSL_OPTS% -Ddeployment.security.TLSv1.2=true
REM SET SSL_OPTS=%SSL_OPTS% -Dhttps.protocols=TLSv1.2
SET SSL_OPTS=-Dhttps.protocols=TLSv1.2

REM Warning - too many system properties will cause the deployment to fail due to environment space.

SET SSL_DEBUG_OPTS=-Djavax.net.debug=ssl

CALL:jwalaSet SPRING_AGENT d:\jwala\cerner\lib\tomcat\ext\spring-instrument-3.2.6.RELEASE.jar
SET SPRING_OPTS=-javaagent:%SPRING_AGENT%

:: -------------------------------------------------------------------------------------------------------------------------------
:: JWALA_HOME and the gsm classLoaderUrl are needed as system properties for replacement in files like server.xml or context.xml
:: -------------------------------------------------------------------------------------------------------------------------------
SET CATALINA_OPTS=-XX:PermSize=512m -XX:MaxPermSize=512m -DJWALA_HOME=%JWALA_HOME% -Dgsm.classloader.url=%JWALA_HOME_UNIX%/cerner/lib/tomcat/ext/gsm

CALL:jwalaSet PROPERTIES_PATH d:\jwala\cerner\properties
SET JWALA_OPTS=-DPROPERTIES_ROOT_PATH=%PROPERTIES_PATH%
SET JWALA_OPTS=%JWALA_OPTS% -DJWALA_PROPERTIES_DIR=%PROPERTIES_PATH%

SET LOG_OPTS=-Dlog4j.debug=true
CALL:jwalaSet LOG_ROOT_DIR d:/jwala/apache-tomcat-7.0.55/logs
SET LOG_OPTS=-Dlog.root.dir=%LOG_ROOT_DIR%
SET LOG_OPTS=%LOG_OPTS% -Dlog4j.configuration=log4j.xml

SET LOGIN_CONFIG=-Djava.security.auth.login.config=%PROPERTIES_PATH%\jaas.config

SET APR_OPTS=-Djava.library.path=%CATALINA_HOME%\bin

SET PROD_OPTS=%APR_OPTS% %JWALA_OPTS% %SSL_OPTS% %JMX_OPTS% %SPRING_OPTS% %CATALINA_OPTS% %LOG_OPTS% %LOGIN_CONFIG%
SET DEBUG_OPTS=%PROD_OPTS%

SET JAVA_SERVICE_OPTS=%PROD_OPTS: =#%
SET JAVA_OPTS=%DEBUG_OPTS%

goto:eof



:: ----------------------------------------------------------------------------
:: Sets the JWALA_DRIVE and JWALA_HOME variables based on the location of this file
:: ----------------------------------------------------------------------------

:jwalaSetHome

set JWALA_DRIVE=%~d0

Setlocal EnableDelayedExpansion

set path=%~p0
set pathList=%path:\=,%
set trimmedPathList=%pathList:~1,-1%
set jwalaDir=%JWALA_DRIVE%
set foundJwala=0
set savedJwalaDir=!jwalaDir!

For %%A in (%trimmedPathList%) do (
   set jwalaDir=!jwalaDir!\%%A
   IF %%A==jwala (
      set savedJwalaDir=!jwalaDir!
      set foundJwala=1
   )
)
:END_FIND_JWALA_DIR

if %foundJwala% EQU 0 (echo jwala dir not found so setting to default & set savedJwalaDir=d:\jwala)
echo jwalaDir is %savedJwalaDir%

ENDLOCAL & SET JWALA_HOME=%savedJwalaDir%

goto:eof


:: ----------------------------------------------------------------------
:: Function to set paths variables based on the current value of JWALA_HOME
:: ----------------------------------------------------------------------

:jwalaSet
:: arg1 = variable to set (eg CATALINA_HOME)
:: arg2 = original value of variable (eg d:\jwala\cerner\apache-tomcat-7.0.55\core)
::
:: if JWALA_HOME=e:\view_stores\jwala, then this fn sets CATALINA_HOME to e:\view_stores\jwala\cerner\apache-tomcat-7.0.55\core
 
SET %~1=%~2
CALL SET %~1=%%%~1:d:\jwala=%JWALA_HOME%%%

goto:eof
