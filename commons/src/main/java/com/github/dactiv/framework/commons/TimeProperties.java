package com.github.dactiv.framework.commons;

import com.github.dactiv.framework.commons.annotation.Time;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * 时间配置
 *
 * @author maurice.chen
 */
public class TimeProperties implements Serializable {

    private static final long serialVersionUID = 2842217678288186207L;
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
     * @param unit  单位
     */
    public TimeProperties(long value, TimeUnit unit) {
        this.value = value;
        this.unit = unit;
    }

    /**
     * 创建一个时间配置
     *
     * @param time 时间注解
     */
    public TimeProperties(Time time) {
        this.value = time.value();
        this.unit = time.unit();
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

    /**
     * 转换为毫秒
     *
     * @return 毫秒值
     */
    public long toMillis() {
        return unit.toMillis(value);
    }

    /**
     * 转换为秒值
     *
     * @return 秒值
     */
    public long toSeconds() {
        return unit.toSeconds(value);
    }

    /**
     * 转换为分钟值
     *
     * @return 分钟值
     */
    public long toMinutes() {
        return unit.toMinutes(value);
    }

    /**
     * 转换为小时值
     *
     * @return 小时值
     */
    public long toHours() {
        return unit.toHours(value);
    }

    /**
     * 转换为微秒值
     *
     * @return 微秒值
     */
    public long toMicros() {
        return unit.toMicros(value);
    }

    /**
     * 转换为天数值
     *
     * @return 天数值
     */
    public long toDays() {
        return unit.toDays(value);
    }

    /**
     * 转换为纳秒值
     *
     * @return 纳秒值
     */
    public long toNanos() {
        return unit.toNanos(value);
    }

    /**
     * 创建时间配置
     *
     * @param value 值
     * @param unit  时间单位
     *
     * @return 时间配置
     */
    public static TimeProperties of(Integer value, TimeUnit unit) {
        return new TimeProperties(value, unit);
    }

    /**
     * 创建时间配置
     *
     * @param time 时间注解
     *
     * @return 时间配置
     */
    public static TimeProperties of(Time time) {
        return new TimeProperties(time);
    }

    /**
     * 创建按天的时间配置
     *
     * @param value 值
     *
     * @return 时间配置
     */
    public static TimeProperties ofDay(Integer value) {
        return new TimeProperties(value, TimeUnit.DAYS);
    }

    /**
     * 创建按小时的时间配置
     *
     * @param value 值
     *
     * @return 时间配置
     */
    public static TimeProperties ofHours(Integer value) {
        return new TimeProperties(value, TimeUnit.HOURS);
    }

    /**
     * 创建按分钟的时间配置
     *
     * @param value 值
     *
     * @return 时间配置
     */
    public static TimeProperties ofMinutes(Integer value) {
        return new TimeProperties(value, TimeUnit.MINUTES);
    }

    /**
     * 创建按秒的时间配置
     *
     * @param value 值
     *
     * @return 时间配置
     */
    public static TimeProperties ofSeconds(Integer value) {
        return new TimeProperties(value, TimeUnit.SECONDS);
    }

    /**
     * 创建按毫秒的时间配置
     *
     * @param value 值
     *
     * @return 时间配置
     */
    public static TimeProperties ofMilliseconds(Integer value) {
        return new TimeProperties(value, TimeUnit.MILLISECONDS);
    }

    /**
     * 创建按微秒的时间配置
     *
     * @param value 值
     *
     * @return 时间配置
     */
    public static TimeProperties ofMicroseconds(Integer value) {
        return new TimeProperties(value, TimeUnit.MICROSECONDS);
    }

    /**
     * 创建按纳秒的时间配置
     *
     * @param value 值
     *
     * @return 时间配置
     */
    public static TimeProperties ofNanoseconds(Integer value) {
        return new TimeProperties(value, TimeUnit.NANOSECONDS);
    }
}
