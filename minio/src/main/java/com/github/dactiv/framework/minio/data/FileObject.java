package com.github.dactiv.framework.minio.data;

/**
 * 文件对象描述
 *
 * @author maurice.chen
 */
public class FileObject extends Bucket {

    private static final long serialVersionUID = 3325877878659487154L;

    /**
     * 对象文件名称
     */
    private String objectName;

    /**
     * 文件对象描述
     */
    public FileObject() {
    }

    /**
     * 文件对象描述
     *
     * @param bucketName 同名称
     * @param objectName 文件对象名称
     */
    public FileObject(String bucketName, String objectName) {
        super(bucketName);
        this.objectName = objectName;
    }

    /**
     * 文件对象描述
     *
     * @param bucket     桶信息
     * @param objectName 文件对象名称
     */
    public FileObject(Bucket bucket, String objectName) {
        this(bucket.getBucketName(), bucket.getRegion(), objectName);
    }

    /**
     * 文件对象描述
     *
     * @param bucketName 桶名称
     * @param region     桶所属区域
     * @param objectName 对象文件名称
     */
    public FileObject(String bucketName, String region, String objectName) {
        super(bucketName, region);
        this.objectName = objectName;
    }

    /**
     * 获取文件对象名称
     *
     * @return 文件对象名称
     */
    public String getObjectName() {
        return objectName;
    }

    /**
     * 设置文件对象名称
     *
     * @param objectName 文件对象名称
     */
    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    /**
     * 创建一个文件对象描述
     *
     * @param bucketName 桶名称
     * @param objectName 文件对象名称
     *
     * @return 文件对象描述
     */
    public static FileObject of(String bucketName, String objectName) {
        return of(bucketName, null, objectName);
    }

    /**
     * 创建一个文件对象描述
     *
     * @param bucketName 桶名称
     * @param region     桶所属区域
     * @param objectName 文件对象名称
     *
     * @return 文件对象描述
     */
    public static FileObject of(String bucketName, String region, String objectName) {
        return new FileObject(bucketName, region, objectName);
    }

    /**
     * 创建一个文件对象描述
     *
     * @param bucket     桶描述
     * @param objectName 文件对象名称
     *
     * @return 文件对象描述
     */
    public static FileObject of(Bucket bucket, String objectName) {
        return of(bucket.getBucketName(), bucket.getRegion(), objectName);
    }
}
