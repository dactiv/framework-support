package com.github.dactiv.framework.minio.config;

import com.github.dactiv.framework.minio.endpoint.UploadEndpoint;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

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
     * 上传响应信息要忽略的字段值
     */
    private List<String> uploadResponseIgnoreFields = Arrays.asList(UploadEndpoint.DEFAULT_IGNORE_FIELDS);

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

    /**
     * 获取上传响应信息要忽略的字段值
     *
     * @return 上传响应信息要忽略的字段值
     */
    public List<String> getUploadResponseIgnoreFields() {
        return uploadResponseIgnoreFields;
    }

    /**
     * 设置上传响应信息要忽略的字段值
     *
     * @param uploadResponseIgnoreFields 上传响应信息要忽略的字段值
     */
    public void setUploadResponseIgnoreFields(List<String> uploadResponseIgnoreFields) {
        this.uploadResponseIgnoreFields = uploadResponseIgnoreFields;
    }
}
