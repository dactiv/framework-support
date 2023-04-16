package com.github.dactiv.framework.spring.web.query.condition.support;

import com.github.dactiv.framework.commons.enumerate.NameEnumUtils;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.spring.web.query.Property;
import com.github.dactiv.framework.spring.web.query.condition.Condition;
import com.github.dactiv.framework.spring.web.query.condition.ConditionParser;
import com.github.dactiv.framework.spring.web.query.condition.ConditionType;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * 简单的条件解析器实现, 默认以 filter_ 最前缀的参数创建条件集合，具体格式为：
 * <p>
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
     * 默认数组符号
     */
    public static final String DEFAULT_ARRAY_SYMBOL = "[]";

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
     *
     * @deprecated 使用 {@link com.github.dactiv.framework.commons.Casts#UNDERSCORE}
     */
    @Deprecated
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
     * 默认的数组符号
     */
    private String arraySymbol = DEFAULT_ARRAY_SYMBOL;

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
    public List<Condition> getCondition(String filter, List<Object> value) {
        String name = StringUtils.removeEnd(filter, arraySymbol);
        String[] fieldConditionList = StringUtils.substringsBetween(name, fieldOpenPrefix, fieldCloseSuffix);

        List<Condition> result = new LinkedList<>();

        if (ArrayUtils.isEmpty(fieldConditionList)) {
            return result;
        }

        for (String fieldCondition : fieldConditionList) {

            String propertyName = StringUtils.substringBeforeLast(fieldCondition, fieldConditionSeparators);
            Object propertyValue = value;

            if (CollectionUtils.isEmpty(value)) {
                propertyValue = null;
            } else if (value.size() == 1) {
                propertyValue = value.iterator().next();
            }

            if (Objects.isNull(propertyValue) || StringUtils.isBlank(propertyValue.toString())) {
                continue;
            }

            Property p = new Property(propertyName, propertyValue);
            String condition = StringUtils.substringAfterLast(fieldCondition, fieldConditionSeparators);
            String end = fieldOpenPrefix + fieldCondition + fieldCloseSuffix;
            ConditionType type = ConditionType.And;

            if (!StringUtils.endsWith(name, end)) {

                String s = end + fieldConditionSeparators;
                String typeValue = StringUtils.substringBetween(name, s, fieldOpenPrefix);
                type = NameEnumUtils.parse(StringUtils.capitalize(typeValue), ConditionType.class, true);

                if (Objects.isNull(type)) {
                    String msg = MessageFormat.format(
                            "找不到条件类型，请检查格式是否存在问题，标准的条件格式为 {0}{1}字段名{2}通配符{3} " +
                                    "如果多个条件，请记得添加 or 或者 and 关联下一个条件的, " +
                                    "如: {0}{1}字段名{2}通配符{3}{2}<and|ro>{1}字段名{2}通配符{3} " +
                                    "= where 字段名 通配符 值 <and|ro> 字段名 通配符 值。",
                            conditionNamePrefix, fieldOpenPrefix, fieldConditionSeparators, fieldCloseSuffix);
                    throw new SystemException(msg);
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

    /**
     * 获取数组符号
     *
     * @return 数组符号
     */
    public String getArraySymbol() {
        return arraySymbol;
    }

    /**
     * 设置数组符号
     *
     * @param arraySymbol 数组符号
     */
    public void setArraySymbol(String arraySymbol) {
        this.arraySymbol = arraySymbol;
    }
}
