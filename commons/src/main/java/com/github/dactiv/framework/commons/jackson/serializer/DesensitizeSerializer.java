package com.github.dactiv.framework.commons.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.dactiv.framework.commons.Casts;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 敏感数据加 * 的 json 序列化实现
 *
 * @author maurice
 */
public class DesensitizeSerializer extends JsonSerializer<Object> {

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
            return string.length() == DEFAULT_MULTIPLE_VALUE ? string.charAt(0) + DEFAULT_DESENSITIZE_SYMBOL : string ;
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
    public void serialize(Object s, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeObject(getDesensitizeValue(s));
    }

    public Object getDesensitizeValue(Object s) {
        if (s instanceof Collection<?>) {
            Collection<?> c = Casts.cast(s);
            return c.stream().map(v -> desensitize(v.toString())).collect(Collectors.toList());
        } else if (s instanceof Map<?,?>) {
            Map<?,?> map = Casts.cast(s);
            Map<?, Object> convert = Casts.cast(map);

            convert.entrySet()
                    .stream()
                    .filter(entry -> Objects.nonNull(entry.getValue()))
                    .filter(entry -> String.class.isAssignableFrom(entry.getValue().getClass()) || Collection.class.isAssignableFrom(entry.getValue().getClass()))
                    .forEach(e -> e.setValue(getDesensitizeValue(e.getValue())));

            return convert;
        } else {
            return desensitize(s.toString());
        }
    }
}
