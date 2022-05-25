package com.github.dactiv.framework.commons.minio;

import java.io.Serializable;

/**
 * 桶描述
 *
 * @author maurice.chen
 */

public class Bucket implements Serializable {

    private static final long serialVersionUID = -8374508623316725573L;

    /**
     * 桶名称
     */
    private String bucketName;
    /**
     * 区域
     */
    private String region;

    /**
     * 桶描述
     */
    public Bucket() {

    }

    /**
     * 桶描述
     *
     * @param bucketName 桶名称
     */
    public Bucket(String bucketName) {
        this.bucketName = bucketName;
    }

    /**
     * 桶描述
     *
     * @param bucketName 桶名称
     * @param region     区域
     */
    public Bucket(String bucketName, String region) {
        this.bucketName = bucketName;
        this.region = region;
    }

    /**
     * 获取桶名称
     *
     * @return 桶名称
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * 设置桶名称
     *
     * @param bucketName 桶名称
     */
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    /**
     * 获取桶所属区域
     *
     * @return 桶所属区域
     */
    public String getRegion() {
        return region;
    }

    /**
     * 设置桶所属区域
     *
     * @param region 桶所属区域
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * 创建一个桶描述
     *
     * @param bucketName 桶名称
     *
     * @return 桶描述
     */
    public static Bucket of(String bucketName) {
        return new Bucket(bucketName);
    }

    /**
     * 创建一个桶描述
     *
     * @param bucketName 桶名称
     * @param region     所属区域
     *
     * @return 桶描述
     */
    public static Bucket of(String bucketName, String region) {
        return new Bucket(bucketName, region);
    }
}
