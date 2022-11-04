package com.github.dactiv.framework.commons.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.NumberFormat;

/**
 * double 类型保留小数位序列化
 *
 * @author maurice.chen
 */
public class DoubleSerializer extends JsonSerializer<Double> {

    private final NumberFormat numberFormat;

    public DoubleSerializer(NumberFormat numberFormat) {
        this.numberFormat = numberFormat;
    }

    public DoubleSerializer() {
        this.numberFormat = NumberFormat.getInstance();
        // 最多两位小数
        this.numberFormat.setMaximumFractionDigits(2);
        // 截断
        this.numberFormat.setRoundingMode(RoundingMode.FLOOR);
    }

    @Override
    public void serialize(Double value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value != null) {
            gen.writeNumber(this.numberFormat.format(value));
        }
    }
}
