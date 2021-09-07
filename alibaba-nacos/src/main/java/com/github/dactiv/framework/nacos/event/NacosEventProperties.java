package com.github.dactiv.framework.nacos.event;

import com.github.dactiv.framework.commons.TimeProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

/**
 * nacoa 事件配置
 *
 * @author maurice.chen
 */
@ConfigurationProperties("spring.cloud.nacos.discovery.event")
public class NacosEventProperties {

    /**
     * 扫描并订阅服务的 cron 表达式
     */
    private String scanServiceCron = "0 0/3 * * * ?";

    /**
     * 取消订阅扫描的 cron 表达式
     */
    private String unsubscribeScanCron = "0 0/3 * * * ?";

    /**
     * 过期取消订阅时间
     */
    private TimeProperties expireUnsubscribeTime = new TimeProperties(1, TimeUnit.HOURS);

    public NacosEventProperties() {
    }

    public NacosEventProperties(String scanServiceCron) {
        this.scanServiceCron = scanServiceCron;
    }

    /**
     * 获取扫描并订阅服务的 cron 表达式
     *
     * @return 扫描并订阅服务的 cron 表达式
     */
    public String getScanServiceCron() {
        return scanServiceCron;
    }

    /**
     * 设置扫描并订阅服务的 cron 表达式
     *
     * @param scanServiceCron 扫描并订阅服务的 cron 表达式
     */
    public void setScanServiceCron(String scanServiceCron) {
        this.scanServiceCron = scanServiceCron;
    }

    /**
     * 获取取消订阅扫描的 cron 表达式
     *
     * @return 取消订阅扫描的 cron 表达式
     */
    public String getUnsubscribeScanCron() {
        return unsubscribeScanCron;
    }

    /**
     * 设置取消订阅扫描的 cron 表达式
     *
     * @param unsubscribeScanCron 取消订阅扫描的 cron 表达式
     */
    public void setUnsubscribeScanCron(String unsubscribeScanCron) {
        this.unsubscribeScanCron = unsubscribeScanCron;
    }

    /**
     * 获取超时取消订阅时间
     *
     * @return 超时取消订阅时间
     */
    public TimeProperties getExpireUnsubscribeTime() {
        return expireUnsubscribeTime;
    }

    /**
     * 设置超时取消订阅时间
     *
     * @param expireUnsubscribeTime 超时取消订阅时间
     */
    public void setExpireUnsubscribeTime(TimeProperties expireUnsubscribeTime) {
        this.expireUnsubscribeTime = expireUnsubscribeTime;
    }
}
