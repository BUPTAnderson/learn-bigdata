package org.learning;

import org.apache.hadoop.hive.common.FileUtils;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.metastore.model.MFieldSchema;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang.StringUtils.repeat;

/**
 * Created by anderson on 17-6-29.
 */
public class TestPartition
{
    public static enum Counter {
        BYTES_READ
    }

    private static final String STATS_COLLIST =
            "\"COLUMN_NAME\", \"COLUMN_TYPE\", \"LONG_LOW_VALUE\", \"LONG_HIGH_VALUE\", "
                    + "\"DOUBLE_LOW_VALUE\", \"DOUBLE_HIGH_VALUE\", \"BIG_DECIMAL_LOW_VALUE\", "
                    + "\"BIG_DECIMAL_HIGH_VALUE\", \"NUM_NULLS\", \"NUM_DISTINCTS\", \"AVG_COL_LEN\", "
                    + "\"MAX_COL_LEN\", \"NUM_TRUES\", \"NUM_FALSES\", \"LAST_ANALYZED\" ";

    public static String makePartName(List<FieldSchema> partCols,
            List<String> vals, String defaultStr) throws MetaException
    {
        if ((partCols.size() != vals.size()) || (partCols.size() == 0)) {
            String errorStr = "Invalid partition key & values; keys [";
            for (FieldSchema fs : partCols) {
                errorStr += (fs.getName() + ", ");
            }
            errorStr += "], values [";
            for (String val : vals) {
                errorStr += (val + ", ");
            }
            throw new MetaException(errorStr + "]");
        }
        List<String> colNames = new ArrayList<String>();
        for (FieldSchema col : partCols) {
            colNames.add(col.getName());
        }
        return FileUtils.makePartName(colNames, vals, defaultStr);
    }

    public static String makePartName(List<FieldSchema> partCols,
            List<String> vals) throws MetaException {
        return makePartName(partCols, vals, null);
    }

    private List<FieldSchema> convertToFieldSchemas(List<MFieldSchema> mkeys) {
        List<FieldSchema> keys = null;
        if (mkeys != null) {
            keys = new ArrayList<FieldSchema>(mkeys.size());
            for (MFieldSchema part : mkeys) {
                keys.add(new FieldSchema(part.getName(), part.getType(), part
                        .getComment()));
            }
        }
        return keys;
    }

    public static void main(String[] args)
            throws MetaException
    {
//        List<FieldSchema> keys = new ArrayList<FieldSchema>(2);
//        // 分区字段:stat_date和province, 值为:20160812和beijing
//        keys.add(new FieldSchema("stat_date", "string", null));
//        keys.add(new FieldSchema("province", "string", null));
//        List<String> vals = new ArrayList<>();
//        vals.add("20160812");
//        vals.add("beijing");
//        System.out.println(makePartName(keys, vals)); // 输出: stat_date=20160812/province=beijing
//        testString();
//        testPartIds();
//        typeTest();
        System.out.println(Counter.BYTES_READ.getDeclaringClass().getName());
    }

    public static void testString() {
        final String queryText0 = "select \"PARTITION_NAME\", " + STATS_COLLIST + " from "
                + " \"PART_COL_STATS\" where \"DB_NAME\" = ? and \"TABLE_NAME\" = ? and \"COLUMN_NAME\""
                + "  in (%1$s) AND \"PARTITION_NAME\" in (%2$s) order by \"PARTITION_NAME\"";
        String queryText = String.format(queryText0,
                makeParams(2), makeParams(1));
        System.out.println(queryText);
    }

    private static String makeParams(int size) {
        // W/ size 0, query will fail, but at least we'd get to see the query in debug output.
        return (size == 0) ? "" : repeat(",?", size).substring(1);
    }

    private static void testPartIds()
            throws MetaException
    {
        List<Object> sqlResult = new ArrayList();
        sqlResult.add(10);
//        sqlResult.add(11);
//        sqlResult.add(12);
//        int idStringWidth = (int)Math.ceil(Math.log10(sqlResult.size())) + 1; // 1 for comma
//        int sbCapacity = sqlResult.size() * idStringWidth;
//        System.out.println("sbCapacity:" + sbCapacity);
        // Prepare StringBuilder for "PART_ID in (...)" to use in future queries.
        StringBuilder partSb = new StringBuilder();
        for (Object partitionId : sqlResult) {
            partSb.append(extractSqlLong(partitionId)).append(",");
        }
        System.out.println("partSb:" + partSb.capacity());
        String partIds = trimCommaList(partSb);
        System.out.println(partIds);
    }

    static Long extractSqlLong(Object obj) throws MetaException {
        if (obj == null) return null;
        if (!(obj instanceof Number)) {
            throw new MetaException("Expected numeric type but got " + obj.getClass().getName());
        }
        return ((Number) obj).longValue();
    }

    private static String trimCommaList(StringBuilder sb) {
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    private static void typeTest() {
        List<Object[]> insResult = new ArrayList<>();
        System.out.println(insResult.getClass().getGenericSuperclass());
    }
}
