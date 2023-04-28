require('base-product');
var $ = require('jquery');
var _ = require('underscore');
var moment = require('moment');
var toastr = require('toastr');
require('jquery.blockUI');
require('bootstrap-table');
require('select2');
require('select2/dist/js/i18n/zh-CN');

// template
var tableDataTemplate = require("dot!./table_data.dot");

var $dsEnv, $dsName, $dsList;
var $tableStruct;
$(function() {
    $dsEnv = $('select[name="ds-env"]').select2({minimumResultsForSearch: Infinity});
    $dsName = $('select[name="ds-name"]').select2();
    $dsList = $('select[name="ds-list"]').select2();

    $dsEnv.on('change', dsChangeEnv);
    $dsName.on('change', dsChangeName);
    $dsList.on('change', dsChangeList);
    $dsEnv.trigger("change");

    $('#struct').on('click', '#data-config-btn', function() {
        if ($tableStruct) {
            var columns = $tableStruct.bootstrapTable('getSelections');
            columns = _.map(columns, function(item) {
                return item.COLUMN_NAME;
            });
            $.blockUI({message: ""});
            $.ajax({
                url: base_path + '/security/data',
                data: {
                    datasource: $dsName.val(),
                    table: $dsList.val(),
                    columns: columns
                },
                success: function (result) {
                    toastr.success('敏感数据配置成功');
                },
                complete: function () {
                    $.unblockUI();
                }
            });
        } else {
            toastr.warning('没有需要操作的数据');
        }
    });
});

function dsChangeEnv() {
    var value = $dsEnv.val();
    if (value) {
        $.get(base_path + '/datasource/list2?env=' + value, function (data) {
            $('select[name="ds-name"] option').remove();
            $dsName.select2({
                data: _.map(data, function(ds) {
                    return {
                        id: ds.id,
                        text: '[' + ds.type + ']' + ds.name
                    }
                })
            });
            $dsName.val(null).trigger("change");
        });
    }
}

function dsChangeName() {
    var value = $dsName.val();
    if (value) {
        $.get(base_path + '/datasource/' + value + '/table', function (data) {
            $('select[name="ds-list"] option').remove();
            $dsList.select2({
                data: _.map(data, function (name) {
                    return {
                        id: name,
                        text: name
                    }
                })
            });
            $dsList.val(null).trigger("change");
        });
    } else {
        $dsList.val(null).trigger("change");
    }
}

function dsChangeList() {
    var value = $dsList.val();
    var $struct = $('#struct');
    if (value) {
        $.blockUI({message: ""});
        $.ajax({
            url: base_path + '/security/ds/' + $dsName.val() + '/' + encodeURIComponent(value),
            success: function (result) {
                if (result.code == 0) {
                    $struct.html(tableDataTemplate());
                    $tableStruct = $struct.find('table').bootstrapTable({
                        data: result.data
                    });
                }
            },
            complete: function () {
                $.unblockUI();
            }
        });
    } else {
        $struct.html('');
    }
}