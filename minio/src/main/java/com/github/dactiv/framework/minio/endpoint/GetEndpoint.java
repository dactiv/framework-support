package com.github.dactiv.framework.minio.endpoint;

import com.github.dactiv.framework.minio.MinioTemplate;
import com.github.dactiv.framework.minio.data.VersionFileObject;
import io.minio.GetObjectResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Objects;

/**
 * 获取文件终端
 *
 * @author maurice.chen
 */
@RestControllerEndpoint(id = "get")
public class GetEndpoint {

    /**
     * minio 模版
     */
    private final MinioTemplate minioTemplate;

    /**
     * 获取文件终端
     *
     * @param minioTemplate minio 模版
     */
    public GetEndpoint(MinioTemplate minioTemplate) {
        this.minioTemplate = minioTemplate;
    }

    /**
     * 获取文件
     *
     * @param object 版本文件对象描述
     *
     * @return 文件字节数组
     *
     * @throws Exception 获取文件错误时抛出
     */
    @GetMapping
    public ResponseEntity<byte[]> get(@RequestParam VersionFileObject object,
                                      @RequestParam(required = false) String contentType) throws Exception {
        GetObjectResponse is = minioTemplate.getObject(object);

        HttpHeaders headers = new HttpHeaders();
        if (StringUtils.isNotEmpty(contentType)) {
            headers.setContentType(MediaType.parseMediaType(contentType));
        } else {
            headers.setContentType(MediaType.parseMediaType(Objects.requireNonNull(is.headers().get(""))));
        }

        return new ResponseEntity<>(IOUtils.toByteArray(is), headers, HttpStatus.OK);
    }
}
