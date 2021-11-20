package com.github.dactiv.framework.mybatis.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.dactiv.framework.commons.Casts;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.util.List;

/**
 * jackson List 引用的 TypeHandler 实现
 *
 * @param <T>
 *
 * @author maurice.chen
 */
@MappedTypes({Object.class})
@MappedJdbcTypes(JdbcType.VARCHAR)
public class JacksonListReferenceTypeHandler<T> extends AbstractJsonTypeHandler<List<T>> {

    @Override
    protected List<T> read(String json) {
        return Casts.readValue(json, new TypeReference<>() {});
    }

    @Override
    protected String write(List<T> obj) {
        return Casts.writeValueAsString(obj);
    }
}
