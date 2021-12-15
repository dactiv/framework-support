package com.github.dactiv.framework.commons.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import com.github.dactiv.framework.commons.enumerate.ValueEnum;
import com.github.dactiv.framework.commons.exception.SystemException;
import org.springframework.beans.BeanUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 值于名称枚举的反序列化实现
 *
 * @param <T> 继承自 {@link NameValueEnum} 的泛型类型
 *
 * @author maurice.chen
 */
@SuppressWarnings("rawtypes")
public class NameValueEnumDeserializer<T extends NameValueEnum> extends JsonDeserializer<T> {

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode jsonNode = p.getCodec().readTree(p);

        String nodeValue = getNodeValue(jsonNode);

        String currentName = p.getCurrentName();
        Object value = p.getCurrentValue();

        Class<?> type = BeanUtils.findPropertyType(currentName, value.getClass());

        List<NameValueEnum> valueEnums = Arrays
                .stream(type.getEnumConstants())
                .map(v -> Casts.cast(v, NameValueEnum.class))
                .collect(Collectors.toList());

        Optional<NameValueEnum> optional = valueEnums
                .stream()
                .filter(v -> v.toString().equals(nodeValue))
                .findFirst();

        if (optional.isEmpty()) {
            optional = valueEnums
                    .stream()
                    .filter(v -> v.getName().equals(nodeValue))
                    .findFirst();
        }

        if (optional.isEmpty()) {

            optional = valueEnums
                    .stream()
                    .filter(v -> v.getValue().toString().equals(nodeValue))
                    .findFirst();
        }

        NameValueEnum result = optional
                .orElseThrow(() -> new SystemException("在类型 [" + type + "] 枚举里找不到值为 [" + nodeValue + "] 的类型"));

        return Casts.cast(result);
    }

    /**
     * 获取 json node 值
     *
     * @param jsonNode json node
     *
     * @return 实际值
     */
    public static String getNodeValue(JsonNode jsonNode) {
        if (jsonNode.isObject()) {
            return jsonNode.get(ValueEnum.FIELD_NAME).asText();
        }

        return Objects.isNull(jsonNode.textValue()) ? jsonNode.toString() : jsonNode.textValue();
    }
}
