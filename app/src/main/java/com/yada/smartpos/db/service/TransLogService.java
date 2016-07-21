package com.yada.smartpos.db.service;

import com.yada.smartpos.model.TransLog;
import org.xutils.DbManager;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.List;

public class TransLogService {

    private DbManager dbManager;

    public TransLogService(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    public void save(TransLog transLog) throws DbException {
        dbManager.save(transLog);
    }

    public void deleteById(String traceNo) throws DbException {
        dbManager.deleteById(TransLog.class, traceNo);
    }

    public void deleteByBatchNo(String batchNo) throws DbException {
        dbManager.delete(TransLog.class, WhereBuilder.b("batchNo", "=", batchNo));
    }

    public TransLog findById(String traceNo) throws DbException {
        return dbManager.findById(TransLog.class, traceNo);
    }

    public List<TransLog> findByBatchNo(String batchNo) throws DbException {
        List<TransLog> transLogs = dbManager.selector(TransLog.class).and("batchNo", "=", batchNo).findAll();
        if(null == transLogs)
            transLogs = new ArrayList<>();
        return transLogs;
    }

    public List<TransLog> findAll() throws DbException {
        List<TransLog> transLogs = dbManager.findAll(TransLog.class);
        if(null == transLogs)
            transLogs = new ArrayList<>();
        return transLogs;
    }
}
