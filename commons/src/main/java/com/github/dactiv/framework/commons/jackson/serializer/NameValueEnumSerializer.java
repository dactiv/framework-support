package com.github.dactiv.framework.commons.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.dactiv.framework.commons.enumerate.NameEnum;
import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import com.github.dactiv.framework.commons.enumerate.ValueEnum;

import java.io.IOException;

/**
 * 名称和值的枚举序列化实现
 *
 * @param <V> 范型类型
 */
public class NameValueEnumSerializer<V> extends JsonSerializer<NameValueEnum<V>> {

    @Override
    public void serialize(NameValueEnum<V> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        V enumValue = value.getValue();

        gen.writeStartObject();

        gen.writeStringField(NameEnum.FIELD_NAME, value.getName());
        gen.writeObjectField(ValueEnum.FIELD_NAME, enumValue);

        gen.writeEndObject();
    }
}
