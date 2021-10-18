package com.github.dactiv.framework.minio.endpoint;


import com.github.dactiv.framework.commons.ReflectionUtils;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.minio.MinioTemplate;
import com.github.dactiv.framework.minio.data.Bucket;
import com.github.dactiv.framework.minio.data.FileObject;
import com.github.dactiv.framework.minio.data.VersionFileObject;
import io.minio.ObjectWriteResponse;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 文件上传 web 终端
 *
 * @author maurice.chen
 */
@RestControllerEndpoint(id = "upload")
public class UploadEndpoint {

    /**
     * 默认忽略的响应字段
     */
    public static final String[] DEFAULT_IGNORE_FIELDS = {"headers"};

    /**
     * 忽略的响应字段
     */
    private List<String> ignoreFields = Arrays.asList(DEFAULT_IGNORE_FIELDS);

    /**
     * minio 模版
     */
    private final MinioTemplate minioTemplate;

    /**
     * 文件上传 web 终端
     *
     * @param minioTemplate minio 模版
     */
    public UploadEndpoint(MinioTemplate minioTemplate) {
        this.minioTemplate = minioTemplate;
    }

    /**
     * 文件上传 web 终端
     *
     * @param ignoreFields 忽略的响应字段
     * @param minioTemplate minio 模版
     */
    public UploadEndpoint(List<String> ignoreFields, MinioTemplate minioTemplate) {
        this.ignoreFields = ignoreFields;
        this.minioTemplate = minioTemplate;
    }

    /**
     * 上传文件
     *
     * @param file 文件信息
     * @param bucket 桶描述
     * @param versionId 版本号
     *
     * @return rest 结果集
     *
     * @throws Exception 上传错误抛出
     */
    @PostMapping
    public RestResult<Map<String, Object>> upload(@RequestParam("file") MultipartFile file,
                                                  @RequestParam Bucket bucket,
                                                  @RequestParam(required = false) String versionId) throws Exception {

        FileObject fileObject = FileObject.of(bucket, file.getOriginalFilename());

        if (StringUtils.isNotEmpty(versionId)) {
            fileObject = VersionFileObject.of(fileObject, versionId);
        }

        ObjectWriteResponse response = minioTemplate.upload(
                fileObject,
                file.getInputStream(),
                file.getSize(),
                file.getContentType()
        );

        Map<String, Object> result = convertFields(response, response.getClass(), ignoreFields.toArray(new String[0]));

        return RestResult.ofSuccess("上传完成", result);
    }

    /**
     * 转换目标对象和目标类的字段为 map
     *
     * @param target       目标对象
     * @param targetClass  目标类
     * @param ignoreFields 要忽略的字段名
     *
     * @return map 对象
     */
    private Map<String, Object> convertFields(Object target, Class<?> targetClass, String... ignoreFields) {

        Map<String, Object> result = new LinkedHashMap<>();

        List<Field> fieldList = Arrays.asList(targetClass.getDeclaredFields());

        fieldList
                .stream()
                .filter(field -> !ArrayUtils.contains(ignoreFields, field.getName()))
                .forEach(field -> result.put(field.getName(), getFieldToStringValue(target, field)));

        if (Objects.nonNull(targetClass.getSuperclass())) {
            result.putAll(convertFields(target, targetClass.getSuperclass(), ignoreFields));
        }

        return result;
    }

    /**
     * 获取字段的 toString 值
     *
     * @param target 目标对象
     * @param field  字段
     *
     * @return 值
     */
    private Object getFieldToStringValue(Object target, Field field) {
        Object value = ReflectionUtils.getFieldValue(target, field);

        if (Objects.isNull(value)) {
            return null;
        }

        return String.class.isAssignableFrom(value.getClass()) ? value : value.toString();
    }
}
