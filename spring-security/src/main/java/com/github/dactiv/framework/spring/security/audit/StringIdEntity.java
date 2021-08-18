package com.github.dactiv.framework.spring.security.audit;

import com.github.dactiv.framework.commons.id.IdEntity;

import java.time.Instant;

/**
 * 字符串的 id 唯一识别
 *
 * @author maurice.chen
 */
public class StringIdEntity extends IdEntity<String> {

    private static final long serialVersionUID = 9005564233086776718L;

    /**
     * 时间戳
     */
    private Instant timestamp;

    public StringIdEntity() {
    }

    /**
     * 获取时间戳
     *
     * @return 时间戳
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * 设置时间戳
     *
     * @param timestamp 时间戳
     */
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
