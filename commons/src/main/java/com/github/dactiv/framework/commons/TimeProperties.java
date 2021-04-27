package com.github.dactiv.framework.commons;

import java.util.concurrent.TimeUnit;

/**
 * 时间配置
 *
 * @author maurice.chen
 */
public class TimeProperties {

    /**
     * 值
     */
    private long value;

    /**
     * 单位
     */
    private TimeUnit unit;

    /**
     * 创建一个时间配置
     */
    public TimeProperties() {
    }

    /**
     * 创建一个时间配置
     *
     * @param value 时间值
     * @param unit 单位
     */
    public TimeProperties(long value, TimeUnit unit) {
        this.value = value;
        this.unit = unit;
    }

    /**
     * 获取时间值
     *
     * @return 时间值
     */
    public long getValue() {
        return value;
    }

    /**
     * 设置时间值
     *
     * @param value 时间值
     */
    public void setValue(long value) {
        this.value = value;
    }

    /**
     * 获取时间单位
     *
     * @return 时间单位
     */
    public TimeUnit getUnit() {
        return unit;
    }

    /**
     * 设置时间单位
     *
     * @param unit 时间单位
     */
    public void setUnit(TimeUnit unit) {
        this.unit = unit;
    }
}
