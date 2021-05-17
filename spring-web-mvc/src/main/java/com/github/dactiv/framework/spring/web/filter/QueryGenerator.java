package com.github.dactiv.framework.spring.web.filter;

import com.github.dactiv.framework.spring.web.filter.condition.Condition;
import com.github.dactiv.framework.spring.web.filter.condition.ConditionParser;
import com.github.dactiv.framework.spring.web.filter.generator.WildcardParser;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 查询生成器，用于 http 提交 filter_ 前缀参数时，根据条件内容生成已给符合查询内容的对象
 *
 * @param <T> 查询结果类型
 */
public interface QueryGenerator<T> {

    /**
     * 生成内容
     *
     * @param conditions 条件信息
     *
     * @return 查询结果类型
     */
    T generate(List<Condition> conditions);

    /**
     * 获取通配符解析器集合
     *
     * @return 通配符解析器集合
     */
    List<WildcardParser> getWildcardParserList();

    /**
     * 获取条件解析器集合
     *
     * @return 条件解析器集合
     */
    List<ConditionParser> getConditionParserList();

    /**
     * 通过 request 创建指定的查询对象
     *
     * @param request http servlet request
     *
     * @return 指定的查询对象
     */
    default T createQueryWrapperFromHttpRequest(HttpServletRequest request) {

        Map<String, String[]> parameterMap = request.getParameterMap();

        Map<String, List<Object>> newValue = new LinkedHashMap<>();

        parameterMap.forEach((key, value) -> newValue.put(key, Arrays.asList(value)));

        return createQueryWrapperFromMap(new LinkedMultiValueMap<>(newValue));
    }

    /**
     * 通过 MultiValueMap 创建指定的查询对象
     *
     * @param columnMap MultiValueMap
     *
     * @return 指定的查询对象
     */
    default T createQueryWrapperFromMap(MultiValueMap<String, Object> columnMap) {
        // 创建条件
        List<Condition> conditions = columnMap
                .entrySet()
                .stream()
                // 过滤掉空的值
                .filter(e -> Objects.nonNull(e.getValue()))
                .flatMap(e ->
                        getConditionParserList()
                                .stream()
                                // 如果支持参数，就执行 getCondition 方法
                                .filter(c -> c.isSupport(e.getKey()))
                                .flatMap(c -> c.getCondition(e.getKey(), columnMap.get(e.getKey())).stream())
                )
                .collect(Collectors.toList());

        return generate(conditions);
    }
}
