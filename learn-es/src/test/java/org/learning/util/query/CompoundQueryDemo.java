package org.learning.util.query;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.BoostingQueryBuilder;
import org.elasticsearch.index.query.ConstantScoreQueryBuilder;
import org.elasticsearch.index.query.DisMaxQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.learning.BaseTest;

/**
 * Created by anderson on 16-12-13.
 */
public class CompoundQueryDemo extends BaseTest
{
    /**
     * 复合查询包装其他复合或叶查询，要么结合其结果和分数，更改其行为，或从查询切换到过滤器上下文。
     * 包含如下查询：
     * constant_score query
     * bool query
     * dis_max query
     * function_score query
     * boosting query
     * indices query
     */

    /**
     * Constant Score Query
     * 一个查询，它包装另一个查询，并简单地返回一个常数分数，这个分数等于等于filter中每个document的query boost。 映射到Lucene ConstantScoreQuery。
     */
    @Test
    public void constantScoreQuery() {
        ConstantScoreQueryBuilder qb = QueryBuilders.constantScoreQuery(
                QueryBuilders.termQuery("name", "Anna")
        )
                .boost(2.0f);
        SearchResponse sResponse = client.prepareSearch(index)
                .setTypes(type)
//                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
//                .addSort("_score", SortOrder.DESC)
                .setQuery(qb)
                .setFrom(0).setSize(12)
                .setExplain(true)
                .execute().actionGet();
        SearchHits hits = sResponse.getHits();
        printWithScore(hits);
    }

    /**
     * Bool 过滤
     *
     * bool 过滤可以用来合并多个过滤条件查询结果的布尔逻辑，它包含一下操作符：
     * must :: 多个查询条件的完全匹配,相当于 and。
     * must_not :: 多个查询条件的相反匹配，相当于 not。
     * should :: 至少有一个查询条件匹配, 相当于 or。
     * 这些参数可以分别继承一个过滤条件或者一个过滤条件的数组：
     * {
     *      "bool": {
     *          "must":     { "term": { "folder": "inbox" }},
     *          "must_not": { "term": { "tag":    "spam"  }},
     *          "should": [
     *              { "term": { "starred": true   }},
     *              { "term": { "unread":  true   }}
     *           ]
     *      }
     * }

     */
    @Test
    public void boolFilter() {
        //多个查询组合
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("address", "street"))//必须
                .mustNot(QueryBuilders.termQuery("state.keyword", "NM"))//排除
                .should(QueryBuilders.termQuery("employer.keyword", "Quantasis")) //可选
                .filter(QueryBuilders.termQuery("gender.keyword", "F")); //a query that must appear in the matching documents but doesn’t contribute to scoring.
        SearchResponse sResponse = client.prepareSearch("bank")
                .setTypes("account")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .addSort("_score", SortOrder.DESC)
                .setQuery(boolQueryBuilder)
                .setFrom(0).setSize(12)
                .setExplain(true)
                .execute().actionGet();
        SearchHits hits = sResponse.getHits();
        printWithScore(hits);
    }

    /**
     * boole Query
     *
     * bool 查询
     * bool 查询与 bool 过滤相似，用于合并多个查询子句。不同的是，bool 过滤可以直接给出是否匹配成功， 而bool 查询要计算每一个查询子句的 _score （相关性分值）。
     * must:: 查询指定文档一定要被包含。
     * must_not:: 查询指定文档一定不要被包含。
     * should:: 查询指定文档，有则可以为文档相关性加分。
     * 以下查询将会找到 title 字段中包含 "how to make millions"，并且 "tag" 字段没有被标为 spam。 如果有标识为 "starred" 或者发布日期为2014年之前，那么这些匹配的文档将比同类网站等级高：

     *  {
     *      "bool": {
     *          "must":     { "match": { "title": "how to make millions" }},
     *          "must_not": { "match": { "tag":   "spam" }},
     *          "should": [
     *              { "match": { "tag": "starred" }},
     *              { "range": { "date": { "gte": "2014-01-01" }}}
     *          ]
     *      }
     *  }
     *
     * 提示： 如果bool 查询下没有must子句，那至少应该有一个should子句。但是 如果有must子句，那么没有should子句也可以进行查询。
     */
    @Test
    public void boolQuery() {
    }

    /**
     * Dis Max Query
     */
    @Test
    public void disMaxQuery() {
        DisMaxQueryBuilder qb = QueryBuilders.disMaxQuery()
                .add(QueryBuilders.termQuery("sex", "男"))
                .add(QueryBuilders.termQuery("age", "35"))
                .boost(1.2f)
                .tieBreaker(0.7f);
        SearchResponse sResponse = client.prepareSearch(index)
                .setTypes(type)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .addSort("_score", SortOrder.DESC)
                .setQuery(qb)
                .setFrom(0).setSize(12)
                .setExplain(true)
                .execute().actionGet();
        SearchHits hits = sResponse.getHits();
        printWithScore(hits);
    }

    /**
     * Function Score Query
     * function_score允许您修改查询检索的文档的分数。
     * 要使用function_score，用户必须定义一个查询和一个或多个函数，为查询返回的每个文档计算新的分数。
     */
    @Test
    public void functionScoreQuery() {
        FunctionScoreQueryBuilder.FilterFunctionBuilder[] functions = {
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                        QueryBuilders.matchQuery("lastname", "gray"),
                        ScoreFunctionBuilders.randomFunction("ABCDEF")),
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                        ScoreFunctionBuilders.exponentialDecayFunction("age", 0L, 1L))
        };
        FunctionScoreQueryBuilder qb = QueryBuilders.functionScoreQuery(functions);
        SearchResponse sResponse = client.prepareSearch("bank")
                .setTypes("account")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .addSort("_score", SortOrder.DESC)
                .setQuery(qb)
                .setFrom(0).setSize(12)
                .setExplain(true)
                .execute().actionGet();
        SearchHits hits = sResponse.getHits();
        printWithScore(hits);
    }

    /**
     * Boosting Query
     */
    @Test
    public void boostingQuery() {
        BoostingQueryBuilder qb = QueryBuilders.boostingQuery(
                QueryBuilders.termQuery("lastname.keyword", "Gray"), //query that will promote documents
                QueryBuilders.termQuery("lastname.keyword", "Berg")) //query that will demote documents
                .negativeBoost(0.2f); //negative boost
        SearchResponse sResponse = client.prepareSearch("bank")
                .setTypes("account")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .addSort("_score", SortOrder.DESC)
                .setQuery(qb)
                .setFrom(0).setSize(12)
                .setExplain(true)
                .execute().actionGet();
        SearchHits hits = sResponse.getHits();
        printWithScore(hits);
    }

    /**
     * Indices Query
     * Deprecated in 5.0.0.
     */
    @Test
    public void indicesQuery() {
    }
}
