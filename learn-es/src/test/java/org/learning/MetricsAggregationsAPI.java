package org.learning;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.avg.Avg;
import org.elasticsearch.search.aggregations.metrics.avg.AvgAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.aggregations.metrics.cardinality.CardinalityAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.geobounds.GeoBounds;
import org.elasticsearch.search.aggregations.metrics.geobounds.GeoBoundsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.max.Max;
import org.elasticsearch.search.aggregations.metrics.max.MaxAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.min.Min;
import org.elasticsearch.search.aggregations.metrics.min.MinAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.percentiles.Percentile;
import org.elasticsearch.search.aggregations.metrics.percentiles.PercentileRanks;
import org.elasticsearch.search.aggregations.metrics.percentiles.PercentileRanksAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.percentiles.Percentiles;
import org.elasticsearch.search.aggregations.metrics.percentiles.PercentilesAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.stats.Stats;
import org.elasticsearch.search.aggregations.metrics.stats.StatsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.stats.extended.ExtendedStats;
import org.elasticsearch.search.aggregations.metrics.stats.extended.ExtendedStatsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.elasticsearch.search.aggregations.metrics.sum.SumAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHits;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCount;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCountAggregationBuilder;
import org.junit.Test;

import static org.elasticsearch.search.aggregations.AggregationBuilders.geoBounds;

/**
 * Metrics aggregations
 * Created by anderson on 16-12-9.
 */
public class MetricsAggregationsAPI
        extends BaseTest
{
    /**
     * - Min Aggregation
     */
    @Test
    public void minAggregationsAPI()
    {
        MinAggregationBuilder aggregation =
                AggregationBuilders
                        .min("agg") //Create a new Min aggregation with the given name.
                        .field("age"); // Sets the field to use for this aggregation.
        SearchResponse response = client.prepareSearch(index)
                .setTypes(type)
//                .setQuery() //可以同时设置查询，对查询的结果进行聚合操作, 没有的话是对整个type进行聚合操作
                .addAggregation(aggregation)
                .get();
        Min agg = response.getAggregations().get("agg");
        int minAge = (int) agg.getValue();
        System.out.println(minAge);
    }

    /**
     * - Max Aggregation
     */
    @Test
    public void maxAggregationsAPI()
    {
        MaxAggregationBuilder aggregation =
                AggregationBuilders
                        .max("agg")
                        .field("age");
        SearchResponse response = client.prepareSearch(index)
                .setTypes(type)
                .addAggregation(aggregation)
                .get();
        Max agg = response.getAggregations().get("agg");
        int maxAge = (int) agg.getValue();
        System.out.println(maxAge);
    }

    /**
     * - Sum Aggregation
     */
    @Test
    public void sumAggregationsAPI()
    {
        SumAggregationBuilder aggregation =
                AggregationBuilders
                        .sum("agg")
                        .field("age");
        SearchResponse response = client.prepareSearch(index)
                .setTypes(type)
                .addAggregation(aggregation)
                .get();
        Sum agg = response.getAggregations().get("agg");
        int sumAge = (int) agg.getValue();
        System.out.println(sumAge);
    }

    /**
     * - Avg Aggregation
     */
    @Test
    public void avgAggregationsAPI()
    {
        AvgAggregationBuilder aggregation =
                AggregationBuilders
                        .avg("agg")
                        .field("age");
        SearchResponse response = client.prepareSearch(index)
                .setTypes(type)
                .addAggregation(aggregation)
                .get();
        Avg agg = response.getAggregations().get("agg");
        int avgAge = (int) agg.getValue();
        System.out.println(avgAge);
    }

    /**
     * - Stats Aggregation
     */
    @Test
    public void statsAggregationsAPI()
    {
        StatsAggregationBuilder aggregation =
                AggregationBuilders
                        .stats("agg")
                        .field("balance");
        SearchResponse response = client.prepareSearch("bank")
                .setTypes("account")
                .addAggregation(aggregation)
                .get();
        Stats agg = response.getAggregations().get("agg");
        int min = (int) agg.getMin();
        int max = (int) agg.getMax();
        int avg = (int) agg.getAvg();
        int sum = (int) agg.getSum();
        long count = agg.getCount();
        System.out.println("min:" + min + ",max:" + max + ",avg:" + avg + ",sum:" + sum + ",count:" + count);
    }

    /**
     * - Extended Stats Aggregation
     */
    @Test
    public void extendedStatsAggregationAPI()
    {
        ExtendedStatsAggregationBuilder aggregation =
                AggregationBuilders
                        .extendedStats("agg")
                        .field("age");
        SearchResponse response = client.prepareSearch("bank")
                .setTypes("account")
                .addAggregation(aggregation)
                .get();
        ExtendedStats agg = response.getAggregations().get("agg");
        double min = agg.getMin();
        double max = agg.getMax();
        double avg = agg.getAvg();
        double sum = agg.getSum();
        long count = agg.getCount();
        double stdDeviation = agg.getStdDeviation(); //标准差, 方差的算数平方根
        double sumOfSquares = agg.getSumOfSquares(); //平方和
        double variance = agg.getVariance(); //方差 {(x_1 - m)^2 +  (x_n - m)^2}/n
        System.out.println("min:" + min + ",max:" + max + ",avg:" + avg + ",sum:" + sum + ",count:" + count
                + ",stdDeviation:" + stdDeviation + ",sumOfSquares:" + sumOfSquares + ",variance:" + variance);
    }

    /**
     * - Value Count Aggregation
     * 对过滤的字段求和，相当去 count(*)
     */
    @Test
    public void valueCountAggregationsAPI()
    {
        ValueCountAggregationBuilder aggregation =
                AggregationBuilders
                        .count("agg")
                        .field("age");
        SearchResponse response = client.prepareSearch(index)
                .setTypes(type)
                .addAggregation(aggregation)
                .get();
        ValueCount agg = response.getAggregations().get("agg");
        long value = agg.getValue();
        System.out.println("value:" + value);
    }

    /**
     * - Percentile Aggregation
     * 基于百分比统计
     */
    @Test
    public void percentileAggregationsAPI()
    {
        PercentilesAggregationBuilder aggregation =
                AggregationBuilders
                        .percentiles("agg")
                        .field("age") //针对age字段进行统计
                        // You can provide your own percentiles instead of using defaults
                        .percentiles(1.0, 5.0, 10.0, 20.0, 30.0, 75.0, 95.0, 99.0);

        SearchResponse response = client.prepareSearch("bank")
                .setTypes("account")
                .addAggregation(aggregation)
                .get();
        Percentiles agg = response.getAggregations().get("agg");
        // For each entry
        for (Percentile entry : agg) {
            double percent = entry.getPercent();    // Percent
            double value = entry.getValue();        // Value
            System.out.println("percent:" + percent + "value:" + value);
        }
        // 输出结果如下：
        //percent:1.0value:20.0
        //percent:5.0value:21.0
        //percent:10.0value:22.0
        //percent:20.0value:24.0
        //percent:30.0value:26.0
        //percent:75.0value:35.0
        //percent:95.0value:39.0
        //percent:99.0value:40.0 // 99%的账户年龄在40岁以内, 这个40.0不一定是所有document中的一个年龄值，是系统计算出来的一个值
    }

    /**
     * - Percentile Ranks Aggregation
     * 指定一个范围，有多少数据落在这里
     */
    @Test
    public void percentileRanksAggregationsAPI()
    {
        PercentileRanksAggregationBuilder aggregation =
                AggregationBuilders
                        .percentileRanks("agg")
                        .field("age")
                        // You can provide your own percentiles instead of using defaults
                        .values(2.0, 6.0, 41.0);

        SearchResponse response = client.prepareSearch(index)
                .setTypes(type)
                .addAggregation(aggregation)
                .get();
        PercentileRanks agg = response.getAggregations().get("agg");
        // For each entry
        for (Percentile entry : agg) {
            double percent = entry.getPercent();    // Percent
            double value = entry.getValue();        // Value
            System.out.println("percent:" + percent + "value:" + value);
        }
        // 输出结果如下：
        //percent:7.5value:2.0
        //percent:13.157894736842108value:6.0
        //percent:64.66666666666667value:41.0 // %64.66的数据落在了41以内，41是我指定的值，系统计算出64.66
    }

    /**
     * - Cardinality Aggregation
     * 获取某个字段上的出现的不同值的个数，这种操作类似于使用SQL的select count( distinct(*) ) from 语句
     */
    @Test
    public void cardinalityAggregationAPI()
    {
        CardinalityAggregationBuilder aggregation =
                AggregationBuilders
                        .cardinality("agg")
                        .field("age"); // 获取不同年龄值的个数
        SearchResponse response = client.prepareSearch(index)
                .setTypes(type)
                .addAggregation(aggregation)
                .get();
        Cardinality agg = response.getAggregations().get("agg");
        long value = agg.getValue();
        System.out.println(value);
    }

    /**
     * - Geo Bounds Aggregation
     * 为空间搜索准备映射:
     * PUT geo
     * {
     * "mappings": {
     * "poi": {
     * "properties": {
     * "name": {
     * "type": "keyword"
     * },
     * "locationpoint": {
     * "type":"geo_point"  //任意的地理坐标
     * }
     * }
     * }
     * }
     * }
     * <p>
     * 批量添加数据：
     * POST geo/poi/_bulk
     * {"index":{"_id":1}}
     * {"name":"New York","locationpoint":"40.664167, -73.938611"}
     * {"index":{"_id":2}}
     * {"name":"London","locationpoint":"-0.1275, 51.5072222"}
     * {"index":{"_id":3}}
     * {"name":"Moscow","locationpoint":"55.75,37.616667"}
     * {"index":{"_id":4}}
     * {"name":"Sydney","locationpoint":"-33.859972, 151.211111"}
     * {"index":{"_id":5}}
     * {"name":"Sydney","locationpoint":"151.206222, -33.893411"}
     */
    @Test
    public void geoBoundsAggregationAPI()
    {
        GeoBoundsAggregationBuilder aggregation =
//                GeoBoundsAggregationBuilder
                geoBounds("agg")
                        .field("locationpoint")
                        .wrapLongitude(true);
        SearchResponse response = client.prepareSearch("geo")
                .setTypes("poi")
                .addAggregation(aggregation)
                .get();
        GeoBounds agg = response.getAggregations().get("agg");
        GeoPoint bottomRight = agg.bottomRight();
        GeoPoint topLeft = agg.topLeft();
        System.out.println("bottomRight:" + bottomRight + ", topLeft:" + topLeft);
    }

    /**
     * Top Hits Aggregation
     * 聚合并返回几条数据, field不能是text类型
     * Fielddata is disabled on text fields by default.
     */
    @Test
    public void topHitsAggregationAPI()
    {
        AggregationBuilder aggregation =
                AggregationBuilders
                        .terms("agg").field("sex")
                        .subAggregation(
                                AggregationBuilders.topHits("top")
                                        .explain(true)
                                        .size(1)
                                        .from(0)
                        );
        SearchResponse response = client.prepareSearch(index)
                .setTypes(type)
                .addAggregation(aggregation)
                .get();
        Terms agg = response.getAggregations().get("agg");
        // For each entry
        for (Terms.Bucket entry : agg.getBuckets()) {
            String key = (String) entry.getKey();                    // bucket key
            long docCount = entry.getDocCount();            // Doc count
            System.out.println("key:" + key + ", doc_count:" + docCount);

            // We ask for top_hits for each bucket
            TopHits topHits = entry.getAggregations().get("top");
            for (SearchHit hit : topHits.getHits().getHits()) {
                System.out.println(" -> id: " + hit.getId() + ", _source:" + hit.getSourceAsString());
            }
        }
        //输出：
        /**
         key:男, doc_count:6
         -> id: 4, _source:{
         "name" : "David",
         "sex" : "男",
         "age" : 20,
         "student" : false,
         "info" : "David is my good boy"
         }
         key:女, doc_count:4
         -> id: 2, _source:{
         "name" : "Anna",
         "sex" : "女",
         "age" : 5,
         "student" : true,
         "info" : "Anna has many book"
         }
         */
    }
}
