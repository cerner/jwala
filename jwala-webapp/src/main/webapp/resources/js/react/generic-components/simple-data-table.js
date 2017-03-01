/**
 * A simple data table intended to be used as a child table.
 */
var SimpleDataTable = React.createClass({
    render: function() {
        var headerRow;
        var colHeaderArray = [];
        var rowArray = [];
        var self = this;

        if (this.props.colHeaders !== undefined) {
            $.each(this.props.colHeaders, function(i, obj) {
                colHeaderArray.push(React.DOM.th(null, obj));
            });
            headerRow = React.DOM.tr(null, colHeaderArray);
        }

        $.each(this.props.data, function(i, obj) {
            var colArray = [];
            for (var idx = 0; idx < self.props.displayColumns.length; idx=idx+1) {
                colArray.push(React.DOM.td(null, obj[self.props.displayColumns[idx]]));
            }
            rowArray.push(React.DOM.tr(null, colArray));
        });
        return React.DOM.table({className:this.props.className}, headerRow, rowArray);
    }
})