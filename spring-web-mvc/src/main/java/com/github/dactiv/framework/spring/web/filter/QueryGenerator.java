package com.github.dactiv.framework.spring.web.filter;

import com.github.dactiv.framework.spring.web.filter.condition.Condition;
import com.github.dactiv.framework.spring.web.filter.condition.ConditionParser;
import com.github.dactiv.framework.spring.web.filter.generator.WildcardParser;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
     * 通过 request 获取指定的查询对象
     *
     * @param request http servlet request
     *
     * @return 指定的查询对象
     */
    default T getQueryWrapperFromHttpRequest(HttpServletRequest request) {

        Map<String, String[]> parameterMap = request.getParameterMap();

        // 创建条件
        List<Condition> conditions = parameterMap
                .entrySet()
                .stream()
                // 过滤掉空的值
                .filter(e -> Arrays.stream(e.getValue()).allMatch(StringUtils::isNoneEmpty))
                .flatMap(e ->
                        getConditionParserList()
                                .stream()
                                // 如果支持参数，就执行 getCondition 方法
                                .filter(c -> c.isSupport(e.getKey()))
                                .flatMap(c -> c.getCondition(e.getKey(), Arrays.asList(e.getValue())).stream())
                )
                .collect(Collectors.toList());

        return generate(conditions);
    }
}
