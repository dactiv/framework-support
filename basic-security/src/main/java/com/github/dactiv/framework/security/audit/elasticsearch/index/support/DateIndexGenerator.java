package com.github.dactiv.framework.security.audit.elasticsearch.index.support;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.security.audit.PropertyIndexGenerator;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * 日期索引生成器
 *
 * @author maurice.chen
 */
public class DateIndexGenerator extends PropertyIndexGenerator {

    public final static String DEFAULT_PATTERN = "yyyy_MM";

    /**
     * 时间时间格式化内容
     */
    private String pattern = DEFAULT_PATTERN;

    /**
     * 时间属性的名称
     */
    private List<String> datePropertyNames;

    public DateIndexGenerator() {
    }

    public DateIndexGenerator(String prefix, String separator, List<String> datePropertyNames) {
        this(new LinkedList<>(), prefix, separator, datePropertyNames);
    }

    public DateIndexGenerator(List<String> propertyNames, String prefix, String separator, List<String> datePropertyNames) {
        super(propertyNames, prefix, separator);
        this.datePropertyNames = datePropertyNames;
    }

    @Override
    protected List<String> afterAppend(Object object) {

        List<String> result = super.afterAppend(object);

        Object date = getDateValue(object);

        if (Objects.isNull(date)) {
            throw new SystemException("在 " + object.getClass().getName() + "中，找不到 " + datePropertyNames + " 字段作为日期值使用");
        }

        if (ChronoLocalDateTime.class.isAssignableFrom(date.getClass())) {

            ChronoLocalDateTime<?> time = Casts.cast(date);

            result.add(time.format(DateTimeFormatter.ofPattern(pattern)));

        } else if (Date.class.isAssignableFrom(date.getClass())) {
            Date d = Casts.cast(date);
            result.add(new SimpleDateFormat(pattern).format(d));
        } else if (Instant.class.isAssignableFrom(date.getClass())) {
            Instant i = Casts.cast(date);
            LocalDateTime localDateTime = LocalDateTime.ofInstant(i, ZoneId.systemDefault());
            result.add(localDateTime.format(DateTimeFormatter.ofPattern(pattern)));
        } else {
            throw new SystemException("对象 [" + object.getClass().getName() + "] 的 [" + datePropertyNames + "] 属性非日期类型");
        }

        return result;
    }

    private Object getDateValue(Object object) {
        Object date = null;
        for (String datePropertyName : datePropertyNames) {
            Field field = ReflectionUtils.findField(object.getClass(), datePropertyName);
            if (Objects.isNull(field)) {
                continue;
            }
            field.setAccessible(true);
            date = ReflectionUtils.getField(field, object);
            if (Objects.nonNull(date)) {
                break;
            }
        }
        return date;
    }

    /**
     * 获取时间格式化内容
     *
     * @return 时间格式化内容
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * 设置时间格式化内容
     *
     * @param pattern 时间格式化内容
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * 获取时间属性的名称
     *
     * @return 时间属性的名称
     */
    public List<String> getDatePropertyNames() {
        return datePropertyNames;
    }

    /**
     * 设置时间属性的名称
     *
     * @param datePropertyNames 时间属性的名称
     */
    public void setDatePropertyNames(List<String> datePropertyNames) {
        this.datePropertyNames = datePropertyNames;
    }
}
