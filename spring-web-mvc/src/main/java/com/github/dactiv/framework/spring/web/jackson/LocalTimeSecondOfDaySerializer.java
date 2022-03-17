package com.github.dactiv.framework.spring.web.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalTime;

/**
 * 日期转时间戳序列化
 *
 * @author maurice.chen
 */
public class LocalTimeSecondOfDaySerializer extends JsonSerializer<LocalTime> {

    @Override
    public void serialize(LocalTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeNumber(value.toSecondOfDay());
    }
}
