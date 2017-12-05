Feature: Test start, status, view httpd.conf, delete and stop

Scenario: Do a start, status, view httpd.conf, stop and deletion of a web server

    Given I logged in

    And I am in the Configuration tab

    # create media
    And I created a media with the following parameters:
        | mediaName       | apache.httpd.media     |
        | mediaType       | Apache HTTPD            |
        | archiveFilename | apache.httpd.media.archive |
        | remoteDir       | media.remote.dir        |

    # create entities
    And I created a group with the name "CONTROL-WEBSERVER-TEST-G"
    And I created a web server with the following parameters:
        | webserverName      | CONTROL-WEBSERVER-TEST-W   |
        | hostName           | host1                      |
        | portNumber         | ws.http.port               |
        | httpsPort          | ws.https.port              |
        | group              | CONTROL-WEBSERVER-TEST-G   |
        | apacheHttpdMediaId | apache.httpd.media        |
        | statusPath         | ws.status.path             |

    # create resources
    And I created a web server resource with the following parameters:
        | group       | CONTROL-WEBSERVER-TEST-G   |
        | webServer   | CONTROL-WEBSERVER-TEST-W   |
        | deployName  | httpd.conf                 |
        | deployPath  | httpd.resource.deploy.path |
        | templateName| httpdconf.tpl              |

    And I am in the Operations tab
    And I expand the group operation's "CONTROL-WEBSERVER-TEST-G" group

    # generate
    And I generated the web servers of "CONTROL-WEBSERVER-TEST-G" group

   #individual generate
    When I generate "CONTROL-WEBSERVER-TEST-W" web server of "CONTROL-WEBSERVER-TEST-G" group
    Then I see the web server was successfully generated

    # test start
    When I click the start button of "CONTROL-WEBSERVER-TEST-W" webserver of "CONTROL-WEBSERVER-TEST-G" group
    Then I see the state of "CONTROL-WEBSERVER-TEST-W" web server of group "CONTROL-WEBSERVER-TEST-G" is "STARTED"
    And I don't see the click status tooltip

    # negative test, try to delete a started web server
    When I click the "Delete Web Server" button of web server "CONTROL-WEBSERVER-TEST-W" under group "CONTROL-WEBSERVER-TEST-G" in the operations tab
    And I click the operation's confirm delete "CONTROL-WEBSERVER-TEST-W" web server dialog yes button
    Then I don't see the click status tooltip
    And I see an error dialog box that tells me to stop the web server "CONTROL-WEBSERVER-TEST-W"

    # we need to close the expected delete web server error message box
    Given I click the ok button

    # test status
    # disabled to make this feature run successfully in Chrome
    # Chrome replaces http to https but not the non-default (port 80) http port number
    # When I click the "status" link of web server "CONTROL-WEBSERVER-TEST-W" under group "CONTROL-WEBSERVER-TEST-G" in the operations tab
    # Then I see the load balancer page

    # test view httpd.conf
    When I click the "httpd.conf" link of web server "CONTROL-WEBSERVER-TEST-W" under group "CONTROL-WEBSERVER-TEST-G" in the operations tab
    Then I see the httpd.conf

    When I click the drain button of "CONTROL-WEBSERVER-TEST-W" webserver under "CONTROL-WEBSERVER-TEST-G" group
    Then I see the drain message for webserver "CONTROL-WEBSERVER-TEST-W" and host "host1"
    And I do not see an error message after clicking drain

    # test stop
    When I click the stop button of "CONTROL-WEBSERVER-TEST-W" webserver of "CONTROL-WEBSERVER-TEST-G" group
    Then I see the state of "CONTROL-WEBSERVER-TEST-W" web server of group "CONTROL-WEBSERVER-TEST-G" is "STOPPED"

    # test delete
    When I click the "Delete Web Server" button of web server "CONTROL-WEBSERVER-TEST-W" under group "CONTROL-WEBSERVER-TEST-G" in the operations tab
    And I click the operation's confirm delete "CONTROL-WEBSERVER-TEST-W" web server dialog yes button
    Then I see that "CONTROL-WEBSERVER-TEST-W" web server got deleted successfully from the operations tab

