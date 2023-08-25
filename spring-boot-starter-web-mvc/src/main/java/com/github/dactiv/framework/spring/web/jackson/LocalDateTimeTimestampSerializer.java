package com.github.dactiv.framework.spring.web.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

/**
 * 日期转时间戳序列化
 *
 * @author maurice.chen 
 */
public class LocalDateTimeTimestampSerializer extends JsonSerializer<LocalDateTime> {

    private final JacksonProperties jacksonProperties;

    public LocalDateTimeTimestampSerializer(JacksonProperties jacksonProperties) {
        this.jacksonProperties = jacksonProperties;
    }

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        ZoneId zoneId = ZoneId.systemDefault();

        if (Objects.nonNull(jacksonProperties.getTimeZone())) {
            zoneId = ZoneId.of(jacksonProperties.getTimeZone().getID());
        }

        gen.writeNumber(value.atZone(zoneId).toInstant().toEpochMilli());
    }
}
