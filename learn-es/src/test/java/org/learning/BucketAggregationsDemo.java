package org.learning;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.children.Children;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.filters.Filters;
import org.elasticsearch.search.aggregations.bucket.filters.FiltersAggregator;
import org.elasticsearch.search.aggregations.bucket.geogrid.GeoHashGrid;
import org.elasticsearch.search.aggregations.bucket.global.Global;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.missing.Missing;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
import org.elasticsearch.search.aggregations.bucket.nested.ReverseNested;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.joda.time.DateTime;
import org.junit.Test;

/**
 * Created by anderson on 16-12-9.
 */
public class BucketAggregationsDemo
        extends BaseTest
{
    /**
     * Global Aggregation
     */
    @Test
    public void globalAggregation()
    {
        AggregationBuilder aggregation = AggregationBuilders
                .global("agg")
                .subAggregation(AggregationBuilders.terms("genders").field("sex"));
        SearchResponse response = client.prepareSearch(index)
                .setTypes(type)
                .addAggregation(aggregation)
                .get();
        Global agg = response.getAggregations().get("agg");
        long count = agg.getDocCount(); // Doc count
        System.out.println("count:" + count); //10 没有sex字段的document也会被统计
        StringTerms stringTerms = agg.getAggregations().get("genders");
        // 将sex字段进行分桶，一共有两个性别：男和女， 分成了两个桶， 没有sex字段的没有分到桶里
        stringTerms.getBuckets().forEach((Terms.Bucket bucket) -> System.out.println(bucket.getKeyAsString())); // 男，女
        System.out.println(stringTerms.getBuckets().size()); // 2
    }

    /**
     * Filter Aggregation
     * 过滤器聚合
     */
    @Test
    public void filterAggregation()
    {
        AggregationBuilder aggregation = AggregationBuilders
                //Create a new Filter aggregation with the given name.
                .filter("agg", QueryBuilders.termQuery("sex", "男"));
        SearchResponse response = client.prepareSearch(index)
                .setTypes(type)
                .addAggregation(aggregation)
                .get();
        Filter agg = response.getAggregations().get("agg");
        long count = agg.getDocCount(); // Doc count
        System.out.println("count:" + count); //6
    }

    /**
     * Filters Aggregation
     * 多个过滤器聚合
     */
    @Test
    public void filtersAggregation()
    {
        AggregationBuilder aggregation = AggregationBuilders
                .filters("agg",
                        new FiltersAggregator.KeyedFilter("men", QueryBuilders.termQuery("sex", "男")),
                        new FiltersAggregator.KeyedFilter("women", QueryBuilders.termQuery("sex", "女")));
        SearchResponse response = client.prepareSearch(index)
                .setTypes(type)
                .addAggregation(aggregation)
                .get();
        Filters agg = response.getAggregations().get("agg");

        // For each entry
        for (Filters.Bucket entry : agg.getBuckets()) {
            String key = entry.getKeyAsString();            // bucket key
            long docCount = entry.getDocCount();            // Doc count
            System.out.println("key：" + key + ",doc_count:" + docCount);
        }
        //结果：
        //key：men,doc_count:6
        //key：women,doc_count:4
    }

    /**
     * Missing Aggregation
     * 缺少某个field的Document做聚合
     */
    @Test
    public void missingAggregation()
    {
        // index：user，里面有两个type：jd_person和my_type jd_person没有group field, my_type还有该field
        AggregationBuilder aggregation = AggregationBuilders.missing("agg").field("group");
        SearchResponse response = client.prepareSearch(index)
                .addAggregation(aggregation)
                .get();
        Missing agg = response.getAggregations().get("agg");
        long count = agg.getDocCount(); // Doc count
        System.out.println("count:" + count);
    }

    /**
     * Nested Aggregation， 需要对nested类型的field进行操作
     * 准备：
     * 添加映射：
     * PUT user
     * {
     * "my_type": {
     * "properties": {
     * "group":{"type":"string", "index":"not_analyzed"},
     * "user": {
     * "type": "nested",
     * "properties":{
     * "first":{"type":"string", "index":"not_analyzed"},
     * "last":{"type":"string", "index":"not_analyzed"},
     * "age":{"type":"integer", "index":"not_analyzed"}
     * }
     * }
     * }
     * }
     * }
     * 添加数据：
     * PUT user/my_type/1
     * {
     * "group" : "fans",
     * "user" : [
     * {
     * "first" : "John",
     * "last" :  "Smith",
     * "age":20
     * },
     * {
     * "first" : "Alice",
     * "last" :  "White",
     * "age":25
     * }
     * ]
     * }
     * <p>
     * PUT user/my_type/2
     * {
     * "group" : "student",
     * "user" : [
     * {
     * "first" : "Jim",
     * "last" :  "Green",
     * "age":30
     * },
     * {
     * "first" : "Bob",
     * "last" :  "Dylan",
     * "age":28
     * }
     * ]
     * }
     * PUT user/my_type/3
     * {
     * "group" : "player",
     * "user" : [
     * {
     * "first" : "Will",
     * "last" :  "Smith",
     * "age":30
     * },
     * {
     * "first" : "Andy",
     * "last" :  "Dylan",
     * "age":28
     * }
     * ]
     * }
     */
    @Test
    public void nestedAggregation()
    {
        AggregationBuilder aggregation = AggregationBuilders
                .nested("agg", "user"); // user field, 类型 nested， nested类型可以看做一种特殊的Object类型，里面的每个指都会被索引
        SearchResponse response = client.prepareSearch(index)
                .setTypes("my_type")
                .addAggregation(aggregation)
                .get();
        Nested agg = response.getAggregations().get("agg");
        long count = agg.getDocCount(); // Doc count
        System.out.println("count:" + count); //结果为：6， 相当于计算数组的长度
    }

    /**
     * Reverse Nested Aggregation
     * 注意field是要能排序的否则报错，不能是Text类型
     * Fielddata is disabled on text fields by default.
     */
    @Test
    public void reverseNestedAggregation()
    {
        AggregationBuilder aggregation =
                AggregationBuilders
                        .nested("agg", "user")
                        .subAggregation(
                                AggregationBuilders
                                        .terms("name").field("user.last")
                                        .subAggregation(
                                                AggregationBuilders
                                                        .reverseNested("reseller_to_product")
                                        )
                        );
        SearchResponse response = client.prepareSearch(index)
                .setTypes("my_type")
                .addAggregation(aggregation)
                .get();
        Nested agg = response.getAggregations().get("agg");
        Terms name = agg.getAggregations().get("name");
        for (Terms.Bucket bucket : name.getBuckets()) {
            ReverseNested resellerToProduct = bucket.getAggregations().get("reseller_to_product");
            System.out.println(bucket.getKey());
            long count = resellerToProduct.getDocCount(); // Doc count
            System.out.println("count:" + count);
        }
        //输出结果先按照分桶后的总量排序然后按照字段名排序，所以field必须可以进行比较：
        //Dylan
        //count:2
        //Smith
        //count:2
        //Green
        //count:1
        //White
        //count:1
    }

    /**
     * Children Aggregation
     * 数据准备，来自官方：
     * PUT child_example
     * {
     * "mappings": {
     * "answer" : {
     * "_parent" : {
     * "type" : "question"
     * }
     * }
     * }
     * }
     * <p>
     * PUT child_example/question/1
     * {
     * "body": "<p>I have Windows 2003 server and i bought a new Windows 2008 server...",
     * "title": "Whats the best way to file transfer my site from server to a newer one?",
     * "tags": [
     * "windows-server-2003",
     * "windows-server-2008",
     * "file-transfer"
     * ]
     * }
     * <p>
     * PUT child_example/answer/1?parent=1&refresh
     * {
     * "owner": {
     * "location": "Norfolk, United Kingdom",
     * "display_name": "Sam",
     * "id": 48
     * },
     * "body": "<p>Unfortunately you're pretty much limited to FTP...",
     * "creation_date": "2009-05-04T13:45:37.030"
     * }
     * PUT child_example/answer/2?parent=1&refresh
     * {
     * "owner": {
     * "location": "Norfolk, United Kingdom",
     * "display_name": "Troll",
     * "id": 49
     * },
     * "body": "<p>Use Linux...",
     * "creation_date": "2009-05-05T13:45:37.030"
     * }
     */
    @Test
    public void childrenAggregation()
    {
        AggregationBuilder aggregation =
                AggregationBuilders
                        .children("agg", "answer");
        SearchResponse response = client.prepareSearch("child_example")
                .addAggregation(aggregation)
                .get();
        Children agg = response.getAggregations().get("agg");
        long count = agg.getDocCount(); // Doc count
        System.out.println("count:" + count); //2
    }

    /**
     * Terms Aggregation
     * <p>
     * GET /user/_search
     * {
     * "aggs": {
     * "genders": {
     * "terms": {
     * "field": "sex",
     * "size": 3
     * }
     * }
     * }
     * }
     */
    @Test
    public void termsAggregation()
    {
        AggregationBuilder aggregation = AggregationBuilders
                .terms("genders")
                .field("sex")
                .size(2); //Sets the size - indicating how many term buckets should be returned 默认为10，即取前几个分桶返回
        SearchResponse response = client.prepareSearch(index)
                .addAggregation(aggregation)
                .get();
        Terms genders = response.getAggregations().get("genders");

        // For each entry
        for (Terms.Bucket entry : genders.getBuckets()) {
            System.out.println(entry.getKey());      // Term
            System.out.println(entry.getDocCount()); // Doc count
        }
        //输出：
        //男
        //6
        //女
        //4
    }

    /**
     * Order
     * 排序结果可能是不准确的
     */
    @Test
    public void order()
    {
        // 根据聚合后的bucket的docCount从小到大排序，sex聚合后两个bucket 女：4 男：6 所以女bucket在前男bucket在后
        AggregationBuilder aggregation = AggregationBuilders
                .terms("genders")
                .field("sex")
                .order(Terms.Order.count(true));
        // 输出：
        //Key:女, DocCount:4
        //Key:男, DocCount:6

        // 据聚合后的bucket的key的字母顺序排序
        AggregationBuilder aggregation2 = AggregationBuilders
                .terms("genders")
                .field("name")
                .order(Terms.Order.term(true));
        //输出：
        //Key:Andy, DocCount:1
        //Key:Anna, DocCount:1
        //Key:Buick, DocCount:1
        //Key:David, DocCount:1
        //Key:Frank, DocCount:1
        //Key:Henry, DocCount:1
        //Key:James, DocCount:1
        //Key:Lily, DocCount:1
        //Key:Linda, DocCount:1
        //Key:Mary, DocCount:1

        //Ordering the buckets by single value metrics sub-aggregation (identified by the aggregation name)
        // 通过对单个field的统计来对bucket进行排序
        AggregationBuilder aggregation3 = AggregationBuilders
                .terms("genders")
                .field("sex")
                //true:asc 从小到大  false：desc 从大到小
                .order(Terms.Order.aggregation("avg_height", true))
                //对bucket中的age求平均值进行排序
                .subAggregation(
                        AggregationBuilders.avg("avg_height").field("age")
                );
        //女的年龄平均值小于男的 输出：
        //Key:女, DocCount:4
        //Key:男, DocCount:6
        SearchResponse response = client.prepareSearch(index)
                .setTypes(type)
                .addAggregation(aggregation3)
                .get();
        Terms genders = response.getAggregations().get("genders");
        // For each entry
        for (Terms.Bucket entry : genders.getBuckets()) {
            System.out.println("Key:" + entry.getKey() + ", DocCount:" + entry.getDocCount());      // Term
        }
    }

    /**
     * Significant Terms Aggregation
     */
    @Test
    public void significantTermsAggregation()
    {
        AggregationBuilder aggregation =
                AggregationBuilders
                        .significantTerms("significant_os")
                        .field("age"); // Fielddata

        // Let say you search for men only
        SearchResponse sr = client.prepareSearch("bank")
                .setTypes("account")
                .setQuery(QueryBuilders.matchQuery("gender", "M")) //先对response为404的进行过滤，然后对machine.ram进行分桶
                .addAggregation(aggregation)
                .get();
        SignificantTerms agg = sr.getAggregations().get("significant_os");
        System.out.println(agg.getBuckets().size());
        // For each entry

        for (SignificantTerms.Bucket entry : agg.getBuckets()) {
            System.out.println(entry.getKey());     // Term
            System.out.println("---" + entry.getDocCount()); // Doc count
        }
    }

    /**
     * Range Aggregation
     */
    @Test
    public void angeAggregation()
    {
        AggregationBuilder aggregation =
                AggregationBuilders
                        .range("agg")
                        .field("age")
                        .addUnboundedTo(25)               // from -infinity to 25.0 (excluded) 小于25
                        .addRange(25, 50)               // from 25.0 to 50.0 (excluded) 大于等于25，小于50
                        .addUnboundedFrom(50);            // from 50.0 to +infinity 大于等于50

        SearchResponse response = client.prepareSearch(index)
                .setTypes(type)
                .addAggregation(aggregation)
                .get();

        Range agg = response.getAggregations().get("agg");

        // For each entry
        for (Range.Bucket entry : agg.getBuckets()) {
            String key = entry.getKeyAsString();             // Range as key
            Number from = (Number) entry.getFrom();          // Bucket from
            Number to = (Number) entry.getTo();              // Bucket to
            long docCount = entry.getDocCount();    // Doc count
            System.out.println("key:" + key + ", from:" + from + ", to:" + to + ", docCount:" + docCount);
        }
    }

    /**
     * Date Range Aggregation
     */
    @Test
    public void dateRangeAggregation()
    {
        AggregationBuilder aggregation =
                AggregationBuilders
                        .dateRange("agg")
                        .field("dateOfBirth")
                        .format("yyyy")
                        .addUnboundedTo("1950")    // from -infinity to 1950 (excluded)
                        .addRange("1950", "1990")  // from 1950 to 1960 (excluded)
                        .addUnboundedFrom("1990"); // from 1960 to +infinity

        SearchResponse response = client.prepareSearch(index)
                .setTypes(type)
                .addAggregation(aggregation)
                .get();
        Range agg = response.getAggregations().get("agg");

        // For each entry
        for (Range.Bucket entry : agg.getBuckets()) {
            String key = entry.getKeyAsString();                // Date range as key
            DateTime fromAsDate = (DateTime) entry.getFrom();   // Date bucket from as a Date
            DateTime toAsDate = (DateTime) entry.getTo();       // Date bucket to as a Date
            long docCount = entry.getDocCount();                // Doc count               // Doc count

            System.out.println("key:" + key + ", from:" + fromAsDate + ", to:" + toAsDate + ", docCount:" + docCount);
        }
        //输出：
        //key:*-1950, from:null, to:1950-01-01T00:00:00.000Z, docCount:2
        //key:1950-1990, from:1950-01-01T00:00:00.000Z, to:1990-01-01T00:00:00.000Z, docCount:4
        //key:1990-*, from:1990-01-01T00:00:00.000Z, to:null, docCount:4
    }

    /**
     * IP Range Aggregation
     */
    @Test
    public void ipRangeAggregation()
    {
        AggregationBuilder aggregation =
                AggregationBuilders
                        .ipRange("agg")
                        .field("ip.keyword")
//                        .addUnboundedTo("100.0.0.0")             // from -infinity to 192.168.1.0 (excluded)
                        .addRange("100.168.1.0", "200.168.1.0");    // from 192.168.1.0 to 192.168.2.0 (excluded)
//                        .addUnboundedFrom("200.0.0.0");          // from 192.168.2.0 to +infinity

        SearchResponse response = client.prepareSearch("logstash-2015.05.18")
                .setTypes("log")
                .addAggregation(aggregation)
                .get();
        Range agg = response.getAggregations().get("agg");

        // For each entry
        for (Range.Bucket entry : agg.getBuckets()) {
//            InternalBinaryRange.Bucket bucket = (InternalBinaryRange.Bucket)entry;
//            System.out.println(bucket.getFrom());
//            System.out.println(entry.getClass().getName());
//            System.out.println(entry.getKey().getClass().getName());            // Ip range as key
//            String fromAsString = entry.getFromAsString();  // Ip bucket from as a String
//            String toAsString = entry.getToAsString();      // Ip bucket to as a String
            long docCount = entry.getDocCount();            // Doc count
//            System.out.println("key:" + key + ", from:" + fromAsString + ", to:" + toAsString + ", docCount:" + docCount);
            System.out.println(", docCount:" + docCount);
        }
    }

    /**
     * Histogram Aggregation
     * 直方图
     */
    @Test
    public void histogramAggregation()
    {
        AggregationBuilder aggregation =
                AggregationBuilders
                        .histogram("agg")
                        .field("balance")
                        .interval(5000);

        SearchResponse response = client.prepareSearch("bank")
                .setTypes("account")
                .addAggregation(aggregation)
                .get();

        Histogram agg = response.getAggregations().get("agg");

        // For each entry
        for (Histogram.Bucket entry : agg.getBuckets()) {
            Number key = (Number) entry.getKey();   // Key
            long docCount = entry.getDocCount();    // Doc count
            System.out.println("key:" + key.intValue() + ", docCount:" + docCount);
        }
        //结果：
        //key:0, docCount:79 //大于0小于5000的数量
        //key:5000, docCount:89
        //key:10000, docCount:111
        //key:15000, docCount:102
        //key:20000, docCount:104
        //key:25000, docCount:113
        //key:30000, docCount:92
        //key:35000, docCount:95
        //key:40000, docCount:121
        //key:45000, docCount:94
    }

    /**
     * Date Histogram Aggregation
     */
    @Test
    public void dateHistogramAggregation()
    {
        AggregationBuilder aggregation =
                AggregationBuilders
                        .dateHistogram("agg")
                        .field("dateOfBirth")
                        .dateHistogramInterval(DateHistogramInterval.YEAR);
        // set an interval of 10 days
//        AggregationBuilder aggregation =
//                AggregationBuilders
//                        .dateHistogram("agg")
//                        .field("dateOfBirth")
//                        .dateHistogramInterval(DateHistogramInterval.days(10));

        SearchResponse response = client.prepareSearch(index)
                .setTypes(type)
                .addAggregation(aggregation)
                .get();

        Histogram agg = response.getAggregations().get("agg");

        // For each entry
        for (Histogram.Bucket entry : agg.getBuckets()) {
            DateTime key = (DateTime) entry.getKey();    // Key
            String keyAsString = entry.getKeyAsString(); // Key as String
            long docCount = entry.getDocCount();         // Doc count
            System.out.println("key:" + key + ", keyAsString" + keyAsString + ", docCount:" + docCount);
        }
    }

    /**
     * Geo Distance Aggregation
     */
    @Test
    public void geoDistanceAggregation()
    {
        AggregationBuilder aggregation =
                AggregationBuilders
                        .geoDistance("agg", new GeoPoint(48.84237171118314, 2.33320027692004))
                        .field("locationpoint")
                        .unit(DistanceUnit.KILOMETERS)
                        .addUnboundedTo(1000.0)
                        .addRange(1000.0, 5000.0)
                        .addRange(5000.0, 50000.0);

        SearchResponse response = client.prepareSearch("geo")
                .setTypes("poi")
                .addAggregation(aggregation)
                .get();

        Range agg = response.getAggregations().get("agg");

        // For each entry
        for (Range.Bucket entry : agg.getBuckets()) {
            String key = entry.getKeyAsString();    // key as String
            Number from = (Number) entry.getFrom(); // bucket from value
            Number to = (Number) entry.getTo();     // bucket to value
            long docCount = entry.getDocCount();    // Doc count
            System.out.println("key:" + key + ", from:" + from + ",to:" + to + ", docCount:" + docCount);
//            logger.info("key [{}], from [{}], to [{}], doc_count [{}]", key, from, to, docCount);
        }
        //输出：
        //key:*-1000.0, from:0.0,to:1000.0, docCount:0
        //key:1000.0-5000.0, from:1000.0,to:5000.0, docCount:1
        //key:5000.0-50000.0, from:5000.0,to:50000.0, docCount:3
    }

    /**
     * Geo Hash Grid Aggregation
     */
    @Test
    public void geoHashGridAggregation()
    {
        AggregationBuilder aggregation =
                AggregationBuilders
                        .geohashGrid("agg")
                        .field("locationpoint")
                        .precision(4);

        SearchResponse response = client.prepareSearch("geo")
                .setTypes("poi")
                .addAggregation(aggregation)
                .get();

        GeoHashGrid agg = response.getAggregations().get("agg");

        // For each entry
        for (GeoHashGrid.Bucket entry : agg.getBuckets()) {
            String keyAsString = entry.getKeyAsString(); // key as String
            GeoPoint key = (GeoPoint) entry.getKey();    // key as geo point
            long docCount = entry.getDocCount();         // Doc count
            System.out.println("keyAsString:" + keyAsString + ", key:" + key + ",docCount:" + docCount);
        }
        //输出：
        //keyAsString:ucft, key:55.72265625, 37.265625,docCount:1
        //keyAsString:r3gx, key:-33.92578125, 151.171875,docCount:1
        //keyAsString:mpux, key:-0.17578125, 51.328125,docCount:1
        //keyAsString:dr5r, key:40.60546875, -74.1796875,docCount:1
    }
}
