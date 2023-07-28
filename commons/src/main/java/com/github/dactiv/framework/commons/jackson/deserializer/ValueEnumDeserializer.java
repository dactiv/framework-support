package com.github.dactiv.framework.commons.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.enumerate.ValueEnum;
import com.github.dactiv.framework.commons.exception.SystemException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 值枚举的反序列化实现
 *
 * @param <T> 继承自 {@link ValueEnum} 的泛型类型
 *
 * @author maurice.chen
 */
@SuppressWarnings("rawtypes")
public class ValueEnumDeserializer<T extends ValueEnum> extends JsonDeserializer<T> {

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode jsonNode = p.getCodec().readTree(p);

        String nodeValue = NameValueEnumDeserializer.getNodeValue(jsonNode);
        Class<?> type = NameValueEnumDeserializer.getType(p);

        List<ValueEnum> valueEnums = Arrays
                .stream(type.getEnumConstants())
                .map(v -> Casts.cast(v, ValueEnum.class))
                .toList();

        Optional<ValueEnum> optional = valueEnums
                .stream()
                .filter(v -> v.getValue().toString().equals(nodeValue))
                .findFirst();

        if (optional.isEmpty()) {
            optional = valueEnums
                    .stream()
                    .filter(v -> v.toString().equals(nodeValue))
                    .findFirst();
        }

        ValueEnum result = optional
                .orElseThrow(() -> new SystemException("在类型 [" + type + "] 枚举里找不到值为 [" + nodeValue + "] 的类型"));

        return Casts.cast(result);
    }


}
