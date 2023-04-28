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

// template
var tableTemplate = require("dot!./struct/table.dot");
var viewTemplate = require("dot!./struct/view.dot");

/** datasource table **/
var $dsTable = null;
var $dsEnv, $dsName, $dsType, $dsList;
$(function() {
    var $datasources = $('#datasources');
    $dsTable = $datasources.bootstrapTable({
        responseHandler: function(data) {
            return _.each(data, function(auth) {
                var roleString = '<code>default</code>';
                if (auth.roles && auth.roles.length) {
                    roleString = '';
                    _.each(auth.roles, function (role) {
                        roleString += '<code>' + role.name + '</code>&nbsp;&nbsp;';
                    });
                }
                auth.role = roleString;
            });
        }
    });

    $('#toolbar').find('[name="env"]').change(changeEnv);
    $datasources.on('click', '[name="delete"]', function() {
        var id = $(this).parents('tr[data-uniqueid]').data('uniqueid');
        var row = $dsTable.bootstrapTable('getRowByUniqueId', id);
        if (row) {
            bootbox.confirm("确定要删除数据源授权吗?(删除后, 使用需要重新申请授权)", function (result) {
                if (result) {
                    $.blockUI({message: ""});
                    $.ajax({
                        url: base_path + '/ds/' + row.id + '/auth/del/' + row.authId,
                        success: function () {
                            $dsTable.bootstrapTable('refresh');
                        },
                        complete: function () {
                            $.unblockUI();
                        }
                    });
                }
            });
        }
    });

    // select
    $dsEnv = $('select[name="ds-env"]').select2({minimumResultsForSearch: Infinity});
    $dsName = $('select[name="ds-name"]').select2();
    $dsType = $('select[name="ds-type"]').select2({minimumResultsForSearch: Infinity});
    $dsList = $('select[name="ds-list"]').select2();

    $dsEnv.on('change', dsChangeEnv);
    $dsName.on('change', dsChangeName);
    $dsType.on('change', dsChangeType);
    $dsList.on('change', dsChangeList);
    $dsEnv.trigger("change");
});

/** change env **/
function changeEnv() {
    var env = $(this).val();
    if ($dsTable) {
        $dsTable.bootstrapTable('refresh', {
            url: base_path + '/ds/list?env=' + env
        });
    }
}
function dsChangeEnv() {
    var value = $dsEnv.val();
    if (value) {
        $.get(base_path + '/ds/list?env=' + value, function (data) {
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
    $dsType.val('').trigger("change");
}
function dsChangeType() {
    var value = $dsType.val();
    var dsName = $dsName.val();
    if (value && dsName) {
        $.get(base_path + '/ds/' + encodeURIComponent(dsName) + '/' + encodeURIComponent(value), function (data) {
            $('select[name="ds-list"] option').remove();
            $dsList.select2({
                data: _.map(data, function(name) {
                    return {
                        id: name,
                        text: name
                    }
                })
            });
            $dsList.val(null).trigger("change");
        });
    } else {
        $('select[name="ds-list"] option').remove();
        $dsList.val(null).trigger("change");
    }
}
function dsChangeList() {
    var value = $dsList.val();
    var dsName = $dsName.val();
    if (value && dsName) {
        $.blockUI({message: ""});
        var type = $dsType.val();
        $.ajax({
            url: base_path + '/ds/' + encodeURIComponent(dsName) + '/' + encodeURIComponent(type) + '/' + encodeURIComponent(value),
            success: function (result) {
                if(result.code == 0) {
                    var $struct = $('.struct');
                    if (type == 'table') {
                        $struct.html(tableTemplate(result.data));
                    } else if(type == 'view') {
                        $struct.html(viewTemplate(result.data));
                    } else {
                        $struct.html('');
                    }
                }
            },
            complete: function () {
                $.unblockUI();
            }
        });
    }
}

// table formatter
window.opFormatter = function(value, row, index) {
    var op = [
        '<a name="delete" class="text-danger" href="javascript:;" title="删除数据源授权"><i class="fa fa-trash-o fa-fw"></i></a>'
    ];
    if (row.roles && _.any(row.roles, function(role) { return role.code == 'owner' })) {
        op.push('<a name="auth" class="text-warning" href="' + base_path + '/ds/' + row.id + '/auth" title="查看、管理授权"><i class="fa fa-search fa-fw"></i></a>');
    }
    return op.join(' ');
};