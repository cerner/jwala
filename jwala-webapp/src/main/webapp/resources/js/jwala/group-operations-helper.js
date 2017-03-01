var groupOperationsHelper = function(){

    var mergeGroupStateData = function(groupStateById, groups) {
        return groups.map(function(group) {
                                return mergeSingleGroupState(groupStateById[group.id.id], group);
                          });
    };

    // TODO: Check when this gets called
    var mergeWebServerStateData = function(webServerStateById, webServers) {
        return webServers.map(function(webServer) {
                                                    return mergeSingleWebServerState(webServerStateById[webServer.id.id], webServer);
                                                  });
    };

    var mergeJvmStateData = function(jvmStateById, jvms) {
        return jvms.map(function(jvm) {return mergeSingleJvmState(jvmStateById[jvm.id.id], jvm);});
    };

    var mergeSingleGroupState = function(groupState, group) {
        if (groupState !== undefined) {
            group.state = groupState;
        } else {
            // TODO: decide if maybe the rendering component should render missing web server state data as UNKNOWN instead
            group.state = {state: "UNKNOWN",
                           stateString: "UNKNOWN",
                           asOf: Date.now(),
                           id: { id: group.id}};
        }
        return group;
    };

    // TODO: Check when this gets called
    var mergeSingleWebServerState = function(webServerState, webServer) {
        if (webServerState !== undefined) {
            webServer.state = webServerState;
        } else {
            // TODO: decide if maybe the rendering component should render missing web server state data as UNKNOWN instead
            webServer.state = {state: "UNKNOWN",
                               stateString: "UNKNOWN",
                               asOf: Date.now(),
                               id: { id: webServer.id}};
        }
        return webServer;
    };

    var mergeSingleJvmState = function(jvmState, jvm) {
        if (jvmState !== undefined) {
            jvm.state = jvmState;
        } else {
            // TODO: decide if maybe the rendering component should render missing jvm state data as UNKNOWN instead
            jvm.state = { state: "UNKNOWN",
                          stateString: "UNKNOWN",
                          asOf: Date.now(),
                          id: { id: jvm.id}};
        }
        return jvm;
    };

    var keyById = function(result, data, idKey) {
        data.forEach(function(d) { result[d[idKey].id] = d;});
    };

    var combineGroupStatesById = function(existingGroupStates, newGroupStates) {
        var result = {};
        var idKey = "id";

        keyById(result, existingGroupStates, idKey);
        keyById(result, newGroupStates, idKey);

        return result;
    };

    var combineWebServerStatesById = function(existingWebServerStates, newWebServerStates) {
        var result = {};
        var idKey = "id";

        keyById(result, existingWebServerStates, idKey);
        keyById(result, newWebServerStates, idKey);

        return result;
    };

    var combineJvmStatesById = function(existingJvmStates, newJvmStates) {
        var result = {};
        var idKey = "id";

        keyById(result, existingJvmStates, idKey);
        keyById(result, newJvmStates, idKey);

        return result;
    };

    var processGroups = function(existingGroups, newGroups, groupStatesById) {
        var combinedResult = {};
        var idKey = "id";

        keyById(combinedResult, existingGroups, idKey);
        keyById(combinedResult, newGroups, idKey);

        var result = extractValuesOnly(combinedResult);

        return mergeGroupStateData(groupStatesById, result);
    };

    var processWebServers = function(existingWebServers, newWebServers, webServerStatesById) {
        var combinedResult = {};
        var idKey = "id";

        keyById(combinedResult, existingWebServers, idKey);
        keyById(combinedResult, newWebServers, idKey);

        var result = extractValuesOnly(combinedResult);

        return mergeWebServerStateData(webServerStatesById, result);
    };

    var processJvms = function(existingJvms, newJvms, jvmStatesById) {
        var combinedResult = {};
        var idKey = "id";

        keyById(combinedResult, existingJvms, idKey);
        keyById(combinedResult, newJvms, idKey);

        var result = extractValuesOnly(combinedResult);

        return mergeJvmStateData(jvmStatesById, result);
    };

    var extractValuesOnly = function(valuesById) {
        var result = [];

        for (theId in valuesById) {
            if (valuesById.hasOwnProperty(theId)) {
                result.push(valuesById[theId]);
            }
        }

        return result;
    };

    var flatten = function(data) {
        var flattenHelper = function(helperData, helperResult) {
            for (var i=0; i<helperData.length; i++) {
                if (helperData[i].constructor === Array) {
                    flattenHelper(helperData[i], helperResult);
                } else {
                    helperResult.push(helperData[i]);
                }
            }
            return helperResult;
        };

        return flattenHelper(data, []);
    };

    var testMRenderVar = 0;

    return {

        processGroupData: function(existingGroups, newGroups, existingGroupStates, newGroupStates) {
            var combinedGroupStatesById = combineGroupStatesById(existingGroupStates,
                                                                 newGroupStates);

            var processedGroups = processGroups(existingGroups,
                                                newGroups,
                                                combinedGroupStatesById);

            var processedGroupStates = extractValuesOnly(combinedGroupStatesById);

            return {
                groups: processedGroups,
                groupStates: processedGroupStates
            };
        },
        processWebServerData: function(existingWebServers, newWebServers, existingWebServerStates, newWebServerStates) {
            var combinedWebServerStatesById = combineWebServerStatesById(existingWebServerStates,
                                                                         newWebServerStates);

            var processedWebServers = processWebServers(existingWebServers,
                                                        newWebServers,
                                                        combinedWebServerStatesById);

            var processedWebServerStates = extractValuesOnly(combinedWebServerStatesById);

            return {
                webServers: processedWebServers,
                webServerStates: processedWebServerStates
            };
        },
        // TODO: Delete this along with its supporting methods since we are getting the states directly from the JVM table.
        processJvmData: function(existingJvms, newJvms, existingJvmStates, newJvmStates) {

            var combinedJvmStatesById = combineJvmStatesById(existingJvmStates,
                                                             newJvmStates);

            var processedJvms = processJvms(existingJvms,
                                            newJvms,
                                            combinedJvmStatesById);

            var processedJvmStates = extractValuesOnly(combinedJvmStatesById);

            return {
                jvms: processedJvms,
                jvmStates: processedJvmStates
            };
        },

        extractJvmDataFromGroups: function(jvmDataInGroups) {
            var result = jvmDataInGroups.map(function(group) { return group.jvms;});
            return flatten(result);
        },
        markGroupExpanded: function(groups, groupId, isExpanded) {
            var groupsById = {};
            keyById(groupsById, groups, "id");
            groupsById[groupId].isExpanded = isExpanded;
            return { groups: extractValuesOnly(groupsById)};
        },
        markJvmExpanded: function(jvms, jvmId, isExpanded) {
            var jvmsById = {};
            keyById(jvmsById, jvms, "id");
            jvmsById[jvmId].isExpanded = isExpanded;
            return { jvms: extractValuesOnly(jvmsById)};
        },

        getGroupStatesById: function(groups) {
            var result = [];
            groups.forEach(function(group) {
                groups.forEach(
                    function(group) {
                        result.push({groupId: group.id, state: group.state})
                    });
            });
            return result;
        },

        getCurrentDateTime: function(time) {
            var currentDate = new Date(time);
            return  ('0' + (currentDate.getMonth() + 1)).slice(-2) + '/' +
                    ('0' + currentDate.getDate()).slice(-2) + '/' +
                    currentDate.getFullYear() + " " +
                    ('0' + currentDate.getHours()).slice(-2) + ":" +
                    ('0' + currentDate.getMinutes()).slice(-2) + ":" +
                    ('0' + currentDate.getSeconds()).slice(-2);
        },

        /**
         * Splits a message specified by the msg parameter by the first CR-
         * @param msg the message
         * @return array that contains the short message and the stack trace.
         */
        splitErrorMsgIntoShortMsgAndStackTrace: function(msg) {
            var indexOfFirstLine = msg.indexOf("\r\n");
            if (indexOfFirstLine > -1) {
                var errMsg = msg.substring(0, indexOfFirstLine);
                var stackTrace = msg.substring(indexOfFirstLine, msg.length - 1);
                return [errMsg, stackTrace];
            }
            return [msg, ""];
        },
        groupArrayToHtmlList: function(groups, excludedGroupId) {
            var str = "<ul>";
            groups.forEach(function(group){
                if (excludedGroupId === undefined || excludedGroupId !== group.id.id) {
                    str += "<li>" + group.name + "</li>";
                }
            });
            return str + "</ul>";
        },
        lastItemEquals: function(array, key, val) {
            if (array.length > 0) {
                var o = array[array.length - 1];
                return o[key] === val;
            }
            return false;
        },

        /**
         * Transform to a wrapper {groupId, jvmIdm, state}.
         */
        getJvmStatesByGroupIdAndJvmId: function(jvms) {
            var result = [];
            jvms.forEach(function(jvm) {
                jvm.groups.forEach(function(group) {
                                       result.push({groupId: group.id, jvmId: jvm.id, state: jvm.state, name: jvm.jvmName})
                                   });
            });
            return result;
        },

        updateGroupsInDataTables: function(groups) {

            // NOTE: We can't redraw the table just to update the status since it would
            //       bring the user to the 1st page if a table redraw is initiated.
            //       The solution for now since we're using Datatable is to use JQuery
            //       to update the group status.
            // $("table[id*='group-operations-table']").filter(function(index, elem) { return $.fn.DataTable.fnIsDataTable(elem);})
            //                                              .each(function(index, elem) { $(elem).dataTable().fnDraw();});

            groups.forEach(function(group) {
                $(".group-state-" + group.id.id).html(group.state.stateString);
            });
        },

        updateWebServersInDataTables: function(groupId, webServerId, state) {
            $(".ws" + webServerId + "-grp" + groupId + "-state").html(state);
        },

        /**
         * Creates a HTML representation of group membership details
         *
         * @param membershipDetails group membership details array
         */
        createMembershipDetailsHtmlRepresentation: function(membershipDetails) {
            var html = "";
            membershipDetails.forEach(function(membershipDetail) {
                var groupNames = "<ul>";
                membershipDetail.groupNames.forEach(function(name){
                                                        groupNames += "<li>" + name + "</li>";
                                                    });
                html += "<b>" + (membershipDetail.type === "WEB_SERVER" ? "Web Server" : "JVM") +
                        " <span style='color:#2a70d0'>" +
                        membershipDetail.name + "</span> is a member of:</b><br/>" +
                        groupNames + "</ul><br/>";
            });
            return html;
        },

        updateDataTables: function(groupId, jvmId, state) {
            $(".jvm" + jvmId + "-grp" + groupId + "-state").html(state);
        },

        // TODO: Make keyGroupsById, keyWebServersById and keyJvmsById into one method
        keyGroupsById: function(groups) {
            var result = {};
            keyById(result, groups, "id");
            return result;
        },

        keyWebServersById: function(webServers) {
            var result = {};
            keyById(result, webServers, "id");
            return result;
        },

        keyJvmsById: function(jvms) {
            var result = {};
            keyById(result, jvms, "id");
            return result;
        },

        /**
         * Processes raw state that comes from the "control" Rest service(s).
         * Basically what this does as of Jan 4, 2015 is that it checks if the msg property of data is equal to "FORCE STOPPED".
         * If it is, the "extracted status" will have a state of "FORCE STOPPED" and the msg would be set to blank.
         * This was done to have the "FORCE KILL" message be displayed in the state column and preventing the error
         * icon from appearing.
         */
        extractStateDetails: function(data) {
            var msg = data.message;
            var state = data.stateString;
            var asOf = data.asOf;
            if (msg === "FORCED STOPPED") {
                state = msg;
                msg = "";
            }

            return {"state": state, "asOf": asOf, "msg": msg};
        }

    }
}();