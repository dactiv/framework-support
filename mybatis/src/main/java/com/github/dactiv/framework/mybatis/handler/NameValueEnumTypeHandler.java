package com.github.dactiv.framework.mybatis.handler;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.enumerate.NameEnum;
import com.github.dactiv.framework.commons.enumerate.NameEnumUtils;
import com.github.dactiv.framework.commons.enumerate.ValueEnum;
import com.github.dactiv.framework.commons.enumerate.ValueEnumUtils;
import org.apache.ibatis.type.EnumTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * 带名称或值得枚举 type handler 实现
 *
 * @param <E> 枚举类型
 *
 * @author maurice.chen
 */
public class NameValueEnumTypeHandler<E extends Enum<E>> extends EnumTypeHandler<E> {

    private final Class<E> type;

    public NameValueEnumTypeHandler(Class<E> type){
        super(type);
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E type, JdbcType jdbcType) throws SQLException {
        Object value = null;
        if (ValueEnum.class.isAssignableFrom(type.getClass())) {
            ValueEnum<?> valueEnum = Casts.cast(type);
            value = valueEnum.getValue();
        } else if (NameEnum.class.isAssignableFrom(type.getClass())) {
            NameEnum nameEnum = Casts.cast(type);
            value = nameEnum.getName();
        }

        if (Objects.isNull(value)) {
            super.setNonNullParameter(ps, i, type, jdbcType);
            return ;
        }

        if (jdbcType == null) {
            ps.setString(i, value.toString());
        } else {
            ps.setObject(i, value, jdbcType.TYPE_CODE);
        }
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Object s = rs.getObject(columnName);
        E value = getValue(s);

        if (Objects.isNull(value)) {
            value = super.getNullableResult(rs, columnName);
        }

        return value;

    }

    @Override
    public E getNullableResult(ResultSet rs, int i) throws SQLException {
        Object s = rs.getObject(i);

        E value = getValue(s);

        if (Objects.isNull(value)) {
            value = super.getNullableResult(rs, i);
        }

        return value;
    }

    @Override
    public E getNullableResult(CallableStatement cs, int i) throws SQLException {
        Object s = cs.getObject(i);

        E value = getValue(s);

        if (Objects.isNull(value)) {
            value = super.getNullableResult(cs, i);
        }

        return value;
    }

    private E getValue(Object s){

        if (Objects.isNull(s)) {
            return null;
        }

        if (ValueEnum.class.isAssignableFrom(type)) {

            Method method = Objects.requireNonNull(getValueEnumMethod(), "在接口 ValueEnum 中，找不到 " + ValueEnum.METHOD_NAME + " 方法.");
            Class<?> returnType = method.getReturnType();

            Object castValue = Casts.cast(s, returnType);
            if (Objects.nonNull(castValue)) {
                s = castValue;
            }

            return Casts.cast(ValueEnumUtils.parse(s, Casts.cast(type), true));
        } else if (NameEnum.class.isAssignableFrom(type)) {
            return Casts.cast(NameEnumUtils.parse(s.toString(), Casts.cast(type), true));
        }

        return null;
    }

    private Method getValueEnumMethod() {
        try {
            return this.type.getMethod(ValueEnum.METHOD_NAME);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
