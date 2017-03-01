/**
 * JQuery plugin to apply column resizing on HTML tables.
 *
 * @author: Jedd Anthony Cuison
 *
 * usage: {@link http://jsfiddle.net/jlkwison/auw373we/21/}
 *
  * @param tableWidth width "value" of the table e.g. 50 (not "50px").
  * @param minColWidth the allowable minimum column width "value". If set to zero, the column dissappears
  *                    if the column's width is set to 0. If not specified, min column width is 3.
  */
(function($) {

    $.fn.makeColumnsResizable = function(tableWidth, minColWidth) {
        var self = this;
        return this.each(function(idx){

            var mouseDrag = false;
            var colIdx;
            var origWidths = [0, 0];
            var RESIZE_COL_CURSOR_CLASS = "resize-col-cursor";

            minColWidth = (minColWidth === undefined ? 5 : minColWidth);

            var onTdMouseMove = function(e) {
                if (mouseDrag) {
                    var newWidth = rightCellBorderPagePos - e.pageX;
                    var newWidthDiff = origWidths[1] - newWidth;

                    // make sure that the mouse pointer is still the "resizable" pointer even if the current target changes
                    // (e.g. when moving mouse very quickly and crosses the neighboring column)
                    $(e.currentTarget).addClass(RESIZE_COL_CURSOR_CLASS);

                    if (newWidth > minColWidth && (origWidths[0] + newWidthDiff) > minColWidth) {
                        $(self[idx]).find("thead th").eq(colIdx - 1).innerWidth(origWidths[0] + newWidthDiff);
                        $(self[idx]).find("thead th").eq(colIdx).innerWidth(newWidth);
                    }

                } else {
                    if ($(e.currentTarget).parent().children().index($(e.currentTarget))!==0){

                        // e.offsetX is undefined in Mozilla thus the need for the code below
                        var offsetX = e.offsetX === undefined ? e.pageX - $(e.currentTarget).offset().left : e.offsetX;

                        if ((offsetX >= 0) && (offsetX <= 3)) {
                            $(e.currentTarget).addClass(RESIZE_COL_CURSOR_CLASS);
                        } else {
                            $(e.currentTarget).removeClass(RESIZE_COL_CURSOR_CLASS);
                        }

                    } else {
                         $(e.currentTarget).removeClass(RESIZE_COL_CURSOR_CLASS);
                    }
                }
                e.preventDefault();
            }

            var onTdMouseDown = function(e) {
                if ($(e.currentTarget).hasClass(RESIZE_COL_CURSOR_CLASS) && !mouseDrag) {
                    mouseDrag = true;
                    colIdx = $(e.currentTarget).parent().children().index($(e.currentTarget));
                    rightCellBorderPagePos = $(e.currentTarget).offset().left + $(e.currentTarget).innerWidth();

                    origWidths[0] = $(e.currentTarget).prev().innerWidth();
                    origWidths[1] = $(e.currentTarget).innerWidth();

                    e.preventDefault();
                }
            }

            var onTdMouseUp = function(e) {
                $(e.currentTarget).removeClass(RESIZE_COL_CURSOR_CLASS);
                mouseDrag = false;
            }

            var onTbodyMouseLeave = function(e) {
                $(e.currentTarget).removeClass(RESIZE_COL_CURSOR_CLASS);
                mouseDrag = false;
            }

            if (tableWidth !== undefined) {
                $(self[idx]).width(tableWidth);
            }

            $(self[idx]).find("thead th").mousemove(onTdMouseMove);
            $(self[idx]).find("thead th").mousedown(onTdMouseDown);
            $(self[idx]).find("thead th").mouseup(onTdMouseUp);
            $(self[idx]).find("thead").mouseleave(onTbodyMouseLeave);

            $(self[idx]).find("tbody td").mousemove(onTdMouseMove);
            $(self[idx]).find("tbody td").mousedown(onTdMouseDown);
            $(self[idx]).find("tbody td").mouseup(onTdMouseUp);
            $(self[idx]).find("tbody").mouseleave(onTbodyMouseLeave);

            $(self[idx]).addClass("adj-col");
            $(self[idx]).find("thead th").addClass("adj-col");
            $(self[idx]).find("tbody td").addClass("adj-col");
        });
    }

} (jQuery));