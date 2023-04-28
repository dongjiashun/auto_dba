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
var tableDataTemplate = require("dot!./table_auth_data.dot");

var $dsEnv, $dsName, $dsList, $users;
var $tableStruct;
$(function() {
    $dsEnv = $('select[name="ds-env"]').select2({minimumResultsForSearch: Infinity});
    $dsName = $('select[name="ds-name"]').select2();
    $dsList = $('select[name="ds-list"]').select2();
    $users = $('select[name="users"]').select2();

    $dsEnv.on('change', dsChangeEnv);
    $dsName.on('change', dsChangeName);
    $dsList.on('change', dsChangeList);
    $dsEnv.trigger("change");

    $('#data-auth-btn').click(function() {
        if ($tableStruct) {
            var columns = $tableStruct.bootstrapTable('getSelections');

            var formData = {
                data: _.map(columns, function (item) {
                    return item.sec_id;
                }),
                users: $users.val()
            };

            if (!formData.data || formData.data.length == 0
                || !formData.users || formData.users.length == 0) {
                toastr.warning('请选择授权列和授权用户');
            } else {
                $.blockUI({message: ""});
                $.ajax({
                    url: base_path + '/security/data-auth',
                    data: formData,
                    success: function (result) {
                        bootbox.alert('敏感数据授权成功', function() {
                            $dsName.val(null).trigger("change");
                        });
                    },
                    complete: function () {
                        $.unblockUI();
                    }
                });
            }
        } else {
            toastr.warning('请选择授权列和授权用户');
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
        $.get(base_path + '/security/ds/' + value + '/table/list', function (data) {
            // datasources
            $('select[name="ds-list"] option').remove();
            $dsList.select2({
                data: _.map(data.tables, function (name) {
                    return {
                        id: name,
                        text: name
                    }
                })
            });
            $dsList.val(null).trigger("change");

            // users
            $('select[name="users"] option').remove();
            $users = $('select[name="users"]').select2({
                data: _.map(data.users, function (user) {
                    return {
                        id: user.username,
                        text: user.username
                    }
                })
            });
            $users.val(null).trigger("change");
        });
    } else {
        $('select[name="ds-list"] option').remove();
        $dsList.val(null).trigger("change");
        $('select[name="users"] option').remove();
        $users.val(null).trigger("change");
    }
}

function dsChangeList() {
    var value = $dsList.val();
    var $struct = $('#struct');
    if (value) {
        $.blockUI({message: ""});
        $.ajax({
            url: base_path + '/security/ds/' + $dsName.val() + '/sec/' + encodeURIComponent(value),
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
        $tableStruct = null;
    }
}