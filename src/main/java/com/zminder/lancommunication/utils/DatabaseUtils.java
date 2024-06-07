package com.zminder.lancommunication.utils;

import com.alibaba.druid.pool.DruidDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

public class DatabaseUtils {
    private static final DruidDataSource dataSource = new DruidDataSource();

    static {
        dataSource.setUrl("jdbc:mysql://localhost:3306/lan_communicate?useUnicode=true&characterEncoding=utf8&useSSL=false");
        dataSource.setUsername("root");
        dataSource.setPassword("040712");
        dataSource.setInitialSize(5); // 初始连接数
        dataSource.setMaxActive(10); // 最大连接数
        dataSource.setMinIdle(5); // 最小空闲连接数
    }

    public static DataSource getDataSource() {
        return dataSource;
    }
}
