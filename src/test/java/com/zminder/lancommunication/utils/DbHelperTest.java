package com.zminder.lancommunication.utils;

import com.zminder.lancommunication.pojo.User;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

public class DbHelperTest {
    @Test
    public void testQuerySingleObject() throws SQLException {
        System.out.println("Testing query single object...");
        String sql = "SELECT * FROM user WHERE username = ?";
        User user = DbHelper.query(sql, User.class, "root");
        if (user != null) {
            System.out.println("User found: " + user);
        } else {
            System.out.println("User not found.");
        }
    }

    @Test
    public void testQueryList() throws SQLException {
        System.out.println("Testing query list...");
        String sql = "SELECT * FROM user";
        List<User> users = DbHelper.queryList(sql, User.class);
        if (users != null && !users.isEmpty()) {
            System.out.println("Users found: " + users.size());
            users.forEach(user -> System.out.println("User: " + user.getUsername()));
        } else {
            System.out.println("No users found.");
        }
    }

    @Test
    public void testQueryScalar() throws SQLException {
        System.out.println("Testing query scalar...");
        String sql = "SELECT COUNT(*) FROM user";
        Long count = DbHelper.query(sql, new ScalarHandler<>());
        if (count != null) {
            System.out.println("User count: " + count);
        } else {
            System.out.println("Failed to get user count.");
        }
    }

    @Test
    public void testUpdate() throws SQLException {
        System.out.println("Testing update...");
        String sql = "UPDATE user SET password_hash = ? WHERE username = ?";
        int rows = DbHelper.update(sql, "newpassword", "testuser1");
        if (rows == 1) {
            System.out.println("Password updated for user: testuser1");
            String verifySql = "SELECT * FROM user WHERE username = ?";
            User user = DbHelper.query(verifySql, User.class, "testuser1");
            if (user != null && "newpassword".equals(user.getPasswordHash())) {
                System.out.println("Password verification passed for user: testuser1");
            } else {
                System.out.println("Password verification failed for user: testuser1");
            }
        } else {
            System.out.println("Password update failed for user: testuser1");
        }
    }
}
