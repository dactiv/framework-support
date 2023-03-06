package com.github.dactiv.framework.spring.security.plugin;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.tree.Tree;
import com.github.dactiv.framework.commons.tree.TreeUtils;
import com.github.dactiv.framework.security.entity.RoleAuthority;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.framework.security.plugin.PluginInfo;
import com.github.dactiv.framework.security.plugin.TargetObject;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import jakarta.annotation.PostConstruct;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.SpelNode;
import org.springframework.expression.spel.ast.MethodReference;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * 插件信息终端
 *
 * @author maurice
 */
@Endpoint(id = PluginEndpoint.DEFAULT_PLUGIN_KEY_NAME)
public class PluginEndpoint {

    private final static Logger LOGGER = LoggerFactory.getLogger(PluginEndpoint.class);

    /**
     * 默认的插件节点名称
     */
    public final static String DEFAULT_PLUGIN_KEY_NAME = "plugin";

    /**
     * 信息奉献者集合
     */
    private final List<InfoContributor> infoContributors = new ArrayList<>();

    /**
     * 需要扫描的包路径
     */
    private List<String> basePackages = new ArrayList<>(16);

    /**
     * 生成插件的来源类型集合，用于通过该值去配合 {@link Plugin#sources()} 来判断构造 info 中的 plugin 时，否添加到 plugin 中
     */
    private List<String> generateSources = new LinkedList<>();

    /**
     * spring 资源解析器
     */
    private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    /**
     * spring 元数据读取工厂
     */
    private final MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(this.resourcePatternResolver);

    /**
     * spring security 方法表达式具柄
     */
    private final MethodSecurityExpressionHandler mse = new DefaultMethodSecurityExpressionHandler();

    /**
     * 缓存值
     */
    private final Map<String, Object> cache = new LinkedHashMap<>();

    /**
     * 找不到父类的插件信息
     */
    private Map<String, PluginInfo> parent = new LinkedHashMap<>();

    /**
     * 找不到父类的插件信息
     */
    private final Map<String, List<Method>> missingParentMap = new LinkedHashMap<>();

    /**
     * 并发锁
     */
    private final Lock lock = new ReentrantLock();

    public PluginEndpoint(List<InfoContributor> infoContributors) {
        this.infoContributors.addAll(infoContributors);
    }

    @ReadOperation
    public Map<String, Object> plugin() {
        // 如果缓存没有，就去扫描遍历
        lock.lock();

        try {

            if (cache.isEmpty()) {

                Info.Builder builder = new Info.Builder();

                for (InfoContributor contributor : this.infoContributors) {
                    contributor.contribute(builder);
                }

                Info build = builder.build();

                Map<String, Object> info = new LinkedHashMap<>();

                Map<String, Object> details = build.getDetails();

                if (MapUtils.isNotEmpty(details)) {
                    info.putAll(details);
                }

                List<Tree<String, PluginInfo>> pluginList = resolvePlugin();

                info.put(DEFAULT_PLUGIN_KEY_NAME, pluginList);

                cache.putAll(info);
            }

            return cache;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 解析资源信息
     */
    private List<Tree<String, PluginInfo>> resolvePlugin() {
        LOGGER.info("开始解析 info.plugin 信息");

        List<Tree<String, PluginInfo>> result = new ArrayList<>();

        // 扫描所有带 Controller 注解的类
        Set<Object> targetSet = resolvePlaceholders();
        // 如果找不到，什么都不做
        if (CollectionUtils.isEmpty(targetSet)) {
            return result;
        }

        List<PluginInfo> pluginInfoList = new ArrayList<>();

        // 循环解析类中的方法
        for (Object target : targetSet) {

            if (Class.class.isAssignableFrom(target.getClass())) {

                Class<?> classTarget = (Class<?>) target;
                Plugin plugin = AnnotationUtils.findAnnotation(classTarget, Plugin.class);
                if (Objects.isNull(plugin)) {
                    continue;
                }

                List<String> sources = Arrays.asList(plugin.sources());
                if (generateSources.stream().noneMatch(sources::contains)) {
                    continue;
                }

                PluginInfo parent = createPluginInfo(plugin, classTarget);
                // 如果该 plugin 配置没有 id 值，就直接用类名做 id 值
                if (StringUtils.isBlank(parent.getId())) {
                    parent.setId(classTarget.getName());
                }

                pluginInfoList.add(parent);
                Method[] methods = classTarget.isInterface() ? classTarget.getMethods() : classTarget.getDeclaredMethods();
                List<Method> methodList = Arrays.asList(methods);
                TargetObject targetObject = new TargetObject(target, methodList);
                // 遍历方法级别的 plugin 注解。并添加到 parent 中
                List<PluginInfo> pluginInfos = buildPluginInfo(targetObject, parent);
                pluginInfoList.addAll(pluginInfos);

            } else if (Method.class.isAssignableFrom(target.getClass())) {

                Method method = (Method) target;
                Plugin plugin = AnnotationUtils.findAnnotation(method, Plugin.class);
                if (Objects.isNull(plugin)) {
                    continue;
                }

                PluginInfo temp = null;
                if (StringUtils.isNotBlank(plugin.parent())) {
                    temp = parent
                            .values()
                            .stream()
                            .filter(p -> p.getId().equals(plugin.parent()))
                            .findFirst()
                            .orElse(null);
                    if (Objects.isNull(temp)) {
                        temp = pluginInfoList
                                .stream()
                                .filter(p -> p.getId().equals(plugin.parent()))
                                .findFirst()
                                .orElse(null);
                    }
                    if (Objects.isNull(temp)) {
                        List<Method> methods = missingParentMap.computeIfAbsent(plugin.parent(), s -> new ArrayList<>());
                        methods.add(method);
                        continue;
                    }
                }

                TargetObject targetObject = new TargetObject(method, Collections.singletonList(method));
                List<PluginInfo> pluginInfos = buildPluginInfo(targetObject, temp);
                pluginInfoList.addAll(pluginInfos);
            } else {
                throw new SystemException("Plugin 注解只支持 Class 或 Method 类型, ");
            }

        }

        parent
                .values()
                .stream()
                .filter(p -> generateSources.stream().anyMatch(s -> p.getSources().contains(s)))
                .peek(p -> p.setParent(PluginInfo.DEFAULT_ROOT_PARENT_NAME))
                .filter(p -> StringUtils.isBlank(p.getType()))
                .forEach(p -> p.setType(ResourceType.Menu.toString()));

        pluginInfoList.addAll(parent.values());

        LOGGER.info("找到" + cache.size() + "条记录信息");

        result = TreeUtils.buildTree(pluginInfoList);

        return result;
    }

    public PluginInfo createPluginInfo(Plugin plugin, Class<?> target) {

        PluginInfo parent = new PluginInfo(plugin);

        // 如果类头存在 RequestMapping 注解，需要将该注解的 value 合并起来
        RequestMapping mapping = AnnotationUtils.findAnnotation(target, RequestMapping.class);
        if (mapping != null) {
            List<String> uri = new ArrayList<>();
            for (String value : mapping.value()) {
                // 添加 /** 通配符，作用是为了可能某些需要带参数过来
                String url = StringUtils.appendIfMissing(value, SpringMvcUtils.ANT_PATH_MATCH_ALL);
                // 删除.（逗号）后缀的所有内容，作用是可能有些配置是
                // 有.html 和 .json的类似配置，但其实一个就够了
                uri.add(RegExUtils.removePattern(url, "\\{.*\\}"));
            }
            parent.setValue(StringUtils.join(uri, SpringMvcUtils.COMMA_STRING));

        }

        return parent;
    }

    /**
     * 遍历方法级别的 {@link Plugin} 信息，并将值合并的 {@link PluginInfo#getChildren()} 中
     *
     * @param targetObject 目标对象
     * @param parent       根节点信息
     */
    private List<PluginInfo> buildPluginInfo(TargetObject targetObject, PluginInfo parent) {

        List<PluginInfo> result = new ArrayList<>();

        if (generateSources.stream().noneMatch(s -> parent.getSources().contains(s))) {
            return result;
        }

        for (Method method : targetObject.getMethodList()) {
            // 如果找不到 PluginInfo 注解，什么都不做
            Plugin plugin = AnnotationUtils.findAnnotation(method, Plugin.class);
            if (plugin == null) {
                continue;
            }

            // 获取请求 url 值
            List<String> values = getRequestValues(targetObject.getTarget(), method, parent);
            if (values.isEmpty()) {
                continue;
            }

            PluginInfo target = new PluginInfo(plugin);
            // 如果方法级别的 plugin 信息没有 id，就用方法名称做 id
            if (StringUtils.isBlank(target.getId())) {
                target.setId(method.getName());
            }

            if (StringUtils.isBlank(target.getParent())) {
                target.setParent(parent.getId());
            }

            if (CollectionUtils.isEmpty(target.getSources())) {
                target.setSources(parent.getSources());
            }

            target.setValue(StringUtils.join(values, SpringMvcUtils.COMMA_STRING));
            List<String> authorize = getSecurityAuthorize(method);

            if (!authorize.isEmpty()) {
                target.setAuthority(StringUtils.join(authorize, SpringMvcUtils.COMMA_STRING));
            }

            result.add(target);

            if (missingParentMap.containsKey(target.getId())) {
                List<Method> subMethods = missingParentMap.get(target.getId());
                TargetObject subObject = new TargetObject(targetObject, subMethods);
                result.addAll(buildPluginInfo(subObject, target));
            }

        }

        return result;
    }

    private List<String> getSecurityAuthorize(Method method) {

        // 获取 RequestMapping 的 value 信息
        List<String> values = new ArrayList<>();

        PreAuthorize preAuthorize = AnnotationUtils.findAnnotation(method, PreAuthorize.class);
        if (preAuthorize != null) {
            String v = preAuthorize.value();
            List<String> methodValues = getAuthorityMethodValue(v);
            values.addAll(methodValues);
        }

        PostAuthorize postAuthorize = AnnotationUtils.findAnnotation(method, PostAuthorize.class);
        if (postAuthorize != null) {
            String v = postAuthorize.value();
            List<String> methodValues = getAuthorityMethodValue(v);
            values.addAll(methodValues);
        }

        return values;
    }

    private List<String> getAuthorityMethodValue(String value) {

        Expression expression = mse.getExpressionParser().parseExpression(value);
        SpelExpression spelExpression = (SpelExpression) expression;
        SpelNode spelNode = spelExpression.getAST();

        return getAuthorityMethodValue(spelNode);
    }

    private List<String> getAuthorityMethodValue(SpelNode spelNode) {

        List<String> result = new ArrayList<>();

        if (MethodReference.class.isAssignableFrom(spelNode.getClass())) {

            MethodReference mr = (MethodReference) spelNode;
            if (SecurityUserDetails.DEFAULT_SUPPORT_SECURITY_METHOD_NAME.contains(mr.getName())) {

                if (mr.getName().equals(SecurityUserDetails.DEFAULT_IS_AUTHENTICATED_METHOD_NAME)) {
                    result.add(SecurityUserDetails.DEFAULT_IS_AUTHENTICATED_METHOD_NAME);
                } else {
                    for (int i = 0; i < mr.getChildCount(); i++) {
                        String value = mr.getChild(i).toString().replaceAll("'", StringUtils.EMPTY);

                        if (SecurityUserDetails.DEFAULT_ROLE_PREFIX_METHOD_NAME.contains(mr.getName())) {
                            value = RoleAuthority.DEFAULT_ROLE_PREFIX + value;
                        }

                        result.add(value);
                    }
                }

                return result;
            }
        }

        for (int i = 0; i < spelNode.getChildCount(); i++) {
            SpelNode child = spelNode.getChild(i);
            result.addAll(getAuthorityMethodValue(child));
        }

        return result;
    }

    /**
     * 获取多个请求 uri 信息，并用 ，（逗号）分割
     *
     * @param target      目标对象
     * @param targetValue 目标 url 值
     * @param parent      父类 plugin
     *
     * @return 多个请求 uri 信息，并用 ，（逗号）分割
     */
    private String getRequestValueString(Object target, String targetValue, PluginInfo parent) {

        List<String> uri = new ArrayList<>();

        List<String> parentValueList = new LinkedList<>();

        if (StringUtils.isEmpty(StringUtils.trimToEmpty(parent.getValue()))) {
            if (Method.class.isAssignableFrom(target.getClass()) && StringUtils.isEmpty(parent.getValue())) {
                Method method = Casts.cast(target);
                RequestMapping requestMapping = AnnotationUtils.findAnnotation(method.getDeclaringClass(), RequestMapping.class);
                if (Objects.nonNull(requestMapping)) {
                    parentValueList = Arrays.stream(requestMapping.value()).map(s -> StringUtils.appendIfMissing(s, AntPathMatcher.DEFAULT_PATH_SEPARATOR)).collect(Collectors.toList());
                } else {
                    return StringUtils.appendIfMissing(targetValue, SpringMvcUtils.ANT_PATH_MATCH_ALL);
                }
            } else {
                return StringUtils.appendIfMissing(targetValue, SpringMvcUtils.ANT_PATH_MATCH_ALL);
            }
        } else if (TargetObject.class.isAssignableFrom(target.getClass())) {
            TargetObject targetObject = Casts.cast(target);
            if (Method.class.isAssignableFrom(targetObject.getTarget().getClass())) {
                return StringUtils.appendIfMissing(targetValue, SpringMvcUtils.ANT_PATH_MATCH_ALL);
            }
        }

        if (StringUtils.isNotEmpty(parent.getValue())) {
            String prefix = StringUtils.appendIfMissing(StringUtils.removeEnd(parent.getValue(),"**"), AntPathMatcher.DEFAULT_PATH_SEPARATOR);
            if (CollectionUtils.isEmpty(parentValueList)) {
                parentValueList.add(prefix);
            } else {
                parentValueList = parentValueList
                        .stream()
                        .map(s -> StringUtils.prependIfMissing(StringUtils.removeStart(s, AntPathMatcher.DEFAULT_PATH_SEPARATOR), prefix))
                        .toList();
            }
        }

        for (String parentValue : parentValueList) {
            for (String value : StringUtils.split(targetValue)) {
                String url = StringUtils.appendIfMissing(parentValue, value + SpringMvcUtils.ANT_PATH_MATCH_ALL);
                uri.add(RegExUtils.removeAll(url, "\\{.*\\}"));
            }
        }

        return StringUtils.join(uri, SpringMvcUtils.COMMA_STRING);
    }

    /**
     * 获取方法名请求值
     *
     * @param target 构建目标
     * @param method 方法
     * @param parent 父类节点
     *
     * @return 请求值集合
     */
    private List<String> getRequestValues(Object target, Method method, PluginInfo parent) {

        // 获取 RequestMapping 的 value 信息
        List<String> values = new ArrayList<>();
        // 如果找不到 RequestMapping 注解，什么都不做
        RequestMapping requestMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
        if (Objects.nonNull(requestMapping)) {
            values = Arrays.asList(requestMapping.value());
        }

        // 如果为空值，表示可能是 GetMapping 注解
        if (CollectionUtils.isEmpty(values)) {
            // 如果找不到 GetMapping 注解，什么都不做
            GetMapping annotation = AnnotationUtils.findAnnotation(method, GetMapping.class);
            if (Objects.nonNull(annotation)) {
                values = Arrays.asList(annotation.value());
            }
        }

        // 如果为空值，表示可能是 PostMapping 注解
        if (CollectionUtils.isEmpty(values)) {
            // 如果找不到 PostMapping 注解，什么都不做
            PostMapping annotation = AnnotationUtils.findAnnotation(method, PostMapping.class);
            if (Objects.nonNull(annotation)) {
                values = Arrays.asList(annotation.value());
            }
        }

        // 如果为空值，表示可能是 PutMapping 注解
        if (CollectionUtils.isEmpty(values)) {
            // 如果找不到 PutMapping 注解，什么都不做
            PutMapping annotation = AnnotationUtils.findAnnotation(method, PutMapping.class);
            if (Objects.nonNull(annotation)) {
                values = Arrays.asList(annotation.value());
            }
        }

        // 如果为空值，表示可能是 DeleteMapping 注解
        if (CollectionUtils.isEmpty(values)) {
            // 如果找不到 PutMapping 注解，什么都不做
            DeleteMapping annotation = AnnotationUtils.findAnnotation(method, DeleteMapping.class);
            if (Objects.nonNull(annotation)) {
                values = Arrays.asList(annotation.value());
            }
        }

        // 如果为空值，表示注解没命名，直接用方法名
        if (CollectionUtils.isEmpty(values)) {
            values = Collections.singletonList(method.getName());
        }

        return values.stream()
                .map(v -> Objects.isNull(parent) ? v : getRequestValueString(target, v, parent))
                .toList();
    }

    /**
     * 扫描包含 Controller 注解的所有类
     *
     * @return 包含 Controller 注解的所有类
     */
    private Set<Object> resolvePlaceholders() {
        Set<Object> target = new HashSet<>();

        for (String basePackage : basePackages) {
            String classPath = ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage)) + "/**/*.class";
            TypeFilter filter = new AnnotationTypeFilter(Plugin.class);

            try {
                Resource[] resources = this.resourcePatternResolver.getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + classPath);
                for (Resource resource : resources) {

                    if (!resource.isReadable()) {
                        continue;
                    }

                    MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(resource);
                    Class<?> targetClass = Class.forName(metadataReader.getClassMetadata().getClassName());

                    if (filter.match(metadataReader, metadataReaderFactory)) {
                        target.add(targetClass);
                    } else {
                        Method[] methods = targetClass.getDeclaredMethods();
                        for (Method method : methods) {
                            Plugin plugin = AnnotationUtils.findAnnotation(method, Plugin.class);
                            if (Objects.nonNull(plugin)) {
                                target.add(method);
                            }
                        }
                    }
                }

            } catch (Exception e) {
                throw new ServiceException(e);
            }
        }

        return target;
    }

    public Map<String, PluginInfo> getParent() {
        return parent;
    }

    public void setParent(Map<String, PluginInfo> parent) {
        this.parent = parent;
    }

    @PostConstruct
    public void init() {
        for (Map.Entry<String, PluginInfo> entry : parent.entrySet()) {
            entry.getValue().setId(entry.getKey());
        }
    }

    /**
     * 设置要扫描的包路径
     *
     * @param basePackages 包路径
     */
    public void setBasePackages(List<String> basePackages) {
        this.basePackages = basePackages;
    }

    /**
     * 获取要扫描的包路径
     *
     * @return 要扫描的包路径集合
     */
    public List<String> getBasePackages() {
        return basePackages;
    }

    /**
     * 获取插件的来源类型集合
     *
     * @return 插件的来源类型集合
     */
    public List<String> getGenerateSources() {
        return generateSources;
    }

    /**
     * 设置插件的来源类型集合
     *
     * @param generateSources 插件的来源类型集合
     */
    public void setGenerateSources(List<String> generateSources) {
        this.generateSources = generateSources;
    }

}
