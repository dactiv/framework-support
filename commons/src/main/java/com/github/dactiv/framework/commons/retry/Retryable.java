package com.github.dactiv.framework.commons.retry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.framework.commons.TimeProperties;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 可重试的对象实现
 *
 * @author maurice.chen
 */
public interface Retryable {

    /**
     * 默认间隔时间次方
     */
    TimeProperties DEFAULT_POW_INTERVAL_TIME = new TimeProperties(5, TimeUnit.SECONDS);

    /**
     * 默认支持的重试 http status
     */
    List<Integer> DEFAULT_SUPPORT_HTTP_STATUS = Arrays.asList(400, 503);

    /**
     * 获取当前重试次数
     *
     * @return 重试次数
     */
    Integer getRetryCount();

    /**
     * 设置当前重试次数
     *
     * @param retryCount 当前重试次数
     */
    void setRetryCount(Integer retryCount);

    /**
     * 设置当前重试次数
     *
     * @return 重试次数
     */
    Integer getMaxRetryCount();

    /**
     * 设置最大重试次数
     *
     * @param maxRetryCount 最大重试次数
     */
    void setMaxRetryCount(Integer maxRetryCount);

    /**
     * 获取下一次重试时间（毫秒为单位）
     *
     * @return 时间（毫秒为单位）
     */
    @JsonIgnore
    default TimeProperties getNextRetryTimeInMillisecond() {
        return DEFAULT_POW_INTERVAL_TIME;
    }

    /**
     * 获取下一次间隔时间
     *
     * @return 间隔时间戳（毫秒为单位）
     */
    @JsonIgnore
    default Integer getNextIntervalTime() {

        return BigDecimal
                .valueOf(getRetryCount())
                .pow(getRetryCount())
                .multiply(BigDecimal.valueOf(getNextRetryTimeInMillisecond().toMillis()))
                .intValue();
    }

    /**
     * 获取下一次重试时间
     *
     * @return 重试时间
     */
    @JsonIgnore
    default Date getNextRetryTime() {
        return new Date(System.currentTimeMillis() + getNextIntervalTime());
    }

    /**
     * 是否可重试
     *
     * @return true 是，否则 false
     */
    default boolean isRetry() {
        return getRetryCount() < getMaxRetryCount();
    }
}
