package com.github.dactiv.framework.commons.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.enumerate.NameEnum;
import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import com.github.dactiv.framework.commons.enumerate.ValueEnum;
import com.github.dactiv.framework.commons.exception.SystemException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 值于名称枚举的反序列化实现
 *
 * @param <T> 继承自 {@link NameEnum} 的泛型类型
 *
 * @author maurice.chen
 */
@SuppressWarnings("rawtypes")
public class NameValueEnumDeserializer<T extends NameValueEnum> extends JsonDeserializer<T> {

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode jsonNode = p.getCodec().readTree(p);

        String nodeValue = jsonNode.textValue();

        String currentName = p.getCurrentName();
        Object value = p.getCurrentValue();

        Class<?> type = BeanUtils.findPropertyType(currentName, value.getClass());

        List<NameValueEnum> valueEnums = Arrays
                .stream(type.getEnumConstants())
                .map(v -> Casts.cast(v, NameValueEnum.class))
                .collect(Collectors.toList());

        Optional<NameValueEnum> optional = valueEnums
                .stream()
                .filter(v -> v.getValue().toString().equals(jsonNode.textValue()))
                .findFirst();

        if (optional.isEmpty()) {
            optional = valueEnums
                    .stream()
                    .filter(v -> v.getName().equals(jsonNode.textValue()))
                    .findFirst();
        }

        if (optional.isEmpty()) {
            optional = valueEnums
                    .stream()
                    .filter(v -> v.toString().equals(jsonNode.textValue()))
                    .findFirst();
        }

        NameValueEnum result = optional
                .orElseThrow(() -> new SystemException("在类型 [" + type + "] 枚举里找不到值为 [" + nodeValue + "] 的类型"));

        return Casts.cast(result);
    }

}
