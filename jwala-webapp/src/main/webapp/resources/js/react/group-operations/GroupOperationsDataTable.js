var GroupOperationsDataTable = React.createClass({
    getInitialState: function () {
        return { currentJvmState: {} };
    },
    shouldComponentUpdate: function (nextProps, nextState) {

        // TODO: Set status here
        this.groupsById = groupOperationsHelper.keyGroupsById(nextProps.groups);

        this.hasNoData = this.props.data.length === 0;

        return this.hasNoData;
    },
    render: function () {
        var groupTableDef = [{ sTitle: "", mData: "jvms", jwalaType: "control", colWidth: "14px" },
          { sTitle: "Group ID", mData: "id.id", bVisible: false },
          { sTitle: "Group Name", mData: "name", colWidth: "651px" },
         [{ id: "startGroup",
            sTitle: "Start Group",
            mData: null,
            jwalaType: "button",
            btnLabel: "Start Group",
            btnCallback: this.startGroup,
            className: "inline-block",
            buttonClassName: "ui-button-height",
            extraDataToPassOnCallback: "name",
            onClickMessage: "Starting..."
        }, { jwalaType: "space" }, { id: "stopGroup",
            sTitle: "Stop Group",
            mData: null,
            jwalaType: "button",
            btnLabel: "Stop Group",
            btnCallback: this.stopGroup,
            className: "inline-block",
            buttonClassName: "ui-button-height",
            extraDataToPassOnCallback: "name",
            onClickMessage: "Stopping..."
        }], { sTitle: "State",
            mData: null,
            jwalaType: "custom",
            jwalaRenderCfgFn: this.renderGroupStateRowData.bind(this, "grp"),
            colWidth: "130px" }, { mData: "jvmDetails", bVisible: false }];

        var webServerOfGrpChildTableDef = [{ sTitle: "Web Server ID", mData: "id.id", bVisible: false },
          { mData: null, colWidth: "5px" },
          { sTitle: "Name", mData: "name", colWidth: "335px", maxDisplayTextLen: 45 },
          { sTitle: "Host", mData: "host", colWidth: "140px", maxDisplayTextLen: 20 },
          { sTitle: "HTTP", mData: "port", colWidth: "41px" },
          { sTitle: "HTTPS", mData: "httpsPort", colWidth: "48px" },
          { sTitle: "Group",
            mData: "groups",
            jwalaType: "array",
            displayProperty: "name",
            maxDisplayTextLen: 17,
            colWidth: "125px" }, { mData: null,
            jwalaType: "custom",
            jwalaRenderCfgFn: this.renderWebServerControlPanelWidget.bind(this, "grp", "webServer") }, { sTitle: "State",
            mData: "stateLabel",
            sSortDataType: "ServerStateWidget",
            jwalaType: "custom",
            jwalaRenderCfgFn: this.renderWebServerStateRowData.bind(this, "grp", "webServer"),
            colWidth: "115px" }];

        var webServerOfGrpChildTableDetails = { tableIdPrefix: "ws-child-table_",
            className: "simple-data-table",
            dataCallback: this.getWebServersOfGrp,
            title: "Web Servers",
            isCollapsible: true,
            headerComponents: [{ id: "drainWebServers",
                sTitle: "Draining all web servers",
                mData: null,
                jwalaType: "button",
                btnLabel: "Drain Web Servers",
                btnCallback: this.drainGroupWebServers,
                className: "inline-block",
                buttonClassName: jwalaVars["resourcesEnabled"] === "false" ? "display-none" : "ui-button-height",
                onClickMessage: "Draining web server configurations ..." },
                { id: "space1", jwalaType: "space" },
                { id: "generateWebServers",
                sTitle: "Generate the httpd.conf and deploy all web servers",
                mData: null,
                jwalaType: "button",
                btnLabel: "Generate Web Servers",
                btnCallback: this.generateGroupWebServers,
                className: "inline-block",
                buttonClassName: (jwalaVars["resourcesEnabled"] === "false" ? "display-none" : "ui-button-height"),
                onClickMessage: "Deploying web server configurations ...",
                disabled: !MainArea.isAdminRole
                },
                { id: "space1", jwalaType: "space" },
                { id: "startWebServers",
                sTitle: "Start Web Servers",
                mData: null,
                jwalaType: "button",
                btnLabel: "Start Web Servers",
                btnCallback: this.startGroupWebServers,
                className: "inline-block",
                buttonClassName: "ui-button-height",
                onClickMessage: "Starting..." },
                { id: "space1", jwalaType: "space" },
                { id: "stopWebServers",
                sTitle: "Stop Web Servers",
                mData: null,
                jwalaType: "button",
                btnLabel: "Stop Web Servers",
                btnCallback: this.stopGroupWebServers,
                className: "inline-block",
                buttonClassName: "ui-button-height margin-right-5px",
                onClickMessage: "Stopping..." },
                { id: "wsStartedCount",
                jwalaType: "label",
                className: "inline-block header-component-label started-count-text",
                text: "" }
                ],
            initialSortColumn: [[2, "asc"]],
            isColResizable: true,
            selectItemCallback: this.onSelectWebServerTableRow };

        webServerOfGrpChildTableDetails["tableDef"] = webServerOfGrpChildTableDef;

        var webAppOfGrpChildTableDetails = { tableIdPrefix: "web-app-child-table_",
            className: "simple-data-table",
            dataCallback: this.getApplicationsOfGrp,
            title: "Applications",
            isCollapsible: true,
            initialSortColumn: [[1, "asc"]],
            isColResizable: true };

        var webAppOfGrpChildTableDef = [{ sTitle: "Web App ID", mData: "id.id", bVisible: false },
        { mData: null, colWidth: "10px" },
        { sTitle: "Name", mData: "name" },
        { sTitle: "War Name", mData: "warName" },
        { sTitle: "Context", mData: "webAppContext", colWidth: "150px"},
        { mData: null,
          jwalaType: "custom",
          jwalaRenderCfgFn: this.renderWebAppControlPanelWidget.bind(this, "grp", "webApp") }];

        webAppOfGrpChildTableDetails["tableDef"] = webAppOfGrpChildTableDef;

        var webAppOfJvmChildTableDetails = { tableIdPrefix: "web-app-child-table_jvm-child-table_", /* TODO: We may need to append the group and jvm id once this table is enabled in the next release. */
            className: "simple-data-table",
            dataCallback: this.getApplicationsOfJvm,
            defaultSorting: { col: 5, sort: "asc" },
            initialSortColumn: [[1, "asc"]] };

        var webAppOfJvmChildTableDef = [{ sTitle: "Web App ID", mData: "id.id", bVisible: false },
        { sTitle: "Web App in JVM", mData: "name" }, { sTitle: "War Path", mData: "warPath" },
        { sTitle: "Context", mData: "webAppContext" },
        { sTitle: "Group", mData: "group.name" },
        { sTitle: "Class Name", mData: "className", bVisible: false }];

        webAppOfJvmChildTableDetails["tableDef"] = webAppOfJvmChildTableDef;

        var jvmChildTableDetails = { tableIdPrefix: "jvm-child-table_",
            className: "simple-data-table",
            title: "JVMs",
            isCollapsible: true,
            headerComponents: [{ id: "generateJvms",
                sTitle: "Generate the JVM resources and deploy all the JVMs",
                mData: null,
                jwalaType: "button",
                btnLabel: "Generate JVMs",
                className: "inline-block",//TODO: use disabled
                onClickMessage: "Deploying JVM configurations ...",
                btnCallback: this.generateGroupJvms,
                buttonClassName: jwalaVars["resourcesEnabled"] === "false" ? "display-none" : "ui-button-height",
                disabled: !MainArea.isAdminRole
                },
                { id: "space1", jwalaType: "space" },
                { id: "startJvms",
                sTitle: "Start JVMs",
                mData: null,
                jwalaType: "button",
                btnLabel: "Start JVMs",
                btnCallback: this.startGroupJvms,
                className: "inline-block",
                buttonClassName: "ui-button-height",
                onClickMessage: "Starting..."
            }, { id: "space1", jwalaType: "space" },
              { id: "stopJvms",
                sTitle: "Stop JVMs",
                mData: null,
                jwalaType: "button",
                btnLabel: "Stop JVMs",
                btnCallback: this.stopGroupJvms,
                className: "inline-block",
                buttonClassName: "ui-button-height margin-right-5px",
                onClickMessage: "Stopping..."
            }, { id: "jvmStartedCount",
                jwalaType: "label",
                className: "inline-block header-component-label started-count-text",
                text: "" }],
            initialSortColumn: [[2, "asc"]],
            isColResizable: true,
            selectItemCallback: this.onSelectJvmTableRow };

        var jvmChildTableDef = [{ mData: null, colWidth: "5px" },
        { sTitle: "JVM ID", mData: "id.id", bVisible: false },
        { sTitle: "Name", mData: "jvmName", colWidth: "335px", maxDisplayTextLen: 48 },
        { sTitle: "Host", mData: "hostName", colWidth: "140", maxDisplayTextLen: 17 },
        { sTitle: "HTTP", mData: "httpPort", colWidth: "41px" },
        { sTitle: "HTTPS", mData: "httpsPort", colWidth: "48px" },
        { sTitle: "Group",
            mData: "groups",
            jwalaType: "array",
            displayProperty: "name",
            maxDisplayTextLen: 17,
            colWidth: "125px" }, { mData: null,
            jwalaType: "custom",
            jwalaRenderCfgFn: this.renderJvmControlPanelWidget.bind(this, "grp", "jvm") }, { sTitle: "State",
            mData: "stateLabel",
            sSortDataType: "ServerStateWidget",
            jwalaType: "custom",
            jwalaRenderCfgFn: this.renderJvmStateRowData.bind(this, "grp", "jvm"),
            colWidth: "115px" }];

        jvmChildTableDetails["tableDef"] = jvmChildTableDef;

        var childTableDetailsArray = [webServerOfGrpChildTableDetails, jvmChildTableDetails, webAppOfGrpChildTableDetails];

        return JwalaDataTable({ tableId: "group-operations-table",
            className: "dataTable hierarchical",
            tableDef: groupTableDef,
            data: this.props.data,
            rowSubComponentContainerClassName: "row-sub-component-container",
            childTableDetails: childTableDetailsArray,
            selectItemCallback: this.props.selectItemCallback,
            initialSortColumn: [[2, "asc"]],
            isColResizable: true,
            openRowLoadDataDoneCallback: this.openRowLoadDataDoneCallbackHandler,
            collapseRowCallback: this.collapseRowCallback });
    },
    collapseRowCallback: function (data) {
        this.props.collapseRowCallback(data);
    },
    onSelectWebServerTableRow: function (group, data, isActive) {
        // Why was I calling the method below ? It's causes a bug wherein the action and event logs items are cleared!
        // TODO: Find out why we call the method below in this event.
        // var serverName = isActive ? data.name : null;
        // this.openRowLoadDataDoneCallbackHandler(data.parentItemId, group, serverName);
    },
    onSelectJvmTableRow: function (group, data, isActive) {
        // Why was I calling the method below ? It's causes a bug wherein the action and event logs items are cleared!
        // TODO: Find out why we call the method below in this event.
        // var serverName = isActive ? data.jvmName : null;
        // this.openRowLoadDataDoneCallbackHandler(data.parentItemId, group, data.jvmName);
    },
    openRowLoadDataDoneCallbackHandler: function (groupId, groupName, serverName) {
        var self = this;
        var key = GroupOperations.getExtDivCompId(groupId);

        // Mount a status window where one can see action events and status errors.
        var mountingNode = $("#" + key);
        mountingNode.empty(); // Remove the node. TODO: Use react's unmount.
        React.render(CommandStatusWidget({ key: key, groupName: groupName, serverName: serverName,
            isOpen: jwalaVars["opsGrpChildrenViewOpen"] === "true" }), mountingNode.get(0), function () {
            self.props.commandStatusWidgetMap[key] = this; // TODO: Deprecate this!
            self.props.commandStatusWidgetMap[groupName] = this;
        });

        // Update web servers and JVMs header states.
        // Note: Since the group operations page is a mix of React and spaghetti code, we do the update using jquery.
        //       This will have to go (replace with better code) when group operations is refactored.
        var wsStartedCount = $("#ws-child-table_group-operations-table_" + groupId + "_wsStartedCount");
        if (wsStartedCount.length) {
            ServiceFactory.getGroupService().getStartedWebServersAndJvmsCount(groupName).then(function (response) {
                wsStartedCount.text("Started: " + response.applicationResponseContent.webServerStartedCount + "/" + response.applicationResponseContent.webServerCount);
            });
        }

        var jvmStartedCount = $("#jvm-child-table_group-operations-table_" + groupId + "_jvmStartedCount");
        if (jvmStartedCount.length) {
            ServiceFactory.getGroupService().getStartedWebServersAndJvmsCount(groupName).then(function (response) {
                jvmStartedCount.text("Started: " + response.applicationResponseContent.jvmStartedCount + "/" + response.applicationResponseContent.jvmCount);
            });
        }
    },
    renderGroupStateRowData: function (type, dataTable, data, aoColumnDefs, itemIndex, parentId) {
        var self = this;
        aoColumnDefs[itemIndex].bSortable = false;
        aoColumnDefs[itemIndex].fnCreatedCell = function (nTd, sData, oData, iRow, iCol) {
            var key = "grp" + oData.id.id;
            return React.render(ServerStateWidget({ key: key, defaultStatus: "",
                errorMsgDlgTitle: oData.name + " State Error Messages" }), nTd, function () {
                GroupOperations.groupStatusWidgetMap[key] = this;
                var serverCount = oData.currentState.jvmCount + oData.currentState.webServerCount;
                var serverStartedCount = oData.currentState.jvmStartedCount + oData.currentState.webServerStartedCount;
                var serverStoppedCount = oData.currentState.webServerStoppedCount + oData.currentState.jvmStoppedCount + oData.currentState.jvmForciblyStoppedCount;;

                var statusColorCode = "partial";
                if (serverCount === 0 || serverStoppedCount === serverCount) {
                    statusColorCode = "stopped";
                } else if (serverStartedCount === serverCount) {
                    statusColorCode = "started";
                }

                this.setStatus("Started: " + serverStartedCount + "/" + serverCount, new Date(), "", statusColorCode);
            });
        }.bind(this);
    },
    renderJvmControlPanelWidget: function (parentPrefix, type, dataTable, data, aoColumnDefs, itemIndex, parentId, parentName) {
        var self = this;
        aoColumnDefs[itemIndex].fnCreatedCell = function (nTd, sData, oData, iRow, iCol) {
            return React.render(JvmControlPanelWidget({ data: oData,
                parentGroup: parentName,
                webServerService: webServerService,
                jvmService: jvmService,
                jvmStartCallback: this.jvmStart,
                jvmStopCallback: this.jvmStop,
                jvmGenerateConfigCallback: this.jvmGenerateConfig,
                jvmDeleteCallback: this.jvmDelete,
                jvmHeapDumpCallback: this.jvmHeapDump,
                jvmDiagnoseCallback: this.jvmDiagnose }), nTd, function () {});
        }.bind(this);
    },
    renderWebServerGenerateBtn: function (parentPrefix, type, dataTable, data, aoColumnDefs, itemIndex, parentId) {
        var self = this;
        aoColumnDefs[itemIndex].fnCreatedCell = function (nTd, sData, oData, iRow, iCol) {
            return React.render(React.DOM.a(null, "Testing..."), nTd);
        }.bind(this);
    },
    renderWebServerControlPanelWidget: function (parentPrefix, type, dataTable, data, aoColumnDefs, itemIndex, parentId, parentName) {
        var self = this;
        aoColumnDefs[itemIndex].fnCreatedCell = function (nTd, sData, oData, iRow, iCol) {
            var newData = $.extend({}, oData); // do a shallow clone so we don't mutate the source data
            newData["parentGroup"] = parentName;
            return React.render(WebServerControlPanelWidget({ data: newData,
                parentGroup: parentName,
                webServerService: webServerService,
                webServerStartCallback: this.webServerStart,
                webServerStopCallback: this.webServerStop,
                webServerDeleteCallback: this.webServerDelete}), nTd, function () {});
        }.bind(this);
    },
    renderWebAppControlPanelWidget: function (parentPrefix, type, dataTable, data, aoColumnDefs, itemIndex, parentId, parentName) {
        var self = this;
        aoColumnDefs[itemIndex].fnCreatedCell = function (nTd, sData, oData, iRow, iCol) {
            var newData = $.extend({}, oData); // do a shallow clone so we don't mutate the source data
            newData["parentGroup"] = parentName;
            return React.render(WebAppControlPanelWidget({data: newData, parentGroup: parentName,
                                webAppService: webAppService}), nTd, function () {});
        }.bind(this);
    },
    renderWebServerStateRowData: function (parentPrefix, type, dataTable, data, aoColumnDefs, itemIndex, parentId) {
        var self = this;
        aoColumnDefs[itemIndex].fnCreatedCell = function (nTd, sData, oData, iRow, iCol) {
            var key = parentPrefix + parentId + type + oData.id.id;
            return React.render(ServerStateWidget({ key: key, defaultStatus: "",
                errorMsgDlgTitle: oData.name + " State Error Messages" }), nTd, function () {
                GroupOperations.webServerStatusWidgetMap[key] = this;
                // TODO: Include lastUpdatedDate from REST and replace "new Date()".
                if(oData.stateLabel) {
                    this.setStatus(oData.stateLabel, new Date(), oData.errorStatus);
                }
            });
        }.bind(this);
    },
    renderJvmStateRowData: function (parentPrefix, type, dataTable, data, aoColumnDefs, itemIndex, parentId) {
        var self = this;
        aoColumnDefs[itemIndex].fnCreatedCell = function (nTd, sData, oData, iRow, iCol) {
            var key = parentPrefix + parentId + type + oData.id.id;
            return React.render(ServerStateWidget({ key: key, defaultStatus: "",
                errorMsgDlgTitle: oData.jvmName + " State Error Messages" }), nTd, function () {
                GroupOperations.jvmStatusWidgetMap[key] = this;

                if (oData.state !== GroupOperations.FAILED && oData.state !== GroupOperations.START_SENT && oData.state !== GroupOperations.STOP_SENT) {
                    // Check if there is new state, if there is use it since the jvm state only
                    // gets updated when GroupOperations is initialized.
                    if (self.state.currentJvmState[oData.id.id] === undefined) {
                        this.setStatus(oData.stateLabel, oData.lastUpdatedDate, oData.errorStatus);
                    } else {
                        this.setStatus(self.state.currentJvmState[oData.id.id].stateLabel, oData.lastUpdatedDate, self.state.currentJvmState[oData.id.id].errorStatus);
                    }
                }
            });
        }.bind(this);
    },
    getWebServersOfGrp: function (idObj, responseCallback) {
        var self = this;

        webServerService.getWebServerByGroupId(idObj.parentId, function (response) {
            // This is when the row is initially opened.
            // Unlike JVMs, web server data is retrieved when the row is opened.

            if (response.applicationResponseContent !== undefined && response.applicationResponseContent !== null) {
                response.applicationResponseContent.forEach(function (o) {
                    o["parentItemId"] = idObj.parentId;
                });
            }

            responseCallback(response);

            self.props.updateWebServerDataCallback(response.applicationResponseContent);
        }, false);
    },
    getApplicationsOfGrp: function (idObj, responseCallback) {
        // TODO: Verify if we need to display the applications on a group. If we need to, I think this needs fixing. For starters, we need to include the group id in the application response.
        webAppService.getWebAppsByGroup(idObj.parentId, responseCallback, false);
    },
    getApplicationsOfJvm: function (idObj, responseCallback) {

        webAppService.getWebAppsByJvm(idObj.parentId, function (data) {

            var webApps = data.applicationResponseContent;
            for (var i = 0; i < webApps.length; i++) {
                if (idObj.rootId !== webApps[i].group.id.id) {
                    webApps[i]["className"] = "highlight";
                } else {
                    webApps[i]["className"] = ""; // This is needed to prevent datatable from complaining
                    // for a missing "className" data since "className" is a defined
                    // filed in mData (please research for JQuery DataTable)
                }
            }

            responseCallback(data);
        });
    },
    deploy: function (id) {
        alert("Deploy applications for group_" + id + "...");
    },
    enableButtonThunk: function (buttonSelector, iconClass) {
        return function () {
            $(buttonSelector).prop('disabled', false);
            if ($(buttonSelector + " span").hasClass("ui-icon")) {
                $(buttonSelector).attr("class", "ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only ui-button-height");
                $(buttonSelector).find("span").attr("class", "ui-icon " + iconClass);
            } else {
                $(buttonSelector).removeClass("ui-state-disabled");
            }
        };
    },
    disableButtonThunk: function (buttonSelector) {
        return function () {
            $(buttonSelector).prop('disabled', true);
            if ($(buttonSelector + " span").hasClass("ui-icon")) {
                $(buttonSelector).attr("class", "busy-button");
                $(buttonSelector).find("span").removeClass();
            } else {
                $(buttonSelector).addClass("ui-state-disabled");
            }
        };
    },
    enableHeapDumpButtonThunk: function (buttonSelector) {
        return function () {
            $(buttonSelector).attr("class", "ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only ui-button-height");
            $(buttonSelector).find("span").attr("class", "ui-icon ui-icon-heap-dump");
        };
    },
    disableHeapDumpButtonThunk: function (buttonSelector) {
        return function () {
            $(buttonSelector).find("span").attr("class", "busy-button");
            $(buttonSelector).attr("class", "busy-button");
        };
    },
    enableJvmGenerateButtonThunk: function (buttonSelector) {
        return function () {
            $(buttonSelector).attr("class", "ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only ui-button-height");
            $(buttonSelector).find("span").attr("class", "ui-icon ui-icon-gear-custom");
        };
    },
    disableJvmGenerateButtonThunk: function (buttonSelector) {
        return function () {
            $(buttonSelector).find("span").attr("class", "busy-button");
            $(buttonSelector).attr("class", "busy-button");
        };
    },
    disableEnable: function (buttonSelector, func, iconClass) {
        var disable = this.disableButtonThunk(buttonSelector);
        var enable = this.enableButtonThunk(buttonSelector, iconClass);
        Promise.method(disable)().then(func).lastly(enable);
    },
    enableLinkThunk: function (linkSelector) {
        return function () {
            $(linkSelector).removeClass("disabled");
        };
    },
    disableLinkThunk: function (linkSelector) {
        return function () {
            $(linkSelector).addClass("disabled");
        };
    },
    disableEnableHeapDumpButton: function (selector, requestTask, requestCallbackTask, errHandler) {
        var disable = this.disableHeapDumpButtonThunk(selector);
        var enable = this.enableHeapDumpButtonThunk(selector);
        Promise.method(disable)().then(requestTask).then(requestCallbackTask).caught(errHandler).lastly(enable);
    },
    disableEnableJvmGenerateConfigButton: function (selector, requestTask, requestCallbackTask, errHandler) {
        var disable = this.disableJvmGenerateButtonThunk(selector);
        var enable = this.enableJvmGenerateButtonThunk(selector);
        Promise.method(disable)().then(requestTask).lastly(enable);
    },

    /**
     * Displays a confirmation dialog box
     * @param id forms part of the dialog box's id e.g. JVM id
     * @param buttonSelector the jquery selector which identifies to which button this dialog box will be mounted to
     * @param msg the message to display in the dialog box
     * @param callbackOnConfirm callback to execute when action is confirmed
     */
    confirmActionDialogBox: function (id, buttonSelector, msg, callbackOnConfirm) {
        let dialogId = "group-confirm-action-dialog-" + id;
        $(buttonSelector).parent().append("<div id='" + dialogId + "' style='text-align:left'>" + msg + "</div>");
        $(buttonSelector).parent().find("#" + dialogId).dialog({
            title: "Confirmation",
            width: "auto",
            modal: true,
            buttons: {
                "Yes": function () {
                    callbackOnConfirm(id, buttonSelector);
                    $(this).dialog("close");
                },
                "No": function () {
                    $(this).dialog("close");
                }
            },
            open: function () {
                // Set focus to "Yes button"
                $(this).closest('.ui-dialog').find('.ui-dialog-buttonpane button:eq(0)').focus();
            }
        });
    },

    /**
     * Verifies and confirms to the user whether to continue the operation or not.
     * @param id the id (e.g. group id)
     * @param name the name (e.g. group name)
     * @param buttonSelector the jquery button selector
     * @param operation control operation namely "Start" and "Stop"
     * @param operationCallback operation to execute (e.g. startGroupCallback)
     * @param groupChildType a group's children to verify membership in other groups
     *                       (jvm - all JVMs, webServer - all web servers, undefined = jvms and web servers)
     */
    verifyAndConfirmControlOperation: function (id, buttonSelector, name, operation, operationCallback, groupChildType) {
        var self = this;
        groupService.getChildrenOtherGroupConnectionDetails(id, groupChildType).then(function (data) {
            if (data.applicationResponseContent instanceof Array && data.applicationResponseContent.length > 0) {
                var membershipDetails = groupOperationsHelper.createMembershipDetailsHtmlRepresentation(data.applicationResponseContent);
                self.confirmActionDialogBox(id, buttonSelector, membershipDetails + "<br/><b>Are you sure you want to "
                    + operation + " <span style='color:#2a70d0'>" + name + "</span> ?</b>", operationCallback);
            } else {
                operationCallback(id, buttonSelector);
            }
        });
    },
    startGroupCallback: function (id, buttonSelector) {
        var self = this;
        groupService.getGroup(id, function (response) {
            var commandStatusWidget = self.props.commandStatusWidgetMap[GroupOperations.getExtDivCompId(id)];
            if (commandStatusWidget) {
                var jvms = response.applicationResponseContent.jvms;
            }
            self.disableEnable(buttonSelector, function () {
                return groupControlService.startGroup(id);
            }, "ui-icon-play");
        }, false);
    },
    startGroup: function (id, buttonSelector, name) {
        this.verifyAndConfirmControlOperation(id, buttonSelector, name, "start", this.startGroupCallback);
    },
    stopGroupCallback: function (id, buttonSelector) {
        var self = this;
        groupService.getGroup(id, function (response) {
            var commandStatusWidget = self.props.commandStatusWidgetMap[GroupOperations.getExtDivCompId(id)];
            if (commandStatusWidget) {
                var jvms = response.applicationResponseContent.jvms;
            }
            self.disableEnable(buttonSelector, function () {
                return groupControlService.stopGroup(id);
            }, "ui-icon-stop");
        }, false);
    },
    stopGroup: function (id, buttonSelector, name) {
        this.verifyAndConfirmControlOperation(id, buttonSelector, name, "stop", this.stopGroupCallback);
    },

    startGroupJvms: function (event) {
        var self = this;
        var callback = function (id, buttonSelector) {

            groupService.getGroup(event.data.name, function (response) {
                var jvms = response.applicationResponseContent.jvms;
                var commandStatusWidget = self.props.commandStatusWidgetMap[GroupOperations.getExtDivCompId(event.data.id)];
                self.disableEnable(event.data.buttonSelector, function () {
                    return groupControlService.startJvms(event.data.id);
                }, "ui-icon-play");
            },
            true);
        };

        this.verifyAndConfirmControlOperation(event.data.id, event.data.buttonSelector, event.data.name, "start all JVMs under", callback, "jvm");
    },

    stopGroupJvms: function (event) {
        var self = this;
        var callback = function (id, buttonSelector) {

            groupService.getGroup(event.data.name, function (response) {
                var jvms = response.applicationResponseContent.jvms;
                self.disableEnable(event.data.buttonSelector, function () {
                    return groupControlService.stopJvms(event.data.id);
                }, "ui-icon-stop");
            }, true);
        };

        this.verifyAndConfirmControlOperation(event.data.id, event.data.buttonSelector, event.data.name, "stop all JVMs under", callback, "jvm");
    },
    startGroupWebServers: function (event) {
        var self = this;
        var callback = function (id, buttonSelector) {
            self.disableEnable(event.data.buttonSelector, function () {
                return groupControlService.startWebServers(event.data.id);
            }, "ui-icon-play");
        };

        this.verifyAndConfirmControlOperation(event.data.id, event.data.buttonSelector, event.data.name, "start all Web Servers under", callback, "webServer");
    },
    stopGroupWebServers: function (event) {
        var self = this;
        var callback = function (id, buttonSelector) {
            self.disableEnable(event.data.buttonSelector, function () {
                return groupControlService.stopWebServers(event.data.id);
            }, "ui-icon-stop");
        };

        this.verifyAndConfirmControlOperation(event.data.id, event.data.buttonSelector, event.data.name, "stop all Web Servers under", callback, "webServer");
    },
    generateGroupWebServers: function (event) {
        var self = this;
        var callback = function (id, buttonSelector) {
            self.disableEnable(event.data.buttonSelector, function () {
                return groupControlService.generateWebServers(event.data.id, function (resp) {
                    $.alert("Successfully generated the web servers for " + resp.applicationResponseContent.name, false);
                }, function (errMsg, errDetails) {
                    $.errorAlert(errMsg, "Generate Web Servers Failed", false, errDetails);
                });
            }, "ui-icon-stop");
            self.writeWebServerActionToCommandStatusWidget(event.data.id, "INVOKE");
        };
        this.verifyAndConfirmControlOperation(event.data.id, event.data.buttonSelector, event.data.name, "generate all Web Servers under", callback, "webServer");
    },
    drainGroupWebServers: function (event) {
        var self = this;
        var callback = function (id, buttonSelector) {
            self.disableEnable(event.data.buttonSelector, function () {
                return groupControlService.drainWebServers(event.data.name, function (errMsg) {
                   $.errorAlert(errMsg, "Drain users Group Failed", false);
                });
            }, "ui-icon-stop");
        };
        this.verifyAndConfirmControlOperation(event.data.id, event.data.buttonSelector, event.data.name, "drain all Web Servers under", callback, "webServer");
        console.log("clicked drain all");
    },
    generateGroupJvms: function (event) {
        var self = this;
        var callback = function (id, buttonSelector) {
            groupService.getGroup(event.data.name, function (response) {
                var jvms = response.applicationResponseContent.jvms;
                var commandStatusWidget = self.props.commandStatusWidgetMap[GroupOperations.getExtDivCompId(event.data.id)];
                jvms.forEach(function (jvm) {
                    commandStatusWidget.push({
                        stateString: "INVOKE",
                        asOf: new Date().getTime(),
                        message: "",
                        from: "JVM " + jvm.jvmName, userId: AdminTab.getCookie("userName") }, "action-status-font");
                });
                self.disableEnable(event.data.buttonSelector, function () {
                    return groupControlService.generateJvms(event.data.id, function (resp) {
                        $.alert("Successfully generated the JVMs for " + resp.applicationResponseContent.name, false);
                        }, function (errMsg, errDetails) {
                        $.errorAlert(errMsg, "Generate JVMs Failed", false, errDetails);
                    });
                }, "ui-icon-stop");
            }, true);
        };

        this.verifyAndConfirmControlOperation(event.data.id, event.data.buttonSelector, event.data.name, "generate all JVMs under", callback, "jvm");
    },
    writeWebServerActionToCommandStatusWidget: function (groupId, action) {
        var self = this;
        webServerService.getWebServerByGroupId(groupId).then(function (response) {
            var webServers = response.applicationResponseContent;
            var commandStatusWidget = self.props.commandStatusWidgetMap[GroupOperations.getExtDivCompId(groupId)];
            webServers.forEach(function (webServer) {
                commandStatusWidget.push({ stateString: action, asOf: new Date().getTime(), message: "",
                    from: "Web Server " + webServer.name, userId: AdminTab.getCookie("userName") }, "action-status-font");
            });
        });
    },

    jvmHeapDump: function (id, selector, host) {
        var requestHeapDump = function () {
            return jvmControlService.heapDump(id.id);
        };
        var heapDumpRequestCallback = function (response) {
            var msg;
            if (!response.applicationResponseContent.standardError) {
                msg = response.applicationResponseContent.standardOutput;
                if (msg.trim() === "") {
                    msg = "Oops! Something went wrong! The JVM might not have been started.";
                    $.errorAlert(msg, "Heap Dump", false);
                } else {
                    $.alert(msg, "Heap Dump", false);
                }
            } else {
                msg = response.applicationResponseContent.execData.standardError;
                $.errorAlert(msg, "Heap Dump", false);
            }
            $(selector).attr("title", "Last heap dump status: " + msg);
        };
        var heapDumpErrorHandler = function (e) {
            var errCodeAndMsg;
            try {
                var errCode = JSON.parse(e.responseText).msgCode;
                var errMsg = JSON.parse(e.responseText).applicationResponseContent;
                errCodeAndMsg = "Error: " + errCode + (errMsg !== "" ? " - " : "") + errMsg;
            } catch (e) {
                errCodeAndMsg = e.responseText;
            }
            $.alert(errCodeAndMsg, "Heap Dump Error!", false);
            $(selector).attr('title', "Last heap dump status: " + errCodeAndMsg);
        };

        this.disableEnableHeapDumpButton(selector, requestHeapDump, heapDumpRequestCallback, heapDumpErrorHandler);
    },
    jvmGenerateConfig: function (data, selector) {
        var self = this;
        var requestJvmGenerateConfig = function () {
            return ServiceFactory.getJvmService().deployJvmConfAllFiles(data.jvmName, self.generateJvmConfigSucccessCallback, self.generateJvmConfigErrorCallback);
        };
        this.disableEnableJvmGenerateConfigButton(selector, requestJvmGenerateConfig, this.generateJvmConfigSucccessCallback, this.generateJvmConfigErrorCallback);
    },
    generateJvmConfigSucccessCallback: function (response) {
        $.alert("Successfully generated and deployed JVM resource files", response.applicationResponseContent.jvmName, false);
    },

    generateJvmConfigErrorCallback: function (applicationResponseContent, errDetails) {
        $.errorAlert(applicationResponseContent, "Error deploying JVM resource files", false, errDetails);
    },

    confirmJvmWebServerStopGroupDialogBox: function (id, parentItemId, buttonSelector, msg, callbackOnConfirm, cancelCallback) {
        var dialogId = "start-stop-confirm-dialog-for_group" + parentItemId + "_jvm_ws_" + id;
        $(buttonSelector).parent().append("<div id='" + dialogId + "' style='text-align:left'>" + msg + "</div>");
        $(buttonSelector).parent().find("#" + dialogId).dialog({
            title: "Confirmation",
            width: "auto",
            modal: true,
            buttons: {
                "Yes": function () {
                    callbackOnConfirm(id);
                    $(this).dialog("close");
                },
                "No": function () {
                    $(this).dialog("close");
                    cancelCallback();
                }
            },
            open: function () {
                // Set focus to "Yes button"
                $(this).closest('.ui-dialog').find('.ui-dialog-buttonpane button:eq(0)').focus();
            }
        });
    },
    verifyAndConfirmJvmWebServerControlOperation: function (id, parentItemId, buttonSelector, name, groups, operation, operationCallback, cancelCallback, serverType) {
        if (groups.length > 1) {
            var msg = "<b>" + serverType + " <span style='color:#2a70d0'>" + name + "</span> is a member of:</b><br/>" + groupOperationsHelper.groupArrayToHtmlList(groups, parentItemId) + "<br/><b> Are you sure you want to " + operation + " <span style='color:#2a70d0'>" + name + "</span></b> ?";
            this.confirmJvmWebServerStopGroupDialogBox(id, parentItemId, buttonSelector, msg, operationCallback, cancelCallback);
        } else {
            operationCallback(id);
        }
    },
    jvmStart: function (data, buttonSelector, cancelCallback) {
        var self = this;
        var doJvmStart = function (jvmId) {
            jvmControlService.startJvm(jvmId);
        };

        this.verifyAndConfirmJvmWebServerControlOperation(data.id.id, data.parentItemId, buttonSelector, data.jvmName, data.groups, "start", doJvmStart, cancelCallback, "JVM");
    },

    jvmStop: function (data, buttonSelector, cancelCallback) {
        var self = this;
        var doJvmStop = function (jvmId) {
            jvmControlService.stopJvm(jvmId);
        };

        this.verifyAndConfirmJvmWebServerControlOperation(data.id.id, data.parentItemId, buttonSelector, data.jvmName, data.groups, "stop", doJvmStop, cancelCallback, "JVM");
    },
    jvmDiagnose: function (data, buttonSelector, cancelCallback) {
        var commandStatusWidget = this.props.commandStatusWidgetMap[GroupOperations.getExtDivCompId(data.parentItemId)];
        ServiceFactory.getJvmService().diagnoseJvm(data.id.id);
    },
    webServerStart: function (id, buttonSelector, data, parentItemId, cancelCallback) {
        var self = this;
        var doWebServerStart = function (webServerId) {
            webServerControlService.startWebServer(webServerId);
        };

        this.verifyAndConfirmJvmWebServerControlOperation(id, parentItemId, buttonSelector, data.name, data.groups, "start", doWebServerStart, cancelCallback, "Web Server");
    },

    webServerStop: function (id, buttonSelector, data, parentItemId, cancelCallback) {
        var self = this;
        var doWebServerStop = function (webServerId) {
            webServerControlService.stopWebServer(webServerId);
        };

        this.verifyAndConfirmJvmWebServerControlOperation(id, parentItemId, buttonSelector, data.name, data.groups, "stop", doWebServerStop, cancelCallback, "Web Server");
    },

    webServerDelete: function(buttonSelector, data) {
        this.confirmActionDialogBox(data.id.id, buttonSelector, "Are you sure you want to delete web server " + data.name,
                                    this.webServerConfirmDeleteCallback.bind(this, data));
    },

    webServerConfirmDeleteCallback: function(data) {
        let self = this;
        webServerService.deleteWebServer(data.id.id, true).then(function(e){
            // Remove deleted row
            $("tr td:contains('" + data.name + "')").filter(function() {
                if ($(this).text() === data.name) {
                    $(this).parent().remove();
                }
            });
        }).caught(
            function(e) {
                self.setState({showDeleteConfirmDialog: false})
                let msg;
                if (e.responseText) {
                    msg = JSON.parse(e.responseText).message;
                } else {
                    msg = JSON.stringify(e);
                }
                $.errorAlert(msg, "Error");
            }
        );
    },

    jvmDelete: function(buttonSelector, data) {
        this.confirmActionDialogBox(data.id.id, buttonSelector, "Are you sure you want to delete JVM " + data.jvmName,
                                    this.jvmConfirmDeleteCallback.bind(this, data));
    },

    jvmConfirmDeleteCallback: function(data) {
        let self = this;
        jvmService.deleteJvm(data.id.id, true).then(function(e){
            // Remove deleted row
            $("tr td:contains('" + data.jvmName + "')").filter(function() {
                if ($(this).text() === data.jvmName) {
                    $(this).parent().remove();
                }
            });
        }).caught(
            function(e) {
                self.setState({showDeleteConfirmDialog: false})
                let msg;
                if (e.responseText) {
                    msg = JSON.parse(e.responseText).message;
                } else {
                    msg = JSON.stringify(e);
                }
                $.errorAlert(msg, "Error");
            }
        );
    }
});
