package org.learning;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.junit.Test;

/**
 * Created by anderson on 16-12-8.
 */
public class StructuringAggregationsDemo
        extends BaseTest
{
    /**
     * 待验证
     */
    @Test
    public void structuringAggregationsAPI()
    {
        SearchResponse sr = client.prepareSearch()
//                .setIndices("logstash-2015.05.18")
//                .setTypes("log")
                .addAggregation(
                        AggregationBuilders.terms("by_country").field("geo.dest")
                                .subAggregation(AggregationBuilders.dateHistogram("by_hour")
                                        .field("utc_time")
                                        .dateHistogramInterval(DateHistogramInterval.HOUR)
                                        .subAggregation(AggregationBuilders.avg("avg_response").field("response"))
                                )
                )
                .execute().actionGet();
        SearchHits hits = sr.getHits();
        if (hits != null) {
            for (SearchHit hit : hits) {
                String json = hit.getSourceAsString();
                System.out.println(json);
            }
        }
    }
}
