Feature: Upload a Resource

  Scenario: Upload a JVM Resource

    Given I logged in
    And I am in the Configuration tab

      # create media
    And I created a media with the following parameters:
      | mediaName       | jdk.media         |
      | mediaType       | JDK               |
      | archiveFilename | jdk.media.archive |
      | remoteDir       | remoteDir         |
    And I created a media with the following parameters:
      | mediaName       | apache.tomcat.media         |
      | mediaType       | Apache Tomcat               |
      | archiveFilename | apache.tomcat.media.archive |
      | remoteDir       | remoteDir                   |

      # create entities
    And I created a group with the name "seleniumGroup"
    And I created a jvm with the following parameters:
      | jvmName    | seleniumJvm         |
      | tomcat     | apache.tomcat.media |
      | jdk        | jdk.media           |
      | hostName   | host1               |
      | portNumber | 80                  |
      | group      | seleniumGroup       |

    And I am in the resource tab
    And I expand "seleniumGroup" node
    And I expand "JVMs" node
    And I click "seleniumJvm" node

     # do the test
    When I click the add resource button
    And I fill in the "Deploy Name" field with "server.xml"
    And I fill in the "Deploy Path" field with "resource.deploy.path"
    And I choose the resource file "server.xml.tpl"
    And I click the upload resource dialog ok button
    Then I see that the resource got uploaded successfully


  Scenario: Upload a Web Server Resource

    Given I logged in
    And I am in the Configuration tab

      # create media
    And I created a media with the following parameters:
      | mediaName       | apache.httpd.media         |
      | mediaType       | Apache HTTPD               |
      | archiveFilename | apache.httpd.media.archive |
      | remoteDir       | d:/ctp                     |

      # create entities
    And I created a group with the name "seleniumGroup"
    And I created a web server with the following parameters:
      | webserverName      | seleniumWebserver  |
      | hostName           | host1              |
      | portNumber         | 80                 |
      | httpsPort          | 443                |
      | group              | seleniumGroup      |
      | apacheHttpdMediaId | apache.httpd.media |
      | statusPath         | /apache_pb.png     |

    And I am in the resource tab
    And I expand "seleniumGroup" node
    And I expand "Web Servers" node
    And I click "seleniumWebserver" node

      # do the test
    When I click the add resource button
    And I fill in the "Deploy Name" field with "httpd.conf"
    And I fill in the "Deploy Path" field with "httpd.resource.deploy.path"
    And I choose the resource file "httpdconf.tpl"
    And I click the upload resource dialog ok button
    Then I see that the resource got uploaded successfully


  Scenario: Upload a Web Application Resource

    Given I logged in
    And I am in the Configuration tab

      # create entities
    And I created a group with the name "seleniumGroup"
    And I created a web app with the following parameters:
      | webappName  | seleniumWebapp |
      | contextPath | /hello         |
      | group       | seleniumGroup  |

    And I am in the resource tab
    And I expand "seleniumGroup" node
    And I expand "Web Apps" node
    And I click "seleniumWebapp" node

      # do the test
    When I click the add resource button
    And I fill in the "Deploy Name" field with "hello-world.war"
    And I fill in the "Deploy Path" field with "resource.deploy.path"
    And I choose the resource file "hello-world.war"
    And I click the upload resource dialog ok button
    Then I see that the resource got uploaded successfully