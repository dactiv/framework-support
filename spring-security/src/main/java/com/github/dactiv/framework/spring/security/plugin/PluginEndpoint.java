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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
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
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * ??????????????????
 *
 * @author maurice
 */
@Endpoint(id = PluginEndpoint.DEFAULT_PLUGIN_KEY_NAME)
public class PluginEndpoint {

    private final static Logger LOGGER = LoggerFactory.getLogger(PluginEndpoint.class);

    /**
     * ???????????????????????????
     */
    public final static String DEFAULT_PLUGIN_KEY_NAME = "plugin";

    /**
     * ?????????????????????
     */
    private final List<InfoContributor> infoContributors = new ArrayList<>();

    /**
     * ????????????????????????
     */
    private List<String> basePackages = new ArrayList<>(16);

    /**
     * ??????????????????????????????????????????????????????????????? {@link Plugin#sources()} ??????????????? info ?????? plugin ?????????????????? plugin ???
     */
    private List<String> generateSources = new LinkedList<>();

    /**
     * spring ???????????????
     */
    private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    /**
     * spring ?????????????????????
     */
    private final MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(this.resourcePatternResolver);

    /**
     * spring security ?????????????????????
     */
    private final MethodSecurityExpressionHandler mse = new DefaultMethodSecurityExpressionHandler();

    /**
     * ?????????
     */
    private final Map<String, Object> cache = new LinkedHashMap<>();

    /**
     * ??????????????????????????????
     */
    private Map<String, PluginInfo> parent = new LinkedHashMap<>();

    /**
     * ??????????????????????????????
     */
    private final Map<String, List<Method>> missingParentMap = new LinkedHashMap<>();

    /**
     * ?????????
     */
    private final Lock lock = new ReentrantLock();

    public PluginEndpoint(List<InfoContributor> infoContributors) {
        this.infoContributors.addAll(infoContributors);
    }

    @ReadOperation
    public Map<String, Object> plugin() {
        // ???????????????????????????????????????
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
     * ??????????????????
     */
    private List<Tree<String, PluginInfo>> resolvePlugin() {
        LOGGER.info("???????????? info.plugin ??????");

        List<Tree<String, PluginInfo>> result = new ArrayList<>();

        // ??????????????? Controller ????????????
        Set<Object> targetSet = resolvePlaceholders();
        // ?????????????????????????????????
        if (CollectionUtils.isEmpty(targetSet)) {
            return result;
        }

        List<PluginInfo> pluginInfoList = new ArrayList<>();

        // ???????????????????????????
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
                // ????????? plugin ???????????? id ??????????????????????????? id ???
                if (StringUtils.isBlank(parent.getId())) {
                    parent.setId(classTarget.getName());
                }

                pluginInfoList.add(parent);
                Method[] methods = classTarget.isInterface() ? classTarget.getMethods() : classTarget.getDeclaredMethods();
                List<Method> methodList = Arrays.asList(methods);
                TargetObject targetObject = new TargetObject(target, methodList);
                // ????????????????????? plugin ????????????????????? parent ???
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
                            .findFirst().orElse(null);

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
                throw new SystemException("Plugin ??????????????? Class ??? Method ??????, ");
            }

        }

        parent
                .values()
                .stream()
                .filter(p -> generateSources.stream().anyMatch(s -> p.getSources().contains(s)))
                .forEach(p -> {
                    p.setParent(PluginInfo.DEFAULT_ROOT_PARENT_NAME);
                    if (StringUtils.isBlank(p.getType())) {
                        p.setType(ResourceType.Menu.toString());
                    }
                });

        pluginInfoList.addAll(parent.values());

        LOGGER.info("??????" + cache.size() + "???????????????");

        result = TreeUtils.buildTree(pluginInfoList);

        return result;
    }

    public PluginInfo createPluginInfo(Plugin plugin, Class<?> target) {

        PluginInfo parent = new PluginInfo(plugin);

        // ?????????????????? RequestMapping ?????????????????????????????? value ????????????
        RequestMapping mapping = AnnotationUtils.findAnnotation(target, RequestMapping.class);
        if (mapping != null) {
            List<String> uri = new ArrayList<>();
            for (String value : mapping.value()) {
                // ?????? /** ????????????????????????????????????????????????????????????
                String url = StringUtils.appendIfMissing(value, "/**");
                // ??????.??????????????????????????????????????????????????????????????????
                // ???.html ??? .json??????????????????????????????????????????
                uri.add(RegExUtils.removePattern(url, "\\{.*\\}"));
            }
            parent.setValue(StringUtils.join(uri, SpringMvcUtils.COMMA_STRING));

        }

        return parent;
    }

    /**
     * ????????????????????? {@link Plugin} ??????????????????????????? {@link PluginInfo#getChildren()} ???
     *
     * @param targetObject ????????????
     * @param parent       ???????????????
     */
    private List<PluginInfo> buildPluginInfo(TargetObject targetObject, PluginInfo parent) {

        List<PluginInfo> result = new ArrayList<>();

        if (generateSources.stream().noneMatch(s -> parent.getSources().contains(s))) {
            return result;
        }

        for (Method method : targetObject.getMethodList()) {
            // ??????????????? PluginInfo ????????????????????????
            Plugin plugin = AnnotationUtils.findAnnotation(method, Plugin.class);
            if (plugin == null) {
                continue;
            }

            // ???????????? url ???
            List<String> values = getRequestValues(targetObject.getTarget(), method, parent);
            if (values.isEmpty()) {
                continue;
            }

            PluginInfo target = new PluginInfo(plugin);
            // ????????????????????? plugin ???????????? id???????????????????????? id
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

        // ?????? RequestMapping ??? value ??????
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
     * ?????????????????? uri ??????????????? ?????????????????????
     *
     * @param target      ????????????
     * @param targetValue ?????? url ???
     * @param parent      ?????? plugin
     *
     * @return ???????????? uri ??????????????? ?????????????????????
     */
    private String getRequestValueString(Object target, String targetValue, PluginInfo parent) {

        List<String> uri = new ArrayList<>();

        if (StringUtils.isBlank(parent.getValue())) {
            return StringUtils.appendIfMissing(targetValue, "/**");
        } else if (TargetObject.class.isAssignableFrom(target.getClass())) {

            TargetObject targetObject = Casts.cast(target);
            if (Method.class.isAssignableFrom(targetObject.getTarget().getClass())) {
                return StringUtils.appendIfMissing(targetValue, "/**");
            }

        }

        for (String parentValue : StringUtils.split(parent.getValue())) {
            for (String value : StringUtils.split(targetValue)) {
                parentValue = StringUtils.remove(parentValue, "**");
                String url = StringUtils.appendIfMissing(parentValue, value + "/**");
                uri.add(RegExUtils.removeAll(url, "\\{.*\\}"));
            }
        }

        return StringUtils.join(uri, SpringMvcUtils.COMMA_STRING);
    }

    /**
     * ????????????????????????
     *
     * @param target ????????????
     * @param method ??????
     * @param parent ????????????
     *
     * @return ???????????????
     */
    private List<String> getRequestValues(Object target, Method method, PluginInfo parent) {

        // ?????? RequestMapping ??? value ??????
        List<String> values = new ArrayList<>();
        // ??????????????? RequestMapping ????????????????????????
        RequestMapping requestMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
        if (requestMapping != null) {
            values = Arrays.asList(requestMapping.value());
        }

        // ????????????????????????????????? GetMapping ??????
        if (values.isEmpty()) {
            // ??????????????? GetMapping ????????????????????????
            GetMapping annotation = AnnotationUtils.findAnnotation(method, GetMapping.class);
            if (annotation != null) {
                values = Arrays.asList(annotation.value());
            }
        }

        // ????????????????????????????????? PostMapping ??????
        if (values.isEmpty()) {
            // ??????????????? PostMapping ????????????????????????
            PostMapping annotation = AnnotationUtils.findAnnotation(method, PostMapping.class);
            if (annotation != null) {
                values = Arrays.asList(annotation.value());
            }
        }

        // ????????????????????????????????? PutMapping ??????
        if (values.isEmpty()) {
            // ??????????????? PutMapping ????????????????????????
            PutMapping annotation = AnnotationUtils.findAnnotation(method, PutMapping.class);
            if (annotation != null) {
                values = Arrays.asList(annotation.value());
            }
        }

        // ????????????????????????????????? DeleteMapping ??????
        if (values.isEmpty()) {
            // ??????????????? PutMapping ????????????????????????
            DeleteMapping annotation = AnnotationUtils.findAnnotation(method, DeleteMapping.class);
            if (annotation != null) {
                values = Arrays.asList(annotation.value());
            }
        }

        // ????????????????????????????????????????????????????????????
        if (values.isEmpty()) {
            values = Collections.singletonList(method.getName());
        }

        return values.stream()
                .map(v -> parent == null ? v : getRequestValueString(target, v, parent))
                .collect(Collectors.toList());
    }

    /**
     * ???????????? Controller ??????????????????
     *
     * @return ?????? Controller ??????????????????
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
     * ???????????????????????????
     *
     * @param basePackages ?????????
     */
    public void setBasePackages(List<String> basePackages) {
        this.basePackages = basePackages;
    }

    /**
     * ???????????????????????????
     *
     * @return ???????????????????????????
     */
    public List<String> getBasePackages() {
        return basePackages;
    }

    /**
     * ?????????????????????????????????
     *
     * @return ???????????????????????????
     */
    public List<String> getGenerateSources() {
        return generateSources;
    }

    /**
     * ?????????????????????????????????
     *
     * @param generateSources ???????????????????????????
     */
    public void setGenerateSources(List<String> generateSources) {
        this.generateSources = generateSources;
    }

}
