package com.github.dactiv.framework.commons.generator.twitter;

import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.generator.IdGenerator;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 雪花 id 生成器
 *
 * @author maurice.chen
 */
public class SnowflakeIdGenerator implements IdGenerator<String> {

    /**
     * Thu, 04 Nov 2010 01:42:54 GMT
     */
    private static final long DEFAULT_TWEPOCH = 0L;
    /**
     * 节点ID长度
     */
    private static final long DEFAULT_WORKER_ID_BITS = 5L;
    /**
     * 数据中心ID长度
     */
    private static final long DEFAULT_DATA_CENTER_ID_BITS = 5L;
    /**
     * 最大支持机器节点数 0~31，一共 32 个
     */
    private static final long DEFAULT_MAX_WORKER_ID = ~(-1L << DEFAULT_WORKER_ID_BITS);
    /**
     * 最大支持数据中心节点数 0~31，一共 32 个
     */
    private static final long DEFAULT_MAX_DATA_CENTER_ID = ~(-1L << DEFAULT_DATA_CENTER_ID_BITS);
    /**
     * 序列号 12 位
     */
    private static final long DEFAULT_SEQUENCE_BITS = 12L;
    /**
     * 业务 ID 16 位
     */
    private static final int DEFAULT_MACHINE_NUMBER_BITS = 3;
    /**
     * 机器节点左移 12 位
     */
    private static final long DEFAULT_WORKER_ID_SHIFT = DEFAULT_SEQUENCE_BITS;
    /**
     * 数据中心节点左移 17 位
     */
    private static final long DEFAULT_DATA_CENTER_ID_SHIFT = DEFAULT_SEQUENCE_BITS + DEFAULT_WORKER_ID_BITS;
    /**
     * 时间毫秒数左移22位
     */
    private static final long DEFAULT_TIMESTAMP_LEFT_SHIFT = DEFAULT_SEQUENCE_BITS + DEFAULT_WORKER_ID_BITS +
            DEFAULT_DATA_CENTER_ID_BITS;
    /**
     * 每毫秒最大ID生成数量，默认为:4095
     */
    private static final long DEFAULT_SEQUENCE_MASK = ~(-1L << DEFAULT_SEQUENCE_BITS);

    /**
     * 默认时间格式化信息
     */
    private static final DateTimeFormatter DEFAULT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHH");
    /**
     * 最后访问的时间戳
     */
    private long lastTimestamp = -1L;
    /**
     * 序列开始值
     */
    private long sequence = 0L;
    /**
     * 配置信息
     */
    private final SnowflakeProperties properties;
    /**
     * 并发锁
     */
    private final Lock lock = new ReentrantLock();

    /**
     * 创建一个 twitter 的 snowflake id 生成算法实现
     *
     * @param properties 配置信息
     */
    public SnowflakeIdGenerator(SnowflakeProperties properties) {
        // sanity check for workerId
        if (properties.getWorkerId() > DEFAULT_MAX_WORKER_ID || properties.getWorkerId() < 0) {
            String msg = "worker Id can't be greater than %d or less than 0";
            throw new IllegalArgumentException(String.format(msg, DEFAULT_MAX_WORKER_ID));
        }
        if (properties.getDataCenterId() > DEFAULT_MAX_DATA_CENTER_ID || properties.getDataCenterId() < 0) {
            String msg = "datacenter Id can't be greater than %d or less than 0";
            throw new IllegalArgumentException(String.format(msg, DEFAULT_MAX_DATA_CENTER_ID));
        }
        if (properties.getServiceId() == null || properties.getServiceId().length() != DEFAULT_MACHINE_NUMBER_BITS) {
            String msg = "serviceId's length must be equal %d";
            throw new IllegalArgumentException(String.format(msg, DEFAULT_MACHINE_NUMBER_BITS));
        }
        this.properties = properties;
    }

    @Override
    public String generateId() {
        try {
            lock.lock();
            // 判断执行时是否在同一毫秒内，如果在同一毫秒内执行，序列号 sequenceBits 递增
            // 一毫秒最多能生成 4095 个ID。超过 4095 则自动等待下一毫秒。
            long timestamp = System.currentTimeMillis();

            if (timestamp < lastTimestamp) {
                String msg = "Clock moved backwards.  Refusing to generate id for %d milliseconds";
                throw new SystemException(String.format(msg, lastTimestamp - timestamp));
            }

            if (lastTimestamp == timestamp) {
                //sequence自增，因为sequence只有12bit，所以和sequenceMask相与一下，去掉高位
                sequence = (sequence + 1) & DEFAULT_SEQUENCE_MASK;
                if (sequence == 0) {
                    timestamp = getNextMillis(lastTimestamp);
                }
            } else {
                sequence = 0L;
            }

            lastTimestamp = timestamp;
            // 按照规则拼出ID,并添加日期
            return timestampToStr(timestamp) + properties.getServiceId() +
                    (((timestamp - DEFAULT_TWEPOCH) << DEFAULT_TIMESTAMP_LEFT_SHIFT)
                            | (properties.getDataCenterId() << DEFAULT_DATA_CENTER_ID_SHIFT)
                            | (properties.getWorkerId() << DEFAULT_WORKER_ID_SHIFT) | sequence);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 将时间戳转换为 string 类型
     *
     * @param timestamp 时间戳
     *
     * @return string 类型的时间戳
     */
    private String timestampToStr(long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).format(DEFAULT_DATE_FORMAT);
    }

    /**
     * 获取下一个毫秒时间戳值
     *
     * @param lastTimestamp 最后访问的时间戳
     *
     * @return 时间戳值
     */
    private long getNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}
