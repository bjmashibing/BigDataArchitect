package com.mashibing.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.hadoop.conf.Configuration;

import com.mashibing.common.GlobalConstants;

/**
 * jdbc管理
 * 
 * @author 马士兵教育
 *
 */
public class JdbcManager {
    /**
     * 根据配置获取获取关系型数据库的jdbc连接
     * 
     * @param conf
     *            hadoop配置信息
     * @param flag
     *            区分不同数据源的标志位
     * @return
     * @throws SQLException
     */
    public static Connection getConnection(Configuration conf, String flag) throws SQLException {
        String driverStr = String.format(GlobalConstants.JDBC_DRIVER, flag);
        String urlStr = String.format(GlobalConstants.JDBC_URL, flag);
        String usernameStr = String.format(GlobalConstants.JDBC_USERNAME, flag);
        String passwordStr = String.format(GlobalConstants.JDBC_PASSWORD, flag);

        String driverClass = conf.get(driverStr);
        String url = conf.get(urlStr);
        String username = conf.get(usernameStr);
        String password = conf.get(passwordStr);
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            // nothing
        }
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * 关闭数据库连接
     * 
     * @param conn
     * @param stmt
     * @param rs
     */
    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                // nothing
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                // nothing
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // nothing
            }
        }
    }
}
