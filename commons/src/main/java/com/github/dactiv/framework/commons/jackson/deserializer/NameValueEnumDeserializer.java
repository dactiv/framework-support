package com.github.dactiv.framework.commons.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.annotation.JsonCollectionGenericType;
import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import com.github.dactiv.framework.commons.enumerate.ValueEnum;
import com.github.dactiv.framework.commons.exception.SystemException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

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
        Class<?> type = getType(p);

        List<NameValueEnum> valueEnums = Arrays
                .stream(type.getEnumConstants())
                .map(v -> Casts.cast(v, NameValueEnum.class))
                .toList();

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

    public static String getCurrentName(JsonParser p) throws IOException {
        String result = p.getCurrentName();

        if (StringUtils.isEmpty(result)) {
            result = p.getParsingContext().getParent().getCurrentName();
        }

        return result;
    }

    public static Object getCurrentValue(JsonParser p) {
        Object result = p.getCurrentValue();

        if (Collection.class.isAssignableFrom(result.getClass())) {
            result = p.getParsingContext().getParent().getCurrentValue();
        }

        return result;
    }

    public static Class<?> getType(JsonParser p) throws IOException {
        String currentName = getCurrentName(p);
        Object value = getCurrentValue(p);

        Class<?> type = BeanUtils.findPropertyType(currentName, value.getClass());

        if (Collection.class.isAssignableFrom(type)) {
            Field field = Objects.requireNonNull(
                    ReflectionUtils.findField(value.getClass(), currentName),
                    "在类 [" + value.getClass() + "] 中找不到 [" + currentName + "] 字段"
            );

            JsonCollectionGenericType genericType = field.getAnnotation(JsonCollectionGenericType.class);
            if (Objects.nonNull(genericType)) {
                return genericType.value();
            }
        }

        return type;
    }
}
