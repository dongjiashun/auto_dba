package com.autodb.ops.dms.domain.aliyun;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.aliyuncs.rds.model.v20140815.*;
import com.dianwoba.springboot.webapi.WebApiResponse;
import com.autodb.ops.dms.common.JSON;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RdsService {
    private final static Logger log = LoggerFactory.getLogger(RdsService.class);

    private final static String ACCESS = "LTAIh6ywJkINEBl0";//阿里云分配给用户的
    private final static String SECRET = "vW5DeFkDrg2ZpoUf1I5w6C7DkmJfVR";//阿里云分配给用户的

    /**
     * 激活阿里云参数
     * @param DBInstanceId
     * @param changeParameters
     */
    public WebApiResponse active(String DBInstanceId, Map<String,String> changeParameters){
        String parametersJson = JSON.objectToString(changeParameters);

        try {
            //aliyun的sdk的请求方式
            ModifyParameterRequest request = new ModifyParameterRequest();
            request.setDBInstanceId(DBInstanceId);
            request.setParameters(parametersJson);
            IClientProfile profile= DefaultProfile
                    .getProfile("cn-hangzhou", ACCESS, SECRET);
            IAcsClient client = new DefaultAcsClient(profile);
            ModifyParameterResponse response = client.getAcsResponse(request);
            log.info("RDS请求参数="+parametersJson+" RDS返回="+JSON.objectToString(response));
            return WebApiResponse.success("调用RDS成功");
        }catch (Exception exp){
            log.info("RDS请求参数="+parametersJson+" RDS返回="+exp.getMessage());
            return WebApiResponse.error(exp.getMessage());
        }
    }

    /**
     * 查询阿里云实例的所有参数
     * @param DBInstanceId
     * @return
     */
    public WebApiResponse activeResult(String DBInstanceId){
        try {
            DescribeParametersRequest request = new DescribeParametersRequest();
            request.setDBInstanceId(DBInstanceId);
            IClientProfile profile= DefaultProfile
                    .getProfile("cn-hangzhou", ACCESS, SECRET);
            IAcsClient client = new DefaultAcsClient(profile);
            DescribeParametersResponse response = client.getAcsResponse(request);
            return WebApiResponse.success(response.getConfigParameters());
        }catch (Exception exp){
            return WebApiResponse.error(exp.getMessage());
        }
    }
}
