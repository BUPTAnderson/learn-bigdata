package org.learning;

import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.ql.parse.ASTNode;
import org.apache.hadoop.hive.ql.parse.ParseDriver;
import org.apache.hadoop.hive.ql.parse.ParseException;
import org.apache.hadoop.hive.ql.parse.ParseUtils;
import org.apache.hadoop.hive.ql.session.SessionState;

import java.io.IOException;
import java.util.Stack;

/**
 * Created by anderson on 17-8-1.
 */
public class TestDFS2
{
    public static void main(String[] args)
            throws IOException, ParseException
    {
        String command = "select * from default.partition_test limit 10";
        command = "INSERT INTO TABLE test.access_log_temp2 SELECT a.users, a.prono, p.maker, p.price FROM access_log_hbase a JOIN product_hbase p ON (a.prono = p.prono)";
        command = "select a.member_id from (select member_id,name from partition_test) a";
        command = "select a.member_id b from (select member_id,name from partition_test) a";
//        command = "INSERT OVERWRITE TABLE access_log_temp2 SELECT a.users, a.prono, p.maker, p.price FROM access_log_hbase a JOIN product_hbase p ON (a.prono = p.prono)";
//        command = "insert overwrite table partition_test partition(stat_date='20110728',province='henan') select member_id,name from partition_test_input where stat_date='20110728' and province='henan'";
//        command = "insert overwrite table partition_test partition(stat_date='20110728',province='henan') if not exists select member_id,name from partition_test_input where stat_date='20110728' and province='henan'";
        HiveConf conf = new HiveConf(SessionState.class);
        conf.set("_hive.hdfs.session.path", "/tmp");
        conf.set("hive.metastore.uris", "thrift://bds-test-003:9083");
//        Context ctx = new Context(conf);
//        ctx.setTryCount(0);
//        ctx.setCmd(command);
//        ctx.setHDFSCleanup(true);

        ParseDriver pd = new ParseDriver();
        ASTNode tree = pd.parse(command, null);

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

//        tree = pd.parse(command, null);
//        tree = ParseUtils.findRootNonNullToken(tree);
//        tree = (ASTNode) ((ASTNode) ((ASTNode) tree.getChild(0)).getChild(0)).getChild(0);
//        String tableIdName = SemanticAnalyzer.getUnescapedName(tree).toLowerCase();
//        System.out.println(tableIdName);
//        String alias = SemanticAnalyzer.getUnescapedUnqualifiedTableName(tree);
//        System.out.println(alias);
        String[] strs = new String[3];
        strs[0] = "abc";
        strs[1] = "def";
        strs[2] = "ghi";
        System.out.println(strs);
    }
}

/**
 select * from default.partition_test limit 10
 TOK_QUERY(860)
 ---|TOK_FROM(748)
 ---|---|TOK_TABREF(954)
 ---|---|---|TOK_TABNAME(953)
 ---|---|---|---|default(26)
 ---|---|---|---|partition_test(26)
 ---|TOK_INSERT(772)
 ---|---|TOK_DESTINATION(726)
 ---|---|---|TOK_DIR(727)
 ---|---|---|---|TOK_TMP_FILE(963)
 ---|---|TOK_SELECT(878)
 ---|---|---|TOK_SELEXPR(880)
 ---|---|---|---|TOK_ALLCOLREF(646)
 ---|---|TOK_LIMIT(797)
 ---|---|---|10(321)
 */

/**
 select a.member_id from (select member_id,name from partition_test) a
 TOK_QUERY(860)
 ---|TOK_FROM(748)
 ---|---|TOK_SUBQUERY(919)
 ---|---|---|TOK_QUERY(860)
 ---|---|---|---|TOK_FROM(748)
 ---|---|---|---|---|TOK_TABREF(954)
 ---|---|---|---|---|---|TOK_TABNAME(953)
 ---|---|---|---|---|---|---|partition_test(26)
 ---|---|---|---|TOK_INSERT(772)
 ---|---|---|---|---|TOK_DESTINATION(726)
 ---|---|---|---|---|---|TOK_DIR(727)
 ---|---|---|---|---|---|---|TOK_TMP_FILE(963)
 ---|---|---|---|---|TOK_SELECT(878)
 ---|---|---|---|---|---|TOK_SELEXPR(880)
 ---|---|---|---|---|---|---|TOK_TABLE_OR_COL(950)
 ---|---|---|---|---|---|---|---|member_id(26)
 ---|---|---|---|---|---|TOK_SELEXPR(880)
 ---|---|---|---|---|---|---|TOK_TABLE_OR_COL(950)
 ---|---|---|---|---|---|---|---|name(26)
 ---|---|---|a(26)
 ---|TOK_INSERT(772)
 ---|---|TOK_DESTINATION(726)
 ---|---|---|TOK_DIR(727)
 ---|---|---|---|TOK_TMP_FILE(963)
 ---|---|TOK_SELECT(878)
 ---|---|---|TOK_SELEXPR(880)
 ---|---|---|---|.(17)
 ---|---|---|---|---|TOK_TABLE_OR_COL(950)
 ---|---|---|---|---|---|a(26)
 ---|---|---|---|---|member_id(26)
 */

/**
 INSERT INTO TABLE test.access_log_temp2 SELECT a.users, a.prono, p.maker, p.price FROM access_log_hbase a JOIN product_hbase p ON (a.prono = p.prono)
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
 ---|---|TOK_INSERT_INTO(773)
 ---|---|---|TOK_TAB(925)
 ---|---|---|---|TOK_TABNAME(953)
 ---|---|---|---|---|test(26)
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

/**
 INSERT OVERWRITE TABLE access_log_temp2 SELECT a.users, a.prono, p.maker, p.price FROM access_log_hbase a JOIN product_hbase p ON (a.prono = p.prono)
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

/**
 insert overwrite table partition_test partition(stat_date='20110728',province='henan') select member_id,name from partition_test_input where stat_date='20110728' and province='henan'

 TOK_QUERY(860)
 ---|TOK_FROM(748)
 ---|---|TOK_TABREF(954)
 ---|---|---|TOK_TABNAME(953)
 ---|---|---|---|partition_test_input(26)
 ---|TOK_INSERT(772)
 ---|---|TOK_DESTINATION(726)
 ---|---|---|TOK_TAB(925)
 ---|---|---|---|TOK_TABNAME(953)
 ---|---|---|---|---|partition_test(26)
 ---|---|---|---|TOK_PARTSPEC(839)
 ---|---|---|---|---|TOK_PARTVAL(840)
 ---|---|---|---|---|---|stat_date(26)
 ---|---|---|---|---|---|'20110728'(332)
 ---|---|---|---|---|TOK_PARTVAL(840)
 ---|---|---|---|---|---|province(26)
 ---|---|---|---|---|---|'henan'(332)
 ---|---|TOK_SELECT(878)
 ---|---|---|TOK_SELEXPR(880)
 ---|---|---|---|TOK_TABLE_OR_COL(950)
 ---|---|---|---|---|member_id(26)
 ---|---|---|TOK_SELEXPR(880)
 ---|---|---|---|TOK_TABLE_OR_COL(950)
 ---|---|---|---|---|name(26)
 ---|---|TOK_WHERE(988)
 ---|---|---|and(34)
 ---|---|---|---|=(20)
 ---|---|---|---|---|TOK_TABLE_OR_COL(950)
 ---|---|---|---|---|---|stat_date(26)
 ---|---|---|---|---|'20110728'(332)
 ---|---|---|---|=(20)
 ---|---|---|---|---|TOK_TABLE_OR_COL(950)
 ---|---|---|---|---|---|province(26)
 ---|---|---|---|---|'henan'(332)
 */

/**
 insert overwrite table partition_test partition(stat_date='20110728',province='henan') if not exists select member_id,name from partition_test_input where stat_date='20110728' and province='henan'

 TOK_QUERY(860)
 ---|TOK_FROM(748)
 ---|---|TOK_TABREF(954)
 ---|---|---|TOK_TABNAME(953)
 ---|---|---|---|partition_test_input(26)
 ---|TOK_INSERT(772)
 ---|---|TOK_DESTINATION(726)
 ---|---|---|TOK_TAB(925)
 ---|---|---|---|TOK_TABNAME(953)
 ---|---|---|---|---|partition_test(26)
 ---|---|---|---|TOK_PARTSPEC(839)
 ---|---|---|---|---|TOK_PARTVAL(840)
 ---|---|---|---|---|---|stat_date(26)
 ---|---|---|---|---|---|'20110728'(332)
 ---|---|---|---|---|TOK_PARTVAL(840)
 ---|---|---|---|---|---|province(26)
 ---|---|---|---|---|---|'henan'(332)
 ---|---|---|TOK_IFNOTEXISTS(767)
 ---|---|TOK_SELECT(878)
 ---|---|---|TOK_SELEXPR(880)
 ---|---|---|---|TOK_TABLE_OR_COL(950)
 ---|---|---|---|---|member_id(26)
 ---|---|---|TOK_SELEXPR(880)
 ---|---|---|---|TOK_TABLE_OR_COL(950)
 ---|---|---|---|---|name(26)
 ---|---|TOK_WHERE(988)
 ---|---|---|and(34)
 ---|---|---|---|=(20)
 ---|---|---|---|---|TOK_TABLE_OR_COL(950)
 ---|---|---|---|---|---|stat_date(26)
 ---|---|---|---|---|'20110728'(332)
 ---|---|---|---|=(20)
 ---|---|---|---|---|TOK_TABLE_OR_COL(950)
 ---|---|---|---|---|---|province(26)
 ---|---|---|---|---|'henan'(332)
 */