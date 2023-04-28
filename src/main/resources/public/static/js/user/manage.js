require('base-product');
var $ = require('jquery');
var _ = require('underscore');
var moment = require('moment');
var toastr = require('toastr');
var bootbox = require('bootbox');
bootbox.setLocale('zh_CN');
require('jquery.blockUI');
require('bootstrap-table');

var authTemplate = require("dot!./auth.dot");
var rolesTemplate = require("dot!./roles.dot");

var $userTable, $dsAuthTable, $rolesTable;
$(function() {
    $userTable = $('#users').bootstrapTable({
        queryParams: function(params) {
            if (params.search) {
                params.q = params.search;
                delete params.search;
            }
            return params;
        },
        responseHandler: function(result) {
            return {
                rows: result.data,
                total: result.pagination.rowCount
            }
        }
    });

    // auth
    $(document).on('click', '[name="auth"]', dsAuth);
    $(document).on('click', '[name="auth-del"]', dsAuthDel);

    // roles
    $(document).on('click', '[name="roles"]', userRoles);
    $(document).on('click', '[name="roles-save"]', userRolesSave);
});

function dsAuth() {
    var id = $(this).parents('tr[data-uniqueid]').data('uniqueid');
    var row = $userTable.bootstrapTable('getRowByUniqueId', id);

    // show
    bootbox.dialog({
        size: "large",
        onEscape: false,
        title: '用户数据源授权【' + row.username + '】',
        message: authTemplate(),
        buttons: {
            "关闭": {
                className: "btn-default",
                callback: function() {}
            }
        }
    });
    $dsAuthTable = $('#ds-auth').find('table').bootstrapTable({
        url: base_path + '/user/manage/' + id + '/auth_data',
        responseHandler: function(data) {
            return _.map(data, function(auth) {
                var roleString = '<code>default</code>';
                if (auth.roles && auth.roles.length) {
                    roleString = '';
                    _.each(auth.roles, function (role) {
                        roleString += '<code>' + role.name + '</code>&nbsp;&nbsp;';
                    });
                }
                auth.role = roleString;
                return auth;
            });
        }
    });
}
function dsAuthDel() {
    var data = $dsAuthTable.bootstrapTable('getSelections');
    if (data && data.length > 0) {
        bootbox.confirm('你确定要删除选中的' + data.length + '个数据源授权吗？', function(result) {
            if (result) {
                var ids = _.map(data, function (auth) {
                    return auth.authId;
                });
                $.blockUI({message: ""});
                $.ajax({
                    url: base_path + '/datasource/auth/del',
                    contentType:"application/json;charset=utf-8",
                    data: JSON.stringify(ids),
                    success: function () {
                        $dsAuthTable.bootstrapTable('refresh');
                    },
                    complete: function () {
                        $.unblockUI();
                    }
                });
            }
        });
    } else {
        bootbox.alert('至少需要选择一个操作对象');
    }
}

function userRoles() {
    var id = $(this).parents('tr[data-uniqueid]').data('uniqueid');
    var row = $userTable.bootstrapTable('getRowByUniqueId', id);

    // show
    bootbox.dialog({
        size: "large",
        onEscape: false,
        title: '用户角色【' + row.username + '】',
        message: rolesTemplate({
            id: id
        }),
        buttons: {
            "关闭": {
                className: "btn-default",
                callback: function() {}
            }
        }
    });

    $rolesTable = $('#user-roles').find('table').bootstrapTable({
        url: base_path + '/user/manage/' + id + '/role_data',
        responseHandler: function(data) {
            _.each(data.roles, function(role) {
                role.checked = _.find(data.userRoles, function(item) {return item == role.id}) ? true : false;
            });

            return data.roles;
        }
    });
}

function userRolesSave() {
    var data = $rolesTable.bootstrapTable('getSelections');
    var ids = _.map(data, function (role) {
        return role.id;
    });

    $.blockUI({message: ""});
    $.ajax({
        url: base_path + '/user/manage/' + $('#user-roles').data('user') + '/roles',
        data: {
            roles : ids
        },
        success: function () {
            bootbox.alert('用户角色保存修改成功', function() {
                $rolesTable.bootstrapTable('refresh');
            });
        },
        complete: function () {
            $.unblockUI();
        }
    });
}

window.opFormatter = function(value, row, index) {
    return [
        '<a name="roles" class="text-warning" href="javascript:;" title="角色管理"><i class="fa fa-users fa-fw"></i></a>',
        '<a name="auth" class="text-success" href="javascript:;" title="查看数据源"><i class="fa fa-database fa-fw"></i></a>'
    ].join(' ');
};