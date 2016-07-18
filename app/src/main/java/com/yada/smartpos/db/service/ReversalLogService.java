package com.yada.smartpos.db.service;

import com.yada.smartpos.model.ReversalLog;
import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.List;

public class ReversalLogService {

    private DbManager dbManager;

    public ReversalLogService(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    public void save(ReversalLog reversalLog) throws DbException {
        dbManager.save(reversalLog);
    }

    public void deleteById(int id) throws DbException {
        dbManager.deleteById(ReversalLog.class, id);
    }

    public void delete() throws DbException {
        dbManager.delete(ReversalLog.class);
    }

    public List<ReversalLog> find() throws DbException {
        List<ReversalLog> reversalLogs = dbManager.findAll(ReversalLog.class);
        if (null == reversalLogs)
            reversalLogs = new ArrayList<>();
        return reversalLogs;
    }
}
