package org.learning;

import org.apache.hadoop.hive.ql.exec.Utilities;
import org.apache.hadoop.hive.ql.parse.SemanticException;
import org.apache.hadoop.hive.ql.stats.StatsUtils;

/**
 * Created by anderson on 17-7-20.
 */
public class TestStatsUtils
{
    public static void main(String[] args)
            throws SemanticException
    {
        String dbName = "default";
        String tableName = "test";
        String fulname = StatsUtils.getFullyQualifiedTableName(dbName.toLowerCase(),
                tableName.toLowerCase());
        System.out.println(fulname);
//        Instance instance = new Instance("instance", "阿兰那", PrincipalType.USER);

//        System.out.println(StatsUtils.getFullyQualifiedTableName(instance.getName() + instance.getOwnerName() + instance.getOwnerType() + "." + dbName, tableName));

        String tblName = "default.tbl001";
        String[] names = Utilities.getDbTableName(tblName);
        for (String tbl : names) {
            System.out.println(tbl);
        }
    }
}
