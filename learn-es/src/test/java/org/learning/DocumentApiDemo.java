package org.learning;

import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.index.get.GetResult;
import org.elasticsearch.index.shard.ShardId;
import org.junit.Test;
import org.learning.entity.Person;
import org.learning.util.JsonUtil;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Created by anderson on 16-11-30.
 * Single document APIs:
 * - Index API
 * - Get API
 * - Delete API
 * - Update API
 * <p>
 * Multi-document APIs
 * - Multi Get API
 * - Bulk API
 * <p>
 * All CRUD APIs are single-index APIs. The index parameter accepts a single index name, or an alias which points to a single index.
 */
public class DocumentApiDemo
        extends BaseTest
{
    /**
     * Index API
     * 索引一条Document
     */
    @Test
    public void indexApi()
            throws IOException
    {
        Person person = new Person();
        person.setAge(20);
        person.setInfo("Jack an Lucy");
        person.setName("Jack");
        person.setSex("男");
        person.setStudent(true);

        String source = JsonUtil.toJSON(person);
        System.out.println(source);
//        Person p = JsonUtil.deserialize(source, Person.class);
//        System.out.println(p);
        // 可以不传入id参数，那样的话es会自动生成id
        IndexResponse response = client.prepareIndex(index, type, "3").setSource(source).get();
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

    /**
     * Get API
     * 获取一条Document
     */
    @Test
    public void GetApi()
    {
        // 必须指定index, type, id
//        GetResponse response = client.prepareGet().setIndex(index).setType(type).setId("1").get();
        // 这样也可以
        GetResponse response = client.prepareGet(index, type, "2").get();
        String index = response.getIndex();
        String type = response.getType();
        String id = response.getId();
        long version = response.getVersion();
        boolean found = response.isExists();
        String source = response.getSourceAsString();
        System.out.println("index:" + index + ", type:" + type + ",id:" + id + ",version:" + version + ",found:" + found + ", source:" + source);
    }

    /**
     * Delete API
     * 删除一条Document
     */
    @Test
    public void deleteAPI()
    {
        DeleteResponse response = client.prepareDelete().setIndex(index).setType(type).setId("1").get();
        // 下面的也可以
//        DeleteResponse response = client.prepareDelete(index, type, "1").get();
        ShardId shardId = response.getShardId(); // [user][3] -> "[" + index.getName() + "][" + shardId + "]"
        System.out.println(shardId.toString());
        ReplicationResponse.ShardInfo shardInfo = response.getShardInfo();
        System.out.println(shardInfo); // ShardInfo{total=2, successful=2, failures=[]}
        String index = response.getIndex();
        String type = response.getType();
        String id = response.getId();
        long version = response.getVersion();
        String result = response.getResult().name(); //DELETED, NOT_FOUND
        String responseName = response.status().name(); // OK, NOT_FOUND
        System.out.println("index:" + index + ", type:" + type + ",id:" + id + ",version:" + version + ",result:" + result + ", responseName:" + responseName);
    }

    /**
     * Update API
     * 更新Document， 通过创建UpdateRequest
     */
    @Test
    public void updateAPI()
            throws IOException, ExecutionException, InterruptedException
    {
        UpdateRequest updateRequest = new UpdateRequest(index, type, "1")
                .doc(jsonBuilder()
                        .startObject()
                        .field("age", "25")
                        .endObject());
        UpdateResponse response = client.update(updateRequest).get();
        ShardId shardId = response.getShardId(); // [user][3] -> "[" + index.getName() + "][" + shardId + "]"
        System.out.println(shardId.toString()); // ShardInfo{total=2, successful=2, failures=[]}
        ReplicationResponse.ShardInfo shardInfo = response.getShardInfo();
        System.out.println(shardInfo);
        String index = response.getIndex();
        String type = response.getType();
        String id = response.getId();
        long version = response.getVersion();
        String result = response.getResult().name(); // UPDATED
        String responseName = response.status().name(); // OK
        System.out.println("index:" + index + ", type:" + type + ",id:" + id + ",version:" + version + ",result:" + result + ", responseName:" + responseName);
        GetResult getResult = response.getGetResult(); // null
        if (getResult == null) {
            System.out.println("getResult is null");
        }
        else {
            System.out.println(getResult.isExists() + "," + getResult.getIndex() + "," + getResult.getType() + "," + getResult.getId() + "," + getResult.getVersion()
                    + "," + getResult.getSource());
        }
    }

    /**
     * Update API
     * 更新Document， 通过prepareUpdate()方法
     */
    @Test
    public void updateAPI2()
            throws IOException
    {
//        UpdateResponse response = client.prepareUpdate(index, type, "1")
//                .setDoc(jsonBuilder()
//                        .startObject()
//                        .field("age", "28")
//                        .endObject())
//                .get();
        // 不使用jsonBuilder而是使用json
        Person person = new Person();
        person.setAge(25);
        person.setInfo("Jack an Lucy");
        person.setName("Jack");
        person.setSex("男");
        person.setStudent(true);

        String source = JsonUtil.toJSON(person);
        UpdateResponse response = client.prepareUpdate(index, type, "1").setDoc(source).get();

        String index = response.getIndex();
        String type = response.getType();
        String id = response.getId();
        long version = response.getVersion();
        String result = response.getResult().name(); // UPDATED
        String responseName = response.status().name(); // OK
        System.out.println("index:" + index + ", type:" + type + ",id:" + id + ",version:" + version + ",result:" + result + ", responseName:" + responseName);
    }

    /**
     * Update API
     * 更新Document, 不存在的话插入, 如果索引和type不存在也会创建对应的index和type并索引Document
     */
    @Test
    public void upsertAPI()
            throws IOException, ExecutionException, InterruptedException
    {
        Person person = new Person();
        person.setAge(20);
        person.setInfo("Lucy is a beautiful girl");
        person.setName("Lucy");
        person.setSex("女");
        person.setStudent(true);
        String source = JsonUtil.toJSON(person);

        IndexRequest indexRequest = new IndexRequest(index, type, "2").source(source);
//                .source(jsonBuilder()
//                        .startObject()
//                        .field("name", "Joe Smith")
//                        .field("gender", "male")
//                        .endObject());
        UpdateRequest updateRequest = new UpdateRequest(index, type, "2")
                .doc(jsonBuilder()
                        .startObject()
                        .field("sex", "male")
                        .endObject())
                .upsert(indexRequest);
        UpdateResponse response = client.update(updateRequest).get();
        String index = response.getIndex();
        String type = response.getType();
        String id = response.getId();
        long version = response.getVersion();
        String result = response.getResult().name(); // CREATED
        String responseName = response.status().name(); // CREATED
        System.out.println("index:" + index + ", type:" + type + ",id:" + id + ",version:" + version + ",result:" + result + ", responseName:" + responseName);
    }

    /**
     * Muti Get API
     */
    @Test
    public void mutiGetAPI()
    {
        MultiGetResponse multiGetItemResponses = client.prepareMultiGet()
                .add(index, type, "1")
                .add(index, type, "2", "3")
                .add("bank", "account", "14")
                .get();

        for (MultiGetItemResponse itemResponse : multiGetItemResponses) {
            GetResponse response = itemResponse.getResponse();
            if (response.isExists()) {
                String json = response.getSourceAsString();
                System.out.println(json);
            }
        }
    }

    /**
     * Bulk API
     * Bulk是批量操作，可以包含如下操作：
     * create   当文档不存在时创建之。
     * index    创建新文档或替换已有文档。
     * update   局部更新文档。
     * delete   删除一个文档。
     * 注意：如果使用Bulk执行批量导入的话，导入过程中因为网络等原因，bulk操作失败了，会造成部分数据导入，部分数据没有导入的情况，这个时候如果再次执行导入操作，
     * 如果document的id是自动生成的话会造成数据重复！！！所以进行批量导入的时候，
     * 1.如果是使用的restful api，应该使用create操作，并且要指定id值：
     * { "create":  { "_index": "website", "_type": "blog", "_id": "1" }}
     * { "title":    "My first blog post" }
     * { "create":  { "_index": "website", "_type": "blog", "_id": "2" }}
     * { "title":    "My second blog post" }
     * 这样id重复的就不会成功了！
     * 2.如果是使用的java client，应该使用prepareIndex(String index, String type, @Nullable String id)方法（java client中没有prepareCreate方法），
     * 并且一定要给定id参数值！
     */
    @Test
    public void bulkAPI()
    {
        Person person = new Person();
        person.setAge(1);
        person.setInfo("little Buick has a buick car");
        person.setName("Buick");
        person.setSex("男");
        person.setStudent(true);
        person.setDateOfBirth("2015-01-01");

        Person person1 = new Person();
        person1.setAge(5);
        person1.setInfo("Anna has many book");
        person1.setName("Anna");
        person1.setSex("女");
        person1.setStudent(true);
        person1.setDateOfBirth("2011-05-05");

        Person person2 = new Person();
        person2.setAge(25);
        person2.setInfo("address: 806 Rockwell Place");
        person2.setName("Mary");
        person2.setSex("女");
        person2.setStudent(false);
        person2.setDateOfBirth("1991-01-19");

        Person person3 = new Person();
        person3.setAge(20);
        person3.setInfo("David is my good boy");
        person3.setName("David");
        person3.setSex("男");
        person3.setStudent(false);
        person3.setDateOfBirth("1996-08-15");

        Person person4 = new Person();
        person4.setAge(50);
        person4.setInfo("James is a elasticsearch developer");
        person4.setName("James");
        person4.setSex("男");
        person4.setStudent(false);
        person4.setDateOfBirth("1966-09-09");

        Person person5 = new Person();
        person5.setAge(75);
        person5.setInfo("my grandmother name is Lily");
        person5.setName("Lily");
        person5.setSex("女");
        person5.setStudent(false);
        person5.setDateOfBirth("1941-05-12");

        Person person6 = new Person();
        person6.setAge(95);
        person6.setInfo("Henry is a retired general");
        person6.setName("Henry");
        person6.setSex("男");
        person6.setStudent(false);
        person6.setDateOfBirth("1921-12-12");

        Person person7 = new Person();
        person7.setAge(30);
        person7.setInfo("The protagonist of Shawshank's salvation is Andy");
        person7.setName("Andy");
        person7.setSex("男");
        person7.setStudent(true);
        person7.setDateOfBirth("1986-07-28");

        Person person8 = new Person();
        person8.setAge(35);
        person8.setInfo("Frank is a generous man");
        person8.setName("Frank");
        person8.setSex("男");
        person8.setStudent(true);
        person8.setDateOfBirth("1981-05-25");

        Person person9 = new Person();
        person9.setAge(40);
        person9.setInfo("Linda is a college teacher");
        person9.setName("Linda");
        person9.setSex("女");
        person9.setStudent(true);
        person9.setDateOfBirth("1976-10-01");

        String source = JsonUtil.toJSON(person);
        String source1 = JsonUtil.toJSON(person1);
        String source2 = JsonUtil.toJSON(person2);
        String source3 = JsonUtil.toJSON(person3);
        String source4 = JsonUtil.toJSON(person4);
        String source5 = JsonUtil.toJSON(person5);
        String source6 = JsonUtil.toJSON(person6);
        String source7 = JsonUtil.toJSON(person7);
        String source8 = JsonUtil.toJSON(person8);
        String source9 = JsonUtil.toJSON(person9);
//        System.out.println(source);
//        System.out.println(source1);
//        System.out.println(source2);
//        System.out.println(source3);
//        System.out.println(source4);
//        System.out.println(source5);
//        System.out.println(source6);
//        System.out.println(source7);
//        System.out.println(source8);
//        System.out.println(source9);

        BulkRequestBuilder bulkRequest = client.prepareBulk();
        // either use client#prepare, or use Requests# to directly build index/delete requests
        bulkRequest.add(client.prepareIndex(index, type, "1").setSource(source));
        bulkRequest.add(client.prepareIndex(index, type, "2").setSource(source1));
        bulkRequest.add(client.prepareIndex(index, type, "3").setSource(source2));
        bulkRequest.add(client.prepareIndex(index, type, "4").setSource(source3));
        bulkRequest.add(client.prepareIndex(index, type, "5").setSource(source4));
        bulkRequest.add(client.prepareIndex(index, type, "6").setSource(source5));
        bulkRequest.add(client.prepareIndex(index, type, "7").setSource(source6));
        bulkRequest.add(client.prepareIndex(index, type, "8").setSource(source7));
        bulkRequest.add(client.prepareIndex(index, type, "9").setSource(source8));
        bulkRequest.add(client.prepareIndex(index, type, "10").setSource(source9));

//        bulkRequest.add(client.prepareUpdate(index, type, "1").setDoc(source));
//        bulkRequest.add(client.prepareUpdate(index, type, "2").setDoc(source1));
//        bulkRequest.add(client.prepareUpdate(index, type, "3").setDoc(source2));
//        bulkRequest.add(client.prepareUpdate(index, type, "4").setDoc(source3));
//        bulkRequest.add(client.prepareUpdate(index, type, "5").setDoc(source4));
//        bulkRequest.add(client.prepareUpdate(index, type, "6").setDoc(source5));
//        bulkRequest.add(client.prepareUpdate(index, type, "7").setDoc(source6));
//        bulkRequest.add(client.prepareUpdate(index, type, "8").setDoc(source7));
//        bulkRequest.add(client.prepareUpdate(index, type, "9").setDoc(source8));
//        bulkRequest.add(client.prepareUpdate(index, type, "10").setDoc(source9));

        BulkResponse bulkResponse = bulkRequest.get();
        if (bulkResponse.hasFailures()) {
            // process failures by iterating through each bulk response item
            System.out.println("index error!");
        }
        else {
            for (BulkItemResponse item : bulkResponse.getItems()) {
                DocWriteResponse response = item.getResponse();
                System.out.println(response.status().name());
            }
        }
    }
}
