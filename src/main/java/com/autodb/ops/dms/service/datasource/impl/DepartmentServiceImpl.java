package com.autodb.ops.dms.service.datasource.impl;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.common.exception.ExCode;
import com.autodb.ops.dms.domain.datasource.DataSourceEncryptUtils;
import com.autodb.ops.dms.entity.datasource.Department;
import com.autodb.ops.dms.repository.datasource.DepartmentDao;
import com.autodb.ops.dms.service.datasource.DepartmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {
    private static Logger logger = LoggerFactory.getLogger(DepartmentServiceImpl.class);

    @Autowired
    private DepartmentDao departmentDao;

    @Override
    public Department find(int id) throws AppException {
        Department department = departmentDao.find(id);
        decryptPW(department);
        return department;
    }

    @Override
    public List<Department> findAll() throws AppException {
        return this.departmentDao.findAll();
    }

    @Override
    public List<Department> findByEnv(String env) throws AppException {
        return departmentDao.findByEnv(env);
    }

    @Override
    public int add(Department department) throws AppException {
        encryptPW(department);
        departmentDao.add(department);
        return 0;
    }

    @Override
    public int update(Department department) throws AppException {
        Department dp = departmentDao.findByDbnameDbinstance(department.getDbname(), department.getDbinstance());
        if (dp != null && !dp.getId().equals(department.getId())) {
            return 1;
        }
        encryptPW(department);
        boolean success = departmentDao.update(department);
        if(!success)
            return 1;
        return 0;
    }

    @Override
    public boolean delete(int id) throws AppException {
        boolean delete = false;
        Department dataSource = this.departmentDao.find(id);
        if (dataSource != null) {
            delete = this.departmentDao.delete(id);
        }
        return delete;
    }

    private void encryptPW(Department department){
        try {
            //加密的是密码，key是实例和用户名
            String newPW = DataSourceEncryptUtils.encrypt(department.getPasswd(), department.getDbinstance() + department.getUsername());
            department.setPasswd(newPW);//设置db的加密密码
        }catch (Exception exp){
//      //      throw new AppException(ExCode.SYS_003, exp);
            //nothing to do
        }

    }

    /**
     * 单个查询返回明文密码
     * @param department
     */
    private void decryptPW(Department department){
        try {
            //解密的是密码，key是实例和用户名
            String newPW = DataSourceEncryptUtils.decrypt(department.getPasswd(), department.getDbinstance() + department.getUsername());
            department.setPasswd(newPW);//设置db的加密密码
        }catch (Exception exp){
            //nothing to do
//            throw new AppException(ExCode.SYS_003, exp);
        }
    }
}
