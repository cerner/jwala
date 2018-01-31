Feature: Test start, thread dump, heap dump, stop and delete

  Scenario: Do a happy path start, thread dump, heap dump, stop and deletion of a JVM

    Given I logged in

    And I am in the Configuration tab

      # create media
    And I created a media with the following parameters:
      | mediaName       | jdk.media         |
      | mediaType       | JDK               |
      | archiveFilename | jdk.media.archive |
      | remoteDir       | media.remote.dir  |
    And I created a media with the following parameters:
      | mediaName       | apache.tomcat.media         |
      | mediaType       | Apache Tomcat               |
      | archiveFilename | apache.tomcat.media.archive |
      | remoteDir       | tomcat.media.remote.dir     |

      # create entities
    And I created a group with the name "CONTROL-JVM-TEST-G"
    And I created a jvm with the following parameters:
      | jvmName    | CONTROL-JVM-TEST-J  |
      | tomcat     | apache.tomcat.media |
      | jdk        | jdk.media           |
      | hostName   | host1               |
      | portNumber | jvm.http.port       |
      | group      | CONTROL-JVM-TEST-G  |

      # create resources
    And I created a JVM resource with the following parameters:
      | group        | CONTROL-JVM-TEST-G              |
      | jvm          | CONTROL-JVM-TEST-J              |
      | deployName   | setenvFileName                  |
      | deployPath   | jvm.setenv.resource.deploy.path |
      | templateName | setenvTemplateFile              |
    And I created a JVM resource with the following parameters:
      | group        | CONTROL-JVM-TEST-G                  |
      | jvm          | CONTROL-JVM-TEST-J                  |
      | deployName   | server.xml                          |
      | deployPath   | jvm.server.xml.resource.deploy.path |
      | templateName | server.xml.tpl                      |

    And I am in the Operations tab
    And I expand the group operation's "CONTROL-JVM-TEST-G" group

      # individual generate
    And I generated "CONTROL-JVM-TEST-J" JVM of "CONTROL-JVM-TEST-G" group

      # test start
    When I click start on jvm "CONTROL-JVM-TEST-J" of the group "CONTROL-JVM-TEST-G"
    Then I see the state of "CONTROL-JVM-TEST-J" JVM of group "CONTROL-JVM-TEST-G" is "STARTED"

    When I click the drain button of "CONTROL-JVM-TEST-J" JVM under "CONTROL-JVM-TEST-G" group
    And I do not see an error message after clicking drain

      # thread dump
    When I click thread dump of jvm "CONTROL-JVM-TEST-J" of the group "CONTROL-JVM-TEST-G"
    Then I don't see the click status tooltip
    And I see the thread dump popup for the jvm "CONTROL-JVM-TEST-J" with the message "thread-dump-popup-message"
    And I click the ok button

      # test heap dump
    When I click on heap dump of "CONTROL-JVM-TEST-J" jvm of "CONTROL-JVM-TEST-G" group
    Then I don't see the click status tooltip
    And I see heap dump popup for jvm "CONTROL-JVM-TEST-J"

      # we need to close the heap dump message box
    Given I click the ok button

      # test stop
    When I click the stop button of "CONTROL-JVM-TEST-J" jvm of "CONTROL-JVM-TEST-G" group
    Then I see the state of "CONTROL-JVM-TEST-J" JVM of group "CONTROL-JVM-TEST-G" is "STOPPED"

      # test delete
    When I click the "Delete JVM" button of JVM "CONTROL-JVM-TEST-J" under group "CONTROL-JVM-TEST-G" in the operations tab
    And I click the operation's confirm delete JVM dialog yes button
    Then I see that "CONTROL-JVM-TEST-J" JVM got deleted successfully from the operations tab