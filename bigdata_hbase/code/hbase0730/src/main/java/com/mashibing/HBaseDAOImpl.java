package com.mashibing;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.CompareOperator;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HBaseDAOImpl {

    static Configuration conf = null;
    Connection conn = null;

    public HBaseDAOImpl() {
        conf = new Configuration();
        String zk_list = "node02,node03,node04";
        conf.set("hbase.zookeeper.quorum", zk_list);
        try {
            conn = ConnectionFactory.createConnection(conf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save(Put put, String tableName) {
        // TODO Auto-generated method stub
        Table table = null;
        try {
            table = conn.getTable(TableName.valueOf(tableName)) ;
            table.put(put) ;

        } catch (Exception e) {
            e.printStackTrace() ;
        }finally{
            try {
                table.close() ;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 插入put集合
     * @param Put
     * @param tableName
     */
    public void save(List<Put> Put, String tableName) {
        // TODO Auto-generated method stub
        Table table = null;
        try {
            table = conn.getTable(TableName.valueOf(tableName)) ;
            table.put(Put) ;
        }
        catch (Exception e) {
            e.printStackTrace();
        }finally
        {
            try {
                table.close() ;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    /**
     * 插入一个cell
     *
     * @param tableName
     * @param rowKey
     * @param family
     * @param quailifer
     * @param value
     */
    public void insert(String tableName, String rowKey, String family,
                       String quailifer, String value) {
        Table table = null;
        try {
            table = conn.getTable(TableName.valueOf(tableName));
            Put put = new Put(rowKey.getBytes());
            put.addColumn(family.getBytes(), quailifer.getBytes(), value.getBytes());
            table.put(put);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                table.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 在一个列族下插入多个单元格
     *
     * @param tableName
     * @param rowKey
     * @param family
     * @param quailifer
     * @param value
     */
    public void insert(String tableName, String rowKey, String family, String quailifer[], String value[]) {
        Table table = null;
        try {
            table = conn.getTable(TableName.valueOf(tableName));
            Put put = new Put(rowKey.getBytes());
            // 批量添加
            for (int i = 0; i < quailifer.length; i++) {
                String col = quailifer[i];
                String val = value[i];
                put.addColumn(family.getBytes(), col.getBytes(), val.getBytes());
            }
            table.put(put);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                table.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据rowkey获取表中的一行数据
     *
     * @param tableName
     * @param rowKey
     * @return
     */
    public Result getOneRow(String tableName, String rowKey) {
        Table table = null;
        Result rsResult = null;
        try {
            table = conn.getTable(TableName.valueOf(tableName));
            Get get = new Get(rowKey.getBytes());
            rsResult = table.get(get);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return rsResult;
    }

    /**
     * 最常用的方法，优化查询
     * 查询一行数据，
     *
     * @param tableName
     * @param rowKey
     * @param cols
     * @return
     */
    public Result getOneRowAndMultiColumn(String tableName, String rowKey, String[] cols) {
        // TODO Auto-generated method stub
        Table table = null;
        Result rsResult = null;
        try {
            table = conn.getTable(TableName.valueOf(tableName));
            Get get = new Get(rowKey.getBytes());
            for (int i = 0; i < cols.length; i++) {
                get.addColumn("cf".getBytes(), cols[i].getBytes());
            }
            rsResult = table.get(get);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return rsResult;
    }

    /**
     * 根据rowkey进行前缀匹配
     *
     * @param tableName
     * @param rowKeyLike
     * @return
     */
    public List<Result> getRows(String tableName, String rowKeyLike) {
        Table table = null;
        List<Result> list = null;
        try {
            FilterList fl = new FilterList(FilterList.Operator.MUST_PASS_ALL);
            table = conn.getTable(TableName.valueOf(tableName));
            PrefixFilter filter = new PrefixFilter(rowKeyLike.getBytes());
            SingleColumnValueFilter filter1 = new SingleColumnValueFilter(
                    "order".getBytes(),
                    "order_type".getBytes(),
                    CompareOperator.EQUAL,
                    Bytes.toBytes("1")
            );
            fl.addFilter(filter);
            fl.addFilter(filter1);
            Scan scan = new Scan();
            scan.setFilter(fl);
            ResultScanner scanner = table.getScanner(scan);
            list = new ArrayList<Result>();
            for (Result rs : scanner) {
                list.add(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    /**
     * 根据前缀匹配rowkey且获取部分列数据
     *
     * @param tableName
     * @param rowKeyLike
     * @param cols
     * @return
     */
    public List<Result> getRows(String tableName, String rowKeyLike, String cols[]) {
        // TODO Auto-generated method stub
        Table table = null;
        List<Result> list = null;
        try {
            table = conn.getTable(TableName.valueOf(tableName));
            PrefixFilter filter = new PrefixFilter(rowKeyLike.getBytes());

            Scan scan = new Scan();
            for (int i = 0; i < cols.length; i++) {
                scan.addColumn("cf".getBytes(), cols[i].getBytes());
            }
            scan.setFilter(filter);
            ResultScanner scanner = table.getScanner(scan);
            list = new ArrayList<Result>();
            for (Result rs : scanner) {
                list.add(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    /**
     * 范围查询
     *
     * @param tableName
     * @param startRow
     * @param stopRow
     * @return
     */
    public List<Result> getRows(String tableName, String startRow, String stopRow) {
        Table table = null;
        List<Result> list = null;
        try {
            table = conn.getTable(TableName.valueOf(tableName));
            Scan scan = new Scan();
            scan.withStartRow(startRow.getBytes());
            scan.withStopRow(stopRow.getBytes());
            ResultScanner scanner = table.getScanner(scan);
            list = new ArrayList<Result>();
            for (Result rsResult : scanner) {
                list.add(rsResult);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    /**
     * 根据rowkey删除数据
     *
     * @param tableName
     * @param rowKeyLike
     */
    public void deleteRecords(String tableName, String rowKeyLike) {
        Table table = null;
        try {
            table = conn.getTable(TableName.valueOf(tableName));
            PrefixFilter filter = new PrefixFilter(rowKeyLike.getBytes());
            Scan scan = new Scan();
            scan.setFilter(filter);
            ResultScanner scanner = table.getScanner(scan);
            List<Delete> list = new ArrayList<Delete>();
            for (Result rs : scanner) {
                Delete del = new Delete(rs.getRow());
                list.add(del);
            }
            table.delete(list);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 删除cell
     *
     * @param tableName
     * @param rowkey
     * @param cf
     * @param column
     */
    public void deleteCell(String tableName, String rowkey, String cf, String column) {
        Table table = null;
        try {
            table = conn.getTable(TableName.valueOf(tableName));
            Delete del = new Delete(rowkey.getBytes());
            del.addColumn(cf.getBytes(), column.getBytes());
            table.delete(del);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 创建表
     * @param tableName
     * @param columnFamilys
     */
    public void createTable(String tableName, String[] columnFamilys) {
        try {
            // admin 对象
            Admin admin = conn.getAdmin();
            if (admin.tableExists(TableName.valueOf(tableName))) {
                System.err.println("此表，已存在！");
            } else {
                TableDescriptorBuilder tableDesc = TableDescriptorBuilder.newBuilder(
                        TableName.valueOf(tableName));

                for (String columnFamily : columnFamilys) {
                    tableDesc.setColumnFamily(ColumnFamilyDescriptorBuilder.newBuilder(columnFamily.getBytes()).build());
                }

                admin.createTable(tableDesc.build());
                System.err.println("建表成功!");

            }
            admin.close();// 关闭释放资源
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 删除一个表
     *
     * @param tableName 删除的表名
     */
    public void deleteTable(String tableName) {
        TableName tn = TableName.valueOf(tableName);
        try {
            Admin admin = conn.getAdmin();
            if (admin.tableExists(tn)){
                admin.disableTable(tn);// 禁用表
                admin.deleteTable(tn);// 删除表
                System.err.println("删除表成功!");
            } else {
                System.err.println("删除的表不存在！");
            }
            admin.close();
        }  catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询表中所有行
     *
     * @param tablename
     */
    public void scaner(String tablename) {
        try {
            Table table = conn.getTable(TableName.valueOf(tablename));
            Scan s = new Scan();
//	        s.addColumn(family, qualifier)
//	        s.addColumn(family, qualifier)
            ResultScanner rs = table.getScanner(s);
            for (Result r : rs) {

                for (Cell cell : r.rawCells()) {
                    System.out.println("RowName:" + new String(CellUtil.cloneRow(cell)) + " ");
                    System.out.println("Timetamp:" + cell.getTimestamp() + " ");
                    System.out.println("column Family:" + new String(CellUtil.cloneFamily(cell)) + " ");
                    System.out.println("row Name:" + new String(CellUtil.cloneQualifier(cell)) + " ");
                    System.out.println("value:" + new String(CellUtil.cloneValue(cell)) + " ");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 筛选表的部分列
     * @param tablename
     */
    public void scanerByColumn(String tablename) {

        try {
            Table table =conn.getTable(TableName.valueOf(tablename));
            Scan s = new Scan();
            s.addColumn("cf".getBytes(), "201504052237".getBytes());
            s.addColumn("cf".getBytes(), "201504052237".getBytes());
            ResultScanner rs = table.getScanner(s);
            for (Result r : rs) {
                for (Cell cell : r.rawCells()) {
                    System.out.println("RowName:" + new String(CellUtil.cloneRow(cell)) + " ");
                    System.out.println("Timetamp:" + cell.getTimestamp() + " ");
                    System.out.println("column Family:" + new String(CellUtil.cloneFamily(cell)) + " ");
                    System.out.println("row Name:" + new String(CellUtil.cloneQualifier(cell)) + " ");
                    System.out.println("value:" + new String(CellUtil.cloneValue(cell)) + " ");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        HBaseDAOImpl dao = new HBaseDAOImpl();

//		创建表
//	    String tableName="test";
//		String cfs[] = {"cf"};
//		dao.createTable(tableName,cfs);

//		存入一条数据
//		Put put = new Put("msb".getBytes());
//		put.addColumn("cf".getBytes(), "name".getBytes(), "cai10".getBytes()) ;
//		dao.save(put, "test") ;

//		插入多列数据
//		Put put = new Put("msb".getBytes());
//		List<Put> list = new ArrayList<Put>();
//		put.addColumn("cf".getBytes(), "addr".getBytes(), "shanghai1".getBytes()) ;
//		put.addColumn("cf".getBytes(), "age".getBytes(), "30".getBytes()) ;
//		put.addColumn("cf".getBytes(), "tel".getBytes(), "13889891818".getBytes()) ;
//		list.add(put) ;
//		dao.save(list, "test");

//		插入单行数据
//		dao.insert("test", "testrow", "cf", "age", "35") ;
//		dao.insert("test", "testrow", "cf", "cardid", "12312312335") ;
//		dao.insert("test", "testrow", "cf", "tel", "13512312345") ;
//
//
//		List<Result> list = dao.getRows("test", "testrow",new String[]{"age"}) ;
//		for(Result rs : list)
//		{
//		    for(Cell cell:rs.rawCells()){
//		        System.out.println("RowName:"+new String(CellUtil.cloneRow(cell))+" ");
//		        System.out.println("Timetamp:"+cell.getTimestamp()+" ");
//		        System.out.println("column Family:"+new String(CellUtil.cloneFamily(cell))+" ");
//		        System.out.println("row Name:"+new String(CellUtil.cloneQualifier(cell))+" ");
//		        System.out.println("value:"+new String(CellUtil.cloneValue(cell))+" ");
//		       }
//		}

//		Result rs = dao.getOneRow("test", "testrow");
//		 System.out.println(new String(rs.getValue("cf".getBytes(), "age".getBytes())));

//		Result rs = dao.getOneRowAndMultiColumn("test", "testrow", new String[]{"age","cardid"});
//		for(Cell cell:rs.rawCells()){
//	        System.out.println("RowName:"+new String(CellUtil.cloneRow(cell))+" ");
//	        System.out.println("Timetamp:"+cell.getTimestamp()+" ");
//	        System.out.println("column Family:"+new String(CellUtil.cloneFamily(cell))+" ");
//	        System.out.println("row Name:"+new String(CellUtil.cloneQualifier(cell))+" ");
//	        System.out.println("value:"+new String(CellUtil.cloneValue(cell))+" ");
//	       }

		dao.deleteTable("test");
    }
}

