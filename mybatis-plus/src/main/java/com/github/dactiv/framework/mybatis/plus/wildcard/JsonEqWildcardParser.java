package com.github.dactiv.framework.mybatis.plus.wildcard;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.spring.web.query.Property;
import com.github.dactiv.framework.spring.web.query.generator.WildcardParser;
import org.apache.commons.lang3.StringUtils;

/**
 * json 格式的等于查询通配符实现
 *
 * @author maurice.chen
 */
public class JsonEqWildcardParser implements WildcardParser<QueryWrapper<?>> {

    private final static String DEFAULT_WILDCARD_NAME = "jeq";

    @Override
    public void structure(Property property, QueryWrapper<?> query) {
        String propertyName = StringUtils.substringBefore(property.getPropertyName(), Casts.DEFAULT_DOT_SYMBOL);
        String path = StringUtils.substringAfter(property.getPropertyName(), Casts.DEFAULT_DOT_SYMBOL);
        query.apply(propertyName + "->'$." + path + "' = {0}", property.getValue());
    }

    @Override
    public boolean isSupport(String condition) {
        return DEFAULT_WILDCARD_NAME.equals(condition);
    }
}
