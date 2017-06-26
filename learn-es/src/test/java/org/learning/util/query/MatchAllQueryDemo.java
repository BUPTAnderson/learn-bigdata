package org.learning.util.query;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.junit.Test;
import org.learning.BaseTest;

/**
 * Created by anderson on 16-12-12.
 */
public class MatchAllQueryDemo extends BaseTest
{
    /**
     * Match All Query
     * 使用match_all 可以查询到所有文档，是没有查询条件下的默认语句。
     * GET /_search
     *  {
     *      "match_all": {}
     *  }
     *此查询常用于合并过滤条件。 比如说你需要检索所有的邮箱,所有的文档相关性都是相同的，所以得到的_score为1
     *
     */
    @Test
    public void matchAllQuery() {
        QueryBuilder qb = QueryBuilders.matchAllQuery();
        SearchResponse sr = client.prepareSearch("bank").setTypes("account")
                .setQuery(qb)
                .setFrom(0).setSize(12).execute().actionGet();

        SearchHits hits = sr.getHits();
        print(hits);
    }
}
