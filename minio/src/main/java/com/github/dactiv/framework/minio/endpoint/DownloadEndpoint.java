package com.github.dactiv.framework.minio.endpoint;

import com.github.dactiv.framework.minio.MinioTemplate;
import com.github.dactiv.framework.minio.data.VersionFileObject;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.InputStream;

/**
 * 文件下载 web 终端
 *
 * @author maurice.chen
 */
@RestControllerEndpoint(id = "download")
public class DownloadEndpoint {

    /**
     * 附件名称头信息
     */
    public static final String DEFAULT_ATTACHMENT_NAME = "attachment;filename=";

    /**
     * minio 模版
     */
    private final MinioTemplate minioTemplate;

    /**
     * 文件下载 web 终端
     *
     * @param minioTemplate minio 模版
     */
    public DownloadEndpoint(MinioTemplate minioTemplate) {
        this.minioTemplate = minioTemplate;
    }

    /**
     * 下载文件
     *
     * @param object 版本文件对象描述
     * @return 文件字节数组
     *
     * @throws Exception 获取文件错误时抛出
     */
    @GetMapping
    public ResponseEntity<byte[]> download(@RequestParam VersionFileObject object) throws Exception {
        InputStream is = minioTemplate.getObject(object);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData(DEFAULT_ATTACHMENT_NAME, object.getObjectName());
        return new ResponseEntity<>(IOUtils.toByteArray(is), headers, HttpStatus.OK);
    }
}
