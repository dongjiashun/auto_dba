package com.autodb.ops.dms.domain.datasource.observer;

import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.common.exception.ExCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Observable;
import java.util.Observer;

/**
 * DataSource change Observer
 * @author dongjs
 * @since 16/4/22
 */
public abstract class DataSourceObserver implements Observer {
    private static Logger logger = LoggerFactory.getLogger(DataSourceObserver.class);

    @Override
    public void update(Observable o, Object arg) {
        try {
            if (arg != null && arg instanceof DataSourceChange) {
                update((DataSourceChange) arg);
            }
        } catch (Exception e) {
            if (ignoreException()) {
                logger.info("ignore exception: {}", e.getMessage());
            } else {
                throw new AppException(ExCode.SYS_001, e);
            }
        }
    }

    public abstract void update(DataSourceChange dataSourceChange);

    protected abstract boolean ignoreException();
}
