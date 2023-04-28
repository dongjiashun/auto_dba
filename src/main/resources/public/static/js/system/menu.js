require('base-product');
var $ = require('jquery');
var _ = require('underscore');
var bootbox = require('bootbox');
require('jquery.blockUI');
bootbox.setLocale('zh_CN');
var Codemirror = require('codemirror');
require('codemirror/mode/javascript/javascript');

$(function() {
    initCodemirror('#menus-config-form [name="menus"]');

    var $menus = $('#menus-config-form');
    $menus.submit(function() {
        bootbox.confirm("你确定要提交菜单配置变更吗?", function(result) {
            if (result) {
                var menus = $('#menus-config-form').find('[name="menus"]').val();

                try {
                    var menusJson = JSON.parse(menus);
                    validateMenus(menusJson);

                    menus = JSON.stringify(menusJson, null, 2);

                    // submit
                    $.blockUI({message: ""});
                    $.ajax({
                        url: base_path + '/system/menu/update',
                        data: {
                            menus: menus
                        },
                        success: function (result) {
                            if(result.code == 0) {
                                bootbox.alert('菜单配置成功', function() {
                                    location.reload();
                                });
                            } else {
                                bootbox.alert('菜单配置失败, 请重新检查格式后再提交变更');
                            }
                        },
                        complete: function () {
                            $.unblockUI();
                        }
                    });
                } catch (err) {
                    bootbox.alert('菜单配置格式不正确, 请重新检查后再提交变更');
                }
            }
        });

        return false;
    });
});

function validateMenus(menus) {
    if (!_.isArray(menus) || menus.length < 1) throw "error menus config";
}

// Codemirror
function initCodemirror(selecter) {
    Codemirror.fromTextArea($(selecter).get(0), {
        mode: 'application/json',
        lineNumbers: true,
        indentWithTabs: true,
        autofocus: false,
        matchBrackets: true
    });
}