package com.github.dactiv.framework.mybatis.handler;

import com.github.dactiv.framework.commons.Casts;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * jackson json type handler 实现
 *
 * @param <T> json 实体类型 class
 *
 * @author maurice.chen
 */
public class JacksonJsonTypeHandler<T> extends BaseTypeHandler<T> {

    private final Class<T> type;

    public JacksonJsonTypeHandler(Class<T> type) {
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        if (Collection.class.isAssignableFrom(parameter.getClass())) {
            Collection<?> collection = Casts.cast(parameter);
            List<Object> nameValueEnums = collection
                    .stream()
                    .map(NameValueEnumTypeHandler::getEnumValue)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!nameValueEnums.isEmpty()) {
                ps.setString(i, Casts.writeValueAsString(nameValueEnums));
            } else {
                ps.setString(i, Casts.writeValueAsString(parameter));
            }
        } else {
            Object nameValueEnum = NameValueEnumTypeHandler.getEnumValue(parameter);
            if (Objects.nonNull(nameValueEnum)) {
                ps.setObject(i, nameValueEnum);
            } else {
                ps.setString(i, Casts.writeValueAsString(parameter));
            }
        }
    }



    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        final String json = rs.getString(columnName);
        return getJsonValue(json);
    }

    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        final String json = rs.getString(columnIndex);
        return getJsonValue(json);
    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        final String json = cs.getString(columnIndex);
        return getJsonValue(json);
    }

    public T getJsonValue(String json) {
        if (StringUtils.isBlank(json)) {
            return null;
        } else if (Object.class.equals(type)) {
            return Casts.cast(json);
        } else {
            return Casts.readValue(json, type);
        }
    }
}
