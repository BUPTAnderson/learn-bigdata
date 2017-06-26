package org.learning.util.query;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.SpanContainingQueryBuilder;
import org.elasticsearch.index.query.SpanFirstQueryBuilder;
import org.elasticsearch.index.query.SpanMultiTermQueryBuilder;
import org.elasticsearch.index.query.SpanNearQueryBuilder;
import org.elasticsearch.index.query.SpanNotQueryBuilder;
import org.elasticsearch.index.query.SpanOrQueryBuilder;
import org.elasticsearch.index.query.SpanTermQueryBuilder;
import org.elasticsearch.index.query.SpanWithinQueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.learning.BaseTest;

/**
 * Created by anderson on 16-12-13.
 */
public class SpanQueryDemo extends BaseTest
{
    /**
     * 跨度查询是低级别位置查询，其提供对指定项的顺序和接近度的专家控制。 这些通常用于实施对法律文件或专利的非常具体的查询。
     * 跨度查询不能与非跨度查询混合（除了span_multi查询）。
     * 适用于text字段
     * 包括如下查询：
     * span_term query
     * span_multi query
     * span_first query
     * span_near query
     * span_or query
     * span_not query
     * span_containing query
     * span_within query
     */

    /**
     * Span Term Query
     */
    @Test
    public void spanTermQuery() {
        SpanTermQueryBuilder qb = QueryBuilders.spanTermQuery(
                "info",
                "is"
        );
        SearchResponse sResponse = client.prepareSearch(index)
                .setTypes(type)
                .addSort("_score", SortOrder.DESC)
                .setQuery(qb)
                .setFrom(0).setSize(12)
                .setExplain(true)
                .execute().actionGet();
        SearchHits hits = sResponse.getHits();
        printWithScore(hits);
    }

    /**
     * Span Multi Term Query
     */
    @Test
    public void spanMultiTermQuery() {
        SpanMultiTermQueryBuilder qb = QueryBuilders.spanMultiTermQueryBuilder(
                QueryBuilders.prefixQuery(
                        "name",
                        "An"
                )
//                QueryBuilders.rangeQuery("age")
//                        .from(20)
//                        .to(30)
//                        .includeLower(true)
//                        .includeUpper(false)
//                QueryBuilders.rangeQuery("age")
//                        .gte("10") // set from to 10 and includeLower to true
//                        .lt("40")
        );
        System.out.println(qb.toString());
        SearchResponse sResponse = client.prepareSearch(index)
                .setTypes(type)
//                .addSort("_score", SortOrder.DESC)
                .setQuery(qb)
//                .setFrom(0).setSize(12)
//                .setExplain(true)
                .execute().actionGet();
        SearchHits hits = sResponse.getHits();
        printWithScore(hits);
    }

    /**
     * Span First Query
     */
    @Test
    public void spanFirstQuery() {
        SpanFirstQueryBuilder qb = QueryBuilders.spanFirstQuery(
                QueryBuilders.spanTermQuery("info", "is"),
                3
        );

        SearchResponse sResponse = client.prepareSearch(index)
                .setTypes(type)
                .addSort("_score", SortOrder.DESC)
                .setQuery(qb)
                .setFrom(0).setSize(12)
                .setExplain(true)
                .execute().actionGet();
        SearchHits hits = sResponse.getHits();
        printWithScore(hits);
    }

    /**
     * Span Near Query
     * 待验证
     */
    @Test
    public void spanNearQuery() {
        SpanNearQueryBuilder qb = QueryBuilders.spanNearQuery(
                QueryBuilders.spanTermQuery("field", "value1"),
                12)
                .addClause(QueryBuilders.spanTermQuery("field", "value2"))
                .addClause(QueryBuilders.spanTermQuery("field", "value3"))
                .inOrder(false);

        SearchResponse sResponse = client.prepareSearch(index)
                .setTypes(type)
                .addSort("_score", SortOrder.DESC)
                .setQuery(qb)
                .setFrom(0).setSize(12)
                .setExplain(true)
                .execute().actionGet();
        SearchHits hits = sResponse.getHits();
        printWithScore(hits);
    }

    /**
     * Span Or Query
     * 待验证
     */
    @Test
    public void spanOrQuery() {
        SpanOrQueryBuilder qb = QueryBuilders.spanOrQuery(
                QueryBuilders.spanTermQuery("field", "value1"))
                .addClause(QueryBuilders.spanTermQuery("field", "value2"))
                .addClause(QueryBuilders.spanTermQuery("field", "value3"));

        SearchResponse sResponse = client.prepareSearch(index)
                .setTypes(type)
                .addSort("_score", SortOrder.DESC)
                .setQuery(qb)
                .setFrom(0).setSize(12)
                .setExplain(true)
                .execute().actionGet();
        SearchHits hits = sResponse.getHits();
        printWithScore(hits);
    }

    /**
     * Span Not Query
     * 待验证
     */
    @Test
    public void spanNotQuery() {
        SpanNotQueryBuilder qb = QueryBuilders.spanNotQuery(
                QueryBuilders.spanTermQuery("field", "value1"),
                QueryBuilders.spanTermQuery("field", "value2"));

        SearchResponse sResponse = client.prepareSearch(index)
                .setTypes(type)
                .addSort("_score", SortOrder.DESC)
                .setQuery(qb)
                .setFrom(0).setSize(12)
                .setExplain(true)
                .execute().actionGet();
        SearchHits hits = sResponse.getHits();
        printWithScore(hits);
    }

    /**
     * Span Containing Query
     * 待验证
     */
    @Test
    public void spanContainingQuery() {
        SpanContainingQueryBuilder qb = QueryBuilders.spanContainingQuery(
                QueryBuilders.spanNearQuery(QueryBuilders.spanTermQuery("field1", "bar"), 5)
                        .addClause(QueryBuilders.spanTermQuery("field1", "baz"))
                        .inOrder(true),
                QueryBuilders.spanTermQuery("field1", "foo"));

        SearchResponse sResponse = client.prepareSearch(index)
                .setTypes(type)
                .addSort("_score", SortOrder.DESC)
                .setQuery(qb)
                .setFrom(0).setSize(12)
                .setExplain(true)
                .execute().actionGet();
        SearchHits hits = sResponse.getHits();
        printWithScore(hits);
    }

    /**
     * Span Within Query
     * 待验证
     */
    @Test
    public void spanWithinQuery() {
        SpanWithinQueryBuilder qb = QueryBuilders.spanWithinQuery(
                QueryBuilders.spanNearQuery(QueryBuilders.spanTermQuery("field1", "bar"), 5)
                        .addClause(QueryBuilders.spanTermQuery("field1", "baz"))
                        .inOrder(true),
                QueryBuilders.spanTermQuery("field1", "foo"));

        SearchResponse sResponse = client.prepareSearch(index)
                .setTypes(type)
                .addSort("_score", SortOrder.DESC)
                .setQuery(qb)
                .setFrom(0).setSize(12)
                .setExplain(true)
                .execute().actionGet();
        SearchHits hits = sResponse.getHits();
        printWithScore(hits);
    }
}
