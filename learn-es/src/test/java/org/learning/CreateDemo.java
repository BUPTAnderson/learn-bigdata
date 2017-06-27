package org.learning;

import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.state.ClusterStateRequest;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesResponse;
import org.elasticsearch.action.admin.indices.alias.exists.AliasesExistResponse;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesResponse;
import org.elasticsearch.action.admin.indices.close.CloseIndexResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.admin.indices.flush.FlushResponse;
import org.elasticsearch.action.admin.indices.forcemerge.ForceMergeResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.admin.indices.open.OpenIndexResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.ClusterAdminClient;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.cluster.health.ClusterIndexHealth;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.common.settings.Settings;
import org.junit.Test;
import org.learning.entity.Person;
import org.learning.util.JsonUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by anderson on 16-10-25.
 */
public class CreateDemo
        extends BaseTest
{
    // 创建索引
    // 使用默认的设置：index.number_of_shards:5，index.number_of_replicas：1
    @Test
    public void createIndex()
    {
        IndicesAdminClient indicesAdminClient = client.admin().indices();
//        CreateIndexResponse response = indicesAdminClient.create(new CreateIndexRequest(index))
//                .actionGet();
//        System.out.println(response);
        CreateIndexResponse response = indicesAdminClient.prepareCreate(index).get();
        System.out.println(response.getClass().getName());
        System.out.println(response.isAcknowledged());
    }

    // 创建索引
    // 指定分片个数及副本数
    // 创建索引的的时候可以同时给索引添加alias
    // 在es中索引名和alis都必须是唯，一的，两者不能相同，我们可以这样认为，alias也是索引，只不过是指向一个已经存在的索引， 所以
    // 如果索引名与已经存在索引或alias相同时会报错
    // 如果alias与已经存在的索引或alias相同时也会报错
    @Test
    public void createIndex2()
    {
        IndicesAdminClient indicesAdminClient = client.admin().indices();
        CreateIndexResponse response = indicesAdminClient.prepareCreate("user2")
                .setSettings(Settings.builder()
                        .put("index.number_of_shards", 3)
                        .put("index.number_of_replicas", 1)
                )
//                .addAlias(new Alias("user2"))
                .get();
        System.out.println(response.isAcknowledged());
        System.out.println(response.isShardsAcked());
    }

    // 索引或别名alias是否存在
    // 如果存在 名字为"datamanage"的索引或别名，则返回ture, prepareExists()是一个可变参数的方法，可以传入多个参数
    @Test
    public void exist()
    {
//        System.out.println(ElasticSearchUtil.indexExist("es"));
        IndicesExistsResponse response = client.admin().indices().prepareExists("datamanage").execute().actionGet();
        System.out.println(response.isExists());
        client.close();
    }

    // 创建索引的同时，创建type的映射
    @Test
    public void createIndexWithMapping()
    {
        IndicesAdminClient indicesAdminClient = client.admin().indices();
        ActionResponse response = indicesAdminClient.prepareCreate(index)
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
                        "                \"info\": {\"type\": \"string\"}\n" +
                        "            }\n" +
                        "     }\n" +
                        "}")
                .addAlias(new Alias(""))
                .get();
        System.out.println(response.getClass().getName());
        System.out.println(response);
    }

    // 所有的type都按照给定的_defalut_ 配置的mapping关系
    @Test
    public void createIndexWithMapping3()
    {
        IndicesAdminClient indicesAdminClient = client.admin().indices();
        ActionResponse response = indicesAdminClient.prepareCreate("xdata-log-2017.03.09")
                .setSettings(Settings.builder()
                        .put("index.number_of_shards", 3)
                        .put("index.number_of_replicas", 1)
                )
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
//                .addAlias(new Alias("test"))
                .get();
        System.out.println(response.getClass().getName());
        System.out.println(response);
    }

    // 向已经存在的index中添加type的映射
    // source中可以带type(jd_person),也可以省略调，直接为properties对应的json对象也可以.
    // 同一个index下两个type的mapping可以一样。
    @Test
    public void createIndexWithMapping2()
    {
        IndicesAdminClient indicesAdminClient = client.admin().indices();
        PutMappingResponse response = indicesAdminClient.preparePutMapping("user2")
                .setType("jd_person_2")
                .setSource("{\n" +
                        "    \"jd_person_2\": {\n" +
                        "            \"properties\": {\n" +
                        "                \"name\": { \"type\": \"string\", \"index\": \"not_analyzed\"},\n" +
                        "                \"sex\": {\"type\": \"string\", \"index\": \"not_analyzed\"},\n" +
                        "                \"age\": {\"type\": \"integer\"},\n" +
                        "                \"student\": {\"type\": \"boolean\"},\n" +
                        "                \"info\": {\"type\": \"string\", \"analyzer\": \"simple\"}\n" +
                        "            }\n" +
                        "     }\n" +
                        "}")
                .get();
//        PutMappingResponse response = indicesAdminClient.preparePutMapping(index)
//                .setType("my_type")
//                .setSource("{\n" +
//                        "    \"my_type\": {\n" +
//                        "      \"properties\": {\n" +
//                        "        \"group\":{\"type\":\"string\", \"index\":\"not_analyzed\"},\n" +
//                        "        \"user\": {\n" +
//                        "          \"type\": \"nested\",\n" +
//                        "          \"properties\":{\n" +
//                        "            \"first\":{\"type\":\"string\", \"index\":\"not_analyzed\"},\n" +
//                        "            \"last\":{\"type\":\"string\", \"index\":\"not_analyzed\"},\n" +
//                        "            \"age\":{\"type\":\"integer\", \"index\":\"not_analyzed\"}\n" +
//                        "          }\n" +
//                        "        }\n" +
//                        "      }\n" +
//                        "    }\n" +
//                        "}")
//                .get();
        System.out.println(response.getClass().getName());
        System.out.println(response.isAcknowledged());
    }

    //更新mapping
    //更新mapping，只能向已经存在的type中添加新的field,已经存在的type的field字段不能更改名字(更改名字实际会被认为是添加新的字段)，不能更改类型
    // 相同index下的不同type之间field的名字可以相同，但是field必须是相同类型，相同的analyzer，否则后创建的type会报错。
    @Test
    public void updateMapping()
    {
        IndicesAdminClient indicesAdminClient = client.admin().indices();
        PutMappingResponse response = indicesAdminClient.preparePutMapping(index)
                .setType("jd_person")
                .setSource("{\n" +
                        "    \"jd_person\": {\n" +
                        "            \"properties\": {\n" +
                        "                \"name\": { \"type\": \"string\", \"index\": \"not_analyzed\"},\n" +
                        "                \"sex\": {\"type\": \"string\", \"index\": \"not_analyzed\"},\n" +
                        "                \"age\": {\"type\": \"integer\"},\n" +
                        "                \"student\": {\"type\": \"boolean\"},\n" +
                        "                \"info\": {\"type\": \"string\"},\n" +
                        "                \"dateOfBirth\": {\"type\": \"date\"}\n" +
                        "            }\n" +
                        "     }\n" +
                        "}")
                .get();
        System.out.println(response.isAcknowledged());
    }

    //获取Mapping
    @Test
    public void getMapping()
            throws IOException
    {
        GetFieldMappingsResponse response = client.admin().indices().prepareGetFieldMappings(index).setTypes(type).setFields("name").get();
        System.out.println(response.fieldMappings(index, type, "name").sourceAsMap().get("name"));
        // setType的话只能获取对应的type的，没有setType的话可以获取对应index下所有type的
//        GetMappingsResponse response1 = client.admin().indices().prepareGetMappings(index).setTypes(type).get();
//        System.out.println(response1.getMappings().get(index).get(type).sourceAsMap().keySet() + ", " + response1.getMappings().get(index).get(type).sourceAsMap().values());
//        System.out.println(response1.getMappings().get(index).get("student").sourceAsMap().keySet() + ", " + response1.getMappings().get(index).get("student").sourceAsMap().values());
    }

    // 刷新index
    public void refreshIndices()
    {
        IndicesAdminClient indicesAdminClient = client.admin().indices();
        // 刷新所有索引
        indicesAdminClient.prepareRefresh().get();
        // 刷新索引user
        indicesAdminClient.prepareRefresh("user").get();
        // 刷新索引user,es
        indicesAdminClient.prepareRefresh("user", "es").get();
    }

    // 更新index
    public void updateIndex()
    {
        IndicesAdminClient indicesAdminClient = client.admin().indices();
        ActionResponse response = indicesAdminClient.prepareUpdateSettings("user")
                .setSettings(Settings.builder()
                        .put("index.number_of_replicas", 0)) // 更新索引的副本数
                .get();
        System.out.println(response.getClass().getName());
        System.out.println(response);
    }

    // 清除所有索引

    /**
     * 如果索引有别名：
     * 如果该别名只对应当前被删除的索引，索引被删除，别名也会被删除
     * 如果该别名除了对应当前被删除的索引还对应其它索引，则当前索引被删除，别名不会被删除。
     * 即当删除索引时，如果该索引有别名（可以有多个），该索引对应的多个别名都会被删除，如果其它索引上的别名与被删除的索引别名相同，其它索引上的别名仍然会存在。
     * 但是任何情况下删除别名，别名对应的索引都不会被删除。
     */
    @Test
    public void deleteIndex()
    {
//        IndicesExistsResponse indicesExistsResponse = client.admin().indices()
//                .exists(new IndicesExistsRequest(new String[] {index, "es"}))
//                .actionGet();
        // 或者使用下面的方法
        IndicesExistsResponse indicesExistsResponse = client.admin().indices().prepareExists("my_index").get();
        // 直接删除，如果index不存在的话会报IndexNotFoundException
//        DeleteIndexResponse responsee = client.admin().indices().prepareDelete("es").get();
//        System.out.println(responsee.isAcknowledged());

        if (indicesExistsResponse.isExists()) {
            System.out.println("-------- exist ---------");
//            client.admin().indices().delete(new DeleteIndexRequest(index))
//                    .actionGet();
            DeleteIndexResponse response = client.admin().indices().prepareDelete("my_index").get();
//            DeleteIndexResponse response1 = client.admin().indices().prepareDelete("twitter").get();
            System.out.println(response.isAcknowledged());
//            System.out.println(response1.isAcknowledged());
        }
    }

    /**
     * 关闭索引
     */
    @Test
    public void closeIndex()
            throws ExecutionException, InterruptedException
    {
        // 查看索引是打开还是关闭状态
        ClusterStateResponse clusterStateResponse = client.admin().cluster().state(new ClusterStateRequest().indices(index)).get();
        String state = clusterStateResponse.getState().metaData().index(index).getState().name(); //CLOSE OPEN
        System.out.println("index:" + index + ", state:" + state);
        if (state.equalsIgnoreCase("close")) {
            System.out.println("索引已经是关闭状态");
            return;
        }
        // 没有关闭，将索引关闭, 关闭前，flush该index的日志
        // 在ES中，进行一次提交并删除事务日志的操作叫做 flush。分片每30分钟，或事务日志过大会进行一次flush操作。
        // 你很少需要手动flush，通常自动的就够了。当你要重启或关闭一个索引，flush该索引是很有用的。当ES尝试恢复或者重新打开一个索引时，它必须重放所有事务日志中的操作，所以日志越小，恢复速度越快。
        FlushResponse flushResponse = client.admin().indices().flush(new FlushRequest(index)).get();
        System.out.println(flushResponse.toString());

        CloseIndexResponse closeIndexResponse = client.admin().indices().prepareClose(index).get();
        if (closeIndexResponse.isAcknowledged()) {
            System.out.println("索引已经关闭");
            // 执行插入操作, 抛出异常IndexClosedException
            Person person = new Person();
            person.setAge(22);
            person.setInfo("It is a fine day today");
            person.setName("Taylor");
            person.setSex("女");
            person.setStudent(true);

            String source = JsonUtil.toJSON(person);
            IndexResponse response = client.prepareIndex(index, type, "12").setSource(source).get();
            System.out.println(response.getResult().name()); //
            System.out.println(response.status().name()); //
            System.out.println(response.status().getStatus());
        }
    }

    /**
     * 打开索引
     */

    @Test
    public void openIndex()
            throws ExecutionException, InterruptedException
    {
        ClusterStateResponse clusterStateResponse = client.admin().cluster().state(new ClusterStateRequest().indices(index)).get();
        String state = clusterStateResponse.getState().metaData().index(index).getState().name();
        System.out.println("index:" + index + ", state:" + state);
        if (state.equalsIgnoreCase("open")) {
            System.out.println("索引已经是打开状态");
            return;
        }
        OpenIndexResponse openIndexResponse = client.admin().indices().prepareOpen(index).get();
        if (openIndexResponse.isAcknowledged()) {
            System.out.println("索引已经打开");
            // 执行插入操作, 抛出异常IndexClosedException
            Person person = new Person();
            person.setAge(22);
            person.setInfo("It is a fine day today");
            person.setName("Taylor");
            person.setSex("女");
            person.setStudent(true);

            String source = JsonUtil.toJSON(person);
            IndexResponse response = client.prepareIndex(index, type, "12").setSource(source).get();
            System.out.println(response.getResult().name()); //CREATED
            System.out.println(response.status().name()); //CREATED
            System.out.println(response.status().getStatus()); //201
        }
    }

    /**
     * optimize API最好描述为强制合并段API。它强制分片合并段以达到指定max_num_segments参数。这是为了减少段的数量（通常为1）达到提高搜索性能的目的。
     * 警告:
     * 不要在动态的索引（正在活跃更新）上使用optimize API。后台的合并处理已经做的很好了，优化命令会阻碍它的工作。不要干涉！
     * <p>
     * 在特定的环境下，optimize API是有用的。典型的场景是记录日志，这中情况下日志是按照每天，周，月存入索引。旧的索引一般是只可读的，它们是不可能修改的。
     * 这种情况下，把每个索引的段降至1是有效的。搜索过程就会用到更少的资源，性能更好:
     */
    public void optimize()
    {
        //把索引中的每个分片都合并成一个段
        ForceMergeResponse forceMergeResponse = client.admin().indices().prepareForceMerge(index).setMaxNumSegments(1).get();
        System.out.println(forceMergeResponse.toString());
    }

    // 删除Index下的某个Type
    // 从2.x版本开始已经不能删除一个type
    // 只能删除index 或者删除 指定document
    @org.junit.Test
    public void deleteType()
            throws IOException
    {
//        DeleteResponse response = client.prepareDelete().setIndex(index).setType(type).setId("1").execute().actionGet();
//        System.out.println(response.getResult().name());
    }

    /**
     * 根据查询条件批量删除数据
     */
    @Test
    public void deleteByQuery()
    {
        int retationDays = 34;
        DateTimeFormatter formatWithMill = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        LocalDateTime localDateTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0).minusDays(retationDays - 1);
        System.out.println(localDateTime.toString());
        String timestamp = formatWithMill.format(localDateTime);
        System.out.println(timestamp);
//        BoolQueryBuilder after = QueryBuilders.boolQuery()
//                .filter(QueryBuilders.rangeQuery("timestamp").lt(timestamp));
//        BulkIndexByScrollResponse response =
//                DeleteByQueryAction.INSTANCE.newRequestBuilder(client)
//                        .filter(after)
//                        .source("xdata-log")
//                        .get();
//
//        long deleted = response.getDeleted();
//        System.out.println("delete size:" + deleted);
    }

    /**
     * The aliases API
     * 为索引创建别名，一个索引可以有多个别名， 多个索引也可以对应同一个别名
     * 注意：索引名和别名不能一样，别名可以看做索引名，索引名是不能重复的，所以索引名和别名不能一样。
     */
    @Test
    public void aliasesAPI()
    {
        IndicesAliasesResponse response = client.admin().indices().prepareAliases()
                .addAlias("my_index", "my_alias")
                .execute()
                .actionGet();
        System.out.println(response.isAcknowledged());
    }

    /**
     * 获取某个索引的所有别名
     */
    @Test
    public void getIndexAlias()
            throws ExecutionException, InterruptedException
    {
        GetAliasesResponse response = client.admin().indices().getAliases(new GetAliasesRequest().indices("es")).get();
        List<AliasMetaData> list = response.getAliases().get("es");
        for (AliasMetaData aliasMetaData : list) {
            System.out.println(aliasMetaData.getAlias());
        }
    }

    /**
     * The get aliases API
     * 获取别名对应的索引
     */
    @Test
    public void getAliasesAPI()
    {
        GetAliasesResponse response = client.admin().indices()
                .prepareGetAliases("es_alias")
                .execute().actionGet();
        response.getAliases().forEach((v) -> {
            System.out.println(v.key); // index名
            System.out.println(v.index); // index中含有的doc数量（这个数量是总的数量包含的副本的数量即： 实际doc数量x(副本数+1)）
        });
        System.out.println("-----------------------------------");
        Iterator<String> iterator = response.getAliases().keysIt();
        while (iterator.hasNext()) {
            String index = iterator.next();
            System.out.println(index);
        }
    }

    /**
     * The aliases exists API
     * 判断别名是否存在
     */
    @Test
    public void aliasesExistsAPI()
    {
        AliasesExistResponse response = client.admin().indices()
                .prepareAliasesExist("es_alias")
                .execute().actionGet();
        System.out.println(response.exists());
    }

    /**
     * 删除索引别名
     * 注意，只会删除索引对应的别名，如果其它索引也有该别名，则其它索引对应的别名不会删除。
     * 如：索引 es 有别名： es_alias
     * 索引 my_index 有别名： es_alias
     * 则removeAlias("my_index", "es_alias")，只会删除my_index对应的别名es_alias, 索引es的别名es_alias依然存在。
     */
    @Test
    public void aliasesDeleteAPI()
    {
        IndicesAliasesResponse response = client.admin().indices()
                .prepareAliases()
                .removeAlias("my_index", "es_alias")
                .execute().actionGet();
        System.out.println(response.isAcknowledged());
    }

    // 通过ClusterAdminClient获取集群的健康状况
    @Test
    public void health()
    {
        ClusterAdminClient clusterAdminClient = client.admin().cluster();
        ClusterHealthResponse healths = clusterAdminClient.prepareHealth().get();
        String clusterName = healths.getClusterName();
        System.out.println("clusterName:" + clusterName);
        int numberOfDataNodes = healths.getNumberOfDataNodes();
        System.out.println("numberOfDataNodes:" + numberOfDataNodes);
        int numberOfNodes = healths.getNumberOfNodes();
        System.out.println("numberOfNodes:" + numberOfNodes);

        for (ClusterIndexHealth health : healths.getIndices().values()) {
            String index = health.getIndex();
            int numberOfShards = health.getNumberOfShards();
            int numberOfReplicas = health.getNumberOfReplicas();
            ClusterHealthStatus status = health.getStatus();
            System.out.println("index:" + index + ",numberOfShards:" + numberOfShards + ",numberOfReplicas:" + numberOfReplicas + ",status:" + status);
        }
    }
}
