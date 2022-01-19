package com.github.dactiv.framework.nacos.task;

/**
 * nacos cron 调度信息
 *
 * @author maurice.chen
 */
public class NacosCronScheduledInfo extends CronScheduledInfo {

    /**
     * 调度配置变量名
     */
    private String cronPropertyName;

    /**
     * 时区配置变量名
     */
    private String timeZonePropertyName;

    public NacosCronScheduledInfo() {
    }

    public NacosCronScheduledInfo(String name, String cronPropertyName) {
        this(name, cronPropertyName, null);
    }

    public NacosCronScheduledInfo(String name, String cronPropertyName, String timeZonePropertyName) {
        super(name);
        this.cronPropertyName = cronPropertyName;
        this.timeZonePropertyName = timeZonePropertyName;
    }

    /**
     * 获取配置变量名
     *
     * @return 变量名
     */
    public String getCronPropertyName() {
        return cronPropertyName;
    }

    /**
     * 设置配置变量名
     *
     * @param cronPropertyName 变量名
     */
    public void setCronPropertyName(String cronPropertyName) {
        this.cronPropertyName = cronPropertyName;
    }

    /**
     * 获取时区配置变量名
     *
     * @return 时区配置变量名
     */
    public String getTimeZonePropertyName() {
        return timeZonePropertyName;
    }

    /**
     * 设置时区配置变量名
     *
     * @param timeZonePropertyName 时区配置变量名
     */
    public void setTimeZonePropertyName(String timeZonePropertyName) {
        this.timeZonePropertyName = timeZonePropertyName;
    }

}
