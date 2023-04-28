package com.autodb.ops.dms.entity.datasource;

import com.autodb.ops.dms.common.JSON;
import com.google.common.collect.Maps;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Map;

@Data
public class InstanceParameters {
    private Integer id;

    @NotNull
    @Size(min = 1)
    private String dbinstance;

    @Size(min = 0)
    private String username;

    @Size(min = 0)
    private String passwd;

    @Size(min = 1)
    private String parameters;

    private Map<String,Object> parametersMap;

    public void convertParametersToMap(){
        parametersMap = JSON.parseObject(parameters,Map.class);
    }

    public void convertMapToParameters(){
        parameters = JSON.objectToString(parametersMap);
    }
}
