package com.github.dactiv.framework.minio;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.minio.Bucket;
import com.github.dactiv.framework.commons.minio.FileObject;
import com.github.dactiv.framework.commons.minio.VersionFileObject;
import com.google.common.collect.Multimap;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import io.minio.messages.Part;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

/**
 * minio 模版
 *
 * @author maurice.chen
 */
public class MinioTemplate {

    private static final Logger LOGGER = LoggerFactory.getLogger(MinioTemplate.class);

    /**
     * 分片参数参数
     */
    public static final String PART_NUMBER_PARAM_NAME = "partNumber";

    /**
     * 分片数量参数
     */
    public static final String CHUNK_PARAM_NAME = "chunk";

    /**
     * 上传 id
     */
    public static final String UPLOAD_ID_PARAM_NAME = "uploadId";

    /**
     * minio 客户端
     */
    private final EnhanceMinioClient minioClient;

    /**
     * json 对象映射
     */
    private final ObjectMapper objectMapper;

    /**
     * minio 模版
     *
     * @param minioClient  minio 客户端
     * @param objectMapper json 对象映射
     */
    public MinioTemplate(EnhanceMinioClient minioClient, ObjectMapper objectMapper) {
        this.minioClient = minioClient;
        this.objectMapper = objectMapper;
    }

    /**
     * 如果桶名称不存在，创建桶。
     *
     * @param bucket 桶描述
     *
     * @return 如果桶存在返回 true，否则创建桶后返回 false
     *
     * @throws Exception 创建错误时抛出
     */
    public boolean makeBucketIfNotExists(Bucket bucket) throws Exception {

        boolean found = isBucketExist(bucket);

        if (!isBucketExist(bucket)) {
            MakeBucketArgs makeBucketArgs = MakeBucketArgs
                    .builder()
                    .bucket(bucket.getBucketName().toLowerCase())
                    .region(bucket.getRegion())
                    .build();
            minioClient.makeBucket(makeBucketArgs).get();
        }

        return found;
    }

    /**
     * 判断文件是否存在
     *
     * @param fileObject 文件对象
     *
     * @return true 是，否则 false
     */
    public boolean isObjectExist(FileObject fileObject) {
        try {
            minioClient.statObject(
                    StatObjectArgs
                            .builder()
                            .bucket(fileObject.getBucketName())
                            .object(fileObject.getObjectName())
                            .region(fileObject.getRegion())
                            .build()
            );
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 判断桶是否存在
     *
     * @param bucket 桶描述
     *
     * @return tru 存在，否则 false
     *
     * @throws Exception 查询错误时抛出
     */
    public boolean isBucketExist(Bucket bucket) throws Exception {

        BucketExistsArgs builder = BucketExistsArgs
                .builder()
                .bucket(bucket.getBucketName().toLowerCase())
                .region(bucket.getRegion())
                .build();

        return minioClient.bucketExists(builder).get();
    }

    /**
     * 上传文件
     *
     * @param object      文件对象描述
     * @param file        文件内容
     * @param contentType 文件类型
     * @param size        文件大小
     *
     * @return 对象写入响应信息
     *
     * @throws Exception 上传错误时抛出
     */
    public ObjectWriteResponse upload(FileObject object, InputStream file, long size, String contentType) throws Exception {
        return upload(object, file, new LinkedHashMap<>(), size, contentType);
    }

    /**
     * 上传文件
     *
     * @param object       文件对象描述
     * @param file         文件内容
     * @param size         文件大小
     * @param userMetadata 用户元数据信息
     * @param contentType  文件类型
     *
     * @return 对象写入响应信息
     *
     * @throws Exception 上传错误时抛出
     */
    public ObjectWriteResponse upload(FileObject object, InputStream file, Map<String, String> userMetadata, long size, String contentType) throws Exception {

        makeBucketIfNotExists(object);

        PutObjectArgs args = PutObjectArgs
                .builder()
                .bucket(object.getBucketName().toLowerCase())
                .region(object.getRegion())
                .object(object.getObjectName())
                .stream(file, size, -1)
                .contentType(contentType)
                .userMetadata(userMetadata)
                .build();

        return minioClient.putObject(args).get();

    }

    /**
     * 删除文件
     *
     * @param fileObject 文件对象描述
     *
     * @throws Exception 删除错误时抛出
     */
    public void deleteObject(FileObject fileObject) throws Exception {
        deleteObject(fileObject, false);
    }

    /**
     * 获取文件列表
     *
     * @param bucket 桶信息
     *
     * @return 文件项
     *
     * @throws Exception 获取错误时抛出
     */
    public List<ObjectItem> getFileObjects(Bucket bucket) throws Exception {

        ListObjectsArgs args = ListObjectsArgs
                .builder()
                .bucket(bucket.getBucketName())
                .includeUserMetadata(true)
                .useApiVersion1(false)
                .build();

        Iterable<Result<Item>> results = minioClient.listObjects(args);

        return covertObjectItem(results);
    }

    /**
     * 推送对象
     *
     * @param bucket 桶信息
     * @param file 文件信息
     *
     * @return 对象写入响应信息
     *
     * @throws Exception 推送错误时跑出
     */
    public ObjectWriteResponse putObject(Bucket bucket, MultipartFile file) throws Exception {
        return minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket.getBucketName())
                        .region(bucket.getRegion())
                        .object(file.getOriginalFilename())
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .build()
        ).get();
    }

    /**
     * 推送对象
     *
     * @param fileObject 文件对象
     * @param inputStream 输入流
     *
     * @return 对象写入响应信息
     *
     * @throws Exception 推送错误时跑出
     */
    public ObjectWriteResponse putObject(FileObject fileObject, InputStream inputStream) throws Exception {
        return minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(fileObject.getBucketName())
                        .region(fileObject.getRegion())
                        .object(fileObject.getObjectName())
                        .stream(inputStream, inputStream.available(), -1)
                        .build()
        ).get();
    }

    /**
     * 推送对象
     *
     * @param fileObject 文件对象
     * @param inputStream 输入流
     * @param objectSize 文件大小
     * @param partSize 段大小
     *
     * @return 对象写入响应信息
     *
     * @throws Exception 推送错误时跑出
     */
    public ObjectWriteResponse putObject(FileObject fileObject, InputStream inputStream, int objectSize, int partSize) throws Exception {
        return minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(fileObject.getBucketName())
                        .region(fileObject.getRegion())
                        .object(fileObject.getObjectName())
                        .stream(inputStream,  objectSize, partSize)
                        .build()
        ).get();
    }

    /**
     * 删除文件
     *
     * @param fileObject          文件对象描述
     * @param deleteBucketIfEmpty true: 如果桶的文件目录为空，删除桶，否则 false
     *
     * @throws Exception 删除错误时抛出
     */
    public void deleteObject(FileObject fileObject, boolean deleteBucketIfEmpty) throws Exception {

        String bucketName = fileObject.getBucketName().toLowerCase();

        if (StringUtils.endsWith(fileObject.getObjectName(), AntPathMatcher.DEFAULT_PATH_SEPARATOR) ||
                StringUtils.endsWith(fileObject.getObjectName(), Casts.DEFAULT_DOT_SYMBOL)) {

            ListObjectsArgs args = ListObjectsArgs
                    .builder()
                    .bucket(fileObject.getBucketName())
                    .region(fileObject.getRegion())
                    .prefix(fileObject.getObjectName())
                    .build();

            Iterable<Result<Item>> list = minioClient.listObjects(args);
            List<DeleteObject> objects = new LinkedList<>();
            for (Result<Item> r : list) {
                objects.add(new DeleteObject(r.get().objectName()));
            }
            RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs
                    .builder()
                    .bucket(fileObject.getBucketName())
                    .region(fileObject.getRegion())
                    .objects(objects)
                    .build();

            Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
            for (Result<DeleteError>  result : results) {
                DeleteError error = result.get();
                LOGGER.warn("Error in deleting object " + error.objectName() + "; " + error.message());
            }
        } else {

            RemoveObjectArgs.Builder args = RemoveObjectArgs
                    .builder()
                    .bucket(bucketName)
                    .region(fileObject.getRegion())
                    .object(fileObject.getObjectName());

            if (VersionFileObject.class.isAssignableFrom(fileObject.getClass())) {
                VersionFileObject version = Casts.cast(fileObject);
                args.versionId(version.getVersionId());
            }

            minioClient.removeObject(args.build()).get();
        }

        if (deleteBucketIfEmpty) {

            ListObjectsArgs listObjectsArgs = ListObjectsArgs
                    .builder()
                    .bucket(bucketName)
                    .region(fileObject.getRegion())
                    .build();

            Iterable<Result<Item>> iterable = minioClient.listObjects(listObjectsArgs);

            if (!iterable.iterator().hasNext()) {
                Bucket bucket = Bucket.of(fileObject.getBucketName(), fileObject.getRegion());
                deleteBucket(bucket);
            }
        }
    }

    /**
     * 获取文件
     *
     * @return 输入流
     *
     * @throws Exception 获取错误时抛出
     */
    public GetObjectResponse getObject(FileObject fileObject) throws Exception {
        ObjectVersionArgs.Builder<GetObjectArgs.Builder, GetObjectArgs> getObjectArgs = GetObjectArgs
                .builder()
                .bucket(fileObject.getBucketName().toLowerCase())
                .region(fileObject.getRegion())
                .object(fileObject.getObjectName());

        if (VersionFileObject.class.isAssignableFrom(fileObject.getClass())) {
            VersionFileObject version = Casts.cast(fileObject);
            getObjectArgs.versionId(version.getVersionId());
        }

        return minioClient.getObject(getObjectArgs.build()).get();
    }

    /**
     * 获取预览 url
     *
     * @param fileObject 文件对象
     * @param method 签署方法
     *
     * @return url
     */
    public String getPresignedObjectUrl(FileObject fileObject, Method method) throws Exception {
        return getPresignedObjectUrl(fileObject, method, null);
    }

    /**
     * 获取预览 url
     *
     * @param fileObject 文件对象
     * @param method 签署方法
     * @param timeProperties 过期时间配置
     *
     * @return url
     */
    public String getPresignedObjectUrl(FileObject fileObject, Method method, TimeProperties timeProperties) throws Exception {
        return getPresignedObjectUrl(fileObject, method, timeProperties, null);
    }

    /**
     * 获取签署 url
     *
     * @param fileObject 文件对象
     * @param method 签署方法
     * @param timeProperties 过期时间配置
     * @param queryParams 扩展的查询参数
     *
     * @return url
     */
    public String getPresignedObjectUrl(FileObject fileObject, Method method, TimeProperties timeProperties, Map<String, String> queryParams) throws Exception {
        GetPresignedObjectUrlArgs.Builder builder = GetPresignedObjectUrlArgs
                .builder()
                .method(method)
                .bucket(fileObject.getBucketName())
                .region(fileObject.getRegion())
                .object(fileObject.getObjectName());

        if (Objects.nonNull(timeProperties)) {
            builder.expiry((int)timeProperties.getValue(), timeProperties.getUnit());
        }

        if (MapUtils.isNotEmpty(queryParams)) {
            builder.extraQueryParams(queryParams);
        }

        return minioClient.getPresignedObjectUrl(builder.build());
    }

    /**
     * 拷贝文件
     *
     * @param fromObject 来源文件
     * @param toObject   目标文件
     *
     * @return minio API 调用响应的 ObjectWriteResponse 对象
     *
     * @throws Exception 拷贝出错时候抛出
     */
    public ObjectWriteResponse copyObject(FileObject fromObject, FileObject toObject) throws Exception {
        CopySource.Builder copySource = CopySource
                .builder()
                .bucket(fromObject.getBucketName().toLowerCase())
                .region(fromObject.getRegion())
                .object(fromObject.getObjectName());

        if (VersionFileObject.class.isAssignableFrom(fromObject.getClass())) {
            VersionFileObject version = Casts.cast(fromObject);
            copySource.versionId(version.getVersionId());
        }

        CopyObjectArgs.Builder args = CopyObjectArgs
                .builder()
                .bucket(toObject.getBucketName().toLowerCase())
                .region(toObject.getRegion())
                .object(StringUtils.defaultString(toObject.getObjectName(), fromObject.getObjectName()))
                .source(copySource.build());

        return minioClient.copyObject(args.build()).get();
    }

    /**
     * 读取桶的文件内容值并将 json 转换为目标类型
     *
     * @param fileObject 文件对象描述
     * @param <T>        目标类型
     *
     * @return 目标类型对象
     */
    public <T> T readJsonValue(FileObject fileObject, Class<T> targetClass) {
        try {
            InputStream inputStream = getObject(fileObject);
            return objectMapper.readValue(inputStream, targetClass);
        } catch (Exception e) {
            if (JsonMappingException.class.isAssignableFrom(e.getClass())) {
                throw new SystemException(e);
            }
            return null;
        }
    }

    /**
     * 读取桶的文件内容值并将 json 转换为目标类型
     *
     * @param fileObject 文件对象描述
     * @param <T>        目标类型
     *
     * @return 目标类型对象
     */
    public <T> T readJsonValue(FileObject fileObject, JavaType javaType) {
        try {
            InputStream inputStream = getObject(fileObject);
            return objectMapper.readValue(inputStream, javaType);
        } catch (Exception e) {
            if (JsonMappingException.class.isAssignableFrom(e.getClass())) {
                throw new SystemException(e);
            }
            return null;
        }
    }

    /**
     * 读取桶的文件内容值并将 json 转换为目标类型
     *
     * @param fileObject 文件对象描述
     * @param <T>        目标类型
     *
     * @return 目标类型对象
     */
    public <T> T readJsonValue(FileObject fileObject, TypeReference<T> typeReference) {
        try {
            InputStream inputStream = getObject(fileObject);
            return objectMapper.readValue(inputStream, typeReference);
        } catch (Exception e) {
            if (JsonMappingException.class.isAssignableFrom(e.getClass())) {
                throw new SystemException(e);
            }
            return null;
        }
    }

    /**
     * 将对象以 json 的格式写入到指定的桶和文件中
     *
     * @param fileObject 文件对象描述
     * @param value      对象值
     */
    public ObjectWriteResponse writeJsonValue(FileObject fileObject, Object value) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        objectMapper.writeValue(outputStream, value);
        outputStream.flush();

        byte[] bytes = outputStream.toByteArray();
        ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(bytes);
        return upload(fileObject, arrayInputStream, bytes.length, MediaType.APPLICATION_JSON_VALUE);
    }

    /**
     * 删除桶
     *
     * @param bucket 桶描述
     */
    public void deleteBucket(Bucket bucket) throws Exception {
        String name = bucket.getBucketName().toLowerCase();
        BucketExistsArgs existsArgs = BucketExistsArgs
                .builder()
                .bucket(name)
                .region(bucket.getRegion())
                .build();

        boolean exist = minioClient.bucketExists(existsArgs).get();

        if (exist) {
            RemoveBucketArgs removeBucketArgs = RemoveBucketArgs
                    .builder()
                    .bucket(name)
                    .region(bucket.getRegion())
                    .build();

            minioClient.removeBucket(removeBucketArgs).get();
        }

    }

    /**
     * 将查询结果转换为 ObjectItem 对象
     *
     * @param results 查询结果
     *
     * @return ObjectItem 对象集合
     *
     */
    public List<ObjectItem> covertObjectItem(Iterable<Result<Item>> results) throws Exception {

        List<ObjectItem> result = new LinkedList<>();

        for (Result<Item> itemResult : results) {
            Item item = itemResult.get();
            result.add(new ObjectItem(item));
        }

        return result;
    }

    /**
     * 完成分片上传，执行合并文件
     *
     * @param fileObject       文件对象
     * @param uploadId         上传ID
     * @param parts            分片
     *
     * @return 文件对象创建情况响应体
     */
    public ObjectWriteResponse completeMultipartUpload(FileObject fileObject,
                                                       String uploadId,
                                                       Part[] parts) throws Exception {
        return completeMultipartUpload(fileObject, uploadId, parts, null);
    }

    /**
     * 完成分片上传，执行合并文件
     *
     * @param fileObject       文件对象
     * @param uploadId         上传ID
     * @param parts            分片
     * @param extraHeaders     额外消息头
     *
     * @return 文件对象创建情况响应体
     */
    public ObjectWriteResponse completeMultipartUpload(FileObject fileObject,
                                                       String uploadId,
                                                       Part[] parts,
                                                       Multimap<String, String> extraHeaders) throws Exception {
        return completeMultipartUpload(fileObject, uploadId, parts, extraHeaders, null);
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
    public ObjectWriteResponse completeMultipartUpload(FileObject fileObject,
                                                       String uploadId,
                                                       Part[] parts,
                                                       Multimap<String, String> extraHeaders,
                                                       Multimap<String, String> extraQueryParams) throws Exception {
        return minioClient.completeMultipartUpload(fileObject, uploadId, parts, extraHeaders, extraQueryParams);
    }

    /**
     * 创建分片上传请求
     *
     * @param fileObject       文件对象
     *
     * @return 创建分片上传响应体
     */
    public CreateMultipartUploadResponse createMultipartUpload(FileObject fileObject) throws Exception {
        return createMultipartUpload(fileObject, null);
    }

    /**
     * 创建分片上传请求
     *
     * @param fileObject       文件对象
     * @param extraHeaders     消息头
     *
     * @return 创建分片上传响应体
     */
    public CreateMultipartUploadResponse createMultipartUpload(FileObject fileObject,
                                                               Multimap<String, String> extraHeaders) throws Exception {
        return createMultipartUpload(fileObject, extraHeaders, null);
    }

    /**
     * 创建分片上传请求
     *
     * @param fileObject       文件对象
     * @param extraHeaders     消息头
     * @param extraQueryParams 额外查询参数
     *
     * @return 创建分片上传响应体
     */
    public CreateMultipartUploadResponse createMultipartUpload(FileObject fileObject,
                                                               Multimap<String, String> extraHeaders,
                                                               Multimap<String, String> extraQueryParams) throws Exception {
        return minioClient.createMultipartUpload(fileObject, extraHeaders, extraQueryParams);
    }

    /**
     * 针对文件对象查询文件分片内容
     *
     * @param fileObject 文件对象
     * @param maxParts 文件部分内容的最大值
     * @param uploadId 上传 id
     *
     * @return 文件分片内容响应体
     */
    public ListPartsResponse listParts(FileObject fileObject,
                                       Integer maxParts,
                                       String uploadId) throws Exception {
        return listParts(fileObject, maxParts, 0, uploadId);
    }

    /**
     * 针对文件对象查询文件分片内容
     *
     * @param fileObject 文件对象
     * @param maxParts 文件部分内容的最大值
     * @param partNumberMarker 文件部分内容位置编号
     * @param uploadId 上传 id
     *
     * @return 文件分片内容响应体
     */
    public ListPartsResponse listParts(FileObject fileObject,
                                       Integer maxParts,
                                       Integer partNumberMarker,
                                       String uploadId) throws Exception {
        return listParts(fileObject, maxParts, partNumberMarker, uploadId, null);
    }

    /**
     * 针对文件对象查询文件分片内容
     *
     * @param fileObject 文件对象
     * @param maxParts 文件部分内容的最大值
     * @param partNumberMarker 文件部分内容位置编号
     * @param uploadId 上传 id
     * @param extraHeaders 额外消息头
     *
     * @return 文件分片内容响应体
     */
    public ListPartsResponse listParts(FileObject fileObject,
                                       Integer maxParts,
                                       Integer partNumberMarker,
                                       String uploadId,
                                       Multimap<String, String> extraHeaders) throws Exception {
        return listParts(fileObject, maxParts, partNumberMarker, uploadId, extraHeaders, null);
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
    public ListPartsResponse listParts(FileObject fileObject,
                                       Integer maxParts,
                                       Integer partNumberMarker,
                                       String uploadId,
                                       Multimap<String, String> extraHeaders,
                                       Multimap<String, String> extraQueryParams) throws Exception {
        return minioClient.listParts(fileObject, maxParts, partNumberMarker, uploadId, extraHeaders, extraQueryParams);
    }

    /**
     * 查询对象信息
     *
     * @param fileObject 文件对象
     *
     * @return 响应内容
     *
     * @throws Exception 查询对象信息错误时抛出
     */
    public StatObjectResponse statObject(FileObject fileObject) throws Exception {
        return statObject(fileObject, null);

    }

    /**
     * 查询对象信息
     *
     * @param fileObject 文件对象
     * @param matchEtag etag 值
     *
     * @return 响应内容
     *
     * @throws Exception 查询对象信息错误时抛出
     */
    public StatObjectResponse statObject(FileObject fileObject, String matchEtag) throws Exception {
        return statObject(fileObject, matchEtag, null);

    }

    /**
     * 查询对象信息
     *
     * @param fileObject 文件对象
     * @param matchEtag etag 值
     * @param headers 头信息
     *
     * @return 响应内容
     *
     * @throws Exception 查询对象信息错误时抛出
     */
    public StatObjectResponse statObject(FileObject fileObject, String matchEtag, Map<String, String> headers) throws Exception {
        return statObject(fileObject, matchEtag, headers, null);

    }

    /**
     * 查询对象信息
     *
     * @param fileObject 文件对象
     * @param matchEtag etag 值
     * @param headers 头信息
     * @param queryParams 查询参数信息
     *
     * @return 响应内容
     *
     * @throws Exception 查询对象信息错误时抛出
     */
    public StatObjectResponse statObject(FileObject fileObject, String matchEtag, Map<String, String> headers, Map<String, String> queryParams) throws Exception {
        ObjectConditionalReadArgs.Builder<StatObjectArgs.Builder, StatObjectArgs> statObjectArgs = StatObjectArgs
                .builder()
                .region(fileObject.getRegion())
                .bucket(fileObject.getBucketName())
                .object(fileObject.getObjectName());

        if (VersionFileObject.class.isAssignableFrom(fileObject.getClass())) {
            VersionFileObject object = Casts.cast(fileObject);
            statObjectArgs.versionId(object.getVersionId());
        }

        if (StringUtils.isNotEmpty(matchEtag)) {
            statObjectArgs.matchETag(matchEtag);
        }
        /*if (StringUtils.isNotEmpty(notMatchEtag)) {
            statObjectArgs.notMatchETag(notMatchEtag);
        }*/
        if (MapUtils.isNotEmpty(headers)) {
            statObjectArgs.extraHeaders(headers);
        }
        if (MapUtils.isNotEmpty(queryParams)) {
            statObjectArgs.extraQueryParams(queryParams);
        }

        return minioClient.statObject(statObjectArgs.build()).get();
    }

    /**
     * 获取 minio 客户端
     *
     * @return minio 客户端
     */
    public EnhanceMinioClient getMinioClient() {
        return minioClient;
    }
}
