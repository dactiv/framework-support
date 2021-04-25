package com.github.dactiv.framework.spring.web.filter.condition.support;

import com.github.dactiv.framework.commons.enumerate.NameEnumUtils;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.spring.web.filter.Property;
import com.github.dactiv.framework.spring.web.filter.condition.Condition;
import com.github.dactiv.framework.spring.web.filter.condition.ConditionParser;
import com.github.dactiv.framework.spring.web.filter.condition.ConditionType;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * 简单的条件解析器实现, 默认以 filter_ 最前缀的参数创建条件集合，具体格式为：
 *
 * filter_[字段名_通配符]_and_[字段名_通配符]_or_[字段名_通配符]
 *
 * @author maurice.chen
 */
public class SimpleConditionParser implements ConditionParser {

    /**
     * 默认条件名称前缀
     */
    public static final String DEFAULT_CONDITION_NAME_PREFIX = "filter";


    /**
     * 默认条件名称前缀
     */
    public static final String DEFAULT_FIELD_OPEN_PREFIX = "_[";

    /**
     * 默认字段结束识别符
     */
    public static final String DEFAULT_FIELD_CLOSE_SUFFIX = "]";

    /**
     * 默认字段条件分隔符
     */
    public static final String DEFAULT_FIELD_CONDITION_SEPARATORS = "_";

    /**
     * 条件名称前缀
     */
    private String conditionNamePrefix = DEFAULT_CONDITION_NAME_PREFIX;

    /**
     * 字段开始识别符
     */
    private String fieldOpenPrefix = DEFAULT_FIELD_OPEN_PREFIX;
    /**
     * 字段结束识别符
     */
    private String fieldCloseSuffix = DEFAULT_FIELD_CLOSE_SUFFIX;

    /**
     * 字段条件分隔符
     */
    private String fieldConditionSeparators = DEFAULT_FIELD_CONDITION_SEPARATORS;

    /**
     * 创建一个简单的条件解析器实现
     */
    public SimpleConditionParser() {
    }

    @Override
    public boolean isSupport(String name) {
        return StringUtils.startsWith(name, conditionNamePrefix + fieldConditionSeparators);
    }

    @Override
    public List<Condition> getCondition(String name, List<String> value) {

        String[] fieldConditionList = StringUtils.substringsBetween(
                name,
                fieldOpenPrefix,
                fieldCloseSuffix
        );

        List<Condition> result = new LinkedList<>();

        if (ArrayUtils.isEmpty(fieldConditionList)) {
            return result;
        }

        for (String fieldCondition : fieldConditionList) {

            String property = StringUtils.substringBeforeLast(fieldCondition, fieldConditionSeparators);

            Property p = new Property(property, value.size() > 1 ? value : value.iterator().next());

            String condition = StringUtils.substringAfterLast(fieldCondition, fieldConditionSeparators);

            String end = fieldOpenPrefix + fieldCondition + fieldCloseSuffix;

            ConditionType type = ConditionType.And;

            if (!StringUtils.endsWith(name, end)) {

                String s = end + fieldConditionSeparators;

                String typeValue = StringUtils.substringBetween(name, s, fieldOpenPrefix);

                type = NameEnumUtils.parse(StringUtils.capitalize(typeValue), ConditionType.class, true);

                if (Objects.isNull(type)) {
                    throw new SystemException("找不到条件类型，请检查格式是否存在问，" +
                            "标准的条件格式为 " + conditionNamePrefix + fieldOpenPrefix + "字段名" + fieldConditionSeparators + "通配符" + fieldCloseSuffix + " 如果多个条件，" +
                            "请记得添加 or 或者 and 关联下一个条件的," +
                            "如: " + conditionNamePrefix + fieldOpenPrefix + "字段名" + fieldConditionSeparators + "通配符" + fieldCloseSuffix + fieldConditionSeparators + "and" + fieldOpenPrefix + "字段名" + fieldConditionSeparators + "通配符" + fieldCloseSuffix + " = where 字段名 通配符 值 and 字段名 通配符 值 , " +
                            conditionNamePrefix + fieldOpenPrefix + "字段名" + fieldConditionSeparators + "通配符" + fieldCloseSuffix + fieldConditionSeparators + "or" + fieldOpenPrefix + "字段名" + fieldConditionSeparators + "通配符" + fieldCloseSuffix + " = where 字段名 通配符 值 and 字段名 通配符 值 , ");
                }
            }

            Condition c = new Condition(condition, type, p);

            result.add(c);
        }

        return result;
    }

    /**
     * 设置条件名称前缀
     *
     * @param conditionNamePrefix 条件名称前缀
     */
    public void setConditionNamePrefix(String conditionNamePrefix) {
        this.conditionNamePrefix = conditionNamePrefix;
    }

    /**
     * 获取条件名称前缀
     *
     * @return 条件名称前缀
     */
    public String getConditionNamePrefix() {
        return conditionNamePrefix;
    }

    /**
     * 获取字段开始识别符
     *
     * @return 字段开始识别符
     */
    public String getFieldOpenPrefix() {
        return fieldOpenPrefix;
    }

    /**
     * 设置字段开始识别符
     *
     * @param fieldOpenPrefix 字段开始识别符
     */
    public void setFieldOpenPrefix(String fieldOpenPrefix) {
        this.fieldOpenPrefix = fieldOpenPrefix;
    }

    /**
     * 获取字段结束识别符
     *
     * @return 字段结束识别符
     */
    public String getFieldCloseSuffix() {
        return fieldCloseSuffix;
    }

    /**
     * 设置字段结束识别符
     *
     * @param fieldCloseSuffix 字段结束识别符
     */
    public void setFieldCloseSuffix(String fieldCloseSuffix) {
        this.fieldCloseSuffix = fieldCloseSuffix;
    }

    /**
     * 获取字段条件分隔符
     *
     * @return 字段条件分隔符
     */
    public String getFieldConditionSeparators() {
        return fieldConditionSeparators;
    }

    /**
     * 设置字段条件分隔符
     *
     * @param fieldConditionSeparators 字段条件分隔符
     */
    public void setFieldConditionSeparators(String fieldConditionSeparators) {
        this.fieldConditionSeparators = fieldConditionSeparators;
    }
}
