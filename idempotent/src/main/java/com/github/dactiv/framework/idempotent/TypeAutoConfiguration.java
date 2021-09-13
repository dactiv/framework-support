package com.github.dactiv.framework.idempotent;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.idempotent.advisor.IdempotentInterceptor;
import com.github.dactiv.framework.idempotent.advisor.IdempotentPointcutAdvisor;
import com.github.dactiv.framework.idempotent.interceptor.IdempotentWebHandlerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;

import java.util.*;

/**
 * 类型自动配置
 *
 * @author maurice.chen
 */
@Configuration
@Import(TypeAutoConfiguration.IdempotentImportSelector.class)
@EnableConfigurationProperties(IdempotentProperties.class)
public class TypeAutoConfiguration {

    private final static Logger LOGGER = LoggerFactory.getLogger(IdempotentAutoConfiguration.class);

    private static final Map<IdempotentType, Class<?>> MAPPINGS;

    static {
        Map<IdempotentType, Class<?>> mappings = new LinkedHashMap<>();

        mappings.put(IdempotentType.Advisor, IdempotentAdvisorConfiguration.class);
        mappings.put(IdempotentType.Interceptor, IdempotentInterceptorConfiguration.class);

        MAPPINGS = Collections.unmodifiableMap(mappings);
    }

    public static class IdempotentImportSelector implements ImportSelector {

        @Override
        public String[] selectImports(AnnotationMetadata importingClassMetadata) {
            List<String> imports = new LinkedList<>();

            for (IdempotentType auditType : IdempotentType.values()) {
                if (MAPPINGS.containsKey(auditType)) {
                    imports.add(MAPPINGS.get(auditType).getName());
                }
            }
            return imports.toArray(new String[0]);
        }
    }

    public static class IdempotentImportSelectorCondition extends SpringBootCondition {

        @Override
        public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {

            String sourceClass = "";

            if (ClassMetadata.class.isAssignableFrom(metadata.getClass())) {

                ClassMetadata classMetadata = Casts.cast(metadata);

                sourceClass = classMetadata.getClassName();

            }

            ConditionMessage.Builder message = ConditionMessage.forCondition("Idempotent", sourceClass);
            Environment environment = context.getEnvironment();

            try {

                BindResult<IdempotentType> specified = Binder
                        .get(environment)
                        .bind("dactiv.idempotent.type", IdempotentType.class);

                if (AnnotationMetadata.class.isAssignableFrom(metadata.getClass())) {

                    AnnotationMetadata annotationMetadata = Casts.cast(metadata);

                    IdempotentType required = getType(annotationMetadata.getClassName());

                    if (!specified.isBound()) {
                        return IdempotentType.Advisor.equals(required) ?
                                ConditionOutcome.match(message.because(IdempotentType.Advisor + " idempotent type")) :
                                ConditionOutcome.noMatch(message.because("unknown idempotent type"));
                    } else if (required.equals(specified.get())) {
                        return ConditionOutcome.match(message.because(specified.get() + " idempotent type"));
                    }

                }

            } catch (BindException ex) {
                LOGGER.warn("在执行幂等内容自动根据类型注入时出现错误", ex);
            }

            return ConditionOutcome.noMatch(message.because("unknown idempotent type"));

        }

    }

    public static IdempotentType getType(String configurationClassName) {
        for (Map.Entry<IdempotentType, Class<?>> entry : MAPPINGS.entrySet()) {
            if (entry.getValue().getName().equals(configurationClassName)) {
                return entry.getKey();
            }
        }
        String msg = "unknown configuration class" + configurationClassName;
        throw new IllegalStateException(msg);
    }

    /**
     * 切面形式的幂等性配置
     *
     * @author maurice.chen
     */
    @Conditional(IdempotentImportSelectorCondition.class)
    @ConditionalOnMissingBean(IdempotentPointcutAdvisor.class)
    public static class IdempotentAdvisorConfiguration {

        @Bean
        IdempotentPointcutAdvisor idempotentPointcutAdvisor(IdempotentInterceptor idempotentInterceptor) {
            return new IdempotentPointcutAdvisor(idempotentInterceptor);
        }
    }

    /**
     * spring mvc 拦截器形式的幂等性配置
     *
     * @author maurice.chen
     */
    @Conditional(IdempotentImportSelectorCondition.class)
    @ConditionalOnMissingBean(IdempotentWebHandlerInterceptor.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public static class IdempotentInterceptorConfiguration {

        @Bean
        IdempotentWebHandlerInterceptor idempotentHandlerInterceptor(IdempotentInterceptor idempotentInterceptor) {
            return new IdempotentWebHandlerInterceptor(idempotentInterceptor);
        }
    }
}
