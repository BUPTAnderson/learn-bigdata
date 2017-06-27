package org.learning;

import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.common.settings.Settings;
import org.junit.Test;
import org.learning.util.JsonUtil;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Created by anderson on 17-3-8.
 */
public class IndexTemplates
        extends BaseTest
{
    /**
     * 获取所有模板
     * GET /_template/
     * 获取模板
     * GET /_template/xdata-log_template
     * 删除模板
     * DELETE /_template/xdata-log_template
     * 下面这个是针对所有开头是xdata-log-的索引创建模板，并且是针对type是log的创建对应的mapping
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
                .addMapping("log", "{\n" +
                        "    \"log\": {\n" +
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
                        "                \"type\": \"object\"\n" +
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

    @Test
    public void indexApi()
            throws IOException
    {
        Log log = new Log();
        log.setPath("/export/Domains/idata-ras-api.jcloud.com/server1/logs");
        log.setHost("192.168.178.80");
        log.setContext("三月 03, 2017 12:07:43 上午 com.jd.service.impl.LogCenterServiceImpl lambda$info$0\n" +
                "信息: insert ES log: indexId:logcenter, type:ras-api-ddl, timeSlice:null, message:null, uuidTraceKey:null, jsonInfo:{\"databaseName\":\"a_test\",\"tableName\":\"filetype_orc\"}, operationName:getTabl\n" +
                "ePreviewData, esRecordId:null, userName:datajingdo_m");
        log.setTimestamp(Timestamp.valueOf(LocalDateTime.now()).toString());
        log.setTag("test");
        String source = JsonUtil.serialize(log);
        System.out.println(source);
        // 可以不传入id参数，那样的话es会自动生成id
        IndexResponse response = client.prepareIndex("xdata-log-2017.03.07", "log", "1").setSource(source).get();
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

    public static void main(String[] args)
    {
//        Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
//        System.out.println(timestamp);
        Log log = new Log();
        log.setPath("/export/Domains/idata-ras-api.jcloud.com/server1/logs");
        log.setHost("192.168.178.80");
        log.setContext("三月 03, 2017 12:07:43 上午 com.jd.service.impl.LogCenterServiceImpl lambda$info$0\n" +
                "信息: insert ES log: indexId:logcenter, type:ras-api-ddl, timeSlice:null, message:null, uuidTraceKey:null, jsonInfo:{\"databaseName\":\"a_test\",\"tableName\":\"filetype_orc\"}, operationName:getTabl\n" +
                "ePreviewData, esRecordId:null, userName:datajingdo_m");
        log.setTimestamp(Timestamp.valueOf(LocalDateTime.now()).toString());
        log.setTag("test");
        String str = JsonUtil.serialize(log);
        System.out.println(str);
    }

    @Test
    public void testTime()
    {
        Timestamp timestamp = new Timestamp(1420070400001L);
        System.out.println(timestamp.toString());
        System.out.println(LocalDateTime.now().toString());
        LocalDateTime localDateTime = timestamp.toLocalDateTime();
        System.out.println(localDateTime.toString());
    }
}

class Log
{
    private String path;
    private String host;
    private String context;
    private String tag;
    private String timestamp;

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public String getContext()
    {
        return context;
    }

    public void setContext(String context)
    {
        this.context = context;
    }

    public String getTag()
    {
        return tag;
    }

    public void setTag(String tag)
    {
        this.tag = tag;
    }

    public String getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(String timestamp)
    {
        this.timestamp = timestamp;
    }

    public Log()
    {
    }

    public Log(String path, String host, String context, String tag, String timestamp)
    {
        this.path = path;
        this.host = host;
        this.context = context;
        this.tag = tag;
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Log log = (Log) o;
        return Objects.equals(path, log.path) &&
                Objects.equals(host, log.host) &&
                Objects.equals(context, log.context) &&
                Objects.equals(tag, log.tag) &&
                Objects.equals(timestamp, log.timestamp);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(path, host, context, tag, timestamp);
    }
}
