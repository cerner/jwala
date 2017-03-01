/**
 * Movable splitter divided container component.
 *
 * Import Note!!! Components that will be contained by the splitter should be wrapped with a div with position
 *                set to absolute so that component hiding works.
 *
 * Properties:
 *
 * 1. orientation - Dictates how the child components are arranged. Possible values VERTICAL_ORIENTATION or
 *                  HORIZONTAL_ORIENTATION or any which defaults to horizontal.
 * 2. components - components held by the splitter component.
 *
 * TODO: Update js docs
 */
var RSplitter = React.createClass({
    getInitialState: function() {
        return {
            mouseOnSplitter: false,
            grabSplitter: false,
            splitterRef: null,
            splitterIdx: -1,
            mousePos: -1
        }
    },
    panelRefs: null,
    render: function() {
        var divs = [];
        var self = this;
        var i = 0;
        var orientationClassName;
        var dividerClassName;

        if (this.props.orientation === RSplitter.VERTICAL_ORIENTATION) {
            orientationClassName = "vert";
            dividerClassName = "horz-divider";
        } else {
            orientationClassName = "horz";
            dividerClassName = "vert-divider";
        }

        this.panelRefs = [];
        this.props.components.forEach(function(item) {
            ++i;
            var key = RSplitter.getKey(i);

            var cursor = "auto";
            if (self.state.mouseOnSplitter || self.state.grabSplitter) {
                if (self.state.splitterIdx === (i - 1)) {
                    cursor = ((self.props.orientation === RSplitter.VERTICAL_ORIENTATION) ? "row-resize" : "col-resize");
                }
            }

            var childContainerSize = (100 / self.props.components.length) + "%";
            var width;
            var height;
            if (self.props.panelDimensions === undefined) {
                width = self.props.orientation === RSplitter.VERTICAL_ORIENTATION ? "100%" : childContainerSize;
                height = self.props.orientation === RSplitter.VERTICAL_ORIENTATION ? childContainerSize : "100%";
            } else {
                width = self.props.panelDimensions[i - 1].width;
                height = self.props.panelDimensions[i - 1].height;
            }

            self.panelRefs.push(key);
            divs.push(React.createElement(RPanel, {key: key,
                                                   ref: key,
                                                   className:(i > 1 ? dividerClassName : "") + " rsplitter childContainer " + orientationClassName,
                                                   style:{cursor: cursor},
                                                   width: width,
                                                   height: height,
                                                   mouseMoveHandler: self.mouseMoveHandler.bind(self, key, i - 1),
                                                   mouseDownHandler: self.mouseDownHandler.bind(self, key, i - 1),
                                                   mouseUpHandler: self.mouseUpHandler}, item));

        });

        return React.createElement("div", {ref: "mainContainer", className:"rsplitter container " + orientationClassName}, divs);
    },

    componentDidUpdate: function(prevProps, prevState) {
        if (this.state.grabSplitter && this.props.updateCallback !== undefined) {
            this.props.updateCallback();
        }
    },

    mouseMoveHandler: function(ref, idx, e) {
        var pagePos = this.props.orientation === RSplitter.VERTICAL_ORIENTATION ? e.pageY : e.pageX;
        if (pagePos !== this.state.mousePos && this.state.grabSplitter) {
            if (this.props.orientation === RSplitter.VERTICAL_ORIENTATION) {
                var topDivHeight =
                        $(this.refs[RSplitter.getKey(this.state.splitterIdx)].getDOMNode()).height();
                var currentDivHeight =
                        $(this.refs[RSplitter.getKey(this.state.splitterIdx + 1)].getDOMNode()).height();
                var dif = pagePos - this.state.mousePos;

                var topDivHeight = topDivHeight + dif;
                var bottomDivHeight = currentDivHeight - dif;

                // Prevent the 2 concerned panels height to affect other divs beside them
                if (topDivHeight > 0 && bottomDivHeight > 0) {
                    this.refs[this.panelRefs[this.state.splitterIdx - 1]].setHeight(topDivHeight);
                    this.refs[this.panelRefs[this.state.splitterIdx]].setHeight(bottomDivHeight);
                    if (this.props.onSplitterChange) {
                        this.props.onSplitterChange([{height: topDivHeight}, {height: bottomDivHeight}]);
                    }
                }

            } else {
                var leftDivWidth =
                        $(this.refs[RSplitter.getKey(this.state.splitterIdx)].getDOMNode()).width();
                var currentDivWidth =
                        $(this.refs[RSplitter.getKey(this.state.splitterIdx + 1)].getDOMNode()).width();
                var dif = pagePos - this.state.mousePos;
                this.refs[this.panelRefs[this.state.splitterIdx - 1]].setWidth(leftDivWidth + dif);
                this.refs[this.panelRefs[this.state.splitterIdx]].setWidth(currentDivWidth - dif);
                if (this.props.onSplitterChange) {
                    var dimensions = {};
                    dimensions[this.state.splitterIdx - 1] = {width: leftDivWidth + dif};
                    dimensions[this.state.splitterIdx] = {width: currentDivWidth - dif};
                    this.props.onSplitterChange(dimensions);
                }
            }
            this.setState({mousePos: pagePos});
            e.preventDefault();
        } else if (idx > 0) {
            var divO = $(this.refs[ref].getDOMNode());

            var relPos = pagePos - (this.props.orientation === RSplitter.VERTICAL_ORIENTATION ? divO.offset().top : divO.offset().left);

            if (relPos < RSplitter.SPLITTER_DRAG_AREA_SIZE) {
                this.setState({mousePos: pagePos, splitterIdx: idx, mouseOnSplitter: true});
            } else {
                this.setState({mouseOnSplitter: false});
                return;
            }
            e.preventDefault();
        }
    },

    mouseDownHandler: function(ref, idx, e) {
        if (idx > 0 && this.state.mouseOnSplitter && !this.state.grabSplitter) {
            this.setState({splitterRef: ref, splitterIdx: idx, grabSplitter: true});
            e.preventDefault();
        }
    },

    mouseUpHandler: function(e) {
        if (this.state.grabSplitter) {
            this.setState({grabSplitter: false});
            e.preventDefault();
        }
    },

    statics: {
        SPLITTER_DRAG_AREA_SIZE: 5,
        CHILD_CONTAINER_PREFIX: "cc",
        VERTICAL_ORIENTATION: "vert",
        HORIZONTAL_ORIENTATION: "horz",
        UID: "uid-" + Date.now(),
        getKey: function(someOtherRefCode) {
            return RSplitter.UID + "-" + someOtherRefCode;
        }
    }

});

/**
 * A panel component.
 */
var RPanel = React.createClass({
    getInitialState: function() {
        return {width: this.props.width, height: this.props.height};
    },
    render: function() {
        var i = this.props.panelIdx;

        var computedStyle = this.props.style;
        if (this.state.width !== undefined || this.state.height !== undefined) {
            computedStyle["width"] = this.state.width;
            computedStyle["height"] = this.state.height;
        }

        return React.createElement("div", {className: this.props.className, style: computedStyle,
                   onMouseMove: this.props.mouseMoveHandler, onMouseDown: this.props.mouseDownHandler,
                   onMouseUp: this.props.mouseUpHandler}, this.props.children);

    },
    setWidth: function(width) {
        this.setState({width: width});
    },
    setHeight: function(height) {
        this.setState({height: height});
    }
});