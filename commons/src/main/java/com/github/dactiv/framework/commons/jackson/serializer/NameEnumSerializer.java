package com.github.dactiv.framework.commons.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.dactiv.framework.commons.enumerate.NameEnum;
import com.github.dactiv.framework.commons.enumerate.ValueEnum;

import java.io.IOException;

/**
 * 名称枚举序列化实现
 *
 * @author maurice.chen
 */
public class NameEnumSerializer extends JsonSerializer<NameEnum> {

    @Override
    public void serialize(NameEnum value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

        gen.writeStartObject();

        gen.writeStringField(NameEnum.FIELD_NAME, value.toString());
        gen.writeObjectField(ValueEnum.FIELD_NAME, value.getName());

        gen.writeEndObject();
    }
}
