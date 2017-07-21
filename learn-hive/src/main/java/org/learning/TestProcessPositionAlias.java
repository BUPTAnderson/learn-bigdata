package org.learning;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.cli.CliSessionState;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.ql.Context;
import org.apache.hadoop.hive.ql.ErrorMsg;
import org.apache.hadoop.hive.ql.QueryState;
import org.apache.hadoop.hive.ql.metadata.Hive;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.parse.ASTNode;
import org.apache.hadoop.hive.ql.parse.HiveParser;
import org.apache.hadoop.hive.ql.parse.ParseDriver;
import org.apache.hadoop.hive.ql.parse.ParseException;
import org.apache.hadoop.hive.ql.parse.ParseUtils;
import org.apache.hadoop.hive.ql.parse.SemanticException;
import org.apache.hadoop.hive.ql.session.SessionState;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Stack;

/**
 * hql中数字别名替换, 如果hive.groupby.orderby.position.alias设置为true
 * 则group by和order by中可以用数字替代前面的列, 比如:
 * "select a, b, c from abc group by 1, 2" SemanticAnalyzer中processPositionAlias方法会将1,2替换为a,b
 * "command = "select a, b, c from abc order by 2, 3" SemanticAnalyzer中processPositionAlias方法会将2,3替换为b, c
 * Created by anderson on 17-7-18.
 */
public class TestProcessPositionAlias
{
    public static void processPositionAlias(ASTNode ast, HiveConf conf) throws SemanticException
    {
        boolean isByPos = true;
        // hive.groupby.orderby.position.alias: 是否启用在分组或排序方式中使用列位置别名, 默认值为false
//        if (HiveConf.getBoolVar(conf,
//                HiveConf.ConfVars.HIVE_GROUPBY_ORDERBY_POSITION_ALIAS) == true) {
//            isByPos = true;
//        }

        Deque<ASTNode> stack = new ArrayDeque<ASTNode>();
        stack.push(ast);

        while (!stack.isEmpty()) {
            ASTNode next = stack.pop();

            if (next.getChildCount()  == 0) {
                continue;
            }

            boolean isAllCol;
            ASTNode selectNode = null;
            ASTNode groupbyNode = null;
            ASTNode orderbyNode = null;

            // get node type
            int childCount = next.getChildCount();
            for (int childPos = 0; childPos < childCount; ++childPos) {
                ASTNode node = (ASTNode) next.getChild(childPos);
                int type = node.getToken().getType();
                if (type == HiveParser.TOK_SELECT) {
                    selectNode = node;
                } else if (type == HiveParser.TOK_GROUPBY) {
                    groupbyNode = node;
                } else if (type == HiveParser.TOK_ORDERBY) {
                    orderbyNode = node;
                }
            }

            if (selectNode != null) {
                int selectExpCnt = selectNode.getChildCount();

                // replace each of the position alias in GROUPBY with the actual column name
                if (groupbyNode != null) {
                    for (int childPos = 0; childPos < groupbyNode.getChildCount(); ++childPos) {
                        ASTNode node = (ASTNode) groupbyNode.getChild(childPos);
                        if (node.getToken().getType() == HiveParser.Number) {
                            if (isByPos) {
                                int pos = Integer.parseInt(node.getText());
                                if (pos > 0 && pos <= selectExpCnt) {
                                    groupbyNode.setChild(childPos,
                                            selectNode.getChild(pos - 1).getChild(0));
                                } else {
                                    throw new SemanticException(
                                            ErrorMsg.INVALID_POSITION_ALIAS_IN_GROUPBY.getMsg(
                                                    "Position alias: " + pos + " does not exist\n" +
                                                            "The Select List is indexed from 1 to " + selectExpCnt));
                                }
                            } else {
                                System.out.println("Using constant number  " + node.getText() +
                                        " in group by. If you try to use position alias when hive.groupby.orderby.position.alias is false, the position alias will be ignored.");
                            }
                        }
                    }
                }

                // replace each of the position alias in ORDERBY with the actual column name
                if (orderbyNode != null) {
                    isAllCol = false;
                    for (int childPos = 0; childPos < selectNode.getChildCount(); ++childPos) {
                        ASTNode node = (ASTNode) selectNode.getChild(childPos).getChild(0);
                        if (node.getToken().getType() == HiveParser.TOK_ALLCOLREF) {
                            isAllCol = true;
                        }
                    }
                    for (int childPos = 0; childPos < orderbyNode.getChildCount(); ++childPos) {
                        ASTNode colNode = (ASTNode) orderbyNode.getChild(childPos).getChild(0);
                        ASTNode node = (ASTNode) colNode.getChild(0);
                        if (node.getToken().getType() == HiveParser.Number) {
                            if (isByPos) {
                                if (!isAllCol) {
                                    int pos = Integer.parseInt(node.getText());
                                    if (pos > 0 && pos <= selectExpCnt) {
                                        colNode.setChild(0, selectNode.getChild(pos - 1).getChild(0));
                                    } else {
                                        throw new SemanticException(
                                                ErrorMsg.INVALID_POSITION_ALIAS_IN_ORDERBY.getMsg(
                                                        "Position alias: " + pos + " does not exist\n" +
                                                                "The Select List is indexed from 1 to " + selectExpCnt));
                                    }
                                } else {
                                    throw new SemanticException(
                                            ErrorMsg.NO_SUPPORTED_ORDERBY_ALLCOLREF_POS.getMsg());
                                }
                            } else { //if not using position alias and it is a number.
                                System.out.println("Using constant number " + node.getText() +
                                        " in order by. If you try to use position alias when hive.groupby.orderby.position.alias is false, the position alias will be ignored.");
                            }
                        }
                    }
                }
            }

            for (int i = next.getChildren().size() - 1; i >= 0; i--) {
                stack.push((ASTNode) next.getChildren().get(i));
            }
        }
    }

    public static void dfs(ASTNode tree) {
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
    public static void main(String[] args) throws ParseException, IOException, NoSuchFieldException, IllegalAccessException, HiveException, MetaException
    {
        String command = "select a, b, c from abc group by 1, 2";
        command = "select a, b, c from abc order by 2, 3";
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
        Hive.get(conf).getMSC();
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
        dfs(tree);
        processPositionAlias(tree, conf);
        System.out.println("---------------");
        dfs(tree);
    }
}
