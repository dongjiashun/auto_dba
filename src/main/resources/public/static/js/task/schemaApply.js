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

var $schemaApply = $('#schema-apply-form');
var $schemaAudit = $('#schema-audit-form');
var $schemaAdjust = $('#schema-adjust-form');
$(function() {
    $schemaApply.find('select[name="env"]').select2({minimumResultsForSearch: Infinity});
    $schemaApply.find('select[name="product"]').select2();
    $schemaApply.find('select[name="scene"]').select2();
    $schemaApply.find('[name="sid-available"]').click(sidAvailable);

    var $agree = $schemaAudit.find('select[name="agree"]').select2({minimumResultsForSearch: Infinity});
    var $env = $schemaAudit.find('select[name="env"]').select2({minimumResultsForSearch: Infinity});
    var $ds = $schemaAudit.find('select[name="ds"]').select2({allowClear: true});
    $agree.on('change', function() {
        var value = $agree.val();
        if (value == '1') {
            $('.auto-options').show();
        } else {
            $('.auto-options').hide();
        }
    });

    $env.on('change', function() {
        var value = $env.val();
        if (value) {
            $.get(base_path + '/datasource/list2?env=' + value, function (data) {
                $schemaAudit.find('select[name="ds"] option').remove();
                $ds.select2({
                    allowClear:true,
                    data: _.map(data, function (ds) {
                        return {
                            id: ds.id,
                            text: '[' + ds.type + ']' + ds.name
                        }
                    })
                });
                $ds.val(null).trigger("change");
            });
        }
    });
    $env.trigger("change");

    // apply
    $schemaApply.validator({
        fields: {
            "sid": "数据源名称:required;length[1~30]",
            "product-other": "其他产品线:length[1~50]",
            "scene-other": "其他项目场景:length[1~50]",
            "product-desc": "项目描述:required;length[1~1000]",
            "capacity-desc": "容量规划描述:required;length[1~1000]",
            "split-desc": "分库分表描述:length[1~1000]"
        },
        stopOnError: true,
        timely: 0,
        msgMaker: false,
        invalid: function(form, errors){
            $schemaApply.find('.form-msg').text(errors[0]);
        },
        valid: function() {
            var product = $schemaApply.find('[name=product]').val() || $schemaApply.find('[name=product-other]').val();
            if (!product) {
                $schemaApply.find('.form-msg').text("请输入产品线");
                return;
            }
            var scene = $schemaApply.find('[name=scene]').val() || $schemaApply.find('[name=scene-other]').val();
            if (!scene) {
                $schemaApply.find('.form-msg').text("请输入项目场景");
                return;
            }

            $schemaApply.find('.form-msg').text('');
            $.blockUI({message: ""});
            $.ajax({
                url: base_path + '/task/schema-apply/apply',
                data: {
                    env: $schemaApply.find('[name=env]').val(),
                    sid: $schemaApply.find('[name=sid]').val(),
                    product: product,
                    scene: scene,
                    productDesc: $schemaApply.find('[name=product-desc]').val(),
                    capacityDesc: $schemaApply.find('[name=capacity-desc]').val(),
                    splitDesc: $schemaApply.find('[name=split-desc]').val()
                },
                success: function (result) {
                    if(result.code == 0) {
                        var data = result.data;
                        if (data == 0) {
                            bootbox.alert('创建数据源申请成功', function() {
                                location.href = base_path + '/';
                            });
                        } else {
                            bootbox.alert('创建数据源申请失败:你申请的创建的数据源已存在');
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
    $schemaAudit.validator({
        fields: {
            dsName: "数据源名称:length[1~30]",
            reason: "审核意见:required;length[1~255]"
        },
        stopOnError: true,
        timely: 0,
        msgMaker: false,
        invalid: function(form, errors){
            $schemaAudit.find('.form-msg').text(errors[0]);
        },
        valid: function() {
            $schemaAudit.find('.form-msg').text('');
            var formData = {
                agree: $schemaAudit.find('[name=agree]').val(),
                ds: $schemaAudit.find('[name=ds]').val() || 0,
                dsName: $schemaAudit.find('[name=dsName]').val(),
                reason: $schemaAudit.find('[name=reason]').val()
            };
            $.blockUI({message: ""});
            $.ajax({
                url: base_path + '/task/schema-apply/audit/' + $schemaAudit.data('id'),
                data: formData,
                success: function (result) {
                    if(result.code == 0) {
                        var data = result.data;
                        if (data.left == 0) {
                            bootbox.alert('创建数据源审核成功', function() {
                                location.href = base_path + '/';
                            });
                        } else {
                            bootbox.alert('创建数据源审核失败:<br/><pre>' + data.right + '</pre>');
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
    $schemaAdjust.validator({
        fields: {
            "product-desc": "项目描述:required;length[1~1000]",
            "capacity-desc": "容量规划描述:required;length[1~1000]",
            "split-desc": "分库分表描述:length[1~1000]"
        },
        stopOnError: true,
        timely: 0,
        msgMaker: false,
        invalid: function(form, errors){
            $schemaAdjust.find('.form-msg').text(errors[0]);
        },
        valid: function() {
            $schemaAdjust.find('.form-msg').text('');
            var formData = {
                apply: $schemaAdjust.find('[name=apply]').val(),
                productDesc: $schemaAdjust.find('[name=product-desc]').val(),
                capacityDesc: $schemaAdjust.find('[name=capacity-desc]').val(),
                splitDesc: $schemaAdjust.find('[name=split-desc]').val()
            };
            $.blockUI({message: ""});
            $.ajax({
                url: base_path + '/task/schema-apply/adjust/' + $schemaAdjust.data('id'),
                data: formData,
                success: function (result) {
                    if(result.code == 0) {
                        var data = result.data;
                        if (data == 0) {
                            bootbox.alert('调整创建数据源申请成功', function() {
                                location.href = base_path + '/';
                            });
                        } else {
                            bootbox.alert('调整创建数据源申请失败:你调整的任务已不存在');
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

function sidAvailable() {
    var $sid = $schemaApply.find('[name="sid"]');
    var sid = $sid.val();
    if (sid) {
        var env = $schemaApply.find('[name="env"]').val();
        $.blockUI({message: ""});
        $.ajax({
            method: 'GET',
            url: base_path + '/ds/' + encodeURIComponent(sid) + '/available?env=' + env,
            success: function (result) {
                if (result && result.data) {
                    toastr.success(sid + '<br/>数据源名称当前可用');
                } else {
                    toastr.warning(sid + '<br/>数据源名称已经存在');
                }
            },
            complete: function () {
                $.unblockUI();
            }
        });
    } else {
        $sid.focus();
    }
}