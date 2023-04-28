require('base-product');
var $ = require('jquery');
var _ = require('underscore');
var moment = require('moment');
var toastr = require('toastr');
var bootbox = require('bootbox');
bootbox.setLocale('zh_CN');
require('jquery.blockUI');
require('bootstrap-table');
require('select2');
require('select2/dist/js/i18n/zh-CN');
require('eonasdan-bootstrap-datetimepicker');

$(function() {
    var $processRelatedForm = $('#process-related-form');
    var $taskRelatedTable = $('#process-related');
    $taskRelatedTable.bootstrapTable({
        responseHandler: function(result) {
            var data = result.data;
            _.each(data, function(item) {
                item.status = item.activeTask ? item.activeTask : item.taskBiz.status;
            });

            return {
                rows: data,
                total: result.pagination.rowCount
            }
        }
    });
    // date
    var $from = $processRelatedForm.find('[name="from"]');
    var $to = $processRelatedForm.find('[name="to"]');
    $from.datetimepicker({
        locale: 'zh-cn',
        showClear: true,
        format: 'YYYY-MM-DD 00:00:00'
    });
    $to.datetimepicker({
        locale: 'zh-cn',
        showClear: true,
        format: 'YYYY-MM-DD 23:59:59',
        useCurrent: false //Important! See issue #1075
    });
    $from.on("dp.change", function (e) {
        $to.data("DateTimePicker").minDate(e.date);
    });
    $to.on("dp.change", function (e) {
        $from.data("DateTimePicker").maxDate(e.date);
    });

    // select and datasource
    var $env = $processRelatedForm.find('select[name="env"]').select2({minimumResultsForSearch: Infinity});
    var $datasource = $processRelatedForm.find('select[name="datasource"]').select2({allowClear:true});
    var $process = $processRelatedForm.find('select[name="process"]').select2({minimumResultsForSearch: Infinity});
    var $finished = $processRelatedForm.find('select[name="finished"]').select2({minimumResultsForSearch: Infinity});

    $env.on('change', function() {
        var value = $env.val();
        if (value) {
            $.get(base_path + '/ds/list?env=' + value, function (data) {
                $processRelatedForm.find('select[name="datasource"] option').remove();
                $datasource.select2({
                    allowClear:true,
                    data: _.map(data, function (ds) {
                        return {
                            id: ds.id,
                            text: '[' + ds.type + ']' + ds.name
                        }
                    })
                });
                $datasource.val(null).trigger("change");
            });
        }
    });
    $env.trigger("change");

    // query
    $processRelatedForm.submit(function() {
        var env = $env.val();
        var datasource = $datasource.val();
        var process = $process.val();
        var finished = $finished.val();
        var from = $from.val();
        var to = $to.val();
        $taskRelatedTable.bootstrapTable('refreshOptions', {
            queryParams : function(params) {
                env && (params.env = env);
                datasource && (params.datasource = datasource);
                process && (params.process = process);
                finished != "" && (params.finished = finished);
                from && (params.from = from);
                to && (params.to = to);
                return params;
            }
        });
        return false;
    });
});

window.myOpFormatter = function(value, row, index) {
    return '<a class="text-primary" href="'
        + base_path + '/process/' + row.taskBiz.type + '/' + row.taskBiz.processInstanceId
        + '" title="查看详情"><i class="fa fa-search fa-fw"></i></a>';
};