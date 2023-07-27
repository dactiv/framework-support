package com.github.dactiv.framework.commons.minio;



/**
 * 版本文件对象描述
 *
 * @author maurice.chen
 */
public class VersionFileObject extends FileObject {

    
    private static final long serialVersionUID = 1336016999557997726L;

    /**
     * 版本 id
     */
    private String versionId;

    /**
     * 版本文件对象描述
     */
    public VersionFileObject() {
    }

    /**
     * 版本文件对象描述
     *
     * @param bucketName 桶名称
     * @param objectName 文件对象名称
     * @param versionId  版本 id
     */
    public VersionFileObject(String bucketName, String objectName, String versionId) {
        super(bucketName, objectName);
        this.versionId = versionId;
    }

    /**
     * 版本文件对象描述
     *
     * @param bucketName 桶名称
     * @param objectName 文件对象名称
     * @param region     桶所属区域
     * @param versionId  版本 id
     */
    public VersionFileObject(String bucketName, String region, String objectName, String versionId) {
        super(bucketName, region, objectName);
        this.versionId = versionId;
    }

    /**
     * 版本文件对象描述
     *
     * @param bucket     桶描述
     * @param objectName 文件对象名称
     * @param versionId  版本 id
     */
    public VersionFileObject(Bucket bucket, String objectName, String versionId) {
        super(bucket, objectName);
        this.versionId = versionId;
    }

    /**
     * 版本文件对象描述
     *
     * @param fileObject 文件对象描述
     * @param versionId  版本 id
     */
    public VersionFileObject(FileObject fileObject, String versionId) {
        super(fileObject.getBucketName(), fileObject.getRegion(), fileObject.getObjectName());
        this.versionId = versionId;
    }

    /**
     * 获取版本 ID
     *
     * @return 版本 ID
     */
    public String getVersionId() {
        return versionId;
    }

    /**
     * 设置版本 ID
     *
     * @param versionId 版本 ID
     */
    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    /**
     * 创建一个版本文件对象描述
     *
     * @param bucketName 桶名称
     * @param objectName 文件对象名称
     *
     * @return 文件对象描述
     */
    public static VersionFileObject of(String bucketName, String objectName, String versionId) {
        return of(bucketName, null, objectName, versionId);
    }

    /**
     * 创建一个版本文件对象描述
     *
     * @param bucketName 桶名称
     * @param region     桶所属区域
     * @param objectName 文件对象名称
     *
     * @return 文件对象描述
     */
    public static VersionFileObject of(String bucketName, String region, String objectName, String versionId) {
        return new VersionFileObject(bucketName, region, objectName, versionId);
    }

    /**
     * 创建一个版本文件对象描述
     *
     * @param bucket     桶描述
     * @param objectName 文件对象名称
     *
     * @return 文件对象描述
     */
    public static VersionFileObject of(Bucket bucket, String objectName, String versionId) {
        return of(bucket.getBucketName(), bucket.getRegion(), objectName, versionId);
    }

    /**
     * 创建一个版本文件对象描述
     *
     * @param fileObject 文件对象描述
     * @param versionId  版本号
     *
     * @return 版本文件对象描述
     */
    public static VersionFileObject of(FileObject fileObject, String versionId) {
        return of(fileObject.getBucketName(), fileObject.getRegion(), fileObject.getObjectName(), versionId);
    }
}
