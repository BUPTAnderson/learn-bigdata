package org.learning.util.query;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.FuzzyQueryBuilder;
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.elasticsearch.index.query.PrefixQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.RegexpQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.index.query.TypeQueryBuilder;
import org.elasticsearch.index.query.WildcardQueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.learning.BaseTest;

/**
 * Created by anderson on 16-12-12.
 */
public class TermLevelQueryDemo extends BaseTest
{
    /**
     * 我们可以认为Terms level queries是过滤查询语句！！！
     *
     * 虽然全文查询将在执行之前分析查询字符串，但是term-level queries 对存储在反向索引中的确切术语进行操作。
     * 这些查询通常用于结构化数据，如数字，日期和枚举，而不是全文本字段。 或者，它们允许您绘在分析处理之前进行低水平的查询。
     * Terms level queries包含如下查询：
     * term query
     * terms query
     * range query
     * exists query
     * prefix query
     * wildcard query
     * regexp query
     * fuzzy query
     * type query
     * ids query
     */

    /**
     * Term Query
     * term主要用于精确匹配哪些值，比如数字，日期，布尔值或 not_analyzed的字符串(未经分析的文本数据类型)：
     * { "term": { "age":    26           }}
     * { "term": { "date":   "2014-09-01" }}
     * { "term": { "public": true         }}
     * { "term": { "tag":    "full_text"  }}
     */
    @Test
    public void termQuery() {
        //termquery只能查询一个单词！！！！，termsQuery可以查询多个单词，但是每个单词作为一个值
        TermQueryBuilder qb = QueryBuilders.termQuery("info", "buick"); //只能查询一个汉字或一个小写的单词，单个单词时，是完整的单个单词匹配，只抽取出几个字母无法匹配
        SearchResponse sResponse = client.prepareSearch(index)
                .setTypes(type)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .addSort("_score", SortOrder.DESC) //针对text字段查询的时候 score不一样
                .setQuery(qb)
                .setFrom(0).setSize(12)
                .setExplain(true)
                .execute().actionGet();
        SearchHits hits = sResponse.getHits();
        printWithScore(hits);
    }

    /**
     * Terms Query
     * terms 跟 term 有点类似，但 terms 允许指定多个匹配条件。
     */
    @Test
    public void termsQuery() {
        //对多个单词进行查询，看指定的字段是否包含给定的一个或多个单词
        TermsQueryBuilder qb = QueryBuilders.termsQuery("name", "Henry", "Lily"); // 查询name字段为Henry或Lily的document
        SearchResponse sResponse = client.prepareSearch(index)
                .setTypes(type)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
//                .addSort("_score", SortOrder.DESC)// score都一样，所以按照score排序无意义
                .setQuery(qb)
                .setFrom(0).setSize(12)
                .setExplain(true)
                .execute().actionGet();
        SearchHits hits = sResponse.getHits();
        printWithScore(hits);
    }

    /**
     * Range Query
     * range过滤允许我们按照指定范围查找一批数据：
     *
     * GET _search
     * {
     *      "query": {
     *          "range" : {
     *              "age" : {
     *                  "gte" : 10,
     *                  "lte" : 20,
     *                  "boost" : 2.0   //返回的document的score都为2.0
     *              }
     *          }
     *      }
     * }

     * 范围操作符包含：
     * gt :: 大于
     * gte:: 大于等于
     * lt :: 小于
     * lte:: 小于等于
     *
     * 发现结果score都为1
     */
    @Test
    public void rangQuery() {
        RangeQueryBuilder qb = QueryBuilders.rangeQuery("age")
                .from(20)
                .to(30)
                .includeLower(true)
                .includeUpper(false);
        SearchResponse sResponse = client.prepareSearch(index)
                .setTypes(type)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
//                .addSort("_score", SortOrder.DESC)// score都一样，所以按照score排序无意义
                .setQuery(qb)
                .setFrom(0).setSize(12)
                .setExplain(true)
                .execute().actionGet();
        SearchHits hits = sResponse.getHits();
        printWithScore(hits);

        //
        RangeQueryBuilder qb2 = QueryBuilders.rangeQuery("age")
                .gte("10") // set from to 10 and includeLower to true
                .lt("20"); // set to to 20 and includeUpper to false
    }

    /**
     * Exists Query
     * exists 和 missing 过滤可以用于查找文档中是否包含指定字段或没有某个字段，类似于SQL语句中的IS_NULL条件
     * {
     *      "exists":   {
     *          "field":    "title"
     *      }
     * }
     * 这两个过滤只是针对已经查出一批数据来，但是想区分出某个字段是否存在的时候使用。
     *
     * 发现结果score都为1(过滤)
     *
     */
    @Test
    public void existsQuery() {
        ExistsQueryBuilder qb = QueryBuilders.existsQuery("info");
        SearchResponse sResponse = client.prepareSearch(index)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
//                .addSort("_score", SortOrder.DESC)// score都是1，所以按照score排序无意义
                .setQuery(qb)
                .setFrom(0).setSize(12)
                .setExplain(true)
                .execute().actionGet();
        SearchHits hits = sResponse.getHits();
        printWithScore(hits);
    }

    /**
     * Prefix Query
     * 查询哪些document的field以个给定的字符开头
     * 发现结果score都为1
     */
    @Test
    public void prefixQuery() {
        // 如果改成an则查询结果为空
        PrefixQueryBuilder qb = QueryBuilders.prefixQuery(
                "firstname.keyword",
                "Al"
        );
        SearchResponse sResponse = client.prepareSearch("bank")
                .setTypes("account")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
//                .addSort("_score", SortOrder.DESC)// score都是1，所以按照score排序无意义
                .setQuery(qb)
                .setFrom(0).setSize(12)
                .setExplain(true)
                .execute().actionGet();
        SearchHits hits = sResponse.getHits();
        printWithScore(hits);
    }

    /**
     * Wildcard Query
     * 查询哪些document的field与给定的通配符一致
     * 发现结果score都为1
     */
    @Test
    public void wildcardQuery() {
        WildcardQueryBuilder qb = QueryBuilders.wildcardQuery("firstname.keyword", "K?r*");
//        WildcardQueryBuilder qb = QueryBuilders.wildcardQuery("firstname", "k?r*");
        SearchResponse sResponse = client.prepareSearch("bank")
                .setTypes("account")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
//                .addSort("_score", SortOrder.DESC)// score都是1，所以按照score排序无意义
                .setQuery(qb)
                .setFrom(0).setSize(12)
                .setExplain(true)
                .execute().actionGet();
        SearchHits hits = sResponse.getHits();
        printWithScore(hits);
    }

    /**
     * Regexp Query
     * 正则表达式查询
     * 发现结果score都为1
     */
    @Test
    public void regexpQuery() {
        RegexpQueryBuilder qb = QueryBuilders.regexpQuery(
                "firstname.keyword",
                "K.*r.*");
        SearchResponse sResponse = client.prepareSearch("bank")
                .setTypes("account")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
//                .addSort("_score", SortOrder.DESC)// score都是1，所以按照score排序无意义
                .setQuery(qb)
                .setFrom(0).setSize(12)
                .setExplain(true)
                .execute().actionGet();
        SearchHits hits = sResponse.getHits();
        printWithScore(hits);
    }

    /**
     * Fuzzy Query 模糊查询
     * Deprecated in 5.0.0.
     */
    @Test
    public void fuzzyQuery() {
        FuzzyQueryBuilder qb = QueryBuilders.fuzzyQuery(
                "info",
                "my"
        );
        SearchResponse sResponse = client.prepareSearch(index)
                .setTypes(type)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
//                .addSort("_score", SortOrder.DESC)//
                .setQuery(qb)
                .setFrom(0).setSize(12)
                .setExplain(true)
                .execute().actionGet();
        SearchHits hits = sResponse.getHits();
        print(hits);
    }

    /**
     * Type Query
     * 查询索引下为指定type的document
     */
    @Test
    public void typeQuery() {
        TypeQueryBuilder qb = QueryBuilders.typeQuery("my_type");
        SearchResponse sResponse = client.prepareSearch(index)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
//                .addSort("_score", SortOrder.DESC)//
                .setQuery(qb)
                .setFrom(0).setSize(12)
                .setExplain(true)
                .execute().actionGet();
        SearchHits hits = sResponse.getHits();
        print(hits);
    }

    /**
     * Ids Query
     * 直接查询指定id的document
     * 发现结果score都为1
     *
     */
    @Test
    public void idsQuery() {
        IdsQueryBuilder qb = QueryBuilders.idsQuery(index, type)
                .addIds("1", "4", "10");

        //type is optional
//        IdsQueryBuilder qb = QueryBuilders.idsQuery()
//                .addIds("1", "4", "100");

        SearchResponse sResponse = client.prepareSearch(index)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
//                .addSort("_score", SortOrder.DESC)//
                .setQuery(qb)
                .setFrom(0).setSize(12)
                .setExplain(true)
                .execute().actionGet();
        SearchHits hits = sResponse.getHits();
        printWithScore(hits);
    }
}
