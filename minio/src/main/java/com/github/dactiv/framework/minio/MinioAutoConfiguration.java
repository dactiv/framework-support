package com.github.dactiv.framework.minio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dactiv.framework.minio.config.MinioProperties;
import io.minio.MinioClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * minio 自动配置类
 *
 * @author maurice.chen
 */
@Configuration
@EnableConfigurationProperties(MinioProperties.class)
@ConditionalOnProperty(prefix = "dactiv.minio", value = "enabled", matchIfMissing = true)
public class MinioAutoConfiguration {

    /**
     * minio 客户端
     *
     * @param minioProperties mini 模版
     *
     * @return mini 客户端
     */
    @Bean
    public EnhanceMinioClient minioClient(MinioProperties minioProperties) {
        return new EnhanceMinioClient(MinioClient
                .builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build());
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
    public MinioTemplate minioTemplate(EnhanceMinioClient minioClient, ObjectProvider<ObjectMapper> objectMapper) {
        return new MinioTemplate(minioClient, objectMapper.getIfUnique(ObjectMapper::new));
    }

}
