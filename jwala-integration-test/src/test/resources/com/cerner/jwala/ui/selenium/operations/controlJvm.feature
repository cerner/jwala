Feature: Test start, thread dump, heap dump, stop and delete

Scenario: Do a happy path start, thread dump, heap dump, stop and deletion of a JVM

    Given I logged in

    And I am in the Configuration tab

    # create media
    And I created a media with the following parameters:
        | mediaName       | jdk1.8.0_92     |
        | mediaType       | JDK             |
        | archiveFilename | jdk1.8.0_92.zip |
        | remoteDir       | media.remote.dir|
    And I created a media with the following parameters:
        | mediaName       | apache-tomcat-7.0.55     |
        | mediaType       | Apache Tomcat            |
        | archiveFilename | apache-tomcat-7.0.55.zip |
        | remoteDir       | tomcat.media.remote.dir  |

    # create entities
    And I created a group with the name "CONTROL-JVM-TEST-G"
    And I created a jvm with the following parameters:
        | jvmName    | CONTROL-JVM-TEST-J   |
        | tomcat     | apache-tomcat-7.0.55 |
        | jdk        | jdk1.8.0_92          |
        | hostName   | host1                |
        | portNumber | jvm.http.port        |
        | group      | CONTROL-JVM-TEST-G   |

    # create resources
    And I created a JVM resource with the following parameters:
        | group       | CONTROL-JVM-TEST-G                   |
        | jvm         | CONTROL-JVM-TEST-J                     |
        | deployName  | setenv.bat                      |
        | deployPath  | jvm.setenv.resource.deploy.path |
        | templateName| setenv.bat.tpl                  |
    And I created a JVM resource with the following parameters:
        | group       | CONTROL-JVM-TEST-G                       |
        | jvm         | CONTROL-JVM-TEST-J                         |
        | deployName  | server.xml                          |
        | deployPath  | jvm.server.xml.resource.deploy.path |
        | templateName| server.xml.tpl                      |

    And I am in the Operations tab
    And I expand the group operation's "CONTROL-JVM-TEST-G" group

    # generate
    And I generated "CONTROL-JVM-TEST-J" JVM of "CONTROL-JVM-TEST-G" group

    # test start
    When I click start on jvm "CONTROL-JVM-TEST-J" of the group "CONTROL-JVM-TEST-G"
    Then I see the state of "CONTROL-JVM-TEST-J" JVM of group "CONTROL-JVM-TEST-G" is "STARTED"

    # thread dump
    When I click thread dump of jvm "CONTROL-JVM-TEST-J" of the group "CONTROL-JVM-TEST-G"
    Then I see the thread dump page

    # test heap dump
    When I click on heap dump of "CONTROL-JVM-TEST-J" jvm of "CONTROL-JVM-TEST-G" group
    Then I see heap dump popup for jvm "CONTROL-JVM-TEST-J"

    # we need to close the heap dump message box
    Given I click the ok button

    # test stop
    When I click the stop button of "CONTROL-JVM-TEST-J" jvm of "CONTROL-JVM-TEST-G" group
    Then I see the state of "CONTROL-JVM-TEST-J" JVM of group "CONTROL-JVM-TEST-G" is "STOPPED"

    # test delete
    When I click the "Delete JVM" button of JVM "CONTROL-JVM-TEST-J" under group "CONTROL-JVM-TEST-G" in the operations tab
    And I click the operation's confirm delete JVM dialog yes button
    Then I see that "CONTROL-JVM-TEST-J" JVM got deleted successfully from the operations tab