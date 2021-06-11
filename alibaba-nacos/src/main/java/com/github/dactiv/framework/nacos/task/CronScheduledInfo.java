package com.github.dactiv.framework.nacos.task;

import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.support.CronTrigger;

import java.util.TimeZone;

/**
 * cron 调度信息
 *
 * @author maurice.chen
 */
public class CronScheduledInfo {

    /**
     * 默认表达式的字段名称
     */
    public static final String DEFAULT_EXPRESSION_FIELD_NAME = "expression";

    /**
     * 默认表达式的时区名称
     */
    public static final String DEFAULT_TIME_ZONE_FIELD_NAME = "timeZone";

    /**
     * 调度名称
     */
    private String name;

    /**
     * 表达式
     */
    private String expression;

    /**
     * 时区
     */
    private TimeZone timeZone;

    /**
     * 调度线程
     */
    private Runnable runnable;

    /**
     * 调度 cron 任务信息的返回值
     */
    private ScheduledTask scheduledTask;

    public CronScheduledInfo() {
    }

    public CronScheduledInfo(String name) {
        this.name = name;
    }

    /**
     * 获取调度名称
     *
     * @return 调度名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置调度名称
     *
     * @param name 调度名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取表达式
     *
     * @return 表达式
     */
    public String getExpression() {
        return expression;
    }

    /**
     * 设置表达式
     *
     * @param expression 表达式
     */
    public void setExpression(String expression) {
        this.expression = expression;
    }

    /**
     * 获取时区
     *
     * @return 获取时区
     */
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * 设置时区
     *
     * @param timeZone 时区
     */
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * 获取调度线程
     *
     * @return 调度线程
     */
    public Runnable getRunnable() {
        return runnable;
    }

    /**
     * 设置调度线程
     *
     * @param runnable 调度线程
     */
    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    /**
     * 获取调度 cron 任务信息的返回值
     *
     * @return 调度 cron 任务信息的返回值
     */
    public ScheduledTask getScheduledTask() {
        return scheduledTask;
    }

    /**
     * 设置调度 cron 任务信息的返回值
     *
     * @param scheduledTask 调度 cron 任务信息的返回值
     */
    public void setScheduledTask(ScheduledTask scheduledTask) {
        this.scheduledTask = scheduledTask;
    }

    /**
     * 创建 cron 任务
     *
     * @return cron 任务
     */
    public CronTask createCronTask() {
        return new CronTask(getRunnable(), new CronTrigger(getExpression(), getTimeZone()));
    }
}
