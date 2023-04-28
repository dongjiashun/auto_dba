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

var proxyTemplate = require("dot!./proxy.dot");
var proxyFormTemplate = require("dot!./proxyForm.dot");

var proxy = {};
$(function() {
    var $proxies = $('#proxies');
    var $proxyList = $proxies.find('.list-group-item');
    var $proxyDataSources = $('#proxy-datasources');
    $proxyList.click(changeProxy);

    if ($proxyList.length > 0) {
        $proxyList.eq(0).click();
    }

    $('#proxy-add').click(proxyAdd);
    $proxyDataSources.on('click', '[name="update"]', proxyUpdate);
    $proxyDataSources.on('click', '[name="delete"]', proxyDelete);
});

function changeProxy() {
    var $that = $(this);
    var $proxies = $('#proxies');
    var $proxyList = $proxies.find('.list-group-item');
    var $proxyDataSources = $('#proxy-datasources');

    $proxyList.removeClass('active');
    $that.addClass('active');

    var id = $that.data('id');
    $.get(base_path + '/datasource/proxy/' + ~~id, function (data) {
        if (data) {
            proxy = data;
            $proxyDataSources.html(proxyTemplate(proxy));
            if (proxy.dataSources && proxy.dataSources.length > 0) {
                var dataSources = [];
                _.each(proxy.dataSources, function(ds) {
                    // proxy
                    dataSources.push({
                        env: ds.env,
                        name: '【代】' + ds.name,
                        type: ds.type,
                        sid: ds.proxySid,
                        host: proxy.host,
                        port: ds.proxyPort
                    });
                    // main
                    dataSources.push({
                        env: ds.env,
                        name: '【主】' + ds.name,
                        type: ds.type,
                        sid: ds.sid,
                        host: ds.host,
                        port: ds.port
                    });
                    // backup
                    dataSources.push({
                        env: ds.env,
                        name: '【备】' + ds.name,
                        type: ds.type,
                        sid: ds.sid2,
                        host: ds.host2,
                        port: ds.port2
                    });
                });
                $proxyDataSources.find('table').bootstrapTable({
                    data: dataSources
                });
            }
        } else {
            bootbox.alert('数据源代理不存在!');
        }
    });
}

function proxyAdd() {
    bootbox.dialog({
        size: 'large',
        onEscape: false,
        title: '新增数据源代理',
        message: proxyFormTemplate({}),
        buttons: {
            "取消": {
                className: "btn-default",
                callback: function() {}
            },
            "新增": {
                className: "btn-primary",
                callback: function() {
                    proxyValidator.trigger("validate");
                    if (proxyValidator.isValid()) {
                        var formData = getProxyFormData();
                        $.blockUI({message: ""});
                        $.ajax({
                            url: base_path + '/datasource/proxy/add',
                            data: formData,
                            success: function (result) {
                                var data = result.data;
                                if (data === 1) {
                                    bootbox.alert('数据源代理 ' + formData.name + '(' + formData.host + ') 已经存在');
                                } else {
                                    bootbox.alert('数据源代理 ' + formData.name + '(' + formData.host + ') 添加成功', function() {
                                        bootbox.hideAll();
                                        location.reload();
                                    });
                                }
                            },
                            complete: function () {
                                $.unblockUI();
                            }
                        });
                    }
                    return false;
                }
            }
        }
    });
    bindProxyValidator();
}

function proxyUpdate() {
    bootbox.dialog({
        size: 'large',
        onEscape: false,
        title: '数据源代理更新',
        message: proxyFormTemplate(proxy),
        buttons: {
            "取消": {
                className: "btn-default",
                callback: function() {}
            },
            "更新": {
                className: "btn-primary",
                callback: function() {
                    proxyValidator.trigger("validate");
                    if (proxyValidator.isValid()) {
                        var formData = getProxyFormData();
                        $.blockUI({message: ""});
                        $.ajax({
                            url: base_path + '/datasource/proxy/update',
                            data: formData,
                            success: function (result) {
                                var data = result.data;
                                if (data === 1) {
                                    bootbox.alert('数据源代理 ' + formData.name + '(' + formData.host + ') 已经存在');
                                } else {
                                    bootbox.alert('数据源代理 ' + formData.name + '(' + formData.host + ') 更新成功', function() {
                                        bootbox.hideAll();
                                        updateProxyData(formData);
                                    });
                                }
                            },
                            complete: function () {
                                $.unblockUI();
                            }
                        });
                    }
                    return false;
                }
            }
        }
    });
    bindProxyValidator();
}

function proxyDelete() {
    var id = $(this).data('id');
    bootbox.confirm("你确定要删除该数据源代理吗?", function(result) {
        if (result) {
            $.blockUI({message: ""});
            $.ajax({
                url: base_path + '/datasource/proxy/' + id + '/del',
                success: function () {
                    location.reload();
                },
                complete: function () {
                    $.unblockUI();
                }
            });
        }
    });
}

// validator
var proxyValidator = null;
function bindProxyValidator() {
    if (proxyValidator) {
        proxyValidator.trigger('destroy');
    }
    proxyValidator = $('#proxy-form').validator({
        fields: {
            name: "name:required;length[1~30]",
            host: "host:required;length[1~64]"
        },
        stopOnError: true,
        timely: 0,
        msgMaker: false,
        invalid: function(form, errors){
            $('#proxy-form').find('.form-msg').html(errors[0]);
        },
        valid: function() {
            $('#proxy-form').find('.form-msg').html('');
        }
    });
}

function getProxyFormData() {
    var $form = $('#proxy-form');
    return {
        id: $form.find('[name="id"]').val(),
        name: $form.find('[name="name"]').val(),
        host: $form.find('[name="host"]').val()
    };
}

function updateProxyData(formData) {
    proxy.name = formData.name;
    proxy.host = formData.host;

    var $proxy = $('#proxy-' + proxy.id);
    $proxy.find('.proxy-name').html(proxy.name);
    $proxy.find('.proxy-host').html(proxy.host);
}
