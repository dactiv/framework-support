package com.github.dactiv.framework.spring.security.plugin;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.security.enumerate.ResourceSource;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.prepost.PreInvocationAttribute;
import org.springframework.security.core.Authentication;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 用户类型表决器实现，用于判断当前 controller 方法里面是否带有 {@link Plugin} 注解的记录是否符合当前用户调用
 *
 * @author maurice
 */
public class PluginSourceTypeVoter implements AccessDecisionVoter<MethodInvocation> {

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

        List<String> resourceTypes = Arrays.asList(plugin.sources());

        if (resourceTypes.contains(ResourceSource.All.toString())) {
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

}
