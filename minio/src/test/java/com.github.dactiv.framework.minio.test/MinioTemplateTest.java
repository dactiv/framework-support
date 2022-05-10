package com.github.dactiv.framework.minio.test;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.minio.MinioTemplate;
import com.github.dactiv.framework.minio.data.Bucket;
import com.github.dactiv.framework.minio.data.FileObject;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * minio 模版单元测试
 *
 * @author maurice.chen
 */
@SpringBootTest
public class MinioTemplateTest {

    public static final String DEFAULT_TEST_BUCKET = "minio.test";
    public static final String DEFAULT_UPPER_CASE_TEST_BUCKET = "minio.upper.case.test".toUpperCase();

    public static final String DEFAULT_TEST_FILE = "classpath:/787963DE-9662-4245-ABC7-8E6673F114F5.jpeg";

    @Autowired
    private MinioTemplate minioTemplate;

    private final ResourceLoader resourceLoader = new DefaultResourceLoader();

    @AfterEach
    public void uninstall() throws Exception {
        Bucket bucket = Bucket.of(DEFAULT_TEST_BUCKET);

        if (minioTemplate.isBucketExist(bucket)) {

            Iterable<Result<Item>> iterable = minioTemplate.getMinioClient().listObjects(ListObjectsArgs.builder().bucket(bucket.getBucketName()).build());

            for (Result<Item> r : iterable) {
                Item item = r.get();
                minioTemplate.deleteObject(FileObject.of(bucket, item.objectName()));
            }

            minioTemplate.deleteBucket(bucket);
        }
    }

    @Test
    void makeBucketIfNotExists() throws Exception {

        String randomRegion = RandomStringUtils.randomAlphabetic(6);

        Bucket bucket = Bucket.of(DEFAULT_TEST_BUCKET);

        Assertions.assertFalse(minioTemplate.makeBucketIfNotExists(bucket));
        Assertions.assertTrue(minioTemplate.makeBucketIfNotExists(bucket));

        Bucket upperCaseBucket = Bucket.of(DEFAULT_UPPER_CASE_TEST_BUCKET);
        Assertions.assertFalse(minioTemplate.makeBucketIfNotExists(upperCaseBucket));
        minioTemplate.deleteBucket(upperCaseBucket);

        Assertions.assertTrue(minioTemplate.makeBucketIfNotExists(Bucket.of(DEFAULT_TEST_BUCKET, randomRegion)));

        try {
            minioTemplate.makeBucketIfNotExists(Bucket.of("Minio$$$Error.Test"));
        } catch (Exception e) {
            Assertions.assertTrue(IllegalArgumentException.class.isAssignableFrom(e.getClass()));
        }
    }

    @Test
    void getPresignedObjectUrl() throws Exception {

        Bucket bucket = Bucket.of(DEFAULT_TEST_BUCKET);
        minioTemplate.makeBucketIfNotExists(bucket);

        FileObject file = FileObject.of(bucket, "presignedObject");
        minioTemplate.upload(
                file,
                resourceLoader.getResource(DEFAULT_TEST_FILE).getInputStream(),
                IOUtils.toByteArray(resourceLoader.getResource(DEFAULT_TEST_FILE).getInputStream()).length,
                MediaType.IMAGE_JPEG_VALUE
        );

        String url = minioTemplate.getPresignedObjectUrl(file, TimeProperties.of(1, TimeUnit.SECONDS));

        Assertions.assertTrue(StringUtils.isNotEmpty(url));
    }

    @Test
    void delete() throws Exception {
        Bucket bucket = Bucket.of(DEFAULT_TEST_BUCKET);
        minioTemplate.makeBucketIfNotExists(bucket);

        FileObject deleteFile = FileObject.of(bucket, "delete");

        minioTemplate.upload(
                deleteFile,
                resourceLoader.getResource(DEFAULT_TEST_FILE).getInputStream(),
                IOUtils.toByteArray(resourceLoader.getResource(DEFAULT_TEST_FILE).getInputStream()).length,
                MediaType.IMAGE_JPEG_VALUE
        );

        ListObjectsArgs listObjectsArgs = ListObjectsArgs
                .builder()
                .bucket(bucket.getBucketName())
                .prefix("delete")
                .build();

        Iterable<Result<Item>> iterable = minioTemplate.getMinioClient().listObjects(listObjectsArgs);
        Assertions.assertEquals(iterable.iterator().next().get().objectName(), "delete");

        minioTemplate.deleteObject(deleteFile);
        iterable = minioTemplate.getMinioClient().listObjects(listObjectsArgs);
        Assertions.assertFalse(iterable.iterator().hasNext());

        minioTemplate.upload(
                deleteFile,
                resourceLoader.getResource(DEFAULT_TEST_FILE).getInputStream(),
                IOUtils.toByteArray(resourceLoader.getResource(DEFAULT_TEST_FILE).getInputStream()).length,
                MediaType.IMAGE_JPEG_VALUE
        );

        iterable = minioTemplate.getMinioClient().listObjects(listObjectsArgs);
        Assertions.assertEquals(iterable.iterator().next().get().objectName(), "delete");

        minioTemplate.deleteObject(deleteFile, true);
        Assertions.assertFalse(minioTemplate.isBucketExist(bucket));
    }

    @Test
    void copy() throws Exception {
        Bucket bucket = Bucket.of(DEFAULT_TEST_BUCKET);
        minioTemplate.makeBucketIfNotExists(bucket);

        FileObject copyFile = FileObject.of(bucket, "copy");

        minioTemplate.upload(
                copyFile,
                resourceLoader.getResource(DEFAULT_TEST_FILE).getInputStream(),
                IOUtils.toByteArray(resourceLoader.getResource(DEFAULT_TEST_FILE).getInputStream()).length,
                MediaType.IMAGE_JPEG_VALUE
        );

        ListObjectsArgs copy = ListObjectsArgs
                .builder()
                .bucket(bucket.getBucketName())
                .prefix("copy")
                .build();

        Iterable<Result<Item>> iterable = minioTemplate.getMinioClient().listObjects(copy);
        Assertions.assertEquals(iterable.iterator().next().get().objectName(), "copy");

        ObjectWriteResponse response = minioTemplate.copyObject(copyFile, FileObject.of(bucket, "newCopy"));

        ListObjectsArgs newCopy = ListObjectsArgs
                .builder()
                .bucket(bucket.getBucketName())
                .prefix("newCopy")
                .build();

        iterable = minioTemplate.getMinioClient().listObjects(newCopy);
        Assertions.assertEquals(iterable.iterator().next().get().objectName(), response.object());
    }

    @Test
    void readAndWriteJsonValue() throws Exception {
        CacheProperties cacheProperties = new CacheProperties("minio:test:json", TimeProperties.ofDay(1));
        FileObject json = FileObject.of(DEFAULT_TEST_BUCKET, "json");
        ObjectWriteResponse response = minioTemplate.writeJsonValue(json, cacheProperties);

        ListObjectsArgs jsonArgs = ListObjectsArgs
                .builder()
                .bucket(json.getBucketName())
                .prefix("json")
                .build();

        Iterable<Result<Item>> iterable = minioTemplate.getMinioClient().listObjects(jsonArgs);
        Assertions.assertEquals(iterable.iterator().next().get().objectName(), response.object());

        CacheProperties jsonValue = minioTemplate.readJsonValue(json, CacheProperties.class);

        Assertions.assertEquals(jsonValue.getName(), cacheProperties.getName());
        Assertions.assertEquals(jsonValue.getExpiresTime().getValue(), cacheProperties.getExpiresTime().getValue());
        Assertions.assertEquals(jsonValue.getExpiresTime().getUnit(), cacheProperties.getExpiresTime().getUnit());
    }

}
