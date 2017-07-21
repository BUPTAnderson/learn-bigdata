package org.learning;

import org.apache.hadoop.hive.ql.parse.ASTNode;
import org.apache.hadoop.hive.ql.parse.ParseDriver;
import org.apache.hadoop.hive.ql.parse.ParseException;

/**获取AST的根节点
 * Created by anderson on 17-7-18.
 */
public class TestTree
{
    public static void main(String[] args)
            throws ParseException
    {
        String str = "INSERT OVERWRITE TABLE access_log_temp2 SELECT a.users, a.prono, p.maker, p.price FROM access_log_hbase a JOIN product_hbase p ON (a.prono = p.prono)";
        ParseDriver pd = new ParseDriver();
        ASTNode tree = pd.parse(str);
        while ((tree.getToken() == null) && (tree.getChildCount() > 0)) {
            tree = (ASTNode) tree.getChild(0);
        }
        System.out.println(tree.getType());
    }
}
