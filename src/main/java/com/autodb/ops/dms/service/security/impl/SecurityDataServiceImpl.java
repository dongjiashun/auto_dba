package com.autodb.ops.dms.service.security.impl;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.common.exception.ExCode;
import com.autodb.ops.dms.domain.datasource.DataSourceManager;
import com.autodb.ops.dms.domain.datasource.visitor.DatabaseVisitor;
import com.autodb.ops.dms.entity.datasource.DataSource;
import com.autodb.ops.dms.entity.security.SecurityData;
import com.autodb.ops.dms.repository.datasource.DataSourceDao;
import com.autodb.ops.dms.repository.security.SecurityDataDao;
import com.autodb.ops.dms.service.security.SecurityDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * SecurityDataServiceImpl
 *
 * @author dongjs
 * @since 16/1/28
 */
@Service
public class SecurityDataServiceImpl implements SecurityDataService {
    @Autowired
    private SecurityDataDao securityDataDao;

    @Autowired
    private DataSourceDao dataSourceDao;

    @Autowired
    private DataSourceManager dataSourceManager;

    @Override
    public List<Map<String, Object>> tableInfo(int dsId, String table) throws AppException {
        DataSource dataSource = this.dataSourceDao.find(dsId);
        List<Map<String, Object>> tableStructInfo = new ArrayList<>();
        try {
            if (dataSource != null) {
                DatabaseVisitor visitor = this.dataSourceManager.getDatabaseVisitor(dataSource.backupConnectionInfo());
                tableStructInfo = visitor.getTableStruct(table);

                table = table.toUpperCase();
                Map<String, SecurityData> securityDataMap = this.securityDataDao.find(dsId, table).stream()
                        .collect(Collectors.toMap(SecurityData::getColumn, data -> data));
                tableStructInfo.forEach(info -> {
                    Object column = info.get("COLUMN_NAME");
                    if (column != null && securityDataMap.get(column.toString().toUpperCase()) != null) {
                        info.put("STATE", true);
                    }
                });
            }
            return tableStructInfo;
        } catch (SQLException e) {
            throw new AppException(ExCode.DS_001, e);
        }
    }

    @Override
    @Transactional
    public void update(int datasource, String table, List<String> columns) throws AppException {
        DataSource dataSource = this.dataSourceDao.find(datasource);
        if (dataSource != null) {
            this.securityDataDao.delete(datasource, table);
            if (columns.size() > 0) {
                List<SecurityData> securityDataList = columns.stream()
                        .map(col -> {
                            SecurityData data = new SecurityData();
                            data.setDataSource(dataSource);
                            data.setTable(table.toUpperCase());
                            data.setColumn(col.toUpperCase());
                            data.setGmtCreate(new Date());
                            return data;
                        }).collect(Collectors.toList());
                this.securityDataDao.add(securityDataList);
            }
        }
    }

    @Override
    public List<Map<String, Object>> securityTableInfo(int dsId, String table) throws AppException {
        DataSource dataSource = this.dataSourceDao.find(dsId);
        List<Map<String, Object>> tableStructInfo = new ArrayList<>();
        try {
            if (dataSource != null) {
                String upperTable = table.toUpperCase();
                List<SecurityData> securityDataList = this.securityDataDao.find(dsId, upperTable);
                if (securityDataList.size() > 0) {
                    DatabaseVisitor visitor = this.dataSourceManager.getDatabaseVisitor(dataSource.backupConnectionInfo());
                    tableStructInfo = visitor.getTableStruct(table);

                    Map<String, SecurityData> securityDataMap = securityDataList.stream()
                            .collect(Collectors.toMap(SecurityData::getColumn, data -> data));

                    return tableStructInfo.stream().filter(info -> {
                        Object column = info.get("COLUMN_NAME");
                        SecurityData data = securityDataMap.get(column.toString().toUpperCase());
                        if (data != null) {
                            info.put("sec_id", data.getId());
                            return true;
                        } else {
                            return false;
                        }
                    }).collect(Collectors.toList());
                }
            }
            return tableStructInfo;
        } catch (SQLException e) {
            throw new AppException(ExCode.DS_001, e);
        }
    }

    @Override
    public List<String> securityTableList(int dsId) throws AppException {
        try {
            List<String> tableList = Collections.emptyList();
            List<String> tables = this.securityDataDao.findTablesByDatasource(dsId);
            if (tables.size() > 0) {
                DataSource dataSource = this.dataSourceDao.find(dsId);
                if (dataSource != null) {
                    DatabaseVisitor visitor = this.dataSourceManager.getDatabaseVisitor(dataSource.backupConnectionInfo());
                    tableList = visitor.getTableNames().stream()
                            .filter(name -> tables.contains(name.toUpperCase()))
                            .collect(Collectors.toList());
                }
            }
            return tableList;
        } catch (SQLException e) {
            throw new AppException(ExCode.DS_001, e);
        }
    }

    @Override
    public Map<String, Set<String>> findMaskData(int dsId, String username) throws AppException {
        List<SecurityData> securityDataList = this.securityDataDao.findNoAuth(dsId, username);

        HashMultimap<String, String> multimap = HashMultimap.create();
        for (SecurityData securityData : securityDataList) {
            multimap.put(securityData.getTable(), securityData.getColumn());
        }

        return Multimaps.asMap(multimap);
    }
}
