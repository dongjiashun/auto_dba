require('base-product');
var $ = require('jquery');
var _ = require('underscore');
var moment = require('moment');
var toastr = require('toastr');
var bootbox = require('bootbox');
bootbox.setLocale('zh_CN');
require('jquery.blockUI');
require('bootstrap-table');
require('nice-validator');
require('select2');
require('select2/dist/js/i18n/zh-CN');

var $dsAuthTable = null;
$(function() {
    $dsAuthTable = $('#ds-auth-table').bootstrapTable({
        responseHandler: function(data) {
            return _.map(data, function(auth) {
                var roleString = '<code>default</code>';
                if (auth.roles && auth.roles.length) {
                    roleString = '';
                    _.each(auth.roles, function (role) {
                        roleString += '<code>' + role.name + '</code>&nbsp;&nbsp;';
                    });
                }

                return {
                    id : auth.id,
                    user: auth.user.username + ' (' + auth.user.nickname + ')',
                    role: roleString,
                    time: auth.gmtAuth
                };
            });
        }
    });

    // delete
    $('#auth-del').click(dsAuthDel);

    var $dsAuthForm = $('#ds-auth-form');
    var $users = $dsAuthForm.find('[name="users"]').select2({
        minimumInputLength: 2,
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
                            id: item.id,
                            text: item.nickname + '(' + item.username + ')'
                        }
                    })
                };
            },
            cache: true
        }
    });

    $dsAuthForm.submit(function () {
        var users = $users.val();
        if (users && users.length > 0) {
            bootbox.confirm("确定要新增/修改数据源授权吗?", function(result) {
                if (result) {
                    var formData = {
                        users: users,
                        roles: _.map($dsAuthForm.find('[name=role]:checked'), function (role) {
                            return $(role).val();
                        })
                    };
                    $.blockUI({message: ""});
                    $.ajax({
                        url: base_path + '/ds/' + $dsAuthForm.find('[name="id"]').val() + '/auth',
                        data: formData,
                        success: function () {
                            $dsAuthTable.bootstrapTable('refresh');
                            $dsAuthForm.find('[name=role]:checked').prop('checked',false);
                            $users.val(null).trigger("change");
                        },
                        complete: function () {
                            $.unblockUI();
                        }
                    });
                }
            });
        } else {
            toastr.warning('至少需要选择一个授权用户');
        }
        return false;
    });
});

function dsAuthDel() {
    var data = $dsAuthTable.bootstrapTable('getSelections');
    if (data && data.length > 0) {
        bootbox.confirm('你确定要删除选中的' + data.length + '个数据源授权吗？', function(result) {
            if (result) {
                var ids = _.map(data, function (auth) {
                    return auth.id;
                });
                $.blockUI({message: ""});
                $.ajax({
                    url: base_path + '/ds/' + $('#ds-auth-form').find('[name="id"]').val() + '/auth/del',
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
        toastr.warning('至少需要选择一个操作对象');
    }
}
