package com.github.dactiv.framework.spring.web.query.mybatis.wildcard;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.spring.web.query.Property;
import com.github.dactiv.framework.spring.web.query.generator.WildcardParser;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * json格式的包含查询通配符实现
 *
 * @author maurice.chen
 */
public class JsonContainsWildcardParser implements WildcardParser {

    private final static String DEFAULT_WILDCARD_NAME = "jin";

    @Override
    public void structure(Property property, QueryWrapper<?> queryWrapper) {
        ApplyObject applyObject = structure(property);
        if (Iterable.class.isAssignableFrom(property.getValue().getClass())) {
            queryWrapper.and(c -> c.apply(applyObject.getSql(), applyObject.getArgs().toArray()));
        } else {
            queryWrapper.apply(applyObject.getSql(), applyObject.getArgs().toArray());
        }
    }

    /**
     * 通过属性对象 构造追加对象信息
     *
     * @param property 属性信息
     *
     * @return 最佳对象信息
     */
    public static ApplyObject structure(Property property) {
        if (Iterable.class.isAssignableFrom(property.getValue().getClass())) {
            Iterable<?> iterable = (Iterable<?>) property.getValue();

            int i = 0;

            List<Object> values = new ArrayList<>();
            List<String> sql = new ArrayList<>();

            for (Object o : iterable) {
                String value = getMatchValue(o);
                values.add(value);
                sql.add("JSON_CONTAINS(" + property.getPropertyName() + ", {" + i + "})");
                i++;
            }

            String applySql = StringUtils.join(sql, " OR ");
            return new ApplyObject(applySql, values);
        } else {
            return new ApplyObject(
                    "JSON_CONTAINS(" + property.getPropertyName() + ", {0})",
                    Collections.singletonList(getMatchValue(property.getValue()))
            );
        }
    }

    /**
     * 获取匹配值
     *
     * @param value 值
     *
     * @return 可以匹配的 json 值
     */
    public static String getMatchValue(Object value) {
        String result = value.toString();
        if (String.class.isAssignableFrom(value.getClass())) {
            result = "\"" + value + "\"";
        }
        return result;
    }

    /**
     * 追加对象
     *
     * @author maurice.chen
     */
    public static class ApplyObject {
        /**
         * 要生成的执行的 sql
         */
        private final String sql;
        /**
         * 要追加的 sql 参数
         */
        private final List<Object> args;

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

    @Override
    public boolean isSupport(String condition) {
        return DEFAULT_WILDCARD_NAME.equals(condition);
    }
}
