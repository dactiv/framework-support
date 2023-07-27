package com.github.dactiv.framework.mybatis.plus.wildcard;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.spring.web.query.Property;
import com.github.dactiv.framework.spring.web.query.generator.WildcardParser;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * 模糊查询通配符实现
 *
 * @author maurice.chen
 */
public class LikeWildcardParser<T> implements WildcardParser<QueryWrapper<T>> {

    private final static String DEFAULT_WILDCARD_NAME = "like";

    public static final String MATCH_SYMBOL = "%";

    public static void addMatchSymbol(Property property) {
        if (property.getValue() instanceof List<?>) {
            List<?> list = Casts.cast(property.getValue());
            List<String> newValue = new LinkedList<>();
            for (Object value : list) {
                String stringValue = value.toString();
                if (StringUtils.startsWith(stringValue, MATCH_SYMBOL) || StringUtils.endsWith(stringValue, MATCH_SYMBOL)) {
                    newValue.add(stringValue);
                } else {
                    newValue.add(MATCH_SYMBOL + stringValue + MATCH_SYMBOL);
                }
            }
            property.setValue(newValue);
        } else {
            String stringValue = property.getValue().toString();
            if (!StringUtils.startsWith(stringValue, "%") && !StringUtils.endsWith(stringValue, "%")) {
                property.setValue("%" + stringValue + "%");
            }
        }
    }

    @Override
    public void structure(Property property, QueryWrapper<T> queryWrapper) {
        queryWrapper.like(property.getPropertyName(), property.getValue());
    }

    @Override
    public boolean isSupport(String condition) {
        return DEFAULT_WILDCARD_NAME.equals(condition);
    }
}
