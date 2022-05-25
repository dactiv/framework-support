package com.github.dactiv.framework.minio.config;

import com.github.dactiv.framework.commons.minio.ExpirableBucket;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 自动删除配置
 *
 * @author maurice.chen
 */
@ConfigurationProperties("dactiv.minio.auto-delete")
public class AutoDeleteProperties {
    /**
     * 自动删除调度表达式
     */
    private String cron = "0 1 * * * ?";

    /**
     * 过期的桶映射 map
     */
    private List<ExpirableBucket> expiration;

    /**
     * 自动删除配置
     */
    public AutoDeleteProperties() {
    }

    /**
     * 获取自动扫描过期文件表达式
     *
     * @return 自动扫描过期文件表达式
     */
    public String getCron() {
        return cron;
    }

    /**
     * 设置自动扫描过期文件表达式
     *
     * @param cron 自动扫描过期文件表达式
     */
    public void setCron(String cron) {
        this.cron = cron;
    }

    /**
     * 获取超时配置
     *
     * @return 超时配置 key 为桶名称，value 为过期时间
     */
    public List<ExpirableBucket> getExpiration() {
        return expiration;
    }

    /**
     * 设置超时配置
     *
     * @param expiration key 为桶名称，value 为过期时间
     */
    public void setExpiration(List<ExpirableBucket> expiration) {
        this.expiration = expiration;
    }
}
