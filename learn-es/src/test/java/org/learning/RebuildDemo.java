package org.learning;

import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.learning.entity.Person;
import org.learning.util.JsonUtil;

/**
 * Created by anderson on 16-12-14.
 */
public class RebuildDemo
        extends BaseTest
{
    /**
     * 重构索引（索引重建 Rebuild）：
     * 场景：
     * 已经建好了索引(index)和type，并且已经插入了很多Document，这时，用户想修改原有的type，比如修改type的mapping，将其中的field改成不索引，或者将field从string改成int
     * 由于mapping建好之后不能修改(只能添加新的field)，所以需要重新所以index，下面是一种解决方案。
     */
    @Test
    public void solutionDemo()
    {
        //1 .创建索引
        IndicesAdminClient indicesAdminClient = client.admin().indices();
        CreateIndexResponse response = indicesAdminClient.prepareCreate("user2")
                .setSettings(Settings.builder()
                        .put("index.number_of_shards", 3)
                        .put("index.number_of_replicas", 1)
                )
                .get();
        System.out.println(response.isAcknowledged());

        //2. 创建索引的别名
        IndicesAliasesResponse response2 = indicesAdminClient.prepareAliases()
                .addAlias("user2", "user_alias")
                .execute()
                .actionGet();
        System.out.println(response2.isAcknowledged());

        //3. 创建mapping
        PutMappingResponse response3 = indicesAdminClient.preparePutMapping("user2")
                .setType(type)
                .setSource("{\n" +
                        "    \"jd_person\": {\n" +
                        "            \"properties\": {\n" +
                        "                \"name\": { \"type\": \"string\", \"index\": \"not_analyzed\"},\n" +
                        "                \"sex\": {\"type\": \"string\", \"index\": \"not_analyzed\"},\n" +
                        "                \"age\": {\"type\": \"integer\"},\n" +
                        "                \"student\": {\"type\": \"boolean\"},\n" +
                        "                \"info\": {\"type\": \"text\", \"index\": \"no\"}\n" +
                        "            }\n" +
                        "     }\n" +
                        "}")
                .get();

        System.out.println(response3.isAcknowledged());
        //4. 插入数据，之后查询的时候可以使用user_alias而不是使用user2
        // 调用DocumentApiDemo下面的bulkAPI

        //--------------------分割线-------------------------------
        // 上面的操作完成了，现在我们向更改上面索引中的mapping，将info field改为analyzed，按照下面的操作，可以不停止原有的服务，前提是我们的索引不是直接暴露给用户的，给用户的都是索引别名

        // 5. 创建一个新的索引
        ActionResponse response4 = indicesAdminClient.prepareCreate("user3")
                .setSettings(Settings.builder()
                        .put("index.number_of_shards", 3)
                        .put("index.number_of_replicas", 1)
                )
                .addMapping("jd_person", "{\n" +
                        "    \"jd_person\": {\n" +
                        "            \"properties\": {\n" +
                        "                \"name\": { \"type\": \"string\", \"index\": \"not_analyzed\"},\n" +
                        "                \"sex\": {\"type\": \"string\", \"index\": \"not_analyzed\"},\n" +
                        "                \"age\": {\"type\": \"integer\"},\n" +
                        "                \"student\": {\"type\": \"boolean\"},\n" +
                        "                \"info\": {\"type\": \"text\", \"index\": \"analyzed\"}\n" +
                        "            }\n" +
                        "     }\n" +
                        "}")
                .get();
        System.out.println(response4.toString());

        // 7. 接下来修改alias别名的指向
        IndicesAliasesResponse aliasesResponse = indicesAdminClient.prepareAliases()
                .addAlias("user3", "user_alias")
                .removeAlias("user2", "user_alias")
                .execute()
                .actionGet();
        System.out.println(aliasesResponse.isAcknowledged());

        // 6.将数据导入到新的索引
        SearchResponse scrollResp = client.prepareSearch("user2")
                .setTypes("jd_person")
                .addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)
                .setScroll(new TimeValue(60000)) // 设置的时间只会对当次请求有影响, 所以每次请求可以单独设置过期时间
                .setQuery(QueryBuilders.matchAllQuery())
                .setSize(100).get(); //max of 100 hits will be returned for each scroll
        //Scroll until no hits are returned

        BulkRequestBuilder bulkRequest = client.prepareBulk();
        do {
            for (SearchHit hit : scrollResp.getHits().getHits()) {
                //Handle the hit...
                String json = hit.getSourceAsString();
                System.out.println("-" + json);

                //批量加入到bulk api
                bulkRequest.add(client.prepareIndex("user3", "jd_person", hit.getId()).setSource(json));
            }
            //批量导入
            BulkResponse bulkResponse = bulkRequest.get();
            if (bulkResponse.hasFailures()) {
                // process failures by iterating through each bulk response item
                System.out.println("index error!!");
            }
            else {
                for (BulkItemResponse item : bulkResponse.getItems()) {
                    DocWriteResponse response5 = item.getResponse();
                    System.out.println(response5.status().name());
                }
            }

            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
        }
        while (scrollResp.getHits().getHits().length != 0); // Zero hits mark the end of the scroll and the while loop.

        // 8.删除老的索引
        DeleteIndexResponse response6 = client.admin().indices().prepareDelete("user2").get();
        System.out.println(response6.isAcknowledged());
    }

    @Test
    public void testDemo2()
    {
        Person person = new Person();
        person.setAge(23);
        person.setInfo("Elasticsearch is a distributed, RESTful search and analytics engine capable of solving a growing number of use cases");
        person.setName("Jeff");
        person.setSex("男");
        person.setStudent(true);
        person.setDateOfBirth("1993-10-01");

        String source = JsonUtil.toJSON(person);
        System.out.println(source);
//        Person p = JsonUtil.deserialize(source, Person.class);
//        System.out.println(p);
        // 不知道id则es会自动生成id
        IndexResponse response = client.prepareIndex("user_alias", type, "12").setSource(source).get();
        // Index name
        String index = response.getIndex();
        // Type name
        String type = response.getType();
        // Document ID (generated or not)
        String id = response.getId();
        // Version (if it's the first time you index this document, you will get: 1)
        long version = response.getVersion();
        // isCreated() is true if the document is a new one, false if it has been updated
        System.out.println("index:" + index + ", type:" + type + ",id:" + id + ",version:" + version);
        // isCreated() is true if the document is a new one, false if it has been updated
        System.out.println(response.getResult().name()); //CREATED
        System.out.println(response.status().name()); //CREATED
        System.out.println(response.status().getStatus()); //201
        System.out.println(response.status().ordinal()); //3
    }
}
