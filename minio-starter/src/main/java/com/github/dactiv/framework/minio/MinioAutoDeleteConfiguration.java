package com.github.dactiv.framework.minio;

import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.commons.minio.ExpirableBucket;
import com.github.dactiv.framework.commons.minio.FileObject;
import com.github.dactiv.framework.minio.config.AutoDeleteProperties;
import io.minio.ListObjectsArgs;
import io.minio.Result;
import io.minio.messages.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 自动删除配置
 *
 * @author maurice.chen
 */
@EnableScheduling
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(MinioAutoConfiguration.class)
@EnableConfigurationProperties(AutoDeleteProperties.class)
@ConditionalOnProperty(prefix = "dactiv.minio", value = "enabled", matchIfMissing = true)
public class MinioAutoDeleteConfiguration implements SchedulingConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MinioAutoDeleteConfiguration.class);

    private final MinioTemplate minioTemplate;

    private final AutoDeleteProperties autoDeleteProperties;

    MinioAutoDeleteConfiguration(MinioTemplate minioTemplate, AutoDeleteProperties autoDeleteProperties) {
        this.minioTemplate = minioTemplate;
        this.autoDeleteProperties = autoDeleteProperties;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        // FIXME 怎么做成可动态的配置
        taskRegistrar.addCronTask(() -> {

            if (Objects.isNull(autoDeleteProperties.getExpiration())) {
                return;
            }

            for (ExpirableBucket bucket : autoDeleteProperties.getExpiration()) {

                TimeProperties time = bucket.getExpirationTime();

                if (Objects.isNull(time)) {
                    LOGGER.warn("找不到 [" + bucket.getBucketName() + "] 桶的自动删除时间配置。");
                    continue;
                }

                ListObjectsArgs listObjectsArgs = ListObjectsArgs
                        .builder()
                        .region(bucket.getRegion())
                        .bucket(bucket.getBucketName())
                        .build();

                Iterable<Result<Item>> iterable = minioTemplate.getMinioClient().listObjects(listObjectsArgs);

                for (Result<Item> result : iterable) {

                    try {

                        Item item = result.get();

                        if (item.isDeleteMarker()) {
                            continue;
                        }

                        LocalDateTime expirationTime = item
                                .lastModified()
                                .toLocalDateTime()
                                .plus(time.getValue(), time.toChronoUnit());

                        if (LocalDateTime.now().isAfter(expirationTime)) {
                            minioTemplate.deleteObject(FileObject.of(bucket, item.objectName()));
                            LOGGER.info("删除桶 [" + bucket.getBucketName() + "] 的 [" + item.objectName() + "] 对象");
                        }

                    } catch (Exception e) {
                        LOGGER.error("获取对象失败", e);
                    }

                }
            }

        }, autoDeleteProperties.getCron());
    }
}
