package com.seattle.msready.mq.transaction.compensating.mq.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class JsonBaseUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonBaseUtils.class);
    private static ObjectMapper objectMapper;
    private static JsonBaseUtils adaptedJsonUtils;
    private static String SPACE = "    ";

    private static AtomicBoolean isDomainApp;
    private static final String CLASS_FULL_NAME = "com.ebao.unicorn.platform.foundation.utils.json.JSONUtils";

    private static Class CLASS_JSON_UTILS;


    // private static FastDateFormat
    // dateFormat=DateFormatUtils.ISO_DATETIME_FORMAT;
    static {
        initMapper();
    }

    private static void initMapper() {
        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setDateFormat(new ISO8601WithoutTimeZoneDateFormat());
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
        objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        objectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        objectMapper.configure(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS, false);
        objectMapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
        objectMapper.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.disable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
        objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategy.PascalCaseStrategy());
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        objectMapper.registerModules(new JavaTimeModule());
    }


    /**
     * convert object to json, the empty Map/List will be convert to null
     *
     * @param object
     * @return
     */
    public static String toJSON(Object object) {

        if (isDomainApp()) {
            try {
                return (String) getJsonUtilsClass().getMethod("toJSON", Object.class).invoke(null, object);
            } catch (Exception e) {
                LOGGER.error("toJSON error:{}", e);
                throw new RuntimeException(e);
            }
        }

        return toJSON(object, true, false);
    }

    public static Class getJsonUtilsClass() {
        if (CLASS_JSON_UTILS != null) {
            return CLASS_JSON_UTILS;
        } else {
            try {
                CLASS_JSON_UTILS = Class.forName(CLASS_FULL_NAME);
            } catch (ClassNotFoundException e) {
                LOGGER.error("getJsonUtilsClass error:{}", e);
                throw new RuntimeException(e);
            }
            return CLASS_JSON_UTILS;
        }
    }

    public static String toJSON(Object object, boolean trimEmptyJson, boolean withNullValue) {
        try {
            if (object == null) {
                return null;
            }

            String jsonString;
            jsonString = objectMapper.writeValueAsString(object);
            if (trimEmptyJson && ("[]".equals(jsonString) || "{}".equals(jsonString))) {
                jsonString = null;
            }
            return jsonString;
        } catch (Exception e) {
            LOGGER.error("toJSON error:{}", e);
            throw new RuntimeException(e);
        }
    }


    /**
     * parse json string as jackson JsonNode
     *
     * @param json
     * @return
     */
    public static JsonNode parseJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            LOGGER.error("parseJson error:{}", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert object from json.<BR>
     * If target class is DomainModel or json contains "@type",then convert all
     * untyped child object to DomainModel.Don't use mixed DomainModel & nested
     * map.<BR>
     *
     * @param json
     * @param clazz
     * @return
     */
    public static <T> T fromJSON(String json, Class<T> clazz) {
        try {
            if (json == null) {
                return null;
            }

            Object object = objectMapper.readValue(json, clazz);
            return (T) object;
        } catch (Exception e) {
            LOGGER.error(" fromJSON error:{}", e);
            throw new RuntimeException(e);
        }
    }

    private static boolean isDomainApp() {
        if (isDomainApp != null) {
            return isDomainApp.get();
        } else {
            try {
                Class.forName("com.ebao.unicorn.platform.data.domain.DomainModel");
            } catch (ClassNotFoundException e) {
                LOGGER.info("There is no domain model in the JVM.");
                isDomainApp = new AtomicBoolean(false);
                return isDomainApp.get();
            }
            isDomainApp = new AtomicBoolean(true);
            return isDomainApp.get();
        }
    }

    /**
     * If target class is DomainModel or json contains "@type",then convert all
     * untyped child object to DomainModel.Don't use mixed DomainModel & nested
     * map.<BR>
     *
     * @param json
     * @param type
     * @return
     */
    public static Object fromJSON(String json, ParameterizedType type) {
         int size = type.getActualTypeArguments().length;
        if (size != 1) {
            throw new RuntimeException(
                    "the type[" + type.getTypeName() + "] is not supported, only suppprt list/set now");
        }

        JavaType javaType = objectMapper.getTypeFactory().constructCollectionType((Class<Collection>) type.getRawType(),
                (Class) type.getActualTypeArguments()[0]);
        try {
            Collection<?> list = (Collection<?>) objectMapper.readValue(json, javaType);
            return list;
        } catch (Exception ex) {
            LOGGER.error(" fromJSON error:{}", ex);
            throw new RuntimeException(ex);
        }

    }

    /**
     * convert object from json<BR>
     * If target class is DomainModel or json contains "@type",then convert all
     * untyped child object to DomainModel.Don't use mixed DomainModel & nested
     * map.<BR>
     *
     * @param json
     * @param elementClazz
     * @return
     */
    public static <T> List<T> fromJSONAsList(String json, Class<T> elementClazz) {
        return fromJSONAsList(json, List.class, elementClazz);
    }

    /**
     * convert object from json<BR>
     * If target class is DomainModel or json contains "@type",then convert all
     * untyped child object to DomainModel.Don't use mixed DomainModel & nested
     * map.<BR>
     *
     * @param json
     * @param elementClazz
     * @return
     */
    public static <T> List<T> fromJSONAsList(String json, Class<? extends List> listClazz, Class<T> elementClazz) {
        if (json == null) {
            return null;
        }
        try {
            JavaType type = objectMapper.getTypeFactory().constructCollectionType(listClazz, elementClazz);
            List<T> list = (List<T>) objectMapper.readValue(json, type);
            return list;
        } catch (Exception e) {
            LOGGER.error(" fromJSONAsList error:{}", e);
            throw new RuntimeException(e);
        }
    }

    public static <T> Set<T> fromJSONAsSet(String json, Class<T> elementClazz) {
        return fromJSONAsSet(json, Set.class, elementClazz);
    }

    public static <T> Set<T> fromJSONAsSet(String json, Class<? extends Set> setClazz, Class<T> elementClazz) {
        if (json == null) {
            return null;
        }
        try {
            JavaType type = objectMapper.getTypeFactory().constructCollectionType(setClazz, elementClazz);
            Set<T> set = (Set<T>) objectMapper.readValue(json, type);
            return set;
        } catch (Exception e) {
            LOGGER.error("fromJSONAsSet error:{}", e);
            throw new RuntimeException(e);
        }
    }

    public static <K, V> Map<K, V> fromJSONAsMap(String json, Class<K> keyClazz, Class<V> valueClazz, JavaType originalJavaType) {
        return fromJSONAsMap(json, Map.class, keyClazz, valueClazz, originalJavaType);
    }


    /**
     * from json as map<BR>
     * If target class is DomainModel or json contains "@type",then convert all
     * untyped child object to DomainModel.Don't use mixed DomainModel & nested
     * map.<BR>
     *
     * @param json
     * @param mapClazz
     * @param keyClazz
     * @param valueClazz
     * @return
     */
    public static <K, V> Map<K, V> fromJSONAsMap(String json, Class mapClazz, Class<K> keyClazz,
                                                 Class<V> valueClazz, JavaType originalType) {
        ObjectMapper readerMapper;
        if (json == null) {
            return null;
        }
        try {
            JavaType valueType;
            JavaType keyType = objectMapper.getTypeFactory().constructType(keyClazz);
            if (List.class.isAssignableFrom(valueClazz)) {
                Class elementClazz = originalType.getContentType().getContentType().getRawClass();
                JavaType elementType = objectMapper.getTypeFactory().constructType(elementClazz);
                valueType = CollectionType.construct(valueClazz, elementType);
            } else {
                valueType = objectMapper.getTypeFactory().constructType(valueClazz);
            }
            JavaType type = MapType.construct(mapClazz, keyType, valueType);
            Map<K, V> map = objectMapper.readValue(json, type);
            return map;
        } catch (Exception e) {
            LOGGER.error("fromJSONAsMap error:{}", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * format json string<BR>
     *
     * @param json
     * @return
     */
    public static String JsonStringFormat(String json) {
        StringBuffer result = new StringBuffer();
        Integer length = json.length();
        int number = 0;
        char word = 0;

        for (int i = 0; i < length; i++) {
            word = json.charAt(i);

            if ((word == '[') || (word == '{')) {
                if ((i - 1 > 0) && (json.charAt(i - 1) == ':')) {
                    result.append('\n');
                    result.append(indent(number));
                }
                result.append(word);
                result.append('\n');
                number++;
                result.append(indent(number));

                continue;
            }
            if ((word == ']') || (word == '}')) {

                result.append('\n');
                number--;
                result.append(indent(number));
                result.append(word);

                if (((i + 1) < length) && (json.charAt(i + 1) != ',')) {
                    result.append('\n');
                }

                continue;
            }

            if ((word == ',') && json.charAt(i - 1) == '"') {
                result.append(word);
                result.append('\n');
                result.append(indent(number));
                continue;
            }

            result.append(word);
        }

        return result.toString();
    }

    private static String indent(int number) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < number; i++) {
            result.append(SPACE);
        }
        return result.toString();
    }

    /**
     * from json as map<BR>
     * If target class is DomainModel or json contains "@type",then convert all
     * untyped child object to DomainModel.Don't use mixed DomainModel & nested
     * map.<BR>
     *
     * @param is
     * @param clazz
     * @return
     */
    public static <T> T fromJSON(InputStream is, Class<T> clazz, Charset... charset) {

        if (isDomainApp()) {
            try {
                return (T) getJsonUtilsClass().getMethod("fromJSON", InputStream.class, Class.class, Charset[].class).invoke(null, is, clazz);
            } catch (Exception e) {
                LOGGER.error("Domain Object fromJSON error:{}", e);
                throw new RuntimeException(e);
            }
        }


        String json;
        try {
            StringBuilder sb = new StringBuilder();
            for (int ch; (ch = is.read()) != -1; ) {
                sb.append((char) ch);
            }
            json = sb.toString();
        } catch (IOException e) {
            LOGGER.error("  fromJSON error:{}", e);
            throw new RuntimeException(e);
        }
        return fromJSON(json, clazz);
    }

    public static Date parseDate(String str) {
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        try {
            return ISO8601WithoutTimeZoneUtils.parse(str, new ParsePosition(0));
        } catch (ParseException e) {
            LOGGER.error("  parseDate error:{}", e);
            throw new RuntimeException("Invalid json date format,value:" + str, e);
        }
    }

    public static String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        return ISO8601WithoutTimeZoneUtils.format(date);
    }

    public static String formatDateIncludeZero(Date date) {
        if (date == null) {
            return null;
        }
        return ISO8601WithoutTimeZoneUtils.formatIncludeZero(date);
    }
}
