require('base-product');
var $ = require('jquery');
var _ = require('underscore');
var moment = require('moment');
var toastr = require('toastr');
var bootbox = require('bootbox');
bootbox.setLocale('zh_CN');
require('jquery.blockUI');
require('bootstrap-table');


var formTemplate = require("dot!./parameters.dot");
var $dsTable = null;

$(function() {

    $dsTable = $('#parameters').bootstrapTable({
        responseHandler: function(resp) {
            var data = [];
            _.each(resp, function(ds) {
                data.push({
                    id: ds.id,
                    dbinstance: ds.dbinstance,
                    username: ds.username,
                    passwd: ds.passwd,
                    parameters: JSON.parse(ds.parameters),
                    parametersMap: ds.parametersMap
                });
            });
            return data;
        }
    });


    $('#toolbar').find('[name="env"]').change(changeEnv);

    $('#ds-add').click(dsAdd);
    $(document).on('click', '[name="modify"]', dsModify);
    $(document).on('click', '[name="delete"]', dsDelete);
    $(document).on('click', '[name="active"]', dsActive);
    $(document).on('click', '[name="states"]', dsStates);
});

function addParamHtml() {
    var paramsDiv = $('#params');
    var item = '<label class="col-sm-3 control-label"> 参数：</label>' +
        '<div class="col-sm-9">\n' +
        '   <input name="key" type="text" value="key" class="form-control">\n' +
        '   <input name="value" type="text" value="value" class="form-control">\n' +
        '</div>';
    paramsDiv.append(item);
}

/** change env **/
function changeEnv() {
    var env = $(this).val();
    if ($dsTable) {
        $dsTable.bootstrapTable('refresh', {
            url: base_path + '/datasource/parameters/list?env=' + env
        });
    }
}


// op
function dsAdd() {
    bootbox.dialog({
        size: 'large',
        onEscape: false,
        title: '新增实例参数',
        message: formTemplate({
            /*username: "dbamgr/qauser",
            password: "De0ca71106a4e4d1/Qauser123",
            username2: "prdquery/qauser",
            password2: "274836Bdec/Qauser123"*/
        }),
        buttons: {
            "添加属性":{
                className: "btn-primary",
                callback:function(){
                    addParamHtml();
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
                    var formData = getDsFormData();
                    $.blockUI({message: ""});
                    $.ajax({
                        url: base_path + '/datasource/parameters/add',
                        data: formData,
                        success: function (result) {
                            var data = result.data;
                            if (data === 1) {
                                bootbox.alert('添加 ' + formData.dbinstance + ' 失败1');
                            } else if(data == 2) {
                                bootbox.alert('添加 ' + formData.dbinstance + ' 失败2');
                            } else if(data == 3) {
                                bootbox.alert('添加 ' + formData.dbinstance + ' 失败3');
                            } else {
                                bootbox.alert('实例参数 ' + formData.dbinstance + ' 添加成功', function() {
                                    bootbox.hideAll();
                                    $dsTable.bootstrapTable('refresh');
                                });
                            }
                        },
                        complete: function () {
                            $.unblockUI();
                        }
                    });
                    return false;
                }
            }
        }
    }).on('shown.bs.modal', function () {
        // bindDsValidator();
        // $('#ds-form').find(':password').password();
    });
}

function dsModify() {
    var id = $(this).parents('tr[data-uniqueid]').data('uniqueid');
    var dsData = getDsData(id);
    if (!dsData) {
        return;
    }

    bootbox.dialog({
        size: 'large',
        onEscape: false,
        title: '修改实例参数',
        message: formTemplate(dsData),
        buttons: {
            "添加属性":{
                className: "btn-primary",
                callback:function(){
                    addParamHtml();
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
                    var formData = getDsFormData();
                    $.blockUI({message: ""});
                    $.ajax({
                        url: base_path + '/datasource/parameters/update',
                        data: formData,
                        success: function (result) {
                            var data = result.data;
                            if (data === 1) {
                                bootbox.alert('更新 数据源名称和实例 '+formData.dbinstance + ' 和数据库的记录对应id不一致');
                            } else if(data == 2) {
                                bootbox.alert('更新 ' + formData.dbinstance + ' 失败2');
                            } else if(data == 3) {
                                bootbox.alert('更新 ' + formData.dbinstance + ' 失败3');
                            } else {
                                bootbox.alert('实例参数 ' + formData.dbinstance + ' 更新成功', function () {
                                    bootbox.hideAll();
                                    $dsTable.bootstrapTable('refresh');
                                });
                            }
                        },
                        complete: function () {
                            $.unblockUI();
                        }
                    });
                    return false;
                }
            }
        }
    }).on('shown.bs.modal', function () {
        // bindDsValidator();
        // $('#ds-form').find(':password').password();
    });
}

function dsDelete() {
    var id = $(this).parents('tr[data-uniqueid]').data('uniqueid');
    bootbox.confirm('你确定要删除该数据源部门映射记录吗？', function(result) {
        if (result) {
            $.blockUI({message: ""});
            $.ajax({
                url: base_path + '/datasource/parameters/' + id + '/del',
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

function dsActive() {
    var id = $(this).parents('tr[data-uniqueid]').data('uniqueid');
    bootbox.confirm('你确定要启用实例参数吗？', function(result) {
        if (result) {
            $.blockUI({message: ""});
            $.ajax({
                url: base_path + '/datasource/parameters/' + id + '/active',
                success: function (request) {
                    if(request.code == 0){
                        bootbox.alert(request.data);
                    }else{
                        bootbox.alert("调用失败:<br/>"+request.error);
                    }
                },
                complete: function () {
                    $.unblockUI();
                }
            });
        }
    });
}

function dsStates() {
    var id = $(this).parents('tr[data-uniqueid]').data('uniqueid');
    $.blockUI({message: ""});
    $.ajax({
        url: base_path + '/datasource/parameters/' + id + '/states',
        success: function (result) {
            if(result.code == 0){
                var viewItems = '<div>';
                for(var item in result.data){
                    viewItems += '<li> parameterName='+result.data[item].parameterName+'</li>';
                    viewItems += '<li> parameterValue='+result.data[item].parameterValue+'</li>';
                    viewItems += '<li> parameterDescription='+result.data[item].parameterDescription+'</li>';
                }
                viewItems += '</div>';
                bootbox.alert("查询状态成功:<br/>"+viewItems);
            }else{
                bootbox.alert("查询状态失败:<br/>"+result.error);
            }
        },
        complete: function () {
            $.unblockUI();
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
        url: base_path + '/datasource/parameters/' + id,
        success: function (result) {
            data = result;
            data.parameters = JSON.parse(result.parameters);//转成对象
            data['type_' + data.type] = true;
            data['env_' + data.env] = true;//todo: delete
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
    var children = $('#params').children();
    // var divList = children.find("div");
    var parametersMap = {};
    var parametersStr = '';
    var realInputs = children.children();
    var length = realInputs.size();
    for(var index = 0; index < length;index++){
        var name = realInputs.get(index).value;
        index = index + 1;
        var value = realInputs.get(index).value;
        if (name == null || value==null || name.replace(/(^\s*)|(\s*$)/g, "").length ==0 || value.replace(/(^\s*)|(\s*$)/g, "").length ==0)
        {
            continue;
            //nothing to do
        }else{
            parametersMap[name] = value;
        }
    }

    parametersStr = JSON.stringify(parametersMap);

    var data = {
        id: $ds.find('[name="id"]').val(),
        dbinstance: $ds.find('[name="dbinstance"]').val(),

        username: $ds.find('[name="username"]').val(),
        passwd: $ds.find('[name="passwd"]').val(),
        parametersMap: parametersMap,
        parameters: parametersStr

        // env: $ds.find('[name="env"]').val(), //todo : 添加环境字段
    };
    return data;
}

window.paramsFormatter = function(value, row, index) {
    var codestr = '';
    for(var key in value){
        codestr += '<li title="' + key + '">' + key + ' = ' + value[key]  + '</li>'
    }
    return '<ul>'+codestr + '</ul>';
};

// table formatter
window.opFormatter = function(value, row, index) {
    return [
        '<a name="modify" class="text-success" href="javascript:;" title="修改"><i class="fa fa-pencil-square-o fa-fw"></i></a>',
        '<a name="delete" class="text-danger" href="javascript:;" title="删除"><i class="fa fa-trash-o fa-fw"></i></a>',
        '<a name="active" class="text-warning" href="javascript:;" title="启用"><i class="fa fa-check fa-fw"></i></a>',
        '<a name="states" class="text-success" href="javascript:;" title="状态"><i class="fa fa-signal fa-fw"></i></a>'
    ].join(' ');
};