package com.github.dactiv.framework.commons.test;

import com.github.dactiv.framework.commons.enumerate.ValueEnumUtils;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EnumTest {

    @Test
    public void testEnum() {
        YesOrNo yesOrNo = ValueEnumUtils.parse(YesOrNo.Yes.getValue(), YesOrNo.class);
        Assertions.assertEquals(yesOrNo.getValue(), YesOrNo.Yes.getValue());

        Assertions.assertEquals(YesOrNo.Yes.getName(), ValueEnumUtils.getName(YesOrNo.Yes.getValue(), YesOrNo.class));

        Assertions.assertEquals(2, ValueEnumUtils.castMap(YesOrNo.class).size());
    }
}
