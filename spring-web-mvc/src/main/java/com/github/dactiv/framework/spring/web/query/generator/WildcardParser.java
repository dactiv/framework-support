package com.github.dactiv.framework.spring.web.query.generator;

import com.github.dactiv.framework.spring.web.query.Property;

/**
 * 通配符解析器
 *
 * @author maurice.chen
 */
public interface WildcardParser<Q> {

    void structure(Property property, Q query);

    boolean isSupport(String condition);
}
