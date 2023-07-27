package com.github.dactiv.framework.spring.security.plugin;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户类型表决器实现，用于判断当前 controller 方法里面是否带有 {@link com.github.dactiv.framework.security.plugin.Plugin} 注解的记录是否符合当前用户调用
 *
 * @author maurice
 */
public class PluginSourceTypeVoter implements AccessDecisionVoter<MethodInvocation> {

    /**
     * 默认同意的来源类型值
     */
    public static final List<String> DEFAULT_GRANTED_SOURCES = Arrays.asList("SYSTEM","ALL");

    /**
     * 默认同意的来源类型
     */
    private List<String> grantedSources = DEFAULT_GRANTED_SOURCES;

    public boolean supports(ConfigAttribute attribute) {
        return true;
    }

    public boolean supports(Class<?> targetClass) {
        return MethodInvocation.class.isAssignableFrom(targetClass);
    }

    @Override
    public int vote(Authentication authentication, MethodInvocation object, Collection<ConfigAttribute> attributes) {

        if (!authentication.isAuthenticated()) {
            return AccessDecisionVoter.ACCESS_ABSTAIN;
        }

        if (!SecurityUserDetails.class.isAssignableFrom(authentication.getDetails().getClass())) {
            return AccessDecisionVoter.ACCESS_ABSTAIN;
        }

        Plugin plugin = AnnotationUtils.findAnnotation(object.getMethod(), Plugin.class);

        if (plugin == null) {
            return AccessDecisionVoter.ACCESS_ABSTAIN;
        }

        List<String> resourceTypes = Arrays
                .stream(plugin.sources())
                .filter(s -> !grantedSources.contains(s))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(resourceTypes)) {
            return AccessDecisionVoter.ACCESS_GRANTED;
        }

        if (SecurityUserDetails.class.isAssignableFrom(authentication.getDetails().getClass())) {

            SecurityUserDetails userDetails = Casts.cast(authentication.getDetails(), SecurityUserDetails.class);

            if (!resourceTypes.contains(userDetails.getType())) {
                return AccessDecisionVoter.ACCESS_DENIED;
            } else {
                return AccessDecisionVoter.ACCESS_GRANTED;
            }
        }

        return AccessDecisionVoter.ACCESS_ABSTAIN;
    }

    /**
     * 获取默认同意的来源类型
     *
     * @return 默认同意的来源类型
     */
    public List<String> getGrantedSources() {
        return grantedSources;
    }

    /**
     * 设置默认同意的来源类型
     *
     * @param grantedSources 默认同意的来源类型
     */
    public void setGrantedSources(List<String> grantedSources) {
        this.grantedSources = grantedSources;
    }
}
