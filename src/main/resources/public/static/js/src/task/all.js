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
    var $processAllForm = $('#process-all-form');
    var $taskAllTable = $('#process-all');
    $taskAllTable.bootstrapTable({
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
    var $from = $processAllForm.find('[name="from"]');
    var $to = $processAllForm.find('[name="to"]');
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
    var $env = $processAllForm.find('select[name="env"]').select2({minimumResultsForSearch: Infinity});
    var $datasource = $processAllForm.find('select[name="datasource"]').select2({allowClear:true});
    var $process = $processAllForm.find('select[name="process"]').select2({minimumResultsForSearch: Infinity});
    var $finished = $processAllForm.find('select[name="finished"]').select2({minimumResultsForSearch: Infinity});
    var $user = $processAllForm.find('select[name="user"]').select2({
        minimumInputLength: 2,
        allowClear: true,
        language: "zh-CN",
        ajax: {
            url: base_path + '/user/query',
            dataType: 'json',
            delay: 500,
            data: function (params) {
                return {
                    q: params.term
                };
            },
            processResults: function (data) {
                return {
                    results: _.map(data, function(item) {
                        return {
                            id: item.username,
                            text: item.nickname + '(' + item.username + ')'
                        }
                    })
                };
            },
            cache: true
        }
    });

    $env.on('change', function() {
        var value = $env.val();
        if (value) {
            $.get(base_path + '/ds/list?env=' + value, function (data) {
                $processAllForm.find('select[name="datasource"] option').remove();
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
    $processAllForm.submit(function() {
        var env = $env.val();
        var datasource = $datasource.val();
        var process = $process.val();
        var finished = $finished.val();
        var from = $from.val();
        var to = $to.val();
        var user = $user.val();
        $taskAllTable.bootstrapTable('refreshOptions', {
            queryParams : function(params) {
                env && (params.env = env);
                datasource && (params.datasource = datasource);
                process && (params.process = process);
                finished != "" && (params.finished = finished);
                from && (params.from = from);
                to && (params.to = to);
                user && (params.user = user);
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