package org.learning;

import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.mustache.SearchTemplateRequestBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalDateHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * Created by anderson on 16-12-8.
 */
public class SearchApiDemo
        extends BaseTest
{
    /**
     * The count search type was deprecated since version 2.0.0 and is now removed.
     * The scan search type was deprecated since version 2.1.0 and is now removed.
     * 所以在5.X版本中已经没有scan和count这种search_type了
     * scroll发生了改变：
     * 2.1.0及之前：
     * GET /shakespeare/line/_search?pretty&search_type=scan&scroll=1m
     * {
     * "query": { "match_all": {}},
     * "size":  100
     * }
     * 循环获取获取数据，每次将新的scroll_id放到参数后面
     * GET /shakespeare/line/_search?scroll=1m&scroll_id=DnF1ZXJ5VGhlbkZldGNoBQAAAAAAAthpFnFoWTZzRTF5VHhPYzA2YjdPTDRqVUEAAAAAABXiCRY5alZmcTdsQ1RRT3lUU3hxNUMwOVRBAAAAAAAC2GoWcWhZNnNFMXlUeE9jMDZiN09MNGpVQQAAAAAAFUK3FjRyRm5BLTNQVGlHcDJBZ2t2c0xpY3cAAAAAABXiChY5alZmcTdsQ1RRT3lUU3hxNUMwOVRB
     * <p>
     * 在5.x中应该这样使用（每次返回100条数据）：
     * GET /shakespeare/line/_search?scroll=1m
     * {
     * "query": { "match_all": {}},
     * "size":  100,
     * "sort": [
     * "_doc"
     * ]
     * }
     * <p>
     * POST  /_search/scroll
     * {
     * "scroll" : "1m",
     * "scroll_id" : "DnF1ZXJ5VGhlbkZldGNoBQAAAAAAFULiFjRyRm5BLTNQVGlHcDJBZ2t2c0xpY3cAAAAAAALYaxZxaFk2c0UxeVR4T2MwNmI3T0w0alVBAAAAAAAV4jUWOWpWZnE3bENUUU95VFN4cTVDMDlUQQAAAAAAFeI0FjlqVmZxN2xDVFFPeVRTeHE1QzA5VEEAAAAAABVC4xY0ckZuQS0zUFRpR3AyQWdrdnNMaWN3"
     * }
     * POST可以改为GET，注意一定不要带index和type，即POST /shakespeare/line/_search/scroll是错误的！！！
     */
    @Test
    public void scrollAPI()
    {
        int i = 1;
        QueryBuilder qb = termQuery("speaker", "PRINCE HENRY");

        SearchResponse scrollResp = client.prepareSearch("shakespeare")
                .setTypes("line")
                .addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)
                // scroll: 滚屏的终止时间会在我们每次执行滚屏请求时刷新，所以他只需要给我们足够的时间来处理当前批次的结果而不是所有的匹配查询的document。
                .setScroll(new TimeValue(60000)) // 设置的时间只会对当次请求有影响, 所以每次请求可以单独设置过期时间
                .setQuery(qb)
                .setSize(100).get(); //max of 100 hits will be returned for each scroll
        //Scroll until no hits are returned
        do {
            for (SearchHit hit : scrollResp.getHits().getHits()) {
                //Handle the hit...
                String json = hit.getSourceAsString();
                System.out.println(i++ + "-" + json);
            }
            System.out.println("--------------------------------");
            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
        }
        while (scrollResp.getHits().getHits().length != 0); // Zero hits mark the end of the scroll and the while loop.
    }

    /**
     * Multi Search API 在一个API里面执行多个搜索请求
     */
    @Test
    public void multiSearchAPI()
    {
        int i = 1;
        QueryBuilder qb = QueryBuilders.queryStringQuery("Lucy");
        SearchRequestBuilder srb1 = client
                .prepareSearch(index).setTypes(type).setQuery(qb).setSize(10);
        // 直接设置QueryBuilder实例
        SearchRequestBuilder srb2 = client
                .prepareSearch("shakespeare").setTypes("line").setQuery(QueryBuilders.matchQuery("speaker", "WESTMORELAND")).setSize(100);

        // 调用prepareMultiSearch
        MultiSearchResponse sr = client.prepareMultiSearch()
                .add(srb1)
                .add(srb2)
                .get();

        // You will get all individual responses from MultiSearchResponse#getResponses()
        long nbHits = 0;
        for (MultiSearchResponse.Item item : sr.getResponses()) {
            // 一个SearchRequestBuilder对应一个response
            SearchResponse response = item.getResponse();
            SearchHits hits = response.getHits();
            if (null == hits || hits.totalHits() == 0) {
                log.error("使用MultiSearch没有查询到任何结果！");
            }
            else {
                System.out.println(i++ + "-totalHits:" + hits.getTotalHits());
                nbHits += hits.getTotalHits();
                for (SearchHit hit : hits) {
                    String json = hit.getSourceAsString();
                    System.out.println(json);
                }
            }
        }
        System.out.println("nbHits:" + nbHits);
    }

    /**
     * 待验证
     */
    @Test
    public void aggregationsAPI()
    {
        SearchResponse sr = client.prepareSearch()
                .setQuery(QueryBuilders.matchAllQuery())
                .addAggregation(
                        AggregationBuilders.terms("agg1").field("field")
                )
                .addAggregation(
                        AggregationBuilders.dateHistogram("agg2")
                                .field("birth")
                                .dateHistogramInterval(DateHistogramInterval.YEAR)
                )
                .get();

        // Get your facet results
        Terms agg1 = sr.getAggregations().get("agg1");
        InternalDateHistogram agg2 = sr.getAggregations().get("agg2");
    }

    /**
     * 查询在分片上达到设定的条目时,该分片上的查询终止
     * setTerminateAfter
     * 每个分片收集的文档的最大数量，到达时，查询执行将提前终止。 如果设置，您将能够通过在SearchResponse对象中请求isTerminatedEarly（）来检查操作是否提前终止
     */
    @Test
    public void treminateAfterAPI()
    {
        SearchResponse sr = client.prepareSearch("shakespeare")
                .setTypes("line")
                .setQuery(QueryBuilders.matchQuery("play_name", "Henry IV"))
                .setTerminateAfter(100)
                .get();

        // setTerminateAfter(100) 则totalHits为500，因为索引有5个分片，没分分片查到100条后就终止，所以一共查询到500条记录， 查询在每个分片上都提前终止了
        // 输出We finished early
        //
        // setTerminateAfter(1000) 则totalHits为3182，是真正的条目数量，因为每个分片有均匀存储的(理论上，如果数据不均匀，有可能某个分片上数据大于1000条，查询到1000条时该分片终止，则这样获取的数量也不对)，
        // 没有分片大道100条，则查询没有在任何分片上提前终止
        // 不会输出We finished early
        System.out.println("totalHits:" + sr.getHits().getTotalHits()); // 500
        if (sr.isTerminatedEarly()) {
            // We finished early
            System.out.println("We finished early");
        }
    }

    /**
     * Search Template: 搜索模板
     * 模板在elasticsearch的 config/scripts/template_test.mustache 文件中，文件内容如下：
     * {
     * "query" : {
     * "match" : {
     * "sex" : "{{param_gender}}"
     * }
     * }
     * }
     */
    @Test
    public void searchTemplateAPI()
    {
        // 定义模板参数
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("param_gender", "男");
        SearchResponse sr = new SearchTemplateRequestBuilder(client)
                .setScript("template_test") // 模板名字, 与文件名对应:template_test.mustache
//                .setScriptType(ScriptService.ScriptType.FILE) // 模板为File
//                .setScriptType(ScriptService.SCRIPT_AUTO_RELOAD_ENABLED_SETTING) // 模板为File
                .setScriptParams(templateParams) // 参数, 文件中有参数param_gender， 作为key， 会从template_params中获取对应的value
                .setRequest(new SearchRequest().indices(index).types(type)) // 执行内容，指定index, type
                .get()
                .getResponse();
        SearchHits hits = sr.getHits();
        if (hits != null) {
            for (SearchHit hit : hits) {
                String json = hit.getSourceAsString();
                System.out.println(json);
            }
        }
    }

    /**
     * Search Template: 搜索模板， 模板类型为STORED，存储在集群中的模板
     */
    @Test
    public void searchTemplateAPI2()
    {
//        client.admin().cluster().preparePutStoredScript()
////                .setScriptLang("mustache")
//                .setId("template_test") //模板名字
//                .setSource(new BytesArray(
//                        "{\n" +
//                                "        \"query\" : {\n" +
//                                "            \"match\" : {\n" +
//                                "                \"sex\" : \"{{param_gender}}\"\n" +
//                                "            }\n" +
//                                "        }\n" +
//                                "}")).get();
//        // 定义模板参数
//        Map<String, Object> template_params = new HashMap<>();
//        template_params.put("param_gender", "男");
//
//        SearchResponse sr = new SearchTemplateRequestBuilder(client)
//                .setScript("template_test")
//                .setScriptType(ScriptService.ScriptType.STORED)
//                .setScriptParams(template_params)
//                .setRequest(new SearchRequest())
//                .get()
//                .getResponse();
//        SearchHits hits = sr.getHits();
//        if (hits != null) {
//            for (SearchHit hit : hits) {
//                String json = hit.getSourceAsString();
//                System.out.println(json);
//            }
//        }
    }

    /**
     * Search Template: 搜索模板， 模板类型为INLINE，在线模板
     */
    @Test
    public void searchTemplateAPI3()
    {
        // 定义模板参数
//        Map<String, Object> template_params = new HashMap<>();
//        template_params.put("param_gender", "男");
//        SearchResponse sr = new SearchTemplateRequestBuilder(client)
//                .setScript("{\n" +
//                        "        \"query\" : {\n" +
//                        "            \"match\" : {\n" +
//                        "                \"sex\" : \"{{param_gender}}\"\n" +
//                        "            }\n" +
//                        "        }\n" +
//                        "}")
//                .setScriptType(ScriptService.ScriptType.INLINE)
//                .setScriptParams(template_params)
//                .setRequest(new SearchRequest().indices(index).types(type))
//                .get()
//                .getResponse();
//        SearchHits hits = sr.getHits();
//        if (hits != null) {
//            for (SearchHit hit : hits) {
//                String json = hit.getSourceAsString();
//                System.out.println(json);
//            }
//        }
    }

    /**
     * post Filter
     */
    @Test
    public void postFilterAPI()
    {
        SearchResponse response = client.prepareSearch("index1", "index2").setTypes("type1", "type2")
                .setQuery(QueryBuilders.termQuery("multi", "test"))
                .setPostFilter(QueryBuilders.rangeQuery("age").from(12).to(18))
                .setFrom(0).setSize(60).get();
        SearchHits hits = response.getHits();
        if (hits != null) {
            for (SearchHit hit : hits) {
                String json = hit.getSourceAsString();
                System.out.println(json);
            }
        }
    }
}
