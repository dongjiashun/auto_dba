require('jquery');
require('bootstrap-table/dist/bootstrap-table.min');
require('bootstrap-table/dist/locale/bootstrap-table-zh-CN.min');

(function ($) {
    'use strict';

    $.fn.bootstrapTable.locales['zh-CN'].formatDetailPagination = function (totalRows) {
        return '显示 ' + totalRows + ' 条记录';
    };

    $.extend($.fn.bootstrapTable.defaults, $.fn.bootstrapTable.locales['zh-CN']);

})(jQuery);