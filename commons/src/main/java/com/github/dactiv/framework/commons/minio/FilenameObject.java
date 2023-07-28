package com.github.dactiv.framework.commons.minio;

import com.github.dactiv.framework.commons.Casts;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.propertyeditors.ResourceBundleEditor;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.DigestUtils;

import java.io.Serial;
import java.nio.charset.Charset;

/**
 * 带文件名称的文件对象描述
 *
 * @author maurice.chen
 */
public class FilenameObject extends FileObject {

    @Serial
    private static final long serialVersionUID = -8658801081544792057L;
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

        String prefix = StringUtils.EMPTY;
        if (StringUtils.contains(fileObject.getObjectName(), AntPathMatcher.DEFAULT_PATH_SEPARATOR)) {
            prefix = StringUtils.substringBeforeLast(fileObject.getObjectName(), AntPathMatcher.DEFAULT_PATH_SEPARATOR);
        }
        String originalFilename = fileObject.getObjectName();

        if (StringUtils.isNotEmpty(prefix)) {
            originalFilename = StringUtils.substringAfterLast(fileObject.getObjectName(), AntPathMatcher.DEFAULT_PATH_SEPARATOR);
        }

        String objectName = DigestUtils.md5DigestAsHex(
                (System.currentTimeMillis() + ResourceBundleEditor.BASE_NAME_SEPARATOR + fileObject.getObjectName()).getBytes(Charset.defaultCharset())
        );
        String suffix = StringUtils.substringAfterLast(fileObject.getObjectName(), Casts.DOT);
        if (StringUtils.isNotEmpty(suffix)) {
            objectName = objectName + Casts.DOT + suffix;
        }
        if (StringUtils.isNotEmpty(prefix)) {
            objectName = prefix + AntPathMatcher.DEFAULT_PATH_SEPARATOR + objectName;
        }
        return of(
                fileObject.getBucketName(),
                fileObject.getRegion(),
                objectName,
                originalFilename
        );
    }

}
