package com.github.dactiv.framework.spring.security.audit.elasticsearch.index.support;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.ReflectionUtils;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.spring.security.audit.PluginAuditEvent;
import com.github.dactiv.framework.spring.security.audit.PropertyIndexGenerator;
import org.springframework.boot.actuate.audit.AuditEvent;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * 日期索引生成器
 *
 * @author maurice.chen
 */
public class DateIndexGenerator extends PropertyIndexGenerator {

    public final static String DEFAULT_PATTERN = "yyyy-MM";

    /**
     * 时间时间格式化内容
     */
    private String pattern = DEFAULT_PATTERN;

    /**
     * 时间属性的名称
     */
    private String datePropertyName;

    public DateIndexGenerator() {
    }

    public DateIndexGenerator(String prefix, String separator, String datePropertyName) {
        this(new LinkedList<>(), prefix, separator, datePropertyName);
    }

    public DateIndexGenerator(List<String> propertyNames, String prefix, String separator, String datePropertyName) {
        super(propertyNames, prefix, separator);
        this.datePropertyName = datePropertyName;
    }

    @Override
    protected List<String> afterAppend(Object object) {

        List<String> result = super.afterAppend(object);

        Object date = ReflectionUtils.getFieldValue(object, datePropertyName);

        if (ChronoLocalDateTime.class.isAssignableFrom(date.getClass())) {

            ChronoLocalDateTime<?> time = Casts.cast(date);

            result.add(time.format(DateTimeFormatter.ofPattern(pattern)));

        } else if (Date.class.isAssignableFrom(date.getClass())) {
            Date d = Casts.cast(date);
            result.add(new SimpleDateFormat(pattern).format(d));
        } else if (Instant.class.isAssignableFrom(date.getClass())){
            Instant i = Casts.cast(date);
            LocalDateTime localDateTime = LocalDateTime.ofInstant(i, ZoneId.systemDefault());
            result.add(localDateTime.format(DateTimeFormatter.ofPattern(pattern)));
        } else {
            throw new SystemException("对象 [" + object.getClass().getName() + "] 的 [" + datePropertyName + "] 属性非日期类型");
        }

        return result;
    }

    public static void main(String[] args) {
        System.out.println(new DateIndexGenerator(PluginAuditEvent.DEFAULT_INDEX_NAME, "-", "timestamp").generateIndex(new AuditEvent(Instant.now(), "", "", new HashMap<>())));
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
    public String getDatePropertyName() {
        return datePropertyName;
    }

    /**
     * 设置时间属性的名称
     *
     * @param datePropertyName 时间属性的名称
     */
    public void setDatePropertyName(String datePropertyName) {
        this.datePropertyName = datePropertyName;
    }
}
