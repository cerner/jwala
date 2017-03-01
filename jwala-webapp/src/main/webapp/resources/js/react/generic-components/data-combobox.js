var DataCombobox = React.createClass({
    getKey: function(idx) {
      if(this.props.data !== undefined &&
         this.props.data[idx] !== undefined) {
          try {
            return eval("this.props.data[idx]." + this.props.dataField);
          } catch(x) {
            if(console !== undefined) { 
              console.log('Exception in getKey' + x);
            }
            return "(null key)";
          }
        }
    },
    getValue: function(idx) {
      if(this.props.data !== undefined &&
         this.props.data[idx] !== undefined) {
          try {
            return eval("this.props.data[idx]." + this.props.val);
          } catch(x) {
            if(console !== undefined) { 
              console.log('Exception in getValue ' + x);
            }
            return "(null value)";
          }
        }
    },
    render: function() {
        var options = [];
        if(this.props.data !== undefined) {
          this.props.data.forEach(function(dval, idx) {  
              var props = {value:this.getKey(idx), key:""+idx};
              if (this.props.selectedVal !== undefined &&
                  this.props.selectedVal === props.value) {
                      props["selected"] = "selected";
              }
              options.push(React.DOM.option(props, this.getValue(idx)));
            }.bind(this));
        }
        return React.DOM.select({
          onChange:this.props.onchange,
          name:this.props.name
         }, options);
    }
})