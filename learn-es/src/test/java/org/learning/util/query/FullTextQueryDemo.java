package org.learning.util.query;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.SimpleQueryStringBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.learning.BaseTest;

import java.io.IOException;

/**
 * Created by anderson on 16-12-12.
 */
public class FullTextQueryDemo extends BaseTest
{
    /**
     * 高级全文查询通常用于在全文字段（如电子邮件正文）上运行全文查询。 他们了解如何分析查询的字段，并在执行之前将每个字段的分析器（或search_analyzer）应用于查询字符串。
     * Full text queries包括：
     * match query
     * multi_match query
     * common_terms query
     * query_string query
     * simple_query_string
     *
     setSearchType(SearchType searchType)：执行检索的类别，值为org.elasticsearch.action.search.SearchType的元素，SearchType是一个枚举类型的类，
     其值如下所示：
     QUERY_THEN_FETCH:查询是针对所有的块执行的，但返回的是足够的信息，而不是文档内容（Document）。结果会被排序和分级，基于此，只有相关的块的文档对象会被返回。由于被取到的仅仅是这些，故而返回的hit的大小正好等于指定的size。这对于有许多块的index来说是很便利的（返回结果不会有重复的，因为块被分组了）
     QUERY_AND_FETCH（查询并且取回）:最原始（也可能是最快的）实现就是简单的在所有相关的shard上执行检索并返回结果。每个shard返回一定尺寸的结果。由于每个shard已经返回了一定尺寸的hit，这种类型实际上是返回多个shard的一定尺寸的结果给调用者。
     这种搜索类型将查询和取回阶段合并成一个步骤。这是一个内部优化选项，当搜索请求的目标只是一个分片时可以使用，例如指定了routing（路由选择）值时。虽然你可以手动选择使用这个搜索类型，但是这么做基本上不会有什么效果。

     dfs搜索类型有一个预查询的阶段，它会从全部相关的分片里取回项目频数来计算全局的项目频数。
     DFS_QUERY_THEN_FETCH：与QUERY_THEN_FETCH相同，预期一个初始的散射相伴用来为更准确的score计算分配了的term频率。
     DFS_QUERY_AND_FETCH:与QUERY_AND_FETCH相同，预期一个初始的散射相伴用来为更准确的score计算分配了的term频率。
     SCAN：在执行了没有进行任何排序的检索时执行浏览。此时将会自动的开始滚动结果集。scan（扫描）搜索类型是和scroll（滚屏）API连在一起使用的，可以高效地取回巨大数量的结果。它是通过禁用排序来实现的。
     COUNT：只计算结果的数量，也会执行facet。count（计数）搜索类型只有一个query（查询）的阶段。当不需要搜索结果只需要知道满足查询的document的数量时，可以使用这个查询类型。

     注：SCAN和COUNT这两种search_type已经在5.x中移除了：
     对于count类型，以前是这样使用的：
     GET /my_index/_search?search_type=count
     {
        "aggs": {
            "my_terms": {
                "terms": {
                    "field": "foo"
                }
            }
        }
     }
     在5.x中应该改成如下形式：
     GET /my_index/_search
     {
        "size": 0,
        "aggs": {
            "my_terms": {
                "terms": {
                    "field": "foo"
                }
            }
        }
     }
     对于SCAN的使用请参考SearchApiDemo

     setSearchType(String searchType)，与setSearchType(SearchType searchType)类似，区别在于其值为字符串型的SearchType，
     值可为dfs_query_then_fetch、dfsQueryThenFetch、dfs_query_and_fetch、dfsQueryAndFetch、query_then_fetch、queryThenFetch、query_and_fetch或queryAndFetch；
     */

    /**
     * Match Query
     * @throws IOException
     *
     * match查询是一个标准查询，不管你需要全文本查询还是精确查询基本上都要用到它。
     * 如果你使用 match 查询一个全文本字段，它会在真正查询之前用分析器先分析match一下查询字符：
     * GET /_search
     *  {
     *      "match": {
     *          "tweet": "About Search"
     *      }
     *  }

     * 如果用match下指定了一个确切值，在遇到数字，日期，布尔值或者not_analyzed 的字符串时，它将为你搜索你给定的值：
     * { "match": { "age":    26           }}
     * { "match": { "date":   "2014-09-01" }}
     * { "match": { "public": true         }}
     * { "match": { "tag":    "full_text"  }}

     * 提示： 做精确匹配搜索时，你最好用过滤语句，因为过滤语句可以缓存数据。
     * match查询不可以用类似"+usid:2 +tweet:search"这样的语句。 它只能就指定某个确切字段某个确切的值进行搜索，而你要做的就是为它指定正确的字段名以避免语法错误。
     */
    @Test
    public void matchQuery() throws IOException {
        QueryBuilder qb = QueryBuilders.matchQuery("info", "my"); //设置字段 值, 值可以是boolean，string，数字
        SearchResponse searchResponse = client.prepareSearch(index)
                .setTypes(type).setQuery(qb)
                .setFrom(0).setSize(10)
                .execute().actionGet();

        SearchHits hits = searchResponse.getHits();
        print(hits);
    }

    /**
     * Multi Match Query
     *
     * multi_match查询允许你做match查询的基础上同时搜索多个字段：
     *  {
     *      "multi_match": {
     *      "query":    "full text search",
     *      "fields":   [ "title", "body" ]
     *      }
     *  }

     */
    @Test
    public void multiMatchQuery() {
        QueryBuilder qb = QueryBuilders.multiMatchQuery(
                "henry May",
                "firstname", "lastname", "employer"
        );
        SearchResponse searchResponse = client.prepareSearch("bank")
                .setTypes("account").setQuery(qb).setFrom(0).setSize(10)
                .execute().actionGet();

        SearchHits hits = searchResponse.getHits();
        print(hits);
    }

    /**
     * Common Terms Query
     *
     常见术语查询将查询项划分为两组：更重要的（即低频项）和不太重要的（即先前已经是停用词的高频项）。
     首先它搜索与更重要的术语匹配的文档。 这些是出现在较少文件中并对相关性具有更大影响的术语。
     然后，它对较不重要的术语执行第二次查询 - 经常出现并且对相关性影响较小的术语。 但是，不是计算所有匹配文档的相关性分数，而是仅计算已由第一个查询匹配的文档的_score。 以这种方式，高频项可以改进相关性计算，而不支付差的性能的成本。
     */
    @Test
    public void commonTermsQuery() {
        QueryBuilder qb = QueryBuilders.commonTermsQuery("address",
                "Herkimer Place");
        SearchResponse searchResponse = client.prepareSearch("bank")
                .setTypes("account").setQuery(qb).setFrom(0).setSize(10)
                .execute().actionGet();

        SearchHits hits = searchResponse.getHits();
        print(hits);
    }

    /**
     * Query String Query
     * 当未在查询字符串语法中显式指定要搜索的字段时，将使用index.query.default_field来导出要搜索的字段。 它默认为_all字段。
     * 如果_all字段被禁用，query_string查询将自动尝试确定索引映射中可查询的现有字段，并对这些字段执行搜索。 请注意，这不会包括嵌套的文档，使用嵌套查询来搜索这些文档。
     */
    @Test
    public void queryStringQuery() {
        //querystring
        QueryStringQueryBuilder queryStringQueryBuilder = QueryBuilders.queryStringQuery("(firstname:H* OR age:20) AND state:KS");
        // term query里面的range query可以单独作为查询QueryBuilder，也可以作为过滤器里面的过滤条件，其实任何QueryBuilder都可以作为过滤器里面的过滤条件
        RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("age").from(20).to(40).includeLower(true).includeUpper(false);
        SearchResponse sResponse = client.prepareSearch("bank")
//                .setTypes(null)
                // // 设置查询类型 1.SearchType.DFS_QUERY_THEN_FETCH = 精确查询 2.SearchType.SCAN 扫描查询,无序
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .addSort("_score", SortOrder.DESC)
                .setQuery(queryStringQueryBuilder)
                // 查询过滤器过滤
//                .setPostFilter(rangeQuery)
                // 分页应用
                .setFrom(0).setSize(10)
                // 设置是否按查询匹配度排序
                .setExplain(true)
                // 执行搜索,返回搜索响应信息
                .execute().actionGet();
        SearchHits hits = sResponse.getHits();
        print(hits);
        System.out.println("----------------------------");
        for (SearchHit hit : hits) {
//            System.out.println(hit.);
        }
        System.out.println(hits.getHits().toString());
    }

    /**
     * Simple Query String Query
     * 使用SimpleQueryParser来解析其上下文的查询。 与常规的query_string查询不同，simple_query_string查询将不会抛出异常，并丢弃查询的无效部分。
     * 查询没有String query严格，所以会获取更多的结果
     */
    @Test
    public void simpleQueryStringQuery() {
        SimpleQueryStringBuilder queryStringQueryBuilder = QueryBuilders.simpleQueryStringQuery("(firstname:H* OR age:20) AND state:KS");
        SearchResponse sResponse = client.prepareSearch("bank")
//                .setTypes(null)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .addSort("_score", SortOrder.DESC)
                .setQuery(queryStringQueryBuilder)
                .setFrom(0).setSize(10)
                .setExplain(true)
                .execute().actionGet();
        SearchHits hits = sResponse.getHits();
        print(hits);
    }
}
