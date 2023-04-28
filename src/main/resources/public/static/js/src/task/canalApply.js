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

var $canalApplyForm = $('#canal-apply-form');
var $canalAuditForm = $('#canal-audit-form');
var $canalAdjustForm = $('#canal-adjust-form');
var $dsEnv, $dsName, $dsTable;
$(function() {
    $dsEnv = $('select[name="ds-env"]').select2({minimumResultsForSearch: Infinity});
    $dsName = $('select[name="ds-name"]').select2();
    $dsTable = $('select[name="ds-table"]').select2();
    $dsEnv.on('change', dsChangeEnv);
    $dsName.on('change', dsChangeName);
    $dsEnv.trigger("change");

    var $agree = $canalAuditForm.find('select[name="agree"]').select2({minimumResultsForSearch: Infinity});
    var $manager = $canalAuditForm.find('select[name="manager"]').select2();
    $agree.on('change', function() {
        var value = $agree.val();
        if (value == '1') {
            $('.auto-options').show();
        } else {
            $('.auto-options').hide();
        }
    });

    var $apply = $canalAdjustForm.find('select[name="apply"]').select2({minimumResultsForSearch: Infinity});

    // apply
    $canalApplyForm.validator({
        fields: {
            "ds-name": "数据源:required",
            "ds-table": "同步数据表:required",
            reason: "申请理由:required;length[1~1000]"
        },
        stopOnError: true,
        timely: 0,
        msgMaker: false,
        invalid: function(form, errors){
            $canalApplyForm.find('.form-msg').text(errors[0]);
        },
        valid: function() {
            $canalApplyForm.find('.form-msg').text('');

            $.blockUI({message: ""});
            $.ajax({
                url: base_path + '/task/canal-apply/apply',
                data: {
                    env: $dsEnv.val(),
                    ds: $dsName.val(),
                    table: $dsTable.val(),
                    reason: $canalApplyForm.find('[name=reason]').val()
                },
                success: function (result) {
                    if(result.code == 0) {
                        var data = result.data;
                        if (data == 0) {
                            bootbox.alert('Canal同步申请成功', function() {
                                location.href = base_path + '/';
                            });
                        } else {
                            bootbox.alert('Canal同步申请失败:你申请的数据源不存在');
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
    $canalAuditForm.validator({
        rules: {
            autoCreate: function() {
                return $agree.val() == 1;
            }
        },
        fields: {
            reason: "审核意见:required;length[1~255]",
            manager: "Manager:required(autoCreate);integer[+]",
            index: "分区索引号:integer[+0]",
            target: "输出名:required(autoCreate);length[1~100]",
            key: "Key表达式:length[1~100]"
        },
        stopOnError: true,
        timely: 0,
        msgMaker: false,
        invalid: function(form, errors){
            $canalAuditForm.find('.form-msg').text(errors[0]);
        },
        valid: function() {
            $canalAuditForm.find('.form-msg').text('');
            var formData = {
                agree: $canalAuditForm.find('[name=agree]').val(),
                manager: $manager.val() || 0,
                target: $canalAuditForm.find('[name=target]').val(),
                index: $canalAuditForm.find('[name=index]').val() || 0,
                key: $canalAuditForm.find('[name=key]').val(),
                reason: $canalAuditForm.find('[name=reason]').val()
            };

            $.blockUI({message: ""});
            $.ajax({
                url: base_path + '/task/canal-apply/audit/' + $canalAuditForm.data('id'),
                data: formData,
                success: function (result) {
                    if(result.code == 0) {
                        var data = result.data;
                        if (data.left == 0) {
                            bootbox.alert('Canal同步审核成功', function() {
                                location.href = base_path + '/';
                            });
                        } else {
                            bootbox.alert('Canal同步审核失败:<br/><pre>' + data.right + '</pre>');
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
    $canalAdjustForm.validator({
        fields: {
            reason: "重新申请理由:required;length[1~1000]"
        },
        stopOnError: true,
        timely: 0,
        msgMaker: false,
        invalid: function(form, errors){
            $canalAdjustForm.find('.form-msg').text(errors[0]);
        },
        valid: function() {
            $canalAdjustForm.find('.form-msg').text('');
            var formData = {
                apply: $canalAdjustForm.find('[name=apply]').val(),
                reason: $canalAdjustForm.find('[name=reason]').val()
            };
            $.blockUI({message: ""});
            $.ajax({
                url: base_path + '/task/canal-apply/adjust/' + $canalAdjustForm.data('id'),
                data: formData,
                success: function (result) {
                    if(result.code == 0) {
                        var data = result.data;
                        if (data == 0) {
                            bootbox.alert('调整Canal同步申请成功', function() {
                                location.href = base_path + '/';
                            });
                        } else {
                            bootbox.alert('调整Canal同步申请失败:你调整的任务已不存在');
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
    var dsName = $dsName.val();
    if (dsName) {
        $.get(base_path + '/ds/' + encodeURIComponent(dsName) + '/table', function (data) {
            $('select[name="ds-table"] option').remove();
            $dsTable.select2({
                data: _.map(data, function(name) {
                    return {
                        id: name,
                        text: name
                    }
                })
            });
            $dsTable.val(null).trigger("change");
        });
    } else {
        $('select[name="ds-table"] option').remove();
        $dsTable.val(null).trigger("change");
    }
}