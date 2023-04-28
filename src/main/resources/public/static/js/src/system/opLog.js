require('base-product');
var $ = require('jquery');
var _ = require('underscore');
var moment = require('moment');
var bootbox = require('bootbox');
bootbox.setLocale('zh_CN');
require('jquery.blockUI');
require('bootstrap-table');
require('select2');
require('select2/dist/js/i18n/zh-CN');
require('eonasdan-bootstrap-datetimepicker');

$(function() {
    var $logForm = $('#log-form');
    var $logTable = $('#log-table');

    $logTable.bootstrapTable({
        responseHandler: function(result) {
            return {
                rows: result.data,
                total: result.pagination.rowCount
            }
        }
    });

    // date
    var $from = $logForm.find('[name="from"]');
    var $to = $logForm.find('[name="to"]');
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
    var $env = $logForm.find('select[name="env"]').select2({minimumResultsForSearch: Infinity});
    var $sid = $logForm.find('select[name="sid"]').select2({allowClear:true});

    $env.on('change', function() {
        var value = $env.val();
        if (value) {
            $.get(base_path + '/ds/list?env=' + value, function (data) {
                $logForm.find('select[name="sid"] option').remove();
                $sid.select2({
                    allowClear:true,
                    data: _.map(data, function (ds) {
                        return {
                            id: ds.sid,
                            text: '[' + ds.type + ']' + ds.name
                        }
                    })
                });
                $sid.val(null).trigger("change");
            });
        }
    });
    $env.trigger("change");

    var $user = $logForm.find('select[name="user"]').select2({
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

    // query
    $logForm.submit(function() {
        var env = $env.val();
        var sid = $sid.val();
        var from = $from.val();
        var to = $to.val();
        var user = $user.val();
        $logTable.bootstrapTable('refreshOptions', {
            queryParams : function(params) {
                env && (params.env = env);
                sid && (params.sid = sid);
                from && (params.from = from);
                to && (params.to = to);
                user && (params.user = user);
                return params;
            }
        });
        return false;
    });
});
