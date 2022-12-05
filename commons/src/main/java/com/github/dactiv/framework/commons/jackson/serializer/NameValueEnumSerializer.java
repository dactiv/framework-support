package com.github.dactiv.framework.commons.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.dactiv.framework.commons.enumerate.NameEnum;
import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import com.github.dactiv.framework.commons.enumerate.ValueEnum;
import com.github.dactiv.framework.commons.enumerate.ValueEnumUtils;

import java.io.IOException;

/**
 * 名称和值的枚举序列化实现
 *
 * @author maurice.chen
 */
@SuppressWarnings("rawtypes")
public class NameValueEnumSerializer extends JsonSerializer<NameValueEnum> {

    @Override
    public void serialize(NameValueEnum value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        Object enumValue = ValueEnumUtils.getValueByStrategyAnnotation(value);

        gen.writeStartObject();

        gen.writeStringField(NameEnum.FIELD_NAME, value.getName());
        gen.writeObjectField(ValueEnum.FIELD_NAME, enumValue);

        gen.writeEndObject();
    }
}
