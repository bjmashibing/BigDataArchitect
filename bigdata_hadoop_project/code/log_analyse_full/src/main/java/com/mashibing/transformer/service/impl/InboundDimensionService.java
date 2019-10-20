package com.mashibing.transformer.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;

import com.mashibing.common.GlobalConstants;
import com.mashibing.util.JdbcManager;

/**
 * 操作dimension_inbound表的服务提供类
 * 
 * @author 马士兵教育
 *
 */
public class InboundDimensionService {
    // 全部的inbound id
    public static final int ALL_OF_INBOUND_ID = 1;
    // 其他外链的inbound id
    public static final int OTHER_OF_INBOUND_ID = 2;

    /**
     * 获取数据库中dimension_inbound表url和id的映射关系
     * 
     * @param conf
     * @param type
     * @return
     * @throws SQLException
     */
    public static Map<String, Integer> getInboundByType(Configuration conf, int type) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // 获取数据库连接
            conn = JdbcManager.getConnection(conf, GlobalConstants.WAREHOUSE_OF_REPORT);
            pstmt = conn.prepareStatement("SELECT `id`,`url` FROM  `dimension_inbound` WHERE `type`=?");
            pstmt.setInt(1, type);
            rs = pstmt.executeQuery();
            Map<String, Integer> result = new HashMap<String, Integer>();
            // 处理返回结果
            while (rs.next()) {
                int id = rs.getInt("id");
                String url = rs.getString("url");
                result.put(url, id);
            }
            return result;
        } finally {
            // 关闭资源
            JdbcManager.close(conn, null, null);
        }
    }
}
