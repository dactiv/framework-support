package com.github.dactiv.framework.mybatis.plus;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.github.dactiv.framework.mybatis.MybatisAutoConfiguration;
import com.github.dactiv.framework.mybatis.interceptor.audit.OperationDataTraceRepository;
import com.github.dactiv.framework.mybatis.plus.audit.MybatisPlusOperationDataTraceRepository;
import com.github.dactiv.framework.mybatis.plus.interceptor.LastModifiedDateInnerInterceptor;
import com.github.dactiv.framework.spring.web.query.QueryGenerator;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * mybatis-plus 自动配置实现
 *
 * @author maurice.chen
 */
@Configuration
@AutoConfigureBefore(MybatisAutoConfiguration.class)
@ConditionalOnProperty(prefix = "dactiv.mybatis.plus", value = "enabled", matchIfMissing = true)
public class MybatisPlusAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(QueryGenerator.class)
    public MybatisPlusQueryGenerator<?> mybatisPlusQueryGenerator() {
        return new MybatisPlusQueryGenerator<>();
    }

    @Bean
    @ConditionalOnMissingBean(OperationDataTraceRepository.class)
    @ConditionalOnProperty(prefix = "dactiv.mybatis.operation-data-trace", value = "enabled", matchIfMissing = true)
    public MybatisPlusOperationDataTraceRepository mybatisPlusOperationDataTraceRepository() {
        return new MybatisPlusOperationDataTraceRepository();
    }

    @Bean
    @ConditionalOnMissingBean(MybatisPlusInterceptor.class)
    public MybatisPlusInterceptor mybatisPlusInterceptor() {

        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor(true));
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        interceptor.addInnerInterceptor(new LastModifiedDateInnerInterceptor(true));
        return interceptor;
    }

}
