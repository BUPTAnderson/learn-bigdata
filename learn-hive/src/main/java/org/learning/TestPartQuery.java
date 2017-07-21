package org.learning;

import org.apache.hive.common.util.HiveStringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by anderson on 17-7-11.
 */
public class TestPartQuery
{
    private void getPartQueryWithParams(String instanceName, String instanceOwnerName, String instanceOwnerType, String dbName,
            String tblName, List<String> partNames) {
        StringBuilder sb = new StringBuilder("table.tableName == t1 && table.database.name == t2 && table.database.instance.name == t3 && table.database.instance.ownerName == t4 && table.database.instance.ownerType == t5 (");
        int n = 0;
        Map<String, String> params = new HashMap<String, String>();
        for (Iterator<String> itr = partNames.iterator(); itr.hasNext(); ) {
            String pn = "p" + n;
            n++;
            String part = itr.next();
            params.put(pn, part);
            sb.append("partitionName == ").append(pn);
            sb.append(" || ");
        }
        sb.setLength(sb.length() - 4); // remove the last " || "
        sb.append(')');
        System.out.println(sb.toString());
        params.put("t1", HiveStringUtils.normalizeIdentifier(tblName));
        params.put("t2", HiveStringUtils.normalizeIdentifier(dbName));
        params.put("t3", instanceName);
        params.put("t4", instanceOwnerName);
        params.put("t5", instanceOwnerType);
        System.out.println(makeParameterDeclarationString(params));
    }

    private String makeParameterDeclarationString(Map<String, String> params) {
        //Create the parameter declaration string
        StringBuilder paramDecl = new StringBuilder();
        for (String key : params.keySet()) {
            paramDecl.append(", java.lang.String " + key);
        }
        return paramDecl.toString();
    }

    public static void main(String[] args)
    {
        List<String> partNames = new ArrayList<>();
        partNames.add("stat_date=20160812/province=beijing");
        partNames.add("stat_date=20160813/province=shanghai");
        TestPartQuery testPartQuery = new TestPartQuery();
        testPartQuery.getPartQueryWithParams("ins1", "datajingod_m", "USER", "db1", "tbl1", partNames);
    }
}
