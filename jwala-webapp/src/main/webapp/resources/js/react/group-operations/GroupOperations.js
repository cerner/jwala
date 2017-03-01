
/** @jsx React.DOM */
var GroupOperations = React.createClass({
    pollError: false,
    getInitialState: function() {
        selectedGroup = null;

        // What does the code below do ?
        this.allJvmData = { jvms: [],
                            jvmStates: []};
        return {
            // Rationalize/unify all the groups/jvms/webapps/groupTableData/etc. stuff so it's coherent
            groupFormData: {},
            groupTableData: [],
            groups: [],
            groupStates: [],
            webServers: [] /* Latest web servers retrieved from the backend when a row is opened. */,
            webServerStates: [],
            jvms: [],
            jvmStates: [],
            visibleWebServers: {} /* All the web servers that are visible due to a user opening row(s). */
        };
    },
    render: function() {
        var btnDivClassName = this.props.className + "-btn-div";

        return  <div className={this.props.className}>
                    <div ref="stompMsgDiv"/>
                    <div className="start-stop-groups-btn-container">
                        <RButton label="START ALL GROUPS"
                                 className="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only start-stop-all-groups-button"
                                 onClick={this.startGroups}/>
                        <RButton label="STOP ALL GROUPS"
                                 className="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only start-stop-all-groups-button"
                                 onClick={this.stopGroups}/>
                    </div>
                    <ModalDialogBox ref="stopGroupsModalDlg"
                                    title="Confirmation Dialog Box"
                                    show={false}
                                    okCallback={this.confirmStopGroupsCallback}
                                    content={<div className="text-align-center"><br/><b>Are you sure you want to STOP all the groups ?</b><br/><br/></div>}
                                    okLabel="Yes"
                                    cancelLabel="No"
                                    top="300"
                                    left="800"/>
                    <ModalDialogBox ref="startGroupsModalDlg"
                                    title="Confirmation Dialog Box"
                                    show={false}
                                    okCallback={this.confirmStartGroupsCallback}
                                    content={<div className="text-align-center"><br/><b>Are you sure you want to START all the groups ?</b><br/><br/></div>}
                                    okLabel="Yes"
                                    cancelLabel="No"
                                    top="300"
                                    left="800"/>
                    <table style={{width:"1084px"}}>
                        <tr>
                            <td>
                                <div>
                                    <GroupOperationsDataTable ref="groupOperationsDataTable"
                                                              data={this.state.groupTableData}
                                                              selectItemCallback={this.selectItemCallback}
                                                              groups={this.state.groups}
                                                              groupsById={groupOperationsHelper.keyGroupsById(this.state.groups)}
                                                              webServers={this.state.webServers}
                                                              jvms={this.state.jvms}
                                                              updateWebServerDataCallback={this.updateWebServerDataCallback}
                                                              collapseRowCallback={this.collapseRowCallback}
                                                              commandStatusWidgetMap={this.commandStatusWidgetMap}
                                                              parent={this}/>
                                </div>
                            </td>
                        </tr>
                   </table>
               </div>
    },
    startGroups: function() {
        this.refs.startGroupsModalDlg.show();
    },
    confirmStartGroupsCallback: function() {
        groupControlService.startGroups().caught(function(e){
            console.log(e);
        });
        this.refs.startGroupsModalDlg.close();
    },
    stopGroups: function() {
        this.refs.stopGroupsModalDlg.show();
    },
    confirmStopGroupsCallback: function() {
        groupControlService.stopGroups().caught(function(e){
            console.log(e);
        });
        this.refs.stopGroupsModalDlg.close();
    },
    retrieveData: function() {
        var self = this;
        this.props.service.getGroups().then(this.retrieveGroupDataHandler).then(this.props.service.getStartedAndStoppedWebServersAndJvmsCount)
            .then(this.retrieveChildrenInfoHandler).then(function(){self.forceUpdate()});
    },
    /**
     * Group data retrieval handler.
     */
    retrieveGroupDataHandler: function(response) {
        var theGroups = response.applicationResponseContent;
        var jvms = [];
        var jvmArrayIdxMap = {}; // We need a map to see if the jvm is already in jvms then use it later to update jvm states.

        // Put all the jvms of all the groups in an array.
        // If the jvm is already in the said array, do not put it in anymore.
        theGroups.forEach(function(group){
            var jvmDetails = "";
            for (var i = 0; i < group.jvms.length; i++) {
                if (jvmArrayIdxMap[group.jvms[i].id.id] == undefined) {
                    jvms.push(group.jvms[i]);
                    jvmArrayIdxMap[group.jvms[i].id.id] = i;

                    // Make a string of all the JVM name and host name of a group.
                    // This will be used to make jQuery datatable do filtering by JVM and host name.
                    jvmDetails = jvmDetails + group.jvms[i].jvmName + group.jvms[i].hostName;
                }
            }
            group["jvmDetails"] = escape(jvmDetails);
        });

        this.state["jvms"] = jvms;
        this.state["jvmArrayIdxMap"] = jvmArrayIdxMap;
        this.state["groupTableData"] = theGroups;
        this.state["groups"] = theGroups;
    },
    /**
     * Children info retrieval handler.
     */
    retrieveChildrenInfoHandler: function(response) {
        // Put the count data in the group's current state count properties.
        this.state.groups.forEach(function(group){
            response.applicationResponseContent.forEach(function(info){
                if (group.name === info.groupName) {
                    if (!group.currentState) {
                        group.currentState = {};
                    }
                    group.currentState.jvmCount = info.jvmCount;
                    group.currentState.jvmStartedCount = info.jvmStartedCount;
                    group.currentState.jvmStoppedCount = info.jvmStoppedCount;
                    group.currentState.jvmForciblyStoppedCount = info.jvmForciblyStoppedCount;
                    group.currentState.webServerCount = info.webServerCount;
                    group.currentState.webServerStartedCount = info.webServerStartedCount;
                    group.currentState.webServerStoppedCount = info.webServerStoppedCount;
                }
            });
        });
    },
    getUpdatedJvmData: function(groups) {
        return groupOperationsHelper.processJvmData(this.state.jvms,
                                                    groupOperationsHelper.extractJvmDataFromGroups(groups),
                                                    this.state.jvmStates,
                                                    []);
    },
    msgHandler: function(msg) {
        if (msg.type === "HISTORY") {
            var commandStatusWidget = this.commandStatusWidgetMap[msg.body.group.name];
            if (commandStatusWidget) {
                commandStatusWidget.push({asOf: msg.body.createDate,
                                          userId: msg.body.createBy,
                                          message: msg.body.event,
                                          from: msg.body.serverName},
                                          msg.body.eventType === "SYSTEM_ERROR" ? "error-status-font": "action-status-font");
            }
        } else if (msg.type === "JVM") {
                this.updateJvmStateData(msg);
        } else if (msg.type === "WEB_SERVER") {
            this.updateWebServerStateData(msg);
        } else if (msg.type === "GROUP") {
            this.updateGroupsStateData(msg);
        }
    },

    /**
     * Check if the session is expired.
     */
    isSessionExpired: function(response) {
        if (typeof response.responseText === "string" && response.responseText.indexOf("Login") > -1) {
            return true;
        }
        return false;
    },

    /*** TODO: Remove when methods below are confirmed to be deprecated **/
    statePollingErrorHandler: function(response) {
        if (this.statePoller.isActive) {
            try {
                if (!this.isSessionExpired(response)) {
                    this.setGroupStatesToPollingError();
                    this.setJvmStatesToPollingError();
                    this.setWebServerStatesToPollingError();
                } else {
                    this.statePoller.stop();
                    this.statePoller = null;
                    alert("The session has expired! You will be redirected to the login page.");
                    window.location.href = "login";
                }
            } finally {
                this.pollError = true;
            }
        }
    },
    setGroupStatesToPollingError: function() {
        for (var key in GroupOperations.groupStatusWidgetMap) {
        var groupStatusWidget = GroupOperations.groupStatusWidgetMap[key];
            if (groupStatusWidget !== undefined) {
                groupStatusWidget.setStatus(GroupOperations.POLL_ERR_STATE,  new Date(), response.responseJSON.applicationResponseContent);
            }
        }
    },
    setJvmStatesToPollingError: function() {
        for (var key in GroupOperations.jvmStatusWidgetMap) {
            var jvmStatusWidget = GroupOperations.jvmStatusWidgetMap[key];
            if (jvmStatusWidget !== undefined) {
                jvmStatusWidget.setStatus(GroupOperations.POLL_ERR_STATE,  new Date(), "");
            }
        }
    },
    setWebServerStatesToPollingError: function() {
        for (var key in GroupOperations.webServerStatusWidgetMap) {
            var webServerStatusWidget = GroupOperations.webServerStatusWidgetMap[key];
            if (webServerStatusWidget !== undefined) {
                webServerStatusWidget.setStatus(GroupOperations.POLL_ERR_STATE,  new Date(), "");
            }
        }
    },
    /*** TODO: Remove when methods above are confirmed to be deprecated **/



    updateGroupsStateData: function(newGroupState) {
        var groupsToUpdate = groupOperationsHelper.getGroupStatesById(this.state.groups);

        if (newGroupState) {
            for (var i = 0; i < groupsToUpdate.length; i++) {
                var group = groupsToUpdate[i];
                if (newGroupState.id.id === group.groupId.id) {
                    // For the group it's a bit different, we need to show the number of started servers
                    // over the total number of servers. Since we reused the existing current state
                    // infrastructure, we have to put the said info in the stateString property.
                    var serverCount = newGroupState.webServerCount + newGroupState.jvmCount;
                    var serverStartedCount = newGroupState.webServerStartedCount + newGroupState.jvmStartedCount;
                    var serverStoppedCount = newGroupState.webServerStoppedCount + newGroupState.jvmStoppedCount
                                    + newGroupState.jvmForciblyStoppedCount;
                    newGroupState.stateString = "Started: " + serverStartedCount + "/" + serverCount;

                    var statusColorCode = "partial";
                    if ((serverCount === 0) || (serverStoppedCount === serverCount)) {
                        statusColorCode = "stopped";
                    } else if (serverStartedCount === serverCount)  {
                        statusColorCode = "started";
                    }

                    GroupOperations.groupStatusWidgetMap["grp" + group.groupId.id].setStatus(newGroupState.stateString,
                        newGroupState.asOf, newGroupState.message, statusColorCode);

                    // Update web server and JVM header states
                    // Note: Since the group operations page is a mix of React and spaghetti code, we do the update using jquery.
                    //       This will have to go (replaced by better code) when group operations is refactored.
                    var  wsStartedCount = $("#ws-child-table_group-operations-table_" + group.groupId.id + "_wsStartedCount");
                    if (wsStartedCount.length) {
                        wsStartedCount.text("Started: " + newGroupState.webServerStartedCount + "/" + newGroupState.webServerCount);
                    }

                    var  jvmStartedCount = $("#jvm-child-table_group-operations-table_" + group.groupId.id + "_jvmStartedCount");
                    if (jvmStartedCount.length) {
                        jvmStartedCount.text("Started: " + newGroupState.jvmStartedCount + "/" + newGroupState.jvmCount);
                    }

                    break;
                }
            }
        };
    },
    commandStatusWidgetMap: {} /* Since we can't create a React class object reference on mount, we need to save the references in a map for later access. */,
    updateWebServerStateData: function(newWebServerState) {

        var visibleWebServerArray = this.getVisibleWebServersAsArray(this.state.visibleWebServers);
        console.log(">>> Updating the following web servers:");
        console.log(visibleWebServerArray);

        var self = this;

        if (newWebServerState !== null) {
            visibleWebServerArray.forEach(function(webServer){
                var webServerStatusWidget = GroupOperations.webServerStatusWidgetMap["grp" + webServer.parentItemId + "webServer" + webServer.id.id];
                if (webServerStatusWidget !== undefined) {
                    if (newWebServerState.id.id === webServer.id.id) {
                        if (newWebServerState.stateString === GroupOperations.FAILED || newWebServerState.stateString === GroupOperations.START_SENT || newWebServerState.stateString === GroupOperations.STOP_SENT ||
                            newWebServerState.stateString === GroupOperations.MSG_TYPE_HISTORY) {

                            if (newWebServerState.stateString === GroupOperations.STARTING) {
                                newWebServerState.stateString = GroupOperations.START_SENT;
                            }
                            if (newWebServerState.stateString === GroupOperations.STOPPING) {
                                newWebServerState.stateString = GroupOperations.STOP_SENT;
                            }
                            var commandStatusWidget = self.commandStatusWidgetMap[GroupOperations.getExtDivCompId(webServer.parentItemId)];
                            if (commandStatusWidget !== undefined) {
                                commandStatusWidget.push({stateString: newWebServerState.stateString,
                                                          asOf: newWebServerState.asOf.millis,
                                                          message: newWebServerState.message,
                                                          from: "Web Server " + webServer.name, userId: newWebServerState.userId},
                                                          newWebServerState.stateString === GroupOperations.FAILED ? "error-status-font" : "action-status-font");
                            }


                        } else if (newWebServerState.stateString === GroupOperations.SCP || newWebServerState.stateString === GroupOperations.INSTALL_SERVICE || newWebServerState.stateString === GroupOperations.DELETE_SERVICE){
                              var commandStatusWidget = self.commandStatusWidgetMap[GroupOperations.getExtDivCompId(webServer.id.id)];
                              if (commandStatusWidget !== undefined) {
                                  commandStatusWidget.push({stateString: newWebServerState.stateString,
                                                            asOf: newWebServerState.asOf.millis,
                                                            message: newWebServerState.message,
                                                            from: "Web Server " + webServer.name,
                                                            userId: newWebServerState.userId},
                                                            "action-status-font");
                              }
                        } else {
                            var stateDetails = groupOperationsHelper.extractStateDetails(newWebServerState);
                            webServerStatusWidget.setStatus(stateDetails.state, stateDetails.asOf.millis, stateDetails.msg);
                        }
                    }
                }
            });
        }
    },

    /**
     * Get visible web servers as an array.
     */
    getVisibleWebServersAsArray: function() {
        var webServerArray = [];
        for (var key in this.state.visibleWebServers) {
            this.state.visibleWebServers[key].forEach(function(webServer){
                webServerArray.push(webServer);
            });
        }
        return webServerArray;
    },
    updateJvmStateData: function(newJvmState) {
        var self = this;
        var jvmsToUpdate = groupOperationsHelper.getJvmStatesByGroupIdAndJvmId(this.state.jvms);

        if (newJvmState) {
            jvmsToUpdate.forEach(function(jvm){
                var jvmStatusWidget = GroupOperations.jvmStatusWidgetMap["grp" + jvm.groupId.id + "jvm" + jvm.jvmId.id];
                if (jvmStatusWidget !== undefined) {
                    if (newJvmState.id.id === jvm.jvmId.id) {
                        if (newJvmState.stateString === GroupOperations.FAILED ||
                            newJvmState.stateString === GroupOperations.START_SENT ||
                            newJvmState.stateString === GroupOperations.STOP_SENT ||
                            newJvmState.stateString === GroupOperations.MSG_TYPE_HISTORY) {

                            var commandStatusWidget = self.commandStatusWidgetMap[GroupOperations.getExtDivCompId(jvm.groupId.id)];
                            if (commandStatusWidget !== undefined) {
                                commandStatusWidget.push({stateString: newJvmState.stateString,
                                                          asOf: newJvmState.asOf.millis,
                                                          message: newJvmState.message,
                                                          from: "JVM " + jvm.name,
                                                          userId: newJvmState.userId},
                                                          newJvmState.stateString === GroupOperations.FAILED ?
                                                          "error-status-font" : "action-status-font");
                            }

                        } else if (newJvmState.stateString === GroupOperations.SCP || newJvmState.stateString === GroupOperations.INSTALL_SERVICE || newJvmState.stateString === GroupOperations.DELETE_SERVICE){
                            var commandStatusWidget = self.commandStatusWidgetMap[GroupOperations.getExtDivCompId(jvm.groupId.id)];
                            if (commandStatusWidget !== undefined) {
                                commandStatusWidget.push({stateString: newJvmState.stateString,
                                                          asOf: newJvmState.asOf.millis,
                                                          message: newJvmState.message,
                                                          from: "JVM " + jvm.name,
                                                          userId: newJvmState.userId},
                                                          "action-status-font");
                            }
                        }
                        else {
                            var stateDetails = groupOperationsHelper.extractStateDetails(newJvmState);
                            jvmStatusWidget.setStatus(stateDetails.state, stateDetails.asOf.millis, stateDetails.msg);

                            // Update the state of the jvm that is in a "react state" so that when the
                            // state component is re rendered it is updated. JVMs are loaded together with the
                            // group and not when the group is opened that is why we need this.
                            self.refs.groupOperationsDataTable.state.currentJvmState[jvm.jvmId.id] = {stateLabel: newJvmState.stateString,
                                                                                      errorStatus: ""};
                        }
                    }
                }
            });
        }
    },
    pollStates: function() {
        React.renderComponent(<span>Connecting to a web socket...</span>, this.refs.stompMsgDiv.getDOMNode());
        ServiceFactory.getServerStateWebSocketService().connect(this.msgHandler, this.stompConnectedCallback, this.stompConnectErrorHandler);
    },
    stompConnectedCallback: function(frame) {
        React.unmountComponentAtNode(this.refs.stompMsgDiv.getDOMNode());
    },
    stompConnectErrorHandler: function(e) {
        React.renderComponent(<span>Connecting to a web socket...</span>, this.refs.stompMsgDiv.getDOMNode());

        // try to connect again...
        ServiceFactory.getServerStateWebSocketService().connect(this.msgHandler, this.stompConnectedCallback, this.stompConnectErrorHandler);
    },
    markGroupExpanded: function(groupId, isExpanded) {
        this.setState(groupOperationsHelper.markGroupExpanded(this.state.groups,
                                                              groupId,
                                                              isExpanded));
    },
    markJvmExpanded: function(jvmId, isExpanded) {
        this.setState(groupOperationsHelper.markJvmExpanded(this.state.jvms,
                                                            jvmId,
                                                            isExpanded));
    },
    componentDidMount: function() {
        this.retrieveData();
        this.pollStates();
    },
    componentWillUnmount: function() {
        ServiceFactory.getServerStateWebSocketService().disconnect();
    },
    updateWebServerDataCallback: function(webServerData) {

        if (webServerData && webServerData.length > 0) {
            this.state.visibleWebServers["grp_" + webServerData[0].parentItemId] = webServerData;
        }

        this.setState(groupOperationsHelper.processWebServerData([],
                                                                 webServerData,
                                                                 this.state.webServerStates,
                                                                 []));
        this.updateWebServerStateData(null);
    },
    collapseRowCallback: function(groupId) {
        delete this.state.visibleWebServers["grp_" + groupId];
    },
    statePoller: null,
    statics: {
        // Used in place of ref since ref will not work without a React wrapper (in the form a data table)
        groupStatusWidgetMap: {},
        webServerStatusWidgetMap: {},
        jvmStatusWidgetMap: {},
        FAILED: "FAILED",
        START_SENT: "START SENT",
        STOP_SENT: "STOP SENT",
        SCP: "secureCopy",
        INSTALL_SERVICE: "installService",
        DELETE_SERVICE: "deleteService",
        DRAIN_USER: "drainUser",
        MSG_TYPE_HISTORY: "history",
        getExtDivCompId: function(groupId) {
            return "ext-comp-div-group-operations-table_" + groupId;
        },
        POLL_ERR_STATE: "POLLING ERROR!",
        STATE_POLLER_INTERVAL: 1
    }
});