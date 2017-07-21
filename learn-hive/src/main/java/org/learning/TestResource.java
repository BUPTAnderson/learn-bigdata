package org.learning;

import org.antlr.runtime.tree.Tree;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.cli.CliSessionState;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.ql.Context;
import org.apache.hadoop.hive.ql.ErrorMsg;
import org.apache.hadoop.hive.ql.QueryProperties;
import org.apache.hadoop.hive.ql.QueryState;
import org.apache.hadoop.hive.ql.exec.Utilities;
import org.apache.hadoop.hive.ql.lib.Node;
import org.apache.hadoop.hive.ql.metadata.Hive;
import org.apache.hadoop.hive.ql.metadata.Table;
import org.apache.hadoop.hive.ql.parse.ASTNode;
import org.apache.hadoop.hive.ql.parse.HiveParser;
import org.apache.hadoop.hive.ql.parse.ParseDriver;
import org.apache.hadoop.hive.ql.parse.ParseException;
import org.apache.hadoop.hive.ql.parse.ParseUtils;
import org.apache.hadoop.hive.ql.parse.QB;
import org.apache.hadoop.hive.ql.parse.QBParseInfo;
import org.apache.hadoop.hive.ql.parse.SemanticException;
import org.apache.hadoop.hive.ql.plan.HiveOperation;
import org.apache.hadoop.hive.ql.session.SessionState;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;

/**
 * Created by anderson on 17-7-19.
 */
public class TestResource
{
    static class Phase1Ctx {
        String dest;
        int nextNum;
    }

    static class PlannerContext {
        protected ASTNode   child;
        protected Phase1Ctx ctx1;

        void setParseTreeAttr(ASTNode child, Phase1Ctx ctx1) {
            this.child = child;
            this.ctx1 = ctx1;
        }

        void setCTASToken(ASTNode child) {
        }

        void setInsertToken(ASTNode ast, boolean isTmpFileDest) {
        }
    }

    private ASTNode ast;
    private ArrayList<String> viewsExpanded;
    ArrayList<String> ctesExpanded;
    private QB qb = new QB(null, null, false);
    protected HiveConf conf;
    protected Context ctx;
    protected QueryState queryState;
    protected QueryProperties queryProperties = new QueryProperties();

    boolean genResolvedParseTree(ASTNode ast, PlannerContext plannerCtx) throws SemanticException
    {
        ASTNode child = ast;
        this.ast = ast;
        viewsExpanded = new ArrayList<String>();
        ctesExpanded = new ArrayList<String>();

        // 1. analyze and process the position alias
        // 如果hive.groupby.orderby.position.alias设置为true,则group by和order by中可以用数字替代前面的列,
        // 比如:"command = "select a, b, c from abc order by 2, 3" 在processPositionAlias方法中会将ast中的2,3替换为b, c
        processPositionAlias(ast);

        // 2. analyze create table command
        // 判断是不是create table语句
        if (ast.getToken().getType() == HiveParser.TOK_CREATETABLE) {
            // if it is not CTAS, we don't need to go further and just return
            System.out.println("create table");
        } else {
            queryState.setCommandType(HiveOperation.QUERY);
        }

        // 3. analyze create view command
        // 判断是不是创建视图操作
        if (ast.getToken().getType() == HiveParser.TOK_CREATEVIEW
                || (ast.getToken().getType() == HiveParser.TOK_ALTERVIEW && ast.getChild(1).getType() == HiveParser.TOK_QUERY)) {
            System.out.println("create view");
        }

        // switch来判断是不是事务相关操作
        switch(ast.getToken().getType()) {
            case HiveParser.TOK_SET_AUTOCOMMIT:
                System.out.println("aaaaaaa");
                //fall through
            case HiveParser.TOK_START_TRANSACTION:
            case HiveParser.TOK_COMMIT:
            case HiveParser.TOK_ROLLBACK:
                System.out.println("-------------");
                return false;
        }

        // masking and filtering should be created here
        // the basic idea is similar to unparseTranslator.
//        tableMask = new TableMask(this, conf, ctx);

        // 4. continue analyzing from the child ASTNode.
        Phase1Ctx ctx1 = initPhase1Ctx();
        // 对于insert操作,判断要插入的表是否加密, 如果加密, 将要插入表的path放到qb的encryptedTargetTablePaths列表中
        preProcessForInsert(child, qb);
        // CalcitePlanner传递过来的plannerCtx是new PreCboCtx()
        if (!doPhase1(child, qb, ctx1, plannerCtx)) {
            // if phase1Result false return
            return false;
        }
        System.out.println("Completed phase 1 of Semantic Analysis");

        // 5. Resolve Parse Tree
        // Materialization is allowed if it is not a view definition
//        getMetaData(qb, createVwDesc == null);
        System.out.println("Completed getting MetaData in Semantic Analysis");

        plannerCtx.setParseTreeAttr(child, ctx1);

        return true;
    }

    private void preProcessForInsert(ASTNode node, QB qb) throws SemanticException {
        try {
            if (!(node != null && node.getToken() != null && node.getToken().getType() == HiveParser.TOK_QUERY)) {
                return;
            }
            for (Node child : node.getChildren()) {
                //each insert of multi insert looks like
                //(TOK_INSERT (TOK_INSERT_INTO (TOK_TAB (TOK_TABNAME T1)))
                if (((ASTNode) child).getToken().getType() != HiveParser.TOK_INSERT) {
                    continue;
                }
                ASTNode n = (ASTNode) ((ASTNode) child).getFirstChildWithType(HiveParser.TOK_INSERT_INTO);
                if (n == null) continue;
                n = (ASTNode) n.getFirstChildWithType(HiveParser.TOK_TAB);
                if (n == null) continue;
                n = (ASTNode) n.getFirstChildWithType(HiveParser.TOK_TABNAME);
                if (n == null) continue;
                String[] dbTab = getQualifiedTableName(n);
//                Table t = db.getTable(dbTab[0], dbTab[1]);
//                Path tablePath = t.getPath();
                // 检查要插入的表是否加密, 如果加密将表的path加入到qb的encryptedTargetTablePaths列表中
//                if (isPathEncrypted(tablePath)) {
//                    qb.addEncryptedTargetTablePath(tablePath);
//                }
            }
        }
        catch (Exception ex) {
            throw new SemanticException(ex);
        }
    }

    public static String[] getQualifiedTableName(ASTNode tabNameNode) throws SemanticException {
        // getChildCount值为1, 表示只有表名, 值为2, 表示是库名加表名
        if (tabNameNode.getType() != HiveParser.TOK_TABNAME ||
                (tabNameNode.getChildCount() != 1 && tabNameNode.getChildCount() != 2)) {
            throw new SemanticException(ErrorMsg.INVALID_TABLE_NAME.getMsg(tabNameNode));
        }
        if (tabNameNode.getChildCount() == 2) {
            String dbName = unescapeIdentifier(tabNameNode.getChild(0).getText());
            String tableName = unescapeIdentifier(tabNameNode.getChild(1).getText());
            return new String[] {dbName, tableName};
        }
        String tableName = unescapeIdentifier(tabNameNode.getChild(0).getText());
        return Utilities.getDbTableName(tableName);
    }

    public static String unescapeIdentifier(String val) {
        if (val == null) {
            return null;
        }
        if (val.charAt(0) == '`' && val.charAt(val.length() - 1) == '`') {
            val = val.substring(1, val.length() - 1);
        }
        return val;
    }

    public Phase1Ctx initPhase1Ctx() {
        Phase1Ctx ctx1 = new Phase1Ctx();
        ctx1.nextNum = 0;
        ctx1.dest = "reduce";

        return ctx1;
    }

    private void processPositionAlias(ASTNode ast) throws SemanticException {
        boolean isByPos = false;
        // hive.groupby.orderby.position.alias: 是否启用在分组或排序方式中使用列位置别名, 默认值为false
        if (HiveConf.getBoolVar(conf,
                HiveConf.ConfVars.HIVE_GROUPBY_ORDERBY_POSITION_ALIAS) == true) {
            isByPos = true;
        }

        // 下面的逻辑是通过对AST做DFS来进行处理
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

    public boolean doPhase1(ASTNode ast, QB qb, Phase1Ctx ctx1, PlannerContext plannerCtx)
            throws SemanticException {
        boolean phase1Result = true;
        QBParseInfo qbp = qb.getParseInfo();
        boolean skipRecursion = false;

        if (ast.getToken() != null) {
            skipRecursion = true;
            switch (ast.getToken().getType()) {
                case HiveParser.TOK_SELECTDI:
                    qb.countSelDi();
                    // fall through
                case HiveParser.TOK_SELECT:
                    qb.countSel();
                    qbp.setSelExprForClause(ctx1.dest, ast);

                    int posn = 0;
                    if (((ASTNode) ast.getChild(0)).getToken().getType() == HiveParser.TOK_HINTLIST) {
                        qbp.setHints((ASTNode) ast.getChild(0));
                        posn++;
                    }

                    if ((ast.getChild(posn).getChild(0).getType() == HiveParser.TOK_TRANSFORM))
                        queryProperties.setUsesScript(true);

//                    LinkedHashMap<String, ASTNode> aggregations = doPhase1GetAggregationsFromSelect(ast,
//                            qb, ctx1.dest);
//                    doPhase1GetColumnAliasesFromSelect(ast, qbp);
//                    qbp.setAggregationExprsForClause(ctx1.dest, aggregations);
//                    qbp.setDistinctFuncExprsForClause(ctx1.dest,
//                            doPhase1GetDistinctFuncExprs(aggregations));
                    break;

                case HiveParser.TOK_WHERE:
                    qbp.setWhrExprForClause(ctx1.dest, ast);
//                    if (!SubQueryUtils.findSubQueries((ASTNode) ast.getChild(0)).isEmpty())
//                        queryProperties.setFilterWithSubQuery(true);
                    break;

                case HiveParser.TOK_INSERT_INTO:
//                    String currentDatabase = SessionState.get().getCurrentDatabase();
//                    String tab_name = getUnescapedName((ASTNode) ast.getChild(0).getChild(0), currentDatabase);
//                    qbp.addInsertIntoTable(tab_name, ast);

                case HiveParser.TOK_DESTINATION:
                    ctx1.dest = "insclause-" + ctx1.nextNum;
                    ctx1.nextNum++;
                    boolean isTmpFileDest = false;
//                    if (ast.getChildCount() > 0 && ast.getChild(0) instanceof ASTNode) {
//                        ASTNode ch = (ASTNode) ast.getChild(0);
//                        if (ch.getToken().getType() == HiveParser.TOK_DIR && ch.getChildCount() > 0
//                                && ch.getChild(0) instanceof ASTNode) {
//                            ch = (ASTNode) ch.getChild(0);
//                            isTmpFileDest = ch.getToken().getType() == HiveParser.TOK_TMP_FILE;
//                        } else {
//                            if (ast.getToken().getType() == HiveParser.TOK_DESTINATION
//                                    && ast.getChild(0).getType() == HiveParser.TOK_TAB) {
//                                String fullTableName = getUnescapedName((ASTNode) ast.getChild(0).getChild(0),
//                                        SessionState.get().getCurrentDatabase());
//                                qbp.getInsertOverwriteTables().put(fullTableName, ast);
//                            }
//                        }
//                    }

                    // is there a insert in the subquery
                    if (qbp.getIsSubQ() && !isTmpFileDest) {
                        throw new SemanticException(ErrorMsg.NO_INSERT_INSUBQUERY.getMsg(ast));
                    }

                    if (plannerCtx != null) {
                        plannerCtx.setInsertToken(ast, isTmpFileDest);
                    }

                    qbp.setDestForClause(ctx1.dest, (ASTNode) ast.getChild(0));
//                    handleInsertStatementSpecPhase1(ast, qbp, ctx1);
                    if (qbp.getClauseNamesForDest().size() > 1) {
                        queryProperties.setMultiDestQuery(true);
                    }
                    break;

                case HiveParser.TOK_FROM:
                    int childCount = ast.getChildCount();
//                    if (child_count != 1) {
//                        throw new SemanticException(generateErrorMessage(ast,
//                                "Multiple Children " + child_count));
//                    }

                    // Check if this is a subquery / lateral view
                    ASTNode frm = (ASTNode) ast.getChild(0);
//                    if (frm.getToken().getType() == HiveParser.TOK_TABREF) {
//                        processTable(qb, frm);
//                    } else if (frm.getToken().getType() == HiveParser.TOK_VIRTUAL_TABLE) {
//                        // Create a temp table with the passed values in it then rewrite this portion of the
//                        // tree to be from that table.
//                        ASTNode newFrom = genValuesTempTable(frm, qb);
//                        ast.setChild(0, newFrom);
//                        processTable(qb, newFrom);
//                    } else if (frm.getToken().getType() == HiveParser.TOK_SUBQUERY) {
//                        processSubQuery(qb, frm);
//                    } else if (frm.getToken().getType() == HiveParser.TOK_LATERAL_VIEW ||
//                            frm.getToken().getType() == HiveParser.TOK_LATERAL_VIEW_OUTER) {
//                        queryProperties.setHasLateralViews(true);
//                        processLateralView(qb, frm);
//                    } else if (isJoinToken(frm)) {
//                        processJoin(qb, frm);
//                        qbp.setJoinExpr(frm);
//                    }else if(frm.getToken().getType() == HiveParser.TOK_PTBLFUNCTION){
//                        queryProperties.setHasPTF(true);
//                        processPTF(qb, frm);
//                    }
                    break;

                case HiveParser.TOK_CLUSTERBY:
                    // Get the clusterby aliases - these are aliased to the entries in the
                    // select list
                    queryProperties.setHasClusterBy(true);
                    qbp.setClusterByExprForClause(ctx1.dest, ast);
                    break;

                case HiveParser.TOK_DISTRIBUTEBY:
                    // Get the distribute by aliases - these are aliased to the entries in
                    // the
                    // select list
                    queryProperties.setHasDistributeBy(true);
                    qbp.setDistributeByExprForClause(ctx1.dest, ast);
//                    if (qbp.getClusterByForClause(ctx1.dest) != null) {
//                        throw new SemanticException(generateErrorMessage(ast,
//                                ErrorMsg.CLUSTERBY_DISTRIBUTEBY_CONFLICT.getMsg()));
//                    } else if (qbp.getOrderByForClause(ctx1.dest) != null) {
//                        throw new SemanticException(generateErrorMessage(ast,
//                                ErrorMsg.ORDERBY_DISTRIBUTEBY_CONFLICT.getMsg()));
//                    }
                    break;

                case HiveParser.TOK_SORTBY:
                    // Get the sort by aliases - these are aliased to the entries in the
                    // select list
                    queryProperties.setHasSortBy(true);
                    qbp.setSortByExprForClause(ctx1.dest, ast);
//                    if (qbp.getClusterByForClause(ctx1.dest) != null) {
//                        throw new SemanticException(generateErrorMessage(ast,
//                                ErrorMsg.CLUSTERBY_SORTBY_CONFLICT.getMsg()));
//                    } else if (qbp.getOrderByForClause(ctx1.dest) != null) {
//                        throw new SemanticException(generateErrorMessage(ast,
//                                ErrorMsg.ORDERBY_SORTBY_CONFLICT.getMsg()));
//                    }

                    break;

                case HiveParser.TOK_ORDERBY:
                    // Get the order by aliases - these are aliased to the entries in the
                    // select list
                    queryProperties.setHasOrderBy(true);
                    qbp.setOrderByExprForClause(ctx1.dest, ast);
//                    if (qbp.getClusterByForClause(ctx1.dest) != null) {
//                        throw new SemanticException(generateErrorMessage(ast,
//                                ErrorMsg.CLUSTERBY_ORDERBY_CONFLICT.getMsg()));
//                    }
                    break;

                case HiveParser.TOK_GROUPBY:
                case HiveParser.TOK_ROLLUP_GROUPBY:
                case HiveParser.TOK_CUBE_GROUPBY:
                case HiveParser.TOK_GROUPING_SETS:
                    // Get the groupby aliases - these are aliased to the entries in the
                    // select list
                    queryProperties.setHasGroupBy(true);
                    if (qbp.getJoinExpr() != null) {
                        queryProperties.setHasJoinFollowedByGroupBy(true);
                    }
//                    if (qbp.getSelForClause(ctx1.dest).getToken().getType() == HiveParser.TOK_SELECTDI) {
//                        throw new SemanticException(generateErrorMessage(ast,
//                                ErrorMsg.SELECT_DISTINCT_WITH_GROUPBY.getMsg()));
//                    }
                    qbp.setGroupByExprForClause(ctx1.dest, ast);
                    skipRecursion = true;

                    // Rollup and Cubes are syntactic sugar on top of grouping sets
                    if (ast.getToken().getType() == HiveParser.TOK_ROLLUP_GROUPBY) {
                        qbp.getDestRollups().add(ctx1.dest);
                    } else if (ast.getToken().getType() == HiveParser.TOK_CUBE_GROUPBY) {
                        qbp.getDestCubes().add(ctx1.dest);
                    } else if (ast.getToken().getType() == HiveParser.TOK_GROUPING_SETS) {
                        qbp.getDestGroupingSets().add(ctx1.dest);
                    }
                    break;

                case HiveParser.TOK_HAVING:
                    qbp.setHavingExprForClause(ctx1.dest, ast);
//                    qbp.addAggregationExprsForClause(ctx1.dest,
//                            doPhase1GetAggregationsFromSelect(ast, qb, ctx1.dest));
                    break;

                case HiveParser.KW_WINDOW:
//                    if (!qb.hasWindowingSpec(ctx1.dest) ) {
//                        throw new SemanticException(generateErrorMessage(ast,
//                                "Query has no Cluster/Distribute By; but has a Window definition"));
//                    }
//                    handleQueryWindowClauses(qb, ctx1, ast);
                    break;

                case HiveParser.TOK_LIMIT:
                    if (ast.getChildCount() == 2) {
                        qbp.setDestLimit(ctx1.dest,
                                new Integer(ast.getChild(0).getText()),
                                new Integer(ast.getChild(1).getText()));
                    } else {
                        qbp.setDestLimit(ctx1.dest, new Integer(0),
                                new Integer(ast.getChild(0).getText()));
                    }
                    break;

                case HiveParser.TOK_ANALYZE:
                    // Case of analyze command
//                    String table_name = getUnescapedName((ASTNode) ast.getChild(0).getChild(0)).toLowerCase();
//                    qb.setTabAlias(table_name, table_name);
//                    qb.addAlias(table_name);
//                    qb.getParseInfo().setIsAnalyzeCommand(true);
//                    qb.getParseInfo().setNoScanAnalyzeCommand(this.noscan);
//                    qb.getParseInfo().setPartialScanAnalyzeCommand(this.partialscan);
                    // Allow analyze the whole table and dynamic partitions
                    HiveConf.setVar(conf, HiveConf.ConfVars.DYNAMICPARTITIONINGMODE, "nonstrict");
                    HiveConf.setVar(conf, HiveConf.ConfVars.HIVEMAPREDMODE, "nonstrict");
                    break;

                case HiveParser.TOK_UNIONALL:
//                    if (!qbp.getIsSubQ()) {
//                        // this shouldn't happen. The parser should have converted the union to be
//                        // contained in a subquery. Just in case, we keep the error as a fallback.
//                        throw new SemanticException(generateErrorMessage(ast,
//                                ErrorMsg.UNION_NOTIN_SUBQ.getMsg()));
//                    }
                    skipRecursion = false;
                    break;

                case HiveParser.TOK_INSERT:
                    ASTNode destination = (ASTNode) ast.getChild(0);
                    Tree tab = destination.getChild(0);

                    // Proceed if AST contains partition & If Not Exists
                    if (destination.getChildCount() == 2 &&
                            tab.getChildCount() == 2 &&
                            destination.getChild(1).getType() == HiveParser.TOK_IFNOTEXISTS) {
                        String tableName = tab.getChild(0).getChild(0).getText();

                        Tree partitions = tab.getChild(1);
                        int childCount2 = partitions.getChildCount();
                        HashMap<String, String> partition = new HashMap<String, String>();
                        for (int i = 0; i < childCount2; i++) {
                            String partitionName = partitions.getChild(i).getChild(0).getText();
                            Tree pvalue = partitions.getChild(i).getChild(1);
                            if (pvalue == null) {
                                break;
                            }
//                            String partitionVal = stripQuotes(pvalue.getText());
//                            partition.put(partitionName, partitionVal);
                        }
                        // if it is a dynamic partition throw the exception
                        if (childCount2 != partition.size()) {
                            throw new SemanticException(ErrorMsg.INSERT_INTO_DYNAMICPARTITION_IFNOTEXISTS
                                    .getMsg(partition.toString()));
                        }
                        Table table = null;
//                        try {
//                            table = this.getTableObjectByName(tableName);
//                        } catch (HiveException ex) {
//                            throw new SemanticException(ex);
//                        }
//                        try {
//                            Partition parMetaData = db.getPartition(table, partition, false);
//                            // Check partition exists if it exists skip the overwrite
//                            if (parMetaData != null) {
//                                phase1Result = false;
//                                skipRecursion = true;
//                                LOG.info("Partition already exists so insert into overwrite " +
//                                        "skipped for partition : " + parMetaData.toString());
//                                break;
//                            }
//                        } catch (HiveException e) {
//                            LOG.info("Error while getting metadata : ", e);
//                        }
//                        validatePartSpec(table, partition, (ASTNode)tab, conf, false);
                    }
                    skipRecursion = false;
                    break;
                case HiveParser.TOK_LATERAL_VIEW:
                case HiveParser.TOK_LATERAL_VIEW_OUTER:
                    // todo: nested LV
                    assert ast.getChildCount() == 1;
                    qb.getParseInfo().getDestToLateralView().put(ctx1.dest, ast);
                    break;
                case HiveParser.TOK_CTE:
//                    processCTE(qb, ast);
                    break;
                default:
                    skipRecursion = false;
                    break;
            }
        }

        if (!skipRecursion) {
            // Iterate over the rest of the children
            int childCount = ast.getChildCount();
            for (int childPos = 0; childPos < childCount && phase1Result; ++childPos) {
                // Recurse
                phase1Result = phase1Result && doPhase1(
                        (ASTNode) ast.getChild(childPos), qb, ctx1, plannerCtx);
            }
        }
        return phase1Result;
    }

    static class PreCboCtx extends PlannerContext
    {
        enum Type {
            NONE, INSERT, CTAS, UNEXPECTED
        }

        private ASTNode nodeOfInterest;
        private PreCboCtx.Type type = PreCboCtx.Type.NONE;

        private void set(PreCboCtx.Type type, ASTNode ast) {
            if (this.type != PreCboCtx.Type.NONE) {
                this.type = PreCboCtx.Type.UNEXPECTED;
                return;
            }
            this.type = type;
            this.nodeOfInterest = ast;
        }

        @Override
        void setCTASToken(ASTNode child) {
            set(PreCboCtx.Type.CTAS, child);
        }

        @Override
        void setInsertToken(ASTNode ast, boolean isTmpFileDest) {
            if (!isTmpFileDest) {
                set(PreCboCtx.Type.INSERT, ast);
            }
        }
    }

    public static void main(String[] args)
            throws NoSuchFieldException, IllegalAccessException, IOException, ParseException, SemanticException
    {
        String command = "select  dt.d_year, item.i_brand_id brand_id, item.i_brand brand, sum(ss_ext_sales_price) sum_agg from  date_dim dt, store_sales, item where dt.d_date_sk = store_sales.ss_sold_date_sk and store_sales.ss_item_sk = item.i_item_sk and item.i_manufact_id = 436 and dt.d_moy=12 group by dt.d_year, item.i_brand, item.i_brand_id order by dt.d_year, sum_agg desc, brand_id limit 100";
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

        TestResource testResource = new TestResource();
        testResource.queryState = new QueryState(conf);
        testResource.conf = conf;
        PreCboCtx cboCtx = new PreCboCtx();
        testResource.genResolvedParseTree(tree, cboCtx);
    }
}
