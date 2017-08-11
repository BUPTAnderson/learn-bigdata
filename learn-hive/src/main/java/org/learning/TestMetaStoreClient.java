package org.learning;

import org.apache.hadoop.hive.metastore.HiveMetaStore;
import org.apache.hadoop.hive.metastore.api.Database;
import org.apache.hadoop.hive.metastore.api.PrincipalType;
import org.apache.hadoop.hive.metastore.api.PrivilegeGrantInfo;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.sql.SQLException;

/**
 * Created by anderson on 17-6-19.
 */
public class TestMetaStoreClient
{
    public  static HiveMetaStore.Client GetThriftConnectedClient() throws SQLException
    {
        HiveMetaStore.Client client = null;
        if (client == null) {
            TTransport transport = null;
            transport = new TSocket("192.168.177.79", 9083);
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

    public static void createInstance(String instanceName, String desc, String ownerName, PrincipalType principalType)
            throws TException, SQLException
    {
//        HiveMetaStore.Client client = GetThriftConnectedClient();
//        Instance instance = new Instance();
//        instance.setName(instanceName);
//        instance.setDescription(desc);
//        instance.setOwnerName(ownerName);
//        instance.setOwnerType(principalType);
//        client.create_instance(instance);
//        closeClient(client);
    }

    public static void getInstance(String instanceName, String ownerName, PrincipalType ownerType)
            throws SQLException, TException
    {
//        HiveMetaStore.Client client = GetThriftConnectedClient();
//        Instance instance = client.get_instance(instanceName, ownerName, ownerType);
//        System.out.println(instance);
//        closeClient(client);
    }

    public static void getInstanceList(String principalName, PrincipalType principalType)
            throws SQLException, TException
    {
//        HiveMetaStore.Client client = GetThriftConnectedClient();
//        List<Instance> list = client.get_instance_list(principalName, principalType);
//        System.out.println(list.size());
//        for (Instance instance : list) {
//            System.out.println(instance);
//        }
//        closeClient(client);
    }

    public static void grantInstancePrivs(String instanceName, String instanceOwnerName, PrincipalType instanceOwnerType,
            String principalName, PrincipalType principalType, PrivilegeGrantInfo info)
            throws SQLException, TException
    {
//        HiveMetaStore.Client client = GetThriftConnectedClient();
//        Instance instance = new Instance();
//        instance.setName(instanceName);
//        instance.setOwnerName(instanceOwnerName);
//        instance.setOwnerType(instanceOwnerType);
//        boolean b = client.grant_instance_privileges(instance, principalName, principalType, info);
//        System.out.println(b);
//        closeClient(client);
    }

//    public static void revokeInstancePrivs(String instanceName, String instanceOwnerName, PrincipalType instanceOwnerType,
//            String principalName, PrincipalType principalType, PrivilegeGrantInfo info) throws SQLException, TException
//    {
//        HiveMetaStore.Client client = GetThriftConnectedClient();
//        Instance instance = new Instance();
//        instance.setName(instanceName);
//        instance.setOwnerName(instanceOwnerName);
//        instance.setOwnerType(instanceOwnerType);
//        instance.setDescription("");
//        boolean b = client.revoke_instance_privileges(instance, principalName, principalType, info);
//        System.out.println(b);
//        closeClient(client);
//    }

    public static void createDatabase(Database db)
            throws SQLException, TException
    {
        HiveMetaStore.Client client = GetThriftConnectedClient();
        client.create_database(db);
        closeClient(client);
    }

//    public static void getDatabase(Instance instance, String dbName)
//            throws SQLException, TException
//    {
//        HiveMetaStore.Client client = GetThriftConnectedClient();
//        Database database = client.get_db(instance, dbName);
//        System.out.println(database);
//        closeClient(client);
//    }

//    public static void getDatabaseList(Instance instance, String principalName, PrincipalType principalType)
//            throws SQLException, TException
//    {
//        HiveMetaStore.Client client = GetThriftConnectedClient();
//        List<String> list = client.get_database_list(instance, principalName, principalType);
//
//        for (String dbName : list) {
//            System.out.println(dbName);
//        }
//        closeClient(client);
//    }

//    public static void getDatabaseEntityList(Instance instance, String principalName, PrincipalType principalType)
//            throws SQLException, TException
//    {
//        HiveMetaStore.Client client = GetThriftConnectedClient();
//        List<Database> list = client.get_database_entity_list(instance, principalName, principalType);
//
//        for (Database database : list) {
//            System.out.println(database);
//        }
//        closeClient(client);
//    }

    public static void createTable(Table table)
            throws SQLException, TException
    {
        HiveMetaStore.Client client = GetThriftConnectedClient();
        client.create_table(table);
        closeClient(client);
    }

    public static void dropDB(Database database)
            throws TException, SQLException
    {
        HiveMetaStore.Client client = GetThriftConnectedClient();
//        client.drop_db(database, true, true);
        closeClient(client);
    }

    public static void main(String[] args)
            throws TException, SQLException
    {
//        Instance instance = new Instance();
//        instance.setName("default");
//        instance.setOwnerName("public");
//        instance.setOwnerType(PrincipalType.ROLE);
        String principalName = "datajingdo_m";
        PrincipalType principalType = PrincipalType.USER;
//        getDatabaseEntityList(instance, principalName, principalType);

//        createInstance("test", "instance for test", "datajingdo_m", PrincipalType.USER);
//        createInstance("test", "instance for test", "hadoop", PrincipalType.USER);
//        getInstance("default", "datajingdo_m", PrincipalType.USER);
//        try {
//            getInstanceList("datajingdo_m", PrincipalType.USER);
//        } catch (TTransportException e) {
//            System.out.println(e.getMessage());
//            e.printStackTrace();
//        }
//        info.setGrantOption(false);
//        info.setGrantorType(PrincipalType.USER);
//        info.setGrantor("hadoop");
//        info.setPrivilege("select");
//        info.setCreateTime((int) System.currentTimeMillis()/1000);
//        grantInstancePrivs("test", "hadoop", PrincipalType.USER, "datajingdo_m", PrincipalType.USER, info);
//        revokeInstancePrivs("test", "hadoop", PrincipalType.USER, "datajingdo_m", PrincipalType.USER, info);

        //--------------- CREATE DATABASE -------------
//        Database db = new Database();
//        db.setName("db2");
//        db.setOwnerName("hadoop");
//        db.setOwnerType(PrincipalType.USER);
//        db.setDescription("create db2 for test");
//        db.setInstanceName("test");
//        db.setInstanceOwnerName("hadoop");
//        db.setInstanceOwnerType(PrincipalType.USER);
//        createDatabase(db);

//        getDatabase(instance, "default");

        //------------------ get database list -------------------
//        getDatabaseList(instance, principalName, PrincipalType.USER);

//        Table table = new Table();
//        table.setInstanceName("default");
//        table.setInstanceOwnerName("datajingdo_m");
//        table.setInstanceOwnerType(PrincipalType.USER);
//        table.setDbName("ysm_test");
//        table.setTableType(TableType.MANAGED_TABLE.name());
//        table.setTableName("test");
//        table.setOwner("datajingdo_m");
//        table.setSd(StorageDescriptor)
//        createTable();
//        System.out.println(((int) (System.currentTimeMillis() / 1000)));
//        Database database = new Database();
//        database.setName("ysm_test");
//        database.setInstanceName("yaf");
//        database.setInstanceOwnerName("datajingdo_m");
//        database.setInstanceOwnerType(PrincipalType.USER);
//        dropDB(database);
    }
}
