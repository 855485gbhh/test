package com.example.springbootredis.utils;

import com.alibaba.fastjson2.JSONObject;
import com.example.springbootredis.pojo.view.Page;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.http.HttpHost;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ElasticSearchUtils {
//    private Class<T> clazz;
//
//    public ElasticSearchUtils(Class<T> clazz) {
//        this.clazz = clazz;
//    }
//
//    public ElasticSearchUtils() {
//
//    }
//
//    public ElasticSearchUtils clazz(Class<T> clazz) {
//        this.clazz = clazz;
//        return this;
//    }

    private final String TERMS = "terms";
    private final String STATS = "stats";
    private final String AGG_SUFFIX = "_agg";
    private final static int MAX_DOC_COUNT = 10000;
    private static RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(HttpHost.create("http://localhost:9200")));


    public static <T> void addDoc(String index, T object) throws NoSuchFieldException, IllegalAccessException, IOException {
        Field idField = object.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        IndexRequest request = new IndexRequest(index).id((String) idField.get(object));
        request.source(JSONObject.toJSONString(object), XContentType.JSON);
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);

    }

    public static void deleteDoc(String index, Long id) throws NoSuchFieldException, IllegalAccessException, IOException {
        DeleteRequest request = new DeleteRequest(index).id(id + "");
        client.delete(request, RequestOptions.DEFAULT);
    }

    public static <T, V> Page<T> pageTermAs(String index, String fieldName, V value, int pageNum, int pageSize, Class<T> clazz) throws IOException {
        SearchRequest request = new SearchRequest(index);
        TermQueryBuilder queryBuilder = QueryBuilders.termQuery(fieldName, value);
        request.source()
                .query(queryBuilder)
                .from((pageNum - 1) * pageSize)
                .size(pageSize);
        int total = pageTermTotal(index, fieldName, value);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        SearchHit[] hits = response.getHits().getHits();

        List<T> list = Arrays.stream(hits).map(doc -> JSONObject.parseObject(doc.getSourceAsString(), clazz)).collect(Collectors.toList());
        return new Page<T>(total, list.size(), pageNum, pageSize, list);
    }

    private static <V> int pageTermTotal(String index, String fieldName, V value) throws IOException {
        SearchRequest request = new SearchRequest(index);
        TermQueryBuilder queryBuilder = QueryBuilders.termQuery(fieldName, value);
        request.source()
                .query(queryBuilder);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        return response.getHits().getHits().length;
    }

    public static <T, V> Page<T> pageMatchAs(String index, String fieldName, V value, int pageNum, int pageSize, Class<T> clazz) throws IOException {
        SearchRequest request = new SearchRequest(index);
        MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery(fieldName, value);
        request.source()
                .query(queryBuilder)
                .from((pageNum - 1) * pageSize)
                .size(pageSize);
        int total = pageMatchTotal(index, fieldName, value);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        SearchHit[] hits = response.getHits().getHits();

        List<T> list = Arrays.stream(hits).map(doc -> JSONObject.parseObject(doc.getSourceAsString(), clazz)).collect(Collectors.toList());
        return new Page<T>(total, list.size(), pageNum, pageSize, list);
    }

    private static <V> int pageMatchTotal(String index, String fieldName, V value) throws IOException {
        SearchRequest request = new SearchRequest(index);
        MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery(fieldName, value);
        request.source()
                .query(queryBuilder);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        return response.getHits().getHits().length;
    }

    public static <T> List<T> selectAllDoc(String index, Class<T> clazz) throws IOException {
        return selectAllDoc(index, clazz, null, null);
    }

    public static <T> List<T> selectAllDoc(String index, Class<T> clazz, String sortBy, Boolean isAsc) throws IOException {
        SearchRequest request = new SearchRequest(index);
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .size(MAX_DOC_COUNT);
        if (sortBy != null && isAsc != null) {
            if (isAsc == true) {
                SortBuilders.fieldSort(sortBy).order(SortOrder.ASC);
            } else {
                SortBuilders.fieldSort(sortBy).order(SortOrder.DESC);
            }
        }
        request.source(builder);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        SearchHit[] hits = response.getHits().getHits();
        List<T> list = new ArrayList<>();
        Arrays.stream(hits).forEach(doc -> {
            T object = JSONObject.parseObject(doc.getSourceAsString(), clazz);
            list.add(object);
        });
        return list;
    }


//    public static List getField(String fieldName, String index, boolean isAsc) throws IOException {
//        SearchRequest request = new SearchRequest(index);
//
//        request.source().size(0)
//                .aggregation(AggregationBuilders.terms(fieldName + "Agg")
//                        .field(fieldName)
//                        .order(BucketOrder.aggregation("count", isAsc)));
//        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
//        Terms fieldTerms = response.getAggregations().get(fieldName + "Agg");
//        ArrayList<Object> list = new ArrayList<>();
//        fieldTerms.getBuckets().forEach(bucket -> {
//            list.add(bucket.getKey());
//        });
//        return list;
//    }
//
//    public static Map<String, List<String>> get() throws IOException {
//        SearchRequest request = new SearchRequest("hotel");
//
//        request.source().size(0)
//                .aggregation(AggregationBuilders.terms("brandAgg")
//                        .field("brand"))
//                .aggregation(AggregationBuilders.terms("cityAgg")
//                        .field("city"))
//                .aggregation(AggregationBuilders.terms("starAgg")
//                        .field("star"));
//        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
//        Aggregations aggregations = response.getAggregations();
//        List<String> brandList = agg(aggregations, "brandAgg");
//        List<String> cityList = agg(aggregations, "cityAgg");
//        List<String> starList = agg(aggregations, "starAgg");
//        Map<String, List<String>> map = new HashMap<>();
//        map.put("brand", brandList);
//        map.put("city", cityList);
//        map.put("star", starList);
//        return map;
//
//    }

    private static List<String> agg(Aggregations aggregations, String aggName) {
        Terms brandTerms = aggregations.get(aggName);
        List<? extends Terms.Bucket> buckets = brandTerms.getBuckets();
        ArrayList<String> list = new ArrayList<>();
        for (Terms.Bucket bucket : buckets) {
            list.add(bucket.getKeyAsString());
        }
        return list;
    }


    public static <T, V> List<T> getDocByTerm(String index, String fieldName, V value, Class<T> clazz) throws IOException {
        SearchRequest request = new SearchRequest(index);
        request.source().query(QueryBuilders.termQuery(fieldName, value));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        return responseHit(response, clazz);
    }

    public static <T, V> List<T> getDocByMatch(String index, String fieldName, V value, Class<T> clazz) throws IOException {
        SearchRequest request = new SearchRequest(index);
        request.source().query(QueryBuilders.matchQuery(fieldName, value));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        return responseHit(response, clazz);
    }

    public static <T> List<T> responseHit(SearchResponse response, Class<T> clazz) {
        SearchHit[] hits = response.getHits().getHits();
        List<T> list = new ArrayList<>();
        Arrays.stream(hits).forEach(doc -> {
            T object = JSONObject.parseObject(doc.getSourceAsString(), clazz);
            list.add(object);
        });
        return list;
    }

    ///
    public static List<String> aggTermQuery(String index, String fieldName, String aggName, String sortedBy, Boolean isAsc) throws IOException {
        SearchRequest request = new SearchRequest(index);
        TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms(aggName).field(fieldName).size(Integer.MAX_VALUE);
        if (Boolean.TRUE.equals(isAsc) == true) {
            aggregationBuilder.order(BucketOrder.aggregation(sortedBy, true));
        }
        if (Boolean.FALSE.equals(isAsc) == false && isAsc != null) {
            aggregationBuilder.order(BucketOrder.aggregation(sortedBy, false));
        }
        request.source().aggregation(aggregationBuilder)
                .size(0);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        Aggregations aggregations = response.getAggregations();
        Terms terms = aggregations.get(aggName);
        List<String> list = new ArrayList<>();
        List<? extends Terms.Bucket> buckets = terms.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            list.add(bucket.getKeyAsString());
        }
        return list;
    }

    public static List<String> aggTermQuery(String index, String fieldName, String aggName) throws IOException {
        return aggTermQuery(index, fieldName, aggName, null, null);
    }

    public static Map<String, List<String>> aggTermsQuery(String index, List<String> fieldNames, String aggNameSuffix) throws IOException {
        SearchRequest request = new SearchRequest(index);
        fieldNames.stream().forEach(fieldName -> {
            request.source().aggregation(AggregationBuilders.terms(fieldName + aggNameSuffix).field(fieldName).size(Integer.MAX_VALUE));
        });
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        Aggregations aggregations = response.getAggregations();
        return getAggData(aggregations, fieldNames, aggNameSuffix);
    }

    private static Map<String, List<String>> getAggData(Aggregations aggregations, List<String> fieldNames, String aggNameSuffix) {
        Map<String, List<String>> map = new HashMap<>();
        for (String fieldName : fieldNames) {
            String aggFieldName = fieldName + aggNameSuffix;
            Terms terms = aggregations.get(aggFieldName);
            List<String> list = new ArrayList<>();
            List<? extends Terms.Bucket> buckets = terms.getBuckets();
            for (Terms.Bucket bucket : buckets) {
                list.add(bucket.getKeyAsString());
            }
            map.put(fieldName, list);
        }
        return map;
    }

    private static Map<String, List<String>> getAggData(Aggregations aggregations, String suffix, String... fieldAggName) {
        Map<String, List<String>> map = new HashMap<>();
        for (String aggName : fieldAggName) {
            String fieldName = null;
            Terms terms = aggregations.get(aggName);
            ArrayList<String> list = new ArrayList<>();
            for (Terms.Bucket bucket : terms.getBuckets()) {
                list.add(bucket.getKeyAsString());
            }
            if (suffix != null) {
                fieldName = aggName.split(suffix)[0];
            }
            map.put(fieldName, list);
        }
        return map;
    }

    public static <T, V> List<T> rangeAs(String index, String fieldName, V less, V more, boolean leftIsOpen, boolean rightIsOpen, String sortedBy, Boolean isAsc, Class<T> clazz) throws IOException {
        SearchRequest request = new SearchRequest(index);
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(fieldName);
        if (leftIsOpen == true) {
            rangeQueryBuilder.gt(less);
        }
        if (leftIsOpen == false) {
            rangeQueryBuilder.gte(less);
        }
        if (rightIsOpen == true) {
            rangeQueryBuilder.lt(more);
        }
        if (rightIsOpen == true) {
            rangeQueryBuilder.lte(more);
        }
        FieldSortBuilder sortBuilder = null;
        if (sortedBy != null && !"".equals(sortedBy) && isAsc != null) {
            sortBuilder = SortBuilders.fieldSort(sortedBy);
            if (isAsc == true) {
                sortBuilder.order(SortOrder.ASC);
            } else {
                sortBuilder.order(SortOrder.DESC);
            }
        }
        request.source().query(rangeQueryBuilder)
                .sort(sortBuilder);

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        SearchHit[] hits = response.getHits().getHits();
        List<T> list = Arrays.stream(hits).map(hit ->
                {
                    T object = JSONObject.parseObject(hit.getSourceAsString(), clazz);
                    System.out.println(object.toString());
                    return object;
                }
        ).collect(Collectors.toList());
        return list;

    }

    public static <T, V> List<T> rangeAs(String index, String fieldName, V less, V more, boolean leftIsOpen, boolean rightIsOpen, Class<T> clazz) throws IOException {
        SearchRequest request = new SearchRequest(index);
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(fieldName);
        if (leftIsOpen == true) {
            rangeQueryBuilder.gt(less);
        }
        if (leftIsOpen == false) {
            rangeQueryBuilder.gte(less);
        }
        if (rightIsOpen == true) {
            rangeQueryBuilder.lt(more);
        }
        if (rightIsOpen == true) {
            rangeQueryBuilder.lte(more);
        }
        request.source().query(rangeQueryBuilder);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        SearchHit[] hits = response.getHits().getHits();
        List<T> list = Arrays.stream(hits).map(hit ->
                {
                    T object = JSONObject.parseObject(hit.getSourceAsString(), clazz);
                    System.out.println(object.toString());
                    return object;
                }
        ).collect(Collectors.toList());
        return list;

    }


}
