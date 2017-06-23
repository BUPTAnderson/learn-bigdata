package org.learning.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class JsonUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static {
        MAPPER.setDateFormat(fmt);
    }

    private JsonUtil() {
    }

    public static <T> T deserialize(String json, Class<T> clazz) throws IOException {
        return MAPPER.readValue(json, clazz);
    }

    public static <T> T deserialize(String content, TypeReference<T> valueTypeRef) throws IOException {
        return MAPPER.readValue(content, valueTypeRef);
    }

    public static String serialize(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return "serialize object to json error : " + e.getMessage();
        }
    }

    public static String toJSON(Object obj) {
        return toJSON(obj, true);
    }

    public static String toJSON(Object obj, boolean prettyPrint) {
        try {
            if (prettyPrint) {
                ObjectWriter e = MAPPER.writerWithDefaultPrettyPrinter();
                return e.writeValueAsString(obj);
            } else {
                return MAPPER.writeValueAsString(obj);
            }
        } catch (Exception var4) {
            throw new RuntimeException(var4);
        }
    }
}
