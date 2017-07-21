package org.learning;

import org.apache.hadoop.hive.metastore.HiveMetaStore;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by anderson on 17-7-10.
 */
public class TestTable
{
    public  static HiveMetaStore.Client getThriftConnectedClient() throws SQLException
    {
        HiveMetaStore.Client client = null;
        if (client == null) {
            TTransport transport = null;
            transport = new TSocket("192.168.177.77", 9083);
            TProtocol protocol = new TBinaryProtocol(transport);
            try {
                transport.open();
                System.out.println("------------- isOpen:" + transport.isOpen());
            } catch (TTransportException e) {
                throw new SQLException("Could not establish connecton to: " + e.getMessage(), "08S01");
            }
            client = new HiveMetaStore.Client(protocol);

//            transport.close();
        }
        return client;
    }

    public static void closeClient(HiveMetaStore.Client client) {
        client.getInputProtocol().getTransport().close();
        client.getOutputProtocol().getTransport().close();
    }

    public static void main(String[] args)
            throws TException, SQLException
    {
        HiveMetaStore.Client client = getThriftConnectedClient();
        Table table = client.get_table("datajingdo_m_002", "partition_test");
        // 只会获取到表的字段(不包括分区字段): member_id 和 name
        List<FieldSchema> list =  table.getSd().getCols();
        for (FieldSchema fieldSchema : list) {
            System.out.println(fieldSchema.getName());
        }

        // 输出分区字段: stat_date 和  province
        List<FieldSchema> partitionKeys =  table.getPartitionKeys();
        for (FieldSchema fieldSchema : partitionKeys) {
            System.out.println(fieldSchema.getName());
        }
    }
}
