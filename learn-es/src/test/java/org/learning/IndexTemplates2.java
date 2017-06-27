package org.learning;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateResponse;
import org.elasticsearch.action.bulk.byscroll.BulkByScrollResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.junit.Test;

/**
 * Created by anderson on 17-3-13.
 */
public class IndexTemplates2
        extends BaseTest
{
    /**
     * 在elasticsearch中一个index下面会有很多的type，在一个项目中type的数量未知，所以在建立template的时候需要mapping不指定type（对所有的type有效）
     * 因此需要用到_default_字段
     * 下面的是针对所有开头是xdata-log-的索引的所有type创建对应的mapping
     */
    @Test
    public void createTemplate()
    {
        IndicesAdminClient indicesAdminClient = client.admin().indices();
        PutIndexTemplateResponse response = indicesAdminClient.preparePutTemplate("xdata-log_template")
                .setTemplate("xdata-log-*")
                .setSettings(Settings.builder()
                        .put("index.number_of_shards", 3)
                        .put("index.number_of_replicas", 1))
                .addMapping("_default_", "{\n" +
                        "    \"_default_\": {\n" +
                        "        \"_all\": {\n" +
                        "            \"enabled\": false\n" +
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
                        "            \"context\": {\n" +
                        "                \"type\": \"text\"\n" +
                        "            }\n" +
                        "        }\n" +
                        "    }\n" +
                        "}")
//                .setOrder(0)
//                .setVersion(0)
//                .addAlias(new Alias("xdata-log"))
                .get();
        System.out.println(response.isAcknowledged());
    }

    /**
     * 可以清空某个type下的数据，但是type的元数据仍然存在。
     * 在elasticsearch 2.x和5.x中，type没有办法直接删除，删除type的方法：
     * 1. 删除type所在的index，这样index下的type都会被删除
     * 2. reindex， 重建索引，删除原来的索引
     * <p>
     * 下面的是DeleteByQuery，可以用来清空一个type下的document，但是就算把type下的document全部删除了，type的metadata信息依然存在。
     */
    @Test
    public void testDeleteType()
    {
        BulkByScrollResponse deleteResponse =
                DeleteByQueryAction.INSTANCE.newRequestBuilder(client)
                        .filter(QueryBuilders.rangeQuery("timestamp")
                                .from("2017-03-10 00:00:00.000")
                                .includeLower(true))
                        .source("xdata-log-2017.03.09")
                        .get();

        long deleted = deleteResponse.getDeleted();
        System.out.println(deleted);
    }

    // 异步
    @Test
    public void testDeleteType2()
    {
        DeleteByQueryAction.INSTANCE.newRequestBuilder(client)
                .filter(QueryBuilders.rangeQuery("timestamp")
                        .from("2017-03-10 00:00:00.000")
                        .includeLower(true))
                .source("xdata-log-2017.03.09")
                .execute(new ActionListener<BulkByScrollResponse>()
                {
                    @Override
                    public void onResponse(BulkByScrollResponse bulkByScrollResponse)
                    {
                    }

                    @Override
                    public void onFailure(Exception e)
                    {
                    }

//                    @Override
//                    public void onResponse(BulkIndexByScrollResponse response) {
//                        long deleted = response.getDeleted();
//                    }
//                    @Override
//                    public void onFailure(Exception e) {
//                        // Handle the exception
//                    }
                });
    }
}
