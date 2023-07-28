package com.github.dactiv.framework.minio;

import com.github.dactiv.framework.commons.minio.FileObject;
import com.google.common.collect.Multimap;
import io.minio.CreateMultipartUploadResponse;
import io.minio.ListPartsResponse;
import io.minio.MinioAsyncClient;
import io.minio.ObjectWriteResponse;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.XmlParserException;
import io.minio.messages.Part;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;


/**
 * 增强 minio 客户端
 *
 * @author maurice.chen
 */
public class EnhanceMinioClient extends MinioAsyncClient {
    protected EnhanceMinioClient(MinioAsyncClient client) {
        super(client);

    }

    /**
     * 创建分片上传请求
     *
     * @param fileObject       文件对象
     * @param headers          消息头
     * @param extraQueryParams 额外查询参数
     *
     * @return 创建分片上传响应体
     */
    public CreateMultipartUploadResponse createMultipartUpload(FileObject fileObject, Multimap<String, String> headers, Multimap<String, String> extraQueryParams) throws InsufficientDataException, IOException, NoSuchAlgorithmException, InvalidKeyException, XmlParserException, InternalException, ExecutionException, InterruptedException {
        return super.createMultipartUploadAsync(fileObject.getBucketName(), fileObject.getRegion(), fileObject.getObjectName(), headers, extraQueryParams).get();
    }

    /**
     * 完成分片上传，执行合并文件
     *
     * @param fileObject       文件对象
     * @param uploadId         上传ID
     * @param parts            分片
     * @param extraHeaders     额外消息头
     * @param extraQueryParams 额外查询参数
     *
     * @return 文件对象创建情况响应体
     */
    public ObjectWriteResponse completeMultipartUpload(FileObject fileObject, String uploadId, Part[] parts, Multimap<String, String> extraHeaders, Multimap<String, String> extraQueryParams) throws InsufficientDataException, IOException, NoSuchAlgorithmException, InvalidKeyException, XmlParserException, InternalException, ExecutionException, InterruptedException {
        return super.completeMultipartUploadAsync(fileObject.getBucketName(), fileObject.getRegion(), fileObject.getObjectName(), uploadId, parts, extraHeaders, extraQueryParams).get();
    }

    /**
     * 针对文件对象查询文件分片内容
     *
     * @param fileObject 文件对象
     * @param maxParts 文件部分内容的最大值
     * @param partNumberMarker 文件部分内容位置编号
     * @param uploadId 上传 id
     * @param extraHeaders 额外消息头
     * @param extraQueryParams 额外查询参数
     *
     * @return 文件分片内容响应体
     */
    public ListPartsResponse listParts(FileObject fileObject, Integer maxParts, Integer partNumberMarker, String uploadId, Multimap<String, String> extraHeaders, Multimap<String, String> extraQueryParams) throws InsufficientDataException, IOException, NoSuchAlgorithmException, InvalidKeyException, XmlParserException, InternalException, ExecutionException, InterruptedException {
        return super.listPartsAsync(fileObject.getBucketName(), fileObject.getRegion(), fileObject.getObjectName(), maxParts, partNumberMarker, uploadId, extraHeaders, extraQueryParams).get();
    }
}
