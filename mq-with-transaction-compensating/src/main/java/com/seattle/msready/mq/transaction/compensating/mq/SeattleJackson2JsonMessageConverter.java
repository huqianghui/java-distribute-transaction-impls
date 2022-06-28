package com.seattle.msready.mq.transaction.compensating.mq;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seattle.msready.mq.transaction.compensating.mq.utils.JsonBaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class SeattleJackson2JsonMessageConverter extends Jackson2JsonMessageConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeattleJackson2JsonMessageConverter.class);
    private JsonBaseUtils jsonUtils;

    public SeattleJackson2JsonMessageConverter() {
        this.jsonUtils = jsonUtils;
    }

    public ObjectMapper getJsonObjectMapper() {
        return jsonObjectMapper;
    }

    public void setJsonObjectMapper(ObjectMapper jsonObjectMapper) {
        this.jsonObjectMapper = jsonObjectMapper;
    }

    private ObjectMapper jsonObjectMapper = new ObjectMapper();

    @Override
    protected Message createMessage(Object objectToConvert, MessageProperties messageProperties)
            throws MessageConversionException {
        LOGGER.debug("createMessage...");
        LOGGER.debug("objectToConvert:{}", objectToConvert);
        LOGGER.debug("objectToConvert.class:{}", objectToConvert.getClass());
        LOGGER.debug("messageProperties:{}", messageProperties);
        byte[] bytes;
        try {
            String jsonString = jsonUtils.toJSON(objectToConvert);
            bytes = jsonString.getBytes(getDefaultCharset());
        } catch (IOException e) {
            LOGGER.error("createMessage error:{}", e);
            throw new MessageConversionException(
                    "Failed to convert Message content", e);
        }
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        messageProperties.setContentEncoding(getDefaultCharset());
        messageProperties.setContentLength(bytes.length);

        LOGGER.debug("ClassMapper:{}", getClassMapper());

        if (getClassMapper() == null) {
            getJavaTypeMapper().fromJavaType(this.getJsonObjectMapper().constructType(objectToConvert.getClass()),
                    messageProperties);

        } else {
            getClassMapper().fromClass(objectToConvert.getClass(),
                    messageProperties);

        }
        LOGGER.debug("messageProperties:{}", messageProperties);
        return new Message(bytes, messageProperties);
    }

    @Override
    public Object fromMessage(Message message)
            throws MessageConversionException {
        LOGGER.debug("fromMessage...");
        LOGGER.debug("message:{}", message);
        Object content = null;
        MessageProperties properties = message.getMessageProperties();
        LOGGER.debug("properties:{}", properties);
        if (properties != null) {
            String contentType = properties.getContentType();
            if (contentType != null && contentType.contains("json")) {
                String charset = properties.getContentEncoding();
                if (charset == null) {
                    charset = getDefaultCharset();
                }
                LOGGER.debug("charset:{}", charset);
                String contentJson = null;
                try {
                    contentJson = new String(message.getBody(), charset);
                    LOGGER.debug("contentJson:{}", contentJson);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    LOGGER.debug("classMapper:{}", getClassMapper());
                    if (getClassMapper() == null) {
                        JavaType javaType = getJavaTypeMapper()
                                .toJavaType(message.getMessageProperties());
                        LOGGER.debug("javaType:{}", javaType);
                        if (javaType.getRawClass() == List.class) {
                            return jsonUtils.fromJSONAsList(contentJson,
                                    javaType.getContentType().getRawClass());
                        } else if (javaType.getRawClass() == Set.class) {
                            return jsonUtils.fromJSONAsSet(contentJson,
                                    javaType.getContentType().getRawClass());
                        } else if (javaType.getRawClass() == Map.class) {
                            {
                                return jsonUtils.fromJSONAsMap(contentJson,
                                        javaType.getRawClass(),
                                        javaType.getKeyType().getRawClass(),
                                        javaType.getContentType().getRawClass(), javaType);
                            }
                        } else {
                            return jsonUtils.fromJSON(contentJson, javaType.getRawClass());
                        }
                    } else {
                        Class<?> targetClass = getClassMapper().toClass(
                                message.getMessageProperties());

                        LOGGER.debug("targetClass:{}", targetClass);
                        content = convertBytesToObject(message.getBody(),
                                charset, targetClass);

                        LOGGER.debug("content:{}", content);
                    }
                } catch (IOException e) {
                    LOGGER.error("fromMessage error:{}", e);
                    throw new MessageConversionException(
                            "Failed to convert Message content", e);
                }
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Could not convert incoming message with content-type ["
                            + contentType + "]");
                }
            }
        }

        LOGGER.debug("content2:{}", content);
        if (content == null) {
            content = message.getBody();
        }
        LOGGER.debug("content3:{}", content);
        return content;
    }

    private Object convertBytesToObject(byte[] body, String encoding, Class<?> targetClass) throws IOException {
        String contentAsString = new String(body, encoding);
        return this.jsonObjectMapper.readValue(contentAsString, this.jsonObjectMapper.constructType(targetClass));
    }
}
