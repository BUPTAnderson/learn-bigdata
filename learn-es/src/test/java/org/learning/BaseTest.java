/**
 * @author Geloin
 */
package org.learning;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.learning.util.ElasticSearchUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by anderson on 16-10-29.
 * 创建的是Transport Client
 * 实际client的类是org.elasticsearch.transport.client.PreBuiltTransportClient，该类是org.elasticsearch.transport.client.TransportClient的子类
 */
public class BaseTest
{
    protected static Logger log = LoggerFactory.getLogger(BaseTest.class);
    protected static String index = "user";
    protected static String type = "jd_person";
    protected static Client client = ElasticSearchUtil.createClient();
    protected static ObjectMapper mapper = new ObjectMapper();

    public void print(SearchHits hits)
    {
        if (null != hits) {
            System.out.println("--------------- " + hits.getTotalHits());
            for (SearchHit hit : hits) {
                String json = hit.getSourceAsString();
                System.out.println(json);
            }
        }
        else {
            System.out.println("没有查询到任何结果！");
        }
    }

    public void printWithScore(SearchHits hits)
    {
        if (null != hits) {
            System.out.println("-----  totalHist: " + hits.getTotalHits() + ", maxScore:" + hits.getMaxScore());
            for (SearchHit hit : hits) {
                System.out.println("id:" + hit.getId() + ", score:" + hit.getScore());
                String json = hit.getSourceAsString();
                System.out.println(json);
            }
        }
        else {
            System.out.println("没有查询到任何结果！");
        }
    }
}
