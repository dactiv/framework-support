package com.github.dactiv.framework.commons.minio;

import com.github.dactiv.framework.commons.Casts;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.propertyeditors.ResourceBundleEditor;

/**
 * 带文件名称的文件对象描述
 *
 * @author maurice.chen
 */
public class FilenameObject extends FileObject {

    /**
     * 文件名称
     */
    private String filename;

    /**
     * 带文件名称的文件对象描述
     */
    public FilenameObject() {
    }

    /**
     * 带文件名称的文件对象描述
     *
     * @param bucketName 桶名称
     * @param objectName 对象名称
     * @param filename   文件名称
     */
    public FilenameObject(String bucketName, String objectName, String filename) {
        super(bucketName, objectName);
        this.filename = filename;
    }

    /**
     * 带文件名称的文件对象描述
     *
     * @param bucket     桶信息名称
     * @param objectName 对象名称
     * @param filename   文件名称
     */
    public FilenameObject(Bucket bucket, String objectName, String filename) {
        super(bucket, objectName);
        this.filename = filename;
    }

    /**
     * 带文件名称的文件对象描述
     *
     * @param bucketName 桶名称
     * @param region     桶所属区域
     * @param objectName 对象名称
     * @param filename   文件名称
     */
    public FilenameObject(String bucketName, String region, String objectName, String filename) {
        super(bucketName, region, objectName);
        this.filename = filename;
    }

    /**
     * 获取文件名称
     *
     * @return 文件名称
     */
    public String getFilename() {
        return filename;
    }

    /**
     * 设置文件名称
     *
     * @param filename 文件名称
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * 创建一个带文件名称的文件对象描述
     *
     * @param bucketName 桶名称
     * @param objectName 文件对象名称
     * @param filename 文件名称
     *
     * @return 带文件名称的文件对象描述
     */
    public static FilenameObject of(String bucketName, String objectName, String filename) {
        return of(bucketName, null, objectName, filename);
    }

    /**
     * 创建一个带文件名称的文件对象描述
     *
     * @param bucketName 桶名称
     * @param region     桶所属区域
     * @param objectName 文件对象名称
     * @param filename 文件名称
     *
     * @return 带文件名称的文件对象描述
     */
    public static FilenameObject of(String bucketName, String region, String objectName, String filename) {
        return new FilenameObject(bucketName, region, objectName, filename);
    }

    /**
     * 创建一个带文件名称的文件对象描述
     *
     * @param bucket     桶描述
     * @param objectName 文件对象名称
     * @param filename 文件名称
     *
     * @return 带文件名称的文件对象描述
     */
    public static FilenameObject of(Bucket bucket, String objectName, String filename) {
        return of(bucket.getBucketName(), bucket.getRegion(), objectName, filename);
    }

    /**
     * 创建一个带文件名称的文件对象描述
     *
     * @param fileObject 文件对象描述
     *
     * @return 带文件名称的文件对象描述
     */
    public static FilenameObject of(FileObject fileObject) {
        FilenameObject filenameObject = of(fileObject.getBucketName(), fileObject.getRegion(), fileObject.getObjectName(), fileObject.getObjectName());
        filenameObject.setObjectName(String.valueOf(System.currentTimeMillis()));
        filenameObject.setObjectName(System.currentTimeMillis() + ResourceBundleEditor.BASE_NAME_SEPARATOR + filenameObject.getFilename());
        return filenameObject;
    }
}
