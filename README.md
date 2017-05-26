# _About_

Jwala is a web application that provides management for a group of Tomcat servers. Jwala is capable of creating and persisting definitions of Group instances, and exposes a RESTful interface to do so. The definition of a Group includes Web Apps, JVMs, Web Servers, and Resources.

Once defined, a Group may also be managed through Jwala’s REST API, to perform management operations on the group. The management operations are listed under Component Responsibilities below. The primary user of Jwala will be the enterprise package manager application, which will interact with Jwala using the REST API.

Jwala utilizes the defined file system structure and SSH agents on each machine to manage running Tomcat instances on remote Servers. Jwala utilizes the Cerner Tomcat Platform application deployment model to know how to request current status from each Tomcat instance and HTTPD instance. Jwala is also able to update each instance as changes are made to the configuration, and allows maintenance operations to be executed from a central console.


# _Building_

Jwala can be build by using gradle 2.9. To build the jwala project run the "gradle build" command. This runs the build and test tasks. This task creates the jwala-tomcat.war file in jwala-webapp module, which can be run by dropping it in Apache Tomcat application server. 

1. [Install Git](https://git-scm.com/) 
2. [Install JDK jdk1.8.0_66](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
3. [Install Gradle verion 2.9](https://services.gradle.org/distributions/gradle-2.9-bin.zip)
4. 	Clone jwala as follows
git clone https://github.com/cerner/jwala.git
5. Build jwala bundle using gradle
      `gradle clean zipJwalaTomcat`
6. Set JAVA_HOME and run apache tomcat
       set JAVA_HOME=jdk1.8.0_66
7. Run jwala on tomcat

cd /jwala/jwala-tomcat/build/apache-tomcat-7.0.55/bin

Start Tomcat as follows

`catalina.bat run`

# Run Jwala
Jwala can run by using the war file build in the the above step 

unzip jwala-tomcat-0.0.22.zip to a location

Example
c:/apache-tomcat-7.0.55

Run tomcat

c:/apache-tomcat-7.0.55/bin/catalina.bat start

Jwala will be accessible from 

[Jwala](https://localhost:8001/jwala/) https://localhost:8001/jwala/

You can login using default credentials as, 

username: jwala 

password: jwala 

# _Availability_

Artifacts or running software associated with this project and where to access them is generally added here.

In the open source community provided pre-built artifacts for your project can greatly assist in adoption and building
good will.


# _Communication_

Please use github issues to track and open new issues related to Jwala
See [issues]() [issues]

# Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md)

# LICENSE

Copyright 2016 Cerner Innovation, Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

&nbsp;&nbsp;&nbsp;&nbsp;http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.






