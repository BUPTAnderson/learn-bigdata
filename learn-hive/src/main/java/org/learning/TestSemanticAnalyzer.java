package org.learning;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.cli.CliSessionState;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.ql.Context;
import org.apache.hadoop.hive.ql.QueryState;
import org.apache.hadoop.hive.ql.metadata.Hive;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.parse.ASTNode;
import org.apache.hadoop.hive.ql.parse.BaseSemanticAnalyzer;
import org.apache.hadoop.hive.ql.parse.ParseDriver;
import org.apache.hadoop.hive.ql.parse.ParseException;
import org.apache.hadoop.hive.ql.parse.ParseUtils;
import org.apache.hadoop.hive.ql.parse.SemanticAnalyzerFactory;
import org.apache.hadoop.hive.ql.session.SessionState;

import java.io.IOException;
import java.lang.reflect.Field;

/**不同的HQL获取到的BaseSemanticAnalyzer类型
 * Created by anderson on 17-7-18.
 */
public class TestSemanticAnalyzer
{
    public static void main(String[] args)
            throws IOException, HiveException, ParseException, NoSuchFieldException, IllegalAccessException, MetaException
    {
        // org.apache.hadoop.hive.ql.parse.CalcitePlanner
        String command = "INSERT OVERWRITE TABLE access_log_temp2 SELECT a.users, a.prono, p.maker, p.price FROM access_log_hbase a JOIN product_hbase p ON (a.prono = p.prono)";
        command = "select  dt.d_year, item.i_category_id, sum(ss_ext_sales_price) as s from date_dim dt, item where dt.dt_item_sk = item.i_item_sk group by dt.d_year, item.i_category order by s desc, dt.d_year, item.i_category_id limit 100";
//        command = "select * from abc limit 10";
//        command = "create table abc(a int)";
        // org.apache.hadoop.hive.ql.parse.DDLSemanticAnalyzer
//        command = "drop table abc";
        HiveConf conf = new HiveConf(SessionState.class);
        conf.set("_hive.hdfs.session.path", "/tmp");
        conf.set("hive.metastore.uris", "thrift://192.168.177.79:9083");
        CliSessionState ss = new CliSessionState(conf);
        Field sessionPath = SessionState.class.getDeclaredField("hdfsSessionPath");
        sessionPath.setAccessible(true);
        sessionPath.set(ss, new Path("/tmp"));
        Field sessionPath2 = SessionState.class.getDeclaredField("localSessionPath");
        sessionPath2.setAccessible(true);
        sessionPath2.set(ss, new Path("/tmp"));

//        System.out.println(ss.getHDFSSessionPath(conf));
        try {
            Hive.get(conf).getMSC().getAllFunctions();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        SessionState.setCurrentSessionState(ss);
//        SessionState.start(ss);

        Context ctx = new Context(conf);
        ctx.setTryCount(0);
        ctx.setCmd(command);
        ctx.setHDFSCleanup(true);

        QueryState queryState = new QueryState(conf);
        ParseDriver pd = new ParseDriver();
        ASTNode tree = pd.parse(command, ctx);

        tree = ParseUtils.findRootNonNullToken(tree);
        System.out.println(tree.getType());
        BaseSemanticAnalyzer sem = SemanticAnalyzerFactory.get(queryState, tree);
        System.out.println(sem.getClass().getName());
        sem.analyze(tree, ctx);
    }
}
