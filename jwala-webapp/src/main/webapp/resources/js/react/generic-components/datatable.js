/** @jsx React.DOM */
var JwalaDataTable = React.createClass({
    getInitialState: function () {
        return { displayLength: null };
    },
    dataTable: null,
    render: function () {
        if (this.props.className !== undefined) {
            $.fn.dataTableExt.oStdClasses = { sTable: this.props.className /* This will not be set if applyThemeRoller is true */
                , sSortAsc: $.fn.dataTableExt.oStdClasses.sSortAsc,
                sSortDesc: $.fn.dataTableExt.oStdClasses.sSortDesc };
        }

        var headerComponents = [];
        var header;
        if (this.props.headerComponents !== undefined) {
            for (var i = 0; i < this.props.headerComponents.length; i++) {
                var obj = this.props.headerComponents[i];
                if (obj.jwalaType === "button") {
                    headerComponents.push(new DataTableButton({ id: this.props.tableId + "_" + obj.id,
                        itemId: "whatever",
                        label: obj.btnLabel,
                        className: "inline-block",
                        callback: obj.btnCallback ,
                        disabled: obj.disabled}));
                } else if (obj.jwalaType === "label") {
                    var labelId = null;
                    if (obj.id !== undefined) {
                        // Generate an id if obj has an id
                        labelId = this.props.tableId + "_" + obj.id;
                    }
                    headerComponents.push(React.DOM.div({ id: labelId, className: obj.className }, obj.text));
                } else if (obj.jwalaType === "space") {
                    headerComponents.push(" ");
                }
            }
            headerComponents.push(React.DOM.span({ className: "accordion-title-text-position" }, this.props.title));
            header = React.DOM.div({ className: "accordion-title nowrap text-align-right", style: this.props.title !== undefined ? {} : { display: 'none' } }, headerComponents);
        } else {
            header = this.props.title === undefined ? "" : new React.DOM.h3(null, this.props.title);
        }

        return React.DOM.div({ style: this.props.hide === true ? { display: "none" } : {}, className: this.props.divTypeContainerClassName }, header, React.DOM.table({ id: this.props.tableId }));
    },
    componentDidUpdate: function () {
        if (this.dataTable === null) {
            this.dataTable = decorateTableAsDataTable(this.props.tableId, this.props.tableDef, this.props.applyThemeRoller, true, this.props.editCallback, this.datableDrawCallback, this.props.expandIcon, this.props.collapseIcon, this.props.childTableDetails, this.props.initialSortColumn, undefined, undefined, undefined, undefined, this.props.openRowLoadDataDoneCallback, this.props.collapseRowCallback);
        }

        if (this.dataTable !== null) {
            this.dataTable.fnClearTable(this.props.data);
            this.dataTable.fnAddData(this.props.data);
            this.dataTable.fnDraw();

            if (this.props.isColResizable) {
                this.dataTable.makeColumnsResizable();
            }

            var sortColumns = [];

            /**
             * Using this.props.initialSortColumn with fnSort directly like this -> this.dataTable.fnSort(sortColumns)
             * produces the following bug:
             * this.props.initialSortColumn will get the sorted data of the last sorted table and will result to
             * the error "Cannot read property 'sSortDataType' of undefined" in IE if the last table
             * sorted column index does not exist in the current table. This can be described better with the
             * following scenario:
             *
             * User goes to JVM configuration tab then sorts the last column (8th column). The user then goes to the
             * group configuration tab which immediately displays "Cannot read property 'sSortDataType' of undefined"
             * if this.props.initialSortColumn is used directly as a parameter to fnSort. This is because
             * the datatable will try to sort the 8th column which does not exists in the group configuration table.
             */
            this.props.initialSortColumn.forEach(function (col) {
                sortColumns.push(col);
            });

            this.dataTable.fnSort(sortColumns);
        }

        if (this.props.applyThemeRoller !== false && this.props.className !== "") {
            $("#" + this.props.tableId).attr("class", this.props.className);
        }
    },
    datableDrawCallback: function () {

        var self = this;
        var dataTable = this.dataTable;

        if (dataTable) {
            if (this.state.displayLength && this.state.displayLength !== dataTable.fnSettings()._iDisplayLength && this.props.isColResizable) {

                // Make new rows resizable...
                dataTable.makeColumnsResizable();
            } else {
                this.state.displayLength = dataTable.fnSettings()._iDisplayLength;
            }
        }

        if (this.props.selectItemCallback === undefined) {
            return;
        }

        $(dataTable).find("thead > tr, tbody > tr, > tr").off("click").on("click", function (e) {
            if ($(this).hasClass("row_selected")) {
                $(this).removeClass("row_selected");
            } else {
                $(dataTable).find("thead > tr, tbody > tr, > tr").removeClass("row_selected");
                $(this).addClass("row_selected");

                var cell = dataTable.find("tbody > tr.row_selected")[0];
                if (cell !== undefined) {
                    var i = dataTable.fnGetPosition(cell);
                    var item = dataTable.fnGetData(i);
                    self.props.selectItemCallback(item);
                }
            }
        });
    },
    getDefaultProps: function () {
        return {
            initialSortColumn: []
        };
    },
    deselectAllRows: function() {
        var rows = $(this.getDOMNode()).find("tr");
        if (rows.length > 0) {
            rows.removeClass("row_selected");
        }
    },
    selectRow: function(value) {
        var td = $(this.getDOMNode()).find("td").filter(function() {
            return $(this).text() == value;
        });

        if (td.length > 0) {
            td.parent().addClass("row_selected");
        }
    },
    getVisibleSelectedRowCount: function() {
        return $(this.getDOMNode()).find(".row_selected").length;
    }
});