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
import org.apache.hadoop.hive.ql.parse.ParseDriver;
import org.apache.hadoop.hive.ql.parse.ParseException;
import org.apache.hadoop.hive.ql.parse.ParseUtils;
import org.apache.hadoop.hive.ql.session.SessionState;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Stack;

/**
 * DFS(深度优先遍历打印抽象语法树)
 * Created by anderson on 17-7-18.
 */
class TreeNode {
    ASTNode node;
    Integer length;

    public TreeNode(ASTNode node, Integer length)
    {
        this.node = node;
        this.length = length;
    }

    public ASTNode getNode()
    {
        return node;
    }

    public void setNode(ASTNode node)
    {
        this.node = node;
    }

    public Integer getLength()
    {
        return length;
    }

    public void setLength(Integer length)
    {
        this.length = length;
    }
}
public class TestDFS
{
    public static void main(String[] args)
            throws ParseException, IOException, NoSuchFieldException, IllegalAccessException, HiveException, MetaException
    {
        // 可以将command换成其它的HQL
        String command = "INSERT OVERWRITE TABLE access_log_temp2 SELECT a.users, a.prono, p.maker, p.price FROM access_log_hbase a JOIN product_hbase p ON (a.prono = p.prono)";
//        command = "create table abc(a int)";
//        command = "drop table abc";
        command = "INSERT INTO TABLE test.access_log_temp2 SELECT a.users, a.prono, p.maker, p.price FROM access_log_hbase a JOIN product_hbase p ON (a.prono = p.prono)";
        // org.apache.hadoop.hive.ql.parse.DDLSemanticAnalyzer
//        command = "drop table abc";
        command = "select a.b from test.abc a limit 10";
//        command = "select a from abc limit 10";
//        command = "select id,devid from (select id,devid,job_time from tb_in_base) a";
//        command = "select id from (select id,devid from tb_in_base) a";
//        command = "create view test_view (id, name_length) as  select id, length(name) from test";
//        command = "select  dt.d_year, item.i_brand_id brand_id, item.i_brand brand, sum(ss_ext_sales_price) sum_agg from  date_dim dt, store_sales, item where dt.d_date_sk = store_sales.ss_sold_date_sk and store_sales.ss_item_sk = item.i_item_sk and item.i_manufact_id = 436 and dt.d_moy=12 group by dt.d_year, item.i_brand, item.i_brand_id order by dt.d_year, sum_agg desc, brand_id limit 100";
        command = "select  dt.d_year, item.i_category_id, sum(ss_ext_sales_price) as s from date_dim dt, item where dt.dt_item_sk = item.i_item_sk group by dt.d_year, item.i_category order by s desc, dt.d_year, item.i_category_id limit 100";
        HiveConf conf = new HiveConf(SessionState.class);
        conf.set("_hive.hdfs.session.path", "/tmp");
        conf.set("hive.metastore.uris", "thrift://bds-test-003:9083");
        CliSessionState ss = new CliSessionState(conf);
        Field sessionPath = SessionState.class.getDeclaredField("hdfsSessionPath");
        sessionPath.setAccessible(true);
        sessionPath.set(ss, new Path("/tmp"));
        Field sessionPath2 = SessionState.class.getDeclaredField("localSessionPath");
        sessionPath2.setAccessible(true);
        sessionPath2.set(ss, new Path("/tmp"));
        try {
            Hive.get(conf).getMSC();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
//        System.out.println(ss.getHDFSSessionPath(conf));
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

        Stack<TreeNode> nodeStack = new Stack<TreeNode>();
        nodeStack.push(new TreeNode(tree, 0));
        while (!nodeStack.isEmpty()) {
            TreeNode treeNode = nodeStack.pop();
            ASTNode next = treeNode.getNode();
            int length = treeNode.getLength();
            for (int i = 0; i < length; i++) {
                System.out.print("---|");
            }
            System.out.println(next.getToken().getText() + "(" + next.getToken().getType() + ")");

            int childCount = treeNode.getNode().getChildCount();
            for (int childPos = (childCount - 1); childPos >= 0; --childPos) {
                ASTNode node = (ASTNode) next.getChild(childPos);
                nodeStack.push(new TreeNode(node, length + 1));
            }
        }
    }
}
//输出
/*
TOK_QUERY(860)
---|TOK_FROM(748)
---|---|TOK_JOIN(790)
---|---|---|TOK_TABREF(954)
---|---|---|---|TOK_TABNAME(953)
---|---|---|---|---|access_log_hbase(26)
---|---|---|---|a(26)
---|---|---|TOK_TABREF(954)
---|---|---|---|TOK_TABNAME(953)
---|---|---|---|---|product_hbase(26)
---|---|---|---|p(26)
---|---|---|=(20)
---|---|---|---|.(17)
---|---|---|---|---|TOK_TABLE_OR_COL(950)
---|---|---|---|---|---|a(26)
---|---|---|---|---|prono(26)
---|---|---|---|.(17)
---|---|---|---|---|TOK_TABLE_OR_COL(950)
---|---|---|---|---|---|p(26)
---|---|---|---|---|prono(26)
---|TOK_INSERT(772)
---|---|TOK_DESTINATION(726)
---|---|---|TOK_TAB(925)
---|---|---|---|TOK_TABNAME(953)
---|---|---|---|---|access_log_temp2(26)
---|---|TOK_SELECT(878)
---|---|---|TOK_SELEXPR(880)
---|---|---|---|.(17)
---|---|---|---|---|TOK_TABLE_OR_COL(950)
---|---|---|---|---|---|a(26)
---|---|---|---|---|users(26)
---|---|---|TOK_SELEXPR(880)
---|---|---|---|.(17)
---|---|---|---|---|TOK_TABLE_OR_COL(950)
---|---|---|---|---|---|a(26)
---|---|---|---|---|prono(26)
---|---|---|TOK_SELEXPR(880)
---|---|---|---|.(17)
---|---|---|---|---|TOK_TABLE_OR_COL(950)
---|---|---|---|---|---|p(26)
---|---|---|---|---|maker(26)
---|---|---|TOK_SELEXPR(880)
---|---|---|---|.(17)
---|---|---|---|---|TOK_TABLE_OR_COL(950)
---|---|---|---|---|---|p(26)
---|---|---|---|---|price(26)
 */