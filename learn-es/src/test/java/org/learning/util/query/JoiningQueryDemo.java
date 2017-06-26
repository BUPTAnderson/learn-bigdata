package org.learning.util.query;

import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.HasChildQueryBuilder;
import org.elasticsearch.index.query.HasParentQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.learning.BaseTest;

/**
 * Created by anderson on 16-12-13.
 */
public class JoiningQueryDemo extends BaseTest
{
    /**
     * 在像Elasticsearch这样的分布式系统中执行完全的SQL样式连接是非常昂贵的。 相反，Elasticsearch提供了两种形式的连接，它们被设计为水平缩放。
     */

    /**
     * Nested Query
     * 文档可能包含嵌套类型的字段。 这些字段用于索引对象数组，其中每个对象可以作为独立文档查询（使用嵌套查询）。
     */
    @Test
    public void nestedQuery() {
        NestedQueryBuilder qb = QueryBuilders.nestedQuery(
                "user",                                     // path to nested document
                QueryBuilders.boolQuery()                   // 您的查询。 查询中引用的任何字段必须使用完整路径（完全限定）。
                        .must(QueryBuilders.matchQuery("user.last", "Smith"))
                        .must(QueryBuilders.rangeQuery("user.age").gt(5)),
                ScoreMode.Avg                               // 得分模式可以是ScoreMode.Max，ScoreMode.Min，ScoreMode.Total，ScoreMode.Avg或ScoreMode.None
        );
        SearchResponse sResponse = client.prepareSearch(index)
                .setTypes("my_type")
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
     *
     * 单个索引中的两个文档类型之间可能存在父子关系。 has_child查询返回其子文档与指定查询匹配的父文档，而has_parent查询返回其父文档与指定查询匹配的子文档。
     *
     * Has Child Query:如果子文档满足某个条件，则返回对应的父类文档
     */
    @Test
    public void hasChildQuery() {
        HasChildQueryBuilder qb = QueryBuilders.hasChildQuery(
                "answer",   // child type
                QueryBuilders.termQuery("body", "linux"), //对child type中的文档的查询条件
                ScoreMode.Avg
        );
        SearchResponse sResponse = client.prepareSearch("child_example")
                .setTypes("question") //父type
//                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .addSort("_score", SortOrder.DESC)
                .setQuery(qb)
                .setFrom(0).setSize(12)
                .setExplain(true)
                .execute().actionGet();
        SearchHits hits = sResponse.getHits();
        printWithScore(hits);
    }

    /**
     * Has Parent Query:如果父文档满足某个条件，则返回对应的子类文档
     */
    @Test
    public void hasParentQuery() {
        HasParentQueryBuilder qb = QueryBuilders.hasParentQuery(
                "question", // parent type
                QueryBuilders.termQuery("tags", "windows"), // 对父文档的查询条件
                false
        );
        SearchResponse sResponse = client.prepareSearch("child_example")
                .setTypes("answer") //child type
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .addSort("_score", SortOrder.DESC)
                .setQuery(qb)
                .setFrom(0).setSize(12)
                .setExplain(true)
                .execute().actionGet();
        SearchHits hits = sResponse.getHits();
        printWithScore(hits);
    }
}
