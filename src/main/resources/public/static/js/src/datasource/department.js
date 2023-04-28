require('base-product');
var $ = require('jquery');
window.jQuery = $;
var _ = require('underscore');
var moment = require('moment');
var toastr = require('toastr');
var bootbox = require('bootbox');
bootbox.setLocale('zh_CN');
require('jquery.blockUI');
require('bootstrap-table');
require('nice-validator');
require('bootstrap-show-password/bootstrap-show-password');

var formTemplate = require("dot!./department.dot");

/** datasource table **/
var $dsTable = null;
var datasourceProxies = [];
$(function() {
    $dsTable = $('#datasources').bootstrapTable({
        responseHandler: function(resp) {
            var data = [];
            _.each(resp, function(ds) {
                data.push({
                    id: ds.id,
                    dbname: ds.dbname,
                    dbinstance: ds.dbinstance,
                    username: ds.username,
                    passwd: ds.passwd,
                    team: ds.team,
                    slowlog: ds.slowlog,
                    sqlkill: ds.sqlkill
                });
            });
            return data;
        }
    });


    $('#toolbar').find('[name="env"]').change(changeEnv);

    $('#ds-add').click(dsAdd);
    $(document).on('click', '[name="modify"]', dsModify);
    $(document).on('click', '[name="delete"]', dsDelete);
});

/** change env **/
function changeEnv() {
    var env = $(this).val();
    if ($dsTable) {
        $dsTable.bootstrapTable('refresh', {
            url: base_path + '/datasource/department/list?env=' + env
        });
    }
}


// op
function dsAdd() {
    bootbox.dialog({
        size: 'large',
        onEscape: false,
        title: '新增数据源部门关系',
        message: formTemplate({
            proxies: datasourceProxies,
            /*username: "dbamgr/qauser",
            password: "De0ca71106a4e4d1/Qauser123",
            username2: "prdquery/qauser",
            password2: "274836Bdec/Qauser123"*/
        }),
        buttons: {
            "取消": {
                className: "btn-default",
                callback: function() {}
            },
            "确定": {
                className: "btn-primary",
                callback: function() {
                var formData = getDsFormData();
                    $.blockUI({message: ""});
                    $.ajax({
                        url: base_path + '/datasource/department/add',
                        data: formData,
                        success: function (result) {
                            var data = result.data;
                            if (data === 1) {
                                bootbox.alert('添加 ' + formData.dbname + ' 失败1');
                            } else if(data == 2) {
                                bootbox.alert('添加 ' + formData.dbname + ' 失败2');
                            } else if(data == 3) {
                                bootbox.alert('添加 ' + formData.dbname + ' 失败3');
                            } else {
                                bootbox.alert('数据源部门映射关系 ' + formData.dbname + ' 添加成功', function() {
                                    bootbox.hideAll();
                                    $dsTable.bootstrapTable('refresh');
                                });
                            }
                        },
                        complete: function () {
                            $.unblockUI();
                        }
                    });
                    return false;
                }
            }
        }
    }).on('shown.bs.modal', function () {
        // bindDsValidator();
        // $('#ds-form').find(':password').password();
    });
}


function dsModify() {
    var id = $(this).parents('tr[data-uniqueid]').data('uniqueid');
    var dsData = getDsData(id);
    if (!dsData) {
        return;
    }

    bootbox.dialog({
        size: 'large',
        onEscape: false,
        title: '修改数据源部门关系',
        message: formTemplate(dsData),
        buttons: {
            "取消": {
                className: "btn-default",
                callback: function() {}
            },
            "确定": {
                className: "btn-primary",
                callback: function() {
                    var formData = getDsFormData();
                    $.blockUI({message: ""});
                    $.ajax({
                        url: base_path + '/datasource/department/update',
                        data: formData,
                        success: function (result) {
                            var data = result.data;
                            if (data === 1) {
                                bootbox.alert('更新 数据源名称和实例 ' + formData.dbname+' '+formData.dbinstance + ' 和数据库的记录对应id不一致');
                            } else if(data == 2) {
                                bootbox.alert('更新 ' + formData.dbname + ' 失败2');
                            } else if(data == 3) {
                                bootbox.alert('更新 ' + formData.dbname + ' 失败3');
                            } else {
                                bootbox.alert('数据源部门映射关系 ' + formData.dbname + ' 更新成功', function () {
                                    bootbox.hideAll();
                                    $dsTable.bootstrapTable('refresh');
                                });
                            }
                        },
                        complete: function () {
                            $.unblockUI();
                        }
                    });
                    return false;
                }
            }
        }
    }).on('shown.bs.modal', function () {
        // bindDsValidator();
        // $('#ds-form').find(':password').password();
    });
}

function dsDelete() {
    var id = $(this).parents('tr[data-uniqueid]').data('uniqueid');
    bootbox.confirm('你确定要删除该数据源部门映射记录吗？', function(result) {
        if (result) {
            $.blockUI({message: ""});
            $.ajax({
                url: base_path + '/datasource/department/' + id + '/del',
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

// table formatter
window.opFormatter = function(value, row, index) {
    return [
        '<a name="modify" class="text-success" href="javascript:;" title="修改"><i class="fa fa-pencil-square-o fa-fw"></i></a>',
        '<a name="delete" class="text-danger" href="javascript:;" title="删除"><i class="fa fa-trash-o fa-fw"></i></a>'
    ].join(' ');
};

// get ds data from id
function getDsData(id) {
    var data = null;
    $.blockUI({message: ""});
    $.ajax({
        method: 'GET',
        async: false,
        url: base_path + '/datasource/department/' + id,
        success: function (result) {
            data = result;
            data['type_' + data.type] = true;
            data['env_' + data.env] = true;//todo: delete
        },
        complete: function () {
            $.unblockUI();
        }
    });

    return data;
}

// get ds form data
function getDsFormData() {
    var $ds = $('#ds-form');
    var data = {
        id: $ds.find('[name="id"]').val(),
        team: $ds.find('[name="team"]').val(),
        dbname: $ds.find('[name="dbname"]').val(),
        dbinstance: $ds.find('[name="dbinstance"]').val(),

        username: $ds.find('[name="username"]').val(),
        passwd: $ds.find('[name="passwd"]').val(),
        slowlog: $ds.find('[name="slowlog"]').val(),
        sqlkill: $ds.find('[name="sqlkill"]').val()

        // env: $ds.find('[name="env"]').val(), //todo : 添加环境字段
    };
    return data;
}