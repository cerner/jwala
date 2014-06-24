# Load mod_jk module
LoadModule jk_module modules/mod_jk.so

JkWorkersFile conf/workers.properties
JkShmFile     logs/mod_jk.shm
JkLogFile     logs/mod_jk.log
JkLogLevel    info
JkLogStampFormat "[%a %b %d %H:%M:%S %Y] "

<% apps.each() { %>
JkMount ${it.mount} lb-${it.name}
<% } %>

JkMount  /jkmanager/* status

<IfModule !mpm_netware_module>
<IfModule !mpm_winnt_module>