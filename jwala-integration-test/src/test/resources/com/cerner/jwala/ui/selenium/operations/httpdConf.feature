Feature: Httpd Conf

    Scenario: View http.conf a new web-server

    Given I logged in

    And I am in the Configuration tab

    # create media
    And I created a media with the following parameters:
        | mediaName       | jdk1.8.0_92     |
        | mediaType       | JDK             |
        | archiveFilename | jdk1.8.0_92.zip |
        | remoteDir       | media.remote.dir|
    And I created a media with the following parameters:
        | mediaName       | apache-httpd-2.4.20     |
        | mediaType       | Apache HTTPD            |
        | archiveFilename | apache-httpd-2.4.20.zip |
        | remoteDir       | media.remote.dir        |

    # create entities
    And I created a group with the name "seleniumGroup"
    And I created a web server with the following parameters:
        | webserverName      | seleniumWebserver   |
        | hostName           | host1               |
        | portNumber         | 80                  |
        | httpsPort          | 443                 |
        | group              | seleniumGroup       |
        | apacheHttpdMediaId | apache-httpd-2.4.20 |
        | statusPath         | /apache_pb.png      |

    # create resources
    And I created a web server resource with the following parameters:
        | group       | seleniumGroup              |
        | webServer   | seleniumWebserver          |
        | deployName  | httpd.conf                 |
        | deployPath  | httpd.resource.deploy.path |
        | templateName| httpdconf.tpl              |

    And I am in the Operations tab
    And I expand the group operation's "seleniumGroup" group

    # generate
    And I generated the web servers of "seleniumGroup" group

    # do the test
    When I click the "httpd.conf" link of web server "seleniumWebserver" under group "seleniumGroup" in the operations tab
    Then I see the httpd.conf