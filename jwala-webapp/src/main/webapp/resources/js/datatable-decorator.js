//TODO The functions in here need to be name-spaced
/**
 * Creates an id delimited by dash delimeter
 * e.g. ["btn", "1"] returns btn-1
 */
var createDelimitedId = function(idFragments, delimiter) {
    var tmpId;
    for (var i = 0; i < idFragments.length; i++) {
        if (tmpId === undefined) {
            tmpId = idFragments[i];
        } else {
            tmpId = tmpId + delimiter + idFragments[i]
        }
    }
    return tmpId;
};

/**
 * This method transforms a table to a JQuery DataTable.
 */
var decorateTableAsDataTable = function(tableId,
                                        tableDef,
                                        applyThemeRoller,
                                        hideHeaderAndFooter,
                                        editCallback,
                                        datableDrawCallback,
                                        expandIcon,
                                        collapseIcon,
                                        childTableDetails,
                                        parentItemId /* e.g. group id. This is used to retrieve child data via the data callback method when the expand-collapse control is clicked */,
                                        rootId /* This is the first element id in a hierarchy */,
                                        initialSortColumn,
                                        parentItemName,
                                        paginationEnabled,
                                        openRowLoadDataCallback,
                                        collapseCallback){

    var self = this;

    // build column definitions based on props
    var aoColumnDefs = [];
    var aaSorting;
    if (initialSortColumn !== undefined) {
        aaSorting = initialSortColumn;
    } else {
        aaSorting = [];
    }

    $(tableDef).each(function(itemIndex, item, itemArray) {
            if (!isArray(item)) {
                aoColumnDefs[itemIndex] = {"sTitle": item.sTitle,
                                           "mData": item.mData,
                                           "aTargets": [itemIndex],
                                           "sSortDataType": item.sSortDataType};

                if(item.bVisible !== undefined) {
                    aoColumnDefs[itemIndex].bVisible = item.bVisible;
                }

                if (item.jwalaType === "control") {
                    self.expandCollapseEnabled = true;
                    aoColumnDefs[itemIndex].mDataProp = null;
                    aoColumnDefs[itemIndex].sClass = "control center";
                    aoColumnDefs[itemIndex].sWidth = "20px";
                    aoColumnDefs[itemIndex].fnCreatedCell = function (nTd, sData, oData, iRow, iCol) {
                        var o = renderExpandCollapseControl(tableId, parentItemId, rootId, childTableDetails,
                                                            sData, item.type, oData, expandIcon, collapseIcon, openRowLoadDataCallback,
                                                            collapseCallback);
                        return React.render(o, nTd);
                    }.bind(this);

                }

                if (item.mRender !== undefined ||
                    item.jwalaType === "button"  ||
                    item.jwalaType === "emptyColumn" ||
                    item.jwalaType === "control") {
                        aoColumnDefs[itemIndex].bSortable = (item.bSortable === undefined ? false : item.bSortable);
                }

                if (item.colWidth !== undefined) {
                    aoColumnDefs[itemIndex].sWidth = item.colWidth;
                }

                if (item.sClass !== undefined) {
                    aoColumnDefs[itemIndex].sClass = aoColumnDefs[itemIndex].sClass + " " + item.sClass;
                }

            } else {

                /**
                 * Only 1 field related to data can be merged to one cell!
                 * And that includes the ExpandCollapseControl which has an mData!
                 * This is because every cell can only have one mData.
                 */
                var theItem;
                var colWidth = 0;
                // look for the item with the mData
                for (var i = 0; i < item.length; i++) {
                    if (theItem === undefined && item[i].mData !== undefined && item[i].jwalaType === undefined) {
                        theItem = item[i];
                    }
                    colWidth += item[i].colWidth !== undefined ? item[i].colWidth : 0;
                }

                if (theItem === undefined) {
                    // Init to empty item
                    theItem = {"sTitle": null, "mData": null};
                }

                aoColumnDefs[itemIndex] = {"sTitle": theItem.sTitle,
                                           "mData": theItem.mData,
                                           "aTargets": [itemIndex]};

                aoColumnDefs[itemIndex].sClass = "nowrap";
                aoColumnDefs[itemIndex].bSortable = false;

                if (colWidth !== 0) {
                    aoColumnDefs[itemIndex].sWidth = colWidth + "px";
                }
            }

            if (!isArray(item) && item.mRender !== undefined) {
                // If mRender is set to a function
                aoColumnDefs[itemIndex].mRender = item.mRender;
            } else if (!isArray(item) && item.jwalaType === "custom") {
                if (item.jwalaRenderer == 'undefined') {
                    alert('You set jwalaType to custom, but you did not set jwalaRenderCfgFn to a function(dataTable, data, aoColumnDefs, i) { aoColumnDefs[i].mRender = function(data, type, full){}}!');
                }
                return item.jwalaRenderCfgFn(self, item, aoColumnDefs, itemIndex, parentItemId, parentItemName);
            } else {
                aoColumnDefs[itemIndex].mRender = function(data, type, full) {
                   if (!isArray(item)) {
                        return renderComponents(tableId,
                                                parentItemId,
                                                rootId,
                                                childTableDetails,
                                                item,
                                                data,
                                                type,
                                                full,
                                                expandIcon,
                                                collapseIcon,
                                                editCallback,
                                                parentItemName);
                    }

                    var renderStr = "";
                    for (var i = 0; i < item.length; i++) {
                        renderStr += renderComponents(tableId,
                                                      parentItemId,
                                                      rootId,
                                                      childTableDetails,
                                                      item[i],
                                                      data,
                                                      type,
                                                      full,
                                                      expandIcon,
                                                      collapseIcon,
                                                      editCallback,
                                                      parentItemName);
                    }
                    return "<div style='overflow:hidden;text-align:right'>" + renderStr + "</div>"
                }
            }
        });

        var dataTableProperties = {"aaSorting": aaSorting,
                                   "aoColumnDefs": aoColumnDefs,
                                   "bJQueryUI": applyThemeRoller === undefined ? true : applyThemeRoller,
                                   "bAutoWidth": false,
                                   "bStateSave": true,
                                   "aLengthMenu": [[25, 50, 100, 200, -1],
                                                   [25, 50, 100, 200, "All"]],
                                   "iDisplayLength": 25,
                                   "fnDrawCallback": datableDrawCallback,
                                   "sPaginationType": "jwala",
                                   "bPaginate": paginationEnabled === undefined ? true : paginationEnabled};

        if (hideHeaderAndFooter === false) {
            dataTableProperties["sDom"] = "t";
        }

        var decorated = $("#" + tableId).dataTable(dataTableProperties);
        return decorated;
};

var JwalaPager = {

    init: function() {
        $.fn.dataTableExt.oPagination.jwala = this;

    },
    "fnInit": function ( oSettings, nPaging, fnCallbackDraw )
    {
    $.fn.dataTableExt.oPagination.two_button.fnInit(oSettings, nPaging, fnCallbackDraw);
    },

    attachExpanderClickEvent: function(idx, obj) {
        var expander = this.allExpanders[obj.id];
        if(expander !== undefined) {
            $("#" + obj.id).off("click");
            $("#" + obj.id).on("click",
                expander.component.onClick.bind(expander.component,
                                                expander.dataSources,
                                                expander.childTableDetailsArray));
        }
    },

    attachButtonMouseOverAndClickEvents: function(idx, obj) {
        var reactBtn = this.allButtons[obj.id];
        var btn = $("#" + obj.id);
        if(reactBtn !== undefined) {
            DataTableButton.bindEvents(reactBtn);
        }
    },

   "fnUpdate": function ( oSettings, fnCallbackDraw )
    {
       $.fn.dataTableExt.oPagination.two_button.fnUpdate(oSettings, fnCallbackDraw);
       // Need to bind onclick to expand collapse options in this method

       var decorated = oSettings.nTable;

       if(decorated !== null) {
           var self = this;
           $('img', decorated).each(function(idx,obj) {self.attachExpanderClickEvent(idx, obj)});
           $('.ui-button').each(function(idx,obj) {self.attachButtonMouseOverAndClickEvents(idx, obj)});
       }
    },
    allExpanders : {},
    allButtons : {}
};

JwalaPager.init();

var renderComponents = function(tableId,
                                parentItemId,
                                rootId,
                                childTableDetails,
                                item,
                                data,
                                type,
                                full,
                                expandIcon,
                                collapseIcon,
                                editCallback,
                                parentItemName) {
    var renderedComponent;

    if (item.jwalaType === "space") {
        renderedComponent = "&nbsp;";
    } else if (item.jwalaType === "link") {
        renderedComponent = renderLink(item, tableId, data, type, full, editCallback);
    } else if (item.jwalaType === "array") {
        renderedComponent = renderArray(item, data);
        if (renderedComponent.length > item.maxDisplayTextLen) {
            renderedComponent = "<span title=" + renderedComponent.replace(/ /gi, "&nbsp;").replace(/,/gi, "&#10;") + ">"  +
                                renderedComponent.substring(0, item.maxDisplayTextLen) + "..." +
                                "</span>";
        }
    } else if (item.jwalaType === "button") {
        renderedComponent = renderButton(tableId, item, data, type, full, parentItemId);
    } else if (item.jwalaType === "emptyColumn") {
        renderedComponent = "";
    } else {

        if (data !== undefined && data !== null && data.length > item.maxDisplayTextLen) {
            renderedComponent = "<span title=' " + data + " '>" +
                                data.substring(0, item.maxDisplayTextLen) + "...</span>";
        } else {
            renderedComponent = data;
        }

    }
    return renderedComponent;
};

var isArray = function(val) {
    return (val instanceof Array)
};

var renderButton = function(tableId, item, data, type, full, parentItemId) {

    var btnClassifier = item.id !== undefined ? item.id : item.btnLabel;
    var id = tableId + "btn" + btnClassifier.replace(/[\. ,:-]+/g, '') +  full.id.id;

    var extraData;
    if (item.extraDataToPassOnCallback instanceof Array) {
        extraData = {};
        item.extraDataToPassOnCallback.forEach(function(property){
            extraData[property] = full[property];
        });
    } else {
        extraData = full[item.extraDataToPassOnCallback];
    }

    var reactBtn = new DataTableButton({id:id,
                                        sTitle:item.sTitle,
                                        className:item.className,
                                        customBtnClassName:item.customBtnClassName,
                                        customSpanClassName:item.customSpanClassName,
                                        clickedStateClassName:item.clickedStateClassName,
                                        itemId:full.id.id,
                                        label:item.btnLabel,
                                        callback:item.btnCallback,
                                        isToggleBtn:item.isToggleBtn,
                                        label2:item.label2,
                                        callback2:item.callback2,
                                        expectedState:item.expectedState,
                                        isBusyCallback:item.isBusyCallback,
                                        buttonClassName:item.buttonClassName,
                                        onClickMessage:item.onClickMessage,
                                        extraDataToPassOnCallback:extraData,
                                        busyStatusTimeout:item.busyStatusTimeout,
                                        parentItemId:parentItemId,
                                        disabled:item.disabled});

    JwalaPager.allButtons[id] = reactBtn;

    return React.renderComponentToStaticMarkup(reactBtn);

};

var renderLink = function(item, tableId, data, type, full, editCallback) {
    if (item.hRefCallback === undefined) {
        var linkLabelPartId = item.linkLabel !== undefined ? item.linkLabel.replace(/[\. ,:-]+/g, '')
                                                           : data.replace(/[\. ,:-]+/g, '');
        var id = createDelimitedId([tableId, "link",  linkLabelPartId, full.id.id], "_");
        return React.renderComponentToStaticMarkup(new Anchor({id:id,
                                                               className: "anchor-font-style",
                                                               data:full,
                                                               value:item.linkLabel !== undefined ?
                                                                     item.linkLabel :
                                                                     data,
                                                               callback:item.onClickCallback !== undefined ?
                                                                        item.onClickCallback :
                                                                        editCallback,
                                                               waitForResponse:item.waitForResponse,
                                                               maxDisplayTextLen:item.maxDisplayTextLen}));
    }  else {
        return "<a class='anchor-font-style' href='" + item.hRefCallback(full) + "' target='_blank'>" + item.linkLabel + "</a>";
    }
};

var renderExpandCollapseControl = function(tableId, parentItemId, rootId, childTableDetails, data, type, full, expandIcon, collapseIcon, openRowLoadDataDoneCallback, collapseCallback) {
    var parentItemId = (parentItemId === undefined ? full.id.id : parentItemId);
    var theRootId = (rootId === undefined ? full.id.id : rootId);
    var delimitedId = createDelimitedId([tableId, full.id.id], "_");

    if (Object.prototype.toString.call(childTableDetails) === "[object Array]") {
        childTableDetails.forEach(function(childTableDetail) {
            if (childTableDetail.dataCallback === undefined) {
                // Note: There should be a generic way to get the data (meaning no specifics like jvm) since this
                //       js code functions as a generic helper.
                // TODO: Remove specifics!
                childTableDetail.data = full.jvms;
            }
        })
    } else {
        // TODO: Remove specifics!
        childTableDetails.data = full.jvms;
    }

    return React.createElement(ExpandCollapseControl, {id:delimitedId,
                                                       key:delimitedId,
                                                       expandIcon:expandIcon,
                                                       collapseIcon:collapseIcon,
                                                       childTableDetails:childTableDetails,
                                                       rowSubComponentContainerClassName:"row-sub-component-container",
                                                       parentItemId:full.id.id,
                                                       parentItemName:full.name,
                                                       dataTable:$("#" + tableId).dataTable(),
                                                       rootId:theRootId,
                                                       openRowLoadDataDoneCallback: openRowLoadDataDoneCallback,
                                                       collapseCallback: collapseCallback});
};

var renderArray = function(item, data) {

    var str = "";
    /* would be better with _Underscore.js : */
    for (var idx = 0; idx < data.length; idx=idx+1) {
        str = str + (str === "" ? "" : ",") + data[idx][item.displayProperty];
    }
    return str;
};