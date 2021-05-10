package com.github.dactiv.framework.spring.security.audit;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.Index;
import org.springframework.boot.actuate.audit.AuditEvent;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 日期索引生成器
 *
 * @author maurice.chen
 */
public class DateIndexGenerator implements IndexGenerator {

    public final static String DEFAULT_INDEX = "audit-event";

    public final static String DEFAULT_PATTERN = "yyyy-MM";

    private String pattern = DEFAULT_PATTERN;

    public DateIndexGenerator() {
    }

    public DateIndexGenerator(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String generateIndex(AuditEvent event) {
        LocalDateTime time = LocalDateTime.ofInstant(event.getTimestamp(), ZoneId.systemDefault());

        String principal = event.getPrincipal();

        if (StringUtils.contains(principal, ":")) {
            principal = StringUtils.substringAfter(event.getPrincipal(), ":");
        }

        return DEFAULT_INDEX + "-" + principal + "-" + time.format(DateTimeFormatter.ofPattern(pattern));
    }

    @Override
    public String getDefaultIndexPrefix() {
        return DEFAULT_INDEX;
    }

    /**
     * 设置时间格式化内容
     *
     * @param pattern 时间格式化内容
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}
