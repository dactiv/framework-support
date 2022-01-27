package com.github.dactiv.framework.security.audit;

import com.github.dactiv.framework.commons.ReflectionUtils;
import com.github.dactiv.framework.security.audit.elasticsearch.index.IndexGenerator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * 属性索引生成器实现
 *
 * @author maurice.chen
 */
public class PropertyIndexGenerator implements IndexGenerator {

    /**
     * 属性名称集合
     */
    private List<String> propertyNames = new LinkedList<>();

    /**
     * 前缀
     */
    private String prefix;

    /**
     * 分隔符
     */
    private String separator;

    public PropertyIndexGenerator() {
    }

    public PropertyIndexGenerator(List<String> propertyNames, String separator) {
        this(propertyNames, null, separator);
    }

    public PropertyIndexGenerator(List<String> propertyNames, String prefix, String separator) {
        this.propertyNames = propertyNames;
        this.prefix = prefix;
        this.separator = separator;
    }

    @Override
    public String generateIndex(Object object) {

        List<String> result = new LinkedList<>();

        if (StringUtils.isNotBlank(prefix)) {
            result.add(prefix);
        }

        propertyNames
                .stream()
                .map(s -> ReflectionUtils.getReadProperty(object, s))
                .filter(Objects::nonNull)
                .forEach(s -> result.add(s.toString()));

        List<String> append = afterAppend(object);

        if (CollectionUtils.isNotEmpty(append)) {
            result.addAll(append);
        }

        return afterSetting(result);
    }

    protected List<String> afterAppend(Object object) {
        return new LinkedList<>();
    }

    /**
     * 执行后续设置
     *
     * @param result 组装好的属性集合
     *
     * @return 索引值
     */
    protected String afterSetting(List<String> result) {
        return StringUtils.join(result, separator);
    }

    /**
     * 获取属性名称集合
     *
     * @return 属性名称集合
     */
    public List<String> getPropertyNames() {
        return propertyNames;
    }

    /**
     * 设置属性名称集合
     *
     * @param propertyNames 属性名称集合
     */
    public void setPropertyNames(List<String> propertyNames) {
        this.propertyNames = propertyNames;
    }

    /**
     * 获取前缀
     *
     * @return 前缀
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * 设置前缀
     *
     * @param prefix 前缀
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * 获取分隔符
     *
     * @return 分隔符
     */
    public String getSeparator() {
        return separator;
    }

    /**
     * 设置分隔符
     *
     * @param separator 分隔符
     */
    public void setSeparator(String separator) {
        this.separator = separator;
    }
}
