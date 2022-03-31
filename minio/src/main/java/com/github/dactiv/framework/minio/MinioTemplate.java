package com.github.dactiv.framework.minio;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.minio.data.Bucket;
import com.github.dactiv.framework.minio.data.FileObject;
import com.github.dactiv.framework.minio.data.ObjectItem;
import com.github.dactiv.framework.minio.data.VersionFileObject;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * minio 模版
 *
 * @author maurice.chen
 */
public class MinioTemplate {

    /**
     * minio 客户端
     */
    private final MinioClient minioClient;

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
    public MinioTemplate(MinioClient minioClient, ObjectMapper objectMapper) {
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
            minioClient.makeBucket(makeBucketArgs);
        }

        return found;
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

        return minioClient.bucketExists(builder);
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

        return minioClient.putObject(args);

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

        List<ObjectItem> result = new LinkedList<>();
        Iterable<Result<Item>> results = minioClient.listObjects(args);

        for (Result<Item> itemResult : results) {
            Item item = itemResult.get();
            result.add(new ObjectItem(item));
        }

        return result;
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

        RemoveObjectArgs.Builder args = RemoveObjectArgs
                .builder()
                .bucket(bucketName)
                .region(fileObject.getRegion())
                .object(fileObject.getObjectName());

        if (VersionFileObject.class.isAssignableFrom(fileObject.getClass())) {
            VersionFileObject version = Casts.cast(fileObject);
            args.versionId(version.getVersionId());
        }

        minioClient.removeObject(args.build());

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

        return minioClient.getObject(getObjectArgs.build());
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

        return minioClient.copyObject(args.build());
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

        boolean exist = minioClient.bucketExists(existsArgs);

        if (exist) {
            RemoveBucketArgs removeBucketArgs = RemoveBucketArgs
                    .builder()
                    .bucket(name)
                    .region(bucket.getRegion())
                    .build();

            minioClient.removeBucket(removeBucketArgs);
        }

    }

    /**
     * 获取 minio 客户端
     *
     * @return minio 客户端
     */
    public MinioClient getMinioClient() {
        return minioClient;
    }
}
