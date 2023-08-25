package com.github.dactiv.framework.minio.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * minio 配置信息
 *
 * @author maurice.chen
 */
@ConfigurationProperties("dactiv.minio")
public class MinioProperties {

    /**
     * 终端地址
     */
    private String endpoint;

    /**
     * 访问密钥
     */
    private String accessKey;

    /**
     * 安全密钥
     */
    private String secretKey;

    /**
     * minio 配置信息
     */
    public MinioProperties() {
    }

    /**
     * 获取终端地址
     *
     * @return 终端地址
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * 设置终端地址
     *
     * @param endpoint 终端地址
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * 获取访问密钥
     *
     * @return 访问密钥
     */
    public String getAccessKey() {
        return accessKey;
    }

    /**
     * 设置访问密钥
     *
     * @param accessKey 访问密钥
     */
    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    /**
     * 获取安全密钥
     *
     * @return 安全密钥
     */
    public String getSecretKey() {
        return secretKey;
    }

    /**
     * 设置安全密钥
     *
     * @param secretKey 安全密钥
     */
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
