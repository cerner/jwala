/**
 * Component that displays a list of error messages in a table.
 * It has a pull down feature (specified as a pullDown property)
 * which can contain detailed description of listed error messages.
 */
var ErrorMsgList = React.createClass({
    statics: {
        getPropertyCount: function(o) {
            var count = 0;
            for (var prop in o) {
                count++;
            }
            return count;
        },
        KEY_PREFIX: "key",
        EVEN: "even",
        ODD: "odd"
    },
    getInitialState: function() {
        return {
            pullDownVisible: {}
        }
    },
    render: function() {
        var rows = [];
        var rowIdx = 1;
        var propertyCount;

        for (var i = (this.props.msgList.length - 1);i >=0 ;i--) {
            var row = this.props.msgList[i];
            var pullDownMsg = null;
            cols = [];

            for (var col in row) {
                if (col !== "pullDown") {
                    var className = ((col === "dateTime") ? "nowrap" : "");
                    var style = ((col === "dateTime") ? {width:"115px"} : {});
                    cols.push(React.DOM.td({className:className, style:style}, row[col]));
                } else {
                   var theKey = ErrorMsgList.KEY_PREFIX + i;
                   var pullDownIconClassName = this.state.pullDownVisible[ErrorMsgList.KEY_PREFIX + i] === true ? "ui-icon-triangle-1-n" : "ui-icon-triangle-1-s";
                   cols.push(React.DOM.td({className:"text-align-center", style:{width:"16px"} /* Easiest way to center the pull down icon. */},
                                           React.DOM.span({className:"ui-icon cursorPointer " + pullDownIconClassName,
                                                          onClick:this.clickPullDown.bind(this, theKey)}, "")));

                        if (propertyCount === undefined) {
                            propertyCount = ErrorMsgList.getPropertyCount(row);
                        }
                        pullDownMsg = React.DOM.td({colSpan:propertyCount}, row[col])

                }
            }
            var trClassName = (rowIdx++ % 2 === 0) ? ErrorMsgList.EVEN : ErrorMsgList.ODD;
            rows.push(React.DOM.tr({className:trClassName} , cols));

            var rowStyle = this.state.pullDownVisible[ErrorMsgList.KEY_PREFIX + i] === true ? {} : {display:"none"};
            rows.push(React.DOM.tr({style:rowStyle}, pullDownMsg));
        }

        return React.DOM.table({className:"errMsgTable"}, rows);
    },
    componentDidMount: function() {
        // Open the latest error message details by default.
        this.state.pullDownVisible[ErrorMsgList.KEY_PREFIX + (this.props.msgList.length - 1)] = true;
        this.forceUpdate();
    },
    clickPullDown: function(key) {
        if (this.state.pullDownVisible[key]) {
            this.state.pullDownVisible[key] = false;
        } else {
            this.state.pullDownVisible[key] = true;
        }
        this.setState({pullDownVisible:this.state.pullDownVisible});
    }
})