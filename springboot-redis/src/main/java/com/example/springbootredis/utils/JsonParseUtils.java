package com.example.springbootredis.utils;


import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author jlp
 * @date 2024/1/16
 * description：json解析工具类
 */
public class JsonParseUtils<T> {

    //用于存放Json对象
    static List<JsonNode> objectList = new ArrayList<>();
    //用于存放Json数组
    static List<JsonNode> arrayList = new ArrayList<>();

    public static List<JsonNode> list() {
        return objectList;
    }

    public static List<JsonNode> array() {
        return arrayList;
    }

    public static String fileToString(String url) throws IOException {
        File file = new File(url);
        StringBuilder builder = new StringBuilder();

        BufferedReader br = new BufferedReader(new FileReader(file));
        String str;
        if ((str = br.readLine()) != null) {
            builder.append(str);
        }
        br.close();
        return builder.toString();
    }

    public static void parser(File file) throws Exception{
        String str = fileToString(file.getAbsolutePath());
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(str);check(jsonNode);



    }
    public static void parser(String str) throws IOException {
        isJsonString(str);
        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createParser(str);
        while (!parser.isClosed()) {
            JsonToken jsonToken = parser.nextToken();
            System.out.println(jsonToken);
        }
    }

//    public static void parserUrl(String url) throws IOException {
//        ObjectMapper objectMapper = new ObjectMapper();
//        String str = toString(url);
//        isJsonString(str);
//        JsonFactory factory = new JsonFactory();
//        JsonParser parser = factory.createParser(str);
//        while (!parser.isClosed()) {
//            JsonToken jsonToken = parser.nextToken();
//            if (JsonToken.FIELD_NAME.equals(jsonToken)) {
//                String fieldName = parser.getCurrentName();
//
//                jsonToken = parser.nextToken();
//                Object value = parser.getCurrentValue();
//                String valueString = parser.getValueAsString();
//
//            }
//        }
//    }

    public static void parserFile(File file) throws IOException {

        JsonFactory factory = new JsonFactory();
        JsonParser parser = null;
        try {
            parser = factory.createParser(file);
        } catch (JsonParseException e) {
            throw new RuntimeException("json格式解析错误");
        }
        Boolean isFirstField = true;
        String[] strArr = new String[1];
        while (!parser.isClosed()) {
            JsonToken jsonToken = parser.nextToken();
            if (JsonToken.FIELD_NAME.equals(jsonToken)) {
                if (!isFirstField) {
                    if (!"{".equals(strArr[0])) {
                        System.out.print(", ");
                    } else {
                        strArr[0] = "";
                    }
                }
                isFirstField = false;
                String fieldName = parser.getCurrentName();
                System.out.print("\"" + fieldName + "\":");
            } else if (JsonToken.START_OBJECT.equals(jsonToken)) {
                System.out.println();
                System.out.print("{ ");
                strArr[0] = "{";
            } else if (JsonToken.END_OBJECT.equals(jsonToken)) {
                System.out.print("} ");
                System.out.println();
            } else if (JsonToken.START_ARRAY.equals(jsonToken)) {
                System.out.println();
                System.out.print("[ ");
            } else if (JsonToken.END_ARRAY.equals(jsonToken)) {
                System.out.print("] ");
                System.out.println();
            } else if (JsonToken.VALUE_STRING.equals(jsonToken)) {
                String value = parser.getText();
                System.out.print("\"" + value + "\"");
            } else if (JsonToken.VALUE_NUMBER_INT.equals(jsonToken)) {
                int value = parser.getIntValue();
                System.out.print(value);
            } else if (JsonToken.VALUE_TRUE.equals(jsonToken)) {
                System.out.print("true ");
            } else if (JsonToken.VALUE_FALSE.equals(jsonToken)) {
                System.out.print("false ");
            } else if (JsonToken.VALUE_NULL.equals(jsonToken)) {
                System.out.print("null ");
            }
        }
    }

    public static void parseUrl(String url) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String str = fileToString(url);
        isJsonString(str);
        JsonNode node = objectMapper.readTree(str);
        checkObject(node);
    }


    public static void parseString(String str) throws IOException {
        isJsonString(str);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.readTree(str);
        if (node.isObject()) {
            checkObject(node);
        } else if (node.isArray()) {
            checkArray(node);
        }

    }


//    public static JSONType getType(String str) throws JsonProcessingException {
//        ObjectMapper objectMapper = new ObjectMapper();
//        JsonNode jsonNode = objectMapper.readTree(str);
//
//        }
//    }


    private static void check(JsonNode jsonNode) {
        if (jsonNode.isObject()) {
            objectList.add(jsonNode);
            Iterator<String> fieldNames = jsonNode.fieldNames();
            while (fieldNames.hasNext() && fieldNames != null) {
                String fieldName = fieldNames.next();
                JsonNode node = jsonNode.get(fieldName);
                if (node.isObject()) {
                    objectList.add(node);
                    checkObject(node);
                } else if (node.isArray()) {
                    arrayList.add(node);
                    checkObject(node);
                } else {

                }
            }
        } else if (jsonNode.isArray()) {
            arrayList.add(jsonNode);
            for (JsonNode node : jsonNode) {
                if (node.isObject()) {
                    objectList.add(node);
                    checkObject(node);
                } else if (node.isArray()) {
                    arrayList.add(node);
                    checkObject(node);
                } else {

                }
            }
        } else {

        }
    }

    private static void checkObject(JsonNode jsonNode) {
        if (jsonNode.isObject()) {
            Iterator<String> fieldNames = jsonNode.fieldNames();
            while (fieldNames.hasNext() && fieldNames != null) {
                String fieldName = fieldNames.next();
                JsonNode node = jsonNode.get(fieldName);
                if (node.isObject()) {
                    objectList.add(node);
                    checkObject(node);
                } else if (node.isArray()) {
                    checkObject(node);
                } else {

                }
            }
        } else if (jsonNode.isArray()) {
            for (JsonNode node : jsonNode) {
                if (node.isObject()) {
                    objectList.add(node);
                    checkObject(node);
                } else if (node.isArray()) {
                    checkObject(node);
                } else {

                }
            }
        } else {

        }
    }

    private static void checkArray(JsonNode jsonNode) {

        if (jsonNode.isArray()) {
            for (JsonNode node : jsonNode) {
                if (node.isArray()) {
                    arrayList.add(node);
                } else if (node.isObject()) {
                    checkArray(node);
                } else {
                }
            }
        } else if (jsonNode.isObject()) {
            Iterator<String> fieldNames = jsonNode.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode node1 = jsonNode.get(fieldName);
                if (node1.isArray()) {
                    arrayList.add(node1);
                    checkArray(node1);
                } else if (node1.isObject()) {
                    checkArray(node1);
                } else {

                }
            }
        } else {

        }
    }

    /**
     * 判断类型
     *
     * @return
     */
    public static JsonNodeType getNodeType(String str) throws JsonProcessingException {
        isJsonString(str);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(str);
        return jsonNode.getNodeType();
    }

//    public static void getType(String str) throws IOException {
//        isJsonString(str);
//        JsonFactory factory = new JsonFactory();
//        JsonParser parser  = factory.createParser(str);
//        parser.T
//
//    }

    public static Boolean isJsonString(String str) {
        if (str == null || str.isBlank()) {
            throw new RuntimeException("Json数据不能为null或空字符串");
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.readTree(str);
            return true;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Json数据格式错误");
        }

    }

    public static Boolean isObject(String str) throws JsonProcessingException {
        isJsonString(str);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(str);
        if (jsonNode.isObject()) {
            return true;
        }
        return false;
    }

    public static Boolean isArray(String str) throws JsonProcessingException {
        isJsonString(str);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(str);
        if (jsonNode.isArray()) {
            return true;
        }
        return false;
    }

    /**
     * 获取所有JsonObject
     */
    public static List<JsonNode> getObjects(String str) throws JsonProcessingException {
        isJsonString(str);
        ObjectMapper objectMapper = new ObjectMapper();
        objectList.clear();
        JsonNode jsonNode = objectMapper.readTree(str);
        if (jsonNode.isObject()) {
            objectList.add(jsonNode);
        }
        checkObject(jsonNode);
        return objectList;
    }

    /**
     * 获取所有JsonArray
     */
    public static List<JsonNode> getArrays(String str) throws JsonProcessingException {
        isJsonString(str);
        ObjectMapper objectMapper = new ObjectMapper();
        arrayList.clear();
        JsonNode jsonNode = objectMapper.readTree(str);
        if (jsonNode.isArray()) {
            arrayList.add(jsonNode);
        }
        checkArray(jsonNode);
        return arrayList;
    }

    public static List<Object> getValue(String fieldName, String jsonString) throws IOException {
        isJsonString(jsonString);
        ArrayList<Object> list = new ArrayList<>();
        List<JsonNode> objects = getObjects(jsonString);
        for (JsonNode object : objects) {
            JsonNode node = object.findValue(fieldName);
//            Iterator<String> fieldNames = object.fieldNames();
//            while (fieldNames.hasNext()){
//                JsonNode jsonNode = object.get(fieldName);
//                list.add(jsonNode);
//            }
            list.add(node);

        }

        return list;
    }

    /**
     * 通过属性名获取json字符串的对象
     *
     * @param fieldName
     * @param jsonString
     */
//    public static <T> List<Object> getValue(String fieldName,T value, String jsonString) throws IOException {
//        isJsonString(jsonString);
//        JsonNodeType type = getNodeType(jsonString);
//        ArrayList<Object> list = new ArrayList<>();
//        List<JsonNode> objects = getObjects(jsonString);
//
//
//        return list;
//    }


}