package com.github.dactiv.framework.spring.security.audit;


import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.spring.security.audit.elasticsearch.ElasticsearchAuditEventRepository;
import com.github.dactiv.framework.spring.security.audit.mongo.MongoAuditEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.actuate.audit.InMemoryAuditEventRepository;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.*;

/**
 * 审计自动配置
 *
 * @author maurice.chen
 */
@Configuration
@EnableConfigurationProperties(AuditProperties.class)
@Import(AuditConfiguration.AuditImportSelector.class)
@ConditionalOnProperty(prefix = "spring.security.support", value = "enabled", matchIfMissing = true)
public class AuditConfiguration {

    private final static Logger LOGGER = LoggerFactory.getLogger(AuditConfiguration.class);

    private static final Map<AuditType, Class<?>> MAPPINGS;

    static {
        Map<AuditType, Class<?>> mappings = new LinkedHashMap<>();

        mappings.put(AuditType.Memory, InMemoryAuditConfiguration.class);
        mappings.put(AuditType.Elasticsearch, ElasticsearchAuditConfiguration.class);
        mappings.put(AuditType.Mongo, MongoAuditConfiguration.class);

        MAPPINGS = Collections.unmodifiableMap(mappings);
    }

    public static class AuditImportSelector implements ImportSelector {

        @Override
        public String[] selectImports(AnnotationMetadata importingClassMetadata) {
            List<String> imports = new LinkedList<>();

            for (AuditType auditType : AuditType.values()) {
                if (MAPPINGS.containsKey(auditType)) {
                    imports.add(MAPPINGS.get(auditType).getName());
                }
            }
            return imports.toArray(new String[0]);
        }
    }

    public static class AuditImportSelectorCondition extends SpringBootCondition {

        @Override
        public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {

            String sourceClass = "";

            if (ClassMetadata.class.isAssignableFrom(metadata.getClass())) {

                ClassMetadata classMetadata = Casts.cast(metadata);

                sourceClass = classMetadata.getClassName();

            }

            ConditionMessage.Builder message = ConditionMessage.forCondition("Audit", sourceClass);
            Environment environment = context.getEnvironment();

            try {

                BindResult<AuditType> specified = Binder.get(environment).bind("spring.security.support.audit.type", AuditType.class);

                if (AnnotationMetadata.class.isAssignableFrom(metadata.getClass())) {

                    AnnotationMetadata annotationMetadata = Casts.cast(metadata);

                    AuditType required = AuditConfiguration.getType(annotationMetadata.getClassName());

                    if (!specified.isBound()) {
                        return AuditType.Memory.equals(required) ?
                                ConditionOutcome.match(message.because(AuditType.Memory + " audit type")) :
                                ConditionOutcome.noMatch(message.because("unknown audit type"));
                    } else if (required.equals(specified.get())) {
                        return ConditionOutcome.match(message.because(specified.get() + " audit type"));
                    }

                }

            } catch (BindException ex) {
                LOGGER.warn("在执行审计内容自动根据类型注入时出现错误", ex);
            }

            return ConditionOutcome.noMatch(message.because("unknown audit type"));

        }

    }

    public static AuditType getType(String configurationClassName) {
        for (Map.Entry<AuditType, Class<?>> entry : MAPPINGS.entrySet()) {
            if (entry.getValue().getName().equals(configurationClassName)) {
                return entry.getKey();
            }
        }
        String msg = "unknown configuration class" + configurationClassName;
        throw new IllegalStateException(msg);
    }

    /**
     * 内存形式的审计仓库配置
     *
     * @author maurice.chen
     */
    @Conditional(AuditImportSelectorCondition.class)
    @ConditionalOnMissingBean(AuditEventRepository.class)
    @ConditionalOnClass(InMemoryAuditEventRepository.class)
    @EnableConfigurationProperties(SecurityProperties.class)
    public static class InMemoryAuditConfiguration {

        @Bean
        public AuditEventRepository auditEventRepository() {
            return new InMemoryAuditEventRepository();
        }
    }

    /**
     * Mongo 审计仓库配置
     *
     * @author maurice.chen
     */
    @Conditional(AuditImportSelectorCondition.class)
    @ConditionalOnClass(MongoAuditEventRepository.class)
    @ConditionalOnMissingBean(AuditEventRepository.class)
    @EnableConfigurationProperties(SecurityProperties.class)
    public static class MongoAuditConfiguration {

        @Bean
        public PageAuditEventRepository auditEventRepository(MongoTemplate mongoTemplate,
                                                             SecurityProperties securityProperties) {

            return new MongoAuditEventRepository(mongoTemplate, securityProperties);

        }
    }

    /**
     * Elasticsearch 审计仓库配置
     *
     * @author maurice.chen
     */
    @Conditional(AuditImportSelectorCondition.class)
    @ConditionalOnMissingBean(AuditEventRepository.class)
    @EnableConfigurationProperties(SecurityProperties.class)
    @ConditionalOnClass(ElasticsearchAuditEventRepository.class)
    public static class ElasticsearchAuditConfiguration {

        @Bean
        public PageAuditEventRepository auditEventRepository(ElasticsearchRestTemplate elasticsearchRestTemplate,
                                                             SecurityProperties securityProperties) {

            return new ElasticsearchAuditEventRepository(elasticsearchRestTemplate, securityProperties);

        }
    }
}