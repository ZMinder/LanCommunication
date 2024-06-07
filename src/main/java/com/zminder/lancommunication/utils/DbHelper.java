package com.zminder.lancommunication.utils;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.sql.SQLException;
import java.util.List;

public class DbHelper {

    private static final QueryRunner RUNNER = new QueryRunner(DatabaseUtils.getDataSource());

    // 通用的查询单个对象方法
    public static <T> T query(String sql, Class<T> type, Object... params) throws SQLException {
        return RUNNER.query(sql, new BeanHandler<>(type), params);
    }

    // 通用的查询多个对象的方法
    public static <T> List<T> queryList(String sql, Class<T> type, Object... params) throws SQLException {
        return RUNNER.query(sql, new BeanListHandler<>(type), params);
    }

    // 通用的执行更新的方法（包括INSERT, UPDATE, DELETE）
    public static int update(String sql, Object... params) throws SQLException {
        return RUNNER.update(sql, params);
    }
}
