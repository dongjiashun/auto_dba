package com.autodb.ops.dms.aop;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.common.exception.ExCode;
import org.apache.ibatis.exceptions.PersistenceException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.stereotype.Component;

/**
 * Translate Mybatis Exception to AppException</br>
 * pointcut extends SuperDao
 * @author dongjs
 * @since 2015/11/10
 */
@Aspect
@Component
public class MybatisExceptionTranslator {
    @Around("this(com.autodb.ops.dms.repository.SuperDao)")
    public Object convertException(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            throw handException(throwable);
        }
    }

    /**
     * 处理mybatis操作数据库的异常
     * @param throwable mybatis操作数据库发生的异常
     */
    protected Throwable handException(Throwable throwable) {
        if (throwable instanceof MyBatisSystemException) {
            return new AppException(ExCode.DB_001, throwable.getCause());
        } else if (throwable instanceof PersistenceException) {
            return new AppException(ExCode.DB_001, throwable);
        } else {
            return throwable;
        }
    }
}
