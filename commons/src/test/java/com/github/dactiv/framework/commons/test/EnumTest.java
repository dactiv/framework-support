package com.github.dactiv.framework.commons.test;

import com.github.dactiv.framework.commons.annotation.GetValueStrategy;
import com.github.dactiv.framework.commons.annotation.IgnoreField;
import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import com.github.dactiv.framework.commons.enumerate.ValueEnumUtils;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.Stream;

public class EnumTest {

    @Test
    public void testEnum() {
        YesOrNo yesOrNo = ValueEnumUtils.parse(YesOrNo.Yes.getValue(), YesOrNo.class);
        Assertions.assertEquals(yesOrNo.getValue(), YesOrNo.Yes.getValue());

        Assertions.assertEquals(YesOrNo.Yes.getName(), ValueEnumUtils.getName(YesOrNo.Yes.getValue(), YesOrNo.class));

        Assertions.assertEquals(2, ValueEnumUtils.castMap(YesOrNo.class).size());

        Map<String, Object> value = ValueEnumUtils.castMap(EnumData.class);

        for (Map.Entry<String, Object> entry : value.entrySet()) {
            Assertions.assertTrue(Stream.of(EnumData.values()).anyMatch(e -> e.toString().equals(entry.getValue().toString())));
        }
        EnumData enumData = ValueEnumUtils.parse(EnumData.One.toString(), EnumData.class);
        Assertions.assertEquals(enumData.toString(), EnumData.One.toString());
    }

    @GetValueStrategy(type = GetValueStrategy.Type.ToString)
    public enum EnumData implements NameValueEnum<Integer> {
        One("一", 1),

        Two("二", 2),

        @IgnoreField
        Three("三", 3);
        EnumData(String name, Integer value) {
            this.name = name;
            this.value = value;
        }

        private final String name;

        private final Integer value;

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Integer getValue() {
            return value;
        }
    }
}
