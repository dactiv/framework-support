package com.github.dactiv.framework.spring.web.result.filter;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.spring.web.result.filter.annotation.Exclude;
import com.github.dactiv.framework.spring.web.result.filter.annotation.Excludes;
import com.github.dactiv.framework.spring.web.result.filter.annotation.view.ExcludeView;
import com.github.dactiv.framework.spring.web.result.filter.annotation.view.ExcludeViews;
import com.github.dactiv.framework.spring.web.result.filter.annotation.view.IncludeView;
import com.github.dactiv.framework.spring.web.result.filter.annotation.view.IncludeViews;
import com.github.dactiv.framework.spring.web.result.filter.holder.FilterResultHolder;
import io.micrometer.core.instrument.util.StringUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 过滤注解构造器
 *
 * @author maurice.chen
 */
public class FilterResultAnnotationBuilder extends JacksonAnnotationIntrospector {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterResultAnnotationBuilder.class);

    public static final String DEFAULT_INCLUDE_PREFIX = "include";

    public static final String DEFAULT_EXCLUDE_PREFIX = "exclude";

    private final static Class<? extends Annotation>[] ANNOTATIONS_TO_ADD = (Class<? extends Annotation>[])
            new Class<?>[] {
                    ExcludeViews.class,
                    ExcludeView.class,
                    IncludeView.class,
                    IncludeViews.class
            };

    /**
     * 需要扫描的包路径
     */
    private final List<String> basePackages;

    /**
     * spring 资源解析器
     */
    private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    /**
     * spring 元数据读取工厂
     */
    private final MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(this.resourcePatternResolver);

    public FilterResultAnnotationBuilder(List<String> basePackages) {
        this.basePackages = basePackages;
    }

    /**
     * 获取 filter 供应者
     *
     * @param config 配置信息
     *
     * @return filter 供应者
     */
    public FilterProvider getFilterProvider(MapperConfig<?> config) {

        SimpleFilterProvider simpleFilterProvider = new SimpleFilterProvider();

        for (String basePackage : basePackages) {

            String placeholders = SystemPropertyUtils.resolvePlaceholders(basePackage);
            String path = ClassUtils.convertClassNameToResourcePath(placeholders);

            String locationPattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + path + "/**/*.class";

            try {

                Resource[] resources = this.resourcePatternResolver.getResources(locationPattern);

                for (Resource resource : resources) {

                    if (!resource.isReadable()) {
                        continue;
                    }

                    MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(resource);

                    String className = metadataReader.getClassMetadata().getClassName();

                    Class<?> targetClass = Class.forName(className);

                    BeanDescription beanDescription = config.introspectClassAnnotations(targetClass);

                    AnnotatedClass annotatedClass = beanDescription.getClassInfo();

                    addProviderFilter(simpleFilterProvider, annotatedClass);

                    for (AnnotatedField field : annotatedClass.fields()) {

                        if (!field.getAllAnnotations().hasOneOf(ANNOTATIONS_TO_ADD)) {
                            continue;
                        }

                        PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(), targetClass);
                        Method readMethod = propertyDescriptor.getReadMethod();

                        AnnotatedMethod annotatedMethod = getAnnotatedMethod(annotatedClass, readMethod);

                        if (Objects.nonNull(annotatedMethod)) {
                            AnnotatedMethod ann = annotatedMethod.withAnnotations(field.getAllAnnotations());
                            addProviderFilter(simpleFilterProvider, ann);
                        }

                    }

                    for (AnnotatedMethod method : annotatedClass.memberMethods()) {
                        addProviderFilter(simpleFilterProvider, method);
                    }

                }
            }  catch (Exception e) {
                LOGGER.warn("加载資源出错，包路径为:" + locationPattern, e);
            }

        }

        return simpleFilterProvider;
    }

    private AnnotatedMethod getAnnotatedMethod(AnnotatedClass annotatedClass, Method readMethod) {
        for (AnnotatedMethod method : annotatedClass.memberMethods()) {

            if (method.getMember().equals(readMethod)) {
                return method;
            }

        }
        return null;
    }

    /**
     * 添加供应者 filter
     *
     * @param simpleFilterProvider 供应者 filter
     * @param annotated 注解信息
     */
    private void addProviderFilter(SimpleFilterProvider simpleFilterProvider, Annotated annotated) {

        ExcludeViews excludeViews = annotated.getAnnotation(ExcludeViews.class);

        if (Objects.nonNull(excludeViews)) {

            for (ExcludeView view : excludeViews.value()) {
                String id = getExcludeViewId(view, annotated);
                Set<String> set = Set.of(view.properties());
                simpleFilterProvider.addFilter(id, new SimpleBeanPropertyFilter.SerializeExceptFilter(set));
            }
        }

        ExcludeView excludeView = annotated.getAnnotation(ExcludeView.class);

        if (Objects.nonNull(excludeView)) {
            String id = getExcludeViewId(excludeView, annotated);
            Set<String> set = Set.of(excludeView.properties());
            simpleFilterProvider.addFilter(id, new SimpleBeanPropertyFilter.SerializeExceptFilter(set));
        }

        IncludeViews includeViews = annotated.getAnnotation(IncludeViews.class);

        if (Objects.nonNull(includeViews)) {

            for (IncludeView view : includeViews.value()) {
                String id = getIncludeViewId(view, annotated);
                Set<String> set = Set.of(view.properties());
                simpleFilterProvider.addFilter(id, new SimpleBeanPropertyFilter.FilterExceptFilter(set));
            }
        }

        IncludeView includeView = annotated.getAnnotation(IncludeView.class);

        if (Objects.nonNull(includeView)) {
            String id = getIncludeViewId(includeView, annotated);
            Set<String> set = Set.of(includeView.properties());
            simpleFilterProvider.addFilter(id, new SimpleBeanPropertyFilter.FilterExceptFilter(set));
        }
    }

    @Override
    public Object findFilterId(Annotated a) {
        Object returnValue = null;

        List<String> ids = FilterResultHolder.get();

        if (CollectionUtils.isNotEmpty(ids)) {

            ExcludeViews ExcludeViewsAnn = a.getAnnotation(ExcludeViews.class);

            if (Objects.nonNull(ExcludeViewsAnn)) {

                for (ExcludeView view : ExcludeViewsAnn.value()) {
                    returnValue = getExcludeViewId(view, ids, a);

                    if (Objects.nonNull(returnValue)) {
                        break;
                    }
                }
            }

            if (Objects.isNull(returnValue)) {
                returnValue = getExcludeViewId(a.getAnnotation(ExcludeView.class), ids, a);
            }

            if (Objects.isNull(returnValue)) {

                IncludeViews includeViewsAnn = a.getAnnotation(IncludeViews.class);

                if (Objects.nonNull(includeViewsAnn)) {

                    for (IncludeView view : includeViewsAnn.value()) {
                        returnValue = getIncludeViewId(view, ids, a);

                        if (Objects.nonNull(returnValue)) {
                            break;
                        }
                    }
                }

                if (Objects.nonNull(returnValue)) {
                    returnValue = getIncludeViewId(a.getAnnotation(IncludeView.class), ids, a);
                }

            }
        }

        if (Objects.isNull(returnValue)) {
            returnValue = super.findFilterId(a);
        }

        return returnValue;
    }

    /**
     * 获取排除视图 id
     *
     * @param view 排除视图注解
     * @param ids  当前应用的 id 值集合
     * @param a    当前注解位置
     *
     * @return 如果匹配返回排除视图  id 值，否则返回 null
     */
    public Object getExcludeViewId(ExcludeView view, List<String> ids, Annotated a) {

        if (view == null) {
            return null;
        }

        if (CollectionUtils.isNotEmpty(ids) && ids.contains(view.value())) {
            return getExcludeViewId(view, a);
        }

        return null;
    }

    /**
     * 获取排除视图 id
     *
     * @param view 引入视图注解
     * @param ids  当前应用的 id 值集合
     * @param a    当前注解位置
     *
     * @return 如果匹配返回排除视图  id 值，否则返回 null
     */
    public Object getIncludeViewId(IncludeView view, List<String> ids, Annotated a) {

        if (view == null) {
            return null;
        }

        if (CollectionUtils.isNotEmpty(ids) && ids.contains(view.value())) {
            return getIncludeViewId(view, a);
        }

        return null;
    }

    /**
     * 获取排除视图 id
     *
     * @param view 排除视图注解
     * @param a    当前注解位置
     *
     * @return 排除视图  id 值
     */
    public String getExcludeViewId(ExcludeView view, Annotated a) {
        return DEFAULT_EXCLUDE_PREFIX + Casts.DEFAULT_DOT_SYMBOL + view.value() + Casts.DEFAULT_DOT_SYMBOL + a.toString();
    }

    /**
     * 获取引入视图 id
     *
     * @param view 引入视图
     * @param a    当前注解位置
     *
     * @return 引入视图  id 值
     */
    private String getIncludeViewId(IncludeView view, Annotated a) {
        return DEFAULT_INCLUDE_PREFIX + Casts.DEFAULT_DOT_SYMBOL + view.value() + Casts.DEFAULT_DOT_SYMBOL + a.toString();
    }

    @Override
    protected boolean _isIgnorable(Annotated a) {

        boolean returnValue = false;

        List<String> ids = FilterResultHolder.get();

        if (CollectionUtils.isNotEmpty(ids)) {

            Excludes excludes = _findAnnotation(a, Excludes.class);

            if (excludes != null) {

                for (Exclude e : excludes.value()) {
                    returnValue = isExclude(e, ids);

                    if (returnValue) {
                        break;
                    }
                }

            }

            if (!returnValue) {
                returnValue = isExclude(_findAnnotation(a, Exclude.class), ids);
            }

        }

        return returnValue || super._isIgnorable(a);
    }

    @Override
    public PropertyName findNameForSerialization(Annotated a) {
        return super.findNameForSerialization(a);
    }

    /**
     * 是否匹配排除
     *
     * @param exclude 配置注解
     * @param ids     当前视图 id 集合
     *
     * @return true 是，否则 false
     */
    private boolean isExclude(Exclude exclude, List<String> ids) {

        if (exclude == null) {
            return false;
        }

        return CollectionUtils.isNotEmpty(ids) && ids.contains(exclude.value());
    }
}
