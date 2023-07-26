package com.github.dactiv.framework.spring.web.result.filter.holder;

import com.github.dactiv.framework.spring.web.result.filter.holder.strategy.ThreadLocalFilterResultHolderStrategy;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * 值持有者
 *
 * @author maurice.chen
 */
public class FilterResultHolder {

    public static final String MODE_THREAD_LOCAL = "MODE_THREAD_LOCAL";

    public static final String SYSTEM_PROPERTY = "jackson.exclude.holder.strategy";

    private static String strategyName = System.getProperty(SYSTEM_PROPERTY);

    private static FilterResultHolderStrategy strategy;

    private static int initializeCount = 0;

    static {
        // 初始化
        initialize();
    }

    /**
     * 获取初始化次数
     *
     * @return 初始化次数
     */
    public static int getInitializeCount() {
        return initializeCount;
    }

    /**
     * 初始化策略实现类
     */
    private static void initialize() {

        // 如果没有配置策略名称，设置默认使用本地线程資源
        if (StringUtils.isBlank(strategyName)) {
            strategyName = MODE_THREAD_LOCAL;
        }

        // 如果策略名称为本地线程資源，创建本地线程資源
        if (strategyName.equals(MODE_THREAD_LOCAL)) {
            strategy = new ThreadLocalFilterResultHolderStrategy();
        } else {
            // 否则尝试去获取自定义的策略实现类
            try {
                Class<?> clazz = Class.forName(strategyName);
                Constructor<?> customStrategy = clazz.getConstructor();
                strategy = (FilterResultHolderStrategy) customStrategy.newInstance();
            } catch (Exception ex) {
                ReflectionUtils.handleReflectionException(ex);
            }
        }

        initializeCount++;
    }

    /**
     * 获取当前值
     *
     * @return 当前值
     */
    public static List<String> get() {
        List<String> strings = strategy.get();

        if (CollectionUtils.isEmpty(strings)) {
            strings = new ArrayList<>();
            strategy.set(strings);
        }

        return strings;
    }

    /**
     * 清除当前值
     */
    public static void clear() {
        strategy.clear();
    }

    /**
     * 设置当前值
     *
     * @param value 值
     */
    public static void set(List<String> value) {
        strategy.set(value);
    }

    /**
     * 添加值
     *
     * @param value 值
     */
    public static void add(String value) {
        if (StringUtils.isBlank(value)) {
            return;
        }

        List<String> strings = get();
        if (strings.contains(value)) {
            return;
        }

        strings.add(value);
    }

    /**
     * 设置策略名称
     *
     * @param strategyName 策略名称
     */
    public static void setStrategyName(String strategyName) {
        FilterResultHolder.strategyName = strategyName;
        initialize();
    }

    /**
     * 获取当前策略实现类
     *
     * @return 当前策略实现类
     */
    public static FilterResultHolderStrategy getContextHolderStrategy() {
        return strategy;
    }

}
