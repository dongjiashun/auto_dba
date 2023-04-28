require('base-product');
var $ = require('jquery');
var _ = require('underscore');
var moment = require('moment');
var toastr = require('toastr');
var bootbox = require('bootbox');
bootbox.setLocale('zh_CN');
require('jquery.blockUI');
require('select2');
require('select2/dist/js/i18n/zh-CN');
require('nice-validator');

var $dsApplyForm = $('#ds-apply-form');
var $dsAuditForm = $('#ds-audit-form');
var $dsAdjustForm = $('#ds-adjust-form');
var $dsEnv, $dsName;
$(function() {
    $dsEnv = $('select[name="ds-env"]').select2({minimumResultsForSearch: Infinity});
    $dsName = $('select[name="ds-name"]').select2();
    $dsEnv.on('change', dsChangeEnv);
    $dsEnv.trigger("change");

    // apply
    $dsApplyForm.validator({
        fields: {
            "ds-name": "数据源:required",
            reason: "申请理由:required;length[1~1000]"
        },
        stopOnError: true,
        timely: 0,
        msgMaker: false,
        invalid: function(form, errors){
            $dsApplyForm.find('.form-msg').text(errors[0]);
        },
        valid: function() {
            $dsApplyForm.find('.form-msg').text('');

            $.blockUI({message: ""});
            $.ajax({
                url: base_path + '/task/ds-apply/apply',
                data: {
                    env: $dsEnv.val(),
                    ds: $dsName.val(),
                    reason: $dsApplyForm.find('[name=reason]').val()
                },
                success: function (result) {
                    if(result.code == 0) {
                        var data = result.data;
                        if (data == 0) {
                            bootbox.alert('数据源申请成功', function() {
                                location.href = base_path + '/';
                            });
                        } else {
                            bootbox.alert('数据源申请失败:你申请的数据源不存在或已经授权');
                        }
                    }
                },
                complete: function () {
                    $.unblockUI();
                }
            });
        }
    });
    // audit
    $dsAuditForm.validator({
        fields: {
            reason: "审核意见:required;length[1~255]"
        },
        stopOnError: true,
        timely: 0,
        msgMaker: false,
        invalid: function(form, errors){
            $dsAuditForm.find('.form-msg').text(errors[0]);
        },
        valid: function() {
            $dsAuditForm.find('.form-msg').text('');
            var formData = {
                agree: $dsAuditForm.find('[name=agree]').val(),
                reason: $dsAuditForm.find('[name=reason]').val(),
                role: _.map($dsAuditForm.find('[name=role]:checked'), function(role) {
                    return $(role).val();
                })
            };
            $.blockUI({message: ""});
            $.ajax({
                url: base_path + '/task/ds-apply/audit/' + $dsAuditForm.data('id'),
                data: formData,
                success: function (result) {
                    if(result.code == 0) {
                        var data = result.data;
                        if (data == 0) {
                            bootbox.alert('数据源审核成功', function() {
                                location.href = base_path + '/';
                            });
                        } else {
                            bootbox.alert('数据源审核失败:你审核的任务已不存在');
                        }
                    }
                },
                complete: function () {
                    $.unblockUI();
                }
            });
        }
    });
    // adjust
    $dsAdjustForm.validator({
        fields: {
            reason: "重新申请理由:required;length[1~1000]"
        },
        stopOnError: true,
        timely: 0,
        msgMaker: false,
        invalid: function(form, errors){
            $dsAdjustForm.find('.form-msg').text(errors[0]);
        },
        valid: function() {
            $dsAdjustForm.find('.form-msg').text('');
            var formData = {
                apply: $dsAdjustForm.find('[name=apply]').val(),
                reason: $dsAdjustForm.find('[name=reason]').val()
            };
            $.blockUI({message: ""});
            $.ajax({
                url: base_path + '/task/ds-apply/adjust/' + $dsAdjustForm.data('id'),
                data: formData,
                success: function (result) {
                    if(result.code == 0) {
                        var data = result.data;
                        if (data == 0) {
                            bootbox.alert('调整数据源申请成功', function() {
                                location.href = base_path + '/';
                            });
                        } else {
                            bootbox.alert('调整数据源申请失败:你调整的任务已不存在');
                        }
                    }
                },
                complete: function () {
                    $.unblockUI();
                }
            });
        }
    });
});

function dsChangeEnv() {
    var value = $dsEnv.val();
    if (value) {
        $.get(base_path + '/ds/list/unauth?env=' + value, function (data) {
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