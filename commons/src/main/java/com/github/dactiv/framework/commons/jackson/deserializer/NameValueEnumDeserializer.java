package com.github.dactiv.framework.commons.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import org.springframework.beans.BeanUtils;

import java.io.IOException;

public class NameValueEnumDeserializer<V> extends JsonDeserializer<NameValueEnum<V>> {

    @Override
    public NameValueEnum<V> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode jsonNode = p.getCodec().readTree(p);

        String currentName = p.getCurrentName();
        Object value = p.getCurrentValue();

        Class<?> type = BeanUtils.findPropertyType(currentName, value.getClass());

        return null;
    }

}
