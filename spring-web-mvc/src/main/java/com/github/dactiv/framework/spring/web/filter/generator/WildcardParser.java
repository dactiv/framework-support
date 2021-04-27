package com.github.dactiv.framework.spring.web.filter.generator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.spring.web.filter.Property;

/**
 * 通配符解析器
 *
 * @author maurice.chen
 */
public interface WildcardParser {

    void structure(Property property, QueryWrapper<?> queryWrapper);

    boolean isSupport(String condition);
}
