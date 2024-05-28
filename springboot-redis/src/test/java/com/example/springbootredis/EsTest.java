package com.example.springbootredis;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson2.JSONObject;
import com.example.springbootredis.pojo.doc.ShopDoc;
import com.example.springbootredis.pojo.view.Page;
import com.example.springbootredis.pojo.vo.ShopVo;
import com.example.springbootredis.service.ShopService;
import com.example.springbootredis.utils.ElasticSearchUtils;
import jakarta.annotation.Resource;
import org.checkerframework.checker.units.qual.C;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest
public class EsTest {
    @Autowired
    private RestHighLevelClient client;
    @Autowired
    private ShopService shopService;

    @Test
    void addIndex() throws IOException {
        ShopVo shopVo = shopService.getById(1L);
        IndexRequest request = new IndexRequest("shop");
        request.id(shopVo.getId().toString());
        request.source(XContentType.JSON, JSONObject.toJSONString(shopVo));
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
    }

    @Test
    void index() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest("shop");
        XContentBuilder mappings = XContentFactory.jsonBuilder()
                .startObject()
                .startObject("properties")
                .startObject("id").field("type", "long").endObject()
                .startObject("name").field("type", "completion").field("analyzer", "text_analyzer").field("search_analyzer", "ik_max_word").field("copy_to", "all").endObject()
                .startObject("typeId").field("type", "long").endObject()
                .startObject("area").field("type", "completion").field("analyzer", "text_analyzer").field("search_analyzer", "ik_max_word").field("copy_to", "all").endObject()
                .startObject("address").field("type", "completion").field("analyzer", "ik_smart").endObject()
                .startObject("location").field("type", "geo_point").endObject()
                .startObject("avgPrice").field("type", "long").endObject()
                .startObject("sold").field("type", "long").endObject()
                .startObject("comments").field("type", "long").endObject()
                .startObject("score").field("type", "long").endObject()
                .startObject("all").field("type", "text").field("analyzer", "text_analyzer").field("search_analyzer", "ik_max_word").endObject()
                .startObject("suggestion").field("type", "completion").field("analyzer", "completion_analyzer").endObject()
                .startObject("openHours").field("type", "keyword").endObject()
                .endObject()
                .endObject();

        XContentBuilder settings = XContentFactory.jsonBuilder()
                .startObject()
                .startObject("analysis")
                .startObject("analyzer")
                .startObject("text_analyzer")
                .field("tokenizer", "ik_max_word")
                .field("filter", "py")
                .endObject()
                .startObject("completion_analyzer")
                .field("tokenizer", "keyword")
                .field("filter", "py")
                .endObject()
                .endObject()
                .startObject("filter")
                .startObject("py")
                .field("type", "pinyin")
                .field("keep_full_pinyin", false)
                .field("keep_joined_full_pinyin", true)
                .field("keep_original", true)
                .field("limit_first_letter_length", "16")
                .field("romove_duplicated_term", true)
                .field("none_chinese_pinyin_tokenize", false)
                .endObject()
                .endObject()
                .endObject()
                .endObject();

        request.mapping(mappings);
        request.settings(settings);
        //  request.settings(Settings.builder()..build());
        client.indices().create(request, RequestOptions.DEFAULT);
    }

    @Test
    void deleteIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("shop");
        client.indices().delete(request, RequestOptions.DEFAULT);
    }
//
//    @Test
//    void bulk() throws IOException {
//        List<ShopVo> shopVos = shopService.getAll();
//        BulkRequest request = new BulkRequest("shop");
//        shopVos.stream().map(shopVo -> new ShopDoc(shopVo)).
//                forEach(doc -> request.add(new IndexRequest("shop")
//                        .id(doc.getId().toString())
//                        .source(JSONObject.toJSONString(doc), XContentType.JSON))
//
//                );
//        BulkResponse response = client.bulk(request, RequestOptions.DEFAULT);
//        System.out.println("hasFailures() = " + response.hasFailures());
//    }
//
//    @Test
//    void addDoc() throws IOException {
//        ShopVo shopVo = shopService.getById(1L);
//        ShopDoc doc = new ShopDoc(shopVo);
//        IndexRequest request = new IndexRequest("shop").id(doc.getId().toString());
//        request.source(JSONObject.toJSONString(doc), XContentType.JSON);
//        client.index(request, RequestOptions.DEFAULT);
//
//    }

    @Test
    void query() throws IOException {
        Page<ShopVo> page = ElasticSearchUtils.pageTermAs("shop", "typeId", 1, 1, 10, ShopVo.class);
        System.out.println(JSONObject.toJSONString(page));
    }

    @Test
    void agg() throws IOException {
        ArrayList<String> names = new ArrayList<>();
        names.add("openHours");
        names.add("score");
        Map<String, List<String>> map = ElasticSearchUtils.aggTermsQuery("shop", names, "Agg");
        map.keySet().forEach(key -> {
            List<String> list = map.get(key);
            System.out.println("---------------------------------------");
            System.out.println("--------------" + key + "-------------");
            list.stream().forEach(item -> System.out.print(item + "  "));
            System.out.println();
        });
    }

    @Test
    void range() throws IOException {
        List<ShopDoc> shopDocs = ElasticSearchUtils.rangeAs("shop", "score", 10, 60, true, true, ShopDoc.class);
        shopDocs.stream().forEach(doc-> System.out.println(doc.getScore()));

    }

}
