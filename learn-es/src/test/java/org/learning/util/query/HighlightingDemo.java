package org.learning.util.query;

import org.apache.lucene.queryparser.classic.QueryParser;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.WildcardQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.junit.Test;
import org.learning.BaseTest;
import org.learning.entity.Person;
import org.learning.util.JsonUtil;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by anderson on 16-12-21.
 */
public class HighlightingDemo extends BaseTest
{
    /**
     * 查询结果高亮
     * @throws IOException
     *
     * require_field_match can be set to false which will cause any field to be highlighted regardless of whether the query matched specifically on them.
     * The default behaviour is true, meaning that only fields that hold a query match will be highlighted.
     * 见官方链接：https://www.elastic.co/guide/en/elasticsearch/reference/5.1/search-request-highlighting.html#plain-highlighter
     */
    @Test
    public void highlightQuery() throws IOException, NoSuchFieldException, IllegalAccessException {
//        QueryBuilder qb = QueryBuilders.termQuery("name", "anderson");
        QueryBuilder qb = QueryBuilders.matchQuery("name", "anderson"); //设置字段 值, 值可以是boolean，string，数字
        QueryBuilder queryString = QueryBuilders.queryStringQuery("anderson");
        RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("age").from(10).to(90).includeLower(true).includeUpper(false);
        WildcardQueryBuilder wildcardQueryBuilder = QueryBuilders.wildcardQuery("info", "m*");  //会查找符合m*的单词，并把符合的单词高亮
        SearchResponse searchResponse = client.prepareSearch(index)
                .setTypes(type).setQuery(qb)
                .setPostFilter(queryString)
                // 下面的设置中，如果不设置requireFieldMatch为false，则当field中的字段与queryBuilder中查询的字段不一致时，结果不会高亮。即不设置false的话，field中的字段必须与QueryBuilder中查询的字段一致，结果才会高亮。
//                .highlighter(new HighlightBuilder().field("info").field("name").requireFieldMatch(false))
//                .highlighter(new HighlightBuilder().field("info").preTags("<span style=\"color:red\">").postTags("</span>").requireFieldMatch(false)) //自定义高亮的格式
                .highlighter(new HighlightBuilder().field("*").preTags("<span style=\"color:red\">").postTags("</span>").requireFieldMatch(false))  // 设为*的话，会查找所有符合的字段
                .setFrom(0).setSize(10)
//                .setExplain(true)
                .execute().actionGet();

        //获取搜索的文档结果
        SearchHits searchHits = searchResponse.getHits();
        SearchHit[] hits = searchHits.getHits();

        for (int i = 0; i < hits.length; i++) {
            SearchHit hit = hits[i];
            //将文档中的每一个对象转换json串值
            String json = hit.getSourceAsString();
            //将json串值转换成对应的实体对象
            Person person = JsonUtil.deserialize(json, Person.class);
            System.out.println(person);
            //获取对应的高亮域
            Map<String, HighlightField> result = hit.highlightFields();
            if (result.isEmpty()) {
                return;
            } else {
                System.out.println("key set:" + result.keySet());
            }
            for (String field : result.keySet()) {
                //从设定的高亮域中取得指定域
                HighlightField titleField = result.get(field);
                System.out.println(titleField.getFragments()[0].toString());
                //取得定义的高亮标签
                Text[] titleTexts =  titleField.fragments();
                //为title串值增加自定义的高亮标签
                String title = "++";
                for (Text text : titleTexts) {
                    title += text;
                }
                //将追加了高亮标签的串值重新填充到对应的对象
                Field personField = Person.class.getDeclaredField(field);
                personField.setAccessible(true);
                personField.set(person, title);
//                person.setInfo(title);
            }
            //打印高亮标签追加完成后的实体对象
            System.out.println(person);
        }
        System.out.println("search success ..");
    }

    /**
     * 搜索关键字包含了特殊字符,那么程序就会报错
     * 需要先把含有特殊字符的搜索关键词进行转义
     */
    @Test
    public void parser() {
        String title = "title+-&&||!(){}[]^\"~*?:\\";
        title = QueryParser.escape(title); // 主要就是这一句把特殊字符都转义,那么lucene就可以识别
        System.out.println(title);
    }
}
