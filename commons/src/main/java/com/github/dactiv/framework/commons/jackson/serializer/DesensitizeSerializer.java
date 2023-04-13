package com.github.dactiv.framework.commons.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 敏感数据加 * 的 json 序列化实现
 *
 * @author maurice
 */
public class DesensitizeSerializer extends JsonSerializer<String> {

    /**
     * 默认倍数值
     */
    private static final int DEFAULT_MULTIPLE_VALUE = 2;
    /**
     * 默认脱敏符号
     */
    private static final String DEFAULT_DESENSITIZE_SYMBOL = "*";

    public static String desensitize(String string) {
        if (StringUtils.isEmpty(string)) {
            return string;
        }

        int length = string.length();

        if (length <= DEFAULT_MULTIPLE_VALUE) {
            return string;
        }

        double avgLength = (double) length / DEFAULT_MULTIPLE_VALUE / DEFAULT_MULTIPLE_VALUE;

        int startIndex = BigDecimal.valueOf(avgLength).setScale(0, RoundingMode.HALF_DOWN).intValue();
        int endIndex = BigDecimal.valueOf(avgLength).setScale(0, RoundingMode.HALF_UP).intValue();
        int numAsterisks = length - startIndex - endIndex;

        if (startIndex + endIndex >= DEFAULT_MULTIPLE_VALUE) {
            numAsterisks--;
        }

        String exp = "(?<=.{" + startIndex + "}).(?=.*.{" + (numAsterisks) + "}$)";
        return string.replaceAll(exp, DEFAULT_DESENSITIZE_SYMBOL);
    }

    @Override
    public void serialize(String s, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(desensitize(s));
    }
}
