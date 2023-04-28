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

var formTemplate = require("dot!./form.dot");

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
                    main: true,
                    name: '【主】' + ds.name,
                    type: ds.cobar ? 'cobar': ds.type,
                    sid: ds.sid,
                    username: ds.username,
                    host: ds.host,
                    port: ds.port,
                    create: ds.gmtCreate
                });

                // backup
                data.push({
                    id: ds.id,
                    main: false,
                    name: '【备】' + ds.name,
                    type: ds.cobar ? 'cobar': ds.type,
                    sid: ds.sid2,
                    username: ds.username2,
                    host: ds.host2,
                    port: ds.port2,
                    create: ds.gmtCreate
                });
            });
            return data;
        }
    });

    // cache datasource proxies
    $.get(base_path + "/datasource/proxy/list", function (proxys) {
        datasourceProxies = proxys;
    });

    $('#toolbar').find('[name="env"]').change(changeEnv);

    $('#ds-add').click(dsAdd);
    $(document).on('click', '[name="modify"]', dsModify);
    $(document).on('click', '[name="delete"]', dsDelete);

    $(document).on('click', '[name="sync-main"]', dsSyncMain);
    $(document).on('click', '[name="sync-main-weak"]', dsSyncMainWeak);
    $(document).on('click', '[name="sync-main-proxy"]', dsSyncMainProxy);

    // test proxy connection
    $(document).on('click', '[name="test-proxy"]', function() {
        if (dsValidator) {
            dsValidator.trigger("validate");
            if (dsValidator.isValid()) {
                testProxyConnection();
            }
        }
    });
});

/** change env **/
function changeEnv() {
    var env = $(this).val();
    if ($dsTable) {
        $dsTable.bootstrapTable('refresh', {
            url: base_path + '/datasource/list?env=' + env
        });
    }
}

// op
function dsAdd() {
    bootbox.dialog({
        size: 'large',
        onEscape: false,
        title: '新增数据源',
        message: formTemplate({
            proxies: datasourceProxies,
            username: "prdquery",
            password: "274836Bdec",
            username2: "prdquery",
            password2: "274836Bdec"
        }),
        buttons: {
            "测试连接": {
                className: "btn-success",
                callback: function() {
                    dsValidator.trigger("validate");
                    if (dsValidator.isValid()) {
                        testConnection();
                    }
                    return false;
                }
            },
            "取消": {
                className: "btn-default",
                callback: function() {}
            },
            "确定": {
                className: "btn-primary",
                callback: function() {
                    dsValidator.trigger("validate");
                    if (dsValidator.isValid()) {
                        testConnection(function(formData) {
                            $.blockUI({message: ""});
                            $.ajax({
                                url: base_path + '/datasource/add',
                                data: formData,
                                success: function (result) {
                                    var data = result.data;
                                    if (data === 1) {
                                        bootbox.alert('数据源名称 ' + formData.name + ' 已经存在');
                                    } else if(data == 2) {
                                        bootbox.alert('数据源SID ' + formData.sid + ' 已经存在');
                                    } else if(data == 3) {
                                        bootbox.alert('数据源名称 ' + formData.name + ' 连接测试失败');
                                    } else {
                                        bootbox.alert('数据源 ' + formData.name + ' 添加成功', function() {
                                            bootbox.hideAll();
                                            $dsTable.bootstrapTable('refresh');
                                        });
                                    }
                                },
                                complete: function () {
                                    $.unblockUI();
                                }
                            });
                        });
                    }
                    return false;
                }
            }
        }
    }).on('shown.bs.modal', function () {
        bindDsValidator();
        $('#ds-form').find(':password').password();
    });
}
function dsModify() {
    var id = $(this).parents('tr[data-uniqueid]').data('uniqueid');
    var dsData = getDsData(id);
    if (!dsData) {
        return;
    }

    dsData.proxies = datasourceProxies;
    bootbox.dialog({
        size: 'large',
        onEscape: false,
        title: '修改数据源',
        message: formTemplate(dsData),
        buttons: {
            "测试连接": {
                className: "btn-success",
                callback: function() {
                    dsValidator.trigger("validate");
                    if (dsValidator.isValid()) {
                        testConnection();
                    }
                    return false;
                }
            },
            "取消": {
                className: "btn-default",
                callback: function() {}
            },
            "确定": {
                className: "btn-primary",
                callback: function() {
                    dsValidator.trigger("validate");
                    if (dsValidator.isValid()) {
                        testConnection(function(formData) {
                            $.blockUI({message: ""});
                            $.ajax({
                                url: base_path + '/datasource/update',
                                data: formData,
                                success: function (result) {
                                    var data = result.data;
                                    if (data === 1) {
                                        bootbox.alert('数据源名称 ' + formData.name + ' 已经存在');
                                    } else if(data == 2) {
                                        bootbox.alert('数据源SID ' + formData.sid + ' 已经存在');
                                    } else if(data == 3) {
                                        bootbox.alert('数据源名称 ' + formData.name + ' 连接测试失败');
                                    } else {
                                        bootbox.alert('数据源 ' + formData.name + ' 更新成功', function () {
                                            bootbox.hideAll();
                                            $dsTable.bootstrapTable('refresh');
                                        });
                                    }
                                },
                                complete: function () {
                                    $.unblockUI();
                                }
                            });
                        });
                    }
                    return false;
                }
            }
        }
    }).on('shown.bs.modal', function () {
        bindDsValidator();
        $('#ds-form').find(':password').password();
    });
}
function dsDelete() {
    var id = $(this).parents('tr[data-uniqueid]').data('uniqueid');
    bootbox.confirm('你确定要删除该数据源吗？', function(result) {
        if (result) {
            $.blockUI({message: ""});
            $.ajax({
                url: base_path + '/datasource/' + id + '/del',
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

function dsSyncMain() {
    var $dsForm = $('#ds-form');
    $dsForm.find('[name="sid2"]').val($dsForm.find('[name="sid"]').val());
    $dsForm.find('[name="host2"]').val($dsForm.find('[name="host"]').val());
    $dsForm.find('[name="port2"]').val($dsForm.find('[name="port"]').val());
    $dsForm.find('[name="username2"]').val($dsForm.find('[name="username"]').val());
    $dsForm.find('[name="password2"]').val($dsForm.find('[name="password"]').val());
}

function dsSyncMainWeak() {
    var $dsForm = $('#ds-form');
    $dsForm.find('[name="sid2"]').val($dsForm.find('[name="sid"]').val());
    $dsForm.find('[name="host2"]').val($dsForm.find('[name="host"]').val());
    $dsForm.find('[name="port2"]').val($dsForm.find('[name="port"]').val());
}

function dsSyncMainProxy() {
    var $dsForm = $('#ds-form');
    $dsForm.find('[name="proxySid"]').val($dsForm.find('[name="sid"]').val());
    $dsForm.find('[name="proxyUsername"]').val($dsForm.find('[name="username"]').val());
    $dsForm.find('[name="proxyPassword"]').val($dsForm.find('[name="password"]').val());
}

function testConnection(successCallback) {
    var formData = getDsFormData();
    $.blockUI({message: ""});
    $.ajax({
        url: base_path + '/datasource/test',
        data: formData,
        success: function (result) {
            var data = result.data;
            if (typeof successCallback != 'undefined' && data.left.left && data.right.left) {
                successCallback(formData);
            } else {
                var message = '';
                message += data.left.left ? '<span class="text-success">主库连接测试成功</span>' : '<span class="text-danger">主库连接测试失败：' + data.left.right + '</span>';
                message += '<br/>';
                message += data.right.left ? '<span class="text-success">备库连接测试成功</span>' : '<span class="text-danger">备库连接测试失败：' + data.right.right + '</span>';

                bootbox.alert(message);
            }
        },
        complete: function () {
            $.unblockUI();
        }
    });
}

function testProxyConnection() {
    var formData = getDsFormData();
    $.blockUI({message: ""});
    $.ajax({
        url: base_path + '/datasource/proxy/test',
        data: formData,
        success: function (result) {
            var data = result.data;
            var message = data.left ? '<span class="text-success">代理连接测试成功</span>' : '<span class="text-danger">代理连接测试失败：' + data.right + '</span>';

            bootbox.alert(message);
        },
        complete: function () {
            $.unblockUI();
        }
    });
}

// table formatter
window.opFormatter = function(value, row, index) {
    return [
        '<a name="modify" class="text-success" href="javascript:;" title="修改"><i class="fa fa-pencil-square-o fa-fw"></i></a>',
        '<a name="delete" class="text-danger" href="javascript:;" title="删除"><i class="fa fa-trash-o fa-fw"></i></a>',
        '<a name="auth" class="text-warning" href="' + base_path + '/datasource/' + row.id + '/auth" title="查看、管理授权"><i class="fa fa-search fa-fw"></i></a>'
    ].join(' ');
};

// validator
var dsValidator = null;
function bindDsValidator() {
    if (dsValidator) {
        dsValidator.trigger('destroy');
    }
    dsValidator = $('#ds-form').validator({
        rules: {
            isProxy: function() {
                return !!$('[name=proxyId]').val();
            }
        },
        fields: {
            name: "数据源名称:required;length[1~30]",

            sid: "SID:required;length[1~30]",
            host: "主机:required;length[1~100]",
            port: "端口号:required;integer[+];range[~65535]",
            username: "用户名:required;length[1~30]",
            password: "密码:required;length[1~64]",

            sid2: "备库SID:required;length[1~30]",
            host2: "备库主机:required;length[1~100]",
            port2: "备库端口号:required;integer[+];range[~65535]",
            username2: "备库用户名:required;length[1~30]",
            password2: "备库密码:required;length[1~64]",

            proxyId: "代理:integer[+]",
            proxyPort: "代理端口号:required(isProxy);integer[+];range[~65535]",
            proxySid: "代理SID:required(isProxy);length[1~30]",
            proxyUsername: "代理用户名:required(isProxy);length[1~30]",
            proxyPassword: "代理密码:required(isProxy);length[1~64]"
        },
        stopOnError: true,
        timely: 0,
        msgMaker: false,
        invalid: function(form, errors){
            $('#ds-form-msg').html(errors[0]);
        },
        valid: function() {
            $('#ds-form-msg').html('');
        }
    });
}

// get ds data from id
function getDsData(id) {
    var data = null;
    $.blockUI({message: ""});
    $.ajax({
        method: 'GET',
        async: false,
        url: base_path + '/datasource/' + id,
        success: function (result) {
            data = result;
            data['type_' + data.type] = true;
            data['env_' + data.env] = true;
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
        name: $ds.find('[name="name"]').val(),
        type: $ds.find('[name="type"]').val(),
        env: $ds.find('[name="env"]').val(),

        sid: $ds.find('[name="sid"]').val(),
        host: $ds.find('[name="host"]').val(),
        port: $ds.find('[name="port"]').val(),
        username: $ds.find('[name="username"]').val(),
        password: $ds.find('[name="password"]').val(),

        sid2: $ds.find('[name="sid2"]').val(),
        host2: $ds.find('[name="host2"]').val(),
        port2: $ds.find('[name="port2"]').val(),
        username2: $ds.find('[name="username2"]').val(),
        password2: $ds.find('[name="password2"]').val()
    };

    var proxyId = $ds.find('[name="proxyId"]').val();
    proxyId && (data["proxy.id"] = proxyId);
    var proxyPort = $ds.find('[name="proxyPort"]').val();
    proxyPort && (data.proxyPort = proxyPort);
    var proxySid = $ds.find('[name="proxySid"]').val();
    proxySid && (data.proxySid = proxySid);
    var proxyUsername = $ds.find('[name="proxyUsername"]').val();
    proxyUsername && (data.proxyUsername = proxyUsername);
    var proxyPassword = $ds.find('[name="proxyPassword"]').val();
    proxyPassword && (data.proxyPassword = proxyPassword);

    return data;
}