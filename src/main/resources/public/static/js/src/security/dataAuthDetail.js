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

$(function() {
    var $authDetailForm = $('#auth-detail-form');
    var $authDetailTable = $('#auth-detail');
    $authDetailTable.bootstrapTable({
        responseHandler: function(result) {
            return {
                rows: result.data,
                total: result.pagination.rowCount
            }
        }
    });

    // select and datasource
    var $env = $authDetailForm.find('select[name="env"]').select2({minimumResultsForSearch: Infinity});
    var $datasource = $authDetailForm.find('select[name="datasource"]').select2({allowClear:true});

    $env.on('change', function() {
        var value = $env.val();
        if (value) {
            $.get(base_path + '/datasource/list2?env=' + value, function (data) {
                $authDetailForm.find('select[name="datasource"] option').remove();
                $datasource.select2({
                    allowClear:true,
                    data: _.map(data, function (ds) {
                        return {
                            id: ds.id,
                            text: '[' + ds.type + ']' + ds.name
                        }
                    })
                });
                $datasource.val(null).trigger("change");
            });
        }
    });
    $env.trigger("change");

    // query
    $authDetailForm.submit(function() {
        var datasource = $datasource.val();
        var user = $authDetailForm.find('[name="user"]').val();
        $authDetailTable.bootstrapTable('refreshOptions', {
            queryParams : function(params) {
                datasource && (params.datasource = datasource);
                user && (params.username = user);

                return params;
            }
        });
        return false;
    });

    // delete
    $('#auth-del-btn').click(function() {
        if ($authDetailTable && $authDetailTable.bootstrapTable('getSelections').length > 0) {
            bootbox.confirm('确定要删除所选的条目吗', function(result) {
                if (result) {
                    var ids = _.map($authDetailTable.bootstrapTable('getSelections'), function(item) { return item.id });
                    $.blockUI({message: ""});
                    $.ajax({
                        url: base_path + '/security/data-auth/del',
                        data: {
                            ids: ids
                        },
                        success: function (result) {
                            // refresh
                            $authDetailTable.bootstrapTable('refresh');
                        },
                        complete: function () {
                            $.unblockUI();
                        }
                    });
                }
            });
        } else {
            toastr.info('请选择要删除的条目');
        }
    });
});
