package com.github.dactiv.framework.minio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dactiv.framework.minio.config.MinioProperties;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

/**
 * minio 自动配置类
 *
 * @author maurice.chen
 */
@Configuration
@EnableConfigurationProperties(MinioProperties.class)
@ConditionalOnProperty(prefix = "dactiv.minio", value = "enabled", matchIfMissing = true)
public class MinioAutoConfiguration {

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    /**
     * minio 客户端
     *
     * @param minioProperties mini 模版
     *
     * @return mini 客户端
     */
    @Bean
    public MinioClient minioClient(MinioProperties minioProperties) {
        return MinioClient
                .builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }

    /**
     * minio 模版配置
     *
     * @param minioClient minio 客户端
     *
     * @return minio 模版
     */
    @Bean
    @ConditionalOnMissingBean(MinioTemplate.class)
    public MinioTemplate minioTemplate(MinioClient minioClient) {
        return new MinioTemplate(minioClient, Objects.isNull(objectMapper) ? new ObjectMapper() : objectMapper);
    }

    /**
     * minio 操作终端
     *
     * @param minioTemplate minio 模版配置
     *
     * @return 文件终端
     */
    /*@Bean
    public MinioEndpoint minioEndpoint(MinioTemplate minioTemplate) {
        return new MinioEndpoint(minioTemplate);
    }*/

    /**
     * 下载文件终端
     *
     * @param minioTemplate minio 模版配置
     *
     * @return 文件终端
     */
    /*@Bean
    public DownloadEndpoint downloadEndpoint(MinioTemplate minioTemplate) {
        return new DownloadEndpoint(minioTemplate);
    }*/
}
