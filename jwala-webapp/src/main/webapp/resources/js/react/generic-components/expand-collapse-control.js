/** @jsx React.DOM */
var ExpandCollapseControl = React.createClass({
    dataTableRenderParams: {subDataTable: null, data: null, defaultSorting: null, isCollapsible: null, headerComponents: null, selectItemCallback: null},
    initDataTableRenderParams: function() {
        this.dataTableRenderParams.subDataTable = [];
        this.dataTableRenderParams.data = [];
        this.dataTableRenderParams.defaultSorting = [];
        this.dataTableRenderParams.isCollapsible = [];
        this.dataTableRenderParams.headerComponents = [];
        this.dataTableRenderParams.selectItemCallback = [];
    },
    loadingIndicatorClassName: null,
    renderTableCountDown: 0,
    render: function() {
        var dataSources = [];
        var childTableDetailsArray = [];

        if(Object.prototype.toString.call(this.props.childTableDetails) === "[object Array]") {
            for (var i = 0; i < this.props.childTableDetails.length; i++) {
                dataSources[i] = this.props.childTableDetails[i].dataCallback === undefined ?
                                    {jsonData:this.props.childTableDetails[i].data} :
                                    {dataCallback:this.props.childTableDetails[i].dataCallback};
                childTableDetailsArray[i] = this.props.childTableDetails[i];
            }
        } else {
                dataSources[0] = this.props.childTableDetails.dataCallback === undefined ?
                                    {jsonData:this.props.childTableDetails.data} :
                                    {dataCallback:this.props.childTableDetails.dataCallback};
                childTableDetailsArray[0] = this.props.childTableDetails;
        }

        return <span id={this.props.id} className="ui-icon ui-icon-triangle-1-e"
                     onClick={this.onClick.bind(this, dataSources, childTableDetailsArray)}/>
    },
    decorateTable: function(childTableDetails) {
        return decorateTableAsDataTable(childTableDetails.tableIdPrefix + this.props.id,
                                        childTableDetails.tableDef,
                                        false,
                                        false,
                                        null,
                                        null,
                                        this.props.expandIcon,
                                        this.props.collapseIcon,
                                        childTableDetails.childTableDetails,
                                        this.props.parentItemId,
                                        this.props.rootId,
                                        childTableDetails.initialSortColumn,
                                        this.props.parentItemName,
                                        false);
    },
    drawDataTable: function(dataTable, data, defaultSorting, isCollapsible, headerComponents) {
        dataTable.fnClearTable(data);
        dataTable.fnAddData(data);
        dataTable.fnDraw();

        // indiscriminately show the table.
        // I didn't feel the need to check if the Datatable is hidden to avoid additional logic
        // without significant performance gains.
        // The table's parent's parent is the div that holds the header and the table container.
        dataTable.parent().parent().show();

        // If className is defined, add it to the row node's class
        var data = dataTable.fnGetData();
        var nodes = dataTable.fnGetNodes();
        for(var i = 0;i < data.length;i++) {
            if (data[i].className !== undefined) {
                $(nodes[i]).closest("tr").addClass(data[i].className);
            }
        }

        if (defaultSorting !== undefined) {
            dataTable.fnSort([[defaultSorting.col, defaultSorting.sort]]);
        }

        if (isCollapsible) {
            // Make the table collapsible by applying an accordion ui
            // on its top level container (the parent of the parent)
            if (!dataTable.parent().parent().hasClass("ui-accordion")) {
                dataTable.parent().parent().accordion({
                    active: jwalaVars["opsGrpChildrenViewOpen"] === "true" ? 0 : false /* active = true will not open the accordion by default, it has to be 0! */,
                    header: headerComponents === undefined ? "h3" : "div.accordion-title",
                    collapsible: true,
                    heightStyle: "content",
                    beforeActivate:function(event, ui){
                        var fromIcon = $(event.originalEvent.target).is('.ui-accordion-header > .ui-icon');
                        return fromIcon;
                    }
                });

                var self = this;
                if (headerComponents) {
                    // Accordion button specific codes
                    // attach handlers to the buttons
                    for (var i = 0; i < headerComponents.length; i++) {

                        var component = headerComponents[i];

                        if (component.jwalaType === "button") {
                            var buttonSelector = dataTable.selector + "_" + component.id;

                            var label = $(buttonSelector).find("span").html();
                            var onClickCallback = function(theComponent, event) {
                                // Popup status (that fades out)
                                if (theComponent.onClickMessage !== undefined && $("#tooltip" + event.data.id).length === 0) {
                                    var top = $(event.data.buttonSelector).parent().position().top - $(event.data.buttonSelector).height()/2;
                                    var left = $(event.data.buttonSelector).parent().position().left + $(event.data.buttonSelector).width()/2;
                                    $(event.data.buttonSelector).parent().append("<div id='tooltip" + theComponent.id +
                                        "' role='tooltip' class='ui-tooltip ui-widget ui-corner-all ui-widget-content' " +
                                        "style='top:" + top + "px;left:" + left + "px'>" + theComponent.onClickMessage + "</div>");
                                    $("#tooltip" + theComponent.id).fadeOut(3000, function() {
                                        $("#tooltip" + theComponent.id).remove();
                                    });
                                }
                                theComponent.btnCallback(event);
                            }.bind(self, component);

                            $(buttonSelector).find("span").remove();
                            $(buttonSelector).html(label);
                            $(buttonSelector).attr("title", component.sTitle);
                            $(buttonSelector).button();

                            if (component.disabled) {
                                $(buttonSelector).removeClass("ui-state-default");
                                $(buttonSelector).addClass("ui-state-disabled");
                            } else {
                                $(buttonSelector).click({id:self.props.parentItemId, name:self.props.parentItemName, buttonSelector: buttonSelector}, onClickCallback);
                            }

                            $(buttonSelector).find("span").attr("class", component.customSpanClassName);
                            $(buttonSelector).addClass(headerComponents[i].buttonClassName);
                        }

                    }
                }

            }
        }
    },
    onClick: function(dataSources, childTableDetailsArray) {
        var self = this;
        var dataTable = this.props.dataTable;

        // We need the <tr> node for DataTable to insert the child table
        var nTr = $("#" + this.props.id).parent().parent().get(0);

        if (!dataTable.fnIsOpen(nTr)) {
            var rowNode = dataTable.fnOpen(nTr,
                                           this.fnFormatDetails(),
                                           this.props.rowSubComponentContainerClassName);

            $(rowNode).addClass($(nTr).attr("class"));
            this.loadingIndicatorClassName = this.props.id + "-loading-indicator";
            $(rowNode.children[0]).append("<div class='" + this.loadingIndicatorClassName  + " expand-collapse-loading-indicator-container'><div class='expand-collapse-loading-indicator-img'><span class='expand-collapse-loading-indicator-text'>LOADING...</span></div>");

            this.initDataTableRenderParams();
            this.renderTableCountDown = dataSources.length;
            for (var i = 0; i < dataSources.length; i++) {
                var subDataTable = this.decorateTable(childTableDetailsArray[i]);
                if (childTableDetailsArray[i].isColResizable) {
                    subDataTable.makeColumnsResizable();
                }

                var data = dataSources[i].jsonData;
                if (data !== undefined && data !== null) {

                    data.forEach(function(o) {
                        o["parentItemId"] = self.props.parentItemId;
                    });

                    this.dataTableRenderParams.subDataTable.push(subDataTable);
                    this.dataTableRenderParams.data.push(data);
                    this.dataTableRenderParams.defaultSorting.push(childTableDetailsArray[i].defaultSorting);
                    this.dataTableRenderParams.isCollapsible.push(childTableDetailsArray[i].isCollapsible);
                    this.dataTableRenderParams.headerComponents.push(childTableDetailsArray[i].headerComponents);

                    this.dataTableRenderParams.selectItemCallback.push(childTableDetailsArray[i].selectItemCallback);

                    this.renderTables();

                } else {
                    if (dataSources[i].dataCallback !== undefined) {
                            dataSources[i].dataCallback({rootId: this.props.rootId, parentId: this.props.parentItemId},
                                                        self.retrieveDataAndRenderTableCallback(subDataTable,
                                                        childTableDetailsArray[i].defaultSorting,
                                                        childTableDetailsArray[i].isCollapsible,
                                                        childTableDetailsArray[i].headerComponents,
                                                        childTableDetailsArray[i].selectItemCallback));
                    }

                }
            }

            $("#" + this.props.id).removeClass("ui-icon-triangle-1-e");
            $("#" + this.props.id).addClass("ui-icon-triangle-1-s");
        } else {
            if ($.isFunction(this.props.collapseCallback)) {
                this.props.collapseCallback(this.props.parentItemId);
            }

            $("#" + this.props.id).removeClass("ui-icon-triangle-1-s");
            $("#" + this.props.id).addClass("ui-icon-triangle-1-e");
            dataTable.fnClose(nTr);
        }

    },
    fnFormatDetails: function() {
        // An extra element which serves the purpose of mounting components "externally" via jQuery or React.render.
        // This is required to mount the command status window without refactoring (REACTifying) the group operations page.
        var externalComponentContainer = "<div id='ext-comp-div-"+ this.props.id  +  "'></div>";
        if(Object.prototype.toString.call(this.props.childTableDetails) === "[object Array]") {
            var jwalaDataTables = [];
            for (var i = 0; i < this.props.childTableDetails.length; i++) {
                jwalaDataTables[i] = new JwalaDataTable({tableId:this.props.childTableDetails[i].tableIdPrefix + this.props.id,
                                                     tableDef:this.props.childTableDetails[i].tableDef,
                                                     className:this.props.childTableDetails[i].className,
                                                     title:this.props.childTableDetails[i].title,
                                                     headerComponents: this.props.childTableDetails[i].headerComponents,
                                                     hide:true // hide (show later) to prevent from screwing up the display when data is not yet available because of the header components
                                                    });
            }
            return externalComponentContainer + React.renderComponentToStaticMarkup(new React.DOM.div("", jwalaDataTables));
        } else {
            return externalComponentContainer + React.renderComponentToStaticMarkup(<JwalaDataTable tableId={this.props.childTableDetails.tableIdPrefix + this.props.id}
                                                                     tableDef={this.props.childTableDetails.tableDef}
                                                                     className={this.props.childTableDetails.className}
                                                                     title={this.props.childTableDetails.title}
                                                                     divTypeContainerClassName={this.props.childTableDetails.divTypeContainerClassName}/>)
        }

    },
    retrieveDataAndRenderTableCallback: function(subDataTable, defaultSorting, isCollapsible, headerComponents, selectItemCallback) {
        var self = this;
        return function(resp) {
            var data = resp.applicationResponseContent;
            if (data !== undefined) {

                self.dataTableRenderParams.subDataTable.push(subDataTable);
                self.dataTableRenderParams.data.push(data);
                self.dataTableRenderParams.defaultSorting.push(defaultSorting);
                self.dataTableRenderParams.isCollapsible.push(isCollapsible);
                self.dataTableRenderParams.headerComponents.push(headerComponents);
                self.dataTableRenderParams.selectItemCallback.push(selectItemCallback);

                self.renderTables();
            }
        }
    },
    renderTables: function() {
        this.renderTableCountDown--;
        if (this.renderTableCountDown !== 0) {
            return;
        }

        var self = this;
        var i = 0;

        try {
            // Note: Future refactoring will use entity names (or client side IDs) instead of database IDs since db ids
            //       shouldn't be exposed to the client. This is the reason why the parentItemName is also passed since
            //       names are used by newer REST services.
            this.props.openRowLoadDataDoneCallback(this.props.parentItemId, this.props.parentItemName);

            var parentItemName = this.props.parentItemName;

            this.dataTableRenderParams.subDataTable.forEach(function(subDataTable) {

                self.drawDataTable(subDataTable,
                                   self.dataTableRenderParams.data[i],
                                   self.dataTableRenderParams.defaultSorting[i],
                                   self.dataTableRenderParams.isCollapsible[i],
                                   self.dataTableRenderParams.headerComponents[i]);

                var selectItemCallback = self.dataTableRenderParams.selectItemCallback[i];
                if (selectItemCallback !== undefined) {
                       $(subDataTable).find("tbody > tr").click(function(e) {

                          // TODO: Attach the event specifically to TD element only.
                          if(event.target.nodeName !== 'TD') {
                            return;
                          }

                           var isActive = false;
                           var cell;

                           if ($(this).hasClass("row_selected") ) {
                               cell = subDataTable.find("tbody > tr.row_selected")[0];
                               $(this).removeClass("row_selected");
                           } else {
                               $(subDataTable).find("tbody > tr").removeClass("row_selected");
                               $(this).addClass("row_selected");
                               isActive = true;
                               cell = subDataTable.find("tbody > tr.row_selected")[0];
                           }


                           if (cell !== undefined) {
                               var i = subDataTable.fnGetPosition(cell);
                               selectItemCallback(parentItemName, subDataTable.fnGetData(i), isActive);
                           }

                           return false;
                       });
                }


                i++;
            })
        } finally {
            $("." + this.loadingIndicatorClassName).remove();
        }

    }
});