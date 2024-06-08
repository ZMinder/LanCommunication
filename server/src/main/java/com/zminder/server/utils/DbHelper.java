package com.zminder.server.utils;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.SQLException;
import java.util.List;

public class DbHelper {

    private static final QueryRunner RUNNER = new QueryRunner(DatabaseUtils.getDataSource());

    // 查询单个对象
    public static <T> T query(String sql, Class<T> type, Object... params) throws SQLException {
        return RUNNER.query(sql, new BeanHandler<>(type), params);
    }

    // 查询对象列表
    public static <T> List<T> queryList(String sql, Class<T> type, Object... params) throws SQLException {
        return RUNNER.query(sql, new BeanListHandler<>(type), params);
    }

    // 查询单个值
    public static <T> T query(String sql, ScalarHandler<T> handler, Object... params) throws SQLException {
        return RUNNER.query(sql, handler, params);
    }

    // 执行更新操作
    public static int update(String sql, Object... params) throws SQLException {
        return RUNNER.update(sql, params);
    }
}

