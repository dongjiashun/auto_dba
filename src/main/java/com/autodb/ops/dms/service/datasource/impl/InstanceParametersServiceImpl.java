package com.autodb.ops.dms.service.datasource.impl;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.common.exception.ExCode;
import com.autodb.ops.dms.domain.datasource.DataSourceEncryptUtils;
import com.autodb.ops.dms.entity.datasource.InstanceParameters;
import com.autodb.ops.dms.repository.datasource.InstanceParametersDao;
import com.autodb.ops.dms.service.datasource.InstanceParametersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstanceParametersServiceImpl implements InstanceParametersService {
    private static Logger logger = LoggerFactory.getLogger(InstanceParametersServiceImpl.class);

    @Autowired
    private InstanceParametersDao instanceParametersDao;


    @Override
    public InstanceParameters find(int id) throws AppException {
        InstanceParameters instanceParameters = instanceParametersDao.find(id);
        decryptPW(instanceParameters);
        return instanceParameters;
    }

    @Override
    public List<InstanceParameters> findAll() throws AppException {
        return this.instanceParametersDao.findAll();
    }

    @Override
    public List<InstanceParameters> findByEnv(String env) throws AppException {
        return instanceParametersDao.findByEnv(env);
    }

    @Override
    public int add(InstanceParameters instanceParameters) throws AppException {
        encryptPW(instanceParameters);
        instanceParametersDao.add(instanceParameters);
        return 0;
    }

    @Override
    public int update(InstanceParameters instanceParameters) throws AppException {
        InstanceParameters dp = instanceParametersDao.findByDbinstance(instanceParameters.getDbinstance());
        if (dp != null && !dp.getId().equals(instanceParameters.getId())) {
            return 1;
        }
        encryptPW(instanceParameters);
        boolean success = instanceParametersDao.update(instanceParameters);
        if(!success)
            return 1;
        return 0;
    }

    @Override
    public boolean delete(int id) throws AppException {
        boolean delete = false;
        InstanceParameters dataSource = this.instanceParametersDao.find(id);
        if (dataSource != null) {
            delete = this.instanceParametersDao.delete(id);
        }
        return delete;
    }

    private void encryptPW(InstanceParameters instanceParameters){
        try {
            //加密的是密码，key是实例和用户名
            String newPW = DataSourceEncryptUtils.encrypt(instanceParameters.getPasswd(), instanceParameters.getDbinstance() + instanceParameters.getUsername());
            instanceParameters.setPasswd(newPW);//设置db的加密密码
        }catch (Exception exp){
//            throw new AppException(ExCode.SYS_003, exp);
            //nothing to do
        }
    }

    /**
     * 单个查询返回明文密码
     * @param instanceParameters
     */
    private void decryptPW(InstanceParameters instanceParameters){
        try {
            //解密的是密码，key是实例和用户名
            String newPW = DataSourceEncryptUtils.decrypt(instanceParameters.getPasswd(), instanceParameters.getDbinstance() + instanceParameters.getUsername());
            instanceParameters.setPasswd(newPW);//设置db的加密密码
        }catch (Exception exp){
//            throw new AppException(ExCode.SYS_003, exp);
            //nothing to do
        }
    }
}

