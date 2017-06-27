package org.learning;

import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.SimpleQueryStringBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.global.Global;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;

/**
 * Created by anderson on 17-3-8.
 */
public class TestLogService
        extends BaseTest
{
    @Test
    public void testQuery()
    {
        RangeQueryBuilder qb = QueryBuilders.rangeQuery("timestamp")
                .from("2017-03-07 01:18:35.614")
                .to("2017-03-08 20:17:35.614")
                .includeLower(true)
                .includeUpper(false);
        SearchResponse sResponse = client.prepareSearch("xdata-log")
                .setTypes("log")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
//                .addSort("_score", SortOrder.DESC)// score都一样，所以按照score排序无意义
                .addSort("timestamp", SortOrder.DESC)
                .setQuery(qb)
                .setFrom(0).setSize(12)
                .setExplain(true)
                .execute().actionGet();
        SearchHits hits = sResponse.getHits();
        printWithScore(hits);
    }

    // 实际查询
    @Test
    public void testCompoundQuery()
    {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery("host", "192.168.178.80"))
                .filter(QueryBuilders.matchQuery("path", "/export/Domains/idata-ras-api.jcloud.com/server1/logs"))
                .filter(QueryBuilders.rangeQuery("timestamp")
//                        .from("2017-03-07 08:21:35.614")
//                        .to("2017-03-08 19:17:35.614")
//                        .includeLower(true)
//                        .includeUpper(false)
                                .gte("2017-03-07 08:21:35.614")
                                .lte("2017-03-08 19:17:35.614")
                )
                .filter(QueryBuilders.matchQuery("tag", "test"))
                .must(QueryBuilders.matchQuery("context", "cpp"));
//                .should(QueryBuilders.queryStringQuery("(firstname:H* OR age:20) AND state:KS"));
//                .must(QueryBuilders.termQuery("address", "street"))//必须
//                .mustNot(QueryBuilders.termQuery("state.keyword", "NM"))//排除
//                .should(QueryBuilders.termQuery("employer.keyword", "Quantasis")) //可选
//                .filter(QueryBuilders.termQuery("gender.keyword", "F")); //a query that must appear in the matching documents but doesn’t contribute to scoring.
        SearchResponse sResponse = client.prepareSearch("xdata-log")
                .setTypes("log")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .addSort("timestamp", SortOrder.DESC)
                .setQuery(boolQueryBuilder)
                .setFrom(0).setSize(12)
                .setExplain(true)
                .execute().actionGet();
        SearchHits hits = sResponse.getHits();
        printWithScore(hits);
    }

    // date histogram
    @Test
    public void dateHistogramAggregation()
    {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery("host", "192.168.178.80"))
                .filter(QueryBuilders.matchQuery("path", "/export/Domains/idata-ras-api.jcloud.com/server1/logs"))
                .filter(QueryBuilders.rangeQuery("timestamp")
                        .from("2017-03-07 08:21:35.614")
                        .to("2017-03-08 19:17:35.614")
                        .includeLower(true)
                        .includeUpper(false))
                .filter(QueryBuilders.matchQuery("tag", "test"))
                .must(QueryBuilders.matchQuery("context", "cpp"));

        AggregationBuilder aggregation =
                AggregationBuilders
                        .dateHistogram("agg")
                        .field("timestamp")
                        .dateHistogramInterval(DateHistogramInterval.DAY);
        // set an interval of 10 days
//        AggregationBuilder aggregation =
//                AggregationBuilders
//                        .dateHistogram("agg")
//                        .field("dateOfBirth")
//                        .dateHistogramInterval(DateHistogramInterval.days(10));

        SearchResponse response = client.prepareSearch("xdata-log")
                .setTypes("log")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .addSort("timestamp", SortOrder.DESC)
                .setQuery(boolQueryBuilder)
                .setFrom(0).setSize(12)
                .setExplain(true)
                .addAggregation(aggregation)
                .get();

//        SearchResponse sResponse = client.prepareSearch("xdata-log")
//                .setTypes("log")
//                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
//                .addSort("timestamp", SortOrder.DESC)
//                .setQuery(boolQueryBuilder)
//                .setFrom(0).setSize(12)
//                .setExplain(true)
//                .execute().actionGet();
        SearchHits hits = response.getHits();
//        System.out.println(response.toString());
        printWithScore(hits);
//        System.out.println(hits.getHits());

        System.out.println("---------------------------------");
        Histogram agg = response.getAggregations().get("agg");
        System.out.println(agg.getBuckets().toString());
        System.out.println("*********************************");

        // For each entry
        for (Histogram.Bucket entry : agg.getBuckets()) {
            DateTime key = (DateTime) entry.getKey();    // Key: 2017-03-07T00:00:00.000Z
            String keyAsString = entry.getKeyAsString(); // Key as String: 2017-03-07 00:00:00.000
            long docCount = entry.getDocCount();         // Doc count: 1
            System.out.println("key:" + key + ", keyAsString" + keyAsString + ", docCount:" + docCount);
        }
    }

    // 分桶获取ip，tag，path
    @Test
    public void testGroupByIP()
    {
        AggregationBuilder aggregation = AggregationBuilders
                .terms("ip")
                .field("host")
                .size(10); //Sets the size - indicating how many term buckets should be returned 默认为10，即取前几个分桶返回
        SearchResponse response = client.prepareSearch("xdata-log")
                .setTypes("log")
                .addAggregation(aggregation)
                .get();
        Terms genders = response.getAggregations().get("ip");

        // For each entry
        for (Terms.Bucket entry : genders.getBuckets()) {
            System.out.println(entry.getKey());      // Term
            System.out.println(entry.getDocCount()); // Doc count
        }
    }

    @Test
    public void testGroupByIP2()
    {
        AggregationBuilder aggregation = AggregationBuilders
                .global("agg")
                .subAggregation(AggregationBuilders.terms("ip").field("host"));
        SearchResponse response = client.prepareSearch("xdata-log")
                .setTypes("log")
                .addAggregation(aggregation)
                .get();
        Global agg = response.getAggregations().get("agg");
        long count = agg.getDocCount(); // Doc count
        System.out.println("count:" + count); //22
        StringTerms stringTerms = agg.getAggregations().get("ip");
        stringTerms.getBuckets().forEach((Terms.Bucket bucket) -> System.out.println(bucket.getKeyAsString())); // 4个ip
        System.out.println(stringTerms.getBuckets().size()); // 4
    }

    @Test
    public void testGroupByTag()
    {
        AggregationBuilder aggregation = AggregationBuilders
                .terms("tag")
                .field("tag.keyword")
                .size(10); //Sets the size - indicating how many term buckets should be returned 默认为10，即取前几个分桶返回
        SearchResponse response = client.prepareSearch("xdata-log")
                .addAggregation(aggregation)
                .get();
        Terms genders = response.getAggregations().get("tag");

        // For each entry
        for (Terms.Bucket entry : genders.getBuckets()) {
            System.out.println(entry.getKey());      // Term
            System.out.println(entry.getDocCount()); // Doc count
        }
    }

    @Test
    public void testGroupByTag2()
    {
        AggregationBuilder aggregation = AggregationBuilders
                .global("agg")
                .subAggregation(AggregationBuilders.terms("tag").field("tag.keyword"));
        SearchResponse response = client.prepareSearch("xdata-log")
                .setTypes("log")
                .addAggregation(aggregation)
                .get();
        Global agg = response.getAggregations().get("agg");
        long count = agg.getDocCount(); // Doc count
        System.out.println("count:" + count); //22
        StringTerms stringTerms = agg.getAggregations().get("tag");
        stringTerms.getBuckets().forEach((Terms.Bucket bucket) -> System.out.println(bucket.getKeyAsString())); // test, huabei, online
        System.out.println(stringTerms.getBuckets().size()); // 3
    }

    @Test
    public void testGroupByPath()
    {
        AggregationBuilder aggregation = AggregationBuilders
                .terms("path")
                .field("path.keyword")
                .size(10); //Sets the size - indicating how many term buckets should be returned 默认为10，即取前几个分桶返回
        SearchResponse response = client.prepareSearch("xdata-log")
                .addAggregation(aggregation)
                .get();
        Terms genders = response.getAggregations().get("path");

        // For each entry
        for (Terms.Bucket entry : genders.getBuckets()) {
            System.out.println(entry.getKey());      // Term
            System.out.println(entry.getDocCount()); // Doc count
        }
    }

    @Test
    public void testGroupByPath2()
    {
        AggregationBuilder aggregation = AggregationBuilders
                .global("agg")
                .subAggregation(AggregationBuilders.terms("path").field("path.keyword"));
        SearchResponse response = client.prepareSearch("xdata-log")
                .setTypes("log")
                .addAggregation(aggregation)
                .get();
        Global agg = response.getAggregations().get("agg");
        long count = agg.getDocCount(); // Doc count
        System.out.println("count:" + count); //22
        StringTerms stringTerms = agg.getAggregations().get("path");
        stringTerms.getBuckets().forEach((Terms.Bucket bucket) -> System.out.println(bucket.getKeyAsString())); // test, huabei, online
        System.out.println(stringTerms.getBuckets().size()); // 3
    }

    //查询上下文, 实例：{"path":"/export/Domains/idata-ras-api.jcloud.com/server1/logs","host":"192.168.178.80","context":"hello javascript","tag":"test","timestamp":"2017-03-08 16:17:35.614"}
    // path:"/export/Domains/idata-ras-api.jcloud.com/server1/logs"
    // host:192.168.178.80
    // timestamp:"2017-03-08 16:17:35.614"
    // tag
    //
    @Test
    public void testQueryContext()
    {
        BoolQueryBuilder boolQueryBuilder2 = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery("host", "192.168.178.80"))
                .filter(QueryBuilders.termQuery("path.keyword", "/export/Domains/idata-ras-api.jcloud.com/server1/logs"))
                .filter(QueryBuilders.rangeQuery("timestamp").gt("2017-03-08 12:17:35.614"));

        SearchResponse sResponse2 = client.prepareSearch("xdata-log")
                .setTypes("log")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .addSort("timestamp", SortOrder.ASC)
                .setQuery(boolQueryBuilder2)
                .setFrom(0).setSize(10)
                .setExplain(true)
                .execute().actionGet();
        SearchHits hits2 = sResponse2.getHits();
        LinkedList<String> linkedList = new LinkedList<>();
        for (SearchHit hit : hits2) {
            linkedList.add(hit.getSourceAsString());
        }
//        printWithScore(hits2);
        Collections.reverse(linkedList);

        System.out.println("------------------");

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery("host", "192.168.178.80"))
                .filter(QueryBuilders.termQuery("path.keyword", "/export/Domains/idata-ras-api.jcloud.com/server1/logs"))
                .filter(QueryBuilders.rangeQuery("timestamp").lte("2017-03-08 12:17:35.614"));

        SearchResponse sResponse = client.prepareSearch("xdata-log")
                .setTypes("log")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .addSort("timestamp", SortOrder.DESC)
                .setQuery(boolQueryBuilder)
                .setFrom(0).setSize(10)
                .setExplain(true)
                .execute().actionGet();
        SearchHits hits = sResponse.getHits();

        for (SearchHit hit : hits) {
            linkedList.add(hit.getSourceAsString());
        }

        for (String str : linkedList) {
            System.out.println(str);
        }
    }

    @Test
    public void test()
    {
        BoolQueryBuilder after = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery("host", "192.168.178.27"))
                .filter(QueryBuilders.termQuery("path.keyword", "/export/Domains/idata-ras-api.jcloud.com/server1/logs"))
                .filter(QueryBuilders.rangeQuery("timestamp").gt("2017-03-08 18:17:35.614"));

        SearchResponse afterResponse = client.prepareSearch("fba56f03-22f7-4047-9ba7-22a0bb4b4f1c")
//                .setTypes()
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .addSort("timestamp", SortOrder.ASC)
                .setQuery(after)
                .setFrom(0).setSize(10)
//                .setExplain(true)
                .execute().actionGet();
        SearchHits hits = afterResponse.getHits();
        print(hits);
    }

    @Test
    public void test2()
    {
        CreateIndexResponse response = client.admin().indices().prepareCreate("xdata-log")
                .setSettings(Settings.builder()
                        .put("index.number_of_shards", 3)
                        .put("index.number_of_replicas", 1)
                )
                .addMapping("_default_", "{\n" +
                        "    \"_default_\": {\n" +
                        "        \"_all\": {\n" +
                        "            \"enabled\": true,\n" +
                        "                \"analyzer\": \"ik_max_word\"\n" +
                        "        },\n" +
                        "        \"_source\": {\n" +
                        "            \"enabled\": true\n" +
                        "        },\n" +
                        "        \"properties\": {\n" +
                        "            \"host\": {\n" +
                        "                \"type\": \"ip\",\n" +
                        "                \"fields\": {\n" +
                        "                    \"keyword\": {\n" +
                        "                        \"type\": \"keyword\",\n" +
                        "                        \"ignore_above\": 256\n" +
                        "                    }\n" +
                        "                }\n" +
                        "            },\n" +
                        "            \"path\": {\n" +
                        "                \"type\": \"text\",\n" +
                        "                \"fields\": {\n" +
                        "                    \"keyword\": {\n" +
                        "                        \"type\": \"keyword\",\n" +
                        "                        \"ignore_above\": 256\n" +
                        "                    }\n" +
                        "                }\n" +
                        "            },\n" +
                        "            \"tag\": {\n" +
                        "                \"type\": \"text\",\n" +
                        "                \"fields\": {\n" +
                        "                    \"keyword\": {\n" +
                        "                        \"type\": \"keyword\",\n" +
                        "                        \"ignore_above\": 256\n" +
                        "                    }\n" +
                        "                }\n" +
                        "            },\n" +
                        "            \"timestamp\": {\n" +
                        "                \"type\": \"date\",\n" +
                        "                \"format\":\"yyyy-MM-dd HH:mm:ss.SSS||yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis\"\n" +
                        "            },\n" +
                        "            \"content\": {\n" +
                        "                \"type\": \"text\",\n" +
                        "                \"analyzer\": \"ik_max_word\"\n" +
                        "            }\n" +
                        "        }\n" +
                        "    }\n" +
                        "}")
                .addAlias(new Alias("182513da-c4ca-4a88-bb26-9c1fd6de0390"))
                .get();
        System.out.println(response.isAcknowledged());
    }

    @Test
    public void queryStringQuery()
    {
        //querystring
        QueryStringQueryBuilder queryStringQueryBuilder = QueryBuilders.queryStringQuery("content:controller");
        SimpleQueryStringBuilder simpleQueryStringBuilder = QueryBuilders.simpleQueryStringQuery("controller");
        QueryBuilder qb = QueryBuilders.multiMatchQuery(
                "controller",
                "path", "host", "content", "tag", "timestamp"
        );
        // term query里面的range query可以单独作为查询QueryBuilder，也可以作为过滤器里面的过滤条件，其实任何QueryBuilder都可以作为过滤器里面的过滤条件
//        RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("age").from(20).to(40).includeLower(true).includeUpper(false);
        SearchResponse sResponse = client.prepareSearch("xdata-log")
//                .setTypes(null)
                // // 设置查询类型 1.SearchType.DFS_QUERY_THEN_FETCH = 精确查询 2.SearchType.SCAN 扫描查询,无序
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .addSort("_score", SortOrder.DESC)
                .setQuery(qb)
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

    @Test
    public void testCount()
    {
//        Client client = ElasticSearchUtil.getClient();
        String host = null;
        String tag = "";
        String queryString = "controller";
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .filter(QueryBuilders.rangeQuery("timestamp")
                        .gte("2017-04-01 08:02:58")
                        .lte("2017-04-02 09:02:58")
                );
        if (host != null && !"".equals(host.trim())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("host", host));
        }
        if (tag != null && !"".equals(tag.trim())) {
            boolQueryBuilder.filter(QueryBuilders.matchQuery("tag.keyword", tag));
        }
        if (queryString == null) {
            boolQueryBuilder.must(QueryBuilders.matchAllQuery());
        }
        else {
            boolQueryBuilder.must(QueryBuilders.queryStringQuery(queryString).boost(5.0f));
        }

        SearchResponse response = client.prepareSearch("xdata-log")
//                .setTypes(esType)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
//                .addSort("timestamp", SortOrder.DESC)
                .setQuery(boolQueryBuilder)
//                .setSearchType(SearchType.COUNT)//设置查询类型，有的版本可能过期
                .setSize(0)//设置返回结果集为0
                .get();
        System.out.println(response.getHits().getTotalHits());
    }
}
