require('base-product');
var $ = require('jquery');
var _ = require('underscore');
var moment = require('moment');
var toastr = require('toastr');
var bootbox = require('bootbox');
bootbox.setLocale('zh_CN');
require('jquery.blockUI');
require('bootstrap-table');

var $taskPendingTable, $processMyTable;
$(function() {
    $taskPendingTable = $('#task-pending').bootstrapTable({
        responseHandler: function(result) {
            return result.data;
        }
    });

    $processMyTable = $('#process-my').bootstrapTable({
        responseHandler: function(result) {
            var data = result.data;
            _.each(data, function(item) {
                item.status = item.activeTask ? item.activeTask : item.taskBiz.status;
            });

            return {
                rows: data,
                total: result.pagination.rowCount
            }
        }
    });
});

window.pendingOpFormatter = function(value, row, index) {
    return '<a  class="btn btn-primary btn-xs" href="'
        + base_path + '/task/' + row.taskBiz.type + '/' + row.taskKey + '/' + row.taskId
        + '" title="查看任务">处理任务</a>';
};

window.myOpFormatter = function(value, row, index) {
    return '<a class="text-primary" href="'
        + base_path + '/process/' + row.taskBiz.type + '/' + row.taskBiz.processInstanceId
        + '" title="查看详情"><i class="fa fa-search fa-fw"></i></a>';
};