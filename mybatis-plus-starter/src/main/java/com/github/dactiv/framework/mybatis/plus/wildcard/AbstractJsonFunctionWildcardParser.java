package com.github.dactiv.framework.mybatis.plus.wildcard;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.spring.web.query.Property;
import com.github.dactiv.framework.spring.web.query.generator.WildcardParser;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * 首相的 json 功能通配符解析器实现
 *
 * @author maurice.chen
 * @param <T>
 */
public abstract class AbstractJsonFunctionWildcardParser<T> implements WildcardParser<QueryWrapper<T>> {

    @Override
    public void structure(Property property, QueryWrapper<T> queryWrapper) {
        ApplyObject applyObject = structureApplyObject(property.getValue(), index -> getExpression(property.getPropertyName(), index));
        if (Iterable.class.isAssignableFrom(property.getValue().getClass())) {
            queryWrapper.and(c -> c.apply(applyObject.getSql(), applyObject.getArgs().toArray()));
        } else {
            queryWrapper.apply(applyObject.getSql(), applyObject.getArgs().iterator().next());
        }
    }

    /**
     * 获取表达式
     *
     * @param propertyName 属性名称
     * @param index   所引值
     * @return json 函数表达式 的 sql 语句
     */
    protected abstract String getExpression(String propertyName, Integer index);

    protected ApplyObject structureApplyObject(Object value, Function<Integer, String> expression) {
        if (value instanceof Iterable<?>) {
            Iterable<?> iterable = Casts.cast(value);
            int i = 0;

            List<Object> values = new ArrayList<>();
            List<String> sql = new ArrayList<>();

            for (Object o : iterable) {
                sql.add(expression.apply(i));
                values.add(o);
                i++;
            }

            String applySql = StringUtils.join(sql, " OR ");
            return new ApplyObject(applySql, values);
        } else {
            return new ApplyObject(
                    expression.apply(0),
                    Collections.singletonList(value)
            );
        }
    }

    /**
     * 追加对象
     *
     * @author maurice.chen
     */
    public static class ApplyObject implements Serializable {

        
        private static final long serialVersionUID = -4857566069778364200L;

        /**
         * 要生成的执行的 sql
         */
        private String sql;
        /**
         * 要追加的 sql 参数
         */
        private List<Object> args;

        public ApplyObject() {
        }

        /**
         * 创建一个新的追加对象
         *
         * @param sql  要生成的执行的 sql
         * @param args 要追加的 sql 参数
         */
        public ApplyObject(String sql, List<Object> args) {
            this.sql = sql;
            this.args = args;
        }

        /**
         * 获取要生成的执行的 sql
         *
         * @return sql
         */
        public String getSql() {
            return sql;
        }

        /**
         * 获取要追加的 sql 参数
         *
         * @return 参数集合
         */
        public List<Object> getArgs() {
            return args;
        }
    }
}
