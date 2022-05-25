package com.github.dactiv.framework.minio;

import com.github.dactiv.framework.commons.minio.Bucket;
import com.github.dactiv.framework.commons.minio.FileObject;
import com.google.common.collect.Multimap;
import io.minio.CreateMultipartUploadResponse;
import io.minio.ListMultipartUploadsResponse;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.errors.*;
import io.minio.messages.Part;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


/**
 * 增强 minio 客户端
 *
 * @author maurice.chen
 */
public class EnhanceMinioClient extends MinioClient {

    protected EnhanceMinioClient(MinioClient client) {
        super(client);
    }

    /**
     * 创建分片上传请求
     *
     * @param fileObject       文件对象
     * @param headers          消息头
     * @param extraQueryParams 额外查询参数
     */
    public CreateMultipartUploadResponse createMultipartUpload(FileObject fileObject, Multimap<String, String> headers, Multimap<String, String> extraQueryParams) throws NoSuchAlgorithmException, InsufficientDataException, IOException, InvalidKeyException, ServerException, XmlParserException, ErrorResponseException, InternalException, InvalidResponseException {
        return super.createMultipartUpload(fileObject.getBucketName(), fileObject.getRegion(), fileObject.getObjectName(), headers, extraQueryParams);
    }

    /**
     * 完成分片上传，执行合并文件
     *
     * @param fileObject       文件对象
     * @param uploadId         上传ID
     * @param parts            分片
     * @param extraHeaders     额外消息头
     * @param extraQueryParams 额外查询参数
     */
    public ObjectWriteResponse completeMultipartUpload(FileObject fileObject, String uploadId, Part[] parts, Multimap<String, String> extraHeaders, Multimap<String, String> extraQueryParams) throws NoSuchAlgorithmException, InsufficientDataException, IOException, InvalidKeyException, ServerException, XmlParserException, ErrorResponseException, InternalException, InvalidResponseException {
        return super.completeMultipartUpload(fileObject.getBucketName(), fileObject.getRegion(), fileObject.getObjectName(), uploadId, parts, extraHeaders, extraQueryParams);
    }

    /**
     * 查询分片数据
     *
     * @param bucket           桶信息
     * @param extraHeaders     额外消息头
     * @param extraQueryParams 额外查询参数
     */
    public ListMultipartUploadsResponse listMultipartUploads(Bucket bucket, String delimiter, String encodingType, String keyMarker, Integer maxUploads, String prefix, String uploadIdMarker, Multimap<String, String> extraHeaders, Multimap<String, String> extraQueryParams) throws NoSuchAlgorithmException, InsufficientDataException, IOException, InvalidKeyException, ServerException, XmlParserException, ErrorResponseException, InternalException, InvalidResponseException {
        return super.listMultipartUploads(bucket.getBucketName(), bucket.getRegion(), delimiter, encodingType, keyMarker, maxUploads, prefix, uploadIdMarker, extraHeaders, extraQueryParams);
    }
}
