<service>
    <id>${springBootApp.name}</id>
    <name>${springBootApp.name}</name>
    <description>This runs Spring Boot as a Service.</description>
    <env name="MYAPP_HOME" value="%BASE%"/>
    <executable>${springBootApp.jdkMedia.remoteDir}\\${springBootApp.jdkMedia.rootDir}\bin\java</executable>
    <arguments>-Xmx256m -jar "%BASE%\target\${springBootApp.name}.jar"</arguments>
    <logmode>rotate</logmode>
</service>

