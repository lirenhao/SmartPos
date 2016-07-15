package com.yada.smartpos.db.service;

import com.yada.smartpos.model.TransLog;
import org.xutils.DbManager;
import org.xutils.ex.DbException;

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

    public TransLog findById(String traceNo) throws DbException {
        return dbManager.findById(TransLog.class, traceNo);
    }
}
