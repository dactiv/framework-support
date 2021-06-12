package com.github.dactiv.framework.nacos.task;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.cloud.nacos.parser.NacosDataParserHandler;
import com.alibaba.nacos.api.config.listener.AbstractSharedListener;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.nacos.task.annotation.NacosCronScheduled;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.PropertiesBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.PropertySource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.ScheduledMethodRunnable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringValueResolver;
import org.springframework.util.SystemPropertyUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

/**
 * nacos 动态 cron 调度监听器实现，用于存在自动调度任务时，通过该类来进行 cron 任务调动的动态改变
 *
 * @author maurice.chen
 */
public class NacosCronScheduledListener implements SchedulingConfigurer, BeanPostProcessor, EmbeddedValueResolverAware, ApplicationContextAware, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(NacosCronScheduledListener.class);

    /**
     * 找不到 NacosCronScheduled 注解的类，用于在 postProcessAfterInitialization 寻找
     * NacosCronScheduled 注解时，直接跳过该类而使用
     */
    private final List<Class<?>> notMatchClassList = new LinkedList<>();

    /**
     * nacos 配置管理类
     */
    private final NacosConfigManager nacosConfigManager;

    /**
     * 任务调度注册类，用于动态刷新 NacosCronScheduled 注解的调度使用
     */
    private ScheduledTaskRegistrar scheduledTaskRegistrar;

    /**
     * spring el 变量解析器
     */
    private StringValueResolver embeddedValueResolver;

    /**
     * spring 应用上下文，用于获取 spring.application.name 配置使用
     */
    private ApplicationContext applicationContext;

    /**
     * 当前调度缓存
     */
    private static final ConcurrentHashMap<List<MatchEvaluation>, NacosCronScheduledInfo> CACHE = new ConcurrentHashMap<>();

    public NacosCronScheduledListener(NacosConfigManager nacosConfigManager) {
        this.nacosConfigManager = nacosConfigManager;
    }

    /**
     * 当 nacos 配置发生变化时，进入此方法
     *
     * @param dataId 数据 id
     * @param configInfo 更新的配置内容
     */
    protected void configReceive(String dataId, String configInfo) throws IOException {

        // 获取后缀名
        String fileExtension = StringUtils.substringAfterLast(dataId, Casts.DEFAULT_POINT_SYMBOL);

        // 通过 configInfo 创建配置信息
        List<PropertySource<?>> properties = NacosDataParserHandler.getInstance().parseNacosData(dataId, configInfo, fileExtension);

        // 创建临时记录改变了调度内容的集合对象
        List<NacosCronScheduledInfo> changeScheduledInfos = new LinkedList<>();

        PropertySource<?> propertySource = properties
                .stream()
                .filter(p -> p.getName().equals(dataId))
                .findFirst()
                .orElseThrow();

        // 通过缓存内容去匹配有哪些调度修改了值，并添加到 changeScheduledInfos 中
        CACHE.forEach((matchEvaluations, target) -> {

            List<MatchEvaluation> result = matchEvaluations
                    .stream()
                    // 匹配等于条件的值
                    .filter(m -> propertySource.containsProperty(m.getMatch().toString()))
                    .collect(Collectors.toList());

            if (result.stream().anyMatch(m -> m.evaluation(propertySource.getProperty(m.getMatch().toString()), target))) {
                changeScheduledInfos.add(target);
            }

        });

        // 循环更新调度内容
        changeScheduledInfos.forEach(c -> {

            LOGGER.info(
                    "名称为 [{}] 的 cron 调度发生变化, 当前 cron 表达式为: {}, 时区为: {}",
                    c.getName(),
                    c.getExpression(),
                    c.getTimeZone()
            );
            // 取消当前调度
            c.getScheduledTask().cancel();
            // 如果不等于注视字符，重新创建调度并加入到调度任务中，否则直接取消。
            if (!ScheduledTaskRegistrar.CRON_DISABLED.equals(c.getExpression())) {
                // 开启新的调度
                ScheduledTask scheduledTask = scheduledTaskRegistrar.scheduleCronTask(c.createCronTask());
                // 记录当前调度内容，用于下次更新时可以直接通过该属性取消
                c.setScheduledTask(scheduledTask);
            } else {
                LOGGER.info("停止对 {} 的任务调度", c.getName());
            }

        });
    }

    /**
     * 重写 configureTasks 方法，当构造完所有 NacosCronScheduled 注解的任务调度时，通过该方法直接启用一次所有任务调度
     *
     * @param scheduledTaskRegistrar 任务任务注册器
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        this.scheduledTaskRegistrar = scheduledTaskRegistrar;

        // CACHE 内容来源 postProcessAfterInitialization 方法进行构造
        CACHE.values().forEach(c -> {
            ScheduledTask scheduledTask = scheduledTaskRegistrar.scheduleCronTask(c.createCronTask());
            c.setScheduledTask(scheduledTask);
        });
    }

    /**
     * 重写 postProcessAfterInitialization 方法，用于扫描所有类下带有 NacosCronScheduled 的方法，
     * 并构造出 {@link #CACHE} 对象
     *
     * @param bean     当前类对象
     * @param beanName 当前类名称
     *
     * @return 当前类信息
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {

        // aop 功能的 bean 不支持 NacosCronScheduled
        if (AopInfrastructureBean.class.isAssignableFrom(bean.getClass())) {
            return bean;
        }

        // 任务执行服务不支持 NacosCronScheduled
        if (ScheduledExecutorService.class.isAssignableFrom(bean.getClass())) {
            return bean;
        }

        // 任务调度服务不支持 NacosCronScheduled
        if (TaskScheduler.class.isAssignableFrom(bean.getClass())) {
            return bean;
        }

        // 获取当前 bean 的 class 信息
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);

        // 如果该 class 被设置为忽略，不做任何处理
        if (notMatchClassList.contains(targetClass)) {
            return bean;
        }

        // 获取 targetClass 下所有带有 NacosCronScheduled 注解的方法
        Map<Method, NacosCronScheduled> annotatedMethods = MethodIntrospector.selectMethods(
                targetClass,
                (MethodIntrospector.MetadataLookup<NacosCronScheduled>) method ->
                        AnnotatedElementUtils.findMergedAnnotation(method, NacosCronScheduled.class)
        );

        // 如果找不到注解方法，添加到找不到 class 集合理，用于下次碰到该 class 时可以直接忽略
        if (annotatedMethods.isEmpty()) {
            notMatchClassList.add(targetClass);
        }

        // 循环构造 CACHE 对象
        annotatedMethods.forEach((k, v) -> {

            // 创建 cron 调度信息
            CronScheduledInfo cronScheduledInfo = createCronScheduledInfo(k, v, bean);

            // 如果该类型为 NacosCronScheduledInfo 时，加入到 CACHE 中
            if (NacosCronScheduledInfo.class.isAssignableFrom(cronScheduledInfo.getClass())) {
                // 定义 key 内容
                List<MatchEvaluation> matchEvaluations = new LinkedList<>();

                NacosCronScheduledInfo info = (NacosCronScheduledInfo) cronScheduledInfo;

                // 定义表达式匹配定值类
                MatchEvaluation cronEvaluation = new MatchEvaluation(
                        info.getCronPropertyName(),
                        CronScheduledInfo.DEFAULT_EXPRESSION_FIELD_NAME,
                        info.getExpression()
                );

                matchEvaluations.add(cronEvaluation);

                // 如果存在时区，定义时区匹配定值类
                if (StringUtils.isNotEmpty(info.getTimeZonePropertyName())) {

                    MatchEvaluation zoneEvaluation = new MatchEvaluation(
                            info.getTimeZonePropertyName(),
                            CronScheduledInfo.DEFAULT_TIME_ZONE_FIELD_NAME,
                            info.getTimeZone(),
                            (value, target) -> getTimeZone(v.toString())
                    );

                    matchEvaluations.add(zoneEvaluation);
                }

                LOGGER.info(
                        "构造 [{}] 的 cron 调度, 当前 cron 表达式为: {}, 时区为: {}",
                        cronScheduledInfo.getName(),
                        cronScheduledInfo.getExpression(),
                        cronScheduledInfo.getTimeZone()
                );
                // 添加到 CACHE 中
                CACHE.put(matchEvaluations, info);
            }

        });


        return bean;
    }

    /**
     * 通过 NacosCronScheduled 注解构造 cron 调度信息
     *
     * @param method    使用 NacosCronScheduled 注解的方法类
     * @param scheduled NacosCronScheduled 注解
     * @param bean      使用 NacosCronScheduled 注解方法的类对象
     *
     * @return cron 调度信息
     */
    protected CronScheduledInfo createCronScheduledInfo(Method method, NacosCronScheduled scheduled, Object bean) {
        // 获取注解的名称，该名称用于展示日志而使用。
        String name = scheduled.name();

        // 如果名称为空，使用类名加方法名代替
        if (StringUtils.isEmpty(name)) {
            name = bean.getClass().getName() + "." + method.getName();
        }

        CronScheduledInfo cronScheduledInfo;

        String cron = scheduled.cron();
        String zone = scheduled.zone();

        // 如果 corn 带有 spring el 表达式，构造 NacosCronScheduledInfo 对象，用于在 nacos 配置刷新时候，
        // 能够通过 cronPropertyName 和 zonePropertyName 刷新当前值，否则直接使用 CronScheduledInfo
        // 来做无刷新的 cron 调度，该调度不会加入到 CACHE 对象中。
        if (StringUtils.startsWith(cron, SystemPropertyUtils.PLACEHOLDER_PREFIX)
                && StringUtils.endsWith(cron, SystemPropertyUtils.PLACEHOLDER_SUFFIX)) {

            // 如果 spring el 的变量内容
            String cronPropertyName = getPropertyName(cron);

            String zonePropertyName = zone;

            // 如果时区也是 spring el 变量，在获取时区的变量内容
            if (StringUtils.startsWith(zone, SystemPropertyUtils.PLACEHOLDER_PREFIX)
                    && StringUtils.endsWith(zone, SystemPropertyUtils.PLACEHOLDER_SUFFIX)) {
                zonePropertyName = getPropertyName(zone);
            }

            // 创建 nacos cron 调度信息
            cronScheduledInfo = new NacosCronScheduledInfo(name, cronPropertyName, zonePropertyName);

        } else {
            cronScheduledInfo = new CronScheduledInfo(name);
        }

        // 如果嵌入式(spring el 表达式)值解析器有注入，通过该类解析实际值
        if (this.embeddedValueResolver != null) {
            cron = this.embeddedValueResolver.resolveStringValue(cron);
            zone = this.embeddedValueResolver.resolveStringValue(zone);
        }

        // 如果 cron 非注释值，构造执行线程，待 configureTasks 方法被触发时，通过以下信息构造任务调度
        if (!ScheduledTaskRegistrar.CRON_DISABLED.equals(cron)) {

            Runnable runnable = createRunnable(bean, method);

            cronScheduledInfo.setRunnable(runnable);
            cronScheduledInfo.setExpression(cron);
            cronScheduledInfo.setTimeZone(getTimeZone(zone));
        }

        return cronScheduledInfo;
    }

    /**
     * 通过字符串获取时区
     *
     * @param zone 时区字符内容
     *
     * @return 时区
     */
    private TimeZone getTimeZone(String zone) {

        return StringUtils.isNotEmpty(zone) ? TimeZone.getTimeZone(zone) : TimeZone.getDefault();

    }

    /**
     * 获取 spring el 变量名
     *
     * @param value 值，格式为:${变量} 或 ${变量:默认值}
     *
     * @return spring el 变量名
     */
    private String getPropertyName(String value) {

        String result = StringUtils.substringBetween(
                value,
                SystemPropertyUtils.PLACEHOLDER_PREFIX,
                SystemPropertyUtils.PLACEHOLDER_SUFFIX
        );

        if (StringUtils.contains(result, SystemPropertyUtils.VALUE_SEPARATOR)) {
            result = StringUtils.substringBefore(result, SystemPropertyUtils.VALUE_SEPARATOR);
        }

        return result;
    }

    /**
     * 通过目标类和方法，构造可运行的后台线程
     *
     * @param target 目标类
     * @param method 方法
     *
     * @return 可运行的后台线程
     */
    protected Runnable createRunnable(Object target, Method method) {
        Assert.isTrue(method.getParameterCount() == 0, "@NacosCronScheduled 必须要在没参数的方法中使用");
        Method invocableMethod = AopUtils.selectInvocableMethod(method, target.getClass());
        return new ScheduledMethodRunnable(target, invocableMethod);
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver embeddedValueResolver) {
        this.embeddedValueResolver = embeddedValueResolver;
    }

    /**
     * 当所有数据准备就绪时，通过此方法添加 nacos 配置中心侦听。
     */
    @Override
    public void afterPropertiesSet() throws Exception {

        // 需要侦听的配置集合
        List<NacosConfigProperties.Config> list = new LinkedList<>();

        // 获取当前服务的默认配置内容（为了避免侦听到其他服务的配置信息，通过该类来获取当前服务所读取的配置中心文件，不干扰其他服务的配置）
        NacosConfigProperties nacosConfigProperties = nacosConfigManager.getNacosConfigProperties();

        // 如果不是自动刷新替换值，不侦听默认配置文件的更新内容
        if (nacosConfigProperties.isRefreshEnabled()) {

            // 获取配置文件名称
            String name = nacosConfigProperties.getName();

            // 如果名称为空，通过 spring.application.name 来构造默认侦听信息
            if (StringUtils.isEmpty(name)) {
                // 获取 spring.application.name 信息
                name = applicationContext.getEnvironment().getProperty("spring.application.name");
                // 如果为空，表示不对服务应用名做 nacos 配置管理，直接略过
                if (StringUtils.isNotEmpty(name)) {

                    // 如果 name 没有 .后缀名，通过配置信息获取
                    String defaultName = StringUtils.appendIfMissing(name, "." + nacosConfigProperties.getFileExtension());

                    // 如果默认要侦听的配置信息
                    NacosConfigProperties.Config config = new NacosConfigProperties.Config(
                            defaultName,
                            nacosConfigProperties.getGroup()
                    );

                    // 添加到侦听集合中
                    list.add(config);

                    // 获取活动环境配置，并构造 [name-环境值.后缀] 的配置文件侦听
                    for (String profile : applicationContext.getEnvironment().getActiveProfiles()) {
                        String profileName = StringUtils.appendIfMissing(name, "-" + profile + "." + nacosConfigProperties.getFileExtension());

                        NacosConfigProperties.Config profileConfig = new NacosConfigProperties.Config(
                                profileName,
                                nacosConfigProperties.getGroup()
                        );

                        list.add(profileConfig);
                    }

                }

            } else {

                NacosConfigProperties.Config config = new NacosConfigProperties.Config(
                        name,
                        nacosConfigProperties.getGroup()
                );

                list.add(config);
            }

        }

        // 如果配置信息存在扩展配置，添加到侦听集合中
        if (!CollectionUtils.isEmpty(nacosConfigProperties.getExtensionConfigs())) {

            List<NacosConfigProperties.Config> result = nacosConfigProperties
                    .getExtensionConfigs()
                    .stream()
                    .filter(NacosConfigProperties.Config::isRefresh)
                    .map(c -> new NacosConfigProperties.Config(c.getDataId(), c.getGroup()))
                    .collect(Collectors.toList());

            list.addAll(result);
        }

        // 如果配置信息存在共享配置，添加到侦听集合中
        if (!CollectionUtils.isEmpty(nacosConfigProperties.getSharedConfigs())) {

            List<NacosConfigProperties.Config> result = nacosConfigProperties
                    .getSharedConfigs()
                    .stream()
                    .filter(NacosConfigProperties.Config::isRefresh)
                    .map(c -> new NacosConfigProperties.Config(c.getDataId(), c.getGroup()))
                    .collect(Collectors.toList());

            list.addAll(result);
        }

        // 循环所有配置集合，并侦听所有内容的改变
        for (NacosConfigProperties.Config c : list) {
            nacosConfigManager.getConfigService().addListener(c.getDataId(), c.getGroup(), new AbstractSharedListener() {
                @Override
                public void innerReceive(String dataId, String group, String configInfo) {
                    try {
                        configReceive(dataId, configInfo);
                    } catch (Exception e) {
                        LOGGER.error("执行接受配置文件变化[" + dataId + "," + group + "]出错", e);
                    }
                }
            });
        }

        // 由于所有动作完成，清除 notMatchClassList 释放内存
        notMatchClassList.clear();

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
